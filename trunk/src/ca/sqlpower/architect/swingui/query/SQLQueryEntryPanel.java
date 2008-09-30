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
            if (!conMap.containsKey(e.getItem())) {
                SPDataSource ds = (SPDataSource)e.getItem();
                try {
                    Connection con = ds.createConnection();
                    conMap.put(ds, con);
                } catch (SQLException e1) {
                    SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedConnectingToDB"), e1);
                }
            }
            try {
                autoCommitToggleButton.setSelected(conMap.get(e.getItem()).getAutoCommit());
            } catch (SQLException ex) {
                SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedConnectingToDB"), ex);
            }
        }
    }
    
    /**
     * The action for executing and displaying a user's query.
     */
    private final AbstractAction executeAction = new AbstractAction(Messages.getString("SQLQuery.execute")) {
        public void actionPerformed(ActionEvent e) {
            logger.debug("Starting execute action.");
            SPDataSource ds = (SPDataSource)databases.getSelectedItem();
            if (ds == null) {
                return;
            }
            Connection con = conMap.get(ds);
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.createStatement();
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
                boolean sqlResult = stmt.execute(queryArea.getText());
                
                if (sqlResult) {
                    rs = stmt.getResultSet();
                    CachedRowSet rowSet = new CachedRowSet();
                    rowSet.populate(rs);
                    logger.debug("Result set row count is " + rowSet.size());
                    
                    for (ExecuteActionListener listener : executeListeners) {
                        listener.sqlQueryExecuted(rowSet);
                    }
                } else {
                    // TODO: Send the update count to the listeners.
                    logger.debug("Update count is : " + stmt.getUpdateCount());
                }

            } catch (SQLException ex) {
                SPSUtils.showExceptionDialogNoReport(getParent(), Messages.getString("SQLQuery.failedConnectingToDB"), ex);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    };
    
    /**
     * A mapping of data sources to live connections. These connections will be left
     * open until the panel's ancestor is closed. The connections are kept open so 
     * auto commit can be turned off and users can enter multiple queries before 
     * committing or rolling back. Additionally, it will allow switching of data
     * sources while keeping the commit or rollback execution sequence preserved.
     */
    private Map<SPDataSource, Connection> conMap;
    
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
     * This recreates the database combo box when the list of databases changes.
     */
    private DatabaseListChangeListener dbListChangeListener = new DatabaseListChangeListener() {

        public void databaseAdded(DatabaseListChangeEvent e) {
            recreateComboBox();
        }

        private void recreateComboBox() {
            databases.removeAllItems();
            for (SPDataSource ds : session.getContext().getConnections()) {
                databases.addItem(ds);
            }
        }

        public void databaseRemoved(DatabaseListChangeEvent e) {
            recreateComboBox();
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
            
            for (Map.Entry<SPDataSource, Connection> entry : conMap.entrySet()) {
                try {
                    Connection con = entry.getValue();
                    if (!con.getAutoCommit()) {
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
                Connection con = conMap.get(databases.getSelectedItem());
                if (con == null) {
                    return;
                }
                try {
                    boolean isPressed = autoCommitToggleButton.getModel().isSelected();
                    if (isPressed) {
                        int result = JOptionPane.showOptionDialog(parent, Messages.getString("SQLQuery.commitOrRollbackBeforeAutoCommit"),
                                Messages.getString("SQLQuery.commitOrRollbackTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                new Object[] {Messages.getString("SQLQuery.commit"), Messages.getString("SQLQuery.cancel"), Messages.getString("SQLQuery.rollback")}, Messages.getString("SQLQuery.commit"));
                        if (result == JOptionPane.OK_OPTION) {
                            con.commit();
                        } else if (result == JOptionPane.CANCEL_OPTION) {
                            con.rollback();
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
        
        commitButton = new JButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.commit")){
            public void actionPerformed(ActionEvent e) {
                Connection con = conMap.get(databases.getSelectedItem());
                if (con == null) {
                    return;
                }
                try {
                    if (!con.getAutoCommit()) {
                        con.commit();
                    }
                } catch (SQLException ex) {
                    SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQlQuery.failedCommit"), ex);
                }
            }});
        
        rollbackButton = new JButton(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.rollback")){

            public void actionPerformed(ActionEvent e) {
                Connection con = conMap.get(databases.getSelectedItem());
                if (con == null) {
                    return;
                }
                try {
                    if (!con.getAutoCommit()) {
                        con.rollback();
                    }
                } catch (SQLException ex) {
                    SPSUtils.showExceptionDialogNoReport(parent, Messages.getString("SQLQuery.failedRollback"), ex);
                }
                
            }});
        
        rowLimitSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, Integer.MAX_VALUE, 1));
        executeListeners = new ArrayList<ExecuteActionListener>();
        queryArea = new JTextArea();
        
        conMap = new HashMap<SPDataSource, Connection>();
        
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
        toolbar.add(executeAction);
        toolbar.add(new AbstractSQLQueryAction(this, Messages.getString("SQLQuery.clear")){
            public void actionPerformed(ActionEvent arg0) {
                queryArea.setText("");
            }});
        toolbar.addSeparator();
        toolbar.add(autoCommitToggleButton);
        toolbar.add(commitButton);
        toolbar.add(rollbackButton);
        
        
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

}
