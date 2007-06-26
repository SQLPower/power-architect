package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.SearchReplace;

public class SearchReplaceAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(SearchReplaceAction.class);
    
    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt;
    
    public SearchReplaceAction(ArchitectSwingSession session) {
        super(session, "Find/Replace...", "Fine/Replace", "search_replace");
        dbt = frame.getDbTree();
    }
    
    public void actionPerformed(ActionEvent evt) {
    	logger.debug(getValue(SHORT_DESCRIPTION) + ": started");
        SearchReplace sr = new SearchReplace();
        sr.showSearchDialog(playpen);
    }
}
