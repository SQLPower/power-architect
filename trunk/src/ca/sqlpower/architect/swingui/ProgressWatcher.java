package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

public class ProgressWatcher implements ActionListener {
	private JProgressBar bar = null;
	private Monitorable worker = null;
	private JLabel label = null;

	private static final Logger logger = Logger.getLogger(ProgressWatcher.class);

	public ProgressWatcher (JProgressBar bar, Monitorable worker) {
		this (bar,worker,null);
	}

	public ProgressWatcher (JProgressBar bar, Monitorable worker, JLabel label) {
		this.bar = bar;
		this.worker = worker;
		this.label = label;
	}

	public void actionPerformed(ActionEvent evt) {
		// update the progress bar
		try {
			int max = worker.getJobSize();
			if (label != null) {
				label.setVisible(true);
			}
			bar.setVisible(true);
			bar.setMaximum(max);
			bar.setValue(worker.getProgress());
			bar.setIndeterminate(false);
			if ( worker.isFinished() ) {
				if (label != null) {
					label.setVisible(false);
				}
				bar.setVisible(false);
				((javax.swing.Timer)evt.getSource()).stop();
			}
		} catch ( ArchitectException e ) {
			logger.error("ProgressWatcher Problem.", e);
		}							
	}
}	
