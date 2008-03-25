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
package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

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
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.KettleJobPanel;
import ca.sqlpower.architect.swingui.UserRepositoryDirectoryChooser;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class KettleJobAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(KettleJobAction.class);
    
    private ArchitectFrame architectFrame;
    
    public KettleJobAction(ArchitectSwingSession session) {
        super(session, "Create Kettle Job...", "Create a new Kettle job");
        architectFrame = session.getArchitectFrame();
        putValue(SHORT_DESCRIPTION, "Create a Kettle Job");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        logger.debug("Starting to create a Kettle job.");
        
        JDialog d;
        final JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        final KettleJobPanel kettleETLPanel = new KettleJobPanel(session);

        Callable<Boolean> okCall, cancelCall;
        okCall = new Callable<Boolean>() {
            
            public Boolean call() {
                if (!kettleETLPanel.applyChanges()) {
                    return new Boolean(false);
                }
                KettleRepositoryDirectoryChooser chooser = new UserRepositoryDirectoryChooser(architectFrame);
                final KettleJob kettleJob = session.getKettleJob();
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
                        kettleJob.doExport(tableList, session.getTargetDatabase());
                    }

                    @Override
                    public void cleanup() throws Exception {
                        createKettleJobMonitor.dispose();
                        if (getDoStuffException() != null) {
                            Throwable ex = getDoStuffException();
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
                return new Boolean(true);
            }
        };
        
        cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                return new Boolean(true);
            }
        };
        
        d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                kettleETLPanel,
                session.getArchitectFrame(),
                "Create a Kettle Job", "OK",
                okCall, cancelCall);
        d.pack();
        d.setLocationRelativeTo(session.getArchitectFrame());
        d.setVisible(true);
    }
}
