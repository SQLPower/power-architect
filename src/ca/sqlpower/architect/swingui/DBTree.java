package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;

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
  			TreePath[] p = t.getSelectionPaths();

			if (p.length == 1) {
				// export single node
				System.out.println("DBTree: exporting single node");
				SQLObject data = (SQLObject) p[0].getLastPathComponent();
				dge.getDragSource().startDrag
					(dge, DragSource.DefaultCopyNoDrop, new SQLObjectTransferable(data), t);
			} else if (p.length == 0) {
				// nothing to export
				return;
			} else {
				// export list of nodes
				System.out.println("DBTree: exporting list of nodes");
				SQLObject[] nodes = new SQLObject[p.length];
				for (int i = 0; i < p.length; i++) {
					nodes[i] = (SQLObject) p[i].getLastPathComponent();
				}
				dge.getDragSource().startDrag
					(dge, 
					 DragSource.DefaultCopyNoDrop, 
					 new SQLObjectListTransferable(nodes), 
					 t);
			}
 		}
	}
}
