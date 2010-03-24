package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;

/**
 * An action that removes the currently-selected source database connection from
 * the project.
 */
public class RemoveSourceDBAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(RemoveSourceDBAction.class);
    
	/**
     * The tree this action removes source connections from.
     */
    private final DBTree tree;

    public RemoveSourceDBAction(DBTree tree) {
		super(Messages.getString("DBTree.removeDbcsActionName")); //$NON-NLS-1$
        this.tree = tree;
	}

	public void actionPerformed(ActionEvent arg0) {
		TreePath tp = tree.getSelectionPath();
		if (tp == null) {
			JOptionPane.showMessageDialog(tree, Messages.getString("DBTree.noItemsSelected"), Messages.getString("DBTree.noItemsSelectedDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (! (tp.getLastPathComponent() instanceof SQLDatabase) ) {
			JOptionPane.showMessageDialog(tree, Messages.getString("DBTree.selectionNotADb"), Messages.getString("DBTree.selectionNotADbDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (tree.isTargetDatabaseNode(tp)) {
			JOptionPane.showMessageDialog(tree, Messages.getString("DBTree.cannotRemoveTargetDb"), Messages.getString("DBTree.cannotRemoveTargetDbDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

        // Historical note: we used to check here if there were any objects in the
        // play pen that depend on any children of this database before agreeing
        // to remove it. Now this is handled by a listener in the PlayPen itself.
        
		SQLDatabase selection = (SQLDatabase) tp.getLastPathComponent();
		SQLObject root = (SQLObject) tree.getModel().getRoot();
		
		try {
		    if (root.removeChild(selection)) {
		        selection.disconnect();
		    } else {
		        logger.error("root.removeChild(selection) returned false!"); //$NON-NLS-1$
		        JOptionPane.showMessageDialog(tree, Messages.getString("DBTree.deleteConnectionFailed"), Messages.getString("DBTree.deleteConnectionFailedDialogTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		    }
		} catch (IllegalArgumentException e) {
		    throw new RuntimeException(e);
		} catch (ObjectDependentException e) {
		    throw new RuntimeException(e);
		}
	}
}