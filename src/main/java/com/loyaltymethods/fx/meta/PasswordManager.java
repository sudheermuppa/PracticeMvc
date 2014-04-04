package com.loyaltymethods.fx.meta;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Cipher;

public class PasswordManager {
	Logger log = Logger.getLogger(PasswordManager.class);
	
	private final String salt = "Does China really exist?";
	private final String algo = "DES";
	private final String sKeyAlgo = "DES";
	
	private String siebelPassword;
	private String batchPassword;
	private String srvrmgrPassword;
	private String mailPassword;

	
	// get password by name
	// TODO convert the whole thing to a hash map.
	public String getNamedPassword(String param) {
		if( param.equals("db.siebel.jdbc.password"))
			return getSiebelPassword();
		else if (param.equals("db.batch.jdbc.password"))
			return getBatchPassword();
		else if (param.equals("srvrmgr.param.password"))
			return getSrvrmgrPassword();
		else if (param.equals("mail.auth.password"))
			return getMailPassword();
		else
			throw new RuntimeException("Unknown password name: "+param);
	}
	
	@SuppressWarnings("restriction")
	public String encrypt64(String input) {
		try {
			return (new sun.misc.BASE64Encoder()).encode(input.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("restriction")
	public String decrypt64(String input) {

		log.debug("Decrypting: "+input);
		try {
			return new String((new sun.misc.BASE64Decoder()).decodeBuffer(input));
		} catch (IOException e) {
			log.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	
	
    @SuppressWarnings("restriction")
	public String encrypt(String input)
            throws InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException {
    	
    	DESKeySpec keySpec = new DESKeySpec(salt.getBytes("UTF-8"));
    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(sKeyAlgo);
    	SecretKey key = keyFactory.generateSecret(keySpec);
    	
    	Cipher cipher = Cipher.getInstance(algo);
    	
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] inputBytes = input.getBytes();
        
        return (new sun.misc.BASE64Encoder()).encode(cipher.doFinal(inputBytes));
    }
    
    public String decrypt(String input)
            throws InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

    	DESKeySpec keySpec = new DESKeySpec(salt.getBytes("UTF-8"));
    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(sKeyAlgo);
    	SecretKey key = keyFactory.generateSecret(keySpec);
    	
    	Cipher cipher = Cipher.getInstance(algo);
	
        cipher.init(Cipher.DECRYPT_MODE, key);
        @SuppressWarnings("restriction")
		byte[] recoveredBytes =
                cipher.doFinal( (new sun.misc.BASE64Decoder()).decodeBuffer((input)));
        
        String recovered = new String(recoveredBytes);
        
        return recovered;
    }

	public String getSiebelPassword() {
		return dec(siebelPassword);
	}

	public void setSiebelPassword(String siebelPassword) {
		this.siebelPassword = siebelPassword;
	}

	public String getBatchPassword() {
		return dec(batchPassword);
	}

	public void setBatchPassword(String batchPassword) {
		this.batchPassword = batchPassword;
	}

	public String getSrvrmgrPassword() {
		return dec(srvrmgrPassword);
	}

	public void setSrvrmgrPassword(String srvrmgrPassword) {
		this.srvrmgrPassword = srvrmgrPassword;
	}

	public String getMailPassword() {
		return dec(mailPassword);
	}

	private String dec(String password) {
		log.debug("Original:"+password);
		if( password.startsWith("__LM__ENC__:")) {
			try {
				log.debug("Chopped:"+password.substring(12));
				password = this.decrypt(password.substring(12));
			}catch(Exception e) {
				log.debug(e.toString());
				throw new RuntimeException(e);
			}
		}
		
		//log.debug("Decrypted:"+password);
		return password;
	}
	
	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}
}