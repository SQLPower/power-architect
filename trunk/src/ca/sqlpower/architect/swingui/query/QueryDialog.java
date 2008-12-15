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
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;

/**
 * This is like DBVisualizer, only not. It'll be different, I promise, trust me....
 */
public class QueryDialog extends JDialog {
    
    private static Logger logger = Logger.getLogger(QueryDialog.class);
    
    private final DBTree dbTree;
    
    private JComponent queryPanel;

    
    /**
     * Creates and displays the window for executing SQL queries.
     */
    public QueryDialog(ArchitectSwingSession session, JFrame sessionframe, String title) {
        this(session, sessionframe, title, null, null);
    }
        
    /**
     * Creates and displays a window for executing SQL queries. Allows
     * specifying an initial data source and SQL script for the query 
     * window. If a null value is passed in for the ds or initialSQL 
     * then no initial querying will be done.
     */
    public QueryDialog(ArchitectSwingSession session, JFrame sessionframe, String title, SPDataSource ds, String initialSQL) {
       super(sessionframe, title);
       setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
       setSize(900,650);
       try {
           dbTree = new DBTree(session);
       } catch (ArchitectException e) {
           throw new RuntimeException(e);
       }
       
       TreeModel model = session.getSourceDatabases().getModel();
       dbTree.setModel(model);
        
        queryPanel = SQLQueryUIComponents.createQueryPanel(session, session.getContext().getPlDotIni(), this, ds, initialSQL);
        queryPanel.setMinimumSize(new Dimension(100,100));
        
        buildUI(session);
    }

    private void buildUI(ArchitectSwingSession session) {
        
        JSplitPane querySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        querySplitPane.add(new JScrollPane(dbTree), JSplitPane.LEFT);
        querySplitPane.add(queryPanel, JSplitPane.RIGHT);
        querySplitPane.setDividerLocation(130);
       
        setLayout(new BorderLayout());
        add(querySplitPane, BorderLayout.CENTER);

    }
    
}
