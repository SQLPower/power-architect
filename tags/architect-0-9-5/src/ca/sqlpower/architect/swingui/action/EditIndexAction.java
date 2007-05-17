package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
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
        final IndexEditPanel editPanel = new IndexEditPanel(index);
  
        d = ArchitectPanelBuilder.createArchitectPanelDialog(
                editPanel, ArchitectFrame.getMainInstance(),
                "Index Properties", "OK");
        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
        d.setVisible(true);
    }

    public void setPlayPen(PlayPen pp) {
        this.pp = pp;
    }

    public void setDBTree(DBTree newDBT) {
        this.dbt = newDBT;
    }
}
