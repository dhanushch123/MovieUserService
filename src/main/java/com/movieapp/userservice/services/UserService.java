package com.movieapp.userservice.services;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.movieapp.userservice.models.AuthResponse;
import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.UserSession;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.UserRepository;
import com.movieapp.userservice.repositories.UserSessionRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;


@Service
public class UserService {
	
	UserRepository repo;
	PasswordEncoder encoder;
	AuthenticationManager authManager;
	JWTService jwtService;
	UserSessionRepository sessionRepo;
	
	public UserService(UserRepository repo,PasswordEncoder encoder,AuthenticationManager authManager,JWTService jwtService,UserSessionRepository sessionRepo) {
		this.repo = repo;
		this.encoder = encoder;
		this.authManager = authManager;
		this.jwtService = jwtService;
		this.sessionRepo = sessionRepo;
	}
	
	public void registerUser(Users user) {
		System.out.println(user.getFirstName() + " " + user.getLastName());
		if(repo.existsByEmail(user.getEmail()) || repo.existsByMobile(user.getMobile())) {
			// User already exists with this email or mobile
			throw new RuntimeException("User already exists with same email or mobile");
		}
		user.setPassword(encoder.encode(user.getPassword()));
		user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));
		repo.save(user);
	}
	

	public AuthResponse authenticate(String username, String password,HttpServletRequest request,HttpServletResponse response) throws NoSuchAlgorithmException {
		// User may enter username or email
		System.out.println(username + " " +password);
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,password);
		Authentication authentication = authManager.authenticate(authToken);
		if(authentication.isAuthenticated()) {
			Users user = repo.findByUsernameOrEmail(username,username).get();
			return getAuthResponse(user,request,response);
		}
		throw new BadCredentialsException("Invalid Credentials");
	}
	
	public AuthResponse getAuthResponse(Users user,HttpServletRequest request,HttpServletResponse response) throws NoSuchAlgorithmException {
		String accessToken = jwtService.generateToken(user);
		createSession(request,response,user);
		return new AuthResponse(user.getUserId(),accessToken,user.getRoles().stream().map(role->role.name()).toList());
	}
	
	public void createSession(HttpServletRequest request,HttpServletResponse response,Users user) throws NoSuchAlgorithmException {
		String rawToken = UUID.randomUUID().toString();
		String refreshToken = hashRefreshToken(rawToken);
		UserAgentAnalyzer uaa = UserAgentAnalyzer
				                .newBuilder()
				                .hideMatcherLoadStats()
				                .withCache(1000)
				                .build();
		String userAgentString = request.getHeader("User-Agent");
		UserAgent agent = uaa.parse(userAgentString);
		String browser = agent.getValue("AgentName");
		String os = agent.getValue("OperatingSystemName");
		String device = agent.getValue("DeviceClass");
		String ipAddress = getClientIp(request); 
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(7);
		UserSession session = new UserSession(refreshToken,browser,os,device,ipAddress,start,end); // store hashed token
		session.setUserId(user);
		sessionRepo.save(session);
		// set refresh token in cookie 
		ResponseCookie cookie = ResponseCookie.from("RefreshToken",rawToken)
				                .httpOnly(true)
				                .secure(true)
				                .path("/api/v1/auth")
				                .maxAge(Duration.ofDays(7))
				                .sameSite("Strict")
				                .build();
		response.addHeader("Set-Cookie", cookie.toString());
		
		// clean up sessions 
	}
	
	
	
	@Scheduled(fixedRate = 3600000)
	public void cleanUpSessions() {
		sessionRepo.revokeExpiredSessions();
	}
	
	

	public Users findByEmail(String email) {
		return repo.findByEmail(email).get();
	}
	
	public String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if(xfHeader == null || xfHeader.isEmpty()) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}

	public AuthResponse refreshAccessToken(String refreshToken) throws NoSuchAlgorithmException {
		// validate Token
		String hashedToken = hashRefreshToken(refreshToken);
		UserSession session = sessionRepo.findByRefreshToken(hashedToken);
		if(session == null) throw new RuntimeException("Invalid Token");
		if(session.getExpiresAt().isBefore(LocalDateTime.now())) {
			session.setRevoked(true);
			sessionRepo.save(session);
			throw new RuntimeException("Token expired");
		}
		Users user = session.getUser();
		String accessToken = jwtService.generateToken(user);

		return new AuthResponse(user.getUserId(),accessToken,user.getRoles().stream().map(role->role.name()).toList());
		
	}

	public void invalidateSession(String refreshToken,HttpServletResponse response) throws NoSuchAlgorithmException {
		UserSession session = sessionRepo.findByRefreshToken(hashRefreshToken(refreshToken));
		System.out.println("Encoded token : " + " " + encoder.encode(refreshToken));
		System.out.println(session);
		if(session != null) {
			session.setRevoked(true);
			sessionRepo.save(session);
			ResponseCookie cookie = ResponseCookie.from("RefreshToken","")
					                .httpOnly(true)
					                .secure(true)
					                .path("/api/v1/auth/refresh")
					                .maxAge(0)
					                .build();
			response.addHeader("Set-Cookie",cookie.toString());  
			
		}
	}
	
	public String hashRefreshToken(String token) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("sha256");
		byte[] hashBytes = md.digest(token.getBytes());
		StringBuilder sb = new StringBuilder();
		for(byte b : hashBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}


	public Users findByUsername(String username) {
		return repo.findByUsername(username).get();
	}
}
