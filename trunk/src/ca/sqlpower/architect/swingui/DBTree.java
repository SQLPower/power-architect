package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;
import ca.sqlpower.sql.DBConnectionSpec;

public class DBTree extends JTree implements DragSourceListener {
	private static Logger logger = Logger.getLogger(DBTree.class);

	protected DragSource ds;
	protected JPopupMenu popup;
	protected JDialog propDialog;
	protected DBCSPanel dbcsPanel;

	public DBTree() {
		setRootVisible(false);
		setShowsRootHandles(true);
		ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer
			(this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());

		setupPropDialog();
		popup = setupPopupMenu();
		addMouseListener(new PopupListener());
	}

	public DBTree(List initialDatabases) throws ArchitectException {
		this();
		setDatabaseList(initialDatabases);
	}

	public void setDatabaseList(List databases) throws ArchitectException {
		setModel(new DBTreeModel(databases));
	}

	public List getDatabaseList() {
		ArrayList databases = new ArrayList();
		TreeModel m = getModel();
		int dbCount = m.getChildCount(m.getRoot());
		for (int i = 0; i < dbCount; i++) {
			databases.add(m.getChild(m.getRoot(), i));
		}
		return databases;
	}

	/**
	 * Sets up the DBCS dialog window and its DBCSPanel instance.  You
	 * only need to call this once, and the constructor does that.
	 */
	protected void setupPropDialog() {
		propDialog = new JDialog(ArchitectFrame.getMainInstance(),
								 "Database Connection Properties");
		final JButton okButton = new JButton("Ok");
		final JButton cancelButton = new JButton("Cancel");
		
		final JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dbcsPanel = new DBCSPanel();
		
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.applyChanges();
					propDialog.dispose();
				}
			});
		southPanel.add(okButton);
		
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.discardChanges();
					propDialog.dispose();
				}
			});
		southPanel.add(cancelButton);
		
		JComponent cp = (JComponent) propDialog.getContentPane();
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		cp.setLayout(new BorderLayout(12,12));
		cp.add(southPanel, BorderLayout.SOUTH);
		cp.add(dbcsPanel, BorderLayout.CENTER);
		propDialog.pack();
	}

	/**
	 * Creates an integer array which holds the child indices of each
	 * node starting from the root which lead to node "node."
	 *
	 * @param node A node in this tree.
	 */
	public int[] getDnDPathToNode(SQLObject node) {
		DBTreeModel m = (DBTreeModel) getModel();
		SQLObject[] sop = m.getPathToNode(node);
		int[] dndp = new int[sop.length-1];
		SQLObject current = sop[0];
		for (int i = 1; i < sop.length; i++) {
			dndp[i-1] = m.getIndexOfChild(current, sop[i]);
			current = sop[i];
		}
		return dndp;
	}

	public SQLObject getNodeForDnDPath(int[] path) throws ArchitectException {
		SQLObject current = (SQLObject) getModel().getRoot();
		for (int i = 0; i < path.length; i++) {
			current = current.getChild(path[i]);
		}
		return current;
	}

	public int getRowForNode(SQLObject node) {
		DBTreeModel m = (DBTreeModel) getModel();
		TreePath path = new TreePath(m.getPathToNode(node));
		return getRowForPath(path);
	}

	// ---------- methods of DragSourceListener -----------
	public void dragEnter(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dragEnter event");
	}

	public void dragOver(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dragOver event");
	}
	
	public void dropActionChanged(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dropActionChanged event");
	}

	public void dragExit(DragSourceEvent dse) {
		logger.debug("DBTree: got dragExit event");
	}
	
	public void dragDropEnd(DragSourceDropEvent dsde) {
		logger.debug("DBTree: got dragDropEnd event");
	}

	/**
	 * Exports the SQLObject which was under the pointer in a DBTree
	 * when the drag gesture started.  If the tree contains
	 * non-SQLObject nodes, you'll get ClassCastExceptions.
	 */
 	public static class DBTreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			logger.info("Drag gesture event: "+dge);

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
			} else {
				// export list of DnD-type tree paths
				ArrayList paths = new ArrayList(p.length);
				for (int i = 0; i < p.length; i++) {
					paths.add(t.getDnDPathToNode((SQLObject) p[i].getLastPathComponent()));
				}
				logger.info("DBTree: exporting list of DnD-type tree paths");
				dge.getDragSource().startDrag
					(dge, 
					 null, //DragSource.DefaultCopyNoDrop, 
					 new DnDTreePathTransferable(paths),
					 t);
			}
 		}
	}

	// ----------------- popup menu stuff ----------------

	protected JPopupMenu setupPopupMenu() {
		JPopupMenu newMenu = new JPopupMenu();
		
		JMenuItem popupNewDatabase = new JMenuItem("New Database Connection...");
		popupNewDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DBConnectionSpec dbcs = new DBConnectionSpec();
					dbcs.setName("New Connection");
					dbcs.setDisplayName("New Connection");
					SQLDatabase db = new SQLDatabase(dbcs);
					((DBTreeModel.DBTreeRoot) getModel().getRoot()).addChild(db);
					ArchitectFrame.getMainInstance().getUserSettings().getConnections().add(dbcs);
					logger.debug("Setting new DBCS on panel: "+dbcs);
					dbcsPanel.setDbcs(dbcs);
					propDialog.setVisible(true);
					propDialog.requestFocus();
				}
			});
		newMenu.add(popupNewDatabase);  // index 0

		newMenu.addSeparator();         // index 1

		JMenuItem popupProperties = new JMenuItem("Properties");
		popupProperties.addActionListener(new PopupPropertiesListener());
		newMenu.add(popupProperties);   // index 2

		return newMenu;
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
				if (p == null) {
					popup.getComponent(0).setVisible(true);
					popup.getComponent(1).setVisible(false);
					popup.getComponent(2).setVisible(false);
				} else {
					//SQLObject so = (SQLObject) p.getLastPathComponent();
					popup.getComponent(0).setVisible(true);
					popup.getComponent(1).setVisible(true);
					popup.getComponent(2).setVisible(true);
				}
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
				DBConnectionSpec dbcs = ((SQLDatabase) so).getConnectionSpec();
				logger.debug("Setting existing DBCS on panel: "+dbcs);
				dbcsPanel.setDbcs(dbcs);
				propDialog.setVisible(true);
				propDialog.requestFocus();
			} else if (so instanceof SQLCatalog) {
			} else if (so instanceof SQLSchema) {
			} else if (so instanceof SQLTable) {
			} else if (so instanceof SQLColumn) {
			}
		}
	}
}
