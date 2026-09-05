package com.movieapp.userservice.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    private String refreshToken;

    private String browser;

    public Users getUser() {
		return user;
	}


	public Boolean getRevoked() {
		return revoked;
	}


	public void setRevoked(Boolean revoked) {
		this.revoked = revoked;
	}


	public void setUserId(Users user) {
		this.user = user;
	}

	private String os;

    private String deviceType;

    public UserSession(String refreshToken, String browser, String os, String deviceType, String ipAddress,
			LocalDateTime loginTime, LocalDateTime expiresAt) {
		super();
		this.refreshToken = refreshToken;
		this.browser = browser;
		this.os = os;
		this.deviceType = deviceType;
		this.ipAddress = ipAddress;
		this.loginTime = loginTime;
		this.expiresAt = expiresAt;
	}

	private String ipAddress;

    private LocalDateTime loginTime;

    private LocalDateTime expiresAt;
    
    
    private Boolean revoked;
    
    @PrePersist
    private void applyDefaults() {
    	if(revoked == null) revoked = false;
    }
    
    
    // Default constructor
    public UserSession() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}