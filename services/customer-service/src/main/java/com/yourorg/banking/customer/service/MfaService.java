package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.MfaSecret;
import com.yourorg.banking.customer.repo.MfaSecretRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class MfaService {

    private final MfaSecretRepository mfaSecretRepository;
    private final SecureRandom secureRandom;

    public MfaService(MfaSecretRepository mfaSecretRepository) {
        this.mfaSecretRepository = mfaSecretRepository;
        this.secureRandom = new SecureRandom();
    }

    public MfaSecret generateMfaSecret(UUID customerId) {
        String secretKey = generateSecretKey();
        List<String> backupCodes = generateBackupCodes();
        
        MfaSecret secret = new MfaSecret(
                UUID.randomUUID(),
                customerId,
                secretKey,
                backupCodes,
                Instant.now(),
                Instant.now()
        );
        
        mfaSecretRepository.save(secret);
        return secret;
    }

    public boolean verifyMfaCode(UUID customerId, String code) {
        return mfaSecretRepository.findByCustomerId(customerId)
                .map(secret -> verifyTotpCode(secret.secretKey(), code) || verifyBackupCode(secret.backupCodes(), code))
                .orElse(false);
    }

    public boolean verifyBackupCode(UUID customerId, String code) {
        return mfaSecretRepository.findByCustomerId(customerId)
                .map(secret -> {
                    if (verifyBackupCode(secret.backupCodes(), code)) {
                        // Remove used backup code
                        List<String> updatedCodes = new ArrayList<>(secret.backupCodes());
                        updatedCodes.remove(code);
                        
                        MfaSecret updatedSecret = new MfaSecret(
                                secret.id(),
                                secret.customerId(),
                                secret.secretKey(),
                                updatedCodes,
                                secret.createdAt(),
                                Instant.now()
                        );
                        mfaSecretRepository.save(updatedSecret);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public String generateQrCodeUrl(UUID customerId, String email) {
        return mfaSecretRepository.findByCustomerId(customerId)
                .map(secret -> {
                    String issuer = "Digital Bank";
                    String accountName = email;
                    return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                            issuer, accountName, secret.secretKey(), issuer);
                })
                .orElse(null);
    }

    public void deleteMfaSecret(UUID customerId) {
        mfaSecretRepository.deleteByCustomerId(customerId);
    }

    private String generateSecretKey() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(generateBackupCode());
        }
        return codes;
    }

    private String generateBackupCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    private boolean verifyTotpCode(String secretKey, String code) {
        try {
            long currentTime = System.currentTimeMillis() / 1000 / 30; // 30-second window
            
            // Check current window and previous/next window for clock drift
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(secretKey, currentTime + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateTotpCode(String secretKey, long time) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] key = Base64.getDecoder().decode(secretKey);
        byte[] timeBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            timeBytes[i] = (byte) (time & 0xFF);
            time >>= 8;
        }

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA1");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0xF;
        int code = ((hash[offset] & 0x7F) << 24) |
                   ((hash[offset + 1] & 0xFF) << 16) |
                   ((hash[offset + 2] & 0xFF) << 8) |
                   (hash[offset + 3] & 0xFF);

        code = code % 1000000; // 6-digit code
        return String.format("%06d", code);
    }

    private boolean verifyBackupCode(List<String> backupCodes, String code) {
        return backupCodes.contains(code);
    }
}

