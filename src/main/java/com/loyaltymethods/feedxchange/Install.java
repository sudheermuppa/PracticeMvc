package com.loyaltymethods.feedxchange;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author Sudheer Muppa 
 * First Extracting jar file to specified location which contains FX and runs db scripts for schema creation etc. 
 * 
 */
public class Install {

	static Map<String, String> params=new HashMap<String, String>();

	
	static Install i=new Install();
	static String osSeparator =null;
	public static void main(String[] args) {
       
	    String curDir=System.getProperty("user.dir");
		String jarPath ="./Install.jar" ;	   
		System.out.println(curDir);
		System.out.println(jarPath);
		Scanner sc=new Scanner(System.in);
		System.out.println("Do you want to create a new directory to install FeedXchange (Y/N)?");
		String dec=sc.nextLine();
		String dir = null;	
		String HOST_NAME=null;
		String ORACLE_SID=null;
		String JAVA_HOME=null;

		if (System.getProperty("os.name").contains("Windows")){
			osSeparator="\\";

		}else{
			osSeparator="/";
		}

		if (dec.equalsIgnoreCase("Y")){

			System.out.println("Enter a directory name please :");
			dir=sc.nextLine();

		}
		String OUTPUT_FOLDER=System.getProperty("user.dir");
		if(dir!=null){
			OUTPUT_FOLDER=curDir+osSeparator+dir;
		}		
		System.out.println(OUTPUT_FOLDER);
		String instDir;
		try {

			//function used to unzip jar file

			unzipJar(OUTPUT_FOLDER, jarPath);

			// prompt for db credentials 
			System.out.println("Please enter Siebel database username :");
			params.put("db.siebel.jdbc.user",sc.nextLine().trim());
			System.out.println("Please enter Siebel database password :");
			params.put("db.siebel.jdbc.password", sc.nextLine().trim());
			System.out.println("Please enter Batch database username :");
			params.put("db.batch.jdbc.user", sc.nextLine().trim());
			System.out.println("Please enter Batch database password :");
			params.put("db.batch.jdbc.password", sc.nextLine().trim());
			System.out.println("Please enter server manager username  (/u option) :");
			params.put("srvrmgr.param.username", sc.nextLine().trim());
			System.out.println("Please enter server manager password  :");
			params.put("srvrmgr.param.password", sc.nextLine().trim());		
			System.out.println("Please enter Siebel server path :");
			params.put("batch.request.serverPath", sc.nextLine().trim());	
			System.out.println("Please enter time zone :");
			params.put("misc.timezone", sc.nextLine().trim());
			System.out.println("Is SMTP authentication enabled(true/false) :");
			params.put("mail.authenticate", sc.nextLine().trim());
			System.out.println("Is SMTP mail TLS enabled(yes/true) :");
			params.put("mail.enableTLS", sc.nextLine().trim());
			if(params.get("mail.authenticate").equalsIgnoreCase("true")){
				System.out.println("Enter SMTP authentication username :");
				params.put("mail.auth.username",sc.nextLine().trim());				
				System.out.println("Enter SMTP authentication password :");
				params.put("mail.auth.password",sc.nextLine().trim());					
			}
			if(params.get("mail.auth.username")==null){
				params.put("mail.auth.username", "Not entered");
			}
			if(params.get("mail.auth.password")==null){
				params.put("mail.auth.password", "Not entered");
			}

			String env=null;

			if(System.getProperty("os.name").contains("Windows")){
				env=i.getCommandOutput("set");
			}
			else{
				env=i.getCommandOutput("env");

			}
			String[] envArray=env.split("\n");

			for (String s: envArray){
				if (s.toUpperCase().contains("HOSTNAME=")){
					int ind=s.indexOf("=");
					HOST_NAME=(s.substring(ind+1,s.length()).trim());
				}
				if (s.toUpperCase().contains("JAVA_HOME=")){
					int ind=s.indexOf("=");		
					JAVA_HOME=s.substring(ind+1,s.length()).trim();
				}
				if (s.toUpperCase().contains("ORACLE_SID=")){
					int ind=s.indexOf("=");		
					ORACLE_SID=s.substring(ind+1,s.length()).trim();
				}

			}
			if (JAVA_HOME==null || JAVA_HOME.length()==0){
				System.out.println("Please enter java home :");
				JAVA_HOME=sc.nextLine();
			}
			if (ORACLE_SID==null || ORACLE_SID.length()==0){
				System.out.println("Please enter tns name :");
				ORACLE_SID=sc.nextLine();
			}
			if (HOST_NAME==null || HOST_NAME.length()==0){
				System.out.println("Please enter host name :");
				HOST_NAME=sc.nextLine();
			}
			params.put("tnsName", ORACLE_SID);

			params.put("hostName", HOST_NAME);

			params.put("jdkHome",JAVA_HOME);		


			String siebelPath=params.get("batch.request.serverPath");
			File file = new File(siebelPath);
			if (!file.isDirectory()){
				throw new Exception("Siebel directory is not available");
			}
			List<String> enterPriseDirs=new ArrayList<String>();
			File enterPrise=new File(siebelPath+osSeparator+"enterprises");

			File[] files=enterPrise.listFiles();
			for (File f: files){
				if (files.length>1){			    	
					if (f.isDirectory()){
						enterPriseDirs.add(f.getName());
					}
				}
				else{
					if(f.isDirectory()){
						enterPriseDirs.add(f.getName());
					}
				}
			}

			if (enterPriseDirs.size()==1){
				params.put("srvrmgr.param.enterprise", enterPriseDirs.get(0).trim());
			}
			else {
				System.out.println("Please choose an enterprise server");
				for (int i=0;i<=enterPriseDirs.size();i++){
					System.out.println(i+":"+enterPriseDirs.get(i));
				}
				String enterPriseChoosen=sc.nextLine();
				params.put("srvrmgr.param.enterprise", enterPriseChoosen.trim());
			}
			List<String> sblSrvrDirs=new ArrayList<String>();
			File sblSrvr=new File(siebelPath+osSeparator+"enterprises"+osSeparator+params.get("srvrmgr.param.enterprise"));
			File[] sblfiles=sblSrvr.listFiles();
			for (File f: sblfiles){
				if (sblfiles.length>1){			    	
					if (f.isDirectory()){
						sblSrvrDirs.add(f.getName());
					}
				}
				else{
					if(f.isDirectory()){
						sblSrvrDirs.add(f.getName());
					}
				}
			}

			if (sblSrvrDirs.size()==1){
				params.put("srvrmgr.param.server", sblSrvrDirs.get(0).trim());
			}
			else {
				System.out.println("Please choose an Siebel server");
				for (int i=0;i<=sblSrvrDirs.size();i++){
					System.out.println(i+":"+sblSrvrDirs.get(i));
				}
				String sblServerChoosen=sc.nextLine();
				params.put("srvrmgr.param.server", sblServerChoosen.trim());
			}

			String gatewayName=null;
			String gteway=i.readDBScripts(siebelPath+osSeparator+"siebenv.sh");
			String[] gateway=gteway.split("\n");
			for (String s: gateway){
				if(s.toLowerCase().contains("siebel_gateway")){
					int ind1=s.indexOf("=");
					int ind2=s.indexOf(";");
					gatewayName=s.substring(ind1+1, ind2);					
				}
			}

			params.put("srvrmgr.param.gateway", gatewayName.trim());

			params.put("mail.host", params.get("hostName"));

			params.put("mail.port", "25");
			params.put("fx_home",OUTPUT_FOLDER+osSeparator+"fx");				
			params.put("oraDriver","oracle.jdbc.driver.OracleDriver");		
			params.put("batch.conf.path",params.get("fx_home")+osSeparator+"conf");
			params.put("batch.meta.path",params.get("fx_home")+osSeparator+"jobs");
			params.put("batch.bin.path",params.get("fx_home")+osSeparator+"bin");
			params.put("batch.log.path",params.get("fx_home")+osSeparator+"logs");
			params.put("batch.incoming.path",params.get("fx_home")+osSeparator+"data"+osSeparator+"incoming");
			params.put("batch.outgoing.path",params.get("fx_home")+osSeparator+"data"+osSeparator+"outgoing");
			params.put("batch.processed.path",params.get("fx_home")+osSeparator+"data"+osSeparator+"processed");
			instDir=OUTPUT_FOLDER+osSeparator+"fx"+osSeparator+"install"+osSeparator+"back_end"+osSeparator;
			params.put("instDir", instDir);
			System.out.println(instDir);
			params.put("url", "jdbc:oracle:thin:@"+params.get("hostName")+":1521:"+params.get("tnsName"));

			System.out.println("============Configuration review=============== ");
			System.out.println();
			System.out.println("Siebel database username is : "+params.get("db.siebel.jdbc.user"));
			System.out.println("Siebel database passWord is : "+params.get("db.siebel.jdbc.password"));
			System.out.println("Batch database username is : "+params.get("db.batch.jdbc.user"));
			System.out.println("Batch database passowrd is : "+params.get("db.batch.jdbc.password"));
			System.out.println("Siebel server manager username is : "+params.get("srvrmgr.param.username"));
			System.out.println("Siebel  server manager password is : "+params.get("srvrmgr.param.password"));
			System.out.println("Siebel server path is : "+params.get("batch.request.serverPath"));
			System.out.println("Siebel enterprise name is : "+params.get("srvrmgr.param.enterprise"));
			System.out.println("Siebel server name  is : "+params.get("srvrmgr.param.server"));
			System.out.println("Siebel gateway name is : "+params.get("srvrmgr.param.gateway"));
			System.out.println("JAVA_HOME is : "+params.get("jdkHome"));
			System.out.println("Tns name is : "+params.get("tnsName"));
			System.out.println("Host name is : "+params.get("hostName"));
			System.out.println("Mail port no is : "+params.get("mail.port"));
			System.out.println("FX_HOME is : "+params.get("fx_home"));
			System.out.println("Time zone is : "+params.get("misc.timezone"));

			System.out.println("=================================================");
			System.out.println("Are these parameters correct? (Y/N)");
			String paramDecision=sc.nextLine();
			if(paramDecision.equalsIgnoreCase("N")){

				System.out.println("Please enter Siebel database username :");
				params.put("db.siebel.jdbc.user",sc.nextLine().trim());
				System.out.println("Please enter Siebel database password :");
				params.put("db.siebel.jdbc.password", sc.nextLine().trim());
				System.out.println("Please enter Batch database username :");
				params.put("db.batch.jdbc.user", sc.nextLine().trim());
				System.out.println("Please enter Batch database password :");
				params.put("db.batch.jdbc.password", sc.nextLine().trim());
				System.out.println("Please enter username for server manager (/u option) :");
				params.put("srvrmgr.param.username", sc.nextLine().trim());
				System.out.println("Please enter server manager password  :");
				params.put("srvrmgr.param.password", sc.nextLine().trim());		
				System.out.println("Please enter Siebel server path :");
				params.put("batch.request.serverPath", sc.nextLine().trim());	
				System.out.println("Please enter Siebel enteprise name :");
				params.put("srvrmgr.param.enterprise", sc.nextLine().trim());					
				System.out.println("Please enter Siebel server name :");
				params.put("srvrmgr.param.server", sc.nextLine().trim());
				System.out.println("Please enter Siebel gateway :");
				params.put("srvrmgr.param.gateway", sc.nextLine().trim());	
				System.out.println("Please enter time zone :");
				params.put("misc.timezone", sc.nextLine().trim());
				System.out.println("Please enter Java home :");
				params.put("jdkHome", sc.nextLine().trim());
				System.out.println("Please enter Tns name entry :");
				params.put("tnsName", sc.nextLine().trim());
				System.out.println("Please enter host name");
				params.put("hostName", sc.nextLine().trim());


			}else if(paramDecision.equalsIgnoreCase("y")){
				System.out.println("=============Confirming parameters ============");
			}
			else{
				throw new Exception("You should enter either Y or N");
			}



			//these all are dbinst scritps whic are getting sql's executed 

			// creates batch schema and privileges 			
			batchSchemaCreate();

			// grant permissions on batch to siebel 
			grantSiebelToBatch();	

			to62BaseSql();
			lmRowId();
			lmEIMSeq();
			lmGenUId();
			lmError();
			springBatch();
			createSynonym();
			grantBatchToSiebel();
			alterEimtables();			
			//Giving 755 permissions to fx_home folder

			if(!System.getProperty("os.name").contains("Windows")){				
				String s=i.getCommandOutput("chmod 755 -R "+params.get("fx_home"));
				System.out.println(s);
			}
			i.genCXFintSetUp();
			i.genCXFintError();
			i.writeToEnvFile();
			String cmd="sh "+params.get("fx_home")+osSeparator+"bin"+osSeparator+"callparam"+" "+params.get("fx_home");
			String paramgenout=i.getCommandOutput(cmd);//  older jdk5 not identifying paramgen class becuase it is compiled with higher version ,So jdk should be 6 or 7
			System.out.println(paramgenout);
			i.modifySrvrPath();
			String sourceFile1=params.get("fx_home")+osSeparator+"ifb"+osSeparator+"EIM_LOY_MEMBER.ifb";
			String sourceFile2=params.get("fx_home")+osSeparator+"ifb"+osSeparator+"EIM_LOY_TXN.ifb";
			String destFile1=params.get("batch.request.serverPath")+osSeparator+"admin"+osSeparator+"EIM_LOY_MEMBER.ifb";
			String destFile2=params.get("batch.request.serverPath")+osSeparator+"admin"+osSeparator+"EIM_LOY_TXN.ifb";
			File sourceMEMBERifb=new File(sourceFile1);
			File sourceTXN=new File(sourceFile2);
			File destMEMBER=new File(destFile1);
			File destTXN=new File(destFile2);
			i.copyIFBFilesToAdminFolder(sourceMEMBERifb, destMEMBER);
			i.copyIFBFilesToAdminFolder(sourceTXN, destTXN);

		}

		catch(Exception e){
			e.printStackTrace();
		}
	}





	private void writeToEnvFile() {
		// TODO Auto-generated method stub
		File file=new File(params.get("fx_home")+osSeparator+"bin"+osSeparator+"env.sh");
		try {
			FileWriter fw=new FileWriter(file);
			fw.write("export FX_HOME="+params.get("fx_home")+"\n");
			fw.write("export JAVA_HOME="+params.get("jdkHome")+"\n");
			fw.write("export TimeZone="+params.get("misc.timezone"));
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





	private void modifySrvrPath() {
		// TODO Auto-generated method stub
		try {
			String srvrScript=i.readDBScripts(params.get("fx_home")+osSeparator+"bin"+osSeparator+"srvrmgr");
			srvrScript=srvrScript.replaceAll("por", params.get("batch.request.serverPath"));
			try{
				Writer output = new BufferedWriter(new FileWriter(params.get("fx_home")+osSeparator+"bin"+osSeparator+"srvrmgr"));
				output.write(srvrScript);
				output.close();				
			}catch (IOException ex){
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public void copyIFBFilesToAdminFolder(File source, File dest)  throws IOException {

		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally{
			//input.close();
			//output.close();
		}
	}



	/* to unzip the jar and moves the content to destination dir*/
	public static void unzipJar(String destinationDir, String jarPath) throws IOException {
		File file = new File(jarPath);
		JarFile jar = new JarFile(file);
		  System.out.println("Extracting Fx Home folder");
		// fist get all directories,
		// then make those directory on the destination Path
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();
			if (entry.getName().contains("fx")){
				String fileName = destinationDir + File.separator + entry.getName();
				File f = new File(fileName);
              
				if (fileName.endsWith("/")) {
					f.mkdirs();
				}
			}

		}

		//now create all files
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();
			if (entry.getName().contains("fx")){
				String fileName = destinationDir + File.separator + entry.getName();
				File f = new File(fileName);

				if (!fileName.endsWith("/")) {
					InputStream is = jar.getInputStream(entry);
					FileOutputStream fos = new FileOutputStream(f);

					// write contents of 'is' to 'fos'
					while (is.available() > 0) {
						fos.write(is.read());
					}

					fos.close();
					is.close();

				}
			}
		}
	}
	/* getting siebel database connection*/
	public static Connection getSblDbConnection(){
		Connection con=null;			
		if(con==null){
			try {
				Class.forName(params.get("oraDriver"));
				con= DriverManager.getConnection(params.get("url"),params.get("db.siebel.jdbc.user"),params.get("db.siebel.jdbc.password"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return con;		

	}

	/* getting batch database connection*/
	public static  Connection getBatchDbConnection() throws ClassNotFoundException{
		Connection con=null;			
		if(con==null){
			try {
				Class.forName(params.get("oraDriver"));
				con= DriverManager.getConnection(params.get("url"),params.get("db.batch.jdbc.user"),params.get("db.batch.jdbc.password"));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return con;		

	}
	/* used to read the sql scripts given*/
	public String readDBScripts(String aSQLScriptFilePath) throws IOException,SQLException {

		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(aSQLScriptFilePath));
			String str;

			while ((str = in.readLine()) != null) {
				sb.append(str + "\n ");
			}
			in.close();						
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to Execute" + aSQLScriptFilePath +". The error is"+ e.getMessage());
		} 
		return sb.toString();
	}


	/* creates batch schema */
	public static void batchSchemaCreate() throws Exception{

		Connection con=getSblDbConnection();		
		PreparedStatement pst=null;
		try {
			String out=i.readDBScripts(params.get("instDir")+"BATCH_SCHEMA.sql");		
			String sql=out.replaceAll(":batch", params.get("db.batch.jdbc.user")).trim();
			String [] sqlS=sql.split(";");
			for (String s: sqlS){
				try{
					pst=con.prepareStatement(s);
					pst.execute();
				}
				catch(Exception e){
					if(e.toString().contains("ORA-01918")){
						System.out.println(e.toString().contains(e.toString()));
					}
					else{
						System.out.println(e.toString());
						throw new Exception();
					}
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();				
			con.close();

		}

	}
	/* giving permissions to batch on cx_fint tables*/
	private static void grantSiebelToBatch() throws SQLException {
		// TODO Auto-generated method stub

		Connection con=getSblDbConnection();
		PreparedStatement pst=null;
		try {

			String sqlRead=i.readDBScripts(params.get("instDir")+"GRANT_SIEBEL_TO_BATCH.sql");
			String sql=sqlRead.replace(":batch", params.get("db.batch.jdbc.user")).trim();
			pst= con.prepareCall(sql);
			pst.execute();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();				
			con.close();

		}
	}

	/* creates a function used by gen_uid() */

	private static void to62BaseSql() throws SQLException {
		// TODO Auto-generated method stub

		Connection con=null;
		CallableStatement pst=null;
		try {
			con= getBatchDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"TO_62BASE.sql").trim();		
			pst=con.prepareCall(out);
			pst.execute();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();				
			con.close();

		}
	}

	/* creating lm_row_id sequence*/
	private static void lmRowId() throws SQLException {
		// TODO Auto-generated method stub
		Connection con=null;
		PreparedStatement pst=null;
		try {
			con= getBatchDbConnection();
			String sql="CREATE SEQUENCE LM_ROWID_SEQ  MINVALUE 1 MAXVALUE 99999999999999999999999 INCREMENT BY 1 START WITH 26746 CACHE 5000 NOORDER  NOCYCLE";
			pst=con.prepareStatement("DROP SEQUENCE LM_ROWID_SEQ");
			try{
				pst.execute();
			}catch(Exception e){
				//suppressing sequence does not exist for first time
				if(e.toString().contains("ORA-02289")){
					System.out.println(e.toString());
				}
				else {
					e.printStackTrace();
					throw new Exception("Sequence is not created");
				}
			}
			pst=con.prepareStatement(sql);
			pst.execute();


		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}

	}
	/*creates sequence LM_EIM_SEQ*/
	
	private static void lmEIMSeq() throws SQLException {
		// TODO Auto-generated method stub
		Connection con=null;
		PreparedStatement pst=null;	
		try {
			con= getBatchDbConnection();
			String sql="CREATE SEQUENCE  LM_EIM_SEQ  MINVALUE 101 MAXVALUE 9999999999999999999999999999 INCREMENT BY 100 START WITH 101 CACHE 20 NOORDER  NOCYCLE";
			pst=con.prepareStatement("DROP SEQUENCE LM_EIM_SEQ");
			try{
				pst.execute();
			}catch(Exception e){
				//suppressing sequence does not exist for first time
				if(e.toString().contains("ORA-02289")){
					System.out.println(e.toString());
				}
				else {
					e.printStackTrace();
					throw new Exception("Sequence is not created");
					
				}
			}
			pst=con.prepareStatement(sql);
			pst.execute();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}
	}

	/*generate function UId*/

	private static void lmGenUId() throws SQLException {
		// TODO Auto-generated method stub
		Connection con=null;
		PreparedStatement pst=null;
		try {
			con= getBatchDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"LM_GEN_UID.sql").trim();
			pst=con.prepareStatement(out);
			pst.execute();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}
	}

	/*lm error function used to retrieve error name */
	private static void lmError() throws SQLException {
		// TODO Auto-generated method stub
		Connection con=null;
		PreparedStatement pst=null;
		try {
			con= getBatchDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"LM_ERROR.sql").trim();
			pst=con.prepareStatement(out);
			pst.execute();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}

	}
	/* batch tables creation*/
	private static void springBatch() throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement pst=null;
		Connection con=null;
		try {
			con= getBatchDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"spring_batch.sql").trim();
			String [] sqlS=out.split(";");
			for (String s: sqlS){
				try{
					pst=con.prepareStatement(s);
					pst.execute();
				}
				catch(Exception e){
					//suppressing  table drop,sequence drop for first time 
					if(e.toString().contains("ORA-00942")||e.toString().contains("ORA-02289")){
						System.out.println(e.toString());
					}
					else {
						System.out.println(e.toString());
						throw new Exception("Batch tables are not created");

					}
				}

			}


		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}
	}

	private static void createSynonym() throws SQLException {
		// TODO Auto-generated method stub

		Connection con=null;
		PreparedStatement pst=null;
		try {
			con=getSblDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"CREATE_SYNONYMS.sql");
			String sql=out.replaceAll(":batch", params.get("db.batch.jdbc.user")).trim();
			String [] sqlS=sql.split(";");			
			for (String s: sqlS){
				try{

					pst=con.prepareStatement(s);
					pst.execute();
				}
				catch(Exception e){
					//suppressing synonym drop for first time
					if(e.toString().contains("ORA-01434")){
						System.out.println(e.toString());
					}
					else{
						e.printStackTrace();
						throw new Exception("Cant create synonyms");
					}
				}

			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}
	}


	private static void grantBatchToSiebel() {
		// TODO Auto-generated method stub


		try {
			Connection con= getBatchDbConnection();
			String out=i.readDBScripts(params.get("instDir")+"GRANT_BATCH_TO_SIEBEL.sql");			
			String sql=out.replaceAll(":batch", params.get("db.siebel.jdbc.user"));
			PreparedStatement pst=con.prepareStatement(sql);

			pst.execute();

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	private static void alterEimtables() throws SQLException {
		// TODO Auto-generated method stub
		Connection con=null;
		PreparedStatement pst=null;
		try {
			con=getSblDbConnection();		
			String out=i.readDBScripts(params.get("instDir")+"ALTER_EIM_TABLES.sql");
			String sql=out.replaceAll(":sbluser", params.get("db.siebel.jdbc.user")).trim();
			String [] sqlS=sql.split(";");

			for (String s: sqlS){
				try{
					pst=con.prepareStatement(s);
					pst.execute();
				}
				catch(Exception e){
					//suppressing exception if columns are added
					if(e.toString().contains("already used by an existing object")||e.toString().contains("column being added already")){
						System.out.println(e.toString());
					}
					else {
						e.printStackTrace();
						throw new Exception(" Cannot alter EIM tables");
					}
				}

			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			pst.close();
			con.commit();
			con.close();

		}
	}
	private void genCXFintSetUp() throws IOException, SQLException{


		List<List> al=new ArrayList<List>();

		List<String> s1=new ArrayList<String>();    s1.add("dll.purge.name") ;s1.add("purgefile");s1.add("Executable to run when purging a file.");    al.add(s1);
		List<String> s2=new ArrayList<String>();    s2.add("job.fileget.retry.sleep");s2.add("300000");  s2.add("How long (ms) to sleep between retry of incoming file retrieval.");  al.add(s2);
		List<String> s3=new ArrayList<String>();    s3.add("job.fileget.retry.max" );s3.add("3");s3.add("How many times to try to retrieve incoming files before giving up."); al.add(s3);
		List<String> s4=new ArrayList<String>();    s4.add("job.error.threshold" ); s4.add("0");s4.add("If Error records in a file exceed this threshold we stop the job, otherwise mark Unfixable and respond."); al.add(s4);
		List<String> s5=new ArrayList<String>();    s5.add("batch.request.server");s5.add(params.get("srvrmgr.param.server"));s5.add("Server name where requests for FX will go by default."); al.add(s5);
		List<String> s6=new ArrayList<String>();    s6.add("status.record.processed"); s6.add( "processed"); s6.add( "What text to place in REC_STATUS when the record is successfully processed."); al.add(s6);
		List<String> s7=new ArrayList<String>();    s7.add("status.record.error"); s7.add( "ERROR"); s7.add("What text to place in REC_STATUS when the record is in error." ); al.add(s7);
		List<String> s8=new ArrayList<String>();    s8.add("batch.meta.path"); s8.add( params.get("batch.meta.path")); s8.add(  "Backend system where all job definitions are stored." ); al.add(s8);
		List<String> s9=new ArrayList<String>();    s9.add("batch.bin.path" ); s9.add( params.get("batch.bin.path")); s9.add(  "Backend system binary path where all DLLs will be located" ); al.add(s9);
		List<String> s10=new ArrayList<String>();	s10.add("response.suffix" ); s10.add( "ack"); s10.add( "Default suffix to append to files for response" ); al.add(s10);
		List<String> s11=new ArrayList<String>();   s11.add("loyengine.inactivity.timeout"); s11.add( "30000"); s11.add( "How long (milliseconds) to wait before assuming that the Loyalty Engine is down. (i.e. # Queued hasn''t changed for the duration of this time)"); al.add(s11);
		List<String> s12=new ArrayList<String>();   s12.add("loyengine.inactivity.interval"); s12.add( "5000"); s12.add( "How often (milliseconds) to check the change in # of Queued records for an integration." ); al.add(s12);
		List<String> s13=new ArrayList<String>();   s13.add("batch.log.path"); s13.add( params.get("batch.log.path")); s13.add( "Backend system log path whwere all log files will go by default." ); al.add(s13);
		List<String> s14=new ArrayList<String>();	s14.add("revalidate.commit.interval"); s14.add("1"); s14.add(  "Commit interval when reading and writing records from the DB during a revalidation." ); al.add(s14);
		List<String> s15=new ArrayList<String>();	s15.add("validate.commit.interval"); s15.add("1"); s15.add( "Commit interval when reading from a file and writing to the DB during intial validation." ); al.add(s15);
		List<String> s16=new ArrayList<String>();	s16.add("db.jdbc.driver"); s16.add(params.get("oraDriver")); s16.add( "BATCH data source JDBC driver" ); al.add(s16);
		List<String> s17=new ArrayList<String>();	s17.add("db.jdbc.url"); s17.add( params.get("url")); s17.add(  "BATCH data source URL"); al.add(s17);
		List<String> s18=new ArrayList<String>();	s18.add("db.batch.jdbc.user" ); s18.add( params.get("db.batch.jdbc.user")); s18.add( "BATCH data source user name" ); al.add(s18);
		List<String> s19=new ArrayList<String>();   s19.add("db.batch.jdbc.password"); s19.add( params.get("db.batch.jdbc.password")); s19.add( "BATCH data source password" ); al.add(s19);
		List<String> s20=new ArrayList<String>();	s20.add( "db.siebel.jdbc.driver"); s20.add( params.get("oraDriver")); s20.add( "SIEBEL data source JDBC driver" ); al.add(s20);
		List<String> s21=new ArrayList<String>();	s21.add( "db.siebel.jdbc.url" ); s21.add( params.get("url")); s21.add( "SIEBEL data source URL" ); al.add(s21);
		List<String> s22=new ArrayList<String>();	s22.add( "db.siebel.jdbc.user"); s22.add( params.get("db.siebel.jdbc.user")); s22.add( "SIEBEL data source username" ); al.add(s22);
		List<String> s23=new ArrayList<String>();	s23.add( "db.siebel.jdbc.password"); s23.add( params.get("db.siebel.jdbc.password")); s23.add( "SIEBEL data source password" ); al.add(s23);
		List<String> s24=new ArrayList<String>();	s24.add( "srvrmgr.executable.path"); s24.add( params.get("fx_home")+osSeparator+"bin"+osSeparator+"srvrmgr"); s24.add( "Path to wrapper script that runs srvrmgr for us." ); al.add(s24);
		List<String> s25=new ArrayList<String>();	s25.add( "srvrmgr.param.username"); s25.add( params.get("srvrmgr.param.username")); s25.add( "What to pass under the /u switch for srvrmgr" ); al.add(s25);
		List<String> s26=new ArrayList<String>();	s26.add( "srvrmgr.param.password"); s26.add( params.get("srvrmgr.param.password")); s26.add( "What to pass under the /p switch for srvrmgr" ); al.add(s26);
		List<String> s27=new ArrayList<String>();	s27.add( "srvrmgr.param.gateway" ); s27.add( params.get("srvrmgr.param.gateway")); s27.add( "What to pass under the /g switch for srvrmgr" ); al.add(s27);
		List<String> s28=new ArrayList<String>();	s28.add( "srvrmgr.param.server" ); s28.add( params.get("srvrmgr.param.server")); s28.add( "What to pass under the /e switch for srvrmgr" ); al.add(s28);
		List<String> s29=new ArrayList<String>();	s29.add( "srvrmgr.param.enterprise"); s29.add( params.get("srvrmgr.param.enterprise")); s29.add( "What to pass under the /e switch for srvrmgr" ); al.add(s29);
		List<String> s30=new ArrayList<String>();	s30.add( "eim.status.interval"); s30.add( "1000" ); s30.add( "How often (milliseconds) to check on EIM task status" ); al.add(s30);
		List<String> s31=new ArrayList<String>();   s31.add( "eim.status.timeout"); s31.add( "18000"); s31.add("How long (milliseconds) to wait before giving up on EIM execution"); al.add(s31);
		List<String> s32=new ArrayList<String>();	s32.add( "demo.sleep"); s32.add( "1"); s32.add( "How long (milliseconds) to sleep between steps - useful if we are trying to slow down the process for a demo" ); al.add(s32);
		List<String> s33=new ArrayList<String>();	s33.add( "mail.host"); s33.add( params.get("mail.host")); s33.add( "Mail gateway host address" ); al.add(s33);
		List<String> s34=new ArrayList<String>();	s34.add( "mail.port"); s34.add( params.get("mail.port")); s34.add( "Mail gateway TCP/IP port number" ); al.add(s34);
		List<String> s35=new ArrayList<String>();	s35.add( "mail.auth.username"); s35.add( params.get("mail.auth.username")); s35.add( "Mail gateway authentication username" ); al.add(s35);
		List<String> s36=new ArrayList<String>();	s36.add( "mail.auth.password"); s36.add( params.get("mail.auth.password")); s36.add( "Mail gateway authentication password" ); al.add(s36);
		List<String> s37=new ArrayList<String>();	s37.add( "mail.authenticate"); s37.add( params.get("mail.authenticate")); s37.add( "Mail gateway authentication required - true/false" ); al.add(s37);
		List<String> s38=new ArrayList<String>();	s38.add( "mail.enableTLS"); s38.add( params.get("mail.enableTLS")); s38.add( "Mail gateway usees TLS - true/false" ); al.add(s38);
		List<String> s39=new ArrayList<String>();	s39.add( "mail.message.from"); s39.add(  "FeedXChange"); s39.add( "What address to use in the FROM: field of any alerts FeedXChange sends" ); al.add(s39);
		List<String> s40=new ArrayList<String>();	s40.add( "batch.incoming.path"); s40.add( params.get("batch.incoming.path")); s40.add( "Where FeedXChange looks for new files to be processed" ); al.add(s40);
		List<String> s41=new ArrayList<String>();	s41.add( "batch.processed.path"); s41.add( params.get("batch.processed.path")); s41.add( "Path where we move files which are processed. We move them from the batch.incoming.path." ); al.add(s41);
		List<String> s42=new ArrayList<String>();	s42.add( "batch.outgoing.path"); s42.add( params.get("batch.outgoing.path")); s42.add( "Path to store response files that are outgoing to partners" ); al.add(s42);
		List<String> s43=new ArrayList<String>();	s43.add( "batch.conf.path"); s43.add( params.get("batch.conf.path")); s43.add( "Backend system configuration path for storing log4j.properties and batch.properties" ); al.add(s43);
		List<String> s44=new ArrayList<String>();   s44.add( "misc.timezone"); s44.add( params.get("misc.timezone")); s44.add( "Default timezone for dates - should match whatever the setting is in Siebel otherwise times in the UI will be incosistent." ); al.add(s44);
		List<String> s45=new ArrayList<String>();	s45.add( "dll.meta.name"); s45.add( "metagen"); s45.add( "Generates metadata for a given integration." ); al.add(s45);
		List<String> s46=new ArrayList<String>();	s46.add( "dll.run.name"); s46.add( "runbatch"); s46.add( "Used to run an integration." ); al.add(s46);
		List<String> s47=new ArrayList<String>();	s47.add( "dll.rerun.name"); s47.add( "rerunbatch"); s47.add( "Used to re-run a file which failed during an integration run." ); al.add(s47);
		List<String> s48=new ArrayList<String>();	s48.add( "dll.param.name"); s48.add( "paramgen"); s48.add( "Generates batch.properties file from these paramters." ); al.add(s48);
		List<String> s49=new ArrayList<String>();	s49.add( "file.page.size"); s49.add( "40"); s49.add( "How many lines to display at a time when browsing a log or response file." );	 al.add(s49);	

		Connection con=getSblDbConnection();
		PreparedStatement pst=null;
		Properties ps=new Properties();
		for(List<String> inList : al) {
			int i=0;
			StringBuilder sb=new StringBuilder();
			sb.append("INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) ");
			sb.append("VALUES  ('1-' || LM_GEN_UID(),	CURRENT_DATE,'0-1',CURRENT_DATE,'0-1','@name','@value','@comment')");
			String sql=sb.toString().replace("SIEBEL",params.get("db.siebel.jdbc.user")).trim();	
			String name=null;
			String value=null;
			for(String s : inList) {

				if (i==0){					
					sql=sql.replace("@name",s);
					name=s;					
				}
				else if(i==1){
					if(s!=null){
						sql=sql.replace("@value", s);
						value=s;						
					}
					else{
						sql=sql.replace("@value", "PLEASE ENTER");
						value=s;
					}
				}
				else if(i==2){
					sql=sql.replace("@comment", s);
				}
				i++;
			}		

			//check for name avaialable in db 
			String nameCheck=("select count(*) from SIEBEL.cx_fint_setup where name='"+name+"'").replace("SIEBEL", params.get("db.siebel.jdbc.user"));
			int count=0; 
			try
			{
				ResultSet rs=con.prepareCall(nameCheck).executeQuery();


				while (rs.next()){
					count=rs.getInt(1);
				}
			}catch (Exception e) {
				// TODO: handle exception
				//System.out.println(name+" not found in table CX_FINT_SETUP");
			}


			pst=con.prepareCall(sql);
			if (count==0){
				if(value!="Not entered"){
					pst.execute();
				}
			}
			ps.setProperty(name, value);
			sb.setLength(0);
		}
		OutputStream out=new FileOutputStream(new File(params.get("fx_home")+osSeparator+"conf"+osSeparator+"batch.properties"));
		ps.store(out, "Properties given by installation");

	}
	private  void genCXFintError() {
		// TODO Auto-generated method stub
		Map<String , String> errDes=new HashMap<String,String>();

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(params.get("instDir")+"errorDesc.properties"));
			Enumeration enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				errDes.put(key, value);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Connection con=getSblDbConnection();
		PreparedStatement pst=null;

		for (Map.Entry<String,String> err: errDes.entrySet()){
			StringBuilder sb=new StringBuilder();
			sb.append("INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)");
			sb.append ("VALUES ('1-' || LM_GEN_UID(),CURRENT_DATE,'0-1',CURRENT_DATE,'0-1',");
			sb.append("'"+err.getKey()+"','"+err.getValue()+"'"+")");

			try {
				pst=con.prepareCall(sb.toString().replace("SIEBEL", params.get("db.siebel.jdbc.user")).trim());
				pst.execute();				
				sb.setLength(0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public String getCommandOutput(String command)  {
		String output = null;       //the string to return

		Process process = null;
		BufferedReader reader = null;
		InputStreamReader streamReader = null;
		InputStream stream = null;

		try {
			process = Runtime.getRuntime().exec(command);

			//Get stream of the console running the command
			stream = process.getInputStream();
			streamReader = new InputStreamReader(stream);
			reader = new BufferedReader(streamReader);

			String currentLine = null;  //store current line of output from the cmd
			String commandOutput = new String();  //build up the output from cmd
			while ((currentLine = reader.readLine()) != null) {
				commandOutput+=currentLine+"\n";
			}

			int returnCode = process.waitFor();
			if (returnCode == 0) {
				output = commandOutput.toString();
			}

		} catch (IOException e) {
			System.err.println("Cannot retrieve output of command");
			System.err.println(e);
			output = null;
		} catch (InterruptedException e) {
			System.err.println("Cannot retrieve output of command");
			System.err.println(e);
		} finally {
			//Close all inputs / readers

			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					System.err.println("Cannot close stream input! " + e);
				}
			} 
			if (streamReader != null) {
				try {
					streamReader.close();
				} catch (IOException e) {
					System.err.println("Cannot close stream input reader! " + e);
				}
			}
			if (reader != null) {
				try {
					streamReader.close();
				} catch (IOException e) {
					System.err.println("Cannot close stream input reader! " + e);
				}
			}
		}
		//Return the output from the command - may be null if an error occured
		return output;
	}
}








