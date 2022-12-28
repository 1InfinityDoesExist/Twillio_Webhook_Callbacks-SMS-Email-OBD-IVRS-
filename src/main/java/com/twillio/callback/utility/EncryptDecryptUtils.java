package com.twillio.callback.utility;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EncryptDecryptUtils {

	private EncryptDecryptUtils() {

	}

	public static String encrypt(String plainText, String cipherTransformation, String characterEncoding,
			String aesEncryptionAlgorithem, String encryptionKey) {
		log.info("-----Inside ENcryptDecrytString Class, encrypt-----");
		String encryptedText = "";
		try {
			log.info("-----CipherTransformation {}", cipherTransformation);
			Cipher cipher = Cipher.getInstance(cipherTransformation);
			byte[] key = encryptionKey.getBytes(characterEncoding);
			SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
			IvParameterSpec ivparameterspec = new IvParameterSpec(key);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
			byte[] cipherText = cipher.doFinal(plainText.getBytes(characterEncoding));
			Base64.Encoder encoder = Base64.getEncoder();
			encryptedText = encoder.encodeToString(cipherText);
		} catch (Exception e) {
			log.error("Encryption Exception : {}", e);
			throw new RuntimeException("--------------------------");
		}
		return encryptedText;
	}

	public static String decrypt(String encryptedText, String cipherTransformation, String characterEncoding,
			String aesEncryptionAlgorithem, String encryptionKey) {
		String decryptedText = "";
		log.info("-----Text to be decrypted {}", encryptedText);
		try {
			Cipher cipher = Cipher.getInstance(cipherTransformation);
			byte[] key = encryptionKey.getBytes(characterEncoding);
			SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithem);
			IvParameterSpec ivparameterspec = new IvParameterSpec(key);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] cipherText = decoder.decode(encryptedText.getBytes(characterEncoding));
			decryptedText = new String(cipher.doFinal(cipherText), characterEncoding);
		} catch (Exception e) {
			log.error("Decryption Exception : {}", e);
			throw new RuntimeException("--------------------------");
		}
		log.info("-----DecryptedText {}", decryptedText);
		return decryptedText;
	}
}