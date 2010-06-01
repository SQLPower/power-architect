package ca.sqlpower.architect;

import java.util.List;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectPreEvent;
import ca.sqlpower.sqlobject.SQLObjectPreEventListener;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

/**
 * Watches the session's root object, and reacts when SQLDatabase items
 * are removed. In that case, it ensures there are no dangling references
 * from the playpen database back to the removed database or its children.
 * If there are, the user is asked to decide to either cancel the operation
 * or allow the ETL lineage (SQLColumn.sourceColumn) references to be broken.
 */
public class SourceObjectIntegrityWatcher implements SQLObjectPreEventListener {

    private final ArchitectSession session;

    SourceObjectIntegrityWatcher(ArchitectSession session) {
        this.session = session;
    }

    public void dbChildrenPreRemove(SQLObjectPreEvent e) {
        UserPrompter up = session.createUserPrompter(
                Messages.getString("SourceObjectIntegrityWatcher.removingETLLineageWarning"), //$NON-NLS-1$
                UserPromptType.BOOLEAN, UserPromptOptions.OK_NOTOK_CANCEL, UserPromptResponse.OK, Boolean.TRUE, 
                Messages.getString("SourceObjectIntegrityWatcher.forgetLineageOption"), Messages.getString("SourceObjectIntegrityWatcher.keepSourceConnectionOption"),
                Messages.getString("cancel")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (SQLObject so : e.getChildren()) {
            SQLDatabase db = (SQLDatabase) so;
            // Setting source columns is a magical side effect, and the user
            // shouldn't be prompted if the change came from the server.
            if (db.isMagicEnabled()) {
                try {
                    List<SQLColumn> refs = SQLObjectUtils.findColumnsSourcedFromDatabase(session.getTargetDatabase(), db);
                    if (!refs.isEmpty()) {
                        UserPromptResponse response = up.promptUser(refs.size(), db.getName());
                        if (response == UserPromptResponse.OK) {
                            // disconnect those columns' source columns
                            for (SQLColumn col : refs) {
                                col.setSourceColumn(null);
                            }
                        } else if (response == UserPromptResponse.NOT_OK) {
                            e.veto();
                        } else if (response == UserPromptResponse.CANCEL) {
                            e.veto();
                        }
                    }
                } catch (SQLObjectException ex) {
                    throw new SQLObjectRuntimeException(ex);
                }
            }
        }
    }

}