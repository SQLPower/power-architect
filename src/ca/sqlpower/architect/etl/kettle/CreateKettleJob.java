package ca.sqlpower.architect.etl.kettle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.mergejoin.MergeJoinMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;
import ca.sqlpower.architect.AlwaysAcceptFileValidator;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.FileValidator;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.FileValidator.FileValidationResponse;
import ca.sqlpower.architect.ddl.DDLUtils;

/**
 * This class stores the settings for creating a new Kettle job 
 */
public class CreateKettleJob {
    
    private static final Logger logger = Logger.getLogger(CreateKettleJob.class);
    
    /**
     * The spacing between nodes in Kettle jobs and transformations
     */
    private static int spacing = 150;
    
    /**
     * The name of the Kettle job
     */
    private String jobName;
    
    /**
     * The name of the target schema
     */
    private String schemaName;
    
    /**
     * The default join type for Kettle. The join types are stored as int as the values
     * are in an array in Kettle.
     */
    private int kettleJoinType;
    
    /**
     * The path to store the Kettle job at
     */
    private String filePath;
    
    /**
     * The file that represents the directory of the new Kettle job. This is set to 
     * prevent null pointer exceptions when first opening the Create Kettle Job window.
     */
    private File parentFile = new File("");
    
    /**
     * A list of tasks that an administrator or tech will have to do to the Kettle
     * job to make it run correctly.
     */
    private List<String> tasksToDo;
    
    /**
     * The file overwrite option to store if we should always overwrite or never
     * overwrite.
     */
    FileValidationResponse overwriteOption;
    
    /**
     * The file validator allows the selection for overwriting a file or not when saving
     * the XML.
     */
    FileValidator fileValidator;
    
    public CreateKettleJob(FileValidator validator) {
        this();
        fileValidator = validator;
    }
    
    public CreateKettleJob() {
        super();
        tasksToDo = new ArrayList<String>();
        fileValidator = new AlwaysAcceptFileValidator();
    }
    
