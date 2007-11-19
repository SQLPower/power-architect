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
    package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.pentaho.di.trans.steps.mergejoin.MergeJoinMeta;

import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.sql.SPDataSource;
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
        panel.setPreferredSize(new Dimension(450,300));
        
        nameField = new JTextField(settings.getJobName());
        databaseComboBox = new JComboBox();
        ASUtils.setupTargetDBComboBox(session, databaseComboBox);
        newDatabaseButton = new JButton();
        newDatabaseButton.setText("Properties...");
        newDatabaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(panel);
                ASUtils.showTargetDbcsDialog(parentWindow, session, databaseComboBox);
            }
        });
        
        schemaName = new JTextField(settings.getSchemaName());
        
        saveFileRadioButton = new JRadioButton("Save Job to File", settings.isSavingToFile());
        
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
                   transformationPath.setText("     " + ((parentFile == null || parentFile.getPath() == null)?"":parentFile.getPath()));
               }
           }
        });
        browseFilePath = new JButton();
        browseFilePath.setText("Browse...");
        browseFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(session.getProject().getFile());
                chooser.addChoosableFileFilter(SPSUtils.XML_FILE_FILTER);
                int response = chooser.showSaveDialog(panel);
                if (response != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    File file = chooser.getSelectedFile();
                    File parentFile = file.getParentFile();
                    filePath.setText(file.getPath());
                    if (parentFile != null) {
                        transformationPath.setText("     " + parentFile.getPath());
                    }
                }
            }
        });
        
        File parentFile = new File(settings.getFilePath()).getParentFile();
        if (settings == null || parentFile == null || parentFile.getPath() == null) {
            transformationPath = new JLabel("");
        } else {
            transformationPath = new JLabel("     " + parentFile.getPath());
        }
        
        saveReposRadioButton = new JRadioButton("Save Job to Repository", !settings.isSavingToFile());

        Object[] connectionArray = session.getContext().getConnections().toArray();
        reposDB = new JComboBox(connectionArray);
        if (connectionArray.length > 0) {
            reposDB.setSelectedIndex(0);
        }
        reposPropertiesButton = new JButton();
        reposPropertiesButton.setText("Properties...");
        reposPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(panel);
                ASUtils.showDbcsDialog(parentWindow, (SPDataSource)reposDB.getSelectedItem(), null);
            }
        });
        
        ButtonGroup saveByButtonGroup = new ButtonGroup();
        saveByButtonGroup.add(saveFileRadioButton);
        saveByButtonGroup.add(saveReposRadioButton);
        
        defaultJoinType = new JComboBox();
        for (int joinType = 0; joinType < MergeJoinMeta.join_types.length; joinType++) {
            defaultJoinType.addItem(MergeJoinMeta.join_types[joinType]);
        }
        defaultJoinType.setSelectedIndex(settings.getKettleJoinType());
        
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref, 4dlu," + //1-4
                "0:grow, 4dlu, pref", //5-7
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, panel);
        builder.nextColumn(2);
        builder.append("Job Name: ");
        builder.append(nameField, 3);
        builder.nextLine();
        builder.append("");
        builder.append("Target Database: ");
        builder.append(databaseComboBox);
        builder.append(newDatabaseButton);
        builder.nextLine();
        builder.append("");
        builder.append("Schema Name: ");
        builder.append(schemaName, 3);
        builder.nextLine();
        builder.append("");
        builder.append("Default Join Type: ");
        builder.append(defaultJoinType);
        builder.nextLine();
        builder.append("");
        builder.append(saveFileRadioButton, 3);
        builder.nextLine();
        builder.append("");
        builder.append("Path: ");
        builder.append(filePath);
        builder.append(browseFilePath);
        builder.nextLine();
        builder.append("");
        builder.append("");
        JLabel transPathLabel = new JLabel("The transformations will be stored in:");
        builder.append(transPathLabel, 3);
        builder.nextLine();
        builder.append("");
        builder.append("");
        builder.append(transformationPath, 3);
        builder.nextLine();
        builder.append("");
        builder.append(saveReposRadioButton, 3);
        builder.nextLine();
        builder.append("");
        builder.append("Repository: ");
        builder.append(reposDB);
        builder.append(reposPropertiesButton);
        builder.nextLine();
        builder.append("");
        //TODO use CompareDM to check if the target database and the playpen are the same
        JLabel check = new JLabel("Check that the target database is the same as the play pen.");
        builder.append(check, 5);
        
    }
   
    /**
     * Copies the settings to the project and verifies that the Job name is not empty and the file path is not
     * empty if the job is to be saved to a file.
     */
    public boolean applyChanges() {
        copySettingsToProject();
        if (nameField.getText().equals("")) {
            JOptionPane.showMessageDialog(panel, "The job name was not set.\nThe Kettle job was not created.");
            return false;
        } else if (filePath.getText().equals("") && saveFileRadioButton.isSelected()) {
            JOptionPane.showMessageDialog(panel, "The job path was not set.\n The Kettle job was not created.");
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
        logger.debug("Saving settings to the project...");
        KettleJob settings = session.getKettleJob();
        settings.setJobName(nameField.getText());
        settings.setSchemaName(schemaName.getText());
        settings.setKettleJoinType(defaultJoinType.getSelectedIndex());
        settings.setFilePath(filePath.getText());
        settings.setRepository((SPDataSource)reposDB.getSelectedItem());
        settings.setSavingToFile(isSaveFile());
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return false;
    }
}