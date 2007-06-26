package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.IndexEditPanel;

public class EditIndexAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(EditIndexAction.class);

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected final DBTree dbt; 

    
    public EditIndexAction(ArchitectSwingSession session) {
        super(session, "Index Properties...", "Index Properties", "IndexProperties");
        dbt = frame.getDbTree();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
            TreePath [] selections = dbt.getSelectionPaths();
            logger.debug("selections length is: " + selections.length);
            if (selections.length != 1) {
                JOptionPane.showMessageDialog(dbt, "To indicate which index you like edit, please select a single index header.");
            } else {
                TreePath tp = selections[0];
                SQLObject so = (SQLObject) tp.getLastPathComponent();
                SQLIndex si = null;

                if (so instanceof SQLIndex) {
                    logger.debug("user clicked on index, so we shall try to edit the index properties.");
                    si = (SQLIndex) so;
                    try {
                        makeDialog(si);
                    } catch (ArchitectException e) {
                        throw new ArchitectRuntimeException(e);
                    } 
                } else {
                    JOptionPane.showMessageDialog(dbt, "To indicate which index name you would like to edit, please select a single index header.");
                }
            }
        } else {
            // unknown action command source, do nothing
        }   
    }

    
    private void makeDialog(SQLIndex index) throws ArchitectException {
        final JDialog d;
        final IndexEditPanel editPanel = new IndexEditPanel(index, session);
  
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                editPanel, frame,
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }
}
