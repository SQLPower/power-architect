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

import java.awt.BorderLayout;
import java.sql.ResultSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.swingui.table.ResultSetTableFactory;
import ca.sqlpower.swingui.table.ResultSetTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is like DBVisualizer, only not. It'll be different, I promise, trust me....
 */
public class QueryDialog extends JPanel {
    
    private static Logger logger = Logger.getLogger(QueryDialog.class);
    
    /**
     * The table showing the result set. This can only be created when we get
     * an actual ResultSet to avoid NPEs.
     */
    private JTable resultSetTable;

    /**
     * The scroll pane that contains the JTable displaying the results of the
     * SQL query.
     */
    private JScrollPane tableScrollPane;

    /**
     * The text area attached to the table that users can type into for filtering
     * the table.
     */
    private JTextArea tableFilterTextArea;
    
    private final DBTree dbTree;
    
    private SQLQueryEntryPanel queryEntryPanel;
    
    /**
     * Creates and displays the window for executing SQL queries.
     */
    public QueryDialog(ArchitectSwingSession session) {

        try {
            dbTree = new DBTree(session);
        } catch (ArchitectException e) {
            throw new RuntimeException(e);
        }
        TreeModel model = session.getSourceDatabases().getModel();
        dbTree.setModel(model);
        
        queryEntryPanel = new SQLQueryEntryPanel(session, dbTree);
        queryEntryPanel.addExecuteAction(new ExecuteActionListener() {

            public void sqlQueryExecuted(ResultSet rs) {
                TableModel newModel = new ResultSetTableModel(rs);
                if (resultSetTable == null) {
                    resultSetTable = ResultSetTableFactory.createResultSetJTableWithSearch(rs, tableFilterTextArea.getDocument());
                    tableScrollPane.getViewport().add(resultSetTable);
                }
                resultSetTable.setModel(newModel);
                resultSetTable.createDefaultColumnsFromModel();
            }});
        buildUI(session);
    }

    private void buildUI(ArchitectSwingSession session) {
        
        JSplitPane queryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        queryPane.add(queryEntryPanel, JSplitPane.TOP);
        
        tableFilterTextArea = new JTextArea();
        FormLayout tableAreaLayout = new FormLayout("pref, 10dlu, pref:grow", "pref, 10dlu, fill:max(100dlu;pref):grow");
        DefaultFormBuilder tableAreaBuilder = new DefaultFormBuilder(tableAreaLayout);
        tableAreaBuilder.setDefaultDialogBorder();
        tableAreaBuilder.append("Filter");
        tableAreaBuilder.append(tableFilterTextArea);
        tableAreaBuilder.nextLine();
        tableAreaBuilder.nextLine();
        resultSetTable = null;
        tableScrollPane = new JScrollPane();
        tableAreaBuilder.append(tableScrollPane, 3);
        
        queryPane.add(tableAreaBuilder.getPanel(), JSplitPane.BOTTOM);
        JSplitPane treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        treePane.add(new JScrollPane(dbTree), JSplitPane.LEFT);
        treePane.add(queryPane, JSplitPane.RIGHT);        
        
        setLayout(new BorderLayout());
        add(treePane, BorderLayout.CENTER);
    }

}
