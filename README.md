dmBasics is a collection of static methods for implementing common DFC programming tasks.  The JAR contains the following methods:

* checkDFCversion(double minDFCversion) => Check if DFC version meets minimum requirement
* createDocbasePath(com.documentum.fc.client.IDfSession session, java.lang.String path) => Create a folder in the Docbase on a specific path.
* createLogFile(java.lang.String fullFileName, boolean unique) => Open a PrintWriter as a log file
* decryptPassword(java.lang.String password) => Decrypt password passed in
* encryptPassword(java.lang.String password) => Encrypt password passed in
* encryptPasswordWithPrefix(java.lang.String password) => Encrypt password passed in and include "DM_ENCR_TEXT=" prefix
* getArgs(java.lang.String[] args) => Parse args into a Properties object and print if necessary
* getCollectionAsString(com.documentum.fc.client.IDfCollection col) => Return an entire IDfCollection as a String with tabbed columns and rows suitable for printing.
* getDFCversion() => Return DFC version
* hasContent(com.documentum.fc.client.IDfSysObject sObj) => Determine if a sysobject has content
* isCabinet(com.documentum.fc.client.IDfSysObject sObj) => Test if object is a cabinet
* isDocument(com.documentum.fc.client.IDfSysObject sObj) => Test if object is a document
* isFolder(com.documentum.fc.client.IDfSysObject sObj) => Test if object is a folder
* isSysObject(com.documentum.fc.client.IDfPersistentObject pObj) => Test if object is a SysObject
* logoff(com.documentum.fc.client.IDfSession session) => Logoff active session
* logon(java.lang.String docbase, java.lang.String username, java.lang.String password) => Establish a Docbase session using credentials supplied
* runCachedQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run DQL cached SELECT query
* runCreateObjectQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => run DQL CREATE...OBJECT query
* runDeleteObjectQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run DQL DELETE...OBJECT query
* runDeleteQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run DELETE query to delete rows from a registered database table
* runDQLQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run a generic DQL query.
* runDQLQueryReturnSingleValue(java.lang.String query, com.documentum.fc.client.IDfSession session) => Return a single value from a query (e.g., count(*))
* runExecQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run DQL EXEC query
* runInsertQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run INSERT query to insert rows into a registered database table
* runRegisterTableQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Register table query
* runSelectQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run a DQL SELECT query
* runSQLQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run SQL pass-through query
* runUpdateObjectQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run DQL UPDATE...OBJECT query
* runUpdateQuery(java.lang.String query, com.documentum.fc.client.IDfSession session) => Run UPDATE query to update rows in a registered database table
* version() => Return DCTMBasics JAR version
