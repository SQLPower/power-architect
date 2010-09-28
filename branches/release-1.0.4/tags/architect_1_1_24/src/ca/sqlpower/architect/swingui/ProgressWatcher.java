package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.event.TaskTerminationEvent;
import ca.sqlpower.architect.swingui.event.TaskTerminationListener;

public class ProgressWatcher implements ActionListener {
	private JProgressBar bar = null;
	private ProgressMonitor pm = null;
	private Monitorable monitorable = null;
	private JLabel label = null;
	private Timer timer;
	
	private List taskTerminationListeners;
	
	private static final Logger logger = Logger.getLogger(ProgressWatcher.class);

	public ProgressWatcher() {
		taskTerminationListeners = new ArrayList();
	}
	
	
	public ProgressWatcher(JProgressBar bar, Monitorable monitorable) {
		this (bar,monitorable,null);
	}

	public ProgressWatcher(JProgressBar bar, Monitorable monitorable, JLabel label) {
		this();
		this.bar = bar;
		this.monitorable = monitorable;
		this.label = label;
		timer = new Timer(50, this);
		timer.start();
	}
	
	public ProgressWatcher (ProgressMonitor pm, Monitorable monitorable) {
		this();
		this.pm = pm;
		this.monitorable = monitorable;
		timer = new Timer(50, this);
		timer.start();
	}

	public void addTaskTerminationListener(TaskTerminationListener ttl) {
		taskTerminationListeners.add(ttl);
	}

	public void removeTaskTerminationListener(TaskTerminationListener ttl) {
		taskTerminationListeners.remove(ttl);
	}
	
	private void fireTaskFinished () {
		TaskTerminationEvent tte = new TaskTerminationEvent(this);
		Iterator it = taskTerminationListeners.iterator();
		while (it.hasNext()) {
			TaskTerminationListener ttl= (TaskTerminationListener) it.next();
			ttl.taskFinished(tte);
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		// update the progress bar
		logger.debug("updating progress bar...");
		try {
			Integer jobSize = monitorable.getJobSize();
			if (bar != null) {
				if (monitorable.hasStarted()) {
					if (jobSize == null) {
						bar.setIndeterminate(true);
					} else {
						bar.setIndeterminate(false);
						bar.setMaximum(jobSize.intValue());
					}
					bar.setVisible(true);
					bar.setValue(monitorable.getProgress());
					bar.setIndeterminate(false);
				}
			}
			
			if (label != null) {
				label.setVisible(true);
			}

			if (pm != null) { // using ProgressMonitor
				if (monitorable.hasStarted()) {					
					if (jobSize != null) {
						pm.setMaximum(jobSize.intValue());					
					}
					pm.setProgress(monitorable.getProgress());
					logger.debug("progress: " + monitorable.getProgress());
					pm.setNote(monitorable.getMessage());
				}
			}
		} catch (ArchitectException e) {
			logger.error("Couldn't update progress bar (Monitorable threw an exception)", e);
		} finally {
			try {				
				logger.debug("monitorable.isFinished():" + monitorable.isFinished());
				if (monitorable.isFinished()) {
					if (label != null) {
						label.setText("");
					}
					if (bar != null) {
						bar.setValue(0);
					}
					if (pm != null) {
						logger.debug("pm done, max was: " + pm.getMaximum());
						pm.close();
					}
					
					// fire a taskTerminationEvent
					fireTaskFinished();
					
					logger.debug("trying to stop timer thread...");
					timer.stop();
					logger.debug("did the timer thread stop???");
				}
			} catch (ArchitectException e1) {
				logger.error("Couldn't tell if Monitorable was finished (it threw an exception)", e1);
			}
		}
	}
}	
