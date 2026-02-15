package com.movieapp.userservice.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService{
	UserRepository repo;
	
	public MyUserDetailsService(UserRepository repo) {
		this.repo = repo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = repo.findByUsernameOrEmail(username,username).orElseThrow(()->{
			return new UsernameNotFoundException("User not found" + username);
		});
		return new UserPrincipal(user);
	}
	
	
}
