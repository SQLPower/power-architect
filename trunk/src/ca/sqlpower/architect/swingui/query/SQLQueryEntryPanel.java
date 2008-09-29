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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.DnDTreePathTransferable;
import ca.sqlpower.architect.swingui.action.DatabaseConnectionManagerAction;
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
     * The action for executing and displaying a user's query.
     */
    private final AbstractAction executeAction = new AbstractAction("Execute") {
        public void actionPerformed(ActionEvent e) {
            logger.debug("Starting execute action.");
            SPDataSource ds = (SPDataSource)databases.getSelectedItem();
            if (ds == null) {
                return;
            }
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                con = ds.createConnection();
                stmt = con.createStatement();
                rs = stmt.executeQuery(queryArea.getText());
                CachedRowSet rowSet = new CachedRowSet();
                rowSet.populate(rs);
                
                for (ExecuteActionListener listener : executeListeners) {
                    listener.sqlQueryExecuted(rowSet);
                }

            } catch (SQLException ex) {
                SPSUtils.showExceptionDialogNoReport(getParent(), "Could not query the database", ex);
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
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    };
    
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
        executeListeners = new ArrayList<ExecuteActionListener>();
        queryArea = new JTextArea();
        databases = new JComboBox(s.getContext().getConnections().toArray());
        
        
        addAncestorListener(new AncestorListener(){

            public void ancestorAdded(AncestorEvent event) {
                session.getContext().getPlDotIni().addDatabaseListChangeListener(dbListChangeListener);
            }

            public void ancestorMoved(AncestorEvent event) {
            }

            public void ancestorRemoved(AncestorEvent event) {
                session.getContext().getPlDotIni().removeDatabaseListChangeListener(dbListChangeListener);
                
            }});
        
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
        toolbar.add(new AbstractAction("Clear"){
            public void actionPerformed(ActionEvent arg0) {
                queryArea.setText("");
            }});
        FormLayout textAreaLayout = new FormLayout(
                "max(250dlu;pref):grow, 10dlu, pref"
                , "pref, pref, fill:max(100dlu;pref):grow");
        DefaultFormBuilder textAreaBuilder = new DefaultFormBuilder(textAreaLayout, this);
        textAreaBuilder.setDefaultDialogBorder();
        textAreaBuilder.append(toolbar);
        textAreaBuilder.nextLine();
        textAreaBuilder.append(databases);
        JButton dbcsManagerButton = new JButton(new DatabaseConnectionManagerAction(session));
        dbcsManagerButton.setText("Manage Connections...");
        textAreaBuilder.append(dbcsManagerButton);
        textAreaBuilder.nextLine();
        textAreaBuilder.append(new JScrollPane(queryArea), 3);
       
  
    }
    
    public void addExecuteAction(ExecuteActionListener l) {
        executeListeners.add(l);
    }
    
    public void removeExecuteAction(ExecuteActionListener l) {
        executeListeners.remove(l);
    }

}
