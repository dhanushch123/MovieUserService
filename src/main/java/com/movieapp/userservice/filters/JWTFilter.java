package com.movieapp.userservice.filters;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.movieapp.userservice.services.JWTService;
import com.movieapp.userservice.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTFilter extends OncePerRequestFilter{
	
	private final MyUserDetailsService userDetailsService;
	private final JWTService jwtService;
	
	public JWTFilter(MyUserDetailsService userDetailsService,JWTService jwtService) {
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
	    String path = request.getServletPath(); 
//	    path.startsWith("/public");
	     return path.startsWith("/api/v1/auth/") ||
	    	    path.startsWith("/oauth2/") ||
	    	    path.startsWith("/login/oauth2/") ||
	    	    path.startsWith("/swagger-ui/") ||
	    	    path.equals("/swagger-ui.html") ||
	    	    path.startsWith("/v3/api-docs") ||
	    	    path.startsWith("/webjars/") ||
		        path.startsWith("/public");
	     

	    	    
	    	
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		System.out.println(
	    	    "PATH: " + request.getRequestURI() +
	    	    " | DISPATCH: " + request.getDispatcherType()
	    	);
	    System.out.println("FILTER INSTANCE: " + this.hashCode());
	    
		if (request.getAttribute("JWT_FILTER_APPLIED") != null) {
		    filterChain.doFilter(request, response);
		    return;
		}
		request.setAttribute("JWT_FILTER_APPLIED", true);
		
		String authHeader = request.getHeader("Authorization");
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = authHeader.substring(7);
		boolean isTokenValid = jwtService.validateToken(token);
		if(!isTokenValid) {
			filterChain.doFilter(request, response);
			return;
		}
//		String username = jwtService.extractUsername(token);
		String email = jwtService.extractEmail(token);
		if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// we have to set the token in security context 
			UserDetails user = userDetailsService.loadUserByUsername(email);
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}
		filterChain.doFilter(request,response);
	}
	

}
