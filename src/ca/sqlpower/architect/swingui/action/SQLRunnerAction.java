package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.sqlrunner.SQLRunnerGUI;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * Invoke the SQLRunner functionality
 */
public class SQLRunnerAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(SQLRunnerAction.class);

    protected ArchitectFrame architectFrame;

    public SQLRunnerAction(JFrame owner) {
        super("SQL Runner...",
                ASUtils.createIcon("SQLRunner", "SQL Runner", ArchitectFrame.getMainInstance()
                .getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        logger.debug("Creating SQLRunnerAction");
        architectFrame = ArchitectFrame.getMainInstance();
        putValue(SHORT_DESCRIPTION, "Run SQL directly");
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("Showing SQLRunnerGUI");
        new SQLRunnerGUI(); // Sets itself visible; this is all we need here.
    }

}