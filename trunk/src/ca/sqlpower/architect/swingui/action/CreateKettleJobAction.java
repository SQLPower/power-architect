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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.FileValidator;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import ca.sqlpower.architect.swingui.CreateKettleJobPanel;
import ca.sqlpower.architect.swingui.ProgressWatcher;
import ca.sqlpower.architect.swingui.PromptingFileValidator;
import ca.sqlpower.architect.swingui.UserRepositoryDirectoryChooser;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CreateKettleJobAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(CreateKettleJobAction.class);
    
    private ArchitectFrame architectFrame;
    
    public CreateKettleJobAction(ArchitectSwingSession session) {
        super(session, "Create Kettle Job...", "Create a new Kettle job");
        architectFrame = session.getArchitectFrame();
        putValue(SHORT_DESCRIPTION, "Create a Kettle Job");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        
        JDialog d;
        final JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        final CreateKettleJobPanel kettleETLPanel = new CreateKettleJobPanel(session);

        Action okAction, cancelAction;
        okAction = new AbstractAction() {
            
            public void actionPerformed(ActionEvent evt) {
                if (!kettleETLPanel.applyChanges()) {
                    return;
                }
                FileValidator validator = new PromptingFileValidator(architectFrame);
                KettleRepositoryDirectoryChooser chooser = new UserRepositoryDirectoryChooser(architectFrame);
                final CreateKettleJob kettleJob = session.getCreateKettleJob();
                kettleJob.setFileValidator(validator);
                kettleJob.setRepositoryDirectoryChooser(chooser);
                
                final JDialog createKettleJobMonitor = new JDialog(architectFrame);
                createKettleJobMonitor.setTitle("Creating Kettle Job");
                FormLayout layout = new FormLayout("pref", "");
                DefaultFormBuilder builder = new DefaultFormBuilder(layout);
                builder.setDefaultDialogBorder();
                builder.append("Creating Kettle Job");
                builder.nextLine();
                JProgressBar progressBar = new JProgressBar();
                builder.append(progressBar);
                builder.nextLine();
                JButton cancel = new JButton("Cancel");
                cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        kettleJob.setCancelled(true);
                    }
                });
                builder.append(cancel);
                createKettleJobMonitor.add(builder.getPanel());
                
                ArchitectSwingWorker compareWorker = new ArchitectSwingWorker() {

                    @Override
                    public void doStuff() throws Exception {
                        createKettleJobMonitor.pack();
                        createKettleJobMonitor.setLocationRelativeTo(architectFrame);
                        createKettleJobMonitor.setVisible(true);
                        List<SQLTable> tableList = session.getPlayPen().getTables();
                        kettleJob.doExport(tableList, session.getPlayPen().getDatabase());
                    }

                    @Override
                    public void cleanup() throws Exception {
                        createKettleJobMonitor.dispose();
                        if (getDoStuffException() != null) {
                            Exception ex = getDoStuffException();
                            if (ex instanceof ArchitectException) {
                                ASUtils.showExceptionDialog(session, "An error occurred reading from the tables for kettle", ex);
                            } else if (ex instanceof RuntimeException || ex instanceof IOException) {
                                ASUtils.showExceptionDialog(session, kettleJob.getTasksToDo().toString(), ex);
                            } else {
                                ASUtils.showExceptionDialog(session, "An unexpected error occurred during the export process", ex);
                            }
                            return;
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
                        builder.append("These items must be done before the Kettle job can be executed.");
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
                
                
                new Thread(compareWorker).start();
                new ProgressWatcher(progressBar, kettleJob);
            }
        };
        
        cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            }
        };
        
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                kettleETLPanel,
                session.getArchitectFrame(),
                "Create a Kettle Job", "OK",
                okAction, cancelAction);
        d.pack();
        d.setLocationRelativeTo(session.getArchitectFrame());
        d.setVisible(true);
    }
}
