package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import be.ibridge.kettle.trans.step.mergejoin.MergeJoinMeta;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CreateKettleJobPanel implements ArchitectPanel {
    
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
    
    private JDialog dbcsDialog;
    
    private SwingUIProject project;
    private File parentFile;
    
    public CreateKettleJobPanel(SwingUIProject project) {
        this.project = project;
        buildUI();
        panel.setVisible(true);
    }
    
    private void buildUI(){
        
        CreateKettleJobSettings settings = project.getCreateKettleJobSettings();
        panel.setLayout(new FormLayout());
        panel.setPreferredSize(new Dimension(450,200));
        
        nameField = new JTextField(settings.getJobName());
        databaseComboBox = new JComboBox();
        ASUtils.setupTargetDBComboBox(project, databaseComboBox);
        newDatabaseButton = new JButton();
        newDatabaseButton.setText("Properties...");
        newDatabaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ASUtils.showDbcsDialog(project, databaseComboBox);
            }
        });
        
        schemaName = new JTextField(settings.getSchemaName());
        
        filePath = new JTextField(settings.getFilePath());
        filePath.addKeyListener(new KeyListener(){
           public void keyPressed(KeyEvent arg0) {}
           public void keyReleased(KeyEvent arg0) {
               copyFilePath();
           }
           public void keyTyped(KeyEvent arg0) {}
           private void copyFilePath() {
               File file = new File(filePath.getText());
               transformationPath2.setText("     " + ((file==null || file.getParentFile()==null)?"":file.getParentFile().getPath()));
           }
        });
        browseFilePath = new JButton();
        browseFilePath.setText("Browse...");
        browseFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(project.getFile());
                chooser.addChoosableFileFilter(ASUtils.XML_FILE_FILTER);
                int response = chooser.showSaveDialog(panel);
                if (response != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    File file = chooser.getSelectedFile();
                    parentFile = file.getParentFile();
                    filePath.setText(file.getPath());
                    if (parentFile != null) {
                        transformationPath2.setText("     " + parentFile.getPath());
                    }
                }
            }
        });
        transformationPath = new JLabel("The transformations will be stored in:");
        transformationPath2 = new JLabel("     " + settings.getParentFile()==null?"":settings.getParentFile().getPath());

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
        builder.appendSeparator("Save Job to File");
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
        
    }
   
    public boolean applyChanges() {
        copySettingsToProject();
        return false;
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
    
    public String getParentPath() {
        if (parentFile == null) {
            return "";
        }
        return parentFile.getPath();
    }
    
    public String getSchemaName() {
        return schemaName.getText();
    }
    
    private void copySettingsToProject() {
        CreateKettleJobSettings settings = project.getCreateKettleJobSettings();
        settings.setJobName(nameField.getText());
        settings.setSchemaName(schemaName.getText());
        settings.setKettleJoinType(defaultJoinType.getSelectedIndex());
        settings.setFilePath(filePath.getText());
        settings.setParentFile(parentFile);
    }
    
}