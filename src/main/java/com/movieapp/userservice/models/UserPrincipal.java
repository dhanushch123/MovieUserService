package com.movieapp.userservice.models;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.movieapp.userservice.utils.MapPermissions;

public class UserPrincipal implements UserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Users user;
	
	public UserPrincipal(Users user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
//		Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream().map((role)->{
//			return new SimpleGrantedAuthority("ROLE_" + role.name());
//		}).toList();
		Set<SimpleGrantedAuthority> authorities = user.getRoles()
				.stream()
				.map(r -> MapPermissions.getAuthorities(r))
				.flatMap(Collection :: stream)
				.collect(Collectors.toSet());
		user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name())));
		return authorities;
				
	}

	@Override
	public  String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

}
