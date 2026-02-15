package com.movieapp.userservice.configurations;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.movieapp.userservice.filters.JWTFilter;
import com.movieapp.userservice.services.CustomOAuth2SuccessHandler;
import com.movieapp.userservice.services.CustomOAuth2UserService;
import com.movieapp.userservice.services.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	
	AuthenticationConfiguration authConfig;
	MyUserDetailsService userDetailsService;
	JWTFilter jwtFilter;
	CustomOAuth2UserService oAuth2UserService;
	CustomOAuth2SuccessHandler oAuth2SuccessHandler;
	
	
	public SecurityConfiguration(AuthenticationConfiguration authConfig,MyUserDetailsService userDetailsService,JWTFilter jwtFilter,CustomOAuth2UserService oAuth2UserService,CustomOAuth2SuccessHandler oAuth2SuccessHandler) {
		this.authConfig = authConfig;
		this.userDetailsService = userDetailsService;
		this.jwtFilter = jwtFilter;
		this.oAuth2SuccessHandler = oAuth2SuccessHandler;
		this.oAuth2UserService = oAuth2UserService;
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
		return security
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		.csrf(csrf -> csrf.disable())
		.formLogin(loginForm -> loginForm.disable())
		.httpBasic(basic -> basic.disable())
		.authorizeHttpRequests(request ->{
			request
			.requestMatchers("/api/v1/auth/**","/api/v1/auth/login","/public/**","/auth/oauth/**","/oauth2/**","/login/oauth2/**","/swagger-ui.html",
			        "/swagger-ui/**",
			        "/v3/api-docs",
			        "/v3/api-docs/**",
			        "/webjars/**")
			.permitAll()
			.anyRequest().authenticated();
		})
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authenticationProvider(authenticationProvider())
		.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
		.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(oAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )
		.build();
	}
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
				"http://localhost:3000"
				));
		config.setAllowedMethods(List.of(
				"GET","PUT","POST","DELETE","PATCH","OPTIONS"
				));
		config.setAllowedHeaders(List.of(
				"Authorization","Content-Type"
				));
		config.setExposedHeaders(List.of(
				"Authorization"
				));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		
		return (CorsConfigurationSource) source;
		
	}
	
	@Bean
	AuthenticationManager authenticationManager() throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
		
		
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
