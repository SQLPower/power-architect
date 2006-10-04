package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SearchReplace;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class SearchReplaceAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(SearchReplaceAction.class);
    
    /**
     * The PlayPen instance that owns this Action.
     */
    protected PlayPen pp;
    
    /**
     * The DBTree instance that is associated with this Action.
     */
    protected DBTree dbt;
    
    public SearchReplaceAction() {
        super("Find/Replace...",
                ASUtils.createJLFIcon("general/Find",
                        "Find/Replace",
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        putValue(SHORT_DESCRIPTION, "Find/Replace");
    }
    
    public void actionPerformed(ActionEvent evt) {
    	logger.debug(getValue(SHORT_DESCRIPTION) + ": started");
        SearchReplace sr = new SearchReplace();
        sr.showSearchDialog(pp);
    }
    
    public void setPlayPen(PlayPen playpen) {
        pp = playpen;
    }
    
    public void setDBTree(DBTree dbTree) {
        dbt = dbTree;
    }
    
}
