# Authentication and session flow

This document describes the current authentication design in MovieUserService. It is a useful reference when implementing the same pattern in another application.

## Overview

The application has two ways to establish an application login:

1. Local login with a username/email and password.
2. Google OAuth 2.0 login.

Both flows finish by issuing **application-owned credentials**:

- A short-lived JWT access token.
- A long-lived random refresh token, represented by a server-side session record.

Google proves a user's identity; it does not become the credential used for ordinary API authorization in this application.

```text
Local password or Google identity
            |
            v
       Local Users record
            |
            v
Access JWT + refresh-token session
            |
            v
JWTFilter authenticates later protected API requests
```

## Local user registration and login

### Registration

`POST /api/v1/auth/register` creates a local user.

1. The application checks that email and mobile number are not already used.
2. The submitted password is hashed with BCrypt before persistence.
3. The original password is never stored in the database.

### Login

`POST /api/v1/auth/login` accepts a username/email and password.

```text
Request credentials
  -> AuthenticationManager
  -> DaoAuthenticationProvider
  -> MyUserDetailsService loads the local user
  -> BCrypt verifies the submitted password against its stored hash
  -> UserService issues the application's access and refresh credentials
```

The login route is public so that a user can begin a session. It is deliberately skipped by `JWTFilter`; a JWT does not exist yet.

## Access token (JWT)

After successful local or OAuth login, `JWTService` creates an RSA-signed (RS256) access JWT.

Current contents include:

- Subject: the user's stable identifier (email in the current design).
- Email.
- Roles.
- Issued-at time.
- Expiry time (currently one hour).

The frontend sends it on protected API calls:

```http
Authorization: Bearer <access-token>
```

### Authentication of later requests

`JWTFilter` runs before the application controller for applicable requests.

1. It looks for a Bearer token.
2. It verifies the JWT signature and expiry.
3. It reads the subject and reloads the user from the database.
4. It converts database roles to Spring Security authorities, such as `ROLE_USER` and `ROLE_ADMIN`.
5. It places an authenticated principal in `SecurityContextHolder`.

After that, Spring Security authorization rules decide whether the request may continue. Requests with no valid JWT do not receive an authenticated security context; routes requiring `.authenticated()` are rejected.

The database is the source of truth for authorities in the current implementation. This means a changed role can apply on the next request, rather than waiting for every existing access token to expire.

## Refresh-token session management

Access tokens expire quickly. A refresh token allows the application to issue a new access token without asking the user to log in again.

When login succeeds, the application:

1. Generates a cryptographically random UUID value as the raw refresh token.
2. Hashes that value with SHA-256.
3. Saves only the hash in `UserSession`, with user, browser, OS, device, IP address, login time, expiry, and revoked status.
4. Sends the raw refresh token only to the browser in a cookie.

### Why store a hash instead of the refresh token?

Refresh tokens are long-lived bearer credentials: anyone who obtains one can exchange it for a new access token. Saving only a hash provides defence in depth:

- A database dump or accidental database/log exposure does not reveal a directly usable refresh token.
- On refresh, the supplied raw token is hashed and matched to the stored hash.
- The server can still revoke a session by marking its stored record as revoked or removing it.

This is analogous to password storage: verify a derived value rather than retaining the secret itself.

### Refresh flow

```text
Browser sends RefreshToken cookie
  -> POST /api/v1/auth/refresh
  -> hash submitted token
  -> locate UserSession by hash
  -> reject missing, expired, or revoked session
  -> generate a new access JWT
  -> return the new access token
```

The refresh token is not intended to be read by frontend JavaScript.

### Cookie protections

The refresh-token cookie should be configured with:

- `HttpOnly`: JavaScript cannot read it, limiting token theft through XSS.
- `Secure`: the browser sends it only over HTTPS. Use this in production; local plain HTTP development may need a development-only setting.
- `SameSite=Strict`: limits cross-site sending of the cookie and helps mitigate CSRF.
- `Path=/api/v1/auth`: sends the cookie only to the authentication endpoints that need it. The path must begin with `/`.
- A finite max age matching the intended session lifetime.

Cookie flags reduce risk but do not replace XSS protection, HTTPS, input validation, or server-side session checks.

### Logout and session revocation

`POST /api/v1/auth/logout` invalidates the server-side refresh session and expires the browser cookie. Once revoked, the refresh token cannot obtain a new access token.

An already-issued access JWT remains valid until it expires. This is the normal trade-off of short-lived stateless access tokens; use a short expiry or an access-token deny-list if immediate access-token revocation is required.

## Google OAuth flow

Google OAuth authenticates the user with Google, then the app exchanges that external identity for its own credentials.

```text
Frontend/browser
  -> /oauth2/authorization/google
  -> Google login and consent
  -> /login/oauth2/code/google
  -> CustomOAuth2UserService loads the Google profile
  -> CustomOAuth2SuccessHandler
       -> find local user by verified email
       -> create local user with provider GOOGLE and USER role if absent
       -> create a random one-time loginCode (60-second expiry)
       -> redirect frontend with loginCode
  -> frontend calls /api/v1/auth/token?loginCode=...
  -> application consumes code, loads local user,
     and issues its access JWT plus refresh-token session
```

The success handler redirects rather than returning JSON because the OAuth callback is a browser navigation. The frontend cannot directly consume a normal JSON response from that redirect in the same way it can consume an API call.

### One-time login code

The `loginCode` is an application-generated exchange code, not Google's authorization code. Its properties are important:

- Generated with `SecureRandom`.
- Sent once to the frontend via redirect.
- Expires after 60 seconds.
- Removed from the in-memory store immediately after successful consumption.

The frontend should exchange it promptly, then remove it from the address bar with `history.replaceState()`.

## Authorization

Authentication establishes **who** the user is. Authorization decides **what** that authenticated user may access.

- Public authentication, OAuth, and documentation endpoints are permitted by `SecurityConfiguration`.
- All other routes require an authenticated security context.
- The root greeting endpoint additionally uses `@PreAuthorize("hasRole('ADMIN')")`, so it requires `ROLE_ADMIN`.

For a path family to be public, use a wildcard matcher such as `/public/**`, not only `/public`, which matches only that exact path.

## Implementation checklist for another application

- Hash passwords with BCrypt/Argon2; never store them as plaintext.
- Use short-lived, signed access JWTs.
- Use random, long-lived refresh tokens; store only hashes server-side.
- Store session expiry and revocation state, and check both on refresh.
- Send refresh tokens in `HttpOnly`, `Secure`, `SameSite` cookies over HTTPS.
- Use a stable, non-null identifier as the JWT subject for both local and OAuth users.
- Fetch roles safely when building Spring Security authorities (for example, an `@EntityGraph` on the user lookup query).
- Verify the OAuth provider's email before automatically linking or creating a local account.
- Decide and document whether a matching local-email account may automatically be linked to an OAuth identity.
- Use a durable shared store (database or Redis) for OAuth exchange codes when the application has multiple instances; an in-memory map is single-instance only.
- Keep OAuth `state`/PKCE protection enabled; Spring Security's OAuth client handles this part of the provider login flow.
