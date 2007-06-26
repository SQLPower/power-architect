package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.DBConnectionCallBack;

/**
 * XXX Somebody should document what this class is useful for.
 */
public final class DBCSOkAction extends AbstractAction {
	private final static Logger logger = Logger.getLogger(DBCSOkAction.class);
	private final DBCSPanel dbcsPanel;
	private JDialog newConnectionDialog;
	private boolean isNew;
	private String oldName;
    private DBConnectionCallBack connectionSelectionCallBack;
    private DataSourceCollection plDotIni;

    /**
     * create the DBCSOkAction object, use the default pl.ini from the
     * ArchitectFrame
     * @param dbcsPanel
     * @param isNew
     */
    public DBCSOkAction(DBCSPanel dbcsPanel, ArchitectSwingSession session, boolean isNew) {
        this(dbcsPanel,
                isNew,
                session.getUserSettings().getPlDotIni());
	}


    /**
     * create a DBCSOkAction object, and use the pl.ini that passed in
     * instead of the one from ArchitectFrame
     * @param dbcsPanel
     * @param isNew
     * @param plDotIni
     */
	public DBCSOkAction(DBCSPanel dbcsPanel, boolean isNew, DataSourceCollection plDotIni) {
	    super("Ok");
	    this.dbcsPanel = dbcsPanel;
	    this.isNew = isNew;
	    if (!isNew) {
	        oldName = dbcsPanel.getDbcs().getName();
	    } else {
	        oldName = null;
	    }
        this.plDotIni = plDotIni;
    }


    public void actionPerformed(ActionEvent e)  {
		logger.debug("DBCS Action invoked");
		ArchitectDataSource newDS = dbcsPanel.getDbcs();
		String curName = dbcsPanel.getDbNameFieldContents();
		if (curName == null) {
			throw new ArchitectRuntimeException(new ArchitectException("DBCS Panel improperly intialized"));
		}

		if (isNew) {
			dbcsPanel.applyChanges();
			if ("".equals(newDS.getName().trim())) {
				JOptionPane.showMessageDialog(newConnectionDialog,"A connection name must have at least 1 character that is not whitespace");
				newConnectionDialog.setVisible(true);
			} else {
				if (plDotIni.getDataSource(newDS.getName()) == null )  {
					plDotIni.addDataSource(newDS);
                    if (connectionSelectionCallBack != null) {
                        connectionSelectionCallBack.selectDBConnection(newDS);
                    }

				} else {
					JOptionPane.showMessageDialog(newConnectionDialog,"A connection with the name \""+curName+"\" already exists");
					newConnectionDialog.setVisible(true);
				}
			}

		} else if ("".equals(curName.trim())) {
			JOptionPane.showMessageDialog(newConnectionDialog,"A connection name must have at least 1 character that is not whitespace");
			newConnectionDialog.setVisible(true);
		} else if (curName.equals(oldName)) {
			logger.debug("The current Name is the same as the old name");
			dbcsPanel.applyChanges();
		} else {
			ArchitectDataSource dataSource = plDotIni.getDataSource(curName);
			if (dataSource == null )  {
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

	public DBConnectionCallBack getConnectionSelectionCallBack() {
	    return connectionSelectionCallBack;
	}

	public void setConnectionSelectionCallBack(DBConnectionCallBack callBack) {
	    this.connectionSelectionCallBack = callBack;
	}
}