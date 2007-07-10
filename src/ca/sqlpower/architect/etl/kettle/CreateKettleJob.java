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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
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
import ca.sqlpower.architect.Monitorable;
import ca.sqlpower.architect.MonitorableImpl;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.FileValidator.FileValidationResponse;
import ca.sqlpower.architect.ddl.DDLUtils;

/**
 * This class stores the settings for and creates a new Kettle job
 */
public class CreateKettleJob implements Monitorable {
    
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
     * A list of tasks that an administrator or tech will have to do to the Kettle
     * job to make it run correctly.
     */
    private List<String> tasksToDo;
    
    /**
     * The file validator allows the selection for overwriting a file or not when saving
     * the XML.
     */
    private FileValidator fileValidator;
    
    /**
     * This is the monitor implementation for this class. This is used to handle the progress bar
     * on the action window.
     */
    private MonitorableImpl monitor;
    
    /**
     * A flag that determines whether we will save the job to an xml file or a Kettle repository 
     */
    private boolean savingToFile = true;
    
    /**
     * The ArchitectDataSource representation of the database with the Kettle repository we want to save to
     */
    private ArchitectDataSource repository;
    
    /**
     * The repository directory chooser that will select, or allow the user to select, the directory to save
     * to.
     */
    private KettleRepositoryDirectoryChooser dirChooser;
    
    public CreateKettleJob(FileValidator validator, KettleRepositoryDirectoryChooser chooser) {
        this();
        fileValidator = validator;
        dirChooser = chooser;
    }
    
    public CreateKettleJob() {
        super();
        filePath = "";
        tasksToDo = new ArrayList<String>();
        fileValidator = new AlwaysAcceptFileValidator();
        monitor = new MonitorableImpl();
        dirChooser = new RootRepositoryDirectoryChooser();
    }
    
    /**
     * This method translates the list of SQLTables to a Kettle Job and Transformations and saves
     * them to KJB and KTR files OR a repository.
     * @throws KettleException 
     */
    public void doExport(List<SQLTable> tableList, SQLDatabase targetDB ) throws ArchitectException, RuntimeException, IOException, KettleException {

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

                if (monitor.isCancelled()) {
                    cancelled();
                    return;
                }
                
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
                trans.setName(transformation.getName());
                trans.setType(JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION);
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

            if (monitor.isCancelled()) {
                cancelled();
                return;
            }
            
            jm.setName(jobName);
            
            if (savingToFile) {
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
     * This is a helper method that sets the taskToDo list with
     * the correct message to display.
     */
    private void cancelled() {
        tasksToDo.clear();
        tasksToDo.add("The Kettle job was cancelled so some files may be missing.");
    }
    
    /**
     * This method adds the given data source to the returned database metadata and
     * the databaseNames mapping if it does not already exist in the databaseNames.
     * This method is package private for testing
     */
    DatabaseMeta addDatabaseConnection(Map<String, DatabaseMeta> databaseNames, ArchitectDataSource dataSource) throws RuntimeException {
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
    
    /**
     * This method outputs the xml for the given transformations and job to files.
     * The path class variable and parent file must be set before using this method.
     * The file is overwritten or not depending on the file validator. This method
     *  is package private for testing purposes.
     */
    void outputToXML(List<TransMeta> transformations, JobMeta job) throws IOException {
        Map<File, String> outputs = new LinkedHashMap<File, String>();
        
        for (TransMeta transMeta : transformations) {
            File file = new File(getTransFilePath(transMeta.getName()));
            transMeta.setFilename(file.getName());
            outputs.put(file, transMeta.getXML());
            if (monitor.isCancelled()) {
                cancelled();
                return;
            }
        }
        
        //This sets the location of the transformations in the job
        //The first entry is not a transformation so skip it
        //This is done here so we know where the files are being saved and that they are saved
        for (int i = 1; i < job.nrJobEntries(); i++) {
            JobEntryTrans trans = (JobEntryTrans)(job.getJobEntry(i).getEntry());
            trans.setFileName(getTransFilePath(trans.getName()));
        }

        String fileName = filePath;
        if (!fileName.toUpperCase().endsWith(".KJB")) {
            fileName += ".kjb";
        }
        job.setFilename(fileName);
        outputs.put(new File(fileName), job.getXML());
        
        FileValidationResponse overwriteOption = FileValidationResponse.WRITE_OK;
        for (File f : outputs.keySet()) {
            try {
                if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS) {
                    continue;
                }
                logger.debug("The file to output is " + f.getPath());
                if (f.exists()) {
                    if (overwriteOption == FileValidationResponse.WRITE_OK_ALWAYS) {
                        f.delete();
                    } else {
                        String parentFilePath = f.getParentFile().getPath();
                        overwriteOption = fileValidator.acceptFile(f.getName(), parentFilePath);
                        if (overwriteOption == FileValidationResponse.WRITE_OK ||
                                overwriteOption == FileValidationResponse.WRITE_OK_ALWAYS) {
                        } else if (overwriteOption == FileValidationResponse.CANCEL) {
                            cancelled();
                            return;
                        } else {
                            continue;
                        }
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
                    cancelled();
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
        String parentPath = new File(filePath).getParentFile().getPath();
        logger.debug("Parent file path is " + parentPath);
        return new File(parentPath, "transformation_for_table_" + transName + ".ktr").getPath();
    }

    /**
     * This method translates the list of SQLTables to a Kettle Job and Transformations and saves 
     * them into a Kettle repository.
     * @throws KettleException 
     */
    void outputToRepository(JobMeta jm, List<TransMeta> transformations, Repository repo) throws KettleException {
        
        try {
            repo.connect("MooCow");
            
            RepositoryDirectory directory = dirChooser.selectDirectory(repo);
            if (directory == null) {
                cancelled();
                return;
            }
            
            FileValidationResponse overwriteOption = FileValidationResponse.WRITE_OK;
            for (TransMeta tm: transformations) {
                if (monitor.isCancelled()) {
                    cancelled();
                    return;
                }
                tm.setDirectory(directory);
                if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS) {
                    monitor.setProgress(monitor.getProgress() + 1);
                    continue;
                }
                if (overwriteOption != FileValidationResponse.WRITE_OK_ALWAYS) {
                    long id = repo.getTransformationID(tm.getName(), directory.getID());
                    if (id >= 0) {
                        logger.debug("We found a transformation with the same name, the id is " + id);
                        overwriteOption = fileValidator.acceptFile(tm.getName(), directory.getPath());
                        if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS ||
                                overwriteOption == FileValidationResponse.WRITE_NOT_OK) {
                            monitor.setProgress(monitor.getProgress() + 1);
                            continue;
                        }
                    }
                }
                tm.saveRep(repo);
                monitor.setProgress(monitor.getProgress() + 1);
                logger.debug("Progress is " + monitor.getProgress() + " out of " + monitor.getJobSize());
            }
            if (monitor.isCancelled()) {
                cancelled();
                return;
            }
            
            //This sets the location of the transformations in the job
            //The first entry is not a transformation so skip it
            //This is done here so we know where the files are being saved and that they are saved
            for (int i = 1; i < jm.nrJobEntries(); i++) {
                JobEntryTrans trans = (JobEntryTrans)(jm.getJobEntry(i).getEntry());
                trans.setDirectory(directory);
            }
            
            jm.setDirectory(directory);
            if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS) {
                return;
            }
            if (overwriteOption != FileValidationResponse.WRITE_OK_ALWAYS) {
                if (repo.getTransformationID(jm.getName(), directory.getID()) >= 0) {
                    overwriteOption = fileValidator.acceptFile(jm.getName(), directory.getPath());
                    if (overwriteOption == FileValidationResponse.WRITE_NOT_OK_ALWAYS ||
                            overwriteOption == FileValidationResponse.WRITE_NOT_OK) {
                        return;
                    }
                }
            }
            jm.saveRep(repo);
            monitor.setProgress(monitor.getProgress() + 1);
            logger.debug("Progress is " + monitor.getProgress() + " out of " + monitor.getJobSize());
        } catch (KettleException e) {
            tasksToDo.clear();
            tasksToDo.add("Kettle job " + jm.getName() + " failed to save to respitory");
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
     * 
     * @throws ArchitectException If the kettle repository connection fails
     * for any reason (usually related to classloading problems)
     */
    private Repository createRepository() throws ArchitectException {
        
        try {
            DatabaseMeta kettleDBMeta = KettleUtils.createDatabaseMeta(repository);

            // Reflection is evil. Sorry.
            Class<RepositoryMeta> rmClass = (Class<RepositoryMeta>) Class.forName("be.ibridge.kettle.RepositoryMeta", true, repository.getParentType().getJdbcClassLoader());

            // These two lines could just be repoMeta = rmClass.newInstance() if we can use the no-args constructor.
            Constructor<RepositoryMeta> rmConst = rmClass.getConstructor(new Class[] {String.class, String.class, DatabaseMeta.class});
            RepositoryMeta repoMeta = rmConst.newInstance(new Object[] {"", "", kettleDBMeta});

            UserInfo userInfo = new UserInfo(repository.get(KettleOptions.KETTLE_REPOS_LOGIN_KEY),
                    repository.get(KettleOptions.KETTLE_REPOS_PASSWORD_KEY),
                    jobName, "", true, null);
            LogWriter lw = LogWriter.getInstance(); // Repository constructor needs this for some reason
            Repository repo = new Repository(lw, repoMeta, userInfo);
            
            return repo;
            
        } catch (ClassNotFoundException e) {
            throw new ArchitectException("Couldn't create Kettle repository connection", e);
        } catch (NoSuchMethodException e) {
            throw new ArchitectException("Couldn't create Kettle repository connection", e);
        } catch (InstantiationException e) {
            throw new ArchitectException("Couldn't create Kettle repository connection", e);
        } catch (IllegalAccessException e) {
            throw new ArchitectException("Couldn't create Kettle repository connection", e);
        } catch (InvocationTargetException e) {
            throw new ArchitectException("Couldn't create Kettle repository connection", e);
        }
    }

    /**
     * This creates all of the MergeJoin kettle steps as well as their hops from
     * the steps in the inputSteps list. The MergeJoin steps are also put into the 
     * TransMeta. This method is package private for testing purposes.
     */
    List<StepMeta> createMergeJoins(int defaultJoinType, TransMeta transMeta, List<StepMeta> inputSteps) {
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

    public List<String> getTasksToDo() {
        return tasksToDo;
    }

    public void setFileValidator(FileValidator fileValidator) {
        this.fileValidator = fileValidator;
    }
    
    public Integer getJobSize() throws ArchitectException {
        return monitor.getJobSize();
    }

    public String getMessage() {
        return monitor.getMessage();
    }

    public int getProgress() throws ArchitectException {
        return monitor.getProgress();
    }

    public boolean hasStarted() throws ArchitectException {
        return monitor.hasStarted();
    }

    public boolean isFinished() throws ArchitectException {
        return monitor.isFinished();
    }

    public void setCancelled(boolean cancelled) {
        monitor.setCancelled(cancelled);
    }

    public boolean isSavingToFile() {
        return savingToFile;
    }
    public void setSavingToFile(boolean savingToFile) {
        this.savingToFile = savingToFile;
    }

    public void setRepository(ArchitectDataSource source) {
        this.repository = source;
    }

    public void setRepositoryDirectoryChooser(KettleRepositoryDirectoryChooser chooser) {
        dirChooser = chooser;
    }

    public ArchitectDataSource getRepository() {
        return repository;
    }
}
