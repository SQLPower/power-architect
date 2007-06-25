package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.FileValidator;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CreateKettleJobPanel;
import ca.sqlpower.architect.swingui.PromptingFileValidator;
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
            
            public void actionPerformed(ActionEvent evt) {
                if (!kettleETLPanel.applyChanges()) {
                    return;
                }
                FileValidator validator = new PromptingFileValidator(architectFrame);
                CreateKettleJob kettleJob = architectFrame.getProject().getCreateKettleJob();
                kettleJob.setFileValidator(validator);
                try {
                    List<SQLTable> tableList = architectFrame.getProject().getPlayPen().getTables();
                    kettleJob.doExport(tableList, architectFrame.getProject().getPlayPen().getDatabase());
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog("An error occurred reading from the tables for kettle", ex);
                } catch (RuntimeException re) {
                    ASUtils.showExceptionDialog(kettleJob.getTasksToDo().toString(), re);
                } catch (IOException e) {
                    ASUtils.showExceptionDialog(kettleJob.getTasksToDo().toString(), e);
                }
                final JDialog toDoListDialog = new JDialog(architectFrame);
                toDoListDialog.setTitle("Kettle Job Tasks");
                FormLayout layout = new FormLayout("10dlu, 2dlu, fill:pref:grow, 12dlu", "pref, fill:pref:grow, pref");
                DefaultFormBuilder builder = new DefaultFormBuilder(layout);
                builder.setDefaultDialogBorder();
                ButtonBarBuilder buttonBarBuilder = new ButtonBarBuilder();
                JTextArea toDoList = new JTextArea(10, 60);
                toDoList.setEditable(false);
                List<String> tasksToDo = kettleJob.getTasksToDo();
                for (String task: tasksToDo) {
                    toDoList.append(task + "\n");
                }
                JButton close = new JButton("Close");
                close.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        toDoListDialog.dispose();
                    }
                });
                builder.nextColumn(2);
                builder.append("The Kettle job was created but these steps must still be completed manually.");
                builder.nextLine();
                builder.append("");
                builder.append(new JScrollPane(toDoList));
                builder.nextLine();
                builder.append("");
                buttonBarBuilder.addGlue();
                buttonBarBuilder.addGridded(close);
                buttonBarBuilder.addGlue();
                builder.append(buttonBarBuilder.getPanel());
                toDoListDialog.add(builder.getPanel());
                toDoListDialog.pack();
                toDoListDialog.setLocationRelativeTo(architectFrame);
                toDoListDialog.setVisible(true);
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
