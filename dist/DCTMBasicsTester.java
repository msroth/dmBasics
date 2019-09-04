package com.dm_misc.dctm;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;

public class DCTMBasicsTester {

	private final String DOCBASE_KEY = "-docbase";
	private final String USERNAME_KEY = "-username";
	private final String PASSWORD_KEY = "-password";
	private IDfSession session = null;
	private Properties p = null;

	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SS");
		Date now;
		Long startTime = 0L;
		Long endTime = 0L;

		try {

			// get current time and print harness header
			now = new Date();
			System.out.println("===== Start " + sdf.format(now) + " =====");
			System.out.println();
			startTime = System.currentTimeMillis();

			// =================
			// instantiate class to test here
			DCTMBasicsTester a = new DCTMBasicsTester();
			a.run(args);
			// =================

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}  finally {
			// get current time and print harness footer
			System.out.println();
			now = new Date();
			endTime = System.currentTimeMillis();
			System.out.println("Total Run Time: " + (endTime - startTime) + " msec");
			System.out.println("===== End " + sdf.format(now) + " =====");
		}


	}

	// constructor
	public DCTMBasicsTester() {
	}

	public void run(String[] args) throws Exception {

		// test command line args
		if (args.length != 6)
			throw new Exception ("Incorrect number of command line arguments.\n Must include:\n-docbase <docbase name> -username <user name> -password <password>");
		
		System.out.println("Test Harness for DCTMBasics, version " + DCTMBasics.version());
		
		// get args
		p = DCTMBasics.getArgs(args);
		
		// run logon tests and get session
		runLogonTests();

		if (session != null) {

			// run tests
			runDFCTests();
			runQueryTests();



			System.out.println("Logging off...");
			DCTMBasics.logoff(session);
		} else {
			System.out.println("Failed.");
		}

	}


	private void runDFCTests() {

		System.out.println("\n<-- DFC tests -->\n");

		try {
			// Check DFC version
			System.out.println("DFC = " + DCTMBasics.getDFCversion());
			System.out.println("DFC is greater than 6.5? " + Boolean.toString(DCTMBasics.checkDFCversion(6.5)));
	
			// Test Encryption
			String clearPassword = "test";
			String cipherPassword = "";
			System.out.println("Password = " + clearPassword);
			cipherPassword = DCTMBasics.encryptPassword(clearPassword);
			System.out.println("Encrypted password = " + cipherPassword);
			clearPassword = DCTMBasics.decryptPassword(cipherPassword);
			System.out.println("Decrypted password = " + clearPassword);
	
			// create folder
			System.out.println("creating Docbase folder /Temp/DCTMBasics/test");
			IDfFolder folder = DCTMBasics.createDocbasePath(session, "/Temp/DCTMBasics/test");
			System.out.println("folder ID = " + folder.getObjectId().toString());
		
			// check object type
			System.out.println("is " + folder.getObjectId().toString() + " a SysObject?: " + Boolean.toString(DCTMBasics.isSysObject(folder)) );
			System.out.println("is " + folder.getObjectId().toString() + " a Folder?: " + Boolean.toString(DCTMBasics.isFolder(folder)) );
			System.out.println("is " + folder.getObjectId().toString() + " a Cabinet?: " + Boolean.toString(DCTMBasics.isCabinet(folder)) );
			System.out.println("is " + folder.getObjectId().toString() + " a Document?: " + Boolean.toString(DCTMBasics.isDocument(folder)) );
			
			// create log file
			String logfile = System.getProperty("java.io.tmpdir") + "DCTMBasics.log";
			System.out.println("creating log file " + logfile + " (unique)");
			PrintWriter log = DCTMBasics.createLogFile(logfile, true);
			log.println("This is a test");
			log.close();
			
		} catch (DfException dfe) {
			dfe.printStackTrace();
		}
	}

	private void runLogonTests() {

		System.out.println("\n<-- logon tests -->\n");

		String docbase = p.getProperty(DOCBASE_KEY); 
		String username = p.getProperty(USERNAME_KEY); 
		String password = p.getProperty(PASSWORD_KEY);

		try {
			// blank docbase
			System.out.println("testing missing docbase name");
			session = DCTMBasics.logon("",username,password);
			System.out.println("session ID = " + session.getSessionId());
		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}

		try {
			// blank user (space)
			System.out.println("testing missing user name");
			session = DCTMBasics.logon(docbase," ",password);
			System.out.println("session ID = " + session.getSessionId());
		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}

		try {
			// blank password (null)
			System.out.println("testing missing password");
			session = DCTMBasics.logon(docbase,username,null);
			System.out.println("session ID = " + session.getSessionId());
		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}

		try {
			// wrong password
			System.out.println("testing wrong password");
			session = DCTMBasics.logon(docbase,username,docbase);	
			System.out.println("session ID = " + session.getSessionId());
		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}

		try {
			// good login
			System.out.println("testing good login");
			session = DCTMBasics.logon(docbase,username,password);	
			System.out.println("session ID = " + session.getSessionId());
		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}

	}

	private void runQueryTests() {

		System.out.println("\n<-- query tests -->\n");

		try {
			// SELECT       
			System.out.println("\nrunning SELECT query");
			String query = "select r_object_id, object_name from dm_document where folder('/Temp')";
			System.out.println("query: " + query);
			IDfCollection col = DCTMBasics.runSelectQuery(query,session);
			System.out.println(getCollectionAsString(col,10).toString());
			col.close();

			// COUNT - get single value from collection
			System.out.println("\nrunning SELECT query that returns single value");
			query = "select count(*) from dm_document where folder('/Temp',descend)";
			System.out.println("query: " + query);
			System.out.println("count: " + DCTMBasics.runDQLQueryReturnSingleValue(query, session));
			col.close();

			// CACHED
			System.out.println("\nrunning CACHED query");
			query = "select r_object_id, object_name from dm_document where folder('/Temp',descend)";
			System.out.println("query: " + query);
			long start = System.currentTimeMillis();
			col = DCTMBasics.runCachedQuery(query,session);
			long stop = System.currentTimeMillis();
			long dif = stop - start;
			System.out.println(getCollectionAsString(col,10).toString());
			col.close();
			System.out.println("duration=" + dif + "ms");

			System.out.println("\n[rerunning CACHED query]");
			System.out.println("query: " + query);
			start = System.currentTimeMillis();
			col = DCTMBasics.runCachedQuery(query,session);
			stop = System.currentTimeMillis();
			dif = stop - start;
			System.out.println(getCollectionAsString(col,10).toString());
			col.close();
			System.out.println("duration=" + dif + "ms");

			// EXEC
			System.out.println("\nrunning EXEC query");
			query = "execute db_stats";
			System.out.println("query: " + query);
			col = DCTMBasics.runExecQuery(query,session);
			System.out.println(getCollectionAsString(col).toString());
			col.close();

			// OBJ CREATE
			System.out.println("\nrunning OBJ CREATE query");
			query = "create dm_document object set object_name = 'DCTMBasics Test Object' link '/Temp'";
			System.out.println("query: " + query);
			String objId = DCTMBasics.runCreateObjectQuery(query,session);
			System.out.println("created " + objId);

			// OBJ UPDATE
			System.out.println("\nrunning OBJ UPDATE query");
			query = "update dm_document object set object_name = 'DCTMBasics updated object name' where r_object_id = '" + objId + "'";
			System.out.println("query: " + query);
			int cnt = DCTMBasics.runUpdateObjectQuery(query,session);
			System.out.println("updated " + cnt + " objs");

			// OBJ DELETE
			System.out.println("\nrunning OBJ DELETE query");
			query = "delete dm_document object where r_object_id = '" + objId + "'";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runDeleteObjectQuery(query,session);
			System.out.println("deleted " + cnt + " objs");

			// SQL PASS-THROUGH
			System.out.println("\nrunning SQL PASS-THROUGH query");
			query = "execute exec_sql with query = 'create table usstates (state_name varchar(25), state_abbr varchar(5))'";
			System.out.println("query: " + query);
			col = DCTMBasics.runSQLQuery(query,session);
			System.out.println(getCollectionAsString(col).toString());
			col.close();

			// REGISTER NEW TABLE
			System.out.println("registering new table...");
			query = "register table dm_dbo.usstates (state_name string(25), state_abbr string(5))";
			String rv = DCTMBasics.runRegisterTableQuery(query,session);
			System.out.println("New table Id=" + rv);

			query = "update dm_registered object set owner_table_permit=15 where r_object_id ='" + rv +"'";
			cnt = DCTMBasics.runUpdateObjectQuery(query,session);

			// TABLE INSERT
			System.out.println("\nrunning SQL INSERT query");
			query = "insert into dm_dbo.usstates (state_name, state_abbr) values ('Virginia','VA')";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runInsertQuery(query,session);
			System.out.println("inserted " + cnt + " rows");
			query = "insert into dm_dbo.usstates (state_name, state_abbr) values ('West Virginia','WVA')";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runInsertQuery(query,session);
			System.out.println("inserted " + cnt + " rows");
			query = "insert into dm_dbo.usstates (state_name, state_abbr) values ('Maryland','MD')";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runInsertQuery(query,session);
			System.out.println("inserted " + cnt + " rows");

			// TABLE UPDATE
			System.out.println("\nrunning SQL UPDATE query");
			query = "update dm_dbo.usstates set state_abbr = 'WV' where state_abbr = 'WVA'";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runUpdateQuery(query,session);
			System.out.println("updated " + cnt + " rows");

			// TABLE DELETE
			System.out.println("\nrunning SQL DELETE query");
			query = "delete from dm_dbo.usstates where state_abbr = 'MD'";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runDeleteQuery(query,session);
			System.out.println("deleted " + cnt + " rows");

			// DELTE REGISTERED TABLE OBJECT
			System.out.println("\nrunning OBJ DELETE query");
			query = "delete dm_registered object where table_name = 'usstates'";
			System.out.println("query: " + query);
			cnt = DCTMBasics.runDeleteObjectQuery(query,session);
			System.out.println("deleted " + cnt + " objs");

			// DELETE TABLE
			System.out.println("\nrunning SQL PASS-THROUGH query");
			query = "execute exec_sql with query = 'drop table usstates'";
			System.out.println("query: " + query);
			col = DCTMBasics.runSQLQuery(query,session);
			System.out.println(getCollectionAsString(col).toString());
			col.close();

		} catch (DfException dfe) {
			System.out.println(dfe.getMessage());
		}
	}

	private StringBuilder getCollectionAsString(IDfCollection col) {
		return getCollectionAsString(col,-1);
	}

	private StringBuilder getCollectionAsString(IDfCollection col, int rows) {
		StringBuilder sb = new StringBuilder();
		int r = 0;

		try {
			int colNum = col.getAttrCount();
			for (int i=0; i<colNum; i++) {
				sb.append(col.getAttr(i).getName() + "\t");
			}
			sb.append("\n");
			while (col.next()) {                 
				for (int j=0; j<colNum; j++) {                 
					int colType = col.getAttr(j).getDataType();                 
					String colValue = "";                
					if (colType == IDfType.DF_BOOLEAN)                   
						colValue = Boolean.toString(col.getBoolean(col.getAttr(j).getName()));                
					else if (colType == IDfType.DF_DOUBLE)                   
						colValue = Double.toString(col.getDouble(col.getAttr(j).getName()));                
					else if (colType == IDfType.DF_ID)                   
						colValue = col.getId(col.getAttr(j).getName()).toString();                
					else if (colType == IDfType.DF_INTEGER)                   
						colValue = Integer.toString(col.getInt(col.getAttr(j).getName()));                
					else if (colType == IDfType.DF_STRING)                   
						colValue = col.getString(col.getAttr(j).getName());                
					else if (colType == IDfType.DF_TIME)                   
						colValue = col.getTime(col.getAttr(j).getName()).toString();  
					sb.append(colValue + "\t");
				}     
				sb.append("\n");

				if (rows != -1) 
					r++;
				if ((r >= rows) && (rows != -1)) {
					sb.append("truncated at " + rows + " rows...");
					break;
				}
			}        

		} catch (DfException e) {
			e.printStackTrace();
		}

		return sb;
	}

}
