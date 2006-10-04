/**
 * 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.PlayPen;


public class CancelAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(CancelAction.class);
	/**
	 * 
	 */
	private PlayPen pp;
	
	public CancelAction(PlayPen cil) {
		setPlayPen(cil); // runner-up for the IOJCC 2006
	}
	
	public void actionPerformed(ActionEvent e) {
		this.pp.fireCancel();
		logger.debug("Fired cancel action");
	}
	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}
	
	
	
}