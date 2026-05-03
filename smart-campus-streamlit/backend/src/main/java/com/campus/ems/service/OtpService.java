

package com.campus.ems.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class OtpService {
    private static final String CHARS = "0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) otp.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return otp.toString();
    }
}
