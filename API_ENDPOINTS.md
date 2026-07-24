# Movie User Service API Endpoints

Base URL for local development:

```text
http://localhost:8080
```

Most responses use JSON. Auth endpoints set and read a refresh token using the `RefreshToken` HTTP-only cookie, so browser/frontend calls must include credentials.

```ts
fetch(url, { credentials: "include" })
```

For protected endpoints, send the JWT access token returned by auth endpoints:

```http
Authorization: Bearer <accessToken>
```

## Common Models

### AuthResponse

Returned by login, refresh, and login-code token exchange.

```json
{
  "accessToken": "jwt-access-token",
  "roles": ["USER"],
  "userId": "uuid"
}
```

### Error Message

Several endpoints return a simple message object for failures.

```json
{
  "message": "Error message"
}
```

## Authentication

Base path: `/api/v1/auth`

These endpoints are public in `SecurityConfiguration`.

### POST `/api/v1/auth/login`

Authenticates a user with username or email and password. On success, returns an access token and sets a `RefreshToken` HTTP-only cookie.

Request body:

```json
{
  "username": "john@example.com",
  "password": "password123"
}
```

Success: `200 OK`

```json
{
  "accessToken": "jwt-access-token",
  "roles": ["USER"],
  "userId": "uuid"
}
```

Possible errors:

| Status | Description |
| --- | --- |
| `401` | Invalid credentials |
| `500` | Internal server error |

### POST `/api/v1/auth/register`

Creates a new local user account.

Request body:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "age": 25,
  "mobile": "9876543210",
  "gender": "MALE",
  "roles": ["USER"],
  "provider": "LOCAL"
}
```

Success: `201 Created`

```json
{
  "message": "User created successfully"
}
```

Possible errors:

| Status | Description |
| --- | --- |
| `400` | Invalid input data |
| `409` | User already exists |
| `500` | Internal server error |

Allowed enum values:

| Field | Values |
| --- | --- |
| `gender` | `MALE`, `FEMALE`, `OTHERS` |
| `roles` | `ADMIN`, `MANAGER`, `USER` |
| `provider` | `LOCAL`, `GOOGLE`, `GITHUB` |

### GET `/api/v1/auth/token`

Exchanges a temporary OAuth login code for an `AuthResponse`. On success, also creates a user session and sets a `RefreshToken` HTTP-only cookie.

Query params:

| Name | Required | Description |
| --- | --- | --- |
| `loginCode` | Yes | Temporary code generated after OAuth login |

Example:

```http
GET /api/v1/auth/token?loginCode=abc123
```

Success: `200 OK`

```json
{
  "accessToken": "jwt-access-token",
  "roles": ["USER"],
  "userId": "uuid"
}
```

Possible errors:

| Status | Description |
| --- | --- |
| `400` | Login code expired or invalid |
| `500` | Internal server error |

### POST `/api/v1/auth/refresh`

Validates the `RefreshToken` cookie and issues a new JWT access token.

Request requirements:

| Item | Required | Description |
| --- | --- | --- |
| `RefreshToken` cookie | Yes | HTTP-only cookie set by login/token exchange |

Success: `200 OK`

```json
{
  "accessToken": "new-jwt-access-token",
  "roles": ["USER"],
  "userId": "uuid"
}
```

Possible errors:

| Status | Description |
| --- | --- |
| `401` | Missing, invalid, or expired refresh token |
| `500` | Internal server error |

### POST `/api/v1/auth/logout`

Invalidates the current refresh-token session and should clear the refresh-token cookie.

Request requirements:

| Item | Required | Description |
| --- | --- | --- |
| `RefreshToken` cookie | Yes | Current session refresh token |

Success: `200 OK`

```json
{
  "message": "Logged out successfully"
}
```

Frontend behavior should also clear local auth state, including access token and user details from local storage.

## OAuth2 Login

Spring Security handles OAuth2 login routes.

### GET `/oauth2/authorization/google`

Starts Google OAuth login. This route is provided by Spring Security OAuth2 client.

After successful Google login, `CustomOAuth2SuccessHandler` generates a temporary `loginCode` and redirects to the configured frontend URL:

```text
<app.frontend.url>?loginCode=<temporary-code>
```

The frontend should then call:

```http
GET /api/v1/auth/token?loginCode=<temporary-code>
```

## Movies

Base path: `/public/movie`

These endpoints are public.

### GET `/public/movie/{id}`

Returns one movie by ID.

Path variables:

| Name | Type | Description |
| --- | --- | --- |
| `id` | Integer | Movie ID |

Success: `200 OK`

```json
{
  "id": 1,
  "title": "Inception",
  "year": 2010,
  "genre": "SCI_FI",
  "rating": 8.8,
  "votes": 2000000,
  "director": "Christopher Nolan",
  "actor1": "Leonardo DiCaprio",
  "actor2": "Joseph Gordon-Levitt",
  "durationMin": 148,
  "budgetMillion": 160.0,
  "revenueMillion": 839.0
}
```

Possible errors:

| Status | Description |
| --- | --- |
| `400` | Movie lookup failed |

### GET `/public/movie/movies`

Returns movies by genre.

Query params:

| Name | Required | Description |
| --- | --- | --- |
| `genre` | Yes | Movie genre enum |

Example:

```http
GET /public/movie/movies?genre=ACTION
```

Success: `200 OK`

```json
[
  {
    "id": 1,
    "title": "Inception",
    "year": 2010,
    "genre": "SCI_FI",
    "rating": 8.8,
    "votes": 2000000,
    "director": "Christopher Nolan",
    "actor1": "Leonardo DiCaprio",
    "actor2": "Joseph Gordon-Levitt",
    "durationMin": 148,
    "budgetMillion": 160.0,
    "revenueMillion": 839.0
  }
]
```

Allowed genre values:

```text
ACTION, DRAMA, COMEDY, THRILLER, SCI_FI, ROMANCE, HORROR
```

Possible errors:

| Status | Description |
| --- | --- |
| `400` | Genre lookup failed |

## Public Test Endpoint

### POST `/public`

Simple public test endpoint that returns a greeting using the submitted username.

Request body:

```json
{
  "username": "john",
  "password": "password123"
}
```

Success: `200 OK`

```text
Hellojohn
```

## Protected User Endpoint

### GET `/`

Protected test endpoint. Requires a valid JWT access token and `ADMIN` role.

Headers:

```http
Authorization: Bearer <accessToken>
```

Success: `200 OK`

```text
Hi User welcome !!
```

Possible errors:

| Status | Description |
| --- | --- |
| `401` | Missing or invalid JWT |
| `403` | Authenticated user does not have `ADMIN` role |

## Swagger/OpenAPI

Swagger UI and OpenAPI docs are public.

| Endpoint | Description |
| --- | --- |
| `/swagger-ui.html` | Swagger UI entry point |
| `/swagger-ui/**` | Swagger UI assets |
| `/v3/api-docs` | OpenAPI JSON |
| `/v3/api-docs/**` | Grouped OpenAPI docs |

## CORS Notes

Current CORS configuration allows:

| Setting | Value |
| --- | --- |
| Origin | `http://localhost:3000` |
| Methods | `GET`, `PUT`, `POST`, `DELETE`, `PATCH`, `OPTIONS` |
| Headers | `Authorization`, `Content-Type` |
| Credentials | Enabled |

Because credentials are enabled, frontend requests that need the refresh cookie must use `credentials: "include"`.

