package ca.sqlpower.architect.swingui.action;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;

public class InsertColumnAction extends AbstractTableTargetedAction {
	private static final Logger logger = Logger.getLogger(InsertColumnAction.class);
	
	public InsertColumnAction() {
		super("New Column",
			  ASUtils.createIcon("NewColumn",
								 "New Column",
								 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "New Column");
		putValue(ACTION_COMMAND_KEY, ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		setEnabled(false);
	}



    void processSQLObject(SQLObject so) throws ArchitectException {
        SQLTable st = null;
        int idx = 0;
        if (so instanceof SQLTable) {
        	logger.debug("user clicked on table, so we shall try to add a column to the end of the table.");
        	st = (SQLTable) so;
        	idx = st.getColumnsFolder().getChildCount();
        	logger.debug("SQLTable click -- idx set to: " + idx);						
        } else if (so instanceof SQLColumn) {
        	// iterate through the column list to figure out what position we are in...
        	logger.debug("trying to determine insertion index for table.");
        	SQLColumn sc = (SQLColumn) so;
        	st = sc.getParentTable();
        	idx = st.getColumnIndex(sc);
        	if (idx == -1)  {
        		// not found
        		logger.debug("did not find column, inserting at start of table.");
        		idx = 0;
        	}
        } else {
        	idx = 0;
        }
        st.addColumn(idx, new SQLColumn());
        EditColumnAction editColumnAction = new EditColumnAction();
        editColumnAction.makeDialog(st, idx);
        
    }

    void processTablePane(TablePane tp) throws ArchitectException {
        int idx = tp.getSelectedColumnIndex();
        
        if (idx < 0) idx = tp.getModel().getColumnsFolder().getChildCount();
        
        tp.getModel().addColumn(idx, new SQLColumn());
        EditColumnAction editColumnAction = new EditColumnAction();
        editColumnAction.makeDialog(tp.getModel(), idx);
    }

    @Override
    public void disableAction() {
        setEnabled(false);
        logger.debug("Disabling Insert Column Action");
        putValue(SHORT_DESCRIPTION, "Insert Column");
    }

}
