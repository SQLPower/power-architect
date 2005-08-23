package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.Color;
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
import ca.sqlpower.architect.ArchitectDataSource;

public class DBTree extends JTree implements DragSourceListener {
	private static Logger logger = Logger.getLogger(DBTree.class);

	protected DragSource ds;
	protected JPopupMenu popup;
	protected JMenu connectionsMenu;
	protected JDialog propDialog;
	protected DBCSPanel dbcsPanel;
	protected NewDBCSAction newDBCSAction;
	protected DBCSPropertiesAction dbcsPropertiesAction;
	protected RemoveDBCSAction removeDBCSAction;
	protected ShowInPlayPenAction showInPlayPenAction;

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
		dbcsPropertiesAction = new DBCSPropertiesAction();
		removeDBCSAction = new RemoveDBCSAction();
		showInPlayPenAction = new ShowInPlayPenAction();
		setupPropDialog();
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
					edittingDB.setDataSource(dbcsPanel.getDbcs());
					if (panelHoldsNewDBCS) { // don't allow new duplicate connections to be added
						ArchitectDataSource dup = getDuplicateDbcs(edittingDB.getDataSource());							
						if (dup == null) { // did not find one, go ahead and add it to User Settings
							ArchitectFrame.getMainInstance().getUserSettings()
								.getPlDotIni().addDataSource(dbcsPanel.getDbcs());
							SQLObject root = (SQLObject) getModel().getRoot();
							try {
								root.addChild(root.getChildCount(), edittingDB);
							} catch (ArchitectException ex) {
								logger.warn("Couldn't add new database to tree", ex);
								JOptionPane.showMessageDialog(DBTree.this, "Couldn't add new connection:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							logger.warn("The connection you tried to create already exists under the name: " + dup.getDisplayName());
							JOptionPane.showMessageDialog(DBTree.this, 
                             "The connection you tried to create already exists under the name: " 
                             + dup.getDisplayName(), 
                             "Error", JOptionPane.ERROR_MESSAGE);
							return; // don't dispose() of the dialog just yet...
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
     * if it exists as a connection in the project (which means they're in this
     * tree's model).
	 */
	public boolean dbcsAlreadyExists(ArchitectDataSource spec) throws ArchitectException {
		SQLObject so = (SQLObject) getModel().getRoot();		
		// the children of the root, if they exists, are always SQLDatabase objects
		Iterator it = so.getChildren().iterator();
		boolean found = false;
		while (it.hasNext() && found == false) {
			ArchitectDataSource dbcs = ((SQLDatabase) it.next()).getDataSource();
			if (spec.equals(dbcs)) {
				found = true;
			}
		}
		return found;		
	}

	/**
     * Pass in a spec, and look for a duplicate in the list of DBCS objects in 
     * User Settings.  If we find one, return a handle to it.  If we don't find
     * one, return null.
	 */
	public ArchitectDataSource getDuplicateDbcs(ArchitectDataSource spec) {
		ArchitectDataSource dup = null;
		boolean found = false;
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();		
		while (it.hasNext() && found == false) {
			ArchitectDataSource dbcs = (ArchitectDataSource) it.next();
			if (spec.equals(dbcs)) {
				dup = dbcs;
				found = true;
			}
		}
		return dup;
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
				logger.debug("TreePath is: " + p);
				if (p != null) {
					logger.debug("selected node object type is: " + p.getLastPathComponent().getClass().getName());
				}
				popup = refreshMenu(p);

				// if the item is not already selected, select it (and deselect everything else)
                // if the item is already selected, don't touch the selection model				
				if (isTargetDatabaseChild(p)) {
					if (!isPathSelected(p)) {
						setSelectionPath(p);
				    }	
				} else {
					// multi-select for menus is not supported outside the Target Database
					setSelectionPath(p);
				}
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
		
	/**
	 * Creates a context sensitive menu for managing Database Connections. There
     * are several modes of operations:
     * 
     * <ol>
     *  <li>click on target database.  the user can modify the properties manually,
     * or select a target from the ones defined in user settings.  If there is 
     * nothing defined, then that option is disabled.
     * 
     *  <li>click on an DBCS reference in the DBTree.  Bring up the dialog that 
     * allows the user to modify this connection.
     * 
     *  <li>click on the background of the DBTree.  Allow the user to select DBCS
     * from a list, or create a new DBCS from scratch (which will be added to the  
     * User Settings list of DBCS objects).
	 * </ol>
     * 
     * <p>FIXME: add in column, table, exported key, imported keys menus; you can figure
     * out where the click came from by checking the TreePath.
	 */
	protected JPopupMenu refreshMenu(TreePath p) {
		logger.debug("refreshMenu is being called.");
		JPopupMenu newMenu = new JPopupMenu();				
		if (isTargetDatabaseNode(p)) {
			// two menu items: "Set Target Database" and "Connection Properties
			newMenu.add(connectionsMenu = new JMenu("Set Target Database"));
			if (ArchitectFrame.getMainInstance().getUserSettings().getConnections().size() == 0) {
				// disable if there's no connections in user settings yet (annoying, but less confusing)
				connectionsMenu.setEnabled(false);
			} else {
				// populate
				Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
				while(it.hasNext()) {
					ArchitectDataSource dbcs = (ArchitectDataSource) it.next();
					connectionsMenu.add(new JMenuItem(new setTargetDBCSAction(dbcs)));
				}
				
				/* this doesn't work, but it's what I want to do... 
				final ScrollablePopupMenu smenu = new ScrollablePopupMenu(new Vector(ArchitectFrame.getMainInstance().getUserSettings().getConnections()));
				smenu.addActionListener( new ActionListener() {
					public void actionPerformed (ActionEvent evt) {
						ArchitectDataSource dbcs = (ArchitectDataSource) smenu.getSelectedItem();
			            logger.debug("Performing setTargetDBCSAction...");
						panelHoldsNewDBCS = false; // we are editing the Target Database dbcs, which has already been created
						edittingDB = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase();
						// copy over the values from the selected DB.
						ArchitectDataSource tSpec = edittingDB.getDataSource();
						// don't copy the sequence number, or it will prevent the Target Database and whatever it
			            // was cloned from from co-existing in the same project
			        	tSpec.setDriverClass(dbcs.getDriverClass());
			        	tSpec.setUrl(dbcs.getUrl());
			        	tSpec.setUser(dbcs.getUser());
			        	tSpec.setPass(dbcs.getPass());
						// for some reason, the above property change events are not being received properly by 
			            // parent SQLDatabase objects
						dbcsPanel.setDbcs(tSpec);
						propDialog.setVisible(true);
						propDialog.requestFocus();					
					}
				});
				connectionsMenu.add(smenu);
				*/
			}
			JMenuItem popupProperties = new JMenuItem(dbcsPropertiesAction);
			newMenu.add(popupProperties);  
		} else if (isTargetDatabaseChild(p)) {			
			ArchitectFrame af = ArchitectFrame.getMainInstance();
			JMenuItem mi;
	
			mi = new JMenuItem();
			mi.setAction(af.editColumnAction);
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if(p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLColumn.class) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
					
			mi = new JMenuItem();
			mi.setAction(af.insertColumnAction);
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if(p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLTable.class ||
			   p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLColumn.class) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}

	
			newMenu.addSeparator();
			
			mi = new JMenuItem();
			mi.setAction(showInPlayPenAction);
			newMenu.add(mi);
	
			mi = new JMenuItem();
			mi.setAction(af.editTableAction);
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if(p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLTable.class ||
			   p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLColumn.class) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
	
			mi = new JMenuItem();
			mi.setAction(af.editRelationshipAction);
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if(p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLRelationship.class) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
	
			mi = new JMenuItem();
			mi.setAction(af.deleteSelectedAction);
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if(p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLTable.class || 
               p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLColumn.class || 
               p.getLastPathComponent().getClass() == ca.sqlpower.architect.SQLRelationship.class) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);	
			}
		} else if (p != null) { // clicked on DBCS item in DBTree
			if (p.getLastPathComponent() instanceof SQLDatabase) {
				newMenu.add(new JMenuItem(removeDBCSAction));
			}
			JMenuItem popupProperties = new JMenuItem(dbcsPropertiesAction);
			newMenu.add(popupProperties);
		} else { // p == null, background click
			newMenu.add(connectionsMenu = new JMenu("Add Connection")); 
			connectionsMenu.add(new JMenuItem(newDBCSAction));		
			connectionsMenu.addSeparator();
			// populate		
			Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
			while(it.hasNext()) {
				ArchitectDataSource dbcs = (ArchitectDataSource) it.next();
				connectionsMenu.add(new JMenuItem(new AddDBCSAction(dbcs)));
			}
		}

		// add in Show Listeners if debug is enabled
		if (logger.isDebugEnabled()) { 
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
     * Checks to see if the SQLDatabase reference from the the DBTree is the 
     * same as the one held by the PlayPen.  If it is, we are looking at the
     * Target Database.
     */
	protected boolean isTargetDatabaseNode(TreePath tp) {
		if (tp == null) {
			return false;
		} else {
			return ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase() == tp.getLastPathComponent();
		}
	}

	/**
     * Checks to see if the given tree path contains the playpen SQLDatabase.
     * 
     * @return True if <code>tp</code> contains the playpen (target) database.
     *   Note that this is not stritcly limited to children of the target
     * database: it will return true if <code>tp</code> ends at the target
     * database node itself.
     */
	protected boolean isTargetDatabaseChild(TreePath tp) {
		if (tp == null) {
			return false;
		}		

		Object[] oo = tp.getPath();
		for (int i = 0; i < oo.length; i++)
			if (ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase() == oo[i]) return true;
		return false;
	}

	/**
	 * When invoked, this action adds the DBCS that was given in the
	 * constructor to the DBTree's model.  There is normally one
	 * AddDBCSAction associated with each item in the "Add Connection"
	 * menu.
	 */
	protected class AddDBCSAction extends AbstractAction {
		protected ArchitectDataSource dbcs;

		public AddDBCSAction(ArchitectDataSource dbcs) {
			super(dbcs.getName());
			this.dbcs = dbcs;
		}

		public void actionPerformed(ActionEvent e) {
			SQLObject root = (SQLObject) getModel().getRoot();
			try {
				// check to see if we've already seen this one
				if (dbcsAlreadyExists(dbcs)) {
					logger.warn("database already exists in this project.");
					JOptionPane.showMessageDialog(DBTree.this, "Can't add connection " 
							                                   + dbcs.getDisplayName() 
															   + ".  It already exists in the current project.", 
												  "Warning", JOptionPane.WARNING_MESSAGE);
				} else {				
					SQLDatabase newDB = new SQLDatabase(dbcs);		
					root.addChild(root.getChildCount(), newDB);
					// start a thread to poke the new SQLDatabase object...
					logger.debug("start poking database " + newDB.getName());
					Thread thread = new PokeDBThread(newDB);
					thread.start();
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
			ArchitectDataSource dbcs = new ArchitectDataSource();
			edittingDB = new SQLDatabase(dbcs);
			panelHoldsNewDBCS = true;
			dbcsPanel.setDbcs(dbcs);
			propDialog.setVisible(true);
			propDialog.requestFocus();
		}
	}

	protected class PokeDBThread extends Thread {
		SQLObject so;
		PokeDBThread (SQLObject so) {
			super();
			this.so = so;			
		}
		public void run() {
			try {
				ArchitectUtils.pokeDatabase(so);
			} catch (ArchitectException ex) {
				logger.error("problem poking database " + so.getName(),ex);
			}
			logger.debug("finished poking database " + so.getName());
		}
	}
	
	/**
	 * The RemoveDBCSAction removes the currently-selected database connection from the project.
	 */
	protected class RemoveDBCSAction extends AbstractAction {
		
		public RemoveDBCSAction() {
			super("Remove Connection");
		}
		
		public void actionPerformed(ActionEvent arg0) {
			TreePath tp = getSelectionPath();
			if (tp == null) {
				JOptionPane.showMessageDialog(DBTree.this, "No items were selected.", "Can't remove", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (! (tp.getLastPathComponent() instanceof SQLDatabase) ) {
				JOptionPane.showMessageDialog(DBTree.this, "The selection was not a database", "Can't remove", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (isTargetDatabaseNode(tp)) {
				JOptionPane.showMessageDialog(DBTree.this, "You can't remove the target database", "Can't remove", JOptionPane.WARNING_MESSAGE);
				return;
			}

			try {
			    SQLDatabase selection = (SQLDatabase) tp.getLastPathComponent();
			    SQLObject root = (SQLObject) getModel().getRoot();
			    List dependants = ArchitectUtils.findColumnsSourcedFromDatabase(ArchitectFrame.getMainInstance().getProject().getTargetDatabase(), selection);
			    if (dependants.size() > 0) {
			        JOptionPane.showMessageDialog(DBTree.this,
			                new Object[] {"The following columns depend on objects in this database:",
			                				new JScrollPane(new JList(dependants.toArray())),
			                				"You can't remove this connection unless you remove these",
			                				"dependencies."},
			                "Can't delete",
			                JOptionPane.INFORMATION_MESSAGE);
			    } else if (root.removeChild(selection)) {
			        selection.disconnect();
			    } else {
			        logger.error("root.removeChild(selection) returned false!");
			        JOptionPane.showMessageDialog(DBTree.this, "Deletion of this database connection failed for an unknown reason.", "Couldn't remove", JOptionPane.ERROR_MESSAGE);
			    }
			} catch (ArchitectException ex) {
				logger.error("Couldn't locate dependant columns", ex);
				JOptionPane.showMessageDialog(DBTree.this, 
						"Couldn't search for dependant columns:\n"+ex.getMessage()
						+"\n\nDatabase connection not removed.",
						"Couldn't remove", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * The DBCSPropertiesAction responds to the "Properties" item in
	 * the popup menu.  It determines which item in the tree is
	 * currently selected, then (creates and) shows its properties
	 * window.
	 */
	protected class DBCSPropertiesAction extends AbstractAction {
		public DBCSPropertiesAction() {
			super("Connection Properties...");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = getSelectionPath();
			if (p == null) {
				return;
			}
			Object [] pathArray = p.getPath();			
			int ii = 0;
			SQLDatabase sd = null;
			while (ii < pathArray.length && sd == null) {
				if (pathArray[ii] instanceof SQLDatabase) {
					sd = (SQLDatabase) pathArray[ii]; 				
				}
				ii++;
			}
			if (sd != null) {
				ArchitectDataSource dbcs = sd.getDataSource();
				logger.debug("Setting existing DBCS on panel: "+dbcs);
				edittingDB = sd;
				dbcsPanel.setDbcs(dbcs);
				propDialog.setVisible(true);
				propDialog.requestFocus();
			}
		}
	}

	/**
	 * The DBCSPropertiesAction responds to the "Properties" item in
	 * the popup menu.  It determines which item in the tree is
	 * currently selected, then (creates and) shows its properties
	 * window.
	 */
	protected class ShowInPlayPenAction extends AbstractAction {
		public ShowInPlayPenAction() {
			super("Show in Play Pen");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = getSelectionPath();
			if (p == null) {
				return;
			}
			PlayPen pp = ArchitectFrame.getMainInstance().playpen;
			SQLObject selection = (SQLObject) p.getLastPathComponent();
			pp.selectAndShow(selection);
		}
	}

	/**
	 * copy the DBCS info from the selected DBCS into the DBCS
     * of Target Database
	 */
	protected class setTargetDBCSAction extends AbstractAction {
		protected ArchitectDataSource dbcs;

		public setTargetDBCSAction(ArchitectDataSource dbcs) {
			super(dbcs.getName());
			this.dbcs = dbcs;
		}

		public void actionPerformed(ActionEvent e) {	
			// make a new connection spec
            logger.debug("Performing setTargetDBCSAction...");
			panelHoldsNewDBCS = false; // we are editing the Target Database dbcs, which has already been created
			edittingDB = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase();
			// copy over the values from the selected DB.
			ArchitectDataSource tSpec = edittingDB.getDataSource();
			// don't copy the sequence number, or it will prevent the Target Database and whatever it
            // was cloned from from co-existing in the same project
        	tSpec.setDriverClass(dbcs.getDriverClass());
        	tSpec.setUrl(dbcs.getUrl());
        	tSpec.setUser(dbcs.getUser());
        	tSpec.setPass(dbcs.getPass());
            tSpec.setPlSchema(dbcs.getPlSchema());
			tSpec.setPlDbType(dbcs.getPlDbType());
			tSpec.setOdbcDsn(dbcs.getOdbcDsn());
			// for some reason, the above property change events are not being received properly by 
            // parent SQLDatabase objects
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
		public static final ImageIcon targetIcon = ASUtils.createIcon("TargetDatabaseArrow", "SQL Database", 16);
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
				logger.debug("project: " + ArchitectFrame.getMainInstance().getProject());
				if (ArchitectFrame.getMainInstance().getProject() == null || // getProject() is null when program is starting
			        ArchitectFrame.getMainInstance().getProject().getTargetDatabase() == value) {
					setIcon(targetIcon);
				} else {
					setIcon(dbIcon);
				}
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
				if (((SQLTable) value).getObjectType() != null) {
				    setText(((SQLTable) value).getName()+" ("+((SQLTable) value).getObjectType()+")");
				} else {
				    setText(((SQLTable) value).getName());
				}
			} else if (value instanceof SQLRelationship) {
				setIcon(keyIcon);
			} else {
				setIcon(null);
			}

			this.selected = sel;
			this.hasFocus = hasFocus;

			if (value instanceof SQLObject) {
			    if (((SQLObject) value).isPopulated()) {
			        setForeground(Color.black);
			    } else {
			        setForeground(Color.lightGray);
			    }
			}
			return this;
		}
	}
}