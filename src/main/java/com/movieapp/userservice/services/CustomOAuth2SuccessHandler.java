package com.movieapp.userservice.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.movieapp.userservice.models.AuthProvider;
import com.movieapp.userservice.models.Role;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler{
	
	MyUserDetailsService userDetailsService;
	JWTService jwtService;
	UserRepository repo;
	TempCodeService tempCodeService;
	public CustomOAuth2SuccessHandler(MyUserDetailsService userDetailsService,JWTService jwtService,UserRepository repo,TempCodeService tempCodeService) {
		this.userDetailsService = userDetailsService;
		this.tempCodeService = tempCodeService;
		this.jwtService = jwtService;
		this.repo = repo;
	}
	
	@Value("${app.frontend.url}")
	String redirectURL;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
		String provider = authToken.getAuthorizedClientRegistrationId(); // Get provider google,github
		OAuth2User authUser = (OAuth2User) authentication.getPrincipal();
		String firstName = authUser.getAttribute("given_name");
		String lastName = authUser.getAttribute("family_name");
		String email = authUser.getAttribute("email");
		Users DBUser = repo.findByEmail(email).orElse(null);
		Users user = new Users();
		if(DBUser == null) {
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
			user.setRoles(List.of(Role.USER));
			repo.save(user);
		}
		String loginCode = tempCodeService.generateCode(email);
		System.out.println(loginCode);
		String redirectWithToken = UriComponentsBuilder.fromUriString(redirectURL)
				                   .queryParam("loginCode", loginCode)
				                   .toUriString();
		response.sendRedirect(redirectWithToken);
		
		
		
		
		
	}

}
