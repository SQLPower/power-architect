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
package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
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
import org.pentaho.di.core.exception.KettleException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.FileValidator;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.CreateKettleJobPanel;
import ca.sqlpower.architect.swingui.PromptingFileValidator;
import ca.sqlpower.architect.swingui.UserRepositoryDirectoryChooser;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;

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
                
                SPSwingWorker compareWorker = new SPSwingWorker(session) {

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
                            } else if (ex instanceof RuntimeException || ex instanceof IOException || ex instanceof SQLException) {
                                StringBuffer buffer = new StringBuffer();
                                buffer.append("An error occurred at runtime.").append("\n");
                                for(String task: kettleJob.getTasksToDo()) {
                                    buffer.append(task).append("\n");
                                }
                                ASUtils.showExceptionDialog(session, buffer.toString(), ex);
                            } else if (ex instanceof KettleException) {
                                ASUtils.showExceptionDialog(session, "An exception in Kettle occurred during the export process" +
                                        "\n" + ex.getMessage().trim(), ex);
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
                ProgressWatcher.watchProgress(progressBar, kettleJob);
            }
        };
        
        cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            }
        };
        
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                kettleETLPanel,
                session.getArchitectFrame(),
                "Create a Kettle Job", "OK",
                okAction, cancelAction);
        d.pack();
        d.setLocationRelativeTo(session.getArchitectFrame());
        d.setVisible(true);
    }
}
