package ca.sqlpower.architect.etl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.LogWriter;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.Monitorable;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.security.PLSecurityManager;
import ca.sqlpower.sql.DBConnection;
import ca.sqlpower.sql.DatabaseObject;
import ca.sqlpower.sql.DefaultParameters;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SQL;

public class PLExport implements Monitorable {

    private static final Logger logger = Logger.getLogger(PLExport.class);

    protected LogWriter logWriter = null;

    public static final String PL_GENERATOR_VERSION = "PLExport $Revision$".replace('$', ' ').trim();

    protected DefaultParameters defParam;

    protected File file;

    protected String folderName; // = "Architect Jobs";

    protected String jobId;

    protected String jobDescription;

    protected String jobComment;

    protected boolean runPLEngine;

    protected PLSecurityManager sm;

    protected ArchitectDataSource repositoryDataSource;

    protected ArchitectDataSource targetDataSource; //

    protected String targetSchema; // save this to properties file?

    protected String targetCatalog;

    protected String repositorySchema;

    protected String repositoryCatalog;

    protected boolean hasStarted;

    private ArrayList<LabelValueBean> exportResultList = new ArrayList();

    /**
     * @return Returns the hasStarted.
     */
    public boolean hasStarted() {
        return hasStarted;
    }

    protected boolean finished; // so the Timer thread knows when to kill itself

    protected boolean cancelled; // FIXME: placeholder for when the user
                                    // cancels halfway through a PL Export

    List<SQLTable> currentDB; // if this is non-null, an export job is running

    int tableCount = 0; // only has meaning when an export job is running

    public Integer getJobSize() throws ArchitectException {
        if (currentDB != null) {
            return new Integer(currentDB.size());
        } else {
            return null;
        }
    }

    public int getProgress() throws ArchitectException {
        if (currentDB != null) {
            return tableCount;
        } else {
            return 0;
        }
    }

    public boolean isFinished() throws ArchitectException {
        return finished;
    }

