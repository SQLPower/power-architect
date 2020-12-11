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
    package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The KettleJobPanel class created a panel for user input to set properties
 * required in creating a Kettle job. The settings on the panel are saved in the 
 * session's KettleJob object.
 */
public class KettleJobPanel implements DataEntryPanel {
    
    private static Logger logger = Logger.getLogger(KettleJobPanel.class);

    /**
     * The panel that this class will create for user input of Kettle settings.
     */
    private JPanel panel = new JPanel();
    
    /**
     * The field to allow users to specify a name for the Kettle job.
     */
    private JTextField nameField;
    
    /**
     * The combo box that lists all of the target databases. This is used to
     * select the database to copy all of the data to.
     */
    private JComboBox<JDBCDataSource> databaseComboBox;
    
    /**
     * This button brings up a new DBCS panel to make a new connection to select
     * for the target database.
     */
    private JButton newDatabaseButton;
    
    /**
     * This field allows the user to specify a schema if it is required for the database.
     */
    private JTextField schemaName;
    
    /**
     * checkBox used to exclude a column with timeStamp datatype
     */
    private JCheckBox timeStampCheckBox;
    
    /**
     * checkbox used to split the job
     */
    private JCheckBox splitJobCheckBox;
    
    /**
     * user can select/write the number to split the job
     */
    private JSpinner splitSpinner;
    
    /**
     * This field allows the user to specify an absolute path to where the file should be
     * saved if the kettle job is to be saved as a file in XML format.
     */
    private JTextField filePath;
    
    /**
     * This button brings up a JFileChooser to allow the user to select a location to save
     * the XML file output to.
     */
    private JButton browseFilePath;
    
    /**
     * The default join type to set Kettle joins to if they are required in a transformation.
     */
    private JComboBox<String> defaultJoinType;
    
    /**
     * The label that shows the user the path to the Job file to let them know that the 
     * transformations will be stored in the same place. This label is defined here as it
     * is updated by inline classes.
     */
    private JLabel transformationPath;
    
    /**
     * The radio button that denoted the Job will be saved to a file.
     */
    private JRadioButton saveFileRadioButton;
    
    /**
     * The radio button that denoted the Job will be saved to a repository.
     */
    private JRadioButton saveReposRadioButton;
    
    /**
     * This button opens a JDBC panel for editing the connection that will be used to
     * connect to the repository.
     */
    private JButton reposPropertiesButton;
    
    /**
     * This combo box holds all of the connections defined in the Architect so they can
     * be used as a repository connection.
     */
    private JComboBox<?> reposDB;
    
    /**
     * The radio button that denoted the output type is 'Table output'.
     */
    private JRadioButton tableOutputRadioButton;
    
    private ButtonGroup outputButtonGroup;
    /**
     * The radio button that denoted the output type is 'Insert/Update'.
     */
    private JRadioButton insertUpdateRadioButton;
    
    /**
     * The session that we will get the play pen from to create a Kettle job and transformations
     * for.
     */
    private final ArchitectSwingSession session;
    
    private List<SQLTable> tableList;
    
    private KettleJob settings;
    
    /**
     * This button opens a split job panel to select tables to split the job.
     */
    private JButton splitPropertiesButton;
    
    /**
     * This constructor creates a Kettle job panel and displays it to the user.
     * 
     * @param session
     *            The session we will be making a Kettle job for.
     */
    public KettleJobPanel(ArchitectSwingSession session) {
        this.session = session;
        buildUI();
        panel.setVisible(true);
    }
    
    /**
     * Sets the {@link #panel} to be a new panel that can be used by the user to define
     * Kettle job properties.
     */
    private void buildUI(){
        settings = session.getKettleJob();
        panel.setLayout(new FormLayout());
        panel.setPreferredSize(new Dimension(500,450));
        try {
           tableList = session.getPlayPen().getTables();
        } catch (SQLObjectException e1) {
             e1.printStackTrace();
        }
        nameField = new JTextField(settings.getJobName());
        databaseComboBox = new JComboBox<JDBCDataSource>();
        ASUtils.setupTargetDBComboBox(session, databaseComboBox);
        newDatabaseButton = new JButton();
        newDatabaseButton.setText(Messages.getString("KettleJobPanel.propertiesButton")); //$NON-NLS-1$
        newDatabaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SPSUtils.getWindowInHierarchy(panel);
                ASUtils.showTargetDbcsDialog(parentWindow, session, databaseComboBox);
            }
        });
        
        schemaName = new JTextField(settings.getSchemaName());
        //output type
        insertUpdateRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.inserUpdateOption"), settings.isInsertUpdate()); //$NON-NLS-1$
        tableOutputRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.tableOutputOption"), !settings.isInsertUpdate()); //$NON-NLS-1$
        saveFileRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.saveJobToFileOption"), settings.isSavingToFile()); //$NON-NLS-1$
        saveFileRadioButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                browseFilePath.setSelected(true);
                reposPropertiesButton.setEnabled(false);
                reposDB.setEditable(false);
            }
            
        });
        //saveto file(XML)
        filePath = new JTextField(settings.getFilePath());
        filePath.getDocument().addDocumentListener(new DocumentListener(){
           public void changedUpdate(DocumentEvent e) {
               copyFilePath();
           }
           public void insertUpdate(DocumentEvent e) {
               copyFilePath();
           }
           public void removeUpdate(DocumentEvent e) {
               copyFilePath();
           }
           private void copyFilePath() {
               File file = new File(filePath.getText());
               if (file != null) { 
                   File parentFile = file.getParentFile();
                   transformationPath.setText("     " + ((parentFile == null || parentFile.getPath() == null)?"":parentFile.getPath())); //$NON-NLS-1$ //$NON-NLS-2$
               }
           }
        });
        browseFilePath = new JButton();
        browseFilePath.setSelected(saveFileRadioButton.isSelected());
        browseFilePath.setText(Messages.getString("KettleJobPanel.browseButton")); //$NON-NLS-1$
        browseFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(session.getProjectLoader().getFile());
                chooser.addChoosableFileFilter(SPSUtils.XML_FILE_FILTER);
                File  currentDir = new java.io.File(System.getProperty("user.home")+"/Desktop");
               
                if (filePath.getText() != null && !filePath.getText().isEmpty()) {
                    File parentFile = new File(filePath.getText()).getParentFile();
                    if (parentFile.isDirectory()) {
                        currentDir = parentFile.getAbsoluteFile();
                    }
                }
                chooser.setCurrentDirectory(currentDir);
                int response = chooser.showSaveDialog(panel);
                if (response != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    saveFileRadioButton.setSelected(true);
                    saveReposRadioButton.setSelected(false);
                    File file = chooser.getSelectedFile();
                    File parentFile = file.getParentFile();
                    filePath.setText(file.getPath());
                    if (parentFile != null) {
                        transformationPath.setText("     " + parentFile.getPath()); //$NON-NLS-1$
                    }
                }
            }
        });
        
        File parentFile = new File(settings.getFilePath()).getParentFile();
        if (settings == null || parentFile == null || parentFile.getPath() == null) {
            transformationPath = new JLabel(""); //$NON-NLS-1$
        } else {
            transformationPath = new JLabel("     " + parentFile.getPath()); //$NON-NLS-1$
        }
        
        saveReposRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.saveJobToRepositoryOption"), !settings.isSavingToFile()); //$NON-NLS-1$

        Object[] connectionArray = session.getContext().getConnections().toArray();
        reposDB = new JComboBox<>(connectionArray);
        if (connectionArray.length > 0) {
            reposDB.setSelectedIndex(0);
        }
        reposPropertiesButton = new JButton();
        reposPropertiesButton.setEnabled(saveReposRadioButton.isSelected());
        reposPropertiesButton.setText(Messages.getString("KettleJobPanel.propertiesButton")); //$NON-NLS-1$
        reposPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveReposRadioButton.setSelected(true);
                Window parentWindow = SPSUtils.getWindowInHierarchy(panel);
                ASUtils.showDbcsDialog(parentWindow, (JDBCDataSource)reposDB.getSelectedItem(), null);
            }
        });
      
        saveReposRadioButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                reposPropertiesButton.setEnabled(true);
                reposDB.setEditable(true);
                browseFilePath.setSelected(false);
            }
            
        });

        outputButtonGroup = new ButtonGroup();
        outputButtonGroup.add(insertUpdateRadioButton);
        outputButtonGroup.add(tableOutputRadioButton);
        
        ButtonGroup saveByButtonGroup = new ButtonGroup();
        saveByButtonGroup.add(saveFileRadioButton);
        saveByButtonGroup.add(saveReposRadioButton);
        timeStampCheckBox = new JCheckBox(Messages.getString("KettleJobPanel.excludeTimeStamp"));
        // to split the main job into multiple inner job
        splitJobCheckBox = new JCheckBox(Messages.getString("KettleJobPanel.split"));
        splitSpinner =  new JSpinner(new SpinnerNumberModel(settings.getSplitJobNo(), 2, null, 1));
        splitSpinner.setEnabled(splitJobCheckBox.isSelected());
        splitPropertiesButton = new JButton();
        splitPropertiesButton.setEnabled(false);
        splitPropertiesButton.setText(Messages.getString("KettleJobPanel.splitPropertiesButton")); //$NON-NLS-1$
        splitPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SPSUtils.getWindowInHierarchy(panel);
                List<SQLTable> tableList = new ArrayList<>();
                SQLDatabase targetDB = session.getTargetDatabase();
                try {
                     tableList = session.getPlayPen().getTables();
                } catch (SQLObjectException e1) {
                    e1.printStackTrace();
                }
                if (!isValidSplitNo()) {
                    ((JSpinner.DefaultEditor) splitSpinner.getEditor()).getTextField().setFocusable(true);
                } else {
                    settings.setSplitJobNo((Integer)splitSpinner.getValue());
                    ASUtils.showSplitPropertiesDialog(tableList,settings,parentWindow);
                }
            }
        });
        splitJobCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractButton abstractButton = (AbstractButton) e.getSource();
                    splitSpinner.setEnabled(abstractButton.getModel().isSelected());
                    splitPropertiesButton.setEnabled(abstractButton.getModel().isSelected());
            }
            
        });
        JComponent editor = splitSpinner.getEditor();
        final JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setColumns(3);
        tf.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void focusLost(FocusEvent e) {
//                if (!isValidSplitNo()) {
//                    tf.setFocusable(true);
//                }
            }
            
        });
        splitSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
//                if (!isValidSplitNo()) {
//                    tf.setFocusable(true);
//                }
            }
        });
        defaultJoinType = new JComboBox<String>();
        for (int joinType = 0; joinType < MergeJoinMeta.join_types.length; joinType++) {
            defaultJoinType.addItem(MergeJoinMeta.join_types[joinType]);
        }
        defaultJoinType.setSelectedIndex(settings.getKettleJoinType());
        
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref, 4dlu," + //1-4 //$NON-NLS-1$
                "0:grow, 4dlu, pref", //5-7 //$NON-NLS-1$
                ""); //$NON-NLS-1$
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, panel);
        builder.nextColumn(2);
        builder.append(Messages.getString("KettleJobPanel.jobNameLabel")); //$NON-NLS-1$
        builder.append(nameField, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.targetDatabaseLabel")); //$NON-NLS-1$
        builder.append(databaseComboBox);
        builder.append(newDatabaseButton);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.schemaNameLabel")); //$NON-NLS-1$
        builder.append(schemaName, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(timeStampCheckBox, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(splitJobCheckBox, splitSpinner);
        builder.append(splitPropertiesButton);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.defaultJoinTypeLabel")); //$NON-NLS-1$
        builder.append(defaultJoinType);
        builder.nextLine();
        builder.appendSeparator();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.outputTypeLabel")); //$NON-NLS-1$
        builder.append(insertUpdateRadioButton);
        builder.append(tableOutputRadioButton);
        builder.nextLine();
        builder.appendSeparator();
        builder.append(""); //$NON-NLS-1$
        builder.append(saveFileRadioButton, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.pathLabel")); //$NON-NLS-1$
        builder.append(filePath);
        builder.append(browseFilePath);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(""); //$NON-NLS-1$
        JLabel transPathLabel = new JLabel(Messages.getString("KettleJobPanel.transfomationsPathLabel")); //$NON-NLS-1$
        builder.append(transPathLabel, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(""); //$NON-NLS-1$
        builder.append(transformationPath, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(saveReposRadioButton, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.repositoryLabel")); //$NON-NLS-1$
        builder.append(reposDB);
        builder.append(reposPropertiesButton);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        //TODO use CompareDM to check if the target database and the playpen are the same
        JLabel check = new JLabel(Messages.getString("KettleJobPanel.checkTargetSameAsPlaypenWarning")); //$NON-NLS-1$
        builder.append(check, 5);
        
    }
   /**
    * check if user entered a valid split number
    */
    private boolean isValidSplitNo() {
        if (splitJobCheckBox.isSelected() ) { 
            if( splitSpinner.getValue() instanceof Integer) {
                int splitno = (Integer)splitSpinner.getValue();
                if (tableList.size() <= splitno) {
                    JOptionPane.showMessageDialog(panel, "The split number must be greater then two or less then the number of tables in a PlayPen", "Invalid Split Number", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Enter a valid number to split the job", "Invalid Split Number", JOptionPane.WARNING_MESSAGE);  
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the settings to the project and verifies that the Job name is not empty and the file path is not
     * empty if the job is to be saved to a file.
     */
    public boolean applyChanges() {
        copySettingsToProject();
        if (!isValidSplitNo()) return false;
        if (nameField.getText().equals("")) { //$NON-NLS-1$
            JOptionPane.showMessageDialog(panel, Messages.getString("KettleJobPanel.jobNameNotSetError")); //$NON-NLS-1$
            return false;
        } else if (filePath.getText().equals("") && saveFileRadioButton.isSelected()) { //$NON-NLS-1$
            JOptionPane.showMessageDialog(panel, Messages.getString("KettleJobPanel.jobPathNotSetError")); //$NON-NLS-1$
            return false;
        }
        return true;
    }
    
    public void discardChanges() {
        //do nothing
    }

    public JComponent getPanel() {
        return panel;
    }
    
    public String getPath() {
        return filePath.getText();
    }
    
    public String getSchemaName() {
        return schemaName.getText();
    }
    
    public boolean isSaveFile() {
        return saveFileRadioButton.isSelected();
    }
    
    public boolean isSaveRepository() {
        return saveReposRadioButton.isSelected();
    }
        
    public int getDefaultJoinType() {
        return defaultJoinType.getSelectedIndex();
    }
    
    public String getJobName() {
        return nameField.getText();
    }
    
    /**
     * Copies the settings to the project by storing them in the KettleJob instance.
     */
    private void copySettingsToProject() {
        logger.debug("Saving settings to the project..."); //$NON-NLS-1$
        KettleJob settings = session.getKettleJob();
        session.getWorkspace().begin("Saving kettle settings");
        settings.setJobName(nameField.getText());
        settings.setSchemaName(schemaName.getText());
        settings.setKettleJoinType(defaultJoinType.getSelectedIndex());
        settings.setFilePath(filePath.getText());
        settings.setRepository((JDBCDataSource)reposDB.getSelectedItem());
        settings.setSavingToFile(isSaveFile());
        settings.setTimeStampExcluded(timeStampCheckBox.isSelected());
        settings.setSplittingJob(splitJobCheckBox.isSelected());
        settings.setIsInsertUpdate(insertUpdateRadioButton.isSelected());
        if(splitJobCheckBox.isSelected()) {
            settings.setSplitJobNo((Integer)splitSpinner.getValue());
        }
        session.getWorkspace().commit();
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }
}