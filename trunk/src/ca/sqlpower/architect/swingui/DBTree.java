package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
	protected JMenu popupDBCSMenu;
	protected JDialog propDialog;
	protected DBCSPanel dbcsPanel;
	protected NewDBCSAction newDBCSAction;

	/**
	 * This is the database whose DBCS is currently being editted in
	 * the DBCS Panel.
	 */
	protected SQLDatabase edittingDB;

	/**
	 * This is set to true when the DBCSPanel is editting a new
	 * connection spec.  The dialog's "ok" and "cancel" button
	 * handlers need to do different things for new and existing
	 * specs.
	 */
	protected boolean panelHoldsNewDBCS;

	// ----------- CONSTRUCTORS ------------

	public DBTree() {
		setUI(new MultiDragTreeUI());
		setRootVisible(false);
		setShowsRootHandles(true);
		ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer
			(this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());

		newDBCSAction = new NewDBCSAction();
		setupPropDialog();
		popup = setupPopupMenu();
		addMouseListener(new PopupListener());
		setCellRenderer(new SQLObjectRenderer());				
	}

	public DBTree(List initialDatabases) throws ArchitectException {
		this();
		setDatabaseList(initialDatabases);
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
					edittingDB.setConnectionSpec(dbcsPanel.getDbcs());
					if (panelHoldsNewDBCS) {
						ArchitectFrame.getMainInstance().getUserSettings()
							.getConnections().add(dbcsPanel.getDbcs());
						SQLObject root = (SQLObject) getModel().getRoot();
						try {
							root.addChild(root.getChildCount(), edittingDB);
						} catch (ArchitectException ex) {
							logger.warn("Couldn't add new database to tree", ex);
							JOptionPane.showMessageDialog(DBTree.this, "Couldn't add new connection:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					panelHoldsNewDBCS = false;
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
		propDialog.setLocationRelativeTo(ArchitectFrame.getMainInstance());
	}


	// ----------- INSTANCE METHODS ------------

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
     * Before adding a new connection to the SwingUIProject, check to see
     * if it exists.
	 */
	public boolean dbcsAlreadyExists(DBConnectionSpec spec) throws ArchitectException {
		SQLObject so = (SQLObject) getModel().getRoot();		
		// the children of the root, if they exists, are always SQLDatabase objects
		Iterator it = so.getChildren().iterator();
		boolean found = false;
		while (it.hasNext() && found == false) {
			DBConnectionSpec dbcs = ((SQLDatabase) it.next()).getConnectionSpec();
			if (spec.equals(dbcs)) {
				found = true;
			}
		}
		return found;		
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

	
	// -------------- JTree Overrides ---------------------
	
	public void expandPath(TreePath tp) {
		try {
			ArchitectFrame.getMainInstance().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			super.expandPath(tp);
		} catch (Exception ex) {
			String message = ex.getMessage();
			logger.warn("Unexpected exception while expanding path "+tp, ex);
		} finally {
			ArchitectFrame.getMainInstance().setCursor(null);
		}
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

		

	// ----------------- popup menu stuff ----------------

	protected JPopupMenu setupPopupMenu() {
		JPopupMenu newMenu = new JPopupMenu();
		
		newMenu.add(popupDBCSMenu = new JMenu("Add Connection")); // index 0

		JMenuItem popupProperties = new JMenuItem(new DBCSPropertiesAction());
		newMenu.add(popupProperties);   // index 1

		if (logger.isDebugEnabled()) { // index 2
			newMenu.addSeparator();
			JMenuItem showListeners = new JMenuItem("Show Listeners");
			showListeners.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SQLObject so = (SQLObject) getLastSelectedPathComponent();
						if (so != null) {
							JOptionPane.showMessageDialog(DBTree.this, new JScrollPane(new JList(new java.util.Vector(so.getSQLObjectListeners()))));
						}
					}
				});
			newMenu.add(showListeners);
		}
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
				refreshDBCSMenu(isTargetDatabaseNode(p));
				if (p == null) {
					logger.debug("not over anything, so give NEW option");
					popup.getComponent(0).setVisible(true); // add new
					popup.getComponent(1).setVisible(false); // show listeners
				} else {
					logger.debug("we are over something, so give properties option.");
					//SQLObject so = (SQLObject) p.getLastPathComponent();
					popup.getComponent(0).setVisible(isTargetDatabaseNode(p)); // add new
					popup.getComponent(1).setVisible(true); // connection properties
				}
				setSelectionPath(p);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
	
	// check to see if the SQLDatabase reference from the DBTree is the same as the one
    // held by the PlayPen.  If it is, we are looking at the Target Database
	protected boolean isTargetDatabaseNode(TreePath tp) {
		if (tp == null) {
			return false;
		}
		
		Object [] oo = tp.getPath();
		if (ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase() == oo [oo.length - 1]) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Refreshes the submenu which contains the DBCS history list.
	 */
	protected void refreshDBCSMenu(boolean suppressNew) {
		logger.debug("refreshDBCSMenu is being called.");
		popupDBCSMenu.removeAll();		
		if (!suppressNew) {
			popupDBCSMenu.add(new JMenuItem(newDBCSAction));		
			popupDBCSMenu.addSeparator();
		}
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while(it.hasNext()) {
			DBConnectionSpec dbcs = (DBConnectionSpec) it.next();
			if (!suppressNew) {
				popupDBCSMenu.setText("Add Connection");
				popupDBCSMenu.add(new JMenuItem(new AddDBCSAction(dbcs)));
			} else {				
				popupDBCSMenu.setText("Set Target Database");
				popupDBCSMenu.add(new JMenuItem(new setTargetDBCSAction(dbcs)));
			}
		}
	}

	/**
	 * When invoked, this action adds the DBCS that was given in the
	 * constructor to the DBTree's model.  There is normally one
	 * AddDBCSAction associated with each item in the "Add Connection"
	 * menu.
	 */
	protected class AddDBCSAction extends AbstractAction {
		protected DBConnectionSpec dbcs;

		public AddDBCSAction(DBConnectionSpec dbcs) {
			super(dbcs.getName());
			this.dbcs = dbcs;
		}

		public void actionPerformed(ActionEvent e) {
			SQLObject root = (SQLObject) getModel().getRoot();
			try {
				// check to see if we've already seen this one
				if (dbcsAlreadyExists(dbcs)) {
					logger.warn("database already exists in this project.");
					JOptionPane.showMessageDialog(DBTree.this, "Can't add connection, connection already exists in this project.", "Warning", JOptionPane.WARNING_MESSAGE);
				} else {					
					root.addChild(root.getChildCount(), new SQLDatabase(dbcs));
				}
			} catch (ArchitectException ex) {
				logger.warn("Couldn't add new database to tree", ex);
				JOptionPane.showMessageDialog(DBTree.this, "Couldn't add new connection:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * When invoked, this action creates a new DBCS, sets the
	 * panelHoldsNewDBCS flag, and pops up the propDialog to edit the
	 * new DBCS.
	 */
	protected class NewDBCSAction extends AbstractAction {

		public NewDBCSAction() {
			super("New Connection...");
		}

		public void actionPerformed(ActionEvent e) {
			DBConnectionSpec dbcs = new DBConnectionSpec();
			edittingDB = new SQLDatabase(dbcs);
			panelHoldsNewDBCS = true;
			dbcsPanel.setDbcs(dbcs);
			propDialog.setVisible(true);
			propDialog.requestFocus();
		}
	}

	/**
	 * The DBCSPropertiesAction responds to the "Properties" item in
	 * the popup menu.  It determines which item in the tree is
	 * currently selected, then (creates and) shows its properties
	 * window.
	 */
	class DBCSPropertiesAction extends AbstractAction {
		public DBCSPropertiesAction() {
			super("Connection Properties...");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = getSelectionPath();
			if (p == null) {
				return;
			}
			SQLObject so = (SQLObject) p.getLastPathComponent();
			if (so instanceof SQLDatabase) {
				DBConnectionSpec dbcs = ((SQLDatabase) so).getConnectionSpec();
				logger.debug("Setting existing DBCS on panel: "+dbcs);
				edittingDB = (SQLDatabase) so;
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


	/**
	 * copy the DBCS info from the selected DBCS into the DBCS
     * of Target Database
	 */
	protected class setTargetDBCSAction extends AbstractAction {
		protected DBConnectionSpec dbcs;

		public setTargetDBCSAction(DBConnectionSpec dbcs) {
			super(dbcs.getName());
			this.dbcs = dbcs;
		}

		public void actionPerformed(ActionEvent e) {	
			// make a new connection spec
			panelHoldsNewDBCS = false; // we are editing the Target Database dbcs, which has already been created
			edittingDB = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase();
			// copy over the values from the selected DB.
			DBConnectionSpec tSpec = edittingDB.getConnectionSpec();
        	tSpec.setSingleLogin(dbcs.isSingleLogin());
			// don't copy the sequence number, or it will prevent the Target Database and whatever it
            // was cloned from from co-existing in the same project
        	tSpec.setDriverClass(dbcs.getDriverClass());
        	tSpec.setUrl(dbcs.getUrl());
        	tSpec.setUser(dbcs.getUser());
        	tSpec.setPass(dbcs.getPass());
			dbcsPanel.setDbcs(tSpec);
			propDialog.setVisible(true);
			propDialog.requestFocus();
		}
	}


	// --------------- INNER CLASSES -----------------
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

	public static class SQLObjectRenderer extends DefaultTreeCellRenderer {
		public static final ImageIcon dbIcon = ASUtils.createIcon("Database", "SQL Database", 16);
		public static final ImageIcon cataIcon = ASUtils.createIcon("Catalog", "SQL Catalog", 16);
		public static final ImageIcon schemaIcon = ASUtils.createIcon("Schema", "SQL Schema", 16);
		public static final ImageIcon tableIcon = ASUtils.createIcon("Table", "SQL Table", 16);
		public static final ImageIcon keyIcon = ASUtils.createIcon("ExportedKey", "Exported key", 16);
		public static final ImageIcon ownerIcon = ASUtils.createIcon("Owner", "Owner", 16);
		
		public Component getTreeCellRendererComponent(JTree tree,
													  Object value,
													  boolean sel,
													  boolean expanded,
													  boolean leaf,
													  int row,
													  boolean hasFocus) {
			setText(value.toString());
			if (value instanceof SQLDatabase) {
				setIcon(dbIcon);
			} else if (value instanceof SQLCatalog) {
				if (((SQLCatalog) value).getNativeTerm().equals("owner")) {
					setIcon(ownerIcon);
				} else if (((SQLCatalog) value).getNativeTerm().equals("database")) {
					setIcon(dbIcon);
				} else if (((SQLCatalog) value).getNativeTerm().equals("schema")) {
					setIcon(schemaIcon);
				} else {
					setIcon(cataIcon);
				}
			} else if (value instanceof SQLSchema) {
				if (((SQLSchema) value).getNativeTerm().equals("owner")) {
					setIcon(ownerIcon);
				} else {
					setIcon(schemaIcon);
				}
			} else if (value instanceof SQLTable) {
				setIcon(tableIcon);
			} else if (value instanceof SQLRelationship) {
				setIcon(keyIcon);
			} else {
				setIcon(null);
			}

			this.selected = sel;
			this.hasFocus = hasFocus;

			return this;
		}
	}
}