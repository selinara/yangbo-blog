package com.chl.gbo.util.token;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RSA token加密
 */
public class RSAPrivateKeyEncrypter {
	private static final Log logger = LogFactory.getLog(RSAPrivateKeyEncrypter.class);

	Cipher ecipher;
	Cipher dcipher;

	public RSAPrivateKeyEncrypter() {
		try {
			String publicKeyFile = "publicKey";
			String privateKeyFile = "privateKey";
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream inStream = cl.getResourceAsStream(publicKeyFile);
			ObjectInputStream publicInput = new ObjectInputStream(inStream);
			PublicKey publicKey = (PublicKey) publicInput.readObject();
			inStream = cl.getResourceAsStream(privateKeyFile);
			ObjectInputStream privateInput = new ObjectInputStream(inStream);
			PrivateKey privateKey = (PrivateKey) privateInput.readObject();
			ecipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			dcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			ecipher.init(Cipher.ENCRYPT_MODE, privateKey);
			dcipher.init(Cipher.DECRYPT_MODE, publicKey);
		} catch (IOException e) {
			logger.error("", e);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		} catch (NoSuchPaddingException e) {
			logger.error("", e);
		} catch (InvalidKeyException e) {
			logger.error("", e);
		}
	}

	public String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");
			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);
			// Encode bytes to base64 to get a string
            // return new sun.misc.BASE64Encoder().encode(enc);
			return new String(new org.apache.commons.codec.binary.Base64(true).encode(enc));
		} catch (Exception e) {
			logger.error("encrypt error----->", e);
		}
		
		return null;
	}

	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
            // byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
			byte[] dec = new org.apache.commons.codec.binary.Base64(true).decode(str);
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			logger.error("decrypt error----->", e);
		}
		
		return null;
	}

	public static void main(String[] args) {
		String aa = "yang8363411&1566890726575&14431244";
		RSAPrivateKeyEncrypter r = new RSAPrivateKeyEncrypter();
		String result = r.encrypt(aa);
		System.out.println(result);
		System.out.println(result.toLowerCase());

		System.out.println("解密后："+r.decrypt(result.toLowerCase()));

	}

}