    public void doExport(List<SQLTable> tableList, SQLDatabase targetDB ) throws ArchitectException, RuntimeException, IOException {
        
        //If the overwrite option is set to WRITE_OK_ALWAYS, or WRITE_NOT_OK_ALWAYS then
        //it will always be that way for every other job creation unless we reset it here
        overwriteOption = FileValidationResponse.WRITE_OK;
        
        EnvUtil.environmentInit();
        LogWriter lw = LogWriter.getInstance();
        JobMeta jm = new JobMeta(lw);
        
        Map<SQLTable, StringBuffer> tableMapping;
        List<TransMeta> transformations = new ArrayList<TransMeta>();
        List<String> noTransTables = new ArrayList<String>();
        tasksToDo = new ArrayList<String>();


        // The depth-first search will do a topological sort of
        // the target table graph, so parent tables will come before
        // their children.
        tableList = new DepthFirstSearch(tableList).getFinishOrder();

        Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();

        for (SQLTable table: tableList) {

            TransMeta transMeta = new TransMeta();
            transMeta.setName(table.getName());
            tableMapping = new LinkedHashMap<SQLTable, StringBuffer>();

            ArchitectDataSource target = targetDB.getDataSource();
            DatabaseMeta targetDatabaseMeta = addDatabaseConnection(databaseNames, target);
            transMeta.addDatabase(targetDatabaseMeta);

            List<SQLColumn> columnList = table.getColumns();
            List<String> noMappingForColumn = new ArrayList<String>();
            List<StepMeta> inputSteps = new ArrayList<StepMeta>();

            for (SQLColumn column: columnList) {
                SQLTable sourceTable;
                String sourceColumn;
                if (column.getSourceColumn() == null) {
                    // if we have no source table then we will get nulls as 
                    // placeholders from the target table.
                    sourceTable = table;
                    sourceColumn = "null";
                    noMappingForColumn.add(column.getName());
                } else {
                    sourceTable = column.getSourceColumn().getParentTable();
                    sourceColumn = column.getSourceColumn().getName();
                }
                if (!tableMapping.containsKey(sourceTable)) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("SELECT ");
                    buffer.append(sourceColumn);
                    buffer.append(" AS ").append(column.getName());
                    tableMapping.put(sourceTable, buffer);
                } else {
                    tableMapping.get(sourceTable).append(", ").append
                    (sourceColumn).append(" AS ").append(column.getName());
                }
            }

            if (tableMapping.containsKey(table)) {
                if (tableMapping.size() == 1) {
                    noTransTables.add(table.getName());
                    tasksToDo.add("Update table " + table.getName() + " as no source data was found");
                    continue;
                } else {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("There is no source for the column(s): ");
                    for (String noMapForCol: noMappingForColumn) {
                        buffer.append(noMapForCol).append(" ");
                    }
                    tasksToDo.add(buffer.toString() + " for the table " + table.getName());
                    transMeta.addNote(new NotePadMeta(buffer.toString(), 0, 150, 125, 125));
                }
            }

            for (SQLTable sourceTable: tableMapping.keySet()) {
                StringBuffer buffer = tableMapping.get(sourceTable);
                buffer.append(" FROM " + DDLUtils.toQualifiedName(sourceTable));
            }

            for (SQLTable sourceTable: tableMapping.keySet()) {
                ArchitectDataSource source = sourceTable.getParentDatabase().getDataSource();
                DatabaseMeta databaseMeta = addDatabaseConnection(databaseNames, source);
                transMeta.addDatabase(databaseMeta);

                TableInputMeta tableInputMeta = new TableInputMeta();
                String stepName = databaseMeta.getName() + ":" + DDLUtils.toQualifiedName(sourceTable);
                StepMeta stepMeta = new StepMeta("TableInput", stepName, tableInputMeta);
                stepMeta.setDraw(true);
                stepMeta.setLocation(inputSteps.size()==0?spacing:(inputSteps.size())*spacing, (inputSteps.size()+1)*spacing);
                tableInputMeta.setDatabaseMeta(databaseMeta);
                tableInputMeta.setSQL(tableMapping.get(sourceTable).toString());
                transMeta.addStep(stepMeta);
                inputSteps.add(stepMeta);
            }

            List<StepMeta> mergeSteps;
            mergeSteps = createMergeJoins(kettleJoinType, transMeta, inputSteps);

            TableOutputMeta tableOutputMeta = new TableOutputMeta();
            tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
            tableOutputMeta.setTablename(table.getName());
            tableOutputMeta.setSchemaName(schemaName);
            StepMeta stepMeta = new StepMeta("TableOutput", "Output to " + table.getName(), tableOutputMeta);
            stepMeta.setDraw(true);
            stepMeta.setLocation((inputSteps.size()+1)*spacing, inputSteps.size()*spacing);
            transMeta.addStep(stepMeta);
            TransHopMeta transHopMeta = 
                new TransHopMeta(mergeSteps.isEmpty()?inputSteps.get(0):mergeSteps.get(mergeSteps.size()-1), stepMeta);
            if (!mergeSteps.isEmpty()) {
                transMeta.addNote(new NotePadMeta("The final hop is disabled because the join types may need to be updated.",0,0,125,125));
                tasksToDo.add("Enable the final hop in " + transMeta.getName() + " after correcting the merge joins.");
                transHopMeta.setEnabled(false);
            }
            transMeta.addTransHop(transHopMeta);

            transformations.add(transMeta);

            logger.debug("Parent file path is " + parentFile.getPath());
            File file = new File(parentFile.getPath() + File.separator + 
                    "transformation_for_table_" + table.getName() + ".KTR");
            transMeta.setFilename(file.getName());
            outputToXML(transMeta.getXML(), file);
            if (overwriteOption == FileValidationResponse.CANCEL) {
                return;
            }
            logger.debug("Saved transformation to file: " + file.getName());
        }
        
        if (!noTransTables.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Transformations were not created for ");
            for (String tableName : noTransTables) {
                buffer.append(tableName).append(" ");
            }
            buffer.append(" as the tables had no source information.");
            jm.addNote(new NotePadMeta(buffer.toString(), 0, 0, 125, 125));
        }
        
        JobEntryCopy startEntry = new JobEntryCopy();
        JobEntrySpecial start = new JobEntrySpecial();
        start.setName("Start");
        start.setType(JobEntryInterface.TYPE_JOBENTRY_SPECIAL);
        start.setStart(true);
        startEntry.setEntry(start);
        startEntry.setLocation(10, spacing);
        startEntry.setDrawn();
        jm.addJobEntry(startEntry);
        
        JobEntryCopy oldJobEntry = null;
        int i = 1;
        for (TransMeta transformation : transformations) {
            JobEntryCopy entry = new JobEntryCopy();
            JobEntryTrans trans = new JobEntryTrans();
            trans.setFileName(transformation.getFilename());
            trans.setName(transformation.getName());
            trans.setType(JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION);
            entry.setEntry(trans);
            entry.setLocation(i*spacing, spacing);
            entry.setDrawn();
            jm.addJobEntry(entry);
            if (oldJobEntry != null) {
                JobHopMeta hop = new JobHopMeta(oldJobEntry, entry);
                jm.addJobHop(hop);
            } else {
                JobHopMeta hop = new JobHopMeta(startEntry, entry);
                jm.addJobHop(hop);
            }
            oldJobEntry = entry;
            i++;
        }
        
        String fileName = filePath;
        if (!fileName.toUpperCase().endsWith(".KJB")) {
            fileName += ".KJB";
        }
        jm.setName(jobName);
        jm.setFilename(fileName);
        outputToXML(jm.getXML(), new File(fileName));
        if (overwriteOption == FileValidationResponse.CANCEL) {
            return;
        }
    }
    
    private DatabaseMeta addDatabaseConnection(Map<String, DatabaseMeta> databaseNames, ArchitectDataSource dataSource) throws RuntimeException {
        DatabaseMeta databaseMeta;
        if (!databaseNames.containsKey(dataSource.getName())) {
            try {
                databaseMeta = KettleUtils.createDatabaseMeta(dataSource);
                try {
                    KettleOptions.testKettleDBConnection(databaseMeta);
                } catch (KettleDatabaseException e) {
                    logger.info("Could not connect to the database " + dataSource.getName() + ".");
                    e.printStackTrace();
                    tasksToDo.add("Check that the database " + dataSource.getName() + " can be connected to.");
                }
            } catch (RuntimeException re) {
                String databaseName = dataSource.getName();
                logger.error("Could not create the database connection for " + databaseName + ".");
                re.printStackTrace();
                tasksToDo.clear();
                tasksToDo.add("The Kettle job was not created as the database connection for " + databaseName + " could not be created.");
                throw re;
            }
            databaseNames.put(dataSource.getName(), databaseMeta);
        } else {
            databaseMeta = databaseNames.get(dataSource.getName());
        }
        return databaseMeta;
    }
    
    private void outputToXML(String xml, File f) throws IOException {
        try {
            if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS) {
                return;
            }
            logger.debug("The file to output is " + f.getPath());
            if (f.exists()) {
                if (overwriteOption == FileValidationResponse.WRITE_OK_ALWAYS) {
                    f.delete();
                } else {
                        overwriteOption = fileValidator.acceptFile(f);
                    if (overwriteOption == FileValidationResponse.WRITE_OK ||
                            overwriteOption == FileValidationResponse.WRITE_OK_ALWAYS) {
                        f.delete();
                    } else {
                        return;
                    }
                }
            }
            f.createNewFile();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                                        FileOutputStream(f), "utf-8"));
            out.write(xml);
            out.flush();
            out.close();
        } catch (IOException er) {
            tasksToDo.clear();
            tasksToDo.add("File " + f.getName() + " was not created");
            throw er;
        }
    }

    /**
     * This creates all of the MergeJoin kettle steps as well as their hops from
     * the steps in the inputSteps list. The MergeJoin steps are also put into the 
     * TransMeta.
     */
    private List<StepMeta> createMergeJoins(int defaultJoinType, TransMeta transMeta, List<StepMeta> inputSteps) {
        List<StepMeta> mergeSteps = new ArrayList<StepMeta>();
        if (inputSteps.size() > 1) {
            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[defaultJoinType]);
            mergeJoinMeta.setStepName1(inputSteps.get(0).getName());
            mergeJoinMeta.setStepMeta1(inputSteps.get(0));
            mergeJoinMeta.setStepName2(inputSteps.get(1).getName());
            mergeJoinMeta.setStepMeta2(inputSteps.get(1));
            mergeJoinMeta.setKeyFields1(new String[]{});
            mergeJoinMeta.setKeyFields2(new String[]{});
            StepMeta stepMeta = new StepMeta("MergeJoin", "Join tables " +
                    inputSteps.get(0).getName() + " and " + 
                    inputSteps.get(1).getName(), mergeJoinMeta);
            stepMeta.setDraw(true);
            stepMeta.setLocation(2*spacing, new Double(1.5*spacing).intValue());
            transMeta.addStep(stepMeta);
            mergeSteps.add(stepMeta);
            TransHopMeta transHopMeta = new TransHopMeta(inputSteps.get(0), stepMeta);
            transMeta.addTransHop(transHopMeta);
            transHopMeta = new TransHopMeta(inputSteps.get(1), stepMeta);
            transMeta.addTransHop(transHopMeta);
            tasksToDo.add("Verify the merge join " + stepMeta.getName() + " does the correct merge.");
        }

        for (int i = 0; i < inputSteps.size()-2; i++) {
            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[defaultJoinType]);
            mergeJoinMeta.setStepName1(mergeSteps.get(i).getName());
            mergeJoinMeta.setStepMeta1(mergeSteps.get(i));
            mergeJoinMeta.setStepName2(inputSteps.get(i+2).getName());
            mergeJoinMeta.setStepMeta2(inputSteps.get(i+2));
            mergeJoinMeta.setKeyFields1(new String[]{});
            mergeJoinMeta.setKeyFields2(new String[]{});
            StepMeta stepMeta = new StepMeta("MergeJoin", "Join table " + inputSteps.get(i+2).getName(), mergeJoinMeta);
            stepMeta.setDraw(true);
            stepMeta.setLocation((i + 3) * spacing, new Double((i + 2.25) * spacing).intValue());
            transMeta.addStep(stepMeta);
            mergeSteps.add(stepMeta);
            TransHopMeta transHopMeta = new TransHopMeta(mergeSteps.get(i), stepMeta);
            transMeta.addTransHop(transHopMeta);
            transHopMeta = new TransHopMeta(inputSteps.get(i+2), stepMeta);
            transMeta.addTransHop(transHopMeta);
            tasksToDo.add("Verify the merge join " + stepMeta.getName() + " does the correct merge.");
        }
        return mergeSteps;
    }
    
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public int getKettleJoinType() {
        return kettleJoinType;
    }
    public void setKettleJoinType(int kettleJoinType) {
        this.kettleJoinType = kettleJoinType;
    }
    public String getSchemaName() {
        return schemaName;
    }
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    public File getParentFile() {
        return parentFile;
    }
    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
    }

    public List<String> getTasksToDo() {
        return tasksToDo;
    }

    public void setFileValidator(FileValidator fileValidator) {
        this.fileValidator = fileValidator;
    }
}
