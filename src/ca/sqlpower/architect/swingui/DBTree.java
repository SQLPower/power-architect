package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;

import ca.sqlpower.architect.*;

public class DBTree extends JTree {

	public DBTree(SQLDatabase root) throws ArchitectException {
		super(new DBTreeModel(root));
		setTransferHandler(new DBTreeTransferHandler());
		setDragEnabled(true);
	}

	/**
	 * Exports the SQLObject under the pointer in a DBTree.  If the
	 * tree contains non-SQLObject nodes, you'll get
	 * ClassCastExceptions.
	 */
	public class DBTreeTransferHandler extends TransferHandler {
		protected Transferable createTransferable(JComponent c) {
			TreePath p = ((JTree) c).getSelectionPath();
			SQLObject data = (SQLObject) p.getLastPathComponent();
			return new SQLObjectTransferable(data);
		}
    
		public int getSourceActions(JComponent c) {
			return COPY;
		}

		public boolean importData(JComponent c, Transferable t) {
			// TODO: import SQLObjects
			return false;
		}
        
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			// TODO: import SQLObjects
			return false;
		} 
	}
}
