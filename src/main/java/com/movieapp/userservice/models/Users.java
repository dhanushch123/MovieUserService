package com.movieapp.userservice.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.*;


@Getter
@Setter
@ToString(exclude = {"sessions", "posts"})
@EqualsAndHashCode(exclude = {"sessions", "posts"})@Entity
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name="user_id")
	private UUID userId;
	@Column(nullable = false,length = 50)
	private String firstName;
	@Column(nullable = false,length = 50)
	private String lastName;
	@Column(unique=true)
	private String username;
	@Column(nullable = false,unique=true)
	private String email;
	@Column(nullable = true)
	private String password;
	private int age;
	@Column(nullable = true,unique=true)
	private String mobile;
	@Enumerated(EnumType.STRING)
	private Gender gender;
	@ElementCollection
	@CollectionTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id")
			)
	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private List<Role> roles;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AuthProvider provider;
	
	private Boolean isProfileCompleted;
	private Boolean isEmailVerified;
	
	@OneToMany(mappedBy="user")
	private List<UserSession> sessions;

	@OneToMany(mappedBy="author")
	private List<Post> posts;
	
	@PrePersist
	public void applyDefaults() {
		if(isProfileCompleted == null) isProfileCompleted = false;
		if(isEmailVerified == null) isEmailVerified = false;
	}
	
	public Users() {}
	public Users(String firstName,String lastName,String username,String email,String password,int age,String mobile,Gender gender,List<Role> roles,AuthProvider provider,List<Post> posts) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.password = password;
		this.age = age;
		this.mobile = mobile;
		this.roles = roles;
		this.provider = provider;
		this.username = username;
		this.sessions = new ArrayList<>();
		this.posts = new ArrayList<>();
	}

	
	
	
}