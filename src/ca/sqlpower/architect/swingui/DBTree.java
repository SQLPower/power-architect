package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;

import ca.sqlpower.architect.*;

public class DBTree extends JTree implements DragSourceListener {

	protected DragSource ds;
	protected JPopupMenu popup;

	public DBTree(SQLDatabase root) throws ArchitectException {
		super(new DBTreeModel(root));
		ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer
			(this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());

		popup = new JPopupMenu();
		JMenuItem item = new JMenuItem("Properties");
		item.addActionListener(new PopupPropertiesListener());
		popup.add(item);
		addMouseListener(new PopupListener());
	}

	// ---------- methods of DragSourceListener -----------
	public void dragEnter(DragSourceDragEvent dsde) {
		//System.out.println("DBTree: got dragEnter event");
	}

	public void dragOver(DragSourceDragEvent dsde) {
		//System.out.println("DBTree: got dragOver event");
	}
	
	public void dropActionChanged(DragSourceDragEvent dsde) {
		//System.out.println("DBTree: got dropActionChanged event");
	}

	public void dragExit(DragSourceEvent dse) {
		//System.out.println("DBTree: got dragExit event");
	}
	
	public void dragDropEnd(DragSourceDropEvent dsde) {
		//System.out.println("DBTree: got dragDropEnd event");
	}

	/**
	 * Exports the SQLObject which was under the pointer in a DBTree
	 * when the drag gesture started.  If the tree contains
	 * non-SQLObject nodes, you'll get ClassCastExceptions.
	 */
 	public static class DBTreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			System.out.println("Drag gesture event: "+dge);

			// we only start drags on left-click drags
			InputEvent ie = dge.getTriggerEvent();
			if ( (ie.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
				return;
			}

			DBTree t = (DBTree) dge.getComponent();
  			TreePath[] p = t.getSelectionPaths();

			if (p ==  null || p.length == 0) {
				// nothing to export
				return;
			} else if (p.length == 1) {
				// export single node
				System.out.println("DBTree: exporting single node");
				SQLObject data = (SQLObject) p[0].getLastPathComponent();
				dge.getDragSource().startDrag
					(dge, DragSource.DefaultCopyNoDrop, new SQLObjectTransferable(data), t);
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

	/**
	 * A simple mouse listener that activates the DBTree's popup menu
	 * when the user right-clicks (or some other platform-specific action).
	 *
	 * @author The Swing Tutorial (Sun Microsystems, Inc.)
	 */
	class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
				TreePath p = getPathForLocation(e.getX(), e.getY());
				if (p == null) return;
				setSelectionPath(p);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }

	/**
	 * The PopupPropertiesListener responds to the "Properties" item
	 * in the popup menu.  It determines which item in the tree is
	 * currently selected, then (creates and) shows its properties window.
	 */
	class PopupPropertiesListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			TreePath p = getSelectionPath();
			if (p == null) {
				return;
			}
			SQLObject so = (SQLObject) p.getLastPathComponent();
			if (so instanceof SQLDatabase) {
				JFrame propWindow = DBCSPanel.createFrame(((SQLDatabase) so).getConnectionSpec());
				propWindow.setVisible(true);
			} else if (so instanceof SQLCatalog) {
			} else if (so instanceof SQLSchema) {
			} else if (so instanceof SQLTable) {
			} else if (so instanceof SQLColumn) {
			}
		}
	}
}
