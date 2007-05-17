package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.etl.PLExport;
import ca.sqlpower.architect.etl.PLUtils;
import ca.sqlpower.architect.swingui.event.DatabaseComboBoxListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PLExportXMLPanel extends JPanel implements ArchitectPanel {


    private static final Logger logger = Logger.getLogger(PLExportPanel.class);

    /**
     * This is the PLExport whose properties this panel edits.
     */
    protected PLExport plexp;

    private JProgressBar progressBar;
    private JLabel label;
    
    protected JTextField xmlFileName;
    
    protected JTextField plFolderName;
    protected JTextField plJobId;
    protected JTextField plJobDescription;
    protected JTextField plJobComment;

    protected DatabaseComboBoxListener dcl;
    protected DatabaseSelector repository;
    
    // Watch PL.INI for changes
    protected javax.swing.Timer timer;
    protected String plDotIniPath;
    
    protected Map ddlGeneratorMap;
    protected TextPanel mainForm;
    
    public PLExportXMLPanel() {


        progressBar = new JProgressBar();
        label = new JLabel();
        label.setText("");
        progressBar.setVisible(false);
        label.setVisible(false);
        
        repository = new DatabaseSelector(progressBar,label,this.getPanel());
        
        plFolderName = new JTextField();
        plJobId = new JTextField();
        plJobDescription = new JTextField();
        plJobComment = new JTextField();
        xmlFileName = new JTextField();

        FormLayout layout = new FormLayout("10dlu, 80dlu,10dlu, 5dlu,fill:100dlu:grow, 10dlu, 40dlu,30dlu", //Columns
        "4dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu, " + 
        "4dlu, 20dlu,1dlu, 20dlu, 1dlu, 20dlu," +   
        "4dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu, 1dlu, 20dlu," +
        "4dlu, 20dlu,4dlu, 20dlu");
        
        PanelBuilder pb = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();     
        
        int row = 0;
        
        row = 2;
        
        pb.add(new JLabel("Repository Connection"), cc.xy(2,row,"r,c"));
        pb.add(repository.getConnectionsBox(), cc.xyw(4,row,2));
        pb.add(repository.getNewButton(), cc.xy(7,row));
        row += 2;
        pb.add(new JLabel("Repository Catalog"), cc.xy(2,row, "r,c"));       
        pb.add(repository.getCatalogBox(), cc.xyw(4,row,2));
        row += 2;
        pb.add(new JLabel("Repository Schema"), cc.xy(2,row, "r,c"));        
        pb.add(repository.getSchemaBox(), cc.xyw(4,row,2));
        
        row += 2;
        
        pb.add(new JLabel("PL Folder Name"), cc.xy(2,row, "r,c"));       
        pb.add(plFolderName, cc.xyw(4,row,2));
        
        row += 2;
        pb.add(new JLabel("PL Job Id"), cc.xy(2,row, "r,c"));        
        pb.add(plJobId, cc.xyw(4,row,2));
        row += 2;
        pb.add(new JLabel("PL Job Description"), cc.xy(2,row, "r,c"));       
        pb.add(plJobDescription, cc.xyw(4,row,2));
        row += 2;
        pb.add(new JLabel("PL Job Comment"), cc.xy(2,row, "r,c"));       
        pb.add(plJobComment, cc.xyw(4,row,2));       

        row += 4;
        pb.add(new JLabel("File Name"), cc.xy(2,row, "r,c"));       
        pb.add(xmlFileName, cc.xyw(4,row,2));  
        pb.add(new JButton(new AbstractAction(){

            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.addChoosableFileFilter(ASUtils.XML_FILE_FILTER);
                File file = null;
                
                while (true) {
                    
                    int response = chooser.showSaveDialog(PLExportXMLPanel.this);
                    if (response != JFileChooser.APPROVE_OPTION) {
                        return;
                    } else {
                        file = chooser.getSelectedFile();
                        if (!file.getPath().endsWith(".xml")) {
                            file = new File(file.getPath()+".xml");
                        }
                    
                        if (!file.exists())
                            break;
                        
                        response = JOptionPane.showConfirmDialog(
                                    PLExportXMLPanel.this,
                                    "The file\n\n"+file.getPath()+"\n\nalready exists. Do you want to overwrite it?",
                                    "File Exists", JOptionPane.YES_NO_OPTION);

                        if (response == JOptionPane.NO_OPTION)
                            continue;
                        else
                            break;
                    }
                }
                xmlFileName.setText(file.getPath());
            }
            
        }), cc.xy(6,row));
        
        pb.add(label, cc.xy(2,24, "r,c"));      
        pb.add(progressBar, cc.xyw(4,24,2));
            
        add(pb.getPanel());
    }
    

    /**
     * Sets a new PLExport object for this panel to edit.  All field
     * values will be updated to reflect the status of the given
     * PLExport object.
     */
    public void setPLExport(PLExport plexp) {
        this.plexp = plexp;

        repository.getConnectionsBox().setSelectedItem(plexp.getRepositoryDataSource());
        plFolderName.setText(plexp.getFolderName());
        plJobId.setText(plexp.getJobId());
        plJobDescription.setText(plexp.getJobDescription());
        plJobComment.setText(plexp.getJobComment());        
    }
    
    /**
     * Returns the PLExport object that this panel is editting.  Call
     * applyChanges() to update it to the current values displayed on
     * the panel.
     */
    public PLExport getPLExport() {
        return plexp;
    }

    
    // -------------------- ARCHITECT PANEL INTERFACE -----------------------

    /**
     * Converts the fields that contain PL Identifiers into the
     * correct format (using PLUtils.toPLIdentifier) and then sets all
     * the properties of plexp to their values in this panel's input
     * fields.
     */
    public boolean applyChanges() {

        logger.debug("Applying changes @ PLExportXMLPanel");

        // make sure user provided a PL Job Name
        if (plJobId.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "You have to specify the PowerLoader Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // make sure user provided a XML file Name
        if (xmlFileName.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "You have to specify the XML File Name.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // repository schema does not need to be set here, it is set in the 
        // the Architect Data Source!
        plJobId.setText(PLUtils.toPLIdentifier(plJobId.getText()));
        plexp.setJobId(plJobId.getText());
        plFolderName.setText(PLUtils.toPLIdentifier(plFolderName.getText()));
        plexp.setFolderName(plFolderName.getText());
        plexp.setJobDescription(plJobDescription.getText());
        plexp.setJobComment(plJobComment.getText());
        
        File file = new File(xmlFileName.getText());
        plexp.setFile(file);

        plexp.setRepositoryDataSource((ArchitectDataSource)repository.getConnectionsBox().getSelectedItem());
        if (repository.getSchemaBox().isEnabled()){
            plexp.setRepositorySchema((repository.getSchemaBox().getSelectedItem()).toString());
        }
        if (repository.getCatalogBox().isEnabled()){
            plexp.setRepositoryCatalog((repository.getCatalogBox().getSelectedItem()).toString());
        }

        if (plexp.getRepositoryDataSource() == null) {
            JOptionPane.showMessageDialog(this, "You have to select a Repository database from the list.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    /**
     * Does nothing right now.
     */
    public void discardChanges() {
        // nothing to discard
    }



    public JPanel getPanel() {
        return this;
    }
    
}





