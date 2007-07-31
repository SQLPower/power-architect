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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.event.TaskTerminationEvent;
import ca.sqlpower.swingui.event.TaskTerminationListener;
import ca.sqlpower.util.Monitorable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
/**
 * This class creates an action with a built in progress dialog.  
 * 
 * By default this class sets up a dialog with an indeterminate progress
 * bar.  The dialog automatically closes when the job finishes.
 */
public abstract class ProgressAction extends AbstractArchitectAction {
    /**
     * A simple bean monitor.  Used to allow the classes extending
     * ProgressAction to manipulate the progressbar.
     */
    public class ActionMonitor implements Monitorable {
        Integer jobSize = null;
        String message ="";
        int progress = 0;
        boolean started = false;
        boolean finished = false;
        boolean cancelled = false;

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public boolean hasStarted() {
            return started;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public Integer getJobSize() {
            return jobSize;
        }

        public void setJobSize(Integer jobSize) {
            this.jobSize = jobSize;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }

    }
    
    public ProgressAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        super(session, actionName, actionDescription, iconResourceName);
    }

    public ProgressAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription) {
        super(session, actionName, actionDescription);
    }
    
    /**
     * Setup the dialog, monitors and worker.  Classes that extend this class
     * should use doStuff() and cleanUp()
     */
    public void actionPerformed(ActionEvent e) {
        final JDialog progressDialog;
        final Map<String,Object> properties = new HashMap<String, Object>();
        progressDialog = new JDialog(frame, "Progress...", false);  
        progressDialog.setLocationRelativeTo(frame);
        progressDialog.setTitle(getDialogMessage());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("4dlu,fill:min(100dlu;default):grow, pref, fill:min(100dlu;default):grow,4dlu",
                "4dlu,pref,4dlu, pref, 6dlu, pref,4dlu"));
        JLabel label = new JLabel(getDialogMessage());
        JProgressBar progressBar = new JProgressBar();
        final ActionMonitor monitor = new ActionMonitor();
        if (!setup(monitor,properties)){
            // if setup indicates not to continue (returns false), then exit method
            return;
        }
        CellConstraints c = new CellConstraints();
        pb.add(label, c.xyw(2, 2, 3));
        pb.add(progressBar,c.xyw(2, 4, 3));
        pb.add(new JButton(new AbstractAction(getButtonText()) {
            public void actionPerformed(ActionEvent e) {
                progressDialog.dispose();
                monitor.setCancelled(true);
            }
        }),c.xy(3,6));
        progressDialog.add(pb.getPanel());
        
        ArchitectSwingWorker worker = new ArchitectSwingWorker(session) {
            @Override
            public void cleanup() throws Exception {
                ProgressAction.this.cleanUp(monitor);
                progressDialog.dispose();
            }

            @Override
            public void doStuff() throws Exception {
                ProgressAction.this.doStuff(monitor,properties);
            }
        };
        ProgressWatcher pw = new ProgressWatcher(progressBar,monitor);
        pw.addTaskTerminationListener(new TaskTerminationListener() {
            public void taskFinished(TaskTerminationEvent e) {
                progressDialog.dispose();
            }
        });
        progressDialog.pack();
        progressDialog.setVisible(true);
        
        new Thread(worker).start();
    }
    
    /**
     *  Return the text that shows up on the dialog button
     */
    public abstract String getButtonText();

    /**
     * Setup the monitor and doStuff parameters before the progress dialog is shown.
     * @return return true to continue with the action, return false to stop the action
     *          This could be used to respond to user choice to cancel the action
     */
    public abstract boolean setup(ActionMonitor monitor, Map<String, Object> properties);
    /**
     * doStuff replaces the actionPerformed() call
     */
    public abstract void doStuff(ActionMonitor monitor, Map<String, Object> properties);
    /**
     * Perform any cleanup code here 
     */
    public abstract void cleanUp(ActionMonitor monitor);
    /**
     * Gets the message that will be displayed on the progressbar
     * 
     * @return the message displayed on the dialog above the progressbar
     */
    public abstract String getDialogMessage();
}
