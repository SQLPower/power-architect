package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.database.DatabaseMeta;
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
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CreateKettleJobPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class CreateKettleJobAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(CreateKettleJobAction.class);
    
    private ArchitectFrame architectFrame;
    
    public CreateKettleJobAction() {
        super("Create Kettle Job...",
              ASUtils.createIcon(""
                                 , "Create a new Kettle job"
                                 , ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE
                                 , ArchitectFrame.DEFAULT_ICON_SIZE)));
        architectFrame = ArchitectFrame.getMainInstance();
        putValue(SHORT_DESCRIPTION, "Create a Kettle Job");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        
        JDialog d;
        final JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        final CreateKettleJobPanel kettleETLPanel = new CreateKettleJobPanel(architectFrame.getProject());

        Action okAction, cancelAction;
        okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                
                if (!kettleETLPanel.applyChanges()) {
                    return;
                }
                
                EnvUtil.environmentInit();
                LogWriter lw = LogWriter.getInstance();
                JobMeta jm = new JobMeta(lw);
                
                Map<SQLTable, StringBuffer> tableMapping = new LinkedHashMap<SQLTable, StringBuffer>();
                List<TransMeta> transitions = new ArrayList<TransMeta>();

                try {
                    // The depth-first search will do a topological sort of
                    // the target table graph, so parent tables will come before
                    // their children.
                    List<SQLTable> tableList = 
                        new DepthFirstSearch(architectFrame.getProject().getPlayPen().getTables()).getFinishOrder();
                    
                    int j = 0;
                    
                    Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();
                    
                    TransMeta transMeta = new TransMeta();
                    ArchitectDataSource target = architectFrame.getProject().getPlayPen().getDatabase().getDataSource();
                    String targetName = target.getName();
                    String username = target.getUser();
                    String password = target.getPass();
                    ArchitectDataSourceType targetType = target.getParentType();
                    String connectionType = targetType.getProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY); 
                    Map<String, String> map = targetType.retrieveURLParsing(target.getUrl());
                    String hostname = map.get("Hostname");
                    String port = map.get("Port");
                    String database = map.get("Database");
                    DatabaseMeta targetDatabaseMeta;
                    try {
                        targetDatabaseMeta = new DatabaseMeta(targetName
                                                              , connectionType
                                                              , "Native"
                                                              , hostname==null?"":hostname
                                                              , database==null?"":database
                                                              , port==null?"":port, username
                                                              , password);
                    } catch (RuntimeException re) {
                        logger.error("Could not connect to the database " + targetName + ".");
                        re.printStackTrace();
                        ASUtils.showExceptionDialog
                                        ("Could not create the database " + targetName + "." +
                                         "\nThe Kettle connection type was not set in User Preferences."
                                         , re);
                        return;
                    }
                    if (!KettleOptions.connectToDB(cp, targetDatabaseMeta)) {
                        return;
                    }
                    transMeta.addDatabase(targetDatabaseMeta); 
                    databaseNames.put(targetName, targetDatabaseMeta);
                    
                    for (SQLTable table: tableList) {
                        transMeta = new TransMeta();
                        transitions.add(transMeta);
                        
                        List<SQLColumn> columnList = table.getColumns();
                        
                        for (SQLColumn column: columnList) {
                            SQLTable sourceTable = column.getSourceColumn().getParentTable();
                                                        
                            String sourceColumn = column.getSourceColumn().getName();
                            if (!tableMapping.containsKey(sourceTable)) {
                                StringBuffer buffer = new StringBuffer();
                                buffer.append("SELECT ");
                                buffer.append(sourceColumn);
                                tableMapping.put(sourceTable, buffer);
                            } else {
                                tableMapping.get(sourceTable).append(", ").append(sourceColumn);
                            }
                        }
                    

                        for (SQLTable sourceTable: tableMapping.keySet()) {
                            StringBuffer buffer = tableMapping.get(sourceTable);
                            buffer.append(" FROM " + sourceTable.getName());
                        }

                        List<StepMeta> inputSteps = new ArrayList<StepMeta>();
                        int i = 200;
                        for (SQLTable sourceTable: tableMapping.keySet()) {
                            ArchitectDataSource source = sourceTable.getParentDatabase().getDataSource();
                            String sourceName = source.getName();
                            DatabaseMeta databaseMeta;
                            String hostName = "";
                            String databaseName = "";
                            username = source.getUser();
                            password = source.getPass();
                            ArchitectDataSourceType sourceType = source.getParentType();
                            connectionType = targetType.getProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY); 
                            map = sourceType.retrieveURLParsing(source.getUrl());
                            hostname = map.get("Hostname");
                            port = map.get("Port");
                            database = map.get("Database");
                            if (!databaseNames.containsKey(sourceName)) {
                                try {
                                    databaseMeta = new DatabaseMeta(sourceName 
                                            , connectionType
                                            , "Native" 
                                            , hostname==null?"":hostname 
                                                    , database==null?"":database 
                                                            , port==null?"":port
                                                                    , username
                                                                    , password);
                                } catch (RuntimeException re) {
                                    logger.error("Could not connect to the database.");
                                    re.printStackTrace();
                                    ASUtils.showExceptionDialog
                                    ("Could not create the database " + targetName + "." +
                                            "\nThe Kettle connection type was not set in User Preferences."
                                            , re);
                                    return;
                                }
                                logger.debug("The database metadata is: " + databaseMeta.getDatabaseTypeDesc() +
                                        " " + databaseMeta.getHostname() + " " + databaseMeta.getDatabaseName() + 
                                        " " + databaseMeta.getUsername() + " " + databaseMeta.getPassword());
                                if (!KettleOptions.connectToDB(cp, databaseMeta)) {
                                    return;
                                }
                                transMeta.addDatabase(databaseMeta);
                                databaseNames.put(sourceName, databaseMeta);
                            } else {
                                databaseMeta = databaseNames.get(sourceName);
                            }
                            
                            TableInputMeta tableInputMeta = new TableInputMeta();
                            String stepName = "Table " + sourceTable.getName() + " from " + hostName + ":" + databaseName;
                            StepMeta stepMeta = new StepMeta("TableInput", stepName, tableInputMeta);
                            stepMeta.setDraw(true);
                            stepMeta.setLocation(i, i+j);
                            tableInputMeta.setDatabaseMeta(databaseMeta);
                            tableInputMeta.setSQL(tableMapping.get(sourceTable).toString());
                            transMeta.addStep(stepMeta);
                            inputSteps.add(stepMeta);
                            i += 200;
                        }

                        List<StepMeta> mergeSteps = new ArrayList<StepMeta>();
                        if (inputSteps.size() > 1) {
                            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
                            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[3]);
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
                            stepMeta.setLocation(600, 300+j);
                            transMeta.addStep(stepMeta);
                            mergeSteps.add(stepMeta);
                            TransHopMeta transHopMeta = new TransHopMeta(inputSteps.get(0), stepMeta);
                            transMeta.addTransHop(transHopMeta);
                            transHopMeta = new TransHopMeta(inputSteps.get(1), stepMeta);
                            transMeta.addTransHop(transHopMeta);
                        }

                        for (i = 0; i < inputSteps.size()-2; i++) {
                            MergeJoinMeta mergeJoinMeta = new MergeJoinMeta();
                            mergeJoinMeta.setJoinType(MergeJoinMeta.join_types[3]);
                            mergeJoinMeta.setStepName1(mergeSteps.get(i).getName());
                            mergeJoinMeta.setStepMeta1(mergeSteps.get(i));
                            mergeJoinMeta.setStepName2(inputSteps.get(i+2).getName());
                            mergeJoinMeta.setStepMeta2(inputSteps.get(i+2));
                            mergeJoinMeta.setKeyFields1(new String[]{});
                            mergeJoinMeta.setKeyFields2(new String[]{});
                            StepMeta stepMeta = new StepMeta("MergeJoin", "Join table " + inputSteps.get(i+2).getName(), mergeJoinMeta);
                            stepMeta.setDraw(true);
                            stepMeta.setLocation(i*200+800, i*200+500+j);
                            transMeta.addStep(stepMeta);
                            mergeSteps.add(stepMeta);
                            TransHopMeta transHopMeta = new TransHopMeta(mergeSteps.get(i), stepMeta);
                            transMeta.addTransHop(transHopMeta);
                            transHopMeta = new TransHopMeta(inputSteps.get(i+2), stepMeta);
                            transMeta.addTransHop(transHopMeta);
                        }
                        
                        TableOutputMeta tableOutputMeta = new TableOutputMeta();
                        tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
                        tableOutputMeta.setTablename(table.getName());
                        tableOutputMeta.setSchemaName(kettleETLPanel.getSchemaName());
                        StepMeta stepMeta = new StepMeta("TableOutput", "Output to " + table.getName(), tableOutputMeta);
                        stepMeta.setDraw(true);
                        int k;
                        if (mergeSteps.isEmpty()) {
                            k = 400;
                        } else {
                            k = 800;
                        }
                        stepMeta.setLocation(i*200+k, i*200+500+j);
                        transMeta.addStep(stepMeta);
                        TransHopMeta transHopMeta = 
                            new TransHopMeta(mergeSteps.isEmpty()?inputSteps.get(0):mergeSteps.get(mergeSteps.size()-1), stepMeta);
                        if (!mergeSteps.isEmpty()) {
                            transMeta.addNote(new NotePadMeta("The final hop is disabled because the join types may need to be updated.",0,0,125,125));
                            transHopMeta.setEnabled(false);
                        }
                        transMeta.addTransHop(transHopMeta);
                        
                        tableMapping = new LinkedHashMap<SQLTable, StringBuffer>();
                        
                        String xml = transMeta.getXML();
                        String fileName = kettleETLPanel.getParentPath() + File.separator + 
                            "transformation_for_table_" + table.getName() + ".xml";
                        transMeta.setFilename(fileName);
                        transMeta.setName(table.getName());
                        try {
                            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));
                            dos.write(xml.getBytes("UTF-8"));
                            dos.close();
                        } catch (Exception er) {
                            ASUtils.showExceptionDialog("File " + fileName + " was not created", er);
                        }
                        logger.debug("Saved transformation to file: " + fileName);
                    }
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog("An error occurred reading from the tables for kettle", ex);
                }
                
                JobEntryCopy startEntry = new JobEntryCopy();
                JobEntrySpecial start = new JobEntrySpecial();
                start.setName("Start");
                start.setType(JobEntryInterface.TYPE_JOBENTRY_SPECIAL);
                start.setStart(true);
                startEntry.setEntry(start);
                startEntry.setLocation(10, 200);
                startEntry.setDrawn();
                jm.addJobEntry(startEntry);
                
                JobEntryCopy oldJobEntry = null;
                int i = 1;
                for (TransMeta transition : transitions) {
                    JobEntryCopy entry = new JobEntryCopy();
                    JobEntryTrans trans = new JobEntryTrans();
                    trans.setFileName(transition.getFilename());
                    trans.setName(transition.getName());
                    trans.setType(JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION);
                    entry.setEntry(trans);
                    entry.setLocation(i*200, 200);
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
                
                String xml = jm.getXML();
                String fileName = kettleETLPanel.getPath();
                try {
                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));
                    dos.write(xml.getBytes("UTF-8"));
                    dos.close();
                } catch (Exception er) {
                    ASUtils.showExceptionDialog("File " + fileName + " was not created", er);
                }
                logger.debug("Saved transformation to file: " + fileName);
            }
        };
        
        cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                
            }
        };
        
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                kettleETLPanel,
                ArchitectFrame.getMainInstance(),
                "Create a Kettle Job", "OK",
                okAction, cancelAction);
        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
        d.setVisible(true);

    }

}
