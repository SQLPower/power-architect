package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.architect.FileValidator;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PromptingFileValidator implements FileValidator {

    /**
     * The dialog for the overwrite/don't overwrite window
     */
    JDialog confirmDialog;
    
    /**
     * The parent component that the modal dialog will always be above
     */
    JFrame parent;
    
    /**
     * The response that will be sent back to the user
     */
    FileValidationResponse response;
    
    public PromptingFileValidator(JFrame parent) {
        this.parent = parent;
    }
    
    public FileValidationResponse acceptFile(File f) {
        String fileName = f.getName();
        confirmDialog = new JDialog(parent);
        confirmDialog.setTitle("Overwrite");
        
        JPanel confirmPanel = new JPanel();
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref:grow, 2dlu, 10dlu"
                                                , "");
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, confirmPanel);
        builder.setDefaultDialogBorder();
        String message1 = "The file " + fileName + " already exists";
        String message2 ="Do you wish to overwrite it?";
        builder.nextColumn(2);
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
        JButton cancel = new JButton("Cancel");
        buttonBar.addGlue();
        buttonBar.addGridded(overwrite);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(overwriteAll);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(dontOverwrite);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(dontOverwriteAll);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(cancel);
        buttonBar.addGlue();
        overwrite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                response = FileValidationResponse.WRITE_OK;
                confirmDialog.dispose();
            }
        });
        overwriteAll.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                response = FileValidationResponse.WRITE_OK_ALWAYS;
                confirmDialog.dispose();
            }
        });
        dontOverwrite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                response = FileValidationResponse.WRITE_NOT_OK;
                confirmDialog.dispose();
            }
        });
        dontOverwriteAll.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                response = FileValidationResponse.WRITE_NOT_OK_ALWAYS;
                confirmDialog.dispose();
            }
        });
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                response = FileValidationResponse.CANCEL;
                confirmDialog.dispose();
            }
        });
        builder.append("");
        builder.append(buttonBar.getPanel());
        confirmDialog.setModal(true);
        confirmDialog.add(builder.getPanel());
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(parent);
        confirmDialog.setVisible(true);
        
        return response;
        
    }

}
