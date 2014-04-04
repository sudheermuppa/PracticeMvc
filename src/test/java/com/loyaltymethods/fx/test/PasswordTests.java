package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.loyaltymethods.fx.meta.PasswordManager;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;


public class PasswordTests {
	Logger log = Logger.getLogger(PasswordTests.class);
	
	@Test
	public void testPWDManager() throws Exception {
		PasswordManager mgr = new PasswordManager();
		
		log.debug(mgr.decrypt(mgr.encrypt("batch")));
		log.debug(mgr.decrypt("dDRibGVvd24zcg=="));
	}
	
	@SuppressWarnings("restriction")
	@Test
	public void testEncoding() throws Exception {
		log.debug(new sun.misc.BASE64Encoder().encode("Emil".getBytes("UTF-8")));
		log.debug(new String(new sun.misc.BASE64Decoder().decodeBuffer("dDRibGVvd24zcg==")));
	}
	
	
//	@SuppressWarnings("restriction")
//	@Test
//	public void test() throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
//		BASE64Encoder enc = new BASE64Encoder();
//		BASE64Decoder dec = new BASE64Decoder();
//		
//		DESKeySpec keySpec = new DESKeySpec("This is some crazy stuff.".getBytes("UTF-8"));
//		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
//		SecretKey key = keyFactory.generateSecret(keySpec);
//		
//		byte [] clearText = "Emil Sarkissian".getBytes("UTF-8");
//		
//		Cipher cipher = Cipher.getInstance("DES");
//		cipher.init(Cipher.ENCRYPT_MODE,key);
//		
//		String encryptedPassword = enc.encode(cipher.doFinal(clearText));
//		log.debug(encryptedPassword);
//		key = keyFactory.generateSecret(keySpec);
//		
//		cipher = Cipher.getInstance("DES");
//		cipher.init(Cipher.DECRYPT_MODE, key);
//		byte [] plainTextBytes = (cipher.doFinal(dec.decodeBuffer(encryptedPassword)));
//		log.debug(new String(plainTextBytes,"UTF-8"));
//		
//	}
}
