package com.movieapp.userservice.services;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;


@Service
public class TempCodeService {
	private final ConcurrentHashMap<String, TempCodeData> store = new ConcurrentHashMap<>();
	
	public String generateCode(String email) {
		String randomCode = generateRandomCode();
		TempCodeData tempCodeData = new TempCodeData(email,System.currentTimeMillis() + 60_000);
		store.put(randomCode, tempCodeData);
		return randomCode;
	}
	
	public String consumeCode(String code) {
        TempCodeData data = store.get(code);

        if (data == null) return null;
        if (System.currentTimeMillis() > data.expiry) {
            store.remove(code);
            return null;
        }

        store.remove(code); // one-time use
        return data.email;
    }
	
	private String generateRandomCode() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
	
	record TempCodeData (String email,long expiry) {}
}
