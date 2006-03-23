package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
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
	
	private List <JComponent> enableDisableList;
	private List <JComponent> disableEnableList;
	private List <JComponent> visableInvisableList;
	private List <JComponent> invisableVisableList;

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
			if ( enableDisableList != null ) {
				for ( JComponent jc : enableDisableList )
					jc.setEnabled(true);
			}
			if ( visableInvisableList != null ) {
				for ( JComponent jc : visableInvisableList )
					jc.setVisible(true);
			}
			
			if ( disableEnableList != null ) {
				for ( JComponent jc : disableEnableList )
					jc.setEnabled(false);
			}
			if ( invisableVisableList != null ) {
				for ( JComponent jc : invisableVisableList )
					jc.setVisible(false);
			}
				
			
			if (max != null) {
				bar.setMaximum(max.intValue());
				bar.setValue(lister.getProgress());
				bar.setIndeterminate(false);
			} else {
				bar.setIndeterminate(true);
			}
			if (lister.isFinished()) {
				bar.setVisible(false);
				if ( enableDisableList != null ) {
					for ( JComponent jc : enableDisableList )
						jc.setEnabled(false);
				}
				if ( visableInvisableList != null ) {
					for ( JComponent jc : visableInvisableList )
						jc.setVisible(false);
				}
				if ( disableEnableList != null ) {
					for ( JComponent jc : disableEnableList )
						jc.setEnabled(true);
				}
				if ( invisableVisableList != null ) {
					for ( JComponent jc : invisableVisableList )
						jc.setVisible(true);
				}
				((javax.swing.Timer) evt.getSource()).stop();
			}
		} catch (ArchitectException e) {
			logger.error("getProgress failt", e);
		}
	}

	/**
	 * enable JComponents in the List when the process start
	 * then disable them after the process is done.  
	 */
	public void setEnableDisableList(List<JComponent> enableDisableList) {
		this.enableDisableList = enableDisableList;
	}


	/**
	 * set JComponents in the List to visable when the process start
	 * then set them back after the process is done.  
	 */
	public void setVisableInvisableList(List<JComponent> visableInvisableList) {
		this.visableInvisableList = visableInvisableList;
	}

	/**
	 * disable JComponents in the List when the process start
	 * then enable them after the process is done.  
	 */
	public void setDisableEnableList(List<JComponent> disableEnableList) {
		this.disableEnableList = disableEnableList;
	}

	/**
	 * set JComponents in the List to invisable when the process start
	 * then set them back after the process is done.  
	 */
	public void setInvisableVisableList(List<JComponent> invisableVisableList) {
		this.invisableVisableList = invisableVisableList;
	}
}