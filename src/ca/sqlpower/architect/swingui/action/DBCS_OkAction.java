/**
 * 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.TextPanel;

public final class DBCS_OkAction extends AbstractAction {
	private final static Logger logger = Logger.getLogger(DBCS_OkAction.class);
	private final DBCSPanel dbcsPanel;
	private JDialog newConnectionDialog;
	private boolean isNew;
	private String oldName;
	
		
	public DBCS_OkAction(DBCSPanel dbcsPanel, boolean isNew) {
		super("Ok");
		this.dbcsPanel = dbcsPanel;
		this.isNew = isNew;
		if (!isNew) {
			oldName = dbcsPanel.getDbcs().getName();
		} else {
			oldName = null;
		}
	}
	

	public void actionPerformed(ActionEvent e)  {
		ArchitectDataSource newDS = dbcsPanel.getDbcs();
		String curName = null;
		for (Component c : ((TextPanel)dbcsPanel.getComponents()[0]).getComponents()) {
			if ("dbNameField".equals(c.getName())){
				curName = ((JTextField) c).getText();
			}
		}
		
		if (curName == null ) {
			throw new ArchitectRuntimeException(new ArchitectException("DBCS Panel improperly intialized"));
		}
		
		if (isNew) {
			dbcsPanel.applyChanges();
			if ("".equals(newDS.getName().trim())) {
				JOptionPane.showMessageDialog(newConnectionDialog,"A connection must have at least 1 character that is not whitespace");
				newConnectionDialog.setVisible(true);
			} else {
				PlDotIni plDotIni = ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni();
				if (plDotIni.getDataSource(newDS.getName()) == null )  {
					plDotIni.addDataSource(newDS);

				} else {
					JOptionPane.showMessageDialog(newConnectionDialog,"A connection with the name \""+curName+"\" already exists");
					newConnectionDialog.setVisible(true);
				}
			}
			
		} else if (curName.equals(oldName)) {
			System.out.println("The current Name is the same as the old name");
			dbcsPanel.applyChanges();
		} else {
			PlDotIni plDotIni = ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni();
			if (plDotIni.getDataSource(newDS.getName()) == null )  {
				dbcsPanel.applyChanges();

			} else {
				JOptionPane.showMessageDialog(newConnectionDialog,"A connection with the name \""+curName+"\" already exists");
				newConnectionDialog.setVisible(true);
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