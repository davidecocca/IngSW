package util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import model.Credential;

public class CryptoUtils {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";

	private static int iterations = 1000;
	private static byte[] salt = "4jMCCU51x2PIqFQf6ppf".getBytes();

	public static void encrypt(String key, TreeMap<String, Credential> map, File outputFile) throws CryptoException {
		try {
			//Key derivation function
			PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt, iterations, 16 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();

			Key secretKey = new SecretKeySpec(hash, ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(map);
			objectStream.close();

			byte[] outputBytes = cipher.doFinal(byteStream.toByteArray());

			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(outputBytes);
			outputStream.close();
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
				BadPaddingException | IllegalBlockSizeException | IOException | InvalidKeySpecException ex) {
			throw new CryptoException("Error encrypting file", ex); 
		}
	}

	public static TreeMap<String, Credential> decrypt(String key, File inputFile) throws CryptoException {
		try {
			//Key derivation function
			PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt, iterations, 16 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			
			Key secretKey = new SecretKeySpec(hash, ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			byte[] inputBytes = cipher.doFinal(Files.readAllBytes(inputFile.toPath()));

			ByteArrayInputStream byteStream = new ByteArrayInputStream(inputBytes);
			ObjectInputStream objectStream = new ObjectInputStream(new BufferedInputStream(byteStream));

			@SuppressWarnings("unchecked")
			TreeMap<String, Credential> mapFromFile = (TreeMap<String, Credential>) objectStream.readObject();

			objectStream.close();
			return mapFromFile;
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
				BadPaddingException | IllegalBlockSizeException | IOException | ClassNotFoundException | InvalidKeySpecException ex) {
			throw new CryptoException("Error decrypting file", ex); 
		}
	}

	public static class CryptoException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CryptoException() {

		}

		public CryptoException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}

}
