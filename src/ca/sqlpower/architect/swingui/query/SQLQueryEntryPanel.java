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

package ca.sqlpower.architect.swingui.query;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.DnDTreePathTransferable;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
import ca.sqlpower.architect.swingui.query.action.AbstractSQLQueryAction;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This JPanel contains a text area to enter SQL script into and execute it.
 */
public class SQLQueryEntryPanel extends JPanel {
    
    private static Logger logger = Logger.getLogger(SQLQueryEntryPanel.class);
    
    /**
     * The entry value in the input map that will map a key press to our
     * "Execute" action.
     */
    private static final String EXECUTE_QUERY_ACTION = "Execute Query";

    /**
     * The entry value in the input map that will map a key press to our
     * undo action on the sql edit text area.
     */

    private static final Object UNDO_SQL_EDIT = "Undo SQL Edit";

    /**
     * The entry value in the input map that will map a key press to our
     * redo action on the sql edit text area.
     */
    private static final Object REDO_SQL_EDIT = "Redo SQL Edit";
    
    /**
     * This QueryDropListener Listens to the SQLObjects being dragged onto the
     * QueryTextArea from a DBTree. When an object is dragged onto the text area
     * the object's name will be entered at the caret position.
     */
    private class QueryDropListener implements DropTargetListener {
        
        private DBTree dbTree;
        
        public QueryDropListener(DBTree dbtree) {
            this.dbTree = dbtree;
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            logger.debug("we are in dragEnter");
            
        }

        public void dragExit(DropTargetEvent dte) {
            logger.debug("we are in dragExit");
            
        }

        public void dragOver(DropTargetDragEvent dtde) {
            logger.debug("we are in dragOver");
            
        }
        
        public void drop(DropTargetDropEvent dtde) {
            DataFlavor[] flavours = dtde.getTransferable().getTransferDataFlavors();
            DataFlavor bestFlavour = null;
            for (int i = 0; i < flavours.length; i++) {
                if (flavours[i] != null) {
                    bestFlavour = flavours[i];
                    break;
                }
            }
            try {
                ArrayList paths = (ArrayList) dtde.getTransferable().getTransferData(bestFlavour);
                Iterator it = paths.iterator();
                while(it.hasNext()) {
                    Object oo = DnDTreePathTransferable.getNodeForDnDPath((SQLObject) dbTree.getModel().getRoot(), (int[])it.next());
                    if (oo instanceof SQLTable) {
                        SQLTable table = ((SQLTable) oo);
                        StringBuffer buffer = new StringBuffer();
                        if (!table.getCatalogName().equals("")) {
                            buffer.append(table.getCatalogName());
                            buffer.append(".");
                        }
                        if(!table.getSchemaName().equals("")) {
                            buffer.append(table.getSchemaName());
                            buffer.append(".");
                        }
                        buffer.append(table.getPhysicalName());
                        queryArea.insert(buffer.toString(), queryArea.getCaretPosition());
                    } else if (oo instanceof SQLObject) {
                        queryArea.insert(((SQLObject) oo).getPhysicalName(), queryArea.getCaretPosition());
                    } else {
                        logger.error("Unknown object dropped in PlayPen: "+oo);
                    }
                }
                dtde.dropComplete(true);
            } catch (UnsupportedFlavorException e) {
                logger.error(e);
                dtde.rejectDrop();
            } catch (IOException e) {
                logger.error(e);
                dtde.rejectDrop();
            } catch (ArchitectException e) {
                logger.error(e);
                dtde.rejectDrop();
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            logger.debug("we are in dropActionChange");
            
        }
        
    }
    
    /**
     * A listener for item selection on a combo box containing {@link SPDataSource}s.
     * This will create a new entry in the connection map to store a live connection
     * for the selected database.
     */
    private class DatabaseItemListener implements ItemListener {
        
        private JPanel parent;
        
