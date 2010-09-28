package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.sqlobject.SQLDatabase;

/**
 * The DBCSPropertiesAction determines which database connection in the tree is
 * currently selected, then shows its properties window.
 */
public class DataSourcePropertiesAction extends AbstractAction {

    private final ArchitectSwingSession session;

    public DataSourcePropertiesAction(ArchitectSwingSession session) {
		super(Messages.getString("DBTree.dbcsPropertiesActionName")); //$NON-NLS-1$
        this.session = session;
	}

	public void actionPerformed(ActionEvent e) {
        TreePath p = session.getSourceDatabases().getSelectionPath();
        if (p == null) {
            return;
        }
        Object[] pathArray = p.getPath();
        int ii = 0;
        SQLDatabase sd = null;
        while (ii < pathArray.length && sd == null) {
            if (pathArray[ii] instanceof SQLDatabase) {
                sd = (SQLDatabase) pathArray[ii];
            }
            ii++;
        }
        if (sd != null) {
            ASUtils.showDbcsDialog(session.getArchitectFrame(), sd.getDataSource(), null);
        }
    }
}