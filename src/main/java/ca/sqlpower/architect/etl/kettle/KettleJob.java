/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.etl.kettle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateMeta;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.PostgresDDLGenerator;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.ColumnMapping;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

/**
 * This class stores the settings for creating Kettle jobs. This class also creates
 * Kettle jobs based on a given session's play pen. The jobs and transformations created
 * by this class can be stored in either a file or repository.
 */
public class KettleJob implements Monitorable {
    
    private static final Logger logger = Logger.getLogger(KettleJob.class);
    
    /**
     * The spacing between nodes in Kettle jobs and transformations
     */
    private static int spacing = 150;
    
    /**
     * A list of tasks that an administrator or tech will have to do to the Kettle
     * job to make it run correctly.
     */
    private List<String> tasksToDo;
    
    /**
     * This is the monitor implementation for this class. This is used to handle the progress bar
     * on the action window.
     */
    private MonitorableImpl monitor;
    
    
    /**
     * The repository directory chooser that will select, or allow the user to select, the directory to save
     * to.
     */
    private KettleRepositoryDirectoryChooser dirChooser;

    /**
     * The session this job belongs to.
     */
    private final ArchitectSession session;
    
    /**
     * Holds the settings for persistence to server.
     */
    private final KettleSettings settings;
    
    /**
     * used when big job spilitted into small
     * list of job name
     */
    private List<JobMeta> jobMetaList = new ArrayList<JobMeta>();
    /**
     * job number used create inner job
     */
    private int job_no = 0;
    public KettleJob(ArchitectSession session, KettleRepositoryDirectoryChooser chooser) {
        this(session);
        dirChooser = chooser;
    }
    
    public KettleJob(ArchitectSession session) {
        super();
        this.session = session;
        if (session.getWorkspace() != null 
                && session.getWorkspace().allowsChildType(KettleSettings.class)
                && !session.getWorkspace().getChildren(KettleSettings.class).isEmpty()) {
            KettleSettings newSettings = session.getWorkspace().getChildren(KettleSettings.class).get(0);
            if (newSettings == null) throw new IllegalStateException(
                    "The workspace should not have null settings if it reports to have them.");
            settings = newSettings;
        } else {
            settings = new KettleSettings();
        }
        tasksToDo = new ArrayList<String>();
        monitor = new MonitorableImpl();
        dirChooser = new RootRepositoryDirectoryChooser();
    }
    
    /**
     * This method translates the list of SQLTables to a Kettle Job and Transformations and saves
     * them to KJB and KTR files OR a repository.
     * @throws KettleException 
     */
    public void doExport(List<SQLTable> tableList, SQLDatabase targetDB ) throws SQLObjectException, RuntimeException, IOException, KettleException, SQLException {

        monitor = new MonitorableImpl();
        monitor.setMessage("");
        monitor.setJobSize(new Integer(tableList.size()+1)); 
        monitor.setStarted(true);

        try {

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

                JDBCDataSource target = targetDB.getDataSource();
                DatabaseMeta targetDatabaseMeta = addDatabaseConnection(databaseNames, target);
                transMeta.addDatabase(targetDatabaseMeta);

                List<SQLColumn> columnList = table.getColumns();
                List<String> noMappingForColumn = new ArrayList<String>();
                List<StepMeta> inputSteps = new ArrayList<StepMeta>();
                List<String[]> keyFields1 = new ArrayList<String[]>();
                List<String[]> keyFields2 = new ArrayList<String[]>();
                JDBCDataSourceType dsType = targetDB.getDataSource().getParentType();
                boolean isQuoting = dsType.getSupportsQuotingName();
                String ddlGeneratorClass = dsType.getDDLGeneratorClass();
                
                logger.debug("isQuoting:  " +isQuoting );
                for (SQLColumn column: columnList) {
                    SQLTable sourceTable;
                    String sourceColumn;
                    
                    String dataType = column.getSourceDataTypeName();
                   
                    if (settings.isTimeStampExcluded() && dataType.equalsIgnoreCase("timestamp")) {
                        continue;
                    }
                    // if quoting and not a PostgresDDLGenerator then adding square brackets for SQLServer
                    String columnName = column.getName();
                    columnName = getUpdatedName(isQuoting, columnName,ddlGeneratorClass);
                    logger.debug(" Quoted columnName: "+ columnName);
                    if (column.getSourceColumn() == null) {
                        /** if we have no source table then we will get nulls as
                         * placeholder from the target table.
                         */
                        sourceTable = table;
                        sourceColumn = "null";
                        noMappingForColumn.add(columnName);
                    } else {
                        sourceTable = column.getSourceColumn().getParent();
                        sourceColumn = column.getSourceColumn().getName();
                    }
                    /** if quoting and not a PostgresDDLGenerator then adding square brackets for SQLServer */
                    sourceColumn = getUpdatedName(isQuoting,sourceColumn,ddlGeneratorClass);
                    if(column.getSourceColumn() != null ) {
                        if (!tableMapping.containsKey(sourceTable)) {
                            List<String> pkCols = new ArrayList<String>();
                            List<String> fkCols = new ArrayList<String>();
                            StringBuffer buffer = new StringBuffer();
                            buffer.append("SELECT ");
                            buffer.append(sourceColumn);
                            buffer.append(" AS ").append(columnName);
                            pkCols.add(sourceColumn);
                            fkCols.add(sourceColumn);
                            List<SQLRelationship> exportedKeys = getPlaypenExportedKeys(tableList, sourceTable);
                            for(SQLRelationship exportedKey : exportedKeys) {
                                for (ColumnMapping mapping: exportedKey.getMappings()) {
                                    SQLColumn pkCol = mapping.getPkColumn();
                                    if(pkCol != null && !pkCol.getName().isEmpty()) {
                                        String updatedPkColName = getUpdatedName(isQuoting, pkCol.getName(),ddlGeneratorClass);
                                        if(!pkCols.contains(updatedPkColName) ){
                                            buffer.append(", ").append
                                            (updatedPkColName).append(" AS ").append(updatedPkColName);
                                            pkCols.add(updatedPkColName);
                                        }
                                    }
                                }
                            }
                            List<SQLImportedKey> importedKeys = getPlaypenImportedKeys(tableList, sourceTable);
                            for(SQLImportedKey importedKey : importedKeys) {
                                SQLRelationship relationShip = importedKey.getRelationship();
                                for (ColumnMapping mapping: relationShip.getMappings()) {
                                    SQLColumn fkCol = mapping.getFkColumn();
                                    if(fkCol != null && !fkCol.getName().isEmpty()) {
                                        String updatedFkColName = getUpdatedName(isQuoting, fkCol.getName(),ddlGeneratorClass);
                                        if(!fkCols.contains(updatedFkColName) ){
                                            buffer.append(", ").append
                                            (updatedFkColName).append(" AS ").append(updatedFkColName);
                                            fkCols.add(updatedFkColName);
                                        }
                                    }
                                }
                            }
                            tableMapping.put(sourceTable, buffer);
                        } else {
                            tableMapping.get(sourceTable).append(", ").append
                            (sourceColumn).append(" AS ").append(columnName);
                        }
                    }
                }

                if (tableMapping.containsKey(table)) {
                    if (tableMapping.size() == 1) {
                        noTransTables.add(table.getName());
                        tasksToDo.add("Update table " + table.getName() + " as no source data was found");
                        continue;
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        if(noMappingForColumn.size() > 0) {
                            buffer.append("There is no source for the column(s): ");
                            for (String noMapForCol: noMappingForColumn) {
                                buffer.append(noMapForCol).append(" ");
                            }

                            tasksToDo.add(buffer.toString() + " for the table " + table.getName());
                            transMeta.addNote(new NotePadMeta(buffer.toString(), 0, 150, 125, 125));
                        }
                    }
                }

                for (SQLTable sourceTable: tableMapping.keySet()) {
                    StringBuffer buffer = tableMapping.get(sourceTable);
                    buffer.append(" FROM " + DDLUtils.toQualifiedName(sourceTable));
                }

                for (SQLTable sourceTable: tableMapping.keySet()) {
                    List<String> keys1 = new LinkedList<String>();
                    List<String> keys2 = new LinkedList<String>();
                    List<SQLRelationship> exportedKeys = getPlaypenExportedKeys(tableList, sourceTable);
                    for (SQLRelationship exportedKey : exportedKeys) {
                        for (ColumnMapping mapping: exportedKey.getMappings()) {
                            SQLColumn pkCol = mapping.getPkColumn();
                            SQLColumn fkCol = mapping.getFkColumn();
                            if(pkCol != null && fkCol!= null) {
                                keys1.add(pkCol.getName());
                                keys2.add(fkCol.getName());
                            }
                        }
                    }

                    keyFields1.add(keys1.toArray(new String[keys1.size()]));
                    keyFields2.add(keys2.toArray(new String[keys2.size()]));
                    JDBCDataSource source = sourceTable.getParentDatabase().getDataSource();
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
               
                mergeSteps = createMergeJoins(settings.getJoinType(), transMeta, inputSteps,keyFields1, keyFields2);
               // boolean isInserUpdate = false;
                StepMeta stepMeta = null; 
                if (settings.isInsertUpdate()) {
                    InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
                     insertUpdateMeta.setDefault();
                    insertUpdateMeta.setDatabaseMeta(targetDatabaseMeta);
                    insertUpdateMeta.setTableName(table.getName());
                    insertUpdateMeta.setSchemaName(settings.getSchemaName());
                    stepMeta = new StepMeta("InsertUpdate", "Insert/Update " + table.getName(), insertUpdateMeta);

                } else {
                    TableOutputMeta tableOutputMeta = new TableOutputMeta();
                    tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
                    tableOutputMeta.setTablename(table.getName());
                    tableOutputMeta.setSchemaName(settings.getSchemaName());
                    stepMeta = new StepMeta("TableOutput", "Output to " + table.getName(), tableOutputMeta);
                    
                }
                if(stepMeta != null) {
                    stepMeta.setDraw(true);
                    stepMeta.setLocation((inputSteps.size()+1)*spacing, inputSteps.size()*spacing);
                    transMeta.addStep(stepMeta);
                }
                if (inputSteps.size() > 1 ) {
                    TransHopMeta transHopMeta = 
                            new TransHopMeta(mergeSteps.isEmpty()?inputSteps.get(0):mergeSteps.get(mergeSteps.size()-1), stepMeta);
                    //Commented as it always disable the hop for merge join
//                if (!mergeSteps.isEmpty()) {
//                    transMeta.addNote(new NotePadMeta("The final hop is disabled because the join types may need to be updated.",0,0,125,125));
//                    tasksToDo.add("Enable the final hop in " + transMeta.getName() + " after correcting the merge joins.");
//                    transHopMeta.setEnabled(false);
//                }
                    transMeta.addTransHop(transHopMeta);
                    transformations.add(transMeta);
                    logger.debug("Added a Trnasformation job for table "+table.getName());
                }
            }
            if (monitor.isCancelled()) {
                cancel();
                return;
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

            //Start entry
            JobEntryCopy startEntry = new JobEntryCopy();
            JobEntrySpecial start = new JobEntrySpecial("Start", true, false);
            startEntry.setEntry(start);
            startEntry.setLocation(10, spacing);
            startEntry.setDrawn();
            jm.addJobEntry(startEntry);

            JobEntryCopy oldJobEntry = null;
            int i = 1;
            for (TransMeta transformation : transformations) {
                JobEntryCopy entry = new JobEntryCopy();
                JobEntryTrans trans = new JobEntryTrans(transformation.getName());
                //trans.setJobEntryType(JobEntryType.TRANSFORMATION);
                trans.setTransname(transformation.getName());
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

            // Success entry

            JobEntrySuccess success = new JobEntrySuccess();
            success.setName("Success");
            JobEntryCopy successEntry = new JobEntryCopy();
            successEntry.setEntry(success);
            successEntry.setLocation(i*spacing, spacing);
            successEntry.setDrawn();
            jm.addJobEntry(successEntry);
            if(oldJobEntry != null) {
                JobHopMeta hop = new JobHopMeta(oldJobEntry, successEntry);
                jm.addJobHop(hop);
            }
            if (monitor.isCancelled()) {
                cancel();
                return;
            }
            String jobname = settings.getJobName();
            if (settings.isSplittingJob() && getJob_no() > 0) {
                 jobname += "_"+getJob_no();
            }
            jm.setName(jobname);
            logger.debug("setting job name: "+jobname);
            if (settings.isSavingToFile()) {
                outputToXML(transformations, jm);
            } else {
                jm.setDirectory(new RepositoryDirectory());
                outputToRepository(jm, transformations, createRepository());
            }
        } finally {
            monitor.setFinished(true);
        }
    }
    
    /**
     * 
     * @param tableList
     * @param targetDB
     * @param splitno, is a number of tables(transformation) per job
     * @throws Exception
     */
    public void doSplitedJobExport(List<SQLTable> tableList, SQLDatabase targetDB ) throws Exception {
        if (settings.isSplittingJob() && tableList.size() > settings.getSplitJobNo()) {
            jobMetaList.clear();
            // split a big job into small a jobs of splitno of (table)transformation/job
            List<List<SQLTable>> splitedTableList = chopped(tableList, settings.getSplitJobNo());
            for (List<SQLTable> newtableList:splitedTableList) {
                job_no++;
                doExport(newtableList,targetDB);
            }
            try {
                EnvUtil.environmentInit();
                LogWriter lw = LogWriter.getInstance();
                JobMeta jm = new JobMeta(lw);
                //Start Entry
                JobEntryCopy startEntry = new JobEntryCopy();
                JobEntrySpecial start = new JobEntrySpecial("Start", true, false);
                startEntry.setEntry(start);
                startEntry.setLocation(10, spacing);
                startEntry.setDrawn();
                jm.addJobEntry(startEntry);


                JobEntryCopy oldJobEntry = null;
                int i = 1;
                for (JobMeta job : jobMetaList) {
                    JobEntryCopy entry = new JobEntryCopy();
                    JobEntryJob jn = new JobEntryJob(job.getName());
                    jn.setJobName(job.getName());
                    entry.setEntry(jn);
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

//                // Success entry
                JobEntrySuccess success = new JobEntrySuccess();
                success.setName("Success");
                JobEntryCopy successEntry = new JobEntryCopy();
                successEntry.setEntry(success);
                successEntry.setLocation(i*spacing, spacing);
                successEntry.setDrawn();
                jm.addJobEntry(successEntry);
              
                JobHopMeta hop = new JobHopMeta(oldJobEntry, successEntry);
                jm.addJobHop(hop);
                
                if (monitor.isCancelled()) {
                    cancel();
                    return;
                }

                jm.setName(settings.getJobName());
                job_no=0;
                if (settings.isSavingToFile()) {
                    jobOutputToXML(jobMetaList, jm);
                } else {
                    jm.setDirectory(new RepositoryDirectory());
                    jobOutputToRepository(jm, jobMetaList, createRepository());
                }
            } finally {
                job_no=0;
            }
            
        } else {
            doExport(tableList,targetDB);
        }
    }
    
 private void jobOutputToRepository(JobMeta jm, List<JobMeta> jmList, Repository createRepository) {
        // TODO Auto-generated method stub
        
    }

private void jobOutputToXML(List<JobMeta> jmList, JobMeta jm) throws IOException {
    Map<File, String> outputs = new LinkedHashMap<File, String>();
    
   
    //This sets the location of the transformations in the job
    //The first entry is not a transformation so skip it
    //This is done here so we know where the files are being saved and that they are saved
    for (int i = 1; i < jm.nrJobEntries() -1; i++) {
        JobEntryJob jobs = (JobEntryJob)(jm.getJobEntry(i).getEntry());
        jobs.setFileName(getJobFilePath(jobs.getName()));
        logger.debug("jobOutputToXML::jobs fileName: "+jobs.getFileName());
    }

    String fileName = settings.getFilePath() ;
    if (!fileName.toUpperCase().endsWith(".KJB")) {
        fileName += ".kjb";
    }
    jm.setFilename(fileName);
    outputs.put(new File(fileName), jm.getXML());
    if (getJob_no() > 0) {
        jobMetaList.add(jm);
    }
    UserPrompter up = session.createUserPrompter(
            "The file {0} already exists. Overwrite?", UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL, UserPromptResponse.NOT_OK, 
            false, "Overwrite", "Don't Overwrite", "Cancel");
    for (File f : outputs.keySet()) {
        try {
            logger.debug("jobOutputToXML :: The file to output is " + f.getPath());
            if (f.exists()) {
                UserPromptResponse overwriteOption = up.promptUser(f.getAbsolutePath());
                if (overwriteOption == UserPromptResponse.OK) {
                    f.delete();
                } else if (overwriteOption == UserPromptResponse.NOT_OK) {
                    continue;
                } else if (overwriteOption == UserPromptResponse.CANCEL) {
                    cancel();
                    return;
                } else {
                    throw new IllegalStateException(
                            "Unknown response value from user prompt: " + overwriteOption);
                }
            }
            f.createNewFile();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                    FileOutputStream(f), "utf-8"));
            out.write(outputs.get(f));
            out.flush();
            out.close();
            monitor.setProgress(monitor.getProgress() + 1);
            
            if (monitor.isCancelled()) {
                cancel();
                return;
            }
        } catch (IOException er) {
            tasksToDo.clear();
            tasksToDo.add("File " + f.getName() + " was not created");
            throw er;
        }
    }
        
    }

private String getJobFilePath(String jobName) {
    String parentPath = new File(settings.getFilePath()).getParentFile().getPath();
    logger.debug("Parent file path is " + parentPath +" for job");
    return new File(parentPath,  jobName + ".kjb").getPath();
}

// chops a list into sublists of length L
    static <T> List<List<SQLTable>> chopped(List<SQLTable> list, final int L) {
        List<List<SQLTable>> parts = new ArrayList<List<SQLTable>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<SQLTable>(
                list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    /**
     * This is a helper method that sets the taskToDo list with
     * the correct message to display.
     */
    private void cancel() {
        tasksToDo.clear();
        tasksToDo.add("The Kettle job was cancelled so some files may be missing.");
    }
    
    /**
     * This method adds the given data source to the returned database metadata and
     * the databaseNames mapping if it does not already exist in the databaseNames.
     * This method is package private for testing
     */
    DatabaseMeta addDatabaseConnection(Map<String, DatabaseMeta> databaseNames, JDBCDataSource dataSource) throws RuntimeException {
        DatabaseMeta databaseMeta;
        if (!databaseNames.containsKey(dataSource.getName())) {
            try {
                databaseMeta = KettleUtils.createDatabaseMeta(dataSource);
                try {
                    Connection conn = dataSource.createConnection();
                    conn.close();
                } catch (SQLException e) {
                    logger.info("Could not connect to the database " + dataSource.getName() + ".");
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
    
    /**
     * This method outputs the xml for the given transformations and job to
     * files. The path class variable and parent file must be set before using
     * this method. The file is overwritten or not depending on the user
     * prompter provided by the ArchitectSession. This method is exposed as
     * package private for testing purposes.
     */
    void outputToXML(List<TransMeta> transformations, JobMeta job) throws IOException {
        Map<File, String> outputs = new LinkedHashMap<File, String>();
        
        for (TransMeta transMeta : transformations) {
            File file = new File(getTransFilePath(transMeta.getName()));
            logger.debug("transformation file: "+file.getAbsolutePath());
            transMeta.setFilename(file.getName());
            try {
                outputs.put(file, transMeta.getXML());
            } catch (KettleException e) {
                throw new RuntimeException(e);
            }
            if (monitor.isCancelled()) {
                cancel();
                return;
            }
        }
        
        //This sets the location of the transformations in the job
        //The first entry is not a transformation so skip it
        //This is done here so we know where the files are being saved and that they are saved
        for (int i = 1; i < job.nrJobEntries()-1; i++) {
            JobEntryTrans trans = (JobEntryTrans)(job.getJobEntry(i).getEntry());
            trans.setFileName(getTransFilePath(trans.getName()));
        }

        String fileName = settings.getFilePath() ;
        if (settings.isSplittingJob() && getJob_no() > 0) {
            String parentPath = new File(settings.getFilePath()).getParentFile().getPath();
            fileName = parentPath+"/"+job.getName()+".kjb";
        }
        if (!fileName.toUpperCase().endsWith(".KJB")) {
            
            fileName += ".kjb";
        }
        job.setFilename(fileName);
        outputs.put(new File(fileName), job.getXML());
        if (settings.isSplittingJob() && getJob_no() > 0) {
            jobMetaList.add(job);
        }
        UserPrompter up = session.createUserPrompter(
                "The file {0} already exists. Overwrite?", UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL, UserPromptResponse.NOT_OK, 
                false, "Overwrite", "Don't Overwrite", "Cancel");
        for (File f : outputs.keySet()) {
            try {
                logger.debug("outputToXML :: The file to output is " + f.getPath());
                if (f.exists()) {
                    UserPromptResponse overwriteOption = up.promptUser(f.getAbsolutePath());
                    if (overwriteOption == UserPromptResponse.OK) {
                        f.delete();
                    } else if (overwriteOption == UserPromptResponse.NOT_OK) {
                        continue;
                    } else if (overwriteOption == UserPromptResponse.CANCEL) {
                        cancel();
                        return;
                    } else {
                        throw new IllegalStateException(
                                "Unknown response value from user prompt: " + overwriteOption);
                    }
                }
                f.createNewFile();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                        FileOutputStream(f), "utf-8"));
                out.write(outputs.get(f));
                out.flush();
                out.close();
                monitor.setProgress(monitor.getProgress() + 1);
                
                if (monitor.isCancelled()) {
                    cancel();
                    return;
                }
            } catch (IOException er) {
                tasksToDo.clear();
                tasksToDo.add("File " + f.getName() + " was not created");
                throw er;
            }
        }
    }
    
    /**
     * This returns the full path and file name for the given transformation name.
     * The file location of the transformations is based on the location of the job.
     */
    String getTransFilePath(String transName) {
        String parentPath = new File(settings.getFilePath()).getParentFile().getPath();
        logger.debug("Parent file path is " + parentPath);
        return new File(parentPath, "transformation_for_table_" + transName + ".ktr").getPath();
    }
    
    /** 
     * Pass the repository a connection straight as the Repository
     * connect method loads its own drivers and we don't want to
     * include them.
     */
    public void createStraightConnection(Repository repo)  throws KettleException, SQLException {
        repo.getDatabase().setConnection(settings.getRepository().createConnection());
    }

    /**
     * This method translates the list of SQLTables to a Kettle Job and Transformations and saves 
     * them into a Kettle repository.
     * @throws KettleException 
     */
    void outputToRepository(JobMeta jm, List<TransMeta> transformations, Repository repo) throws KettleException, SQLException {
        
        try {
            createStraightConnection(repo);
            
            RepositoryDirectory directory;
            
            try {
                // We refresh the repository tree as we are passing a connection
                // straight. The Repository connect method does this automatically
                // but we now need to do it manually.
                repo.refreshRepositoryDirectoryTree();

                directory = dirChooser.selectDirectory(repo);
                if (directory == null) {
                    throw new KettleException("The directory of the repository was not available.");
                }
            } catch (KettleException e) {
                tasksToDo.clear();
                tasksToDo.add("The directory of the repository was not available.");
                throw e;
            } 

            try {
                UserPrompter up = session.createUserPrompter(
                        "{0} {1} already exists in the repository. Replace?", UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL,
                        UserPromptResponse.NOT_OK, false, "Replace", "Don't Replace", "Cancel");
                for (TransMeta tm: transformations) {
                    if (monitor.isCancelled()) {
                        cancel();
                        return;
                    }
                    tm.setDirectory(directory);
                    
                    // check for existing transaction having same name
                    long id = repo.getTransformationID(tm.getName(), directory.getID());
                    if (id >= 0) {
                        logger.debug("We found a transformation with the same name, the id is " + id);
                        UserPromptResponse overwriteOption = up.promptUser("Transformation", tm.getName());
                        
                        if (overwriteOption == UserPromptResponse.OK) {
                            // will fall through and overwrite
                        } else if (overwriteOption == UserPromptResponse.NOT_OK) {
                            monitor.setProgress(monitor.getProgress() + 1);
                            continue;
                        } else if (overwriteOption == UserPromptResponse.CANCEL) {
                            cancel();
                            break;
                        } else {
                            throw new IllegalStateException(
                                    "Unknown user prompt response: " + overwriteOption);
                        }
                    }
                    tm.saveRep(repo);
                    monitor.setProgress(monitor.getProgress() + 1);
                    logger.debug("Progress is " + monitor.getProgress() + " out of " + monitor.getJobSize());
                }
                if (monitor.isCancelled()) {
                    cancel();
                    return;
                }

                //This sets the location of the transformations in the job
                //The first entry is not a transformation so skip it
                //This is done here so we know where the files are being saved and that they are saved
                for (int i = 1; i < jm.nrJobEntries(); i++) {
                    JobEntryTrans trans = (JobEntryTrans) (jm.getJobEntry(i).getEntry());
                    trans.setDirectory(directory.getDirectoryName());
                    trans.setDirectoryPath(directory.getPath());
                }

                jm.setDirectory(directory);
                if (repo.getTransformationID(jm.getName(), directory.getID()) >= 0) {
                    UserPromptResponse overwriteOption = up.promptUser("Job", jm.getName());
                    if (overwriteOption == UserPromptResponse.OK) {
                        // will fall through and overwrite
                    } else if (overwriteOption == UserPromptResponse.NOT_OK) {
                        return;
                    } else if (overwriteOption == UserPromptResponse.CANCEL) {
                        cancel();
                        return;
                    } else {
                        throw new IllegalStateException(
                                "Unknown user prompt response: " + overwriteOption);
                    }
                }
                jm.saveRep(repo);
                monitor.setProgress(monitor.getProgress() + 1);
                logger.debug("Progress is " + monitor.getProgress() + " out of " + monitor.getJobSize());
            } catch (KettleException e) {
                tasksToDo.clear();
                tasksToDo.add("Kettle job " + jm.getName() + " failed to save to respitory due to a Kettle error.");
                throw e;
            }
        } catch (SQLException e) {
            tasksToDo.clear();
            tasksToDo.add("Kettle job " + jm.getName() + " failed to save to respitory due to a SQL error.");
            throw e;
        } finally {
            repo.disconnect();
        }
    }
    
    /**
     * This method creates a Kettle Repository instance that refers to the
     * existing Kettle repository schema referred to by the {@link #repository}
     * database connection.  This method does not attempt to actually make
     * a database connection itself; the returned Repository will attempt
     * the connection by itself later on.
     */
    Repository createRepository() {

        DatabaseMeta kettleDBMeta = KettleUtils.createDatabaseMeta(settings.getRepository());
        RepositoryMeta repoMeta = new RepositoryMeta("", "", kettleDBMeta);

        UserInfo userInfo = new UserInfo(settings.getRepository().get(KettleOptions.KETTLE_REPOS_LOGIN_KEY),
                settings.getRepository().get(KettleOptions.KETTLE_REPOS_PASSWORD_KEY),
                settings.getJobName(), "", true, null);
        LogWriter lw = LogWriter.getInstance(); // Repository constructor needs this for some reason

        Repository repo = new Repository(lw, repoMeta, userInfo);
        return repo;

    }
    
    /**
     * This method returns the necessary data to create a repository, but doesn't actually
     * create it so that we create a testing repository.
     */
    public Object[] createTestRepository() {

        DatabaseMeta kettleDBMeta = KettleUtils.createDatabaseMeta(settings.getRepository());
        RepositoryMeta repoMeta = new RepositoryMeta("", "", kettleDBMeta);

        UserInfo userInfo = new UserInfo(settings.getRepository().get(KettleOptions.KETTLE_REPOS_LOGIN_KEY),
                settings.getRepository().get(KettleOptions.KETTLE_REPOS_PASSWORD_KEY),
                settings.getJobName(), "", true, null);
        LogWriter lw = LogWriter.getInstance(); // Repository constructor needs this for some reason

        Object[] ret = new Object[3];
        ret[0] = (Object)lw;
        ret[1] = repoMeta;
        ret[2] = userInfo;
        return ret;

    }

    /**
     * This creates all of the MergeJoin kettle steps as well as their hops from
     * the steps in the inputSteps list. The MergeJoin steps are also put into the 
     * TransMeta. This method is package private for testing purposes.
     */
    List<StepMeta> createMergeJoins(int defaultJoinType, TransMeta transMeta, List<StepMeta> inputSteps, List<String[]> keyField1, List<String[]> keyField2) {
        List<StepMeta> mergeSteps = new ArrayList<StepMeta>();
        if (inputSteps.size() > 1) {
            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[defaultJoinType]);
            mergeJoinMeta.setStepName1(inputSteps.get(0).getName());
            mergeJoinMeta.setStepMeta1(inputSteps.get(0));
            mergeJoinMeta.setStepName2(inputSteps.get(1).getName());
            mergeJoinMeta.setStepMeta2(inputSteps.get(1));
            String[] keyField_1 = keyField1.get(0);
            String[] keyField_2 = keyField2.get(0);
            logger.debug("MergeJoin Join tables " +
                    inputSteps.get(0).getName() + " and " + 
                    inputSteps.get(1).getName());
            logger.debug("Key_Field1 :"+Arrays.toString(keyField_1));
            logger.debug("Key_Field2 :"+Arrays.toString(keyField_2));
            mergeJoinMeta.setKeyFields1(keyField_1);
            mergeJoinMeta.setKeyFields2(keyField_2);
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
            //commenting it disable the final hop. So when user open transformation in Pentaho they received warning about hop
           tasksToDo.add("Verify the merge join " + stepMeta.getName() + " does the correct merge.");
        }

        for (int i = 0; i < inputSteps.size()-2; i++) {
            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[defaultJoinType]);
            mergeJoinMeta.setStepName1(mergeSteps.get(i).getName());
            mergeJoinMeta.setStepMeta1(mergeSteps.get(i));
            mergeJoinMeta.setStepName2(inputSteps.get(i+2).getName());
            mergeJoinMeta.setStepMeta2(inputSteps.get(i+2));
            String[] keyField_1 = keyField1.get(i+2);
            String[] keyField_2 = keyField2.get(i+2);
            logger.debug("*** MergeJoin Join tables " +
                    inputSteps.get(i+2).getName() + " and " + 
                    inputSteps.get(i+2).getName());
            logger.debug("*** Key_Field1 :"+Arrays.toString(keyField_1));
            logger.debug("*** Key_Field2 :"+Arrays.toString(keyField_2));

            mergeJoinMeta.setKeyFields1(keyField_1);
            mergeJoinMeta.setKeyFields2(keyField_2);
            StepMeta stepMeta = new StepMeta("MergeJoin", "Join table " + inputSteps.get(i+2).getName(), mergeJoinMeta);
            stepMeta.setDraw(true);
            stepMeta.setLocation((i + 3) * spacing, new Double((i + 2.25) * spacing).intValue());
            transMeta.addStep(stepMeta);
            mergeSteps.add(stepMeta);
            TransHopMeta transHopMeta = new TransHopMeta(mergeSteps.get(i), stepMeta);
            transMeta.addTransHop(transHopMeta);
            transHopMeta = new TransHopMeta(inputSteps.get(i+2), stepMeta);
            transMeta.addTransHop(transHopMeta);
            //commenting it disable the final hop. So when user open transformation in Pentaho they received warning about hop
            tasksToDo.add("Verify the merge join " + stepMeta.getName() + " does the correct merge.");
        }
        return mergeSteps;
    }
    
    public String getFilePath() {
        return settings.getFilePath();
    }
    public void setFilePath(String filePath) {
        settings.setFilePath(filePath);
    }
    public String getJobName() {
        return settings.getJobName();
    }
    public void setJobName(String jobName) {
       settings.setJobName(jobName);
    }
    public int getKettleJoinType() {
        return settings.getJoinType();
    }
    public void setKettleJoinType(int kettleJoinType) {
        settings.setJoinType(kettleJoinType);
    }
    public String getSchemaName() {
        return settings.getSchemaName();
    }
    public void setSchemaName(String schemaName) {
        settings.setSchemaName(schemaName);
    }
 
    public boolean isTimeStampExcluded() {
        return settings.isTimeStampExcluded();
    }
 
    public void setTimeStampExcluded(boolean isTimeStampExcluded) {
        settings.setTimeStampExcluded(isTimeStampExcluded);
    }

    public List<String> getTasksToDo() {
        return tasksToDo;
    }

    public Integer getJobSize() {
        return monitor.getJobSize();
    }

    public String getMessage() {
        return monitor.getMessage();
    }

    public int getProgress() {
        return monitor.getProgress();
    }

    public boolean hasStarted() {
        return monitor.hasStarted();
    }

    public boolean isFinished() {
        return monitor.isFinished();
    }

    public void setCancelled(boolean cancelled) {
        monitor.setCancelled(cancelled);
    }

    public boolean isCancelled() {
        return monitor.isCancelled();
    }
    
    public boolean isSavingToFile() {
        return settings.isSavingToFile();
    }
    public void setSavingToFile(boolean savingToFile) {
        settings.setSavingToFile(savingToFile);
    }

    public void setRepository(JDBCDataSource source) {
        settings.setRepository(source);
    }

    public void setRepositoryDirectoryChooser(KettleRepositoryDirectoryChooser chooser) {
        dirChooser = chooser;
    }

    public SPDataSource getRepository() {
        return settings.getRepository();
    }
    
    public int getSplitJobNo() {
        return settings.getSplitJobNo();
    }
  

    public void setSplitJobNo(int newValue) {
        settings.setSplitJobNo(newValue);
    }

    /**
     * return the current job no
     */
    private int getJob_no() {
        return job_no;
    }
    
    public boolean isSplittingJob() {
        return settings.isSplittingJob();
    }
 
    public void setSplittingJob(boolean newValue) {
        settings.setSplittingJob(newValue);
    }
    
    public boolean isInsertUpdate() {
        return settings.isInsertUpdate();
    }
    
    public void setIsInsertUpdate(boolean newValue) {
        settings.setIsInsertUpdate(newValue);
    }
    /**
     * Method to get the latest exported keys in a playpen.
     * Exported keys are different for table in Database then table in PlayPen 
     * when user create new Relationship manually which doesn't exists in database table.
     * When user create a kettle job with tables in a Playpen, user might create 
     * new relationship which doesn't exists in database and limited to playpen only.
     * So getting the correct/latest exported keys from the relationship from the table whose parent is PlayPen.
     * Note: Even though the table is dragged from database tree it can have different exported keys 
     * (after dragging it in the playpen)specially when user create new relationship manually.
     * @param tableList
     *      List of Tables in a playpen
     * @param sourceTable
     *          Table whose parent is Database(dbo)
     * @return
     */
    private List<SQLRelationship> getPlaypenExportedKeys(List<SQLTable> tableList, SQLTable  sourceTable) {
        List<SQLRelationship> exportedKeys = new ArrayList<>();
        SQLTable playpenTable = null;
        for(SQLTable pTable :tableList) {
            if (sourceTable.getName().equalsIgnoreCase(pTable.getName())) {
                playpenTable = pTable; 
                break;
            }
        }
        if(playpenTable != null) {
            try {
                exportedKeys = playpenTable.getExportedKeys();
            } catch (SQLObjectException e) {
                e.printStackTrace();
            }
        }
        return exportedKeys;
    }

    /**
     * Method to get the latest imported keys in a playpen.
     * @param tableList
     *      List of Tables in a playpen
     * @param sourceTable
     *      Table whose parent is Database(dbo)
     * @return
     */
    private List<SQLImportedKey> getPlaypenImportedKeys(List<SQLTable> tableList, SQLTable sourceTable) {
        List<SQLImportedKey> importedKeys = new ArrayList<>();
        SQLTable playpenTable = null;
        for(SQLTable pTable :tableList) {
            if (sourceTable.getName().equalsIgnoreCase(pTable.getName())) {
                playpenTable = pTable; 
                break;
            }
        }
        if(playpenTable != null) {
            try {
                importedKeys = playpenTable.getImportedKeys();
            } catch (SQLObjectException e) {
                e.printStackTrace();
            }
        }
        return importedKeys;
    }

    /**
     * Get the updated Colum,n name based on 
     * @param isQuoting
     * @param columnName
     * @param ddlGeneratorClass
     * @return
     */
    private String getUpdatedName(boolean isQuoting, String columnName, String ddlGeneratorClass) {
        if ((isQuoting) && ddlGeneratorClass!= null && !ddlGeneratorClass.equals(PostgresDDLGenerator.class.getName())
                && !(columnName.startsWith("[") && columnName.endsWith("]"))) {
            columnName = "["+columnName+"]";
        }  else if ((isQuoting) && !(columnName.startsWith("\"") && columnName.endsWith("\""))) { 
            //else if quoting for PostgresDDLGenerator
            columnName = "\""+columnName+"\"";
        }
        return columnName;
    }
}
