package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.Properties;
import java.util.Vector;
 
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPTest {

	@Test
	public void test() throws JSchException, SftpException {
		JSch jsch = new JSch();
		
		Properties config = new Properties();
		
		config.put("StrictHostKeyChecking", "no");
		config.put("compression.s2c","zlib,none");
		config.put("compression.c2s","zlib,none");
		
		//jsch.addIdentity("C:\\Users\\Emil\\Documents\\Dropbox\\EC2\\sbux.ppk");
		jsch.addIdentity("C:\\temp\\test.key");
		
		Session session = jsch.getSession("sadmin", "ec2-50-16-112-79.compute-1.amazonaws.com");
		
		session.setConfig(config);
		session.setPort(22);
		//session.setPassword("Password1");
		session.connect();
		
		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		channel.connect();
		
		final Vector<LsEntry> files = channel.ls(".");
		
		for( LsEntry file: files) {
			//System.out.println(file.toString());
			System.out.println(file.getFilename());
		}
		
		// try to download a file
		
		//channel.get("/Test/prostak.txt", "c:/temp");
		//channel.rm("/Test/prostak.txt");
		//channel.put("c:/temp/prostak.txt","/Test");
		
		// channel
		
		//channel.rename("/Test/prostak.txt","/Test/prostak.txt");
		
		channel.disconnect();
		session.disconnect();
	}
}
