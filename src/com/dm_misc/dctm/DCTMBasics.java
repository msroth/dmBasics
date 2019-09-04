/**
 * (C) 2014-2015 MSRoth http://msroth.wordpress.com
 * 
 * 
 * v2 - 11/10/14
 * 
 * v3 - 7/1/15
 * 		- Added following methods:
 *		- getDFCVersion
 *		- checkDFCVersion
 *		- createDocbasePath
 *		- encrypt password
 *		- decrypt password
 *		- runDQLQueryReturnSingleValue
 *		- isSysObject
 *		- createLogFile
 *		- other misc updates and changes
 *
 * BACKLOG ITEMS
 * =============
 * TODO - launch workflow
 * TODO - apply/remove aspect
 * TODO - attach/detach lifecycle
 * TODO - promote/demote
 * TODO - power promote
 * TODO - operations
 * TODO - getObjectPath
*/

package com.dm_misc.dctm;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.tools.RegistryPasswordUtils;


public class DCTMBasics {

	private static final String CLASSNAME = "DCTMBasics";
	private static final String VERSION = "1.3";


	/*************************************************************************
	 * Establish a Docbase session using credentials supplied
	 * @param docbase name of Docbase to login to
	 * @param username name of user to login with
	 * @param password password to authenticate with
	 * @return IDfSession
	 * @throws DfException
	 *************************************************************************            
	 */
	public static IDfSession logon(String docbase, String username, String password) throws DfException {
		IDfSession session = null;

		// validate arguments
		if ((docbase == null) || (docbase.trim().isEmpty()) )
			throw new DfException ("Docbase name is null or blank.");

		if ((username == null) || (username.trim().isEmpty()) )
			throw new DfException ("Username name is null or blank.");

		if ((password == null) || (password.trim().isEmpty()) )
			throw new DfException ("Password is null or blank.");

		// create login info
		IDfLoginInfo li = new DfLoginInfo();
		li.setUser(username);
		li.setPassword(password);
		li.setDomain("");

		// get session manager
		IDfSessionManager sessionMgr = DfClient.getLocalClient().newSessionManager();

		// login
		if (sessionMgr != null) {
			sessionMgr.setIdentity(docbase, li);
			session = sessionMgr.getSession(docbase);
			DfLogger.info(CLASSNAME + ".logon","Log on successful",null,null);
		} else {
			DfLogger.error(CLASSNAME + ".logon","Could not create Session Manager",null,null);
			throw new DfException("Could not create Session Manager.");
		}

		return session;
	}

	/*************************************************************************
	 * Logoff active session
	 * @param session active session to end
	 *************************************************************************            
	 */	
	public static void logoff(IDfSession session) {
		if (session != null) {
			session.getSessionManager().release(session);
			DfLogger.info(CLASSNAME + ".logoff","Session released",null,null);
		} else {
			DfLogger.debug(CLASSNAME + ".logoff","This session is already null",null,null);
		}
	}

	/*************************************************************************
	 * Parse args into a Properties object and print if necessary
	 * @param args arguments to parse (must be balanced)
	 * @return arguments as Properties object
	 * @throws DfException
	 *************************************************************************            
	 */	
	public static Properties getArgs(String[] args) throws DfException {
		Properties props = new Properties();

		if (args.length % 2 != 0)
			throw new DfException("Unbalanced command line arguments recieved.");

		for (int i=0; i<args.length; i++) {
			String key = args[i];
			String value = args[++i];
			props.setProperty(key, value);
		}

		// print args to log if in debug mode
		Enumeration<Object> keys = props.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String value = props.getProperty(key);
			DfLogger.debug(CLASSNAME + ".getArgs", "key=" + key + "  value=" + value, null, null);
		}

		return props;
	}


	/************************************************************************* 
	 * Register table query
	 * @param query registered table DQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************* 
	 */
	public static String runRegisterTableQuery(String query, IDfSession session) throws DfException {
		//IDfCollection col = null;   
		String rv = null;

		if (! query.toLowerCase().startsWith("register"))
			throw new DfException("Register Table query must start with 'REGISTER'");

		//col = runDQLQuery(query, session, DfQuery.DF_QUERY);
		//		while (col.next()) {
		//			rv = col.getString("new_object_ID");
		//		}
		//rv = getSingleCollectionValueAsString(col);
		rv = runDQLQueryReturnSingleValue(query,session);
		//col.close();
		return rv;
	}

	/************************************************************************* 
	 * Run a DQL SELECT query
	 * @param query DQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************* 
	 */
	public static IDfCollection runSelectQuery(String query, IDfSession session) throws DfException {
		IDfCollection col = null;   

		if (! query.toLowerCase().startsWith("select"))
			throw new DfException("Select query must start with 'SELECT'");

		col = runDQLQuery(query, session, DfQuery.DF_READ_QUERY);
		return col;
	}

//	/************************************************************************* 
//	 * Run a DQL SELECT query are return a dmRecordSet
//	 * @param query DQL query string
//	 * @param session active session object
//	 * @return dmRecordSet
//	 * @throws Exception
//	 ************************************************************************* 
//	 */
//	public static dmRecordSet runSelectQueryAsRecordSet(String query, IDfSession session) throws Exception {
//		IDfCollection col = runSelectQuery(query,session);
//		return new dmRecordSet(col);
//	}

	/*************************************************************************
	 * Run DQL UPDATE...OBJECT query
	 * @param query DQL query string
	 * @param session active session object
	 * @return the number of objects updated
	 * @throws Exception
	 *************************************************************************
	 */
	public static int runUpdateObjectQuery(String query, IDfSession session) throws DfException {
		int objsChanged = 0;

		if (! query.toLowerCase().startsWith("update") && ! query.toLowerCase().contains("object"))
			throw new DfException("Update Object query must start with 'UPDATE' and contain 'OBJECT'");

		objsChanged = runQueryAsInt(query, session, DfQuery.DF_QUERY, "objects_updated");
		return objsChanged;
	}


	/*************************************************************************
	 * Run DQL cached SELECT query
	 * @param query DQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 *************************************************************************
	 */
	public static IDfCollection runCachedQuery(String query, IDfSession session) throws DfException {
		IDfCollection col = null;   

		if (! query.toLowerCase().startsWith("select"))
			throw new DfException("Cached query must start with 'SELECT'");

		col = runDQLQuery(query, session, DfQuery.DF_CACHE_QUERY);
		return col;
	}

	/*************************************************************************
	 * run DQL CREATE...OBJECT query
	 * @param query DQL query string
	 * @param session active session object
	 * @return r_object_id of created object
	 * @throws DfException
	 ************************************************************************
	 */
	public static String runCreateObjectQuery(String query, IDfSession session) throws DfException {
		String objId = null;

		if (! query.toLowerCase().startsWith("create") && ! query.toLowerCase().contains("object"))
			throw new DfException("Create Object query must start with 'CREATE' and contain 'OBJECT'");

		objId = runQueryAsString(query, session, DfQuery.DF_QUERY, "object_created");
		return objId;
	}

	/*************************************************************************
	 * Run DQL DELETE...OBJECT query 
	 * @param query DQL query string
	 * @param session active session object
	 * @return number of objects deleted
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static int runDeleteObjectQuery(String query, IDfSession session) throws DfException {
		int objsDeleted = 0;

		if (! query.toLowerCase().startsWith("delete") && ! query.toLowerCase().contains("object") )
			throw new DfException("Delete Object query must start with 'DELETE' and contain 'OBJECT'");

		objsDeleted = runQueryAsInt(query, session, DfQuery.DF_QUERY, "objects_deleted");
		return objsDeleted;
	}

	/*************************************************************************
	 * Run DQL EXEC query
	 * @param query DQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static IDfCollection runExecQuery(String query, IDfSession session) throws DfException {
		IDfCollection col = null;   

		if (! query.toLowerCase().startsWith("exec"))
			throw new DfException("Exec query must start with 'EXEC'");

		col = runDQLQuery(query, session, DfQuery.DF_EXEC_QUERY);
		return col;
	}

	/*************************************************************************
	 * Run SQL pass-through query
	 * @param query SQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static IDfCollection runSQLQuery(String query, IDfSession session) throws DfException {
		IDfCollection col = null;   

		if (! query.toLowerCase().startsWith("execute exec_sql with query"))
			throw new DfException("SQL pass-through query must start with 'execute exec_sql with query'");

		col = runDQLQuery(query, session, DfQuery.DF_EXEC_QUERY);
		return col;
	}

	/*************************************************************************
	 * Run INSERT query to insert rows into a registered database table
	 * @param query insert query string
	 * @param session active session object
	 * @return number of rows inserted
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static int runInsertQuery(String query, IDfSession session) throws DfException {
		int rowsInserted = 0;

		if (! query.toLowerCase().startsWith("insert"))
			throw new DfException("Insert query must start with 'INSERT'");

		rowsInserted = runQueryAsInt(query, session, DfQuery.DF_QUERY, "rows_inserted");
		return rowsInserted;
	}

	/***********************************************************************
	 * Run DELETE query to delete rows from a registered database table
	 * @param query delete query string
	 * @param session active session object
	 * @return number of rows deleted
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static int runDeleteQuery(String query, IDfSession session) throws DfException {
		int rowsDeleted = 0;

		if (! query.toLowerCase().startsWith("delete") || query.toLowerCase().contains("object"))
			throw new DfException("Delete query must start with 'DELETE' and not contain 'OBJECT'");

		rowsDeleted = runQueryAsInt(query, session, DfQuery.DF_QUERY, "rows_deleted");
		return rowsDeleted;
	}

	/***********************************************************************
	 * Run UPDATE query to update rows in a registered database table
	 * @param query update query string
	 * @param session active session object
	 * @return number of rows updated
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static int runUpdateQuery(String query, IDfSession session) throws DfException {
		int rowsUpdated = 0;

		if (! query.toLowerCase().startsWith("update") || query.toLowerCase().contains("object"))
			throw new DfException("Update query must start with 'DELETE' and not contain 'OBJECT'");

		rowsUpdated = runQueryAsInt(query, session, DfQuery.DF_QUERY, "rows_updated");
		return rowsUpdated;
	}

	/***********************************************************************
	 * Run a DQL query and return a single integer as the result
	 * @param query DQL query string
	 * @param session active session object
	 * @param queryType DfQuery.DF_ query type
	 * @param returnCol name of column containing the integer result to return
	 * @return return single result as int 
	 * @throws DfException
	 ************************************************************************ 
	 */
	private static int runQueryAsInt(String query, IDfSession session, int queryType, String returnCol) throws DfException {
		IDfCollection col = null;
		int rv = 0;

		// run query
		col = runDQLQuery(query, session, queryType);

		// determine results
		while (col.next()) {
			rv = col.getInt(returnCol);
			break;
		}

		// close collection
		if ( (col != null) && (col.getState() != IDfCollection.DF_CLOSED_STATE) ) 
			col.close();

		return rv;
	}

	/************************************************************************
	 * Run a DQL query and return a single String as the result
	 * @param query DQL query string
	 * @param session active session object
	 * @param queryType DfQuery.DF_ query type
	 * @param returnCol name of column containing the String result to return
	 * @return return single result as String
	 * @throws DfException
	 ************************************************************************ 
	 */
	private static String runQueryAsString(String query, IDfSession session, int queryType, String returnCol) throws DfException {
		IDfCollection col = null;
		String rv = "";

		// run query
		col = runDQLQuery(query, session, queryType);

		// determine results
		while (col.next()) {
			rv = col.getString(returnCol);
			break;
		}

		// close collection
		if ( (col != null) && (col.getState() != IDfCollection.DF_CLOSED_STATE) ) 
			col.close();

		return rv;
	}

	/***********************************************************************
	 * Run a generic DQL query.  All query methods call this method.
	 * @param query DQL query string
	 * @param session active session object
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************ 
	 */
	public static IDfCollection runDQLQuery(String query, IDfSession session) throws DfException {  
		return runDQLQuery(query,session,DfQuery.DF_QUERY);
	}


	/***********************************************************************
	 * Run a generic DQL query.  All query methods call this method.
	 * @param query DQL query string
	 * @param session active session object
	 * @param queryType DfClient.DF_ query type
	 * @return IDfCollection of results
	 * @throws DfException
	 ************************************************************************ 
	 */
	private static IDfCollection runDQLQuery(String query, IDfSession session, int queryType) throws DfException {  
		IDfCollection col = null;   

		if( (query == null) || (query.trim().isEmpty()) ) 
			throw new DfException ("Query string is null or blank.");
		if( session == null ) 
			throw new DfException ("Session is null.");

		DfLogger.debug(CLASSNAME + ".runDQLQuery", "query=" + query, null, null);

		IDfQuery q = new DfQuery();
		q.setDQL(query);
		col = q.execute(session, queryType);   

		return col;
	}

	/**********************************************************************
	 * Return the value from a collection that only contains a single value
	 * such as a count query
	 * @param col the collection to represent as a String
	 * @return return the value of a collection containing a single value
	 **********************************************************************
	 */
	private static String getSingleCollectionValueAsString(IDfCollection col) {
		String value = "";

		try {
			while (col.next()) {                 
				value = col.getString(col.getAttr(0).getName());
				break;
			}
		} catch (DfException e) {
			DfLogger.error(CLASSNAME + ".getCollectionAsString",e.getMessage(),null,null);
		}

		return value;
	}


	/**********************************************************************
	 * Return an entire IDfCollection as a String with tabbed columns and
	 * rows.
	 * @param col the collection to represent as a String
	 * @return entire collection as string
	 **********************************************************************
	 */
	public static StringBuilder getCollectionAsString(IDfCollection col) {
		StringBuilder sb = new StringBuilder();
		try {
			// build the "row" of column headers
			int colNum = col.getAttrCount();
			for (int i=0; i<colNum; i++) {
				sb.append(col.getAttr(i).getName() + "\t");
			}
			sb.append("\n");

			// build each "row" by appending column values
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
			}        

		} catch (DfException e) {
			DfLogger.error(CLASSNAME + ".getCollectionAsString",e.getMessage(),null,null);
		}

		return sb;
	}

	/************************************************************************
	 * Return a single value from a query (e.g., count(*))
	 * @param query DQL query string
	 * @param session active session object
	 * @return return a single value
	 * @throws DfException 
	 ************************************************************************
	 */
	public static String runDQLQueryReturnSingleValue(String query, IDfSession session) throws DfException { 
		String rv = "";
		
		// run query
		IDfCollection col = runDQLQuery(query, session);

		rv = getSingleCollectionValueAsString(col);
		
		// close collection
		if ( (col != null) && (col.getState() != IDfCollection.DF_CLOSED_STATE) ) 
			col.close();
		return rv;
	}
	/************************************************************************
	 * Test if object is a SysObject
	 * @param pObj persistent object to check
	 * @return true/false
	 * ***********************************************************************
	 */
	public static boolean isSysObject(IDfPersistentObject pObj) {
		try {
			if (pObj.getObjectId().toString().startsWith("08"))
				return true;
			else if (isFolder((IDfSysObject) pObj))
				return true;
			else if (isCabinet((IDfSysObject) pObj))
				return true;
			else if (isDocument((IDfSysObject) pObj))
				return true;
				
		} catch (DfException dfe) {
			DfLogger.error(CLASSNAME + ".isSysObject",dfe.getMessage(),null,null);
		}
		return false;
	}
	
	/************************************************************************
	 * Test if object is a folder
	 * @param sObj object to test
	 * @return true/false
	 ************************************************************************ 
	 */
	public static boolean isFolder(IDfSysObject sObj) {
		try {
			if (sObj.getObjectId().toString().startsWith("0b"))
				return true;
		} catch (DfException dfe) {
			DfLogger.error(CLASSNAME + ".isFolder",dfe.getMessage(),null,null);
		}

		return false;
	}


	/************************************************************************
	 * Test if object is a document
	 * @param sObj object to test
	 * @return true/false
	 ************************************************************************ 
	 */
	public static boolean isDocument(IDfSysObject sObj) {
		try {
			if (sObj.getObjectId().toString().startsWith("09"))
				return true;
		} catch (DfException dfe) {
			DfLogger.error(CLASSNAME + ".isDocument",dfe.getMessage(),null,null);
		}

		return false;
	}


	/************************************************************************
	 * Test if object is a cabinet
	 * @param sObj object to test
	 * @return true/false
	 ************************************************************************ 
	 */
	public static boolean isCabinet(IDfSysObject sObj) {
		try {
			if (sObj.getObjectId().toString().startsWith("0c"))
				return true;
		} catch (DfException dfe) {
			DfLogger.error(CLASSNAME + ".isCabinet",dfe.getMessage(),null,null);
		}

		return false;
	}

	/************************************************************************
	 * Determine if a sysobject has content
	 * @param sObj document object to test for content
	 * @return true is document contains content, else false
	 ***********************************************************************
	 */
	public static boolean hasContent(IDfSysObject sObj) {
		try {
			if (sObj.getContentSize() > 0)
				return true;
		} catch (DfException dfe){
			DfLogger.error(CLASSNAME + ".hasContent",dfe.getMessage(),null,null);
		}
		return false;
	}

	/************************************************************************
	 * Return DFC version
	 * @return DFC version
	 ***********************************************************************
	 */
	public static String getDFCversion() {
		IDfClientX cx = new DfClientX();
		return cx.getDFCVersion();
	}

	/************************************************************************ 
	 * Check if DFC version meets minimum requirement
	 * @param minDFCversion minimum DFC version to check
	 * @return true if DFC meets or exceeds minimum, else false
	 ***********************************************************************
	 */
	public static boolean checkDFCversion(double minDFCversion) {
		String DFCversion = getDFCversion().substring(0,3);
		double dDFC = Double.parseDouble(DFCversion);
		if (dDFC < minDFCversion)
			return false;
		else 
			return true;
	}

	/**********************************************************************
	 * Return DCTMBasics JAR version
	 * @return version
	 ********************************************************************** 
	 */
	public static String version() {
		return VERSION;
	}
	
	/***********************************************************************
	 * Create a folder in the Docbase on a specific path.  Will create all
	 * necessary folders to create path passed in.
	 * @param path String containing full path to create
	 * @return IDfFolder object of leaf folder created
	 **********************************************************************
	 */
	public static IDfFolder createDocbasePath(IDfSession session, String path) {
		IDfFolder folder = null;
		try {
			// first see if the folder already exists
			folder = (IDfFolder) session.getObjectByQualification("dm_folder where any r_folder_path='" + path + "'");

			// if not build it
			if (null == folder) {
				// split path into separate folders
				String[] dirs = path.split("/");

				// loop through path folders and build
				String dm_path = "";
				for (int i = 0; i < dirs.length; i++) {

					if (dirs[i].length() > 0) {

						// build up path
						dm_path = dm_path + "/" + dirs[i];

						// see if this path exists
						IDfFolder testFolder = (IDfFolder) session.getObjectByQualification("dm_folder where any r_folder_path='" + dm_path + "'");
						if (null == testFolder) {

							// check if a cabinet need to be made
							if (dm_path.equalsIgnoreCase("/" + dirs[i])) {
								IDfFolder cab = (IDfFolder) session.newObject("dm_cabinet");
								cab.setObjectName(dirs[i]);
								cab.save();
								// else make a folder 
							} else {
								folder = (IDfFolder) session.newObject("dm_folder");
								folder.setObjectName(dirs[i]);

								// link it to parent
								String parent_path = "";
								for (int j = 0; j < i; j++) {
									if (dirs[j].length() > 0) {
										parent_path = parent_path + "/" + dirs[j];
									}
								}
								folder.link(parent_path);
								folder.save();
							}
						}
					}
				}
			}

		} catch (DfException dfe){
			DfLogger.error(CLASSNAME + ".createDocbasePath",dfe.getMessage(),null,null);
		}
		return folder;
	}

	/**********************************************************************
	 * Encrypt password passed in
	 * @param password password to encrypt
	 * @return encrypted password
	 **********************************************************************
	 */
	public static String encryptPassword(String password) {
		String encryptedPassword = "";
		
		try {
			encryptedPassword = RegistryPasswordUtils.encrypt(password);
			//encryptedPassword = encryptedPassword.replace("\\", "");
		} catch (DfException dfe){
			DfLogger.error(CLASSNAME + ".encryptPassword",dfe.getMessage(),null,null);
		}

        return encryptedPassword;
	}
	
	/**********************************************************************
	 * Encrypt password passed in and include "DM_ENCR_TEXT=" prefix
	 * @param password password to encrypt
	 * @return encrypted password
	 **********************************************************************
	 */
	public static String encryptPasswordWithPrefix(String password) {
		
        return "DM_ENCR_TEXT=" + encryptPassword(password);
	}

	/**********************************************************************
	 * Decrypt password passed in
	 * @param password String to decrypt
	 * @return clear text password
	 **********************************************************************
	 */
	public static String decryptPassword(String password) {
		String newPassword = "";
		
		try {
			if (password.startsWith("DM_ENCR_TEXT=")) 
				password = password.substring("DM_ENCR_TEXT=".length());
		    //password = password.replace("\\", "");
		    newPassword = RegistryPasswordUtils.decrypt(password);
		} catch (DfException dfe){
			DfLogger.error(CLASSNAME + ".encryptPassword",dfe.getMessage(),null,null);
		}
		return newPassword;
	}
	
	/**********************************************************************
	 * Open a PrintWriter as a log file
	 * @param fullFileName fully qualified file name for log file (e.g.,
	 *        c:/temp/mytest.log
	 * @param unique make the name unique in the directory
	 * @return log file to print to
	 **********************************************************************
	 */
	public static PrintWriter createLogFile(String fullFileName, boolean unique) {
        PrintWriter pw = null;
        
        try {
            //validate path to log file
            if (fullFileName == null || fullFileName.isEmpty())
            	throw new Exception ("Filename is blank, cannot open log file.");
            
            // convert slashes
            fullFileName = fullFileName.replace("\\","/");
            String logPath = fullFileName.substring(0, fullFileName.lastIndexOf("/"));
            //System.out.println("logPath=" + logPath);
            if (!logPath.isEmpty()) {
            	File dir = new File(logPath);
                if (!dir.exists()) 
                    dir.mkdirs();
            } 
            
            // create serialized name for log file
            if (unique) {
            	String filename = fullFileName.substring(fullFileName.lastIndexOf("/") + 1);
            	//System.out.println("filename=" + filename);
            	String name = filename.substring(0, filename.lastIndexOf("."));
            	//System.out.println("name=" + name);
            	String ext = filename.substring(filename.lastIndexOf(".") + 1);
            	//System.out.println("ext=" + ext);
            	//String now = Long.toString(System.currentTimeMillis());
            	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String now = formatter.format(new Date());
                //System.out.println("now=" + now);
            	fullFileName = logPath + "/" + name + "-" + now + "." + ext;
            	//System.out.println("fullFileNAme=" + fullFileName);
            } 

            // open file
            File log = new File(fullFileName);
            pw = new PrintWriter(log);

            DfLogger.debug(CLASSNAME + ".createLogFile","log file is " + fullFileName,null,null);
        } catch (Exception dfe){
			DfLogger.error(CLASSNAME + ".createLogFile",dfe.getMessage(),null,null);
        }
        return pw;
    }

    
// <SDG><
	
	
}
