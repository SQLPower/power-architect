package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;
import java.awt.event.*;
import javax.swing.*;
import org.apache.log4j.Logger;

public class ProgressWatcher implements ActionListener {
	private JProgressBar bar = null;
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

	public void actionPerformed(ActionEvent evt) {
		// update the progress bar
		try {
			Integer jobSize = monitorable.getJobSize();
			if (jobSize == null) {
				bar.setIndeterminate(true);
			} else {
				bar.setIndeterminate(false);
				bar.setMaximum(jobSize.intValue());
			}
			if (label != null) {
				label.setVisible(true);
			}
			bar.setVisible(true);
			bar.setValue(monitorable.getProgress());
			bar.setIndeterminate(false);
		} catch (ArchitectException e) {
			logger.error("Couldn't update progress bar (Monitorable threw an exception)", e);
		} finally {
			try {
				if (monitorable.isFinished()) {
					if (label != null) {
						label.setVisible(false);
					}
					bar.setVisible(false);
					timer.stop();
				}
			} catch (ArchitectException e1) {
				logger.error("Couldn't tell if Monitorable was finished (it threw an exception)", e1);
			}
		}
	}
}	
