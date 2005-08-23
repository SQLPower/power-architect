package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

public class ProgressWatcher implements ActionListener {
	private JProgressBar bar = null;
	private ProgressMonitor pm = null;
	private Monitorable monitorable = null;
	private JLabel label = null;
	private Timer timer;
	
	private static final Logger logger = Logger.getLogger(ProgressWatcher.class);

	public ProgressWatcher(JProgressBar bar, Monitorable monitorable) {
		this (bar,monitorable,null);
	}

	public ProgressWatcher(JProgressBar bar, Monitorable monitorable, JLabel label) {
		this.bar = bar;
		this.monitorable = monitorable;
		this.label = label;
		timer = new Timer(50, this);
		timer.start();
	}
	
	public ProgressWatcher (ProgressMonitor pm, Monitorable monitorable) {
		this.pm = pm;
		this.monitorable = monitorable;
		timer = new Timer(50, this);
		timer.start();
	}

	public void actionPerformed(ActionEvent evt) {
		// update the progress bar
		logger.debug("updating progress bar...");
		try {
			Integer jobSize = monitorable.getJobSize();
			if (bar != null) {
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
			
			if (label != null) {
				label.setVisible(true);
			}

			if (pm != null) { // using ProgressMonitor
				if (jobSize != null) {
					pm.setMaximum(jobSize.intValue());					
				}
				pm.setProgress(monitorable.getProgress());
				logger.debug("progress: " + monitorable.getProgress());
				pm.setNote(monitorable.getMessage());
			}
		} catch (ArchitectException e) {
			logger.error("Couldn't update progress bar (Monitorable threw an exception)", e);
		} finally {
			try {				
				logger.debug("monitorable.isFinished():" + monitorable.isFinished());
				if (monitorable.isFinished()) {
					if (label != null) {
						label.setVisible(false);
					}
					if (bar != null) {
						bar.setVisible(false);
					}
					if (pm != null) {
						logger.debug("pm done, max was: " + pm.getMaximum());
						// pm.close();
					}
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
