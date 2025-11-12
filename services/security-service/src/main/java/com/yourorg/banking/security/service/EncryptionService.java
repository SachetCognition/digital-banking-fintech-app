package com.yourorg.banking.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class EncryptionService {

    private final StringEncryptor stringEncryptor;
    private final String encryptionKey;
    private final String algorithm;
    private final int keyLength;
    private final int ivLength;
    private final int tagLength;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public EncryptionService(@Value("${app.security.encryption.key}") String encryptionKey,
                           @Value("${app.security.encryption.algorithm}") String algorithm,
                           @Value("${app.security.encryption.key-derivation.iterations}") int iterations,
                           @Value("${app.security.encryption.key-derivation.salt-length}") int saltLength) {
        this.encryptionKey = encryptionKey;
        this.algorithm = algorithm;
        this.keyLength = 256; // AES-256
        this.ivLength = 12; // 96 bits for GCM
        this.tagLength = 128; // 128 bits for GCM

        // Initialize Jasypt encryptor for field-level encryption
        this.stringEncryptor = createStringEncryptor(encryptionKey, iterations, saltLength);
    }

    private StringEncryptor createStringEncryptor(String password, int iterations, int saltLength) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithHmacSHA256AndAES_256");
        config.setKeyObtentionIterations(iterations);
        config.setPoolSize(1);
        config.setProviderName("BC");
        config.setSaltGenerator(new RandomSaltGenerator());
        config.setIvGenerator(new RandomIvGenerator());
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * Encrypt sensitive data using AES-256-GCM
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);

            // Create secret key from password
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(algorithm, "BC");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            byte[] encryptedData = new byte[ivLength + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, ivLength);
            System.arraycopy(ciphertext, 0, encryptedData, ivLength, ciphertext.length);

            return Base64.toBase64String(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt sensitive data using AES-256-GCM
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        try {
            // Decode from Base64
            byte[] encrypted = Base64.decode(encryptedData);

            // Extract IV and ciphertext
            byte[] iv = Arrays.copyOfRange(encrypted, 0, ivLength);
            byte[] ciphertext = Arrays.copyOfRange(encrypted, ivLength, encrypted.length);

            // Create secret key from password
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(algorithm, "BC");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Encrypt using Jasypt for field-level encryption
     */
    public String encryptField(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        return stringEncryptor.encrypt(plaintext);
    }

    /**
     * Decrypt using Jasypt for field-level encryption
     */
    public String decryptField(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        return stringEncryptor.decrypt(encryptedText);
    }

    /**
     * Hash sensitive data for searching (one-way)
     */
    public String hash(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        return stringEncryptor.encrypt(plaintext); // Using encrypt as hash for searchability
    }

    /**
     * Generate a secure random key
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keyLength);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.toBase64String(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Generate a secure random IV
     */
    public String generateIV() {
        byte[] iv = new byte[ivLength];
        new SecureRandom().nextBytes(iv);
        return Base64.toBase64String(iv);
    }

    /**
     * Check if data is encrypted (basic check)
     */
    public boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        try {
            // Try to decode as Base64 and check length
            byte[] decoded = Base64.decode(data);
            return decoded.length > ivLength; // Should have IV + ciphertext
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mask sensitive data for logging
     */
    public String maskSensitiveData(String data, String dataType) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        switch (dataType.toLowerCase()) {
            case "ssn":
                return maskSSN(data);
            case "creditcard":
            case "cardnumber":
                return maskCreditCard(data);
            case "bankaccount":
            case "accountnumber":
                return maskAccountNumber(data);
            case "email":
                return maskEmail(data);
            case "phone":
                return maskPhone(data);
            default:
                return maskGeneric(data);
        }
    }

    private String maskSSN(String ssn) {
        if (ssn.length() < 4) return "***-**-****";
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }

    private String maskCreditCard(String cardNumber) {
        if (cardNumber.length() < 4) return "****-****-****-****";
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber.length() < 4) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    private String maskEmail(String email) {
        if (!email.contains("@")) return "***@***.***";
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return "***@" + parts[1];
        return parts[0].charAt(0) + "***" + parts[0].charAt(parts[0].length() - 1) + "@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone.length() < 4) return "***-***-****";
        return "***-***-" + phone.substring(phone.length() - 4);
    }

    private String maskGeneric(String data) {
        if (data.length() <= 4) return "****";
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }
}

