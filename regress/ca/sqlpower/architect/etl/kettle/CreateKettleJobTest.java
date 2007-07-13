/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.etl.kettle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.FileValidator;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.FileValidator.FileValidationResponse;

public class CreateKettleJobTest extends TestCase {

    private SQLDatabase target;
    private SQLTable targetTableNoSource;
    private SQLTable targetTableMixedSource;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        target = new SQLDatabase();
        target.setName("Target for Testing");
        ArchitectDataSource ds = new ArchitectDataSource();
        target.setDataSource(ds);
        ds.setName("Target Data Source for Testing");
        ds.setUser("Guest");
        ds.setPass("Guest");
        ArchitectDataSourceType dsType = ds.getParentType();
        dsType.setJdbcUrl("<Hostname>:<Port>:<Database>");
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        ds.setUrl("hostname:1234:database");
        
        SQLDatabase source = new SQLDatabase();
        source.setName("Source for Testing");
        ArchitectDataSource sourceDS = new ArchitectDataSource();
        source.setDataSource(sourceDS);
        sourceDS.setName("Source Data Source for Testing");
        sourceDS.setUser("Guest");
        sourceDS.setPass("Guest");
        ArchitectDataSourceType sourceDSType = sourceDS.getParentType();
        sourceDSType.setJdbcUrl("<Hostname>:<Port>:<Database>");
        sourceDSType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "oracle");
        sourceDS.setUrl("hostname:1234:database");
        
        SQLTable sourceTable1 = new SQLTable(source, true);
        sourceTable1.setName("sourceTable1");
        SQLColumn col1Source1 = new SQLColumn(sourceTable1, "Column1", 1, 10, 0);
        sourceTable1.addColumn(col1Source1);
        SQLColumn col2Source1 = new SQLColumn(sourceTable1, "Column2", 2, 10, 0);
        sourceTable1.addColumn(col2Source1);
        
        SQLTable sourceTable2 = new SQLTable(source, true);
        sourceTable2.setName("sourceTable2");
        SQLColumn col1Source2 = new SQLColumn(sourceTable2, "Column1", 1, 10, 0); 
        sourceTable2.addColumn(col1Source2);
        SQLColumn col2Source2 = new SQLColumn(sourceTable2, "Column2", 2, 10, 0);
        sourceTable2.addColumn(col2Source2);
            
        
        targetTableNoSource = new SQLTable(target, true);
        targetTableNoSource.setName("TargetTable1");
        targetTableNoSource.addColumn(new SQLColumn(targetTableNoSource, "Column1", 1, 10, 0));
        targetTableNoSource.addColumn(new SQLColumn(targetTableNoSource, "Column2", 2, 10, 0));
        
        targetTableMixedSource = new SQLTable(target, true);
        targetTableMixedSource.setName("TargetTable2");
        SQLColumn colFromCol1Source1 = new SQLColumn(targetTableMixedSource, "ColumnA", 1, 10, 0);
        colFromCol1Source1.setSourceColumn(col1Source1);
        targetTableMixedSource.addColumn(colFromCol1Source1);
        SQLColumn colFromCol2Source1 = new SQLColumn(targetTableMixedSource, "ColumnB", 2, 10, 0);
        colFromCol2Source1.setSourceColumn(col2Source1);
        targetTableMixedSource.addColumn(colFromCol2Source1);
        SQLColumn colFromCol1Source2 = new SQLColumn(targetTableMixedSource, "ColumnC", 1, 10, 0);
        colFromCol1Source2.setSourceColumn(col1Source2);
        targetTableMixedSource.addColumn(colFromCol1Source2);
        SQLColumn colFromCol2Source2 = new SQLColumn(targetTableMixedSource, "ColumnD", 2, 10, 0);
        colFromCol2Source2.setSourceColumn(col2Source2);
        targetTableMixedSource.addColumn(colFromCol2Source2);
        targetTableMixedSource.addColumn(new SQLColumn(targetTableMixedSource, "ColumnE", 1, 10, 0));
        targetTableMixedSource.addColumn(new SQLColumn(targetTableMixedSource, "ColumnF", 2, 10, 0));
    }
    
    public void testCreatingJobsWithTablesWithNoSource() throws ArchitectException, IOException, RuntimeException, KettleException, SQLException {
        new File("TestingJob.kjb").delete();
        new File("transformation_for_table_TargetTable1.ktr").delete();
        CreateKettleJob job = new CreateKettleJob();
        job.setJobName("Testing Job");
        job.setSchemaName("Schema");
        job.setFilePath("TestingJob");
        job.setKettleJoinType(0);
        List<SQLTable> tableList = new ArrayList<SQLTable>();
        tableList.add(targetTableNoSource);
        job.doExport(tableList, target);
        assertTrue(new File("TestingJob.kjb").exists());
        assertFalse(new File("transformation_for_table_TargetTable1.ktr").exists());
    }
    
    public void testCreatingJobsWithTablesWithSources() throws ArchitectException, IOException, RuntimeException, KettleException, SQLException {
        new File("TestingJob.kjb").delete();
        new File("transformation_for_table_TargetTable2.ktr").delete();
        CreateKettleJob job = new CreateKettleJob();
        job.setJobName("Testing Job");
        job.setSchemaName("Schema");
        File jobFile = File.createTempFile("TestingJob", null);
        job.setFilePath(jobFile.getPath());
        job.setKettleJoinType(0);
        List<SQLTable> tableList = new ArrayList<SQLTable>();
        tableList.add(targetTableMixedSource);
        job.doExport(tableList, target);
        assertTrue(new File(jobFile.getPath() + ".kjb").exists());
        assertFalse(new File(jobFile.getParentFile().getPath() + "transformation_for_table_TargetTable2.ktr").exists());
    }

    public void testAddDatabaseConnection() {
       Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();
       CreateKettleJob job = new CreateKettleJob();
       DatabaseMeta dbMeta = job.addDatabaseConnection(databaseNames, target.getDataSource());
       assertEquals(dbMeta.getName(), target.getDataSource().getName());
       assertTrue(databaseNames.containsKey(target.getDataSource().getName()));
       assertEquals(databaseNames.get(target.getDataSource().getName()), dbMeta);
       assertEquals(1, job.getTasksToDo().size());
    }
    
    public void testAddExistingDatabaseConnection() {
        Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();
        DatabaseMeta dbMetaBean = new DatabaseMeta();
        dbMetaBean.setName("Meta Bean");
        databaseNames.put(target.getDataSource().getName(), dbMetaBean);
        CreateKettleJob job = new CreateKettleJob();
        DatabaseMeta dbMeta = job.addDatabaseConnection(databaseNames, target.getDataSource());
        assertEquals(dbMeta.getName(), dbMetaBean.getName());
        assertTrue(databaseNames.containsKey(target.getDataSource().getName()));
        assertEquals(1, databaseNames.size());
        assertEquals(0, job.getTasksToDo().size());
    }
    
    public void testAddDatabaseConnectionThrowsRuntimeException() {
        Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();
        CreateKettleJob job = new CreateKettleJob();
        try {
            job.addDatabaseConnection(databaseNames, new ArchitectDataSource());
            fail("A runtime exception was not thrown when an invalid data source was passed in");
        } catch (RuntimeException re) {
            assertEquals(1, job.getTasksToDo().size());
        }
    }
    
    public void testOutputToXMLWriteNotOKAlways() throws IOException {
        outputToXMLTesting(FileValidationResponse.WRITE_NOT_OK_ALWAYS, true);
    }


    public void testOutputToXMLWriteOKAlways() throws IOException {
        outputToXMLTesting(FileValidationResponse.WRITE_OK_ALWAYS, false);
    }
    
    public void testOutputToXMLFileValidatorWriteOk() throws IOException {
        outputToXMLTesting(FileValidationResponse.WRITE_OK, false);
    }
    
    public void testOutputToXMLFileValidatorWriteNotOk() throws IOException {
        outputToXMLTesting(FileValidationResponse.WRITE_NOT_OK, true);
    }
    
    public void testOutputToXMLFileException() throws IOException {
        CreateKettleJob job = new CreateKettleJob();
        
        LogWriter lw = LogWriter.getInstance();
        List<TransMeta> transList = new ArrayList<TransMeta>();
        TransMeta newTransMeta = new TransMeta();
        newTransMeta.setName("tableName");
        newTransMeta.addNote(new NotePadMeta("new trans meta note", 0, 150, 125, 125));
        transList.add(newTransMeta);
        
        JobMeta newJob = new JobMeta(lw);
        newJob.setName("jobName");
        newJob.addNote(new NotePadMeta("new job note", 0, 150, 125, 125));
        
        File outputFile = File.createTempFile("garbage", ".gbg");
        job.setFilePath(outputFile.getPath() + File.separator + "garbage.gbg.again");
        try {
            job.outputToXML(transList, newJob);
            fail("This test was unsuccessful as it did not throw an IOException at " +
                    "the correct location");
        } catch (IOException e) {
            assertEquals(1, job.getTasksToDo().size());
        }
    }
    
    public void testCreateMergeJoinsNoInputs() {
        CreateKettleJob job = new CreateKettleJob();
        TransMeta transMeta = new TransMeta();
        List<StepMeta> mergeSteps = job.createMergeJoins(0, transMeta, new ArrayList<StepMeta>());
        assertEquals(0, mergeSteps.size());
        assertEquals(0, transMeta.nrSteps());
    }
    
    public void testCreateMergeJoinsOneInput() {
        CreateKettleJob job = new CreateKettleJob();
        TransMeta transMeta = new TransMeta();
        List<StepMeta> inputSteps = new ArrayList<StepMeta>();
        inputSteps.add(new StepMeta());
        List<StepMeta> mergeSteps = job.createMergeJoins(0, transMeta, inputSteps);
        assertEquals(0, mergeSteps.size());
        assertEquals(0, transMeta.nrSteps());
        assertEquals(0, transMeta.nrTransHops());
    }
    
    public void testCreateMergeJoinsTwoInputs() {
        CreateKettleJob job = new CreateKettleJob();
        TransMeta transMeta = new TransMeta();
        List<StepMeta> inputSteps = new ArrayList<StepMeta>();
        StepMeta input1 = new StepMeta();
        input1.setName("input1");
        inputSteps.add(input1);
        transMeta.addStep(input1);
        StepMeta input2 = new StepMeta();
        input2.setName("input2");
        inputSteps.add(input2);
        transMeta.addStep(input2);
        List<StepMeta> mergeSteps = job.createMergeJoins(0, transMeta, inputSteps);
        assertEquals(1, mergeSteps.size());
        assertEquals(3, transMeta.nrSteps());
        assertEquals(2, transMeta.nrTransHops());
    }
    
    public void testCreateMergeJoinsThreeInputs() {
        CreateKettleJob job = new CreateKettleJob();
        TransMeta transMeta = new TransMeta();
        List<StepMeta> inputSteps = new ArrayList<StepMeta>();
        StepMeta input1 = new StepMeta();
        input1.setName("input1");
        inputSteps.add(input1);
        transMeta.addStep(input1);
        StepMeta input2 = new StepMeta();
        input2.setName("input2");
        inputSteps.add(input2);
        transMeta.addStep(input2);
        List<StepMeta> mergeSteps = job.createMergeJoins(0, transMeta, inputSteps);
        assertEquals(1, mergeSteps.size());
        assertEquals(3, transMeta.nrSteps());
        assertEquals(2, transMeta.nrTransHops());
    }
    
    public void testCreateRepository() {
        CreateKettleJob job = new CreateKettleJob();
        ArchitectDataSource architectDS = target.getDataSource();
        job.setRepository(architectDS);
        Repository rep = job.createRepository();
        DatabaseMeta dbMeta = rep.getDatabase().getDatabaseMeta();
        assertEquals(architectDS.getName(), dbMeta.getName());
        assertEquals(architectDS.getPass(), dbMeta.getPassword());
        assertEquals(architectDS.getUser(), dbMeta.getUsername());
    }
    
    public void testOutputToRepositoryOverwrite() throws SQLException, KettleException {
        testOutputToRepository(FileValidationResponse.WRITE_OK);
    }
    
    public void testOutputToRepositoryOverwriteAlways() throws SQLException, KettleException {
        testOutputToRepository(FileValidationResponse.WRITE_OK_ALWAYS);
    }
    
    public void testOutputToRepositoryDontOverwrite() throws SQLException, KettleException {
        testOutputToRepository(FileValidationResponse.WRITE_NOT_OK);
    }
    
    public void testOutputToRepositoryDontOverwriteAlways() throws SQLException, KettleException {
        testOutputToRepository(FileValidationResponse.WRITE_NOT_OK_ALWAYS);
    }
    
    private void testOutputToRepository(final FileValidationResponse fvr) throws SQLException, KettleException{
        TransMeta transMeta = createTransMeta();
        JobMeta job = createJobMeta();
        
        List<TransMeta> transList = new ArrayList<TransMeta>();
        transList.add(transMeta);

        CreateKettleJob kettleJob = new CreateKettleJob(new FileValidator(){
            public FileValidationResponse acceptFile(String name, String path) {
                return fvr;
            }
        }, new KettleRepositoryDirectoryChooser(){
            public RepositoryDirectory selectDirectory(Repository repo) {
                return new RepositoryDirectory();
            }
        });
        kettleJob.setRepository(new ArchitectDataSourceStub());
        kettleJob.setSavingToFile(false);
        kettleJob.setJobName("jobName");
        KettleRepositoryStub rep = new KettleRepositoryStub(new RepositoryMeta("", "", null));
        kettleJob.outputToRepository(job, transList, rep);

        if (fvr == FileValidationResponse.WRITE_NOT_OK || fvr == FileValidationResponse.WRITE_NOT_OK_ALWAYS) {
            assertEquals(0, rep.getNumTransformationsSaved());
            assertEquals(0, rep.getNumJobsSaved());
        } else {
            assertEquals(1, rep.getNumTransformationsSaved());
            assertEquals(1, rep.getNumJobsSaved());
        }
        assertTrue(rep.getRepositoryDisconnected());
    }
    
    /**
     * This method tests the outputToXML method based on different settings.
     * @param fvr The FileValidationResponse that will always be chosen
     * @param checkOriginalXML If true then we compare the final file with the
     * original xml. If false we compare the final file with the xml output from
     * the outputToXML file.
     * @throws IOException
     */
    private void outputToXMLTesting(final FileValidationResponse fvr, boolean checkOriginalXML) throws IOException {
        TransMeta transMeta = createTransMeta();
        JobMeta job = createJobMeta();
        
        File jobOutputFile = File.createTempFile("HelperFile", ".kjb");
        File transOutputFile = getTransOutputXMLFile(jobOutputFile, transMeta.getName());
        
        transOutputFile.delete();
        transOutputFile.createNewFile();
        createOutputXMLFile(transOutputFile, transMeta.getXML());
        createOutputXMLFile(jobOutputFile, job.getXML());
        
        List<TransMeta> transList = new ArrayList<TransMeta>();
        TransMeta newTransMeta = new TransMeta();
        newTransMeta.setName("tableName");
        newTransMeta.addNote(new NotePadMeta("new trans meta note", 0, 150, 125, 125));
        transList.add(newTransMeta);
        
        LogWriter lw = LogWriter.getInstance();
        JobMeta newJob = new JobMeta(lw);
        newJob.setName("jobName");
        newJob.addNote(new NotePadMeta("new job note", 0, 150, 125, 125));
        
        CreateKettleJob kettleJob = new CreateKettleJob(new FileValidator(){
            public FileValidationResponse acceptFile(String name, String path) {
                return fvr;
            }
        }, new RootRepositoryDirectoryChooser());
        kettleJob.setFilePath(jobOutputFile.getPath());
        kettleJob.setSavingToFile(true);
        kettleJob.setJobName("jobName");
        kettleJob.outputToXML(transList, newJob);
        
        if (checkOriginalXML) {
            assertEquals(transMeta.getXML(), readOutputXMLFile(transOutputFile));
            assertEquals(job.getXML(), readOutputXMLFile(jobOutputFile));
        } else {
            assertEquals(newTransMeta.getXML().replaceAll("date.*/.*date", ""),
                    readOutputXMLFile(transOutputFile).replaceAll("date.*/.*date", ""));
            assertEquals(newJob.getXML().replaceAll("date.*/.*date", ""),
                    readOutputXMLFile(jobOutputFile).replaceAll("date.*/.*date", ""));
        }
        transOutputFile.delete();
    }
    
    private JobMeta createJobMeta() {
        LogWriter lw = LogWriter.getInstance();
        JobMeta job = new JobMeta(lw);
        job.setName("jobName");
        job.addNote(new NotePadMeta("original job note", 0, 150, 125, 125));
        return job;
    }

    private TransMeta createTransMeta() {
        TransMeta transMeta = new TransMeta();
        transMeta.setName("tableName");
        transMeta.addNote(new NotePadMeta("original trans meta note", 0, 150, 125, 125));
        return transMeta;
    }
    
    private void createOutputXMLFile(File outputFile, String xml) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                FileOutputStream(outputFile), "utf-8"));
        out.write(xml);
        out.flush();
        out.close();
    }
    
    private String readOutputXMLFile(File outputFile) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new 
                FileInputStream(outputFile), "utf-8"));
        StringBuffer buffer = new StringBuffer();
        String inputXML = in.readLine();
        while (inputXML != null) {
            buffer.append(inputXML).append(System.getProperty("line.separator"));
            inputXML = in.readLine();
        }
        in.close();
        return buffer.toString();
    }
    
    private File getTransOutputXMLFile(File outputFile, String name) throws IOException {
        return new File(outputFile.getParentFile().getPath() + File.separator + 
                "transformation_for_table_" + name + ".ktr");
    }
    
    /**
     * This is a stub of the Repository class from Kettle. This was made to have a 
     * stub repository to give to the outputToRepository method but there are too 
     * many methods and not all of them are stubbed. This means when you create a 
     * transformation to output to the repository you may not be able to give a 
     * complex transformation. Jobs should be fine to make complex.
     */
    private class KettleRepositoryStub extends Repository {
        
        KettleDatabaseStub db;
        boolean repositoryDisconnected = false;
        int transformationsSaved = 0;
        int jobsSaved = 0;
        
        public KettleRepositoryStub(RepositoryMeta repMeta) {
            super(null, repMeta, null);
            db = new KettleDatabaseStub();
        }
        
        public KettleDatabaseStub getDatabase() {
            return db;
        }
        
        /**
         * This method does not actually disconnect from the connection!
         */
        public void disconnect() {
            repositoryDisconnected = true;
        }
        
        public boolean getRepositoryDisconnected() {
            return repositoryDisconnected;
        }
        
        public int getNumTransformationsSaved() {
            return transformationsSaved;
        }
        
        public int getNumJobsSaved() {
            return jobsSaved;
        }
        
        public void refreshRepositoryDirectoryTree() {
        }
        
        public long getTransformationID(String s, long l) {
            return getValFromString(s);
        }
        
        public long getNextTransformationID() {
            return 1;
        }
        
        public synchronized void delAllFromTrans(long id) {
        }

        public void lockRepository() {
        }

        public synchronized long insertLogEntry(String s) {
            return 0;
        }
            
        public long getJobID(String s, RepositoryDirectory d) {
            return getValFromString(s);
        }
        
        public long getNextJobID() {
            return 1;
        }
        
        public synchronized void delAllFromJob(long id) {
        }
        
        public void insertJobNote(long l1, long l2) {
        }
        
        public synchronized void insertJob(long id_job, long id_directory, String name, long id_database_log, String table_name_log,
                String modified_user, Date modified_date, boolean useBatchId, boolean batchIdPassed, boolean logfieldUsed, 
                String sharedObjectsFile, String description, String extended_description, String version, int status,
                String created_user, Date created_date) throws KettleException {
            jobsSaved++;
        }
        
        public synchronized long insertJobEntryCopy(long id_job, long id_jobentry, long id_jobentry_type, int nr, long gui_location_x,
                long gui_location_y, boolean gui_draw, boolean parallel) throws KettleException {
            return 1;
        }
        
        public synchronized long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled,
                boolean evaluation, boolean unconditional) throws KettleException {
            return 1;
        }
        
        public synchronized long insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width, 
                long gui_location_height) throws KettleException {
            return 1;
        }
        
        public synchronized long insertDatabase(String name, String type, String access, String host, String dbname, String port,
                String user, String pass, String servername, String data_tablespace, String index_tablespace)
        throws KettleDatabaseException {
            return 1;
        }
        
        public synchronized void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
                String port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
                throws KettleDatabaseException {
        }
        
        public synchronized void delDatabaseAttributes(long id_database) throws KettleDatabaseException {
        }
        
        public synchronized long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleDatabaseException {
            return 1;
        }
        
        public synchronized void updateJobEntryTypes() throws KettleException {
        }
        
        public long getJobEntryTypeID(String s) {
            return getValFromString(s);
        }
        
        public void commit() {
        }
        
        public void rollback() {
        }
        
        public void unlockRepository() {
        }
        
        public void clearNextIDCounters() {
        }
        
        public synchronized void updateStepTypes() throws KettleException {
        }
        
        public synchronized void closeStepAttributeInsertPreparedStatement() throws KettleDatabaseException {
        }
        
        public long getDatabaseID(String s) {
            return getValFromString(s);
        }
        
        public synchronized void insertTransformation(TransMeta transMeta) throws KettleDatabaseException {
            transformationsSaved++;
        }
        
        public synchronized void closeTransAttributeInsertPreparedStatement() throws KettleDatabaseException {
        }
        
        public synchronized void insertTransNote(long id_transformation, long id_note) throws KettleException {
        }
        
        public synchronized long getJobID(String name, long id_directory) throws KettleException {
            return 1;
        }
        
        private long getValFromString(String s) {
            try {
                return new Integer(s).intValue();
            } catch (NumberFormatException e) {
                return 1;
            }
        }

    }
    
    private class KettleDatabaseStub extends Database {
        
        public KettleDatabaseStub() {
            super(null);
        }
        
        public void setConnection(Connection conn) {
        }
    }
    
    private class ArchitectDataSourceStub extends ArchitectDataSource {
        
        public Connection createConnection() {
            return null;
        }
    }
    
}
