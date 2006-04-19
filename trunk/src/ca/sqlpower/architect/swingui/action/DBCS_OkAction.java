/**
 * 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBCSPanel;

public final class DBCS_OkAction extends AbstractAction {
	private final static Logger logger = Logger.getLogger(DBCS_OkAction.class);
	private final DBCSPanel dbcsPanel;
	private JDialog newConnectionDialog;
	private boolean isNew;
	
		
	public DBCS_OkAction(DBCSPanel dbcsPanel, boolean isNew) {
		super("Ok");
		this.dbcsPanel = dbcsPanel;
		this.isNew = isNew;
	}
	
	public void actionPerformed(ActionEvent e) {
		logger.debug(getValue(SHORT_DESCRIPTION) + " started");
		dbcsPanel.applyChanges();
		ArchitectDataSource newDS = dbcsPanel.getDbcs();
		if (isNew) {
			if ("".equals(newDS.getName().trim())) {
				JOptionPane.showMessageDialog(newConnectionDialog,"A connection must have at least 1 character that is not whitespace");
				newConnectionDialog.setVisible(true);
			} else {
				PlDotIni plDotIni = ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni();
				if (plDotIni.getDataSource(newDS.getName()) == null )  {
					plDotIni.addDataSource(newDS);

				} else {
					JOptionPane.showMessageDialog(newConnectionDialog,"A connection with the name \""+newDS.getName()+"\" already exists");
					newConnectionDialog.setVisible(true);
				}
			}
			
		} 
		
	}
	
	/**
	 * If you set the connection dialog this action will hide the dialog passed in on a success.
	 */
	public void setConnectionDialog(JDialog newConnectionDialog) {
		this.newConnectionDialog = newConnectionDialog;
	}
}