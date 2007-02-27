package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.IndexEditPanel;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class EditIndexAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(EditIndexAction.class);

    /**
     * The PlayPen instance that owns this Action.
     */
    protected PlayPen pp;

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected DBTree dbt; 

    
    public EditIndexAction() {
        super("Index Properties...",
              ASUtils.createIcon("IndexProperties",
                                 "Index Properties",
                                 ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        putValue(SHORT_DESCRIPTION, "Index Properties");
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
                    makeDialog(si); 
                } else {
                    JOptionPane.showMessageDialog(dbt, "To indicate which index name you would like to edit, please select a single index header.");
                }
            }
        } else {
            // unknown action command source, do nothing
        }   
    }

    private JDialog d;
    
    private void makeDialog(SQLIndex index) {
        final IndexEditPanel editPanel = new IndexEditPanel(index);

        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                //We need to see if the operation is successful, if
                //successful, we close down the dialog, if not, we need 
                //to return the dialog (hence why it is setVisible(!success))
                boolean success = editPanel.applyChanges();
                // XXX: also apply changes on mapping tab                
                d.setVisible(!success);
            }
        };

        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                editPanel.discardChanges();
                // XXX: also discard changes on mapping tab
                d.setVisible(false);
            }
        };

        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                editPanel, ArchitectFrame.getMainInstance(),
                "Index Properties", "OK", okAction, cancelAction);

        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
        d.setVisible(true);
    }

    public void setPlayPen(PlayPen pp) {
        this.pp = pp;
    }

    public void setDBTree(DBTree newDBT) {
        this.dbt = newDBT;
        // do I need to add a selection listener here?
    }
}
