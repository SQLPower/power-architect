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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.sql.JDBCDataSource;
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
    private JComboBox databaseComboBox;
    
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
     * This field allows the user to specify an absolute path to where the file should be
     * saved for FileBasedRepository.
     */
    private JTextField fileBasedPath;
    
    /**
     * This button brings up a JFileChooser to allow the user to select a 'File based repository' (folder) to save
     * the XML file output to.
     */
    private JButton browseFileBasedPath;
    
    /**
     * The label that shows the user the path to the file based repository to let them know that the 
     * transformations will be stored in the same place. This label is defined here as it
     * is updated by inline classes.
     */
    private JLabel fileBasedTransPathLabel;

    /**
     * The default join type to set Kettle joins to if they are required in a transformation.
     */
    private JComboBox defaultJoinType;
    
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
     * The radio button that denoted the Job will be saved to a 'File based repository'.
     */
    private JRadioButton saveFileBasedRadioButton;
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
    private JComboBox reposDB;
    
    /**
     * The session that we will get the play pen from to create a Kettle job and transformations
     * for.
     */
    private final ArchitectSwingSession session;
    
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
        KettleJob settings = session.getKettleJob();
        panel.setLayout(new FormLayout());
        panel.setPreferredSize(new Dimension(500,450));
        
        nameField = new JTextField(settings.getJobName());
        databaseComboBox = new JComboBox();
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
        
        saveFileRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.saveJobToFileOption"), settings.isSavingToFile()); //$NON-NLS-1$
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
        // save to FileBasedRepository
        saveFileBasedRadioButton  = new JRadioButton(Messages.getString("KettleJobPanel.saveJobToFileBasedOption"), settings.isSavingToFileBased()); //$NON-NLS-1$
        fileBasedPath = new JTextField(settings.getFileBasedPath());

        fileBasedPath.getDocument().addDocumentListener(new DocumentListener(){
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
                if (fileBasedPath.getText() != null && !fileBasedPath.getText().isEmpty()) {
                    File repoPath = new File(fileBasedPath.getText());
                    if (repoPath != null) { 
                        fileBasedTransPathLabel.setText("     " + ((repoPath.getPath() == null)?"":repoPath.getPath())); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        });
        browseFileBasedPath = new JButton();
        browseFileBasedPath.setText(Messages.getString("KettleJobPanel.browseButton")); //$NON-NLS-1$
        browseFileBasedPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // 'saveAs' dialog has a built in 'New Folder' button, while 'Open' dialog doesn't.
                // so here we are using 'saveAs' dialog instead of 'Open' dialog, which allows user to create a 
                // 'New Folder' if user want to save a job to a new folder.
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                chooser.setApproveButtonText("Select");
                File  currentDir = new java.io.File(System.getProperty("user.home")+"/Desktop");//new java.io.File(".");
                if (fileBasedPath.getText() != null && !fileBasedPath.getText().isEmpty()) {
                    if (new File(fileBasedPath.getText()).isDirectory()) {
                        currentDir = new File(fileBasedPath.getText());
                    }
                }
                chooser.setCurrentDirectory(currentDir);
                chooser.setDialogTitle("Select Repository Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // Hiding the 'saveAs' panel, as we are using 'saveAs' dialog to open the folder.
                ArrayList<JPanel> jpanels = new ArrayList<JPanel>();
                for(Component c : chooser.getComponents()){
                    if( c instanceof JPanel ){
                        jpanels.add((JPanel)c);
                    }
                }

                jpanels.get(0).getComponent(0).setVisible(false);
                int response = chooser.showSaveDialog(panel);
                if (response != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    saveFileBasedRadioButton.setSelected(true);
                    File dir = chooser.getSelectedFile();
                    if (!dir.exists()) {
                        dir = dir.getParentFile();
                    }
                    if (dir != null) {
                        fileBasedTransPathLabel.setText("     " + dir.getPath()); //$NON-NLS-1$
                        fileBasedPath.setText(dir.getPath());
                    }
                }
            }
        });

        File fileBased = new File(settings.getFileBasedPath());
        if (settings == null || fileBased == null || fileBased.getPath() == null) {
            fileBasedTransPathLabel = new JLabel(""); //$NON-NLS-1$
        } else {
            fileBasedTransPathLabel = new JLabel("     " + fileBased.getPath()); //$NON-NLS-1$
        }
    
        //save to DatabaseRepository
        saveReposRadioButton = new JRadioButton(Messages.getString("KettleJobPanel.saveJobToRepositoryOption"), !(settings.isSavingToFile() || settings.isSavingToFileBased())); //$NON-NLS-1$

        Object[] connectionArray = session.getContext().getConnections().toArray();
        reposDB = new JComboBox(connectionArray);
        if (connectionArray.length > 0) {
            reposDB.setSelectedIndex(0);
        }
        reposDB.addItemListener(new ItemListener(){
          @Override
          public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED && !saveReposRadioButton.isSelected()) {
                  saveReposRadioButton.setSelected(true);
              }
          }
        });

        reposPropertiesButton = new JButton();
        reposPropertiesButton.setText(Messages.getString("KettleJobPanel.propertiesButton")); //$NON-NLS-1$
        reposPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveReposRadioButton.setSelected(true);
                Window parentWindow = SPSUtils.getWindowInHierarchy(panel);
                ASUtils.showDbcsDialog(parentWindow, (JDBCDataSource)reposDB.getSelectedItem(), null);
            }
        });
        
        ButtonGroup saveByButtonGroup = new ButtonGroup();
        saveByButtonGroup.add(saveFileRadioButton);
        saveByButtonGroup.add(saveFileBasedRadioButton);
        saveByButtonGroup.add(saveReposRadioButton);
        
        defaultJoinType = new JComboBox();
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
        builder.append(Messages.getString("KettleJobPanel.defaultJoinTypeLabel")); //$NON-NLS-1$
        builder.append(defaultJoinType);
        builder.nextLine();
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
        builder.append(saveFileBasedRadioButton, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(Messages.getString("KettleJobPanel.pathLabel")); //$NON-NLS-1$
        builder.append(fileBasedPath);
        builder.append(browseFileBasedPath);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(""); //$NON-NLS-1$
        JLabel transPathLabel2 = new JLabel(Messages.getString("KettleJobPanel.transfomationsPathLabel")); //$NON-NLS-1$
        builder.append(transPathLabel2, 3);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
        builder.append(""); //$NON-NLS-1$
        builder.append(fileBasedTransPathLabel, 3);
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
     * Copies the settings to the project and verifies that the Job name is not empty and the file path is not
     * empty if the job is to be saved to a file.
     */
    public boolean applyChanges() {
        copySettingsToProject();
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
    
    public boolean isSaveFileBased() {
        return saveFileBasedRadioButton.isSelected();
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
        settings.setFileBasedPath(fileBasedPath.getText());
        settings.setSavingToFileBased(isSaveFileBased());
        session.getWorkspace().commit();
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }
}