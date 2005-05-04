package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

public class ProgressWatcher implements ActionListener {
	private JProgressBar bar = null;
	private Monitorable monitorable = null;
	private JLabel label = null;

	private static final Logger logger = Logger.getLogger(ProgressWatcher.class);

	public ProgressWatcher (JProgressBar bar, Monitorable monitorable) {
		this (bar,monitorable,null);
	}

	public ProgressWatcher (JProgressBar bar, Monitorable monitorable, JLabel label) {
		this.bar = bar;
		this.monitorable = monitorable;
		this.label = label;
	}

	public void actionPerformed(ActionEvent evt) {
		// update the progress bar
		try {
			int max = monitorable.getJobSize();
			if (label != null) {
				label.setVisible(true);
			}
			bar.setVisible(true);
			bar.setMaximum(max);
			bar.setValue(monitorable.getProgress());
			bar.setIndeterminate(false);
			if ( monitorable.isFinished() ) {
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
