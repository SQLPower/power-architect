package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.FileValidator;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PromptingFileValidator implements FileValidator {

    /**
     * The dialog for the overwrite/don't overwrite window
     */
    private JDialog confirmDialog;
    
    /**
     * The parent component that the modal dialog will always be above
     */
    private JFrame parent;
    
    /**
     * The response that will be sent back to the user
     */
    private FileValidationResponse response;
    
    /**
     * The check box that decides if the decision should be applied to all of the
     * files that may need to be overwritten.
     */
    private final JCheckBox applyToAll;
    
    private static Logger logger = Logger.getLogger(PromptingFileValidator.class);
   
    public PromptingFileValidator(JFrame parent) {
        this.parent = parent;
        applyToAll = new JCheckBox("Apply my choice to all files.");
    }
    
    public FileValidationResponse acceptFile(String fileName, String path) {
        confirmDialog = new JDialog(parent);
        confirmDialog.setTitle("Overwrite");
        
        JPanel confirmPanel = new JPanel();
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref:grow, 2dlu, 10dlu"
                                                , "");
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, confirmPanel);
        builder.setDefaultDialogBorder();
        String fileNameMessage = fileName;
        String filePathMessage = "at " + path;
        String questionMessage = "already exists. Do you wish to overwrite it?";
        
        builder.nextColumn(2);
        builder.append(fileNameMessage);
        builder.nextLine();
        builder.append("");
        builder.append(filePathMessage);
        builder.nextLine();
        builder.append("");
        builder.append(questionMessage);
        builder.nextLine();
        
        builder.append("");
        builder.append(applyToAll);
        builder.nextLine();
        
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        JButton overwrite = new JButton("Overwrite");
        JButton dontOverwrite = new JButton("Don't Overwrite");
        JButton cancel = new JButton("Cancel");
        buttonBar.addGlue();
        buttonBar.addGridded(overwrite);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(dontOverwrite);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(cancel);
        buttonBar.addGlue();
        
        overwrite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (applyToAll.isSelected()) {
                    response = FileValidationResponse.WRITE_OK_ALWAYS;
                } else {
                    response = FileValidationResponse.WRITE_OK;
                }
                confirmDialog.dispose();
            }
        });
        dontOverwrite.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (applyToAll.isSelected()) {
                    response = FileValidationResponse.WRITE_NOT_OK_ALWAYS;
                } else {
                    response = FileValidationResponse.WRITE_NOT_OK;
                }
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

        Runnable promptUser = new Runnable() {
          public void run() {
            confirmDialog.pack();
            confirmDialog.setLocationRelativeTo(parent);
            confirmDialog.setVisible(true);
          }
        };

        if (SwingUtilities.isEventDispatchThread ()) {
          promptUser.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(promptUser);
            } catch (InterruptedException e) {
                ASUtils.showExceptionDialog("While queing the dialog's pack, setVisible and setLocation, we were interrupted", e);
            } catch (InvocationTargetException e) {
                ASUtils.showExceptionDialog("While queing the dialog's pack, setVisible and setLocation, an InvocationTargetException was thrown", e);
            }
        }        
        return response;
        
    }

}
