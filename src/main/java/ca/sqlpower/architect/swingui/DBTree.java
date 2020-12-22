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
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Transferable;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.action.DataSourcePropertiesAction;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
import ca.sqlpower.architect.swingui.action.NewDataSourceAction;
import ca.sqlpower.architect.swingui.action.RefreshAction;
import ca.sqlpower.architect.swingui.action.RemoveSourceDBAction;
import ca.sqlpower.architect.swingui.action.ShowTableContentsAction;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPObjectSnapshot;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.JDBCDataSourcePanel;
import ca.sqlpower.swingui.JTreeCollapseAllAction;
import ca.sqlpower.swingui.JTreeExpandAllAction;
import ca.sqlpower.swingui.MultiDragTreeUI;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;

public class DBTree extends JTree implements DragSourceListener {
	private static Logger logger = Logger.getLogger(DBTree.class);
	
	// actionCommand identifier for actions shared by DBTree
	public static final String ACTION_COMMAND_SRC_DBTREE = "DBTree";
	
	protected JPopupMenu popup;
	protected JMenu dbcsMenu;
	protected JDBCDataSourcePanel spDataSourcePanel;
	protected NewDataSourceAction newDBCSAction;
	private DataSourcePropertiesAction dbcsPropertiesAction;
	private RemoveSourceDBAction removeDBCSAction;
	protected ShowInPlayPenAction showInPlayPenAction;
	protected Action collapseAllAction;
    protected Action expandAllAction;
	protected SetConnAsTargetDB setConnAsTargetDB;
	protected SelectAllChildTablesAction selectAllChildTablesAction;
	/**
	 * A flag that if true, then popup menus will be displayed
	 * when DBTree is right-clicked.
	 */
	private boolean popupMenuEnabled = true;
    
    /**
     * The architect session, so we can access common objects
     */
    private final ArchitectSwingSession session;

	/**
     * The ActionMap key for the action that deletes the selected
     * object in this DBTree.
     */
	private static final Object KEY_DELETE_SELECTED
        = "ca.sqlpower.architect.swingui.DBTree.KEY_DELETE_SELECTED"; //$NON-NLS-1$
	
	private static final String KEY_LEFT = "ca.sqlpower.architect.swingui.DBTree.KEY_LEFT";
	private static final String KEY_RIGHT = "ca.sqlpower.architect.swingui.DBTree.KEY_RIGHT";
	
	/**
	 * The model behind this DB tree.
	 */
	private final DBTreeModel treeModel;

    /**
     * The tree cell renderer that can have other icon filters added to it to
     * give the tree a unique look.
     */
    private final DBTreeCellRenderer treeCellRenderer;
	
	// ----------- CONSTRUCTORS ------------

	public DBTree(final ArchitectSwingSession session) {
        this.session = session;
        if (session.isEnterpriseSession()) {
            treeModel = new DBTreeModel(session.getRootObject(), this, session.getWorkspace().getSnapshotCollection());
        } else {
            treeModel = new DBTreeModel(session.getRootObject(), this);
        }
        setModel(treeModel);
		setUI(new MultiDragTreeUI());
		setRootVisible(false);
		setShowsRootHandles(true);
		if (!GraphicsEnvironment.isHeadless()) {
		    //XXX See http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=3036
		    new DragSource().createDefaultDragGestureRecognizer
		        (this, DnDConstants.ACTION_COPY, new DBTreeDragGestureListener());
		}

        setConnAsTargetDB = new SetConnAsTargetDB(null);
		newDBCSAction = new NewDataSourceAction(session);
		dbcsPropertiesAction = new DataSourcePropertiesAction(session);	
		removeDBCSAction = new RemoveSourceDBAction(this);
		showInPlayPenAction = new ShowInPlayPenAction();
		collapseAllAction = new JTreeCollapseAllAction(this, Messages.getString("DBTree.collapseAllActionName"));
		expandAllAction = new JTreeExpandAllAction(this, Messages.getString("DBTree.expandAllActionName"));
		addMouseListener(new PopupListener());
        treeCellRenderer = new DBTreeCellRenderer();
        getTreeCellRenderer().addIconFilter(new ProfiledTableIconFilter());
        setCellRenderer(getTreeCellRenderer());
        selectAllChildTablesAction = new SelectAllChildTablesAction();
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (getPathForLocation(e.getX(), e.getY()) != null) {
                    Object node = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
                    if (e.getClickCount() == 2 && node instanceof SQLObject && 
                            !((SQLObject) node).getChildrenInaccessibleReasons().isEmpty()) {
                        Throwable firstException = ((SQLObject) node).
                            getChildrenInaccessibleReasons().entrySet().iterator().next().getValue();
                        SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                                Messages.getString("DBTree.exceptionNodeReport"), firstException); //$NON-NLS-1$
                    }
                }
            }
        });
        
        getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copySelection();
            }
        });
        
        getActionMap().put("cut", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cutSelection();
            }
        });
	}
	
	// ----------- INSTANCE METHODS ------------

	/**
	 * Returns a list of all the databases in this DBTree's model.
	 */
	public List<SQLDatabase> getDatabaseList() {
		List<SQLDatabase> databases = new ArrayList<SQLDatabase>();
		TreeModel m = getModel();
		int dbCount = m.getChildCount(m.getRoot());
		for (int i = 0; i < dbCount; i++) {
			Object child = m.getChild(m.getRoot(), i);
			if (child instanceof SQLDatabase) {
			    databases.add((SQLDatabase) child);
			}
		}
		return databases;
	}

	/**
     * Before adding a new connection to the SwingUIProject, check to see
     * if it exists as a connection in the project (which means they're in this
     * tree's model).
	 */
	public boolean dbcsAlreadyExists(SPDataSource spec) throws SQLObjectException {
		SQLObject so = (SQLObject) getModel().getRoot();
		// the children of the root, if they exists, are always SQLDatabase objects
		Iterator<SQLDatabase> it = so.getChildren(SQLDatabase.class).iterator();
		boolean found = false;
		while (it.hasNext() && found == false) {
			SPDataSource dbcs = it.next().getDataSource();
			if (dbcs != null && dbcs.equals(spec)) {
				found = true;
			}
		}
		return found;
	}
	
	/**
	 * If the SPDataSource exists in the DBTree then the SQLDatabase
	 * containing it will be returned. If the SPDataSource does not
	 * exist null will be returned.
	 * @throws SQLObjectException 
	 */
	public SQLDatabase getDatabase(SPDataSource spec) throws SQLObjectException {
	    SQLObject so = (SQLObject) getModel().getRoot();
        // the children of the root, if they exists, are always SQLDatabase objects
        Iterator<SQLDatabase> it = so.getChildren(SQLDatabase.class).iterator();
        boolean found = false;
        while (it.hasNext() && found == false) {
            final SQLDatabase database = (SQLDatabase) it.next();
            SPDataSource dbcs = database.getDataSource();
            if (dbcs.equals(spec)) {
                return database;
            }
        }
        return null;
	}

	/**
     * Pass in a spec, and look for a duplicate in the list of DBCS objects in
     * User Settings.  If we find one, return a handle to it.  If we don't find
     * one, return null.
	 */
	public JDBCDataSource getDuplicateDbcs(SPDataSource spec) {
	    JDBCDataSource dup = null;
		boolean found = false;
		Iterator<JDBCDataSource> it = session.getContext().getConnections().iterator();
		while (it.hasNext() && found == false) {
		    JDBCDataSource dbcs = (JDBCDataSource) it.next();
			if (spec.equals(dbcs)) {
				dup = dbcs;
				found = true;
			}
		}
		return dup;
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
			logger.warn("Unexpected exception while expanding path "+tp, ex); //$NON-NLS-1$
		} finally {
			session.getArchitectFrame().setCursor(null);
		}
	}
	
	// ---------- methods of DragSourceListener -----------
	public void dragEnter(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dragEnter event"); //$NON-NLS-1$
	}

	public void dragOver(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dragOver event"); //$NON-NLS-1$
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		logger.debug("DBTree: got dropActionChanged event"); //$NON-NLS-1$
	}

	public void dragExit(DragSourceEvent dse) {
		logger.debug("DBTree: got dragExit event"); //$NON-NLS-1$
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		logger.debug("DBTree: got dragDropEnd event"); //$NON-NLS-1$
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
				logger.debug("TreePath is: " + p); //$NON-NLS-1$

				if (p != null) {
					logger.debug("selected node object type is: " + p.getLastPathComponent().getClass().getName()); //$NON-NLS-1$
				}

				// if the item is not already selected, select it (and deselect everything else)
				// if the item is already selected, don't touch the selection model
				if (!isPathSelected(p)) {
				    setSelectionPath(p);
				}
				
				if (popupMenuEnabled) {
    				popup = refreshMenu(p);
                    popup.show(e.getComponent(),
                               e.getX(), e.getY());
				}
            } else {
                if ( p == null && !isPress && e.getButton() == MouseEvent.BUTTON1 )
                    setSelectionPath(null);
            }
        }
    }

	/**
	 * Creates a context sensitive menu for manipulating the SQLObjects in the
	 * tree. There are several modes of operations:
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
     * <p>TODO: add in column, table, exported key, imported keys menus; you can figure
     * out where the click came from by checking the TreePath.
	 */
	protected JPopupMenu refreshMenu(TreePath p) {
		logger.debug("refreshMenu is being called."); //$NON-NLS-1$
		JPopupMenu newMenu = new JPopupMenu();
		newMenu.add(session.createDataSourcesMenu());
		
		newMenu.add(new DatabaseConnectionManagerAction(session));

		newMenu.addSeparator();
        newMenu.add(new JMenuItem(expandAllAction));
        newMenu.add(new JMenuItem(collapseAllAction));
        
        expandAllAction.setEnabled(p != null);
        collapseAllAction.setEnabled(p != null);

		if (!isTargetDatabaseNode(p) && isTargetDatabaseChild(p)) {
		    JMenuItem mi;
		    
		    newMenu.addSeparator();

			ArchitectFrame af = session.getArchitectFrame();
            
			// index menu items
			
            mi = new JMenuItem();
            mi.setAction(af.getInsertIndexAction());
            mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLTable) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }
            
            mi = new JMenuItem();
            mi.setAction(af.getEditIndexAction());
            mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLIndex) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }

            newMenu.addSeparator();
            
            // column menu items
            
            mi = new JMenuItem();
            mi.setAction(af.getInsertColumnAction());
            mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLTable || p.getLastPathComponent() instanceof SQLColumn) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }
            
			mi = new JMenuItem();
			mi.setAction(af.getEditColumnAction());
			mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLColumn) {
				mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
            
			newMenu.addSeparator();
			
			// relationship menu items
			
            JMenu setFocus = new JMenu(Messages.getString("Relationship.setFocusMenu"));
            mi = new JMenuItem();
            mi.setAction(af.getFocusToParentAction());
            setFocus.add(mi);
            mi = new JMenuItem();
            mi.setAction(af.getFocusToChildAction());
            setFocus.add(mi);
            newMenu.add(setFocus);
            if (p.getLastPathComponent() instanceof SQLRelationship) {
                setFocus.setEnabled(true);
            } else {
                setFocus.setEnabled(false);
            }
            
            mi = new JMenuItem(af.getReverseRelationshipAction());
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLRelationship) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }
            
            mi = new JMenuItem();
            mi.setAction(af.getEditRelationshipAction());
            mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLRelationship) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }

            // table menu items
            
			newMenu.addSeparator();

			JMenu alignTables = new JMenu(Messages.getString("TablePane.alignTablesMenu"));
            mi = new JMenuItem();
            mi.setAction(af.getAlignTableHorizontalAction());
            alignTables.add(mi);
            mi = new JMenuItem();
            mi.setAction(af.getAlignTableVerticalAction());
            alignTables.add(mi);
            newMenu.add(alignTables);
            if (p.getLastPathComponent() instanceof SQLTable) {
                alignTables.setEnabled(true);
            } else {
                alignTables.setEnabled(false);
            }
            
            mi = new JMenuItem();
            mi.setAction(af.getEditTableAction());
            mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLTable) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }
            
            // other menu items
            
            newMenu.addSeparator();

            mi = new JMenuItem(showInPlayPenAction);
            newMenu.add(mi);
            if (p.getLastPathComponent() instanceof SQLTable ||
                    p.getLastPathComponent() instanceof SQLColumn ||
                    p.getLastPathComponent() instanceof SQLRelationship) {
                mi.setEnabled(true);
            } else {
                mi.setEnabled(false);
            }
            
            newMenu.addSeparator();
            
			mi = new JMenuItem();
			mi.setAction(af.getDeleteSelectedAction());
			mi.setActionCommand(ACTION_COMMAND_SRC_DBTREE);
			newMenu.add(mi);
			if (p.getLastPathComponent() instanceof SQLTable ||
			        p.getLastPathComponent() instanceof SQLColumn ||
			        p.getLastPathComponent() instanceof SQLRelationship ||
                    p.getLastPathComponent() instanceof SQLIndex) {
			    mi.setEnabled(true);
			} else {
				mi.setEnabled(false);
			}
		} else if (p != null && p.getLastPathComponent() instanceof SPObjectSnapshot<?>) {
		    final SPObjectSnapshot<?> snapshot = (SPObjectSnapshot<?>) p.getLastPathComponent();
		    newMenu.addSeparator();

		    newMenu.add(new AbstractAction("Update to latest changes") {
		        @Override
		        public void actionPerformed(ActionEvent e) {
		            session.createUpdateSnapshotRunnable(snapshot).run();
		        }
		    });
		} else if (p != null && !isTargetDatabaseNode(p) && 
		        p.getLastPathComponent() != treeModel.getSnapshotContainer()) { // clicked on DBCS item in DBTree
			newMenu.addSeparator();
			
			newMenu.add(new RefreshAction(session, this));
			
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
			                        tempDB.getChildrenInaccessibleReasons().isEmpty()))
			        {
			            //a new action is needed to maintain the database variable
			            CompareToCurrentAction compareToCurrentAction = new CompareToCurrentAction();
			            compareToCurrentAction.putValue(CompareToCurrentAction.DATABASE,tempDB);
			            JMenuItem popupCompareToCurrent = new JMenuItem(compareToCurrentAction);            
			            newMenu.add(popupCompareToCurrent);
			        }
			    } catch (SQLObjectException e) {
			        SPSUtils.showExceptionDialogNoReport(this, Messages.getString("DBTree.errorCommunicatingWithDb"), e); //$NON-NLS-1$
			    }
			    
			    JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
			    newMenu.add(profile);

                JMenuItem setAsDB = new JMenuItem(new SetConnAsTargetDB(tempDB.getDataSource()));
                newMenu.add(setAsDB);
                
                newMenu.add(new JMenuItem(removeDBCSAction));
                newMenu.addSeparator();
            } else if (p.getLastPathComponent() instanceof SQLSchema){
                //a new action is needed to maintain the schema variable
                CompareToCurrentAction compareToCurrentAction = new CompareToCurrentAction();
                compareToCurrentAction.putValue(CompareToCurrentAction.SCHEMA, p.getLastPathComponent());
                JMenuItem popupCompareToCurrent = new JMenuItem(compareToCurrentAction);            
                newMenu.add(popupCompareToCurrent);
                
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
                newMenu.addSeparator();
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
                } catch (SQLObjectException e) {
                    SPSUtils.showExceptionDialogNoReport(this, Messages.getString("DBTree.errorCommunicatingWithDb"), e); //$NON-NLS-1$
                }
                
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
                newMenu.addSeparator();
            } else if (p.getLastPathComponent() instanceof SQLTable) {
                JMenuItem profile = new JMenuItem(session.getArchitectFrame().getProfileAction());
                newMenu.add(profile);
                JMenuItem selectAllChildTables = new JMenuItem(this.selectAllChildTablesAction);
                newMenu.add(selectAllChildTables);
                newMenu.add(new JMenuItem(new ShowTableContentsAction(session, (SQLTable) p.getLastPathComponent())));
                newMenu.addSeparator();
            }
			
            JMenuItem popupProperties = new JMenuItem(dbcsPropertiesAction);
            newMenu.add(popupProperties);
		}

		// Show exception details (SQLException node can appear anywhere in the hierarchy)
		if (p != null && p.getLastPathComponent() instanceof SQLObject && 
		        !((SQLObject) p.getLastPathComponent()).getChildrenInaccessibleReasons().isEmpty()) {
			newMenu.addSeparator();
            final SQLObject node = (SQLObject) p.getLastPathComponent();
            newMenu.add(new JMenuItem(new AbstractAction(Messages.getString("DBTree.showExceptionDetails")) { //$NON-NLS-1$
                public void actionPerformed(ActionEvent e) {
                    Throwable firstException = ((SQLObject) node).
                        getChildrenInaccessibleReasons().entrySet().iterator().next().getValue();
                    SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                            Messages.getString("DBTree.exceptionNodeReport"), firstException); //$NON-NLS-1$
                }
            }));

            // If the sole child is an exception node, we offer the user a way to re-try the operation
            if (!node.getChildrenInaccessibleReasons().isEmpty()) {
                newMenu.add(new JMenuItem(new AbstractAction(Messages.getString("DBTree.retryActionName")) { //$NON-NLS-1$
                    public void actionPerformed(ActionEvent e) {
                        node.setPopulated(false);
                        node.getChildren(); // forces populate
                    }
                }));
            }
		}

		// add in Show Listeners if debug is enabled
		if (logger.isDebugEnabled()) {
			newMenu.addSeparator();
			JMenuItem showListeners = new JMenuItem("Show Listeners"); //$NON-NLS-1$
			showListeners.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SQLObject so = (SQLObject) getLastSelectedPathComponent();
						if (so != null) {
							JOptionPane.showMessageDialog(DBTree.this, new JScrollPane(new JList(new java.util.Vector<SPListener>(so.getSPListeners()))));
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
	public boolean isTargetDatabaseNode(TreePath tp) {
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
	public boolean isTargetDatabaseChild(TreePath tp) {
		if (tp == null) {
			return false;
		}

		Object[] oo = tp.getPath();
		for (int i = 0; i < oo.length; i++)
			if (session.getTargetDatabase() == oo[i]) return true;
		return false;
	}

	/**
     * Adds the given data source to the db tree as a source database
     * connection.
     * 
     * @param dbcs The data source to be added to the db tree.
     */
	public void addSourceConnection(SPDataSource dbcs) {
	    SQLObject root = (SQLObject) getModel().getRoot();
	    try {
	        // check to see if we've already seen this one (and it's not the playpen database)
	        if (dbcsAlreadyExists(dbcs) && !dbcs.equals(session.getTargetDatabase().getDataSource())) {
	            logger.warn("database already exists in this project."); //$NON-NLS-1$
	            JOptionPane.showMessageDialog(DBTree.this, Messages.getString("DBTree.connectionAlreadyExists", dbcs.getDisplayName()), //$NON-NLS-1$
	                    Messages.getString("DBTree.connectionAlreadyExistsDialogTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
	        } else if (dbcs instanceof JDBCDataSource) {
	            SQLDatabase newDB = new SQLDatabase((JDBCDataSource) dbcs);
	            root.addChild(newDB, root.getChildCount());
	            session.getProjectLoader().setModified(true);
	            // start a thread to poke the new SQLDatabase object...
	            logger.debug("start poking database " + newDB.getName()); //$NON-NLS-1$
	            PokeDBWorker poker = new PokeDBWorker(newDB);
	            session.runInBackground(poker, "PokeDB: " + newDB.getName()); //$NON-NLS-1$
	        } else {
	            JOptionPane.showMessageDialog(DBTree.this, Messages.getString("DBTree.cannotAddConnectionType", dbcs.getClass().toString()), 
	                    Messages.getString("DBTree.cannotAddConnectionTypeTitle"), JOptionPane.INFORMATION_MESSAGE);
	        }
	    } catch (SQLObjectException ex) {
	        logger.warn("Couldn't add new database to tree", ex); //$NON-NLS-1$
	        SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
	                Messages.getString("DBTree.couldNotAddNewConnection"), ex); //$NON-NLS-1$
	    }
	}
    
    protected class SetConnAsTargetDB extends AbstractAction{
        JDBCDataSource dbcs;

        public SetConnAsTargetDB(JDBCDataSource dbcs){
            super(Messages.getString("DBTree.setAsTargetDbActionName")); //$NON-NLS-1$
            this.dbcs  = dbcs;
        }

        public void actionPerformed(ActionEvent e) {
            session.getPlayPen().setDatabaseConnection(dbcs);
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
         * Recursively visits SQLObjects starting at {@link #root}, stopping
         * at the first leaf node (SQLColumn) encountered.
         */
		@Override
		public void doStuff() throws Exception {
		    pokeDatabase(root);
		    logger.debug("successfully poked database " + root.getName()); //$NON-NLS-1$
		}
		
		/**
		 * The recursive subroutine of doStuff(). That means it will be called
		 * on a worker thread, and it shouldn't do anything that needs to be
		 * done on Swing's Event Dispatch Thread!
		 */
		private boolean pokeDatabase(final SQLObject source) throws SQLObjectException {
		    if (logger.isDebugEnabled()) logger.debug("HELLO my class is " + source.getClass().getName() + ", my name is + " + source.getName()); //$NON-NLS-1$ //$NON-NLS-2$
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
		        throw new RuntimeException(getDoStuffException());
		    }
		}
	}

	//the action for clicking on "Compare to current"
	protected class CompareToCurrentAction extends AbstractAction {
	    public static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$
	    public static final String CATALOG = "CATALOG"; //$NON-NLS-1$
	    public static final String DATABASE = "DATABASE"; //$NON-NLS-1$
	    
        public CompareToCurrentAction() {
            super(Messages.getString("DBTree.compareToCurrentActionName")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            //gets the database and catalog from the tree
            SQLSchema schema = null;
            SQLDatabase db = null;
            SQLCatalog catalog = null;
            
            if (getValue(SCHEMA)!= null) {
                schema = (SQLSchema)getValue(SCHEMA);
                //oracle does not have catalogs so a check is needed
                if (schema.getParent().getParent() instanceof SQLDatabase) {
                    db = (SQLDatabase)schema.getParent().getParent();
                    catalog = (SQLCatalog)schema.getParent();                    
                } else {
                    db = (SQLDatabase)schema.getParent();                    
                }
            } else if (getValue(CATALOG) != null) {
                catalog = (SQLCatalog)getValue(CATALOG);
                db = (SQLDatabase)catalog.getParent();
            } else if (getValue(DATABASE) != null) {
                db = (SQLDatabase)getValue(DATABASE);
            }
            
            CompareDMDialog compareDialog = new CompareDMDialog(session);
            compareDialog.setVisible(true);
            //sets to the right settings
            compareDialog.compareCurrentWithOrig(schema,catalog, db);
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
			logger.info("Drag gesture event: " + dge); //$NON-NLS-1$

			// we only start drags on left-click drags
			InputEvent ie = dge.getTriggerEvent();
			if ( (ie.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
				return;
			}
			
			DBTree t = (DBTree) dge.getComponent();
  			final Set<SQLObject> sqlObjectsToCopy = t.findSQLObjectsToCopy();
  			if (!sqlObjectsToCopy.isEmpty()) {
  			    Transferable transferObject = new SQLObjectSelection(sqlObjectsToCopy);
  			    if (transferObject != null) {
  			        // TODO add undo event
  			        
  			        dge.getSourceAsDragGestureRecognizer().setSourceActions(DnDConstants.ACTION_COPY);
  			        dge.getDragSource().startDrag
  			                (dge,
  			                null, //DragSource.DefaultCopyNoDrop,
  			                transferObject,
  			                t);
  			    }
  			}
		}

	}
 	
 	public void setupKeyboardActions() {
        final ArchitectFrame frame = session.getArchitectFrame();

        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
        
        getActionMap().put(KEY_DELETE_SELECTED, new AbstractAction(){
            public void actionPerformed(ActionEvent evt) {
                TreePath tp = getSelectionPath();
                if (tp != null) {
                    if (!isTargetDatabaseNode(tp) && isTargetDatabaseChild(tp)) {
                        frame.getDeleteSelectedAction().actionPerformed(evt);
                    } else if (!isTargetDatabaseNode(tp) && tp.getLastPathComponent() instanceof SQLDatabase) {
                        removeDBCSAction.actionPerformed(evt);
                    }
                }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KEY_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KEY_RIGHT);
        
        getActionMap().put(KEY_LEFT, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePath selection = getSelectionPath();
                if (selection != null) {
                    if (isExpanded(selection) && treeModel.getChildCount(selection.getLastPathComponent()) != 0) {
                        setExpandedState(selection, false);
                    } else {
                        if (selection.getPathCount() != 2) {
                            setSelectionPath(selection.getParentPath());
                        }
                    }
                }
            }
        });
        
        getActionMap().put(KEY_RIGHT, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePath selection = getSelectionPath();
                if (selection != null) {
                    if (!(treeModel.isLeaf(selection.getLastPathComponent()) || isExpanded(selection))) {
                        setExpandedState(selection, true);
                    }
                }
            }
        });
    }
 	
 	/**
     * Creates a list of SQLObjects to decide which of the selected object(s)
     * in the tree are to be copied or dragged to a different location.
     * An empty set will be returned if the transferable cannot be created.
     * <p>
     * This is package private for testing purposes.
     */
    Set<SQLObject> findSQLObjectsToCopy() {
        TreePath[] p = this.getSelectionPaths();
        Set<SQLObject> objectsToTransfer = new HashSet<SQLObject>();
        if (p ==  null || p.length == 0) {
            // nothing to export
            return new HashSet<SQLObject>();
        } else {
            
            //If there are multiple selections, then transfer the objects
            //that are of the same type. To choose what type to move look
            //at the ancestors of each component to decide if (a) items
            //selected are parents of other components and (b) where is the
            //highest point in the tree path that multiple objects of the same
            //type are selected.
            Map<Integer, List<SQLObject>> pathLengthsToSelectedObjectsMap = new HashMap<Integer, List<SQLObject>>();
            for (int i = 0; i < p.length; i++) {
                if (!(p[i].getLastPathComponent() instanceof SQLObject)) continue;
                if (pathLengthsToSelectedObjectsMap.get(p[i].getPathCount()) == null) {
                    pathLengthsToSelectedObjectsMap.put(p[i].getPathCount(), new ArrayList<SQLObject>());
                }
                pathLengthsToSelectedObjectsMap.get(p[i].getPathCount()).add((SQLObject) p[i].getLastPathComponent());
            }
            
            List<Integer> sortedLengths = new ArrayList<Integer>(pathLengthsToSelectedObjectsMap.keySet());
            Collections.sort(sortedLengths);
            
            for (int i = 0; i < sortedLengths.size(); i++) {
                Integer pathCount = sortedLengths.get(i);
                //get the length of each tree path to see if there are multiples
                //at the lowest length
                if (pathLengthsToSelectedObjectsMap.get(pathCount).size() > 1) {
                    objectsToTransfer.addAll(pathLengthsToSelectedObjectsMap.get(pathCount));
                    
                    for (int j = i + 1; j < sortedLengths.size(); j++) {
                        for (SQLObject childObject : pathLengthsToSelectedObjectsMap.get(sortedLengths.get(j))) {
                            SQLObject parent = childObject;
                            logger.debug("Initial Object before recursively go to parent is: " + parent);
                            for (int k = 0; k < sortedLengths.get(j) - pathCount; k++) {
                                if (!(parent.getParent() instanceof SQLObject)) break;
                                parent = (SQLObject) parent.getParent();
                            }
                            objectsToTransfer.add(parent);
                            logger.debug("Final Object after recursively go to parent is: " + parent);
                        }
                    }
                    break;
                } else {
                    //if there is only one at the lowest length we need to see if it
                    //is the parent of the rest of the elements
                    SQLObject singleParent = pathLengthsToSelectedObjectsMap.get(pathCount).get(0);
                    boolean isParentOfAllSelected = true;
                    for (int j = i + 1; j < sortedLengths.size(); j++) {
                        for (SQLObject childObject : pathLengthsToSelectedObjectsMap.get(sortedLengths.get(j))) {
                            SQLObject parent = childObject;
                            boolean parented = false;
                            for (int k = 0; k < sortedLengths.get(j) - pathCount; k++) {
                                if (!(parent.getParent() instanceof SQLObject)) break;
                                parent = (SQLObject) parent.getParent();
                                if (parent == singleParent) {
                                    parented = true;
                                    break;
                                }
                            }
                            if (!parented) {
                                isParentOfAllSelected = false;
                                break;
                            }
                        }
                        if (!isParentOfAllSelected) {
                            break;
                        }
                    }
                    //if it is not the parent then that is the depth we will copy and need to find the parent
                    //of all other selections
                    if (!isParentOfAllSelected) {
                        objectsToTransfer.addAll(pathLengthsToSelectedObjectsMap.get(pathCount));

                        for (int j = i + 1; j < sortedLengths.size(); j++) {
                            for (SQLObject childObject : pathLengthsToSelectedObjectsMap.get(sortedLengths.get(j))) {
                                SQLObject parent = childObject;
                                for (int k = 0; k < sortedLengths.get(j) - pathCount; k++) {
                                    if (!(parent.getParent() instanceof SQLObject)) break;
                                    parent = (SQLObject) parent.getParent();
                                }
                                objectsToTransfer.add(parent);
                            }
                        }
                        break;
                    }
                }
                //if it is the parent of the rest then we only need to copy selections that are lower
                //but if it is the lowest component in the selections then the elements at this level
                //should be selected.
                if (i + 1 == sortedLengths.size()) {
                    objectsToTransfer.addAll(pathLengthsToSelectedObjectsMap.get(pathCount));
                }
                
            }
             
            logger.info("DBTree: exporting list of DnD-type tree paths"); //$NON-NLS-1$

        }
        if (logger.isDebugEnabled()) {
        	for(SQLObject object: objectsToTransfer) {
            	logger.debug("The object to transfer copy is: " + object);
        	}
        }
        return objectsToTransfer;
    }
 	
 	/**
 	 * This will copy the selection of the DBTree to the system's clipboard
 	 * via a Transferable.
 	 */
 	public void copySelection() {
 	    Transferable transferObject = new SQLObjectSelection(findSQLObjectsToCopy());
 	    if (transferObject != null) {
 	        session.getContext().setClipboardContents(transferObject);
 	    }
 	}
 	
 	/**
     * This will cut the selection of the DBTree to the system's clipboard
     * via a Transferable.
     */
 	public void cutSelection() {
 	   final Set<SQLObject> copyObjects = findSQLObjectsToCopy();
 	   Transferable transferObject = new SQLObjectSelection(copyObjects);
       if (transferObject != null) {
           session.getContext().setClipboardContents(transferObject);
       }
       for (SQLObject o : copyObjects) {
           SQLDatabase target = session.getTargetDatabase();
           if(o.equals(target)) break;
           if (SQLObjectUtils.ancestorList(o).contains(target)) {
               try {
                   o.getParent().removeChild(o);
               } catch (ObjectDependentException e) {
                    // FIXME Add an actual method for dealing with dependencies
                    // here. As of now, nothing in Architect has depencies, so
                    // this won't be hit, but in the future, this will need to
                    // be changed.
                   throw new RuntimeException(e);
               }
           }
       }
 	}
 	

 	/**
 	 * Removes all selections of objects that are not represented on the playpen.
 	 */
    public void clearNonPlayPenSelections() {
        if (getSelectionPaths() == null) return;
        for (TreePath tp : getSelectionPaths()) {
            SPObject obj = (SPObject) tp.getLastPathComponent();
            if (!(obj instanceof SQLTable || obj instanceof SQLRelationship || obj instanceof SQLColumn || obj instanceof SQLRelationship.SQLImportedKey)) {
                removeSelectionPath(tp);
            }
        }
    }
    
    /**
     * Returns the TreePath built from the getParent() of the given SQLObject.
     * 
     * @param obj SQLObject to build TreePath upon.
     * @return TreePath for given object.
     */
    public TreePath getTreePathForNode(SQLObject obj) {
        return new TreePath(treeModel.getPathToNode(obj));
    }
    
    public void setPopupMenuEnabled(boolean popupMenuEnabled) {
        this.popupMenuEnabled = popupMenuEnabled;
    }

    public boolean isPopupMenuEnabled() {
        return popupMenuEnabled;
    }

    public DBTreeCellRenderer getTreeCellRenderer() {
        return treeCellRenderer;
    }
    
    protected class ShowInPlayPenAction extends AbstractAction {
        public ShowInPlayPenAction() {
            super(Messages.getString("DBTree.showInPlaypenAction")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e) {
            session.getPlayPen().showSelected();
        }
    }
    
    /**
     * Adds to selection all child tables of the current table
     */
    protected class SelectAllChildTablesAction extends AbstractAction {
        public SelectAllChildTablesAction() {
            super(Messages.getString("DBTree.selectAllChildTablesActionName")); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e) {
            TreePath selected = getSelectionPath();
            try {
                if (selected == null) {
                    return;
				} else {
                    SQLTable centralTable = (SQLTable)selected.getLastPathComponent();
                    List<SQLRelationship> exportedKeys = centralTable.getExportedKeys();
                    for(int i = 0; i < exportedKeys.size(); i++) {
                        SQLTable childTable = exportedKeys.get(i).getFkTable();
                        DBTree.this.addSelectionPath(getTreePathForNode(childTable));
                    }
                }
            } catch (SQLObjectException ex) {
                logger.debug("Failed to select all child tables", ex);
                throw new SQLObjectRuntimeException(ex);
            }
        }
    }

}