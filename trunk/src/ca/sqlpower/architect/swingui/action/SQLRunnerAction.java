package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.sqlrunner.ArchitectSQLRunnerConfigurationManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

import com.darwinsys.sql.ConfigurationManager;
import com.darwinsys.sql.SQLRunnerGUI;

/**
 * Invoke the SQLRunner functionality
 */
public class SQLRunnerAction extends AbstractArchitectAction {

    private static final Logger logger = Logger.getLogger(SQLRunnerAction.class);

    static ConfigurationManager configManager;

    public SQLRunnerAction(ArchitectSwingSession session) {
        super(session, "SQL Runner...", "Run SQL directly", "query");
        configManager = new ArchitectSQLRunnerConfigurationManager(session);
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("Showing SQLRunnerGUI");
        new SQLRunnerGUI(new ArchitectSQLRunnerConfigurationManager(session),"Power*Architect SQLRunner"); // Sets itself visible; this is all we need here.
    }

}