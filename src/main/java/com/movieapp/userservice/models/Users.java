package com.movieapp.userservice.models;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
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
	
	public Users() {}
	public Users(String firstName,String lastName,String username,String email,String password,int age,String mobile,Gender gender,List<Role> roles,AuthProvider provider) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.password = password;
		this.age = age;
		this.mobile = mobile;
		this.roles = roles;
		this.provider = provider;
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public AuthProvider getProvider() {
		return provider;
	}
	public void setProvider(AuthProvider provider) {
		this.provider = provider;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	public UUID getId() {
		return userId;
	}
}
