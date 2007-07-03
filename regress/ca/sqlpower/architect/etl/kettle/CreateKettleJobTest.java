package ca.sqlpower.architect.etl.kettle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
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
    
    public void testCreatingJobsWithTablesWithNoSource() throws ArchitectException, IOException, RuntimeException, KettleException {
        new File("TestingJob.KJB").delete();
        new File("transformation_for_table_TargetTable1.KTR").delete();
        CreateKettleJob job = new CreateKettleJob();
        job.setJobName("Testing Job");
        job.setSchemaName("Schema");
        job.setFilePath("TestingJob");
        job.setKettleJoinType(0);
        List<SQLTable> tableList = new ArrayList<SQLTable>();
        tableList.add(targetTableNoSource);
        job.doExport(tableList, target);
        assertTrue(new File("TestingJob.KJB").exists());
        assertFalse(new File("transformation_for_table_TargetTable1.KTR").exists());
    }
    
    public void testCreatingJobsWithTablesWithSources() throws ArchitectException, IOException, RuntimeException, KettleException {
        new File("TestingJob.KJB").delete();
        new File("transformation_for_table_TargetTable2.KTR").delete();
        CreateKettleJob job = new CreateKettleJob();
        job.setJobName("Testing Job");
        job.setSchemaName("Schema");
        File jobFile = File.createTempFile("TestingJob", null);
        job.setFilePath(jobFile.getPath());
        job.setKettleJoinType(0);
        List<SQLTable> tableList = new ArrayList<SQLTable>();
        tableList.add(targetTableMixedSource);
        job.doExport(tableList, target);
        assertTrue(new File(jobFile.getPath() + ".KJB").exists());
        assertFalse(new File(jobFile.getParentFile().getPath() + "transformation_for_table_TargetTable2.KTR").exists());
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
    
    //TODO test outputToRepository
    
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
        
        File jobOutputFile = File.createTempFile("HelperFile", ".KJB");
        System.out.println(jobOutputFile.getPath());
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
                "transformation_for_table_" + name + ".KTR");
    }
    
}