    public String getMessage() {
        return null;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void cancelJob() {
        finished = true;
        cancelled = true;
    }

    /**
     * Creates a folder if one with the name folderName does not exist already.
     */
    public void maybeInsertFolder(Connection con) throws SQLException {

        if (folderName == null || folderName.length() == 0) {
            exportResultList.add(new LabelValueBean("Create Folder:" + folderName, "Skipped, no parameter"));
            return;
        }

        String status = "Unknown";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            String folder_table = DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "pl_folder");

            rs = stmt.executeQuery("SELECT 1 FROM " + folder_table + " WHERE folder_name=" + SQL.quote(folderName));

            if (!rs.next()) {
                status = "OK";
                StringBuffer sql = new StringBuffer("INSERT INTO ");
                sql.append(folder_table);
                sql.append(" (folder_name,folder_desc,folder_status,last_backup_no)");
                sql.append(" VALUES (");

                sql.append(SQL.quote(folderName)); // folder_name
                sql.append(",").append(
                        SQL.quote("This Folder contains jobs and transactions created by the Power*Architect")); // folder_desc
                sql.append(",").append(SQL.quote(null)); // folder_status
                sql.append(",").append(SQL.quote(null)); // last_backup_no
                sql.append(")");
                logWriter.info("Insert into " + folder_table + ", PK=" + folderName);
                logger.debug("MAYBE INSERT SQL: " + sql.toString());
                stmt.executeUpdate(sql.toString());
            } else {
                status = "Skipped, exist";
            }
        } catch (SQLException e) {
            status = "Error";
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            exportResultList.add(new LabelValueBean("Create Folder:" + folderName, status));

        }
    }

    /**
     * Inserts an entry in the folderName folder of the named object of the
     * given type.
     */
    public void insertFolderDetail(Connection con, String objectType, String objectName) throws SQLException {

        if (folderName == null || folderName.length() == 0)
            return;

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "PL_FOLDER_DETAIL"));
        sql.append(" (folder_name,object_type,object_name)");
        sql.append(" VALUES (");

        sql.append(SQL.quote(folderName)); // folder_name
        sql.append(",").append(SQL.quote(objectType)); // object_type
        sql.append(",").append(SQL.quote(objectName)); // object_name
        sql.append(")");
        logWriter.info("Insert into PL FOLDER_DETAIL, PK=" + folderName + "|" + objectType + "|" + objectName);
        Statement s = con.createStatement();
        try {
            logger.debug("INSERT FOLDER DETAIL SQL: " + sql.toString());
            s.executeUpdate(sql.toString());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    /**
     * Get a collection of transsaction name
     * 
     * @param con
     *            A connection to the PL database
     */
    public Set getTransName(Connection con, String baseTransId) throws SQLException {

        StringBuffer sql = new StringBuffer("SELECT TRANS_ID FROM ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "TRANS"));
        sql.append(" WHERE TRANS_ID LIKE ");
        sql.append(SQL.quote(baseTransId + "%"));
        Statement s = con.createStatement();
        ResultSet rs = null;
        Set set = new HashSet();
        try {
            logger.debug("DETECT TRANS_ID COLLISION: " + sql.toString());
            rs = s.executeQuery(sql.toString());
            while (rs.next()) {
                set.add(rs.getString(1));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (s != null) {
                s.close();
            }
        }
        return set;
    }

    /**
     * Detects collisions in the trans_id and job_ib namespaces and generates
     * the index of the next unique identifier. The insert trans logic uses the
     * index of the identifier, so return it instead of the unique string.
     * 
     * @param con
     *            A connection to the PL database
     */
    public int generateUniqueTransIdx(Connection con, String transId) throws SQLException {

        Set set = getTransName(con, transId);
        boolean foundUnique = false;
        int i = 0;
        while (!foundUnique) {
            i++;
            foundUnique = !set.contains(transId + "_" + i);
            if (!foundUnique) {
                logger.debug("detected collision for trans id: " + transId + "_" + i);
            } else {
                logger.debug("next unique trans id is: " + transId + "_" + i);
            }
        }
        return i;
    }

    public int generateUniqueTransIdx(Set set, String transId) throws SQLException {

        boolean foundUnique = false;
        int i = 0;
        while (!foundUnique) {
            i++;
            foundUnique = !set.contains(transId + "_" + i);
            if (!foundUnique) {
                logger.debug("detected collision for trans id: " + transId + "_" + i);
            } else {
                logger.debug("next unique trans id is: " + transId + "_" + i);
            }
        }
        return i;
    }

    /**
     * Inserts a job into the PL_JOB table. The job name is specified by
     * {@link #jobId}.
     * 
     * @param con
     *            A connection to the PL database
     */
    private void insertJob(Connection con) throws SQLException {

        String status = "Unknown";
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "PL_JOB"));

        sql
                .append(" (JOB_ID, JOB_DESC, JOB_FREQ_DESC, PROCESS_CNT, SHOW_PROGRESS_FREQ, PROCESS_SEQ_CODE, MAX_RETRY_COUNT, WRITE_DB_ERRORS_IND, ROLLBACK_SEGMENT_NAME, LOG_FILE_NAME, ERR_FILE_NAME, UNIX_LOG_FILE_NAME, UNIX_ERR_FILE_NAME, APPEND_TO_LOG_IND, APPEND_TO_ERR_IND, DEBUG_MODE_IND, COMMIT_FREQ, JOB_COMMENT, CREATE_DATE, LAST_update_DATE, LAST_update_USER, BATCH_SCRIPT_FILE_NAME, JOB_SCRIPT_FILE_NAME, UNIX_BATCH_SCRIPT_FILE_NAME, UNIX_JOB_SCRIPT_FILE_NAME, JOB_STATUS, LAST_BACKUP_NO, LAST_RUN_DATE, SKIP_PACKAGES_IND, SEND_EMAIL_IND, LAST_update_OS_USER, STATS_IND, checked_out_ind, checked_out_date, checked_out_user, checked_out_os_user");
        sql.append(") VALUES (");
        sql.append(SQL.quote(jobId)); // JOB_ID
        sql.append(",").append(SQL.quote(jobDescription)); // JOB_DESC
        sql.append(",").append(SQL.quote(null)); // JOB_FREQ_DESC
        sql.append(",").append(SQL.quote(null)); // PROCESS_CNT
        sql.append(",").append(SQL.quote(null)); // SHOW_PROGRESS_FREQ
        sql.append(",").append(SQL.quote(null)); // PROCESS_SEQ_CODE
        sql.append(",").append(SQL.quote(null)); // MAX_RETRY_COUNT
        sql.append(",").append(SQL.quote(null)); // WRITE_DB_ERRORS_IND
        sql.append(",").append(SQL.quote(null)); // ROLLBACK_SEGMENT_NAME
        logger.debug("default log path is: " + defParam.get("default_log_file_path"));
        logger.debug("default err path is: " + defParam.get("default_err_file_path"));
        sql.append(",").append(
                SQL.quote(escapeString(con, fixWindowsPath(defParam.get("default_log_file_path"))) + jobId + ".log")); // LOG_FILE_NAME
        sql.append(",").append(
                SQL.quote(escapeString(con, fixWindowsPath(defParam.get("default_err_file_path"))) + jobId + ".err")); // ERR_FILE_NAME
        sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_log_file_path")) + jobId + ".log")); // UNIX_LOG_FILE_NAME
        sql.append(",").append(SQL.quote(fixUnixPath(defParam.get("default_err_file_path")) + jobId + ".err")); // UNIX_ERR_FILE_NAME
        sql.append(",").append(SQL.quote("N")); // APPEND_TO_LOG_IND
        sql.append(",").append(SQL.quote("N")); // APPEND_TO_ERR_IND
        sql.append(",").append(SQL.quote("N")); // DEBUG_MODE_IND
        sql.append(",").append("100"); // COMMIT_FREQ
        sql.append(",").append(SQL.quote(jobComment)); // JOB_COMMENT
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // CREATE_DATE
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",").append(SQL.quote(null)); // BATCH_SCRIPT_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // JOB_SCRIPT_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // UNIX_BATCH_SCRIPT_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // UNIX_JOB_SCRIPT_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // JOB_STATUS
        sql.append(",").append(SQL.quote(null)); // LAST_BACKUP_NO
        sql.append(",").append(SQL.quote(null)); // LAST_RUN_DATE
        sql.append(",").append(SQL.quote(null)); // SKIP_PACKAGES_IND
        sql.append(",").append(SQL.quote("N")); // SEND_EMAIL_IND
        sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
        sql.append(",").append(SQL.quote("N")); // STATS_IND
        sql.append(",").append(SQL.quote(null)); // checked_out_ind
        sql.append(",").append(SQL.quote(null)); // checked_out_date
        sql.append(",").append(SQL.quote(null)); // checked_out_user
        sql.append(",").append(SQL.quote(null)); // checked_out_os_user
        sql.append(")");
        Statement s = con.createStatement();
        logWriter.info("INSERT into PL JOB, PK=" + jobId);

        try {
            logger.debug("INSERT PL JOB: " + sql.toString());
            s.executeUpdate(sql.toString());
            status = "OK";
        } catch (SQLException e) {
            status = "Error";
            throw e;
        } finally {
            if (s != null) {
                s.close();
            }
            exportResultList.add(new LabelValueBean("Create Job " + jobId, status));
        }

    }

    /**
     * Inserts a job entry into the JOB_DETAIL table. The job name is specified
     * by {@link #jobId}.
     * 
     * @param con
     *            A connection to the PL database
     */
    public void insertJobDetail(Connection con, int seqNo, String objectType, String objectName) throws SQLException {

        String status = "Unknown";
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "JOB_DETAIL"));
        sql
                .append(" (JOB_ID, JOB_PROCESS_SEQ_NO, OBJECT_TYPE, OBJECT_NAME, JOB_DETAIL_COMMENT, LAST_update_DATE, LAST_update_USER, FAILURE_ABORT_IND, WARNING_ABORT_IND, PKG_PARAM, ACTIVE_IND, LAST_update_OS_USER )");
        sql.append(" VALUES (");
        sql.append(SQL.quote(jobId)); // JOB_ID
        sql.append(",").append(seqNo); // JOB_PROCESS_SEQ_NO
        sql.append(",").append(SQL.quote(objectType)); // OBJECT_TYPE
        sql.append(",").append(SQL.quote(objectName)); // OBJECT_NAME
        sql.append(",").append(SQL.quote("Generated by Power*Architect " + PL_GENERATOR_VERSION)); // JOB_DETAIL_COMMENT
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",").append(SQL.quote("N")); // FAILURE_ABORT_IND
        sql.append(",").append(SQL.quote("N")); // WARNING_ABORT_IND
        sql.append(",").append(SQL.quote(null)); // PKG_PARAM
        sql.append(",").append(SQL.quote("Y")); // ACTIVE_IND
        sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
        sql.append(")");
        logWriter.info("INSERT into JOB DETAIL, PK=" + jobId + "|" + seqNo);
        Statement s = con.createStatement();

        try {
            logger.debug("INSERT JOB DETAIL: " + sql.toString());
            s.executeUpdate(sql.toString());
            status = "OK";
        } catch (SQLException e) {
            status = "Error";
            throw e;
        } finally {
            if (s != null) {
                s.close();
            }
            exportResultList.add(new LabelValueBean("Create Job Step " + seqNo + objectName, status));
        }
    }

    /**
     * Inserts a Power*Loader transaction header into the TRANS table.
     * 
     * @param con
     *            A connection to the PL database
     * @param transId
     *            the name that the new transaction should have.
     * @param remarks
     *            The transaction comment/remarks. transaction will populate.
     */
    public void insertTrans(Connection con, String transId, String remarks) throws ArchitectException, SQLException {
        String status = "Unknown";
        StringBuffer sql = new StringBuffer();
        sql.append(" INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "TRANS"));

        sql.append(" (TRANS_ID, TRANS_DESC, TRANS_COMMENT, ACTION_TYPE, MAX_RETRY_COUNT,\n");
        sql.append(" PROCESS_SEQ_CODE, LAST_update_DATE, LAST_update_USER, DEBUG_MODE_IND,\n");
        sql.append(" COMMIT_FREQ, PROCESS_ADD_IND, PROCESS_UPD_IND, PROCESS_DEL_IND,\n");
        sql.append(" WRITE_DB_ERRORS_IND, ROLLBACK_SEGMENT_NAME, ERR_FILE_NAME,\n");
        sql.append(" LOG_FILE_NAME, BAD_FILE_NAME, SHOW_PROGRESS_FREQ, SKIP_CNT,\n");
        sql.append(" PROCESS_CNT, CREATE_DATE, UNIX_LOG_FILE_NAME,\n");
        sql.append(" UNIX_ERR_FILE_NAME, UNIX_BAD_FILE_NAME, REMOTE_CONNECTION_STRING,\n");
        sql.append(" APPEND_TO_LOG_IND, APPEND_TO_ERR_IND, APPEND_TO_BAD_IND, TRANS_STATUS,\n");
        sql.append(" LAST_BACKUP_NO, LAST_RUN_DATE, SKIP_PACKAGES_IND, SEND_EMAIL_IND,\n");
        sql.append(" PROMPT_COLMAP_INDEXES_IND, TRANSACTION_TYPE, DELTA_SORT_IND,\n");
        sql.append(" LAST_update_OS_USER, STATS_IND, ODBC_IND, checked_out_ind,\n");
        sql.append(" checked_out_date, checked_out_user, checked_out_os_user\n");
        sql.append(") VALUES (");
        sql.append(SQL.quote(transId)); // TRANS_ID
        sql.append(",\n").append(SQL.quote("Generated by Power*Architect " + PL_GENERATOR_VERSION)); // TRANS_DESC
        sql.append(",\n").append(SQL.quote(remarks)); // TRANS_COMMENT
        sql.append(",\n").append(SQL.quote(null)); // ACTION_TYPE
        sql.append(",\n").append(SQL.quote(null)); // MAX_RETRY_COUNT
        sql.append(",\n").append(SQL.quote(null)); // PROCESS_SEQ_CODE
        sql.append(",\n").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",\n").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",\n").append(SQL.quote("N")); // DEBUG_MODE_IND
        sql.append(",\n").append(defParam.get("commit_freq")); // COMMIT_FREQ
        sql.append(",\n").append(SQL.quote(null)); // PROCESS_ADD_IND
        sql.append(",\n").append(SQL.quote(null)); // PROCESS_UPD_IND
        sql.append(",\n").append(SQL.quote(null)); // PROCESS_DEL_IND
        sql.append(",\n").append(SQL.quote(null)); // WRITE_DB_ERRORS_IND
        sql.append(",\n").append(SQL.quote(null)); // ROLLBACK_SEGMENT_NAME
        logger.debug("err_file_path: " + defParam.get("default_err_file_path"));
        logger.debug("log_file_path: " + defParam.get("default_log_file_path"));
        logger.debug("bad_file_path: " + defParam.get("default_bad_file_path"));
        sql.append(",\n").append(
                SQL.quote(escapeString(con, fixWindowsPath(defParam.get("default_err_file_path"))) + transId + ".err"));
        sql.append(",\n").append(
                SQL.quote(escapeString(con, fixWindowsPath(defParam.get("default_log_file_path"))) + transId + ".log"));
        sql.append(",\n").append(
                SQL.quote(escapeString(con, fixWindowsPath(defParam.get("default_bad_file_path"))) + transId + ".bad"));
        sql.append(",\n").append(defParam.get("show_progress_freq")); // SHOW_PROGRESS_FREQ
        sql.append(",\n").append("0");// SKIP_CNT
        sql.append(",\n").append("0");// PROCESS_CNT
        // SOURCE_DATE_FORMAT: col was missing in arthur-test-pl,
        // and we were setting it to null here, so I took it out of the
        // statement. -JF
        sql.append(",\n").append(SQL.escapeDate(con, new java.util.Date())); // CREATE_DATE
        sql.append(",\n").append(SQL.quote(fixUnixPath(defParam.get("default_unix_log_file_path")) + transId + ".log"));
        sql.append(",\n").append(SQL.quote(fixUnixPath(defParam.get("default_unix_err_file_path")) + transId + ".err"));
        sql.append(",\n").append(SQL.quote(fixUnixPath(defParam.get("default_unix_bad_file_path")) + transId + ".bad"));
        sql.append(",\n").append(SQL.quote(null)); // REMOTE_CONNECTION_STRING
        sql.append(",\n").append(SQL.quote(defParam.get("append_to_log_ind"))); // APPEND_TO_LOG_IND
        sql.append(",\n").append(SQL.quote(defParam.get("append_to_err_ind"))); // APPEND_TO_ERR_IND
        sql.append(",\n").append(SQL.quote(defParam.get("append_to_bad_ind"))); // APPEND_TO_BAD_IND
        // REC_DELIMITER: col was missing in most of our schemas;
        // to save trouble, we'll let it default to null. -JF
        sql.append(",\n").append(SQL.quote(null)); // TRANS_STATUS
        sql.append(",\n").append(SQL.quote(null)); // LAST_BACKUP_NO
        sql.append(",\n").append(SQL.quote(null)); // LAST_RUN_DATE
        sql.append(",\n").append(SQL.quote("N")); // SKIP_PACKAGES_IND
        sql.append(",\n").append(SQL.quote("N")); // SEND_EMAIL_IND
        sql.append(",\n").append(SQL.quote(null)); // PROMPT_COLMAP_INDEXES_IND
        sql.append(",\n").append(SQL.quote("POWER_LOADER")); // TRANSACTION_TYPE
        sql.append(",\n").append(SQL.quote("Y")); // DELTA_SORT_IND
        sql.append(",\n").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
        sql.append(",\n").append(SQL.quote("N")); // STATS_IND
        sql.append(",\n").append(SQL.quote("Y")); // ODBC_IND
        sql.append(",\n").append(SQL.quote(null)); // checked_out_ind
        sql.append(",\n").append(SQL.quote(null)); // checked_out_date
        sql.append(",\n").append(SQL.quote(null)); // checked_out_user
        sql.append(",\n").append(SQL.quote(null)); // checked_out_os_user
        sql.append(")");
        logWriter.info("INSERT into TRANS, PK=" + transId);
        Statement s = con.createStatement();
        try {
            logger.debug("INSERT TRANS: " + sql.toString());
            s.executeUpdate(sql.toString());
            status = "OK";

        } catch (SQLException ex) {
            logger.error("This statement caused an exception: " + sql);
            status = "Error";
            throw ex;
        } finally {
            if (s != null) {
                s.close();
            }
            exportResultList.add(new LabelValueBean("Create Trans " + transId, status));
        }
    }

    /**
     * Inserts a record into the TRANS_TABLE_FILE table.
     * 
     * @param con
     *            The connection to the PL databse.
     * @param transId
     *            The name of the header transaction.
     * @param table
     *            The SQLTable that this record describes.
     * @param isOutput
     *            True if table is an output table (part of the play pen); false
     *            if table is an input table (part of a source DB).
     * @param seqNo
     *            The sequence number of this table in its parent transaction.
     */
    public void insertTransTableFile(Connection con, String transId, String tableFileId, SQLTable table,
            boolean isOutput, int seqNo) throws SQLException {
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "TRANS_TABLE_FILE"));

        sql
                .append(" (TRANS_ID, TABLE_FILE_ID, TABLE_FILE_IND, TABLE_FILE_TYPE, INPUT_OUTPUT_IND, SYSTEM_NAME, SERVER_NAME, FILE_CHAR_SET, TEXT_DELIMITER, TEXT_QUALIFIER, OWNER, TABLE_FILE_NAME, TABLE_FILE_ACCESS_PATH, MAX_ADD_COUNT, MAX_UPD_COUNT, MAX_DEL_COUNT, MAX_ERR_COUNT, FILTER_CRITERION, PROC_SEQ_NO, HEADER_REC_IND, LAST_UPDATE_DATE, LAST_UPDATE_USER, TRANS_TABLE_FILE_COMMENT, DB_CONNECT_NAME, UNIX_FILE_ACCESS_PATH, REC_DELIMITER, SELECT_CLAUSE, FROM_CLAUSE, WHERE_CLAUSE, ORDER_BY_CRITERION, TRUNCATE_IND, ACTION_TYPE, ANALYZE_IND, PRE_PROCESSED_FILE_NAME, UNIX_PRE_PROCESSED_FILE_NAME, PARENT_FILE_ID, CHILD_REQUIRED_IND, LAST_UPDATE_OS_USER, DELETE_IND, FROM_CLAUSE_DB)");

        sql.append(" VALUES (");
        sql.append(SQL.quote(transId)); // TRANS_ID
        sql.append(",").append(SQL.quote(tableFileId)); // TABLE_FILE_ID
        sql.append(",").append(SQL.quote("TABLE")); // TABLE_FILE_IND

        String type;
        String dbConnectName;
        ArchitectDataSource dataSource;

        if (isOutput) {
            dataSource = targetDataSource; // target table
        } else {
            dataSource = table.getParentDatabase().getDataSource(); // input
                                                                    // table
        }
        dbConnectName = dataSource.get(ArchitectDataSource.PL_LOGICAL);

        if (isOracle(dataSource)) {
            type = "ORACLE";
        } else if (isSQLServer(dataSource)) {
            type = "SQL SERVER";
        } else if (isDB2(dataSource)) {
            type = "DB2";
        } else if (isPostgres(dataSource)) {
            type = "POSTGRES";
        } else {
            throw new IllegalArgumentException("Unsupported target database type");
        }
        sql.append(",").append(SQL.quote(type)); // TABLE_FILE_TYPE

        sql.append(",").append(SQL.quote(isOutput ? "O" : "I")); // INPUT_OUTPUT_IND
        sql.append(",").append(SQL.quote(null)); // SYSTEM_NAME
        sql.append(",").append(SQL.quote(null)); // SERVER_NAME
        sql.append(",").append(SQL.quote(null)); // FILE_CHAR_SET
        sql.append(",").append(SQL.quote(null)); // TEXT_DELIMITER
        sql.append(",").append(SQL.quote(null)); // TEXT_QUALIFIER
        sql.append(",").append(SQL.quote(isOutput ? targetSchema : table.getParent().toString())); // OWNER
        sql.append(",").append(SQL.quote(table.getName())); // TABLE_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // TABLE_FILE_ACCESS_PATH
        sql.append(",").append(SQL.quote(null)); // MAX_ADD_COUNT
        sql.append(",").append(SQL.quote(null)); // MAX_UPD_COUNT
        sql.append(",").append(SQL.quote(null)); // MAX_DEL_COUNT
        sql.append(",").append(SQL.quote(null)); // MAX_ERR_COUNT
        sql.append(",").append(SQL.quote(null)); // FILTER_CRITERION
        sql.append(",").append(seqNo); // PROC_SEQ_NO
        sql.append(",").append(SQL.quote(null)); // HEADER_REC_IND
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",").append(SQL.quote("Generated by Power*Architect " + PL_GENERATOR_VERSION)); // TRANS_TABLE_FILE_COMMENT
        sql.append(",").append(SQL.quote(dbConnectName)); // DB_CONNECT_NAME
                                                            // (PL_LOGICAL)
        sql.append(",").append(SQL.quote(null)); // UNIX_FILE_ACCESS_PATH
        sql.append(",").append(SQL.quote(null)); // REC_DELIMITER
        sql.append(",").append(SQL.quote(null)); // SELECT_CLAUSE
        sql.append(",").append(SQL.quote(null)); // FROM_CLAUSE
        sql.append(",").append(SQL.quote(null)); // WHERE_CLAUSE
        sql.append(",").append(SQL.quote(null)); // ORDER_BY_CRITERION
        sql.append(",").append(SQL.quote(null)); // TRUNCATE_IND
        sql.append(",").append(SQL.quote(null)); // ACTION_TYPE
        sql.append(",").append(SQL.quote(null)); // ANALYZE_IND
        sql.append(",").append(SQL.quote(null)); // PRE_PROCESSED_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // UNIX_PRE_PROCESSED_FILE_NAME
        sql.append(",").append(SQL.quote(null)); // PARENT_FILE_ID
        sql.append(",").append(SQL.quote(null)); // CHILD_REQUIRED_IND
        sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_UPDATE_OS_USER
        sql.append(",").append(SQL.quote(null)); // DELETE_IND
        sql.append(",").append(SQL.quote(null)); // FROM_CLAUSE_DB
        sql.append(")");
        logWriter.info("INSERT into TRANS_TABLE_FILE, PK=" + transId + "|" + tableFileId);
        Statement s = con.createStatement();
        try {
            logger.debug("INSERT TRANS_TABLE_FILE: " + sql.toString());
            s.executeUpdate(sql.toString());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    /**
     * Inserts mapping records (by calling insertTransColMap) for all mandatory
     * columns of outputTable, as well as all columns of outputTable whose
     * source table is inputTable.
     */
    public void insertMappings(Connection con, String transId, String outputTableId, SQLTable outputTable,
            String inputTableId, SQLTable inputTable) throws SQLException, ArchitectException {
        int seqNo = 1;
        Iterator outCols = outputTable.getColumns().iterator();
        while (outCols.hasNext()) {
            SQLColumn outCol = (SQLColumn) outCols.next();
            SQLColumn sourceCol = outCol.getSourceColumn();
            if (sourceCol != null) {
                if ((sourceCol.getParentTable() == inputTable)
                        && ((outCol.getNullable() == DatabaseMetaData.columnNoNulls) || (sourceCol != null))) {
                    // also covers PK
                    insertTransColMap(con, transId, outputTableId, outCol, inputTableId, seqNo);
                    seqNo++;
                }
            }
        }
    }

    /**
     * Inserts a column mapping record for outputColumn into the TRANS_COL_MAP
     * table.
     * 
     * @param con
     *            The connection to the PL database.
     * @param transId
     *            The transaction name.
     * @param outputColumn
     *            The column to generate a mapping for.
     * @param seqNo
     *            The sequence number of the output table in trans_table_file
     */
    public void insertTransColMap(Connection con, String transId, String outputTableId, SQLColumn outputColumn,
            String inputTableId, int seqNo) throws SQLException {
        SQLColumn inputColumn = outputColumn.getSourceColumn();
        String inputColumnName;
        if (inputColumn != null) {
            inputColumnName = inputColumn.getName();
        } else {
            inputColumnName = null;
        }
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "TRANS_COL_MAP"));
        sql
                .append(" (TRANS_ID, INPUT_TABLE_FILE_ID, INPUT_TRANS_COL_NAME, OUTPUT_TABLE_FILE_ID, OUTPUT_TRANS_COL_NAME, VALID_ACTION_TYPE, NATURAL_ID_IND, REAL_MEM_TRANS_IND, DEFAULT_VALUE, INPUT_TRANS_VALUE, OUTPUT_TRANS_VALUE, TRANS_TABLE_NAME, SEQ_NAME, GRP_FUNC_STRING, TRANS_COL_MAP_COMMENT, PROCESS_SEQ_NO, LAST_update_DATE, LAST_update_USER, OUTPUT_PROC_SEQ_NO, TRANSLATION_VALUE, ACTIVE_IND, PL_SEQ_IND, PL_SEQ_INCREMENT, LAST_update_OS_USER, TRANSFORMATION_CRITERIA, PL_SEQ_update_TABLE_IND, SEQ_TABLE_IND, SEQ_WHERE_CLAUSE)");
        sql.append(" VALUES (");

        sql.append(SQL.quote(transId)); // TRANS_ID
        sql.append(",").append(SQL.quote(inputTableId)); // INPUT_TABLE_FILE_ID
        sql.append(",").append(SQL.quote(inputColumnName)); // INPUT_TRANS_COL_NAME
        sql.append(",").append(SQL.quote(outputTableId)); // OUTPUT_TABLE_FILE_ID
        sql.append(",").append(SQL.quote(outputColumn.getName())); // OUTPUT_TRANS_COL_NAME
        sql.append(",").append(SQL.quote(outputColumn.getPrimaryKeySeq() != null ? "A" : "AU")); // VALID_ACTION_TYPE
        sql.append(",").append(SQL.quote(outputColumn.getPrimaryKeySeq() != null ? "Y" : "N")); // NATURAL_ID_IND
        sql.append(",").append(SQL.quote(null)); // REAL_MEM_TRANS_IND
        sql.append(",").append(SQL.quote(outputColumn.getDefaultValue())); // DEFAULT_VALUE
        sql.append(",").append(SQL.quote(null)); // INPUT_TRANS_VALUE
        sql.append(",").append(SQL.quote(null)); // OUTPUT_TRANS_VALUE
        sql.append(",").append(SQL.quote(null)); // TRANS_TABLE_NAME
        sql.append(",").append(SQL.quote(null)); // SEQ_NAME
        sql.append(",").append(SQL.quote(null)); // GRP_FUNC_STRING
        sql.append(",").append(SQL.quote("Generated by Power*Architect " + PL_GENERATOR_VERSION)); // TRANS_COL_MAP_COMMENT
        sql.append(",").append(SQL.quote(null)); // PROCESS_SEQ_NO
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",").append(seqNo); // OUTPUT_PROC_SEQ_NO from
                                        // trans_table_file.seq_no?
        sql.append(",").append(SQL.quote(null)); // TRANSLATION_VALUE
        sql.append(",").append(SQL.quote("Y")); // ACTIVE_IND
        sql.append(",").append(SQL.quote(null)); // PL_SEQ_IND
        sql.append(",").append(SQL.quote(null)); // PL_SEQ_INCREMENT
        sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
        sql.append(",").append(SQL.quote(null)); // TRANSFORMATION_CRITERIA
        sql.append(",").append(SQL.quote(null)); // PL_SEQ_update_TABLE_IND
        sql.append(",").append(SQL.quote(null)); // SEQ_TABLE_IND
        sql.append(",").append(SQL.quote(null)); // SEQ_WHERE_CLAUSE
        sql.append(")");
        logWriter.info("INSERT into TRANS_COL_MAP, PK=" + transId + "|" + outputTableId + "|" + outputColumn.getName());
        Statement s = con.createStatement();
        try {
            logger.debug("INSERT TRANS_COL_MAP: " + sql.toString());
            s.executeUpdate(sql.toString());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    /**
     * Inserts a transaction exception handler into the TRANS_EXCEPT_HANDLE
     * table. You specify the action type as one of ACTION_TYPE_ADD,
     * ACTION_TYPE_UPDATE, or ACTION_TYPE_DELETE and this method figures out the
     * rest for you.
     * 
     * @param con
     *            A connection to the PL database
     * @param actionType
     *            the action type to insert.
     * @param transId
     *            the transaction to add this exception handler to.
     */
    public void insertTransExceptHandler(Connection con, String actionType, String transId, Connection targetConnection)
            throws SQLException {

        String errorCode = "";
        String resultActionType;
        String databaseType = "";

        if (DBConnection.isOracle(targetConnection)) {
            databaseType = "ORACLE";
            if (actionType.equals("A")) {
                errorCode = "-1";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "1403";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "1403";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isSQLServer(targetConnection)) {
            databaseType = "SQL SERVER";
            if (actionType.equals("A")) {
                errorCode = "-2627";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isDB2(targetConnection)) {
            databaseType = "DB2";
            if (actionType.equals("A")) {
                errorCode = "-803";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isPostgres(targetConnection)) {
            databaseType = "POSTGRES";
            if (actionType.equals("A")) {
                errorCode = "23505";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else {
            throw new IllegalArgumentException("Unsupported Target Database type");
        }
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(DDLUtils.toQualifiedName(repositoryCatalog, repositorySchema, "TRANS_EXCEPT_HANDLE"));
        sql
                .append(" (TRANS_ID,INPUT_ACTION_TYPE,DBMS_ERROR_CODE,RESULT_ACTION_TYPE,EXCEPT_HANDLE_COMMENT,LAST_update_DATE,LAST_update_USER,PKG_NAME,PKG_PARAM,PROC_FUNC_IND,ACTIVE_IND,LAST_update_OS_USER,DATABASE_TYPE)");
        sql.append(" VALUES (");
        sql.append(SQL.quote(transId)); // TRANS_ID
        sql.append(",").append(SQL.quote(actionType)); // INPUT_ACTION_TYPE
        sql.append(",").append(SQL.quote(errorCode)); // DBMS_ERROR_CODE
        sql.append(",").append(SQL.quote(resultActionType)); // RESULT_ACTION_TYPE
        sql.append(",").append(SQL.quote("Generated by Power*Architect " + PL_GENERATOR_VERSION)); // EXCEPT_HANDLE_COMMENT
        sql.append(",").append(SQL.escapeDate(con, new java.util.Date())); // LAST_update_DATE
        sql.append(",").append(SQL.quote(con.getMetaData().getUserName())); // LAST_update_USER
        sql.append(",").append(SQL.quote(null)); // PKG_NAME
        sql.append(",").append(SQL.quote(null)); // PKG_PARAM
        sql.append(",").append(SQL.quote(null)); // PROC_FUNC_IND
        sql.append(",").append(SQL.quote("Y")); // ACTIVE_IND
        sql.append(",").append(SQL.quote(System.getProperty("user.name"))); // LAST_update_OS_USER
        sql.append(",").append(SQL.quote(databaseType)); // DATABASE_TYPE
        sql.append(")");
        logWriter.info("INSERT into TRANS_EXCEPT_HANDLE, PK=" + transId + "|" + actionType + "|" + errorCode);
        Statement s = con.createStatement();
        try {
            logger.debug("INSERT TRANS_EXCEPT_HANDLE: " + sql.toString());
            s.executeUpdate(sql.toString());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    /**
     * Does the actual insertion of the PL metadata records into the PL
     * database.
     * 
     * @return
     * 
     * TODO: Strictly speaking, this method should be synchronized (though
     * currently, it's pretty hard to get two copies of it going at the same
     * time)
     */
    public void export(List<SQLTable> tablesToExport) throws SQLException, ArchitectException {
        logger.debug("Starting export of tables: "+tablesToExport);
        finished = false;
        hasStarted = true;
        Connection con = null; // repository connection
        Connection tCon = null; // target db connection
        try {
            currentDB = tablesToExport;

            // first, set the logWriter
            logWriter = new LogWriter(ArchitectSession.getInstance().getUserSettings().getETLUserSettings().getString(
                    ETLUserSettings.PROP_ETL_LOG_PATH, ""));

            SQLDatabase repository = new SQLDatabase(repositoryDataSource); // we
                                                                            // are
                                                                            // exporting
                                                                            // db
                                                                            // into
                                                                            // this

            con = repository.getConnection();
            try {
                defParam = new DefaultParameters(con);
            } catch (PLSchemaException p) {
                throw new ArchitectException("couldn't load default parameters", p);
            }

            SQLDatabase target = new SQLDatabase(targetDataSource);
            tCon = target.getConnection();
            exportResultList.add(new LabelValueBean("\n  Creating Power Loader Job", "\n"));
            sm = null;
            for (int tryNum = 0; tryNum < 3 && sm == null; tryNum++) {
                String username;
                if (tryNum == 1) {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID).toUpperCase();
                } else if (tryNum == 2) {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID).toLowerCase();
                } else {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID);
                }
                try {
                    // don't need to verify passwords in client apps (as opposed
                    // to webapps)
                    sm = new PLSecurityManager(con, username, repositoryDataSource.get(ArchitectDataSource.PL_PWD),
                            false);
                } catch (PLSecurityException se) {
                    logger.debug("Couldn't find pl user " + username, se);
                }
            }
            if (sm == null) {
                throw new ArchitectException("There is no entry for \""
                        + repositoryDataSource.get(ArchitectDataSource.PL_UID)
                        + "\" in the PL_USER table");
            }
            logWriter.info("Starting creation of job <" + jobId + "> at "
                    + new java.util.Date(System.currentTimeMillis()));
            logWriter.info("Connected to database: " + repositoryDataSource.toString());
            maybeInsertFolder(con);

            PLJob job = new PLJob(jobId);

            insertJob(con);
            insertFolderDetail(con, job.getObjectType(), job.getObjectName());

            // This will order the target tables so that the parent tables are
            // loaded before their children

            DepthFirstSearch targetDFS = new DepthFirstSearch(tablesToExport);
            List tables = targetDFS.getFinishOrder();

            if (logger.isDebugEnabled()) {
                StringBuffer tableOrder = new StringBuffer();
                Iterator dit = tables.iterator();
                while (dit.hasNext()) {
                    tableOrder.append(((SQLTable) dit.next()).getName()).append(", ");
                }
                logger.debug("Safe load order for job is: " + tableOrder);
            }

            int outputTableNum = 1;
            Hashtable inputTables = new Hashtable();

            Iterator targetTableIt = tables.iterator();
            while (targetTableIt.hasNext()) {
                tableCount++;
                SQLTable outputTable = (SQLTable) targetTableIt.next();
                // reset loop variables for each output table
                boolean createdOutputTableMetaData = false;
                int transNum = 0;
                int seqNum = 1;
                String transName = null;
                String outputTableId = null;
                String inputTableId = null;

                for (SQLColumn outputCol : outputTable.getColumns()) {
                    SQLColumn inputCol = outputCol.getSourceColumn();
                    if (inputCol != null && !inputTables.keySet().contains(inputCol.getParentTable())) {
                        // create transaction and input table meta data here if
                        // we need to
                        SQLTable inputTable = inputCol.getParentTable();
                        String baseTransName = PLUtils.toPLIdentifier("LOAD_" + outputTable.getName());
                        transNum = generateUniqueTransIdx(con, baseTransName);
                        transName = baseTransName + "_" + transNum;
                        logger.debug("transName: " + transName);
                        insertTrans(con, transName, outputTable.getRemarks());
                        insertFolderDetail(con, "TRANSACTION", transName);
                        insertTransExceptHandler(con, "A", transName, tCon); // error
                                                                                // handling
                                                                                // is
                                                                                // w.r.t.
                                                                                // target
                                                                                // database
                        insertTransExceptHandler(con, "U", transName, tCon); // error
                                                                                // handling
                                                                                // is
                                                                                // w.r.t.
                                                                                // target
                                                                                // database
                        insertTransExceptHandler(con, "D", transName, tCon); // error
                                                                                // handling
                                                                                // is
                                                                                // w.r.t.
                                                                                // target
                                                                                // database
                        insertJobDetail(con, outputTableNum * 10, "TRANSACTION", transName);
                        inputTableId = PLUtils.toPLIdentifier(inputTable.getName() + "_IN_" + transNum);
                        logger.debug("inputTableId: " + inputTableId);
                        insertTransTableFile(con, transName, inputTableId, inputTable, false, transNum);
                        inputTables.put(inputTable, new PLTransaction(transName, inputTableId, transNum));
                    } else {
                        // restore input/transaction variables
                        PLTransaction plt = (PLTransaction) inputTables.get(inputCol.getParentTable());
                        transName = plt.getName();
                        inputTableId = plt.getInputTableId();
                        transNum = plt.getTransNum();
                    }

                    if (!createdOutputTableMetaData) {
                        // create output table meta data once
                        logger.debug("outputTableNum: " + outputTableNum);
                        outputTableId = PLUtils.toPLIdentifier(outputTable.getName() + "_OUT_" + outputTableNum);
                        logger.debug("outputTableId: " + outputTableId);
                        insertTransTableFile(con, transName, outputTableId, outputTable, true, transNum);
                        createdOutputTableMetaData = true;
                    }

                    // finally, insert the mapping for this column
                    if (inputCol != null) {
                        // note: output proc seq num appears to be meaningless
                        // based on what the Power Loader
                        // does after you view generated transaction in the VB
                        // Front end.
                        insertTransColMap(con, transName, outputTableId, outputCol, inputTableId, seqNum * 10);
                    }
                    seqNum++;
                    outputTableNum++;
                }
            }
        } finally {
            hasStarted = false;
            finished = true;
            currentDB = null;
            // close and flush the logWriter (and set the reference to null)
            logWriter.flush();
            logWriter.close();
            logWriter = null;
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                logger.error("Couldn't close repository connection", e);
            }
            try {
                if (tCon != null)
                    tCon.close();
            } catch (SQLException e) {
                logger.error("Couldn't close target connection", e);
            }
        }
    }

    class PLTransaction {

        public PLTransaction(String name, String inputTableId, int transNum) {
            this.name = name;
            this.inputTableId = inputTableId;
            this.transNum = transNum;
        }

        private String name;

        private String inputTableId;

        private int transNum;

        public String getInputTableId() {
            return inputTableId;
        }

        public void setInputTableId(String inputTableId) {
            this.inputTableId = inputTableId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTransNum() {
            return transNum;
        }

        public void setTransNum(int transNum) {
            this.transNum = transNum;
        }

    }

    // --------------------------- UTILITY METHODS ------------------------
    protected String fixWindowsPath(String path) {
        if (path == null) {
            return "";
        }
        if (!path.endsWith("\\")) {
            path += "\\";
        }

        return path;
    }

    protected String fixUnixPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    /*
     * Do any platform dependent escaping of Strings here. For example, Postgres
     * backslashes need to be doubled or Postgres will mangle them.
     * 
     * FIXME: this needs to be pushed into the more generic SQL utility class in
     * ca.sqlpower.sql. All Strings must be washed through it. And then the
     * entire application suite needs to be regression tested.
     * 
     */
    protected String escapeString(Connection con, String string) {
        String retString = null;
        if (DBConnection.isPostgres(con)) {
            // compilation halves the number of slashes, and then regex
            // halves them once again. Confusing eh? 4==1...
            retString = string.replaceAll("\\\\", "\\\\\\\\");
        } else {
            retString = string;
        }
        return retString;
    }

    protected boolean isOracle(ArchitectDataSource dbcs) {
        if (dbcs.getDriverClass().toLowerCase().indexOf("oracledriver") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isSQLServer(ArchitectDataSource dbcs) {
        if (dbcs.getDriverClass().toLowerCase().indexOf("sqlserver") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isDB2(ArchitectDataSource dbcs) {
        if (dbcs.getDriverClass().toLowerCase().indexOf("db2") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isPostgres(ArchitectDataSource dbcs) {
        if (dbcs.getDriverClass().toLowerCase().indexOf("postgres") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static class PLJob implements DatabaseObject {

        public String jobId;

        public PLJob(String jobId) {
            this.jobId = jobId;
        }

        public String getObjectType() {
            return "JOB";
        }

        public String getObjectName() {
            return jobId;
        }
    }

    public static class PLTrans implements DatabaseObject {

        public String transId;

        public PLTrans(String transId) {
            this.transId = transId;
        }

        public String getObjectType() {
            return "TRANSACTION";
        }

        public String getObjectName() {
            return transId;
        }
    }

    // ----------------------- accessors and mutators --------------------------

    public void setJobId(String jobId) {
        this.jobId = PLUtils.toPLIdentifier(jobId);
    }

    public String getJobId() {
        return jobId;
    }

    public void setFolderName(String folderName) {
        this.folderName = PLUtils.toPLIdentifier(folderName);
    }

    public String getFolderName() {
        return folderName;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobComment(String jobComment) {
        this.jobComment = jobComment;
    }

    public String getJobComment() {
        return jobComment;
    }

    public void setRepositoryDataSource(ArchitectDataSource dbcs) {
        this.repositoryDataSource = dbcs;
    }

    public ArchitectDataSource getRepositoryDataSource() {
        return repositoryDataSource;
    }

    public boolean getRunPLEngine() {
        return runPLEngine;
    }

    public void setRunPLEngine(boolean runEngine) {
        runPLEngine = runEngine;
    }

    public void setPlSecurityManager(PLSecurityManager sm) {
        this.sm = sm;
    }

    public PLSecurityManager getPlSecurityManager() {
        return sm;
    }

    /**
     * @return Returns the targetDataSource.
     */
    public ArchitectDataSource getTargetDataSource() {
        return targetDataSource;
    }

    /**
     * @param targetDataSource
     *            The targetDataSource to set.
     */
    public void setTargetDataSource(ArchitectDataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String schema) {
        targetSchema = schema;
    }

    public void setTargetSchema(SQLSchema schema) {
        if (schema != null)
            targetSchema = schema.getName();
    }

    /**
     * @return Returns the targetCatalog.
     */
    public String getTargetCatalog() {
        return targetCatalog;
    }

    /**
     * @param targetCatalog
     *            The targetCatalog to set.
     */
    public void setTargetCatalog(String targetCatalog) {
        this.targetCatalog = targetCatalog;
    }

    public void setTargetCatalog(SQLCatalog targetCatalog) {
        if (targetCatalog != null)
            this.targetCatalog = targetCatalog.getName();
    }

    public String getRepositoryCatalog() {
        return repositoryCatalog;
    }

    public void setRepositoryCatalog(SQLCatalog repositoryCatalog) {
        if (repositoryCatalog != null)
            this.repositoryCatalog = repositoryCatalog.getName();
    }

    public void setRepositoryCatalog(String repositoryCatalog) {
        this.repositoryCatalog = repositoryCatalog;
    }

    public String getRepositorySchema() {
        return repositorySchema;
    }

    public void setRepositorySchema(SQLSchema repositorySchema) {
        if (repositorySchema != null)
            this.repositorySchema = repositorySchema.getName();
    }

    public void setRepositorySchema(String repositorySchema) {
        this.repositorySchema = repositorySchema;
    }

    public ArrayList<LabelValueBean> getExportResultList() {
        return exportResultList;
    }

    public void setExportResultList(ArrayList<LabelValueBean> exportResultList) {
        this.exportResultList = exportResultList;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    protected void println(PrintWriter out, int indent, String text) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.println(text);
    }

    public void exportXML(List<SQLTable> tablesToExport) throws IOException, ArchitectException, SQLException {

        PrintWriter out = null;
        final String encoding = "ISO-8859-1";
        int indent = 0;

        out = new PrintWriter(file);
        Connection con = null;

        try {
            SQLDatabase repository = new SQLDatabase(repositoryDataSource);
            con = repository.getConnection();
            currentDB = tablesToExport;

            try {
                defParam = new DefaultParameters(con);
            } catch (PLSchemaException p) {
                throw new ArchitectException("couldn't load default parameters", p);
            }

            sm = null;
            for (int tryNum = 0; tryNum < 3 && sm == null; tryNum++) {
                String username;
                if (tryNum == 1) {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID).toUpperCase();
                } else if (tryNum == 2) {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID).toLowerCase();
                } else {
                    username = repositoryDataSource.get(ArchitectDataSource.PL_UID);
                }
                try {
                    // don't need to verify passwords in client apps (as opposed
                    // to webapps)
                    sm = new PLSecurityManager(con, username, repositoryDataSource.get(ArchitectDataSource.PL_PWD),
                            false);
                } catch (PLSecurityException se) {
                    logger.debug("Couldn't find pl user " + username, se);
                }
            }
            if (sm == null) {
                throw new ArchitectException("Could not find login for: "
                        + repositoryDataSource.get(ArchitectDataSource.PL_UID));
            }

            println(out, indent, "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
            println(out, indent, "<EXPORT>");
            indent++;
            println(out, indent, "<SCHEMA_VERSION>5.0.22</SCHEMA_VERSION>");

            exportXMLJobs(out, indent, con);
            if (folderName != null) {
                exportXMLFolder(out, indent, folderName);
                exportXMLFolderDetail(out, indent, folderName, "JOB", jobId);
            }

            Hashtable inputTables = new Hashtable();
            Hashtable trans = new Hashtable();
            tableCount = 0;
            Iterator targetTableIt = currentDB.iterator();
            while (targetTableIt.hasNext()) {
                tableCount++;
                int outputTableNum = 1;
                SQLTable outputTable = (SQLTable) targetTableIt.next();
                String baseTransName = PLUtils.toPLIdentifier("LOAD_" + outputTable.getName());
                boolean createdOutputTableMetaData = false;
                Iterator cols = outputTable.getColumns().iterator();
                String transName;
                int transNum;
                String outputTableId = null;
                String inputTableId = null;
                int seqNum = 1;

                while (cols.hasNext()) {

                    SQLColumn outputCol = (SQLColumn) cols.next();
                    SQLColumn inputCol = outputCol.getSourceColumn();
                    if (inputCol != null && !inputTables.keySet().contains(inputCol.getParentTable())) {
                        // create transaction and input table meta data here if
                        // we need to
                        SQLTable inputTable = inputCol.getParentTable();

                        Set set;
                        if (!trans.keySet().contains(baseTransName)) {
                            set = getTransName(con, baseTransName);
                            trans.put(baseTransName, set);
                        } else {
                            set = (Set) trans.get(baseTransName);
                        }
                        transNum = generateUniqueTransIdx(set, baseTransName);
                        transName = baseTransName + "_" + transNum;
                        set.add(transName);

                        exportXMLJobsDetail(out, indent, con, transName, tableCount * 10);

                        exportXMLTrans(out, indent, con, transName, outputTable.getRemarks());

                        exportXMLTransException(out, indent, con, transName, "A");
                        exportXMLTransException(out, indent, con, transName, "U");
                        exportXMLTransException(out, indent, con, transName, "D");

                        inputTableId = PLUtils.toPLIdentifier(inputTable.getName() + "_IN_" + transNum);
                        inputTables.put(inputTable, new PLTransaction(transName, inputTableId, transNum));

                        exportXMLTransTableFile(out, indent, con, transName, inputTableId, inputTable, 10 * seqNum++,
                                false);
                        if (folderName != null) {
                            exportXMLFolder(out, indent, folderName);
                            exportXMLFolderDetail(out, indent, folderName, "TRANSACTION", transName);
                        }
                       
                    } else {
                        // restore input/transaction variables
                        PLTransaction plt = (PLTransaction) inputTables.get(inputCol.getParentTable());
                        transName = plt.getName();
                        inputTableId = plt.getInputTableId();
                        transNum = plt.getTransNum();
                    }

                    if (!createdOutputTableMetaData) {
                        outputTableId = PLUtils.toPLIdentifier(outputTable.getName() + "_OUT_" + outputTableNum);

                        exportXMLTransTableFile(out, indent, con, transName, outputTableId, outputTable, 10 * seqNum++,
                                true);
                        createdOutputTableMetaData = true;
                       
                    }

                    if (inputCol != null) {
                        exportXMLTransColMap(out, indent, con,
                                inputCol, transName, inputTableId,
                                outputTableId, seqNum);
                    }
                }
                outputTableNum++;

            }

            indent--;
            println(out, indent, "</EXPORT>");
        } finally {
            if (out != null)
                out.close();
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                logger.error("Couldn't close connection", e);
            }
            currentDB = null;
        }

    }

    public void exportXMLJobs(PrintWriter out, int indent, Connection con) {

        println(out, indent, "<PL_JOB>");
        indent++;
        println(out, indent, "<JOB_ID>" + jobId + "</JOB_ID>");
        if (jobDescription != null)
            println(out, indent, "<JOB_DESC>" + jobDescription + "</JOB_DESC>");
        println(out, indent, "<SHOW_PROGRESS_FREQ>100</SHOW_PROGRESS_FREQ>");
        println(out, indent, "<LOG_FILE_NAME>" + fixWindowsPath(defParam.get("default_log_file_path")) + jobId + ".log"
                + "</LOG_FILE_NAME>");
        println(out, indent, "<ERR_FILE_NAME>" + fixWindowsPath(defParam.get("default_err_file_path")) + jobId + ".err"
                + "</ERR_FILE_NAME>");
        println(out, indent, "<UNIX_LOG_FILE_NAME>" + fixUnixPath(defParam.get("default_log_file_path")) + jobId
                + ".log" + "</UNIX_LOG_FILE_NAME>");
        println(out, indent, "<UNIX_ERR_FILE_NAME>" + fixUnixPath(defParam.get("default_err_file_path")) + jobId
                + ".err" + "</UNIX_ERR_FILE_NAME>");
        println(out, indent, "<APPEND_TO_LOG_IND>" + "N" + "</APPEND_TO_LOG_IND>");
        println(out, indent, "<APPEND_TO_ERR_IND>" + "N" + "</APPEND_TO_ERR_IND>");

        println(out, indent, "<DEBUG_MODE_IND>" + "N" + "</DEBUG_MODE_IND>");
        println(out, indent, "<APPEND_TO_ERR_IND>" + "N" + "</APPEND_TO_ERR_IND>");
        println(out, indent, "<COMMIT_FREQ>" + "100" + "</COMMIT_FREQ>");
        if (jobComment != null)
            println(out, indent, "<JOB_COMMENT>" + jobComment + "</JOB_COMMENT>");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        println(out, indent, "<CREATE_DATE><DATE>" + df.format(new java.util.Date()) + "</DATE></CREATE_DATE>");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + "" + "</LAST_UPDATE_USER>");

        println(out, indent, "<SKIP_PACKAGES_IND>N</SKIP_PACKAGES_IND>");
        println(out, indent, "<SEND_EMAIL_IND>N</SEND_EMAIL_IND>");
        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");
        println(out, indent, "<STATS_IND>N</STATS_IND>");
        println(out, indent, "<ODBC_IND>N</ODBC_IND>");
        indent--;
        println(out, indent, "</PL_JOB>");
    }

    public void exportXMLJobsDetail(PrintWriter out, int indent, Connection con, String objName, int seqno)
            throws SQLException {

        println(out, indent, "<JOB_DETAIL>");
        indent++;
        println(out, indent, "<JOB_ID>" + jobId + "</JOB_ID>");
        println(out, indent, "<JOB_PROCESS_SEQ_NO>" + seqno + "</JOB_PROCESS_SEQ_NO>");
        println(out, indent, "<OBJECT_TYPE>TRANSACTION</OBJECT_TYPE>");
        println(out, indent, "<OBJECT_NAME>" + objName + "</OBJECT_NAME>");
        println(out, indent, "<JOB_DETAIL_COMMENT>Generated by POWER*Architect PLExport Revision: "
                + PL_GENERATOR_VERSION + "</JOB_DETAIL_COMMENT>");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + con.getMetaData().getUserName() + "</LAST_UPDATE_USER>");
        println(out, indent, "<FAILURE_ABORT_IND>N</FAILURE_ABORT_IND>");
        println(out, indent, "<WARNING_ABORT_IND>N</WARNING_ABORT_IND>");
        println(out, indent, "<ACTIVE_IND>Y</ACTIVE_IND>");
        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");
        indent--;
        println(out, indent, "</JOB_DETAIL>");

    }

    public void exportXMLFolder(PrintWriter out, int indent, String name) {

        println(out, indent, "<PL_FOLDER>");
        indent++;
        println(out, indent, "<FOLDER_NAME>" + name + "</FOLDER_NAME>");
        indent--;
        println(out, indent, "</PL_FOLDER>");
    }

    private void exportXMLFolderDetail(PrintWriter out, int indent, String name, Object objType, Object objName) {
        println(out, indent, "<PL_FOLDER_DETAIL>");
        indent++;
        println(out, indent, "<FOLDER_NAME>" + name + "</FOLDER_NAME>");
        println(out, indent, "<OBJECT_TYPE>" + objType + "</OBJECT_TYPE>");
        println(out, indent, "<OBJECT_NAME>" + objName + "</OBJECT_NAME>");
        indent--;
        println(out, indent, "</PL_FOLDER_DETAIL>");
    }

    private void exportXMLTrans(PrintWriter out, int indent, Connection con, String transId, String comment)
            throws SQLException {

        println(out, indent, "<TRANS>");
        indent++;
        println(out, indent, "<TRANS_ID>" + transId + "</TRANS_ID>");
        println(out, indent, "<TRANS_DESC>" + "Generated by Power*Architect " + PL_GENERATOR_VERSION + "</TRANS_DESC>");
        println(out, indent, "<TRANS_COMMENT>" + comment + "</TRANS_COMMENT>");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + con.getMetaData().getUserName() + "</LAST_UPDATE_USER>");
        println(out, indent, "<DEBUG_MODE_IND>N</DEBUG_MODE_IND>");
        println(out, indent, "<ERR_FILE_NAME>"
                + escapeString(con, fixWindowsPath(defParam.get("default_err_file_path"))) + transId + ".err"
                + "</ERR_FILE_NAME>");
        println(out, indent, "<LOG_FILE_NAME>"
                + escapeString(con, fixWindowsPath(defParam.get("default_log_file_path"))) + transId + ".err"
                + "</LOG_FILE_NAME>");
        println(out, indent, "<BAD_FILE_NAME>"
                + escapeString(con, fixWindowsPath(defParam.get("default_bad_file_path"))) + transId + ".err"
                + "</BAD_FILE_NAME>");
        println(out, indent, "<SKIP_CNT>0</SKIP_CNT>");
        println(out, indent, "<PROCESS_CNT>0</PROCESS_CNT>");
        println(out, indent, "<CREATE_DATE><DATE>" + SQL.escapeDate(con, new java.util.Date())
                + "</DATE></CREATE_DATE>");

        println(out, indent, "<UNIX_LOG_FILE_NAME>"
                + escapeString(con, fixUnixPath(defParam.get("default_unix_log_file_path"))) + transId + ".err"
                + "</UNIX_LOG_FILE_NAME>");
        println(out, indent, "<UNIX_ERR_FILE_NAME>"
                + escapeString(con, fixUnixPath(defParam.get("default_unix_err_file_path"))) + transId + ".err"
                + "</UNIX_ERR_FILE_NAME>");
        println(out, indent, "<UNIX_BAD_FILE_NAME>"
                + escapeString(con, fixUnixPath(defParam.get("default_unix_bad_file_path"))) + transId + ".err"
                + "</UNIX_BAD_FILE_NAME>");

        println(out, indent, "<SKIP_PACKAGES_IND>N</SKIP_PACKAGES_IND>");
        println(out, indent, "<SEND_EMAIL_IND>N</SEND_EMAIL_IND>");
        println(out, indent, "<TRANSACTION_TYPE>POWER_LOADER</TRANSACTION_TYPE>");
        println(out, indent, "<DELTA_SORT_IND>Y</DELTA_SORT_IND>");

        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");
        println(out, indent, "<STATS_IND>N</STATS_IND>");
        println(out, indent, "<ODBC_IND>Y</ODBC_IND>");

        indent--;
        println(out, indent, "</TRANS>");

    }

    private void exportXMLTransException(PrintWriter out, int indent, Connection con, String transId, String actionType)
            throws SQLException {

        String errorCode = "";
        String resultActionType;
        String databaseType = "";

        if (DBConnection.isOracle(con)) {
            databaseType = "ORACLE";
            if (actionType.equals("A")) {
                errorCode = "-1";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "1403";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "1403";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isSQLServer(con)) {
            databaseType = "SQL SERVER";
            if (actionType.equals("A")) {
                errorCode = "-2627";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isDB2(con)) {
            databaseType = "DB2";
            if (actionType.equals("A")) {
                errorCode = "-803";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else if (DBConnection.isPostgres(con)) {
            databaseType = "POSTGRES";
            if (actionType.equals("A")) {
                errorCode = "23505";
                resultActionType = "CHANGE_TO_UPD";
            } else if (actionType.equals("U")) {
                errorCode = "100";
                resultActionType = "CHANGE_TO_ADD";
            } else if (actionType.equals("D")) {
                errorCode = "100";
                resultActionType = "SKIP";
            } else {
                throw new IllegalArgumentException("Invalid Action type " + actionType);
            }
        } else {
            throw new IllegalArgumentException("Unsupported Target Database type");
        }

        println(out, indent, "<TRANS_EXCEPT_HANDLE>");
        indent++;
        println(out, indent, "<TRANS_ID>" + transId + "</TRANS_ID>");

        println(out, indent, "<database_type>" + databaseType + "</database_type>");
        println(out, indent, "<INPUT_ACTION_TYPE>" + actionType + "</INPUT_ACTION_TYPE>");
        println(out, indent, "<DBMS_ERROR_CODE>" + errorCode + "</DBMS_ERROR_CODE>");
        println(out, indent, "<RESULT_ACTION_TYPE>" + resultActionType + "</RESULT_ACTION_TYPE>");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + con.getMetaData().getUserName() + "</LAST_UPDATE_USER>");
        println(out, indent, "<ACTIVE_IND>Y</ACTIVE_IND>");
        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");

        indent--;
        println(out, indent, "</TRANS_EXCEPT_HANDLE>");
    }

    private void exportXMLTransTableFile(PrintWriter out,
                                int indent, Connection con,
                                String transId,
                                String inputTableId,
                                SQLTable table, int seqNo,
                                boolean isOutput) throws SQLException {

        println(out, indent, "<TRANS_TABLE_FILE>");
        indent++;
        println(out, indent, "<TRANS_ID>" + transId + "</TRANS_ID>");

        println(out, indent, "<TABLE_FILE_ID>" + inputTableId + "</TABLE_FILE_ID>");
        println(out, indent, "<TABLE_FILE_IND>TABLE</TABLE_FILE_IND>");
        println(out, indent, "<INPUT_OUTPUT_IND>" + (isOutput ? "O" : "I") + "</INPUT_OUTPUT_IND>");

        String type;
        String dbConnectName;
        ArchitectDataSource dataSource;
        SQLDatabase database = table.getParentDatabase();

        if (database != null) {
            dataSource = database.getDataSource(); // input table
            dbConnectName = dataSource.get(ArchitectDataSource.PL_LOGICAL);

            try {
                if (dataSource != null && isOracle(dataSource)) {
                    type = "ORACLE";
                } else if (dataSource != null && isSQLServer(dataSource)) {
                    type = "SQL SERVER";
                } else if (dataSource != null && isDB2(dataSource)) {
                    type = "DB2";
                } else if (dataSource != null && isPostgres(dataSource)) {
                    type = "POSTGRES";
                } else {
                    throw new IllegalArgumentException("Unsupported target database type");
                }
                println(out, indent, "<TABLE_FILE_TYPE>" + type + "</TABLE_FILE_TYPE>");
                println(out, indent, "<DB_CONNECT_NAME>" + dbConnectName + "</DB_CONNECT_NAME>");
            } catch (NullPointerException e) {

            }
        }

        println(out, indent, "<OWNER>" + table.getParent().toString() + "</OWNER>");
        println(out, indent, "<TABLE_FILE_NAME>" + table.getName() + "</TABLE_FILE_NAME>");
        println(out, indent, "<PROC_SEQ_NO>" + seqNo + "</PROC_SEQ_NO>");
        println(out, indent, "<TRANS_TABLE_FILE_COMMENT>" + "Generated by Power*Architect " + PL_GENERATOR_VERSION
                + "</TRANS_TABLE_FILE_COMMENT>");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + con.getMetaData().getUserName() + "</LAST_UPDATE_USER>");
        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");

        indent--;
        println(out, indent, "</TRANS_TABLE_FILE>");
    }

    private void exportXMLTransColMap(PrintWriter out, int indent, Connection con, SQLColumn outputColumn,
            String transId, String inputTableId, String outputTableId, int seqNo) throws SQLException {

        SQLColumn inputColumn = outputColumn.getSourceColumn();
        String inputColumnName;
        if (inputColumn != null) {
            inputColumnName = inputColumn.getName();
        } else {
            inputColumnName = null;
        }

        println(out, indent, "<TRANS_COL_MAP>");
        indent++;
        println(out, indent, "<TRANS_ID>" + transId + "</TRANS_ID>");
        println(out, indent, "<INPUT_TABLE_FILE_ID>" + inputTableId + "</INPUT_TABLE_FILE_ID>");
        println(out, indent, "<INPUT_TRANS_COL_NAME>" + inputColumnName + "</INPUT_TRANS_COL_NAME>");

        println(out, indent, "<OUTPUT_TABLE_FILE_ID>" + outputTableId + "</OUTPUT_TABLE_FILE_ID>");
        println(out, indent, "<OUTPUT_TRANS_COL_NAME>" + outputColumn.getName() + "</OUTPUT_TRANS_COL_NAME>");

        println(out, indent, "<VALID_ACTION_TYPE>" + (outputColumn.getPrimaryKeySeq() != null ? "A" : "AU")
                + "</VALID_ACTION_TYPE>");
        println(out, indent, "<NATURAL_ID_IND>" + (outputColumn.getPrimaryKeySeq() != null ? "Y" : "N")
                + "</NATURAL_ID_IND>");
        println(out, indent, "<OUTPUT_PROC_SEQ_NO>" + seqNo + "</OUTPUT_PROC_SEQ_NO>");

        println(out, indent, "<TRANS_COL_MAP_COMMENT>" + "Generated by Power*Architect " + PL_GENERATOR_VERSION
                + "</TRANS_COL_MAP_COMMENT>");
        println(out, indent, "<ACTIVE_IND>Y</ACTIVE_IND>");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        println(out, indent, "<LAST_UPDATE_DATE><DATE>" + df.format(new java.util.Date())
                + "</DATE></LAST_UPDATE_DATE>");
        println(out, indent, "<LAST_UPDATE_USER>" + con.getMetaData().getUserName() + "</LAST_UPDATE_USER>");
        println(out, indent, "<LAST_UPDATE_OS_USER>" + System.getProperty("user.name") + "</LAST_UPDATE_OS_USER>");

        indent--;
        println(out, indent, "</TRANS_COL_MAP>");
    }
}
