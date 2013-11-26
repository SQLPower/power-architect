package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;

/**
 * When invoked, this action creates a new data source and pops up the
 * connection properties dialog to edit the new data source.
 * <p>
 * If the dialog is not canceled, the new data source will be added to the db
 * tree as well as the session context's data source collection (pl.ini).
 */
public class NewDataSourceAction extends AbstractAction {

    private final ArchitectSwingSession session;

    public NewDataSourceAction(ArchitectSwingSession session) {
		super(Messages.getString("DBTree.newDbcsActionName")); //$NON-NLS-1$
        this.session = session;
	}

	public void actionPerformed(ActionEvent e) {
        final DataSourceCollection plDotIni = session.getDataSources();
        final JDBCDataSource dataSource = new JDBCDataSource(plDotIni);
        Runnable onAccept = new Runnable() {
            public void run() {
                plDotIni.addDataSource(dataSource);
                session.getDBTree().addSourceConnection(dataSource);
            }
        };
        dataSource.setDisplayName(Messages.getString("DBTree.newConnectionName")); //$NON-NLS-1$
        ASUtils.showDbcsDialog(session.getArchitectFrame(), dataSource, onAccept);
	}
}