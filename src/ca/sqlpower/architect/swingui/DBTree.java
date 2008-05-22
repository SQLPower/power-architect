/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.Cursor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLExceptionNode;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPDataSourcePanel;
import ca.sqlpower.swingui.SPSwingWorker;

public class DBTree extends JTree implements DragSourceListener {
	static Logger logger = Logger.getLogger(DBTree.class);
	
	protected DragSource ds;
	protected JPopupMenu popup;
	protected JMenu connectionsMenu;
	protected SPDataSourcePanel spDataSourcePanel;
	protected NewDBCSAction newDBCSAction;
	protected DBCSPropertiesAction dbcsPropertiesAction;
	protected RemoveDBCSAction removeDBCSAction;
	protected ShowInPlayPenAction showInPlayPenAction;
    protected SetConnAsTargetDB setConnAsTargetDB;
    
    /**
     * The architect session, so we can access common objects
     */
    private final ArchitectSwingSession session;

	/**
	 * This is set to true when the SPDataSourcePanel is editting a new
	 * connection spec.  The dialog's "ok" and "cancel" button
	 * handlers need to do different things for new and existing
	 * specs.
	 */
	protected boolean panelHoldsNewDBCS;


	// ----------- CONSTRUCTORS ------------

	public DBTree(ArchitectSwingSession session) throws ArchitectException {
        this.session = session;
        setModel(new DBTreeModel(session));
		setUI(new MultiDragTreeUI());
		setRootVisible(false);
		setShowsRootHandles(true);
		ds = new DragSource();
		ds.createDefaultDragGestureRecognizer
			(this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());

        setConnAsTargetDB = new SetConnAsTargetDB(null);
		newDBCSAction = new NewDBCSAction();
		dbcsPropertiesAction = new DBCSPropertiesAction();	
		removeDBCSAction = new RemoveDBCSAction();
		showInPlayPenAction = new ShowInPlayPenAction();
		addMouseListener(new PopupListener());
        setCellRenderer(new DBTreeCellRenderer(session));
	}

	// ----------- INSTANCE METHODS ------------

	/**
	 * Returns a list of all the databases in this DBTree's model.
	 */
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
	public boolean dbcsAlreadyExists(SPDataSource spec) throws ArchitectException {
		SQLObject so = (SQLObject) getModel().getRoot();
		// the children of the root, if they exists, are always SQLDatabase objects
		Iterator it = so.getChildren().iterator();
		boolean found = false;
		while (it.hasNext() && found == false) {
			SPDataSource dbcs = ((SQLDatabase) it.next()).getDataSource();
			if (spec==dbcs) {
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
	public SPDataSource getDuplicateDbcs(SPDataSource spec) {
		SPDataSource dup = null;
		boolean found = false;
		Iterator it = session.getContext().getConnections().iterator();
		while (it.hasNext() && found == false) {
			SPDataSource dbcs = (SPDataSource) it.next();
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
		List<SQLObject[]> sopList = m.getPathsToNode(node);
		SQLObject[] sop = sopList.get(0);
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
			session.getArchitectFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			super.expandPath(tp);
		} catch (Exception ex) {
			logger.warn("Unexpected exception while expanding path "+tp, ex);
		} finally {
			session.getArchitectFrame().setCursor(null);
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
            maybeShowPopup(e,true);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e,false);
        }

        private void maybeShowPopup(MouseEvent e, boolean isPress) {

            TreePath p = getPathForLocation(e.getX(), e.getY());
            if (e.isPopupTrigger()) {
				logger.debug("TreePath is: " + p);

				if (p != null) {
					logger.debug("selected node object type is: " + p.getLastPathComponent().getClass().getName());
				}

				// if the item is not already selected, select it (and deselect everything else)
                // if the item is already selected, don't touch the selection model
				if (isTargetDatabaseChild(p)) {
					if (!isPathSelected(p)) {
						setSelectionPath(p);
				    }
				} else {
					// multi-select for menus is not supported outside the Target Database
                    if (!isPathSelected(p)) {
                        setSelectionPath(p);
                    }
				}
				popup = refreshMenu(p);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            } else {
                if ( p == null && !isPress && e.getButton() == MouseEvent.BUTTON1 )
                    setSelectionPath(null);
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
     * 
     * <li> click on a schema in the tree. Allow the user to quickly compare the 
     * contents of the playpen with the selected schema. 
	 * </ol>
     *
     * <p>FIXME: add in column, table, exported key, imported keys menus; you can figure
     * out where the click came from by checking the TreePath.
	 */
	protected JPopupMenu refreshMenu(TreePath p) {
		logger.debug("refreshMenu is being called.");
		JPopupMenu newMenu = new JPopupMenu();
		newMenu.add(connectionsMenu = new JMenu("Add Source Connection"));
		connectionsMenu.add(new JMenuItem(newDBCSAction));
		connectionsMenu.addSeparator();
		
		newMenu.add(new DatabaseConnectionManagerAction(session));

		// populate

		for (SPDataSource dbcs : session.getContext().getConnections()) {
			connectionsMenu.add(new JMenuItem(new AddDBCSAction(dbcs)));
		}
		ASUtils.breakLongMenu(session.getArchitectFrame(),connectionsMenu);

		if (!isTargetDatabaseNode(p) && isTargetDatabaseChild(p)) {
			newMenu.addSeparator();
			ArchitectFrame af = session.getArchitectFrame();
			JMenuItem mi;
            
            mi = new JMenuItem();
            mi.setAction(af.getInsertIndexAction());
            mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLTable) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }

            newMenu.addSeparator();
            
			mi = new JMenuItem();
			mi.setAction(af.getEditColumnAction());
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLColumn) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
            
			mi = new JMenuItem();
			mi.setAction(af.getInsertColumnAction());
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLTable || p.getLastPathComponent() instanceof SQLColumn) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}

			newMenu.addSeparator();

			mi = new JMenuItem();
			mi.setAction(showInPlayPenAction);
			newMenu.add(mi);

			mi = new JMenuItem();
			mi.setAction(af.getEditTableAction());
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLTable || p.getLastPathComponent() instanceof SQLColumn) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}

			mi = new JMenuItem();
			mi.setAction(af.getEditRelationshipAction());
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLRelationship) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}

            mi = new JMenuItem();
            mi.setAction(af.getEditIndexAction());
            mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLIndex) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }            

			mi = new JMenuItem();
			mi.setAction(af.getDeleteSelectedAction());
			mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLTable ||
			        p.getLastPathComponent() instanceof SQLColumn ||
			        p.getLastPathComponent() instanceof SQLRelationship ||
                    (p.getLastPathComponent() instanceof SQLIndex && 
                           !((SQLIndex) p.getLastPathComponent()).isPrimaryKeyIndex())) {
			    mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
		} else if (!isTargetDatabaseNode(p) && p != null) { // clicked on DBCS item in DBTree
			newMenu.addSeparator();

			if (p.getLastPathComponent() instanceof SQLDatabase){
			    SQLDatabase tempDB=(SQLDatabase)(p.getLastPathComponent());

			    try {
			        //this if is looking for a database with only tables in it
			        //it checks first that it does not hold schemas of catalogs
			        //then it looks if it contains error nodes, which will occur if the 
			        //tree only has one child
			        if (!tempDB.isCatalogContainer() && !tempDB.isSchemaContainer() && 
			                (!(tempDB.getChildCount() == 1) || 
			                        !tempDB.getChild(0).getClass().equals(SQLExceptionNode.class)))
			        {
			            //a new action is needed to maintain the database variable
			            CompareToCurrentAction compareToCurrentAction = new CompareToCurrentAction();
			            compareToCurrentAction.putValue(CompareToCurrentAction.DATABASE,tempDB);
			            JMenuItem popupCompareToCurrent = new JMenuItem(compareToCurrentAction);            
			            newMenu.add(popupCompareToCurrent);
			        }
			    } catch (ArchitectException e) {
			        ASUtils.showExceptionDialog(session, "Error Communicating with database", e);
			    }
			    
			    JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
			    newMenu.add(profile);

                JMenuItem setAsDB = new JMenuItem(new SetConnAsTargetDB(tempDB.getDataSource()));
                newMenu.add(setAsDB);
                
                newMenu.add(new JMenuItem(removeDBCSAction));
            } else if (p.getLastPathComponent() instanceof SQLSchema){
                //a new action is needed to maintain the schema variable
                CompareToCurrentAction compareToCurrentAction = new CompareToCurrentAction();
                compareToCurrentAction.putValue(CompareToCurrentAction.SCHEMA, p.getLastPathComponent());
                JMenuItem popupCompareToCurrent = new JMenuItem(compareToCurrentAction);            
                newMenu.add(popupCompareToCurrent);
                
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
            } else if (p.getLastPathComponent() instanceof SQLCatalog) {
                SQLCatalog catalog = (SQLCatalog)p.getLastPathComponent();
                try {
                    //this is only needed if the database type does not have schemas
                    //like in MYSQL
                    if (!catalog.isSchemaContainer()) {
                        //a new action is needed to maintain the catalog variable
                        CompareToCurrentAction compareToCurrentAction = new CompareToCurrentAction();
                        compareToCurrentAction.putValue(CompareToCurrentAction.CATALOG,catalog);
                        JMenuItem popupCompareToCurrent = new JMenuItem(compareToCurrentAction);            
                        newMenu.add(popupCompareToCurrent);
                    }
                } catch (ArchitectException e) {
                    ASUtils.showExceptionDialog(session, "Error Communicating with database", e);
                }
                
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
            } else if (p.getLastPathComponent() instanceof SQLTable) {
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
            }
            
			newMenu.addSeparator();
            JMenuItem popupProperties = new JMenuItem(dbcsPropertiesAction);
            newMenu.add(popupProperties);
		}

		// Show exception details (SQLException node can appear anywhere in the hierarchy)
		if (p != null && p.getLastPathComponent() instanceof SQLExceptionNode) {
			newMenu.addSeparator();
            final SQLExceptionNode node = (SQLExceptionNode) p.getLastPathComponent();
            newMenu.add(new JMenuItem(new AbstractAction("Show Exception Details") {
                public void actionPerformed(ActionEvent e) {
                    ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                            "Exception Node Report", node.getException());
                }
            }));

            // If the sole child is an exception node, we offer the user a way to re-try the operation
            try {
                final SQLObject parent = node.getParent();
                if (parent.getChildCount() == 1) {
                    newMenu.add(new JMenuItem(new AbstractAction("Retry") {
                        public void actionPerformed(ActionEvent e) {
                            parent.removeChild(0);
                            parent.setPopulated(false);
                            try {
                                parent.getChildren(); // forces populate
                            } catch (ArchitectException ex) {
                                try {
									parent.addChild(new SQLExceptionNode(ex, "New exception during retry"));
								} catch (ArchitectException e1) {
									logger.error("Couldn't add SQLExceptionNode to menu:", e1);
									ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
									        "Failed to add SQLExceptionNode.", e1);
								}
                                ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                                        "Exception occurred during retry", ex);
                            }
                        }
                    }));
                }
            } catch (ArchitectException ex) {
                logger.error("Couldn't count siblings of SQLExceptionNode", ex);
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
			return session.getTargetDatabase() == tp.getLastPathComponent();
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
			if (session.getTargetDatabase() == oo[i]) return true;
		return false;
	}

	/**
	 * When invoked, this action adds the DBCS that was given in the
	 * constructor to the DBTree's model.  There is normally one
	 * AddDBCSAction associated with each item in the "Set Connection"
	 * menu.
	 */
	protected class AddDBCSAction extends AbstractAction {
		protected SPDataSource dbcs;

		public AddDBCSAction(SPDataSource dbcs) {
			super(dbcs.getName());
			this.dbcs = dbcs;
		}

		public void actionPerformed(ActionEvent e) {
			addSourceConnection(dbcs);
		}

	}

    /**
     * Adds the given data source to the db tree as a source database
     * connection.
     * 
     * @param dbcs The data source to be added to the db tree.
     */
	private void addSourceConnection(SPDataSource dbcs) {
	    SQLObject root = (SQLObject) getModel().getRoot();
	    try {
	        // check to see if we've already seen this one
	        if (dbcsAlreadyExists(dbcs)) {
	            logger.warn("database already exists in this project.");
	            JOptionPane.showMessageDialog(DBTree.this, "Can't set connection "
	                    + dbcs.getDisplayName()
	                    + ".  It already exists in the current project.",
	                    "Warning", JOptionPane.WARNING_MESSAGE);
	        } else {
	            SQLDatabase newDB = new SQLDatabase(dbcs);
	            root.addChild(root.getChildCount(), newDB);
	            session.getProject().setModified(true);
	            // start a thread to poke the new SQLDatabase object...
	            logger.debug("start poking database " + newDB.getName());
	            PokeDBWorker poker = new PokeDBWorker(newDB);
	            new Thread(poker, "PokeDB: " + newDB.getName()).start();
	        }
	    } catch (ArchitectException ex) {
	        logger.warn("Couldn't add new database to tree", ex);
	        ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
	                "Couldn't add new connection.", ex);
	    }
	}
    
    protected class SetConnAsTargetDB extends AbstractAction{
        SPDataSource dbcs;

        public SetConnAsTargetDB(SPDataSource dbcs){
            super("Set As Target Database");
            this.dbcs  = dbcs;
        }

        public void actionPerformed(ActionEvent e) {
            session.getPlayPen().setDatabaseConnection(dbcs);
        }
    }

	/**
	 * When invoked, this action creates a new DBCS, sets the
	 * panelHoldsNewDBCS flag, and pops up the propDialog to edit the
	 * new DBCS.
     * <p>
     * If the database connection is created it will be added to the
     * db tree as well as the PL.ini.
	 */
	protected class NewDBCSAction extends AbstractAction {

		public NewDBCSAction() {
			super("New Connection...");
		}

		public void actionPerformed(ActionEvent e) {

            final DataSourceCollection plDotIni = session.getContext().getPlDotIni();
            final SPDataSource dataSource = new SPDataSource(plDotIni);
            Runnable onAccept = new Runnable() {
                public void run() {
                    addSourceConnection(dataSource);
                    plDotIni.addDataSource(dataSource);
                }
            };
            ASUtils.showDbcsDialog(session.getArchitectFrame(), dataSource, onAccept);
            
			panelHoldsNewDBCS = true;
		}
	}

	/**
	 * A Swing Worker that descends a tree of SQLObjects, stopping when a
	 * SQLColumn is encountered. This is useful in making the application
	 * more responsive: As soon as a source database is added to the tree,
	 * this worker will start to connect to it and exercise its JDBC driver.
	 * Then once the user goes to expand the tree, the response is instant!
	 */
	private class PokeDBWorker extends SPSwingWorker {
	    
	    /**
	     * The top object where the poking starts.
	     */
		final SQLObject root;
		
		/**
		 * The most-recently-visited object. This is tracked so that cleanup
		 * knows where to add the SQLExceptionNode in case of failure.
		 */
		SQLObject mostRecentlyVisited;
		
		/**
		 * Creates a new worker that will recursively visit the SQLObject tree
		 * rooted at so, stopping when a SQLColumn descendant of so is encountered.
		 * 
		 * @param so The object to start with
		 */
		PokeDBWorker(SQLObject so) {
			super(session);
			this.root = so;
		}
		
		/**
         * Recursively visits SQLObjects starting at {@link #source}, stopping
         * at the first leaf node (SQLColumn) encountered.
         */
		@Override
		public void doStuff() throws Exception {
		    pokeDatabase(root);
		    logger.debug("successfully poked database " + root.getName());
		}
		
		/**
		 * The recursive subroutine of doStuff(). That means it will be called
		 * on a worker thread, and it shouldn't do anything that needs to be
		 * done on Swing's Event Dispatch Thread!
		 */
		private boolean pokeDatabase(final SQLObject source) throws ArchitectException {
		    if (logger.isDebugEnabled()) logger.debug("HELLO my class is " + source.getClass().getName() + ", my name is + " + source.getName());
		    if (source.allowsChildren()) {
		        mostRecentlyVisited = source;
		        int j = 0;
		        boolean done = false;
		        int childCount = source.getChildCount();
		        while (!done && j < childCount) {
		            done = pokeDatabase(source.getChild(j));
		            j++;
		        }
		        return done;
		    } else {
		        return true; // found a leaf node
		    }
		}
		
		/**
		 * Checks if the doStuff() procedure ran into trouble, and if so, attaches
		 * a SQLException node under the node that was being visited at the time
		 * the exception was thrown.
		 */
		@Override
		public void cleanup() throws Exception {
		    if (getDoStuffException() != null) {
		        // FIXME: SQLObject should have an "exception" property that's not a child,
		        //        and client code shouldn't have to clean up populate exceptions
		        //        like this
		        mostRecentlyVisited.addChild(
		                new SQLExceptionNode(
		                        getDoStuffException(),
		                        "Error during initial database probe"));
		    }
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

            // Historical note: we used to check here if there were any objects in the
            // play pen that depend on any children of this database before agreeing
            // to remove it. Now this is handled by a listener in the PlayPen itself.
            
			SQLDatabase selection = (SQLDatabase) tp.getLastPathComponent();
			SQLObject root = (SQLObject) getModel().getRoot();
			if (root.removeChild(selection)) {
			    selection.disconnect();
			} else {
			    logger.error("root.removeChild(selection) returned false!");
			    JOptionPane.showMessageDialog(DBTree.this, "Deletion of this database connection failed for an unknown reason.", "Couldn't remove", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * The DBCSPropertiesAction responds to the "Properties" item in
	 * the popup menu.  It determines which item in the tree is
	 * currently selected, then shows its properties
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
                ASUtils.showDbcsDialog(session.getArchitectFrame(), sd.getDataSource(), null);
                logger.debug("Setting existing DBCS on panel");
			}
		}
	}
	
	
	//the action for clicking on "Compare to current"
	protected class CompareToCurrentAction extends AbstractAction {
	    public static final String SCHEMA = "SCHEMA";
	    public static final String CATALOG = "CATALOG";
	    public static final String DATABASE = "DATABASE";
	    
        public CompareToCurrentAction() {
            super("Compare to Current");
        }

        public void actionPerformed(ActionEvent e) {
            //gets the database and catalog from the tree
            SQLSchema schema = null;
            SQLDatabase db = null;
            SQLCatalog catalog = null;
            
            if (getValue(SCHEMA)!= null) {
                schema = (SQLSchema)getValue(SCHEMA);
                //oracle does not have catalogs so a check is needed
                if (schema.getParent().getParent() == null) {
                    db = (SQLDatabase)schema.getParent();
                } else {
                    db = (SQLDatabase)schema.getParent().getParent();
                    catalog = (SQLCatalog)schema.getParent();
                }
            } else if (getValue(CATALOG) != null) {
                catalog = (SQLCatalog)getValue(CATALOG);
                db = (SQLDatabase)catalog.getParent();
            } else if (getValue(DATABASE) != null) {
                db = (SQLDatabase)getValue(DATABASE);
            }
            
            session.getArchitectFrame().getCompareDMDialog().setVisible(true);
            //sets to the right settings
            session.getArchitectFrame().getCompareDMDialog().compareCurrentWithOrig(schema,catalog, db);
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
			super("Show in Playpen");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = getSelectionPath();
			if (p == null) {
				return;
			}
			PlayPen pp = session.getPlayPen();
			SQLObject selection = (SQLObject) p.getLastPathComponent();
            //Since we cannot directly select a SQLColumn directly
            //from the playpen, there is a special case for it
            if (selection instanceof SQLColumn){
                SQLColumn col = (SQLColumn)selection;
                SQLTable table = col.getParentTable();
                TablePane tp = pp.findTablePane(table);
                pp.selectAndShow(table);
                tp.selectedColumns.add(col);
            } else
                pp.selectAndShow(selection);

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
			logger.info("Drag gesture event: " + dge);

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
					// ignore any playpen tables
					if (t.getDnDPathToNode((SQLObject) p[i].getLastPathComponent())[0]  !=0 )
					{
						paths.add(t.getDnDPathToNode((SQLObject) p[i].getLastPathComponent()));
					}
				}
				logger.info("DBTree: exporting list of DnD-type tree paths");

				// TODO add undo event
				dge.getDragSource().startDrag
					(dge,
					 null, //DragSource.DefaultCopyNoDrop,
					 new DnDTreePathTransferable(paths),
					 t);
			}
 		}
	}
}