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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.swingui.table.ResultSetTableFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is like DBVisualizer, only not. It'll be different, I promise, trust me....
 */
public class QueryDialog extends JPanel {
    
    private static Logger logger = Logger.getLogger(QueryDialog.class);
    
    private final DBTree dbTree;
    
    private SQLQueryEntryPanel queryEntryPanel;
    
    private JTabbedPane tabPane;
    
    /**
     * Any results returned by SQL statement execution that does not return a table
     * will be placed here. 
     */
    private JTextArea logTextArea;
    
    private JTabbedPane tableTabPane;
    
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

            public void sqlQueryExecuted(List<ResultSet> resultSets, List<Integer> rowsAffected) {
                tableTabPane.removeAll();
                for (ResultSet rs : resultSets) {
                    tableTabPane.add(Messages.getString("SQLQuery.result"), createResultSetTablePanel(rs));
                }
                
                logTextArea.setText("");
                for (Integer i : rowsAffected) {
                    logTextArea.append(Messages.getString("SQLQuery.rowsAffected", i.toString()));
                    logTextArea.append("\n\n");
                }
            } 
        
        });
        
        tabPane = new JTabbedPane();
        tableTabPane = new JTabbedPane();
        logTextArea = new JTextArea();
        
        buildUI(session);
    }

    private void buildUI(ArchitectSwingSession session) {
        
        JSplitPane queryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        queryPane.add(queryEntryPanel, JSplitPane.TOP);
        
        tabPane.add(Messages.getString("SQLQuery.log"), new JScrollPane(logTextArea));
        
        tabPane.add(Messages.getString("SQLQuery.result"), tableTabPane);
        queryPane.add(tabPane, JSplitPane.BOTTOM);
        JSplitPane treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        treePane.add(new JScrollPane(dbTree), JSplitPane.LEFT);
        treePane.add(queryPane, JSplitPane.RIGHT);        
        
        setLayout(new BorderLayout());
        add(treePane, BorderLayout.CENTER);
    }

    /**
     * Creates a new JPanel that displays a table of the result set.
     */
    private JPanel createResultSetTablePanel(ResultSet rs) {
        JTextArea tableFilterTextArea = new JTextArea();
        FormLayout tableAreaLayout = new FormLayout("pref, 10dlu, pref:grow", "pref, 10dlu, fill:min(pref;100dlu):grow");
        DefaultFormBuilder tableAreaBuilder = new DefaultFormBuilder(tableAreaLayout);
        tableAreaBuilder.setDefaultDialogBorder();
        tableAreaBuilder.append(Messages.getString("SQLQuery.filter"));
        tableAreaBuilder.append(new JScrollPane(tableFilterTextArea));
        tableAreaBuilder.nextLine();
        tableAreaBuilder.nextLine();
        JScrollPane tableScrollPane = new JScrollPane(ResultSetTableFactory.createResultSetJTableWithSearch(rs, tableFilterTextArea.getDocument()));
        tableAreaBuilder.append(tableScrollPane, 3);
        
        return tableAreaBuilder.getPanel();
    }

}
