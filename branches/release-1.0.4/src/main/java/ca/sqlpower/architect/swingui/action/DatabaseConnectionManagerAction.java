package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class DatabaseConnectionManagerAction extends AbstractArchitectAction  {
    
    public DatabaseConnectionManagerAction(ArchitectSwingSession session) {
        super(session, Messages.getString("DatabaseConnectionManagerAction.name"), Messages.getString("DatabaseConnectionManagerAction.description"), "database_connect"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public DatabaseConnectionManagerAction(ArchitectFrame frame) {
        super(frame, Messages.getString("DatabaseConnectionManagerAction.name"), Messages.getString("DatabaseConnectionManagerAction.description"), "database_connect"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void actionPerformed(ActionEvent e) {
        getSession().showConnectionManager(getSession().getArchitectFrame());
    }
}
