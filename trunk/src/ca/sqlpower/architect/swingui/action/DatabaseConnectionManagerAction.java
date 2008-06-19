package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class DatabaseConnectionManagerAction extends AbstractArchitectAction  {
    
    public DatabaseConnectionManagerAction(ArchitectSwingSession session) {
        super(session, "Database Connection Manager...", "Database Connection Manager", "database_connect");
    }

    public void actionPerformed(ActionEvent e) {
        session.getContext().showConnectionManager(session.getArchitectFrame());
    }

}
