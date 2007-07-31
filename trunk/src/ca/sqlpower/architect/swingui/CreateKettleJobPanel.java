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

import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CreateKettleJobPanel implements DataEntryPanel {
    
    private static Logger logger = Logger.getLogger(CreateKettleJobPanel.class);

    private JPanel panel = new JPanel();
    private JTextField nameField;
    private JComboBox databaseComboBox;
    private JButton newDatabaseButton;
    private JTextField schemaName;
    private JTextField filePath;
    private JButton browseFilePath;
    private JComboBox defaultJoinType;
    private JLabel transformationPath;
    private JLabel transformationPath2;
    private JRadioButton saveFileRadioButton;
    private JRadioButton saveReposRadioButton;
    private ButtonGroup saveByButtonGroup;
    private JButton reposPropertiesButton;
    private JComboBox reposDB;
    
    
    private final SwingUIProject project;
    private final ArchitectSwingSession session;

    
    public CreateKettleJobPanel(ArchitectSwingSession session) {
        this.project = session.getProject();
        this.session = session;
        buildUI();
        panel.setVisible(true);
    }
    
    private void buildUI(){
        
        CreateKettleJob settings = session.getCreateKettleJob();
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
                   transformationPath2.setText("     " + ((parentFile == null || parentFile.getPath() == null)?"":parentFile.getPath()));
               }
           }
        });
        browseFilePath = new JButton();
        browseFilePath.setText("Browse...");
        browseFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(project.getFile());
                chooser.addChoosableFileFilter(SPSUtils.XML_FILE_FILTER);
                int response = chooser.showSaveDialog(panel);
                if (response != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    File file = chooser.getSelectedFile();
                    File parentFile = file.getParentFile();
                    filePath.setText(file.getPath());
                    if (parentFile != null) {
                        transformationPath2.setText("     " + parentFile.getPath());
                    }
                }
            }
        });
        transformationPath = new JLabel("The transformations will be stored in:");
        File parentFile = new File(settings.getFilePath()).getParentFile();
        if (settings == null || parentFile == null || parentFile.getPath() == null) {
            transformationPath2 = new JLabel("");
        } else {
            transformationPath2 = new JLabel("     " + parentFile.getPath());
        }
        
        saveReposRadioButton = new JRadioButton("Save Job to Repository", !settings.isSavingToFile());

        Object[] connectionArray = session.getUserSettings().getConnections().toArray();
        reposDB = new JComboBox(connectionArray);
        if (connectionArray.length > 0) {
            reposDB.setSelectedIndex(0);
        }
        reposPropertiesButton = new JButton();
        reposPropertiesButton.setText("Properties...");
        reposPropertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(panel);
                ASUtils.showDbcsDialog(parentWindow, session, (SPDataSource)reposDB.getSelectedItem(), null);
            }
        });
        
        saveByButtonGroup = new ButtonGroup();
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
        builder.append(transformationPath, 3);
        builder.nextLine();
        builder.append("");
        builder.append("");
        builder.append(transformationPath2, 3);
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
    
    private void copySettingsToProject() {
        CreateKettleJob settings = session.getCreateKettleJob();
        settings.setJobName(nameField.getText());
        settings.setSchemaName(schemaName.getText());
        settings.setKettleJoinType(defaultJoinType.getSelectedIndex());
        settings.setFilePath(filePath.getText());
        settings.setRepository((SPDataSource)reposDB.getSelectedItem());
        settings.setSavingToFile(isSaveFile());
    }
}