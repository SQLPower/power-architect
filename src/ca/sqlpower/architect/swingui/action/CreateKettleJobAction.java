package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DepthFirstSearch;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.etl.kettle.KettleOptions;
import ca.sqlpower.architect.etl.kettle.KettleUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CreateKettleJobPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

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
            
            /**
             * The spacing between nodes in Kettle jobs and transformations
             */
            private final int spacing = 150;

            private static final int OVERWRITE_ALL = 0;
            private static final int OVERWRITE_ONE = 1;
            private static final int DONT_OVERWRITE_ONE = 2;
            private static final int DONT_OVERWRITE_ANY = 3;
            private int overwriteOption = -1;
            JDialog confirmDialog;
            
            public void actionPerformed(ActionEvent evt) {
                
                if (!kettleETLPanel.applyChanges()) {
                    return;
                }
                
                EnvUtil.environmentInit();
                LogWriter lw = LogWriter.getInstance();
                JobMeta jm = new JobMeta(lw);
                
                Map<SQLTable, StringBuffer> tableMapping;
                List<TransMeta> transformations = new ArrayList<TransMeta>();
                List<String> noTransTables = new ArrayList<String>();

                try {
                    // The depth-first search will do a topological sort of
                    // the target table graph, so parent tables will come before
                    // their children.
                    List<SQLTable> tableList = 
                        new DepthFirstSearch(architectFrame.getProject().getPlayPen().getTables()).getFinishOrder();
                    
                    Map<String, DatabaseMeta> databaseNames = new LinkedHashMap<String, DatabaseMeta>();
                    
                    for (SQLTable table: tableList) {
                        
                        TransMeta transMeta = new TransMeta();
                        tableMapping = new LinkedHashMap<SQLTable, StringBuffer>();
                        
                        ArchitectDataSource target = architectFrame.getProject().getPlayPen().getDatabase().getDataSource();
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
                                continue;
                            } else {
                                StringBuffer buffer = new StringBuffer();
                                buffer.append("There is no mapping for the column(s): ");
                                for (String noMapForCol: noMappingForColumn) {
                                    buffer.append(noMapForCol).append(" ");
                                }
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
                        mergeSteps = createMergeJoins(kettleETLPanel.getDefaultJoinType(), transMeta, inputSteps);
                        
                        TableOutputMeta tableOutputMeta = new TableOutputMeta();
                        tableOutputMeta.setDatabaseMeta(targetDatabaseMeta);
                        tableOutputMeta.setTablename(table.getName());
                        tableOutputMeta.setSchemaName(kettleETLPanel.getSchemaName());
                        StepMeta stepMeta = new StepMeta("TableOutput", "Output to " + table.getName(), tableOutputMeta);
                        stepMeta.setDraw(true);
                        stepMeta.setLocation((inputSteps.size()+1)*spacing, inputSteps.size()*spacing);
                        transMeta.addStep(stepMeta);
                        TransHopMeta transHopMeta = 
                            new TransHopMeta(mergeSteps.isEmpty()?inputSteps.get(0):mergeSteps.get(mergeSteps.size()-1), stepMeta);
                        if (!mergeSteps.isEmpty()) {
                            transMeta.addNote(new NotePadMeta("The final hop is disabled because the join types may need to be updated.",0,0,125,125));
                            transHopMeta.setEnabled(false);
                        }
                        transMeta.addTransHop(transHopMeta);
                        
                        transformations.add(transMeta);
                        
                        transMeta.setName(table.getName());
                        String fileName = kettleETLPanel.getParentPath() + File.separator + 
                                          "transformation_for_table_" + table.getName() + ".KTR";
                        transMeta.setFilename(fileName);
                        outputToXML(transMeta.getXML(), fileName);
                        logger.debug("Saved transformation to file: " + fileName);
                    }
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog("An error occurred reading from the tables for kettle", ex);
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
                
                String fileName = kettleETLPanel.getPath();
                if (!fileName.toUpperCase().endsWith(".KJB")) {
                    fileName += ".KJB";
                }
                jm.setName(kettleETLPanel.getJobName());
                jm.setFilename(fileName);
                outputToXML(jm.getXML(), fileName);
            }

            private DatabaseMeta addDatabaseConnection(Map<String, DatabaseMeta> databaseNames, ArchitectDataSource dataSource) {
                DatabaseMeta databaseMeta;
                if (!databaseNames.containsKey(dataSource.getName())) {
                    try {
                        databaseMeta = KettleUtils.createDatabaseMeta(dataSource);
                        try {
                            KettleOptions.testKettleDBConnection(databaseMeta);
                        } catch (KettleDatabaseException e) {
                            logger.error("Could not connect to the database " + dataSource.getName() + ".");
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(architectFrame, "Could not connect to the database "
                                                                            + dataSource.getName() + ".");
                        }
                    } catch (RuntimeException re) {
                        String databaseName = dataSource.getName();
                        logger.error("Could not create the database connection for " + databaseName + ".");
                        re.printStackTrace();
                        ASUtils.showExceptionDialog
                                        ("Could not create the database connection for " + databaseName + "."
                                         , re);
                        databaseMeta = null;
                    }
                    databaseNames.put(dataSource.getName(), databaseMeta);
                } else {
                    databaseMeta = databaseNames.get(dataSource.getName());
                }
                return databaseMeta;
            }
            
            private void outputToXML(String xml, String fileName) {
                try {
                    if (overwriteOption == DONT_OVERWRITE_ANY) {
                        return;
                    }
                    File outputFile = new File(fileName);
                    logger.debug("The file to output is " + fileName);
                    if (outputFile.exists()) {
                        if (overwriteOption == OVERWRITE_ALL) {
                            outputFile.delete();
                        } else {
                            confirmDialog = new JDialog(architectFrame);
                            
                            JPanel confirmPanel = new JPanel();
                            FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref:grow, 2dlu, 10dlu"
                                                                    , "");
                            DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, confirmPanel);
                            String message1 = "The file " + fileName + " already exists";
                            String message2 ="Do you wish to overwrite it?";
                            builder.nextColumn(2);
                            builder.append("");
                            builder.nextLine();
                            builder.append("");
                            builder.append(message1);
                            builder.nextLine();
                            builder.append("");
                            builder.append(message2);
                            builder.nextLine();
                            ButtonBarBuilder buttonBar = new ButtonBarBuilder();
                            JButton overwrite = new JButton("Overwrite");
                            JButton overwriteAll = new JButton("Overwrite All");
                            JButton dontOverwrite = new JButton("Don't Overwrite");
                            JButton dontOverwriteAll = new JButton("Don't Overwrite Any");
                            buttonBar.addGlue();
                            buttonBar.addGridded(overwrite);
                            buttonBar.addRelatedGap();
                            buttonBar.addGridded(overwriteAll);
                            buttonBar.addRelatedGap();
                            buttonBar.addGridded(dontOverwrite);
                            buttonBar.addRelatedGap();
                            buttonBar.addGridded(dontOverwriteAll);
                            buttonBar.addGlue();
                            overwrite.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    overwriteOption = OVERWRITE_ONE;
                                    confirmDialog.dispose();
                                }
                            });
                            overwriteAll.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    overwriteOption = OVERWRITE_ALL;
                                    confirmDialog.dispose();
                                }
                            });
                            dontOverwrite.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    overwriteOption = DONT_OVERWRITE_ONE;
                                    confirmDialog.dispose();
                                }
                            });
                            dontOverwriteAll.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    overwriteOption = DONT_OVERWRITE_ANY;
                                    confirmDialog.dispose();
                                }
                            });
                            builder.append("");
                            builder.append(buttonBar.getPanel());
                            builder.nextLine();
                            builder.append("");
                            confirmDialog.setModal(true);
                            confirmDialog.add(builder.getPanel());
                            confirmDialog.pack();
                            confirmDialog.setLocationRelativeTo(architectFrame);
                            confirmDialog.setVisible(true);
                            if (overwriteOption == OVERWRITE_ALL || overwriteOption == OVERWRITE_ONE) {
                                outputFile.delete();
                            } else {
                                return;
                            }
                        }
                    }
                    outputFile.createNewFile();
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new
                                                FileOutputStream(outputFile), "utf-8"));
                    out.write(xml);
                    out.flush();
                    out.close();
                } catch (Exception er) {
                    ASUtils.showExceptionDialog("File " + fileName + " was not created", er);
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
                }
                return mergeSteps;
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
