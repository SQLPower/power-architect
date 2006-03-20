package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;

/**
 * Intended to be called periodically by a Swing Timer thread. Whenever the
 * actionPerformed method is called, it polls the lister for its job size
 * and current progress, then updates the given progress bar with that
 * information.
 */
public class ListerProgressBarUpdater implements ActionListener {
	
	private static final Logger logger = Logger.getLogger(ListerProgressBarUpdater.class);
	private JProgressBar bar;
	private Lister lister;

	public ListerProgressBarUpdater(JProgressBar bar, Lister lister) {
		this.bar = bar;
		this.lister = lister;
	}

	/**
	 * Must be invoked on the Event Dispatch Thread, most likely by a Swing
	 * Timer.
	 */
	public void actionPerformed(ActionEvent evt) {

		try {
			Integer max = lister.getJobSize(); // could take noticable time
												// to calculate job size
			bar.setVisible(true);
			if (max != null) {
				bar.setMaximum(max.intValue());
				bar.setValue(lister.getProgress());
				bar.setIndeterminate(false);
			} else {
				bar.setIndeterminate(true);
			}

			if (lister.isFinished()) {
				bar.setVisible(false);
				((javax.swing.Timer) evt.getSource()).stop();
			}
		} catch (ArchitectException e) {
			logger.error("getProgress failt", e);
		}
	}
}