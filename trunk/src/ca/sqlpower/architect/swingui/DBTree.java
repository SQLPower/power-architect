package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import ca.sqlpower.architect.*;

public class DBTree extends JTree implements DragSourceListener {

	protected DragSource ds;

	public DBTree(SQLDatabase root) throws ArchitectException {
		super(new DBTreeModel(root));
		ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer
			(this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());
	}

	// ---------- methods of DragSourceListener -----------
	public void dragEnter(DragSourceDragEvent dsde) {
		System.out.println("DBTree: got dragEnter event");
	}

	public void dragOver(DragSourceDragEvent dsde) {
		System.out.println("DBTree: got dragOver event");
	}
	
	public void dropActionChanged(DragSourceDragEvent dsde) {
		System.out.println("DBTree: got dropActionChanged event");
	}

	public void dragExit(DragSourceEvent dse) {
		System.out.println("DBTree: got dragExit event");
	}
	
	public void dragDropEnd(DragSourceDropEvent dsde) {
		System.out.println("DBTree: got dragDropEnd event");
	}

	/**
	 * Exports the SQLObject which was under the pointer in a DBTree
	 * when the drag gesture started.  If the tree contains
	 * non-SQLObject nodes, you'll get ClassCastExceptions.
	 */
 	public static class DBTreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			System.out.println("Drag gesture event: "+dge);
			DBTree t = (DBTree) dge.getComponent();
  			TreePath p = t.getSelectionPath();
  			SQLObject data = (SQLObject) p.getLastPathComponent();
  			dge.getDragSource().startDrag
				(dge, DragSource.DefaultCopyDrop, new SQLObjectTransferable(data), t);
 		}
	}
}
