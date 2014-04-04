package com.loyaltymethods.fx.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.loyaltymethods.fx.ex.FXException;

/**
 * Downloads/uploads files for a specific integration. Since we can't do this inside each job (because each job is launched with a filename)
 * we will do this as a simple POJO that is called by the Directory Launcher.
 * 
 * We still use a spring context initialization, just not Spring Batch related. 
 * 
 * Note: for the Response part, we wrap a call to this in a Invoker Step Adapter.
 * 
 * @author Emil
 *
 * */
public class SFTPManager {
	Logger log = Logger.getLogger(SFTPManager.class);
	
	private String remotePath;
	private String protocol;
	private String ftpURL;
	private String login;
	private String password;
	private String localSourceDirectory;
	private String localRespDirectory;
	private String remoteRespPath;
	private String keyFile;
	private String authType;
	private String downloadAction; 

	private String renameSuffix;			// rename downloaded files to a specific suffix to mark them downloaded.
	
	// download all the files to the local path that start with intPrefix
	// any successfully downloaded files will be renamed at the remote host to downloaded.*
	
	public void download(String intPrefix) throws Exception {
		log.debug("download called with: " + intPrefix);
		if(!isDownloadConfigured()) {
			log.info("Download was not configured. SFTP will not be attempted.");
			return;
		}
		
		log.debug("About to create JSch()");
		JSch jsch = new JSch();
		log.debug("Created it successfully.");
		
		Properties config = new Properties();

		log.debug("Checking authType which is "+authType);
		
		if( authType.equals("File")) {
			log.debug("SFTP Manager using keyfile='"+keyFile+"'");
			jsch.addIdentity(keyFile);
		}
		else
			log.debug("SFTP Manager using password authentication.");
			
		
		config.put("StrictHostKeyChecking", "no");
		config.put("compression.s2c","zlib,none");
		config.put("compression.c2s","zlib,none");
		
		Session session = jsch.getSession(login, ftpURL);
		session.setConfig(config);
		session.setPort(22);
		
		if( authType.equals("Login"))
			session.setPassword(password);
		
		log.debug("Attempting SFTP session connection.");
		session.connect();
		log.debug("Connect SFTP session successful.");
		
		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		
		log.debug("Attempting SFTP channel connection.");
		channel.connect();
		log.debug("SFTP channel connection succesful.");
		
		@SuppressWarnings("unchecked")
		final Vector<LsEntry> files = channel.ls(remotePath + "/*");
		log.debug("Look at remote path: "+remotePath + "/*");
		log.debug("Found "+files.size()+" files.");
		
		for( LsEntry file : files ) {
			if( file.getFilename().matches(intPrefix)) {
				try {
					log.debug("SFTP: downloading file '"+file.getFilename()+"'.");
					channel.get(remotePath+"/"+file.getFilename(),localSourceDirectory);

					// rename or delete the file if metadata has told us to do that.
					if( downloadAction.equals("Rename") && getRenameSuffix() != null ) {
						log.debug("Downloaded successfully and renaming to '"+remotePath+"/"+file.getFilename()+"."+getRenameSuffix()+"'.");
						channel.rename(remotePath+"/"+file.getFilename(),remotePath+"/"+file.getFilename()+"."+getRenameSuffix());
					}
					else if(downloadAction.equals("Delete")) {
						log.debug("Download action is delete - so deleting the file.");
						channel.rm(remotePath+"/"+file.getFilename());
					}
				}catch(SftpException e) {
					log.error("Failed to download file '"+file.getFilename()+"'. SFTP said: "+e.getMessage());
					throw e;
				}
			}
			else
				log.debug("SFTP: skipping file because it didn not match '"+intPrefix+"' expression: '"+file.getFilename()+"'.");
			
			
		}
		log.debug("Disconnecting from SFTP channel and session.");
		channel.disconnect();
		session.disconnect();
	}
	
	// upload response files from the response directory

	public void upload(String filePath) throws Exception {
		if(!isUploadConfigured()) {
			log.info("Upload is not configured. SFTP will not be attempted.");
			return;
		}
		
		log.debug("Proceeding with upload of file '"+filePath+"'.");
		
		JSch jsch = new JSch();
		
		Properties config = new Properties();
		
		config.put("StrictHostKeyChecking", "no");
		config.put("compression.s2c","zlib,none");
		config.put("compression.c2s","zlib,none");
		
		log.debug("Configuring session "+login+"@"+ftpURL);
		Session session = jsch.getSession(login, ftpURL);
		
		session.setConfig(config);
		session.setPort(22);
		session.setPassword(password);
		log.debug("Connecting to session.");
		session.connect();
		
		log.debug("Opening channel.");
		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		channel.connect();
		
		log.debug("Putting '"+filePath+"' onto remote path '"+remoteRespPath+"'.");
		channel.put(filePath, remoteRespPath);
		
		// rename the local response file we just uploaded - technically not required because the step will have completed and
		// we are not really uploading en masse, so we will not upload the same file once this step gets marked completed.

		//File file = new File(filePath);
		//File fileNew = new File("uploaded."+file.getCanonicalPath());

		/*if( !file.renameTo(fileNew) ) {
			throw new Exception("Unable to rename local response file.");
		}*/
		log.debug("Disconnecting from channel and session.");
		channel.disconnect();
		session.disconnect();
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException, FXException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    
	    if (length > Integer.MAX_VALUE) {
	        throw new FXException("Unable to read key file: "+file.getAbsolutePath());
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}	

	// check if we have all the details specified for download
	public boolean isDownloadConfigured() {
		log.debug("isDownloadConfigured called - ftpURL is "+ftpURL);
		return (ftpURL != null && !ftpURL.equals("${ftpURL}"));
	}
	
	// check if we have all the details specified for upload
	public boolean isUploadConfigured() {
		log.debug("isUploadConfigured called - remoteRespPath is "+remoteRespPath);
		return (remoteRespPath != null && !remoteRespPath.equals("${ftpRemoteRespPath}"));
	}
	
	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getFtpURL() {
		return ftpURL;
	}

	public void setFtpURL(String ftpURL) {
		this.ftpURL = ftpURL;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLocalSourceDirectory() {
		return localSourceDirectory;
	}

	public void setLocalSourceDirectory(String localpath) {
		this.localSourceDirectory = localpath;
	}

	public String getLocalRespDirectory() {
		return localRespDirectory;
	}

	public void setLocalRespDirectory(String localRespDirectory) {
		this.localRespDirectory = localRespDirectory;
	}

	public String getRemoteRespPath() {
		return remoteRespPath;
	}

	public void setRemoteRespPath(String remoteRespPath) {
		this.remoteRespPath = remoteRespPath;
	}

	public String getRenameSuffix() {
		return renameSuffix;
	}

	@Required
	public void setRenameSuffix(String renameSuffix) {
		this.renameSuffix = renameSuffix;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getDownloadAction() {
		return downloadAction;
	}

	public void setDownloadAction(String downloadAction) {
		this.downloadAction = downloadAction;
	}
}
