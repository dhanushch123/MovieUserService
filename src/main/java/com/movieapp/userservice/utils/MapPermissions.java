package com.movieapp.userservice.utils;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.movieapp.userservice.models.Permissions;
import com.movieapp.userservice.models.Role;

public class MapPermissions {
	
	static Map<Role,Set<Permissions>> map;
	
	static {
		map = Map.of(
				Role.USER , Set.of(Permissions.GET_TEST) ,
				Role.ADMIN , Set.of(Permissions.GET_TEST,Permissions.POST_TEST)
				);
	}
		
	
	
	public static Set<SimpleGrantedAuthority> getAuthorities(Role role) {
		return map.get(role).stream()
				.map(p -> new SimpleGrantedAuthority(p.toString()))
				.collect(Collectors.toSet());
	}
}