        public DatabaseItemListener(JPanel parent) {
            this.parent = parent;
        }
        
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            if (!conMap.containsKey(e.getItem())) {
                SPDataSource ds = (SPDataSource)e.getItem();
                try {
                    Connection con = ds.createConnection();
                    conMap.put(ds, new ConnectionAndStatementBean(con));
                } catch (SQLException e1) {
                    SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedConnectingToDBWithName", ds.getName()), e1);
                    return;
                }
            }
            try {
                autoCommitToggleButton.setSelected(conMap.get(e.getItem()).getConnection().getAutoCommit());
            } catch (SQLException ex) {
                SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedConnectingToDB"), ex);
            }
            stopButton.setEnabled(conMap.get(e.getItem()).getCurrentStmt() != null);
            executeButton.setEnabled(conMap.get(e.getItem()).getCurrentStmt() == null);
        }
    }
    
    /**
     * This will execute the sql statement in the sql text area.
     */
    private class ExecuteSQLWorker extends SPSwingWorker {
        
        List<CachedRowSet> resultSets = new ArrayList<CachedRowSet>();
        List<Integer> rowsAffected = new ArrayList<Integer>();

        public ExecuteSQLWorker(SwingWorkerRegistry registry) {
            super(registry);
        }

        @Override
        public void cleanup() throws Exception {
            Throwable e = getDoStuffException();
            if (e != null) {
                if (e instanceof SQLException) {
                    SPSUtils.showExceptionDialogNoReport(getParent(), Messages.getString("SQLQuery.failedConnectingToDB"), e);
                } else {
                    throw new RuntimeException(e);
                }
            }
            
            for (ExecuteActionListener listener : executeListeners) {
                List<ResultSet> newRSList = new ArrayList<ResultSet>();
                for (CachedRowSet rs : resultSets) {
                    newRSList.add(rs.createShared());
                }
                listener.sqlQueryExecuted(newRSList, rowsAffected);
            }
        }

        @Override
        public void doStuff() throws Exception {
            logger.debug("Starting execute action.");
            SPDataSource ds = (SPDataSource)databases.getSelectedItem();
            if (ds == null) {
                return;
            }
            Connection con = conMap.get(ds).getConnection();
            Statement stmt = null;
            try {
                executeButton.setEnabled(false);
                stopButton.setEnabled(true);
                stmt = con.createStatement();
                conMap.get(ds).setCurrentStmt(stmt);
                try {
                    rowLimitSpinner.commitEdit();
                } catch (ParseException e1) {
                    // If the spinner can't parse it's current value set it to it's previous
                    // value to keep it an actual number.
                    rowLimitSpinner.setValue(rowLimitSpinner.getValue());
                }
                int rowLimit = ((Integer) rowLimitSpinner.getValue()).intValue();
                logger.debug("Row limit is " + rowLimit);
                
                stmt.setMaxRows(rowLimit);
                String sql = queryArea.getText();
                logger.debug("Executing statement " + sql);
                boolean sqlResult = stmt.execute(sql);
                logger.debug("Finished execution");
                boolean hasNext = true;
                
                while (hasNext) {
                    if (sqlResult) {
                        ResultSet rs = stmt.getResultSet();
                        CachedRowSet rowSet = new CachedRowSet();
                        logger.debug("Populating cached row set");
                        rowSet.populate(rs);
                        logger.debug("Result set row count is " + rowSet.size());
                        resultSets.add(rowSet);
                        rowsAffected.add(new Integer(rowSet.size()));
                        rs.close();
                    } else {
                        rowsAffected.add(new Integer(stmt.getUpdateCount()));
                        logger.debug("Update count is : " + stmt.getUpdateCount());
                    }
                    sqlResult = stmt.getMoreResults();
                    hasNext = !((sqlResult == false) && (stmt.getUpdateCount() == -1));
                }
                logger.debug("Finished Execute method");
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    conMap.get(ds).setCurrentStmt(null);
                }
                executeButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        }
        
    }
    
    
    /**
     * The worker that the execute action runs on to query the database and create the
     * result sets.
     */
    private ExecuteSQLWorker sqlExecuteWorker;
    
    /**
     * The action for executing and displaying a user's query.
     */
    private final AbstractAction executeAction = new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.execute")) {

        public void actionPerformed(ActionEvent e) {
            ConnectionAndStatementBean conBean = conMap.get(databases.getSelectedItem());
            try {
                if(conBean!= null) {
                    if (!conBean.getConnection().getAutoCommit()) {
                        conBean.setConnectionUncommitted(true);
                    }
                }
            } catch (SQLException e1) {
                SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedRetrievingConnection", ((SPDataSource)databases.getSelectedItem()).getName()), e1);
            }
            sqlExecuteWorker = new ExecuteSQLWorker(session);
            new Thread(sqlExecuteWorker).start();
        }
    };
    
    /**
     * A mapping of data sources to live connections. These connections will be left
     * open until the panel's ancestor is closed. The connections are kept open so 
     * auto commit can be turned off and users can enter multiple queries before 
     * committing or rolling back. Additionally, it will allow switching of data
     * sources while keeping the commit or rollback execution sequence preserved.
     */
    private Map<SPDataSource, ConnectionAndStatementBean> conMap;
    
    /**
     * The text area users can enter SQL queries to get data from the database.
     */
    private final JTextArea queryArea;
    
    /**
     * A combo box of available connections the user have specified. The selected
     * one will have the query run on it when the user hits the execute button.
     */
    private final JComboBox databases;
    
    private DropTarget dt;
    
    
    /**
     * A JTextField for the user to enter the row limit of a query.
     */
    private final JSpinner rowLimitSpinner;
    
    /**
     * Toggles auto commit on an off for the selected connection.
     */
    private final JToggleButton autoCommitToggleButton;
    
    /**
     * Commits the changes made on the currently selected connection.
     */
    private final JButton commitButton;
    
    /**
     * Rolls back the changes made on the currently selected connection.
     */
    private final JButton rollbackButton;
    
    /**
     * Listeners that will have it's sqlQueryExecuted method called when a successful
     * query is run. 
     */
    private List<ExecuteActionListener> executeListeners;
    
    private final ArchitectSwingSession session;
    
    /**
     * The undo manager for the text area containing the SQL statement.
     */
    private UndoManager undoManager;
    
    private Action undoSQLStatementAction = new AbstractAction(Messages.getString("SQLQuery.undo")){

        public void actionPerformed(ActionEvent arg0) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
            
        }
    };
        
    private Action redoSQLStatementAction = new AbstractAction(Messages.getString("SQLQuery.redo")){

        public void actionPerformed(ActionEvent arg0) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
            
        }
    };
    
    
    /**
     * This recreates the database combo box when the list of databases changes.
     */
    private DatabaseListChangeListener dbListChangeListener = new DatabaseListChangeListener() {

        public void databaseAdded(DatabaseListChangeEvent e) {
            databases.addItem(e.getDataSource());
        }

        public void databaseRemoved(DatabaseListChangeEvent e) {
            if (databases.getSelectedItem() != null && databases.getSelectedItem().equals(e.getDataSource())) {
                databases.setSelectedItem(null);
            }
            
            databases.removeItem(e.getDataSource());
        }
        
    };

    /**
     * Listens to when the an ancestor is added or removed. This will clean up open
     * connections and remove handlers when the ancestor is removed.
     */
    private AncestorListener closeListener = new AncestorListener(){

        public void ancestorAdded(AncestorEvent event) {
            session.getContext().getPlDotIni().addDatabaseListChangeListener(dbListChangeListener);
        }

        public void ancestorMoved(AncestorEvent event) {
        }

        public void ancestorRemoved(AncestorEvent event) {
            logger.debug("Removing database list change listener");
            session.getContext().getPlDotIni().removeDatabaseListChangeListener(dbListChangeListener);
            
            for (Map.Entry<SPDataSource, ConnectionAndStatementBean> entry : conMap.entrySet()) {
                try {
                    Connection con = entry.getValue().getConnection();
                    if (!con.getAutoCommit() && entry.getValue().isConnectionUncommitted()) {
                        int result = JOptionPane.showOptionDialog(session.getArchitectFrame(), Messages.getString("SQLQuery.commitOrRollback", entry.getKey().getName()),
                                Messages.getString("SQLQuery.commitOrRollbackTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                new Object[] {Messages.getString("SQLQuery.commit"), Messages.getString("SQLQuery.rollback")}, Messages.getString("SQLQuery.commit"));
                        if (result == JOptionPane.OK_OPTION) {
                            con.commit();
                        } else if (result == JOptionPane.CANCEL_OPTION) {
                            con.rollback();
                        }
                    }
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            
        }};

    /**
     * This button will execute the sql statements in the text area.
     */
    private JButton executeButton;

    /**
     * This button will stop the execution of the currently executing statement
     * on the selected data source's connection that this panel holds.
     */
    private JButton stopButton;
    
    /**
     * Creates a SQLQueryEntryPanel and attaches a drag and drop listener
     * to a DB Tree.
     */
    public SQLQueryEntryPanel(ArchitectSwingSession session, DBTree dbTree) {
        this(session);
        
        dt = new DropTarget(queryArea, new QueryDropListener(dbTree));
    }
    
    public SQLQueryEntryPanel(ArchitectSwingSession s) {
        super();
        this.session = s;
        
        autoCommitToggleButton = new JToggleButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.autoCommit")) {
        
            public void actionPerformed(ActionEvent e) {
                Connection con = conMap.get(databases.getSelectedItem()).getConnection();
                if (con == null) {
                    return;
                }
                try {
                    boolean isPressed = autoCommitToggleButton.getModel().isSelected();
                    if (isPressed && conMap.get(databases.getSelectedItem()).isConnectionUncommitted()) {
                        int result = JOptionPane.showOptionDialog(parent, Messages.getString("SQLQuery.commitOrRollbackBeforeAutoCommit"),
                                Messages.getString("SQLQuery.commitOrRollbackTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                new Object[] {Messages.getString("SQLQuery.commit"), Messages.getString("SQLQuery.cancel"), Messages.getString("SQLQuery.rollback")}, Messages.getString("SQLQuery.commit"));
                        if (result == JOptionPane.OK_OPTION) {
                            commitCurrentDB();
                        } else if (result == JOptionPane.CANCEL_OPTION) {
                            rollbackCurrentDB();
                        } else {
                            ((JToggleButton)e.getSource()).setSelected(con.getAutoCommit());
                            return;
                        }
                         
                        
                    }
                    con.setAutoCommit(isPressed);
                    logger.debug("The auto commit button is toggled " + isPressed);
                } catch (SQLException ex) {
                    SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedAutoCommit"), ex);
                }
        
            }
        
        });
        
        autoCommitToggleButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (autoCommitToggleButton.isSelected()) {
                    commitButton.setEnabled(false);
                    rollbackButton.setEnabled(false);
                } else {
                    commitButton.setEnabled(true);
                    rollbackButton.setEnabled(true);
                }
            }
        });
        
        commitButton = new JButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.commit")) {
            public void actionPerformed(ActionEvent e) {
                commitCurrentDB();
            }});
        
        rollbackButton = new JButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.rollback")){
            public void actionPerformed(ActionEvent e) {
                rollbackCurrentDB();
            }});
        
        
        rowLimitSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
        executeListeners = new ArrayList<ExecuteActionListener>();
        queryArea = new JTextArea();
        undoManager = new UndoManager();
        queryArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
        queryArea.getActionMap().put(UNDO_SQL_EDIT, undoSQLStatementAction);
        queryArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), UNDO_SQL_EDIT);
        
        queryArea.getActionMap().put(REDO_SQL_EDIT, redoSQLStatementAction);
        queryArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK), REDO_SQL_EDIT);
        
        conMap = new HashMap<SPDataSource, ConnectionAndStatementBean>();
        
        databases = new JComboBox(s.getContext().getConnections().toArray());
        databases.setSelectedItem(null);
        databases.addItemListener(new DatabaseItemListener(this));
        
        addAncestorListener(closeListener);
        
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                , EXECUTE_QUERY_ACTION);
        getActionMap().put(EXECUTE_QUERY_ACTION, executeAction);
        
        buildUI();
    }
    
    /**
     * Builds the UI of the {@link SQLQueryEntryPanel}.
     */
    private void buildUI() {
        JToolBar toolbar = new JToolBar();
        executeButton = new JButton(executeAction);
        toolbar.add(executeButton);
        stopButton = new JButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.stop")) {
            public void actionPerformed(ActionEvent arg0) {
                ConnectionAndStatementBean conBean = conMap.get(databases.getSelectedItem());
                if (conBean != null) {
                    Statement stmt = conBean.getCurrentStmt();
                    if (stmt != null) {
                        try {
                            logger.debug("stmt is being cancelled...supposely");
                            stmt.cancel();
                            if (sqlExecuteWorker != null) {
                                sqlExecuteWorker.kill();
                            }
                        } catch (SQLException e) {
                            SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.stopException", ((SPDataSource)databases.getSelectedItem()).getName()), e);
                        }
                    }
                }
            }
        });
        toolbar.add(stopButton);
        toolbar.add(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.clear")){
            public void actionPerformed(ActionEvent arg0) {
                queryArea.setText("");
            }});
        toolbar.addSeparator();
        toolbar.add(autoCommitToggleButton);
        toolbar.add(commitButton);
        toolbar.add(rollbackButton);
        toolbar.addSeparator();
        toolbar.add(undoSQLStatementAction);
        toolbar.add(redoSQLStatementAction);
        
        FormLayout textAreaLayout = new FormLayout(
                "pref:grow, 10dlu, pref, 10dlu, pref, 10dlu, pref"
                , "pref, pref, fill:max(100dlu;pref):grow");
        DefaultFormBuilder textAreaBuilder = new DefaultFormBuilder(textAreaLayout, this);
        textAreaBuilder.setDefaultDialogBorder();
        textAreaBuilder.append(toolbar, 7);
        textAreaBuilder.nextLine();
        textAreaBuilder.append(databases);
        JButton dbcsManagerButton = new JButton(new DatabaseConnectionManagerAction(session));
        dbcsManagerButton.setText(Messages.getString("SQLQuery.mangeConnections"));
        textAreaBuilder.append(dbcsManagerButton);
        textAreaBuilder.append(Messages.getString("SQLQuery.rowLimit"));
        textAreaBuilder.append(rowLimitSpinner);
        textAreaBuilder.nextLine();
        textAreaBuilder.append(new JScrollPane(queryArea), 7);
       
  
    }
    
    public void addExecuteAction(ExecuteActionListener l) {
        executeListeners.add(l);
    }
    
    public void removeExecuteAction(ExecuteActionListener l) {
        executeListeners.remove(l);
    }
    
    /**
     * If the connection to the database currently selected in the combo box is not in 
     * auto commit mode then any changes will be committed.
     */
    private void commitCurrentDB() {
        ConnectionAndStatementBean conBean = conMap.get(databases.getSelectedItem());
        Connection con = conBean.getConnection();
        if (con == null) {
            return;
        }
        try {
            if (!con.getAutoCommit()) {
                con.commit();
                conBean.setConnectionUncommitted(false);
            }
        } catch (SQLException ex) {
            SPSUtils.showExceptionDialogNoReport(this, Messages.getString("SQlQuery.failedCommit"), ex);
        }
    }
    
    /**
     * If the connection to the database currently selected in the combo box is not in 
     * auto commit mode then any changes will be rolled back.
     */
    private void rollbackCurrentDB() {
        ConnectionAndStatementBean conBean = conMap.get(databases.getSelectedItem());
        Connection con = conBean.getConnection();
        if (con == null) {
            return;
        }
        try {
            if (!con.getAutoCommit()) {
                con.rollback();
                conBean.setConnectionUncommitted(false);
            }
        } catch (SQLException ex) {
            SPSUtils.showExceptionDialogNoReport(this, Messages.getString("SQLQuery.failedRollback"), ex);
        }
    }

}
