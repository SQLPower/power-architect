/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.olap;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.tree.TreeModel;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.InlineTable;
import ca.sqlpower.architect.olap.MondrianModel.Join;
import ca.sqlpower.architect.olap.MondrianModel.Relation;
import ca.sqlpower.architect.olap.MondrianModel.RelationOrJoin;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.querypen.QueryPen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This entry panel will be used for editing a {@link Join} that defines
 * a {@link Cube} in the OLAP editor.
 */
public class JoinEntryPanel implements DataEntryPanel {
    
    /**
     * This compares two SQLJoin objects and orders them by the joins that contain
     * the most columns. The first value should have the smallest number of columns
     * to the largest number of columns.
     */
    private class compareByColumnCount implements Comparator<SQLJoin> {

        public int compare(SQLJoin o1, SQLJoin o2) {
            int join1ColCount = Math.max(o1.getLeftColumn().getParent().getItems().size(), 
                                         o1.getRightColumn().getParent().getItems().size());
            int Join2ColCount = Math.max(o2.getLeftColumn().getParent().getItems().size(), 
                                         o2.getRightColumn().getParent().getItems().size());
            if (join1ColCount < Join2ColCount) {
                return -1;
            } else if (join1ColCount > Join2ColCount) {
                return 1;
            } else {
                return 0;
            }
        }
        
    }
    
    private final JPanel panel = new JPanel();
    
    private final QueryPen pen;

    /**
     * This action will not execute the query in the query pen. If later we
     * want to show the results of the joins defined by the user we can implement
     * this method. One thing to keep in mind is that a join created on the playpen
     * database will not be able to execute.
     */
    private final Action noExecutionAction = new AbstractAction() {
    
        public void actionPerformed(ActionEvent e) {
            //Do nothing as we are currently not executing this join.
        }
    };
    
    private final Query model;
    
    private final SQLDatabase db;

    private final ArchitectSwingSession session;

    /**
     * This panel made the JoinEntryPanel and will have it's join
     * set when the OK button is pressed.
     */
    private final CubeEditPanel editPanel;
    
    public JoinEntryPanel(ArchitectSwingSession session, SQLDatabase db, CubeEditPanel editPanel, Join join) {
        this.session = session;
        this.db = db;
        this.editPanel = editPanel;
        model = new QueryImpl(session);
        
        if (join != null) {
            addSQLJoinsToModel(join);
        }
        
        pen = new QueryPen(noExecutionAction , model, false);
        pen.setExecuteIcon(new ImageIcon(QueryPen.class.getClassLoader().getResource("icons/auto_layout16.png")));
        buildUI();
    }
    
    private List<Container> addSQLJoinsToModel(Join join) {
        
        List<Container> tablesRight = new ArrayList<Container>();
        List<Container> tablesLeft = new ArrayList<Container>();

        //Recursively add the tables and their joins to the model
        if (join.getLeft() instanceof Join) {
            tablesLeft.addAll(addSQLJoinsToModel((Join) join.getLeft()));
        }
        if (join.getRight() instanceof Join) {
            tablesRight.addAll(addSQLJoinsToModel((Join) join.getRight()));
        }
        
        //Base case: add tables to the model if one or both sides of the join are tables.
        Container leftContainer = createContainerForRelation(join.getLeft());
        if (leftContainer != null) {
            tablesLeft.add(leftContainer);
        }
        Container rightContainer = createContainerForRelation(join.getRight());
        if (rightContainer != null) {
            tablesRight.add(rightContainer);
        }
        
        //Add the join the given join defines
        if (leftContainer == null) {
            if (join.getLeftAlias() == null || join.getLeftAlias().trim().length() == 0) {
                //XXX This may be a valid case where we would need to iterate over all of the tables to find the unique key on the one side.
                throw new IllegalStateException("Expecting 'leftAlias' to specify which table contains the " + join.getLeftKey() + " key.");
            }
            for (Container c : tablesLeft) {
                if (c.getName().equals(join.getLeftAlias())) {
                    leftContainer = c;
                    break;
                }
            }
            if (leftContainer == null) {
                throw new IllegalStateException("Could not find the table for the 'leftAlias' " + join.getLeftAlias());
            }
        }
        if (rightContainer == null) {
            if (join.getRightAlias() == null || join.getRightAlias().trim().length() == 0) {
                //XXX This may be a valid case where we would need to iterate over all of the tables to find the unique key on the one side.
                throw new IllegalStateException("Expecting 'rightAlias' to specify which table contains the " + join.getRightKey() + " key.");
            }
            for (Container c : tablesRight) {
                if (c.getName().equals(join.getRightAlias())) {
                    rightContainer = c;
                    break;
                }
            }
            if (rightContainer == null) {
                throw new IllegalStateException("Could not find the table for the 'rightAlias' " + join.getRightAlias());
            }
        }
        
        Item leftItem = null;
        for (Item item : leftContainer.getItems()) {
            if (join.getLeftKey().equals(item.getName())) {
                leftItem = item;
                break;
            }
        }
        if (leftItem == null) {
            throw new IllegalStateException("Could not find the 'leftKey' " + join.getLeftKey() + " from the table " + leftContainer.getName());
        }
        Item rightItem = null;
        for (Item item : rightContainer.getItems()) {
            if (join.getRightKey().equals(item.getName())) {
                rightItem = item;
                break;
            }
        }
        if (leftItem == null) {
            throw new IllegalStateException("Could not find the 'rightKey' " + join.getRightKey() + " from the table " + rightContainer.getName());
        }
        
        SQLJoin sqlJoin = new SQLJoin(leftItem, rightItem);
        model.addJoin(sqlJoin);
        
        tablesRight.addAll(tablesLeft);
        return tablesRight;
    }

    /**
     * This will create a Container with items that will represent the relation 
     * passed in. The container will be added to the model in this method and
     * does not need to be added again. Containers will only be made for {@link Relation}
     * objects, a {@link Join} should be broken down in the {@link #addSQLJoinsToModel(Join)}
     * method.
     * <p>
     * This is a helper method for {@link #addSQLJoinsToModel(Join)}.
     */
    private Container createContainerForRelation(RelationOrJoin relation) {
        if (relation instanceof Table) {
            Table table1 = (Table) relation;
            SQLTable sqlTable;
            try {
                sqlTable = db.getTableByName(null, table1.getSchema(), table1.getName());
            } catch (SQLObjectException e) {
                throw new RuntimeException(e);
            } //XXX Can't get the catalog from the table so we are currently only looking by name and schema
            TableContainer container = new TableContainer(model.getDatabase(), sqlTable);
            model.addTable(container);
            return container;
        } else if (relation instanceof InlineTable) {
            InlineTable table = (InlineTable) relation;
            
            Container inlineContainer = new ItemContainer(table.getName());
            for (OLAPObject child : table.getChildren(OLAPObject.class)) {
                Item childItem = new StringItem(child.getName());
                inlineContainer.addItem(childItem);
            }
            
            model.addTable(inlineContainer);
            return inlineContainer;
        } else if (relation instanceof Join) {
            return null;
        } else { //TODO create a container for View objects.
            throw new UnsupportedOperationException("Cannot create a container of the type " + relation.getClass() + " for the object " + relation);
        }
    }
    
    private void buildUI() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("fill:max(500dlu;pref):grow", "pref, fill:max(300dlu;pref):grow"), panel);
        JToolBar toolbar = new JToolBar();
        toolbar.add(pen.getDeleteButton());
        toolbar.add(pen.getCreateJoinButton());
        toolbar.addSeparator();
        toolbar.add(pen.getZoomSliderContainer());
        builder.append(toolbar);
        builder.nextLine();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(pen.getScrollPane(), JSplitPane.LEFT);
        
        SQLObjectRoot root = new SQLObjectRoot();
        TreeModel treeModel;
        DBTree tree;
        try {
            root.addChild(db);
            tree = new DBTree(session);
            treeModel = new DBTreeModel(root, tree);
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
        tree.setModel(treeModel);
        tree.setPopupMenuEnabled(false);
        
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(new JLabel(db.getName()), BorderLayout.NORTH);
        treePanel.add(new JScrollPane(tree));
        
        splitPane.add(treePanel, JSplitPane.RIGHT);
        splitPane.setResizeWeight(0.95);
        
        builder.append(splitPane);
    }
    
    /**
     * This will recursively create a list of the table names in the join.
     */
    private List<Relation> getRelationList(Join join) {
        if (join == null) return null;
        List<Relation> list = new ArrayList<Relation>();
        
        if (join.getLeft() instanceof Join) {
            list.addAll(getRelationList((Join) join.getLeft()));
        } else {
            list.add((Relation) join.getLeft());
        }
        if (join.getRight() instanceof Join) {
            list.addAll(getRelationList((Join) join.getRight()));
        } else {
            list.add((Relation) join.getRight());
        }
        return list;
    }

    public boolean applyChanges() {
        List<SQLJoin> joins = new ArrayList<SQLJoin>(model.getJoins());
        
        Collections.sort(joins, new compareByColumnCount());
        
        Map<Container, Join> tableToJoinMap = new HashMap<Container, Join>();
        
        //Iterate through the SQLJoins slowly building up a larger Mondrian Join
        //by either using Tables or other joins made by Tables.
        
        //This join will contain the last Join added to the map.
        Join join = null;
        for (SQLJoin sqlJoin : joins) {
            join = new Join();
            //check if the left side is in the map
            final Container leftContainer = sqlJoin.getLeftColumn().getContainer();
            Join leftExistingJoin = null;
            if (tableToJoinMap.containsKey(leftContainer)) {
                //if so get the join the left side is involved with and update the joins in the map accordingly
                Join existingJoin = tableToJoinMap.get(leftContainer);
                leftExistingJoin = existingJoin;
                join.setLeftAlias(leftContainer.getName());
                join.setLeft(existingJoin);
                join.setLeftKey(sqlJoin.getLeftColumn().getName());
                for (Map.Entry<Container, Join> entry : tableToJoinMap.entrySet()) {
                    if (entry.getValue() == existingJoin) {
                        tableToJoinMap.put(leftContainer, join);
                    }
                }
            } else {
                //if not just use the table
                Table table = new Table();
                table.setName(leftContainer.getName());
                join.setLeft(table);
                join.setLeftKey(sqlJoin.getLeftColumn().getName());
                tableToJoinMap.put(leftContainer, join);
            }
            
            //check if the right side is in the map
            final Container rightContainer = sqlJoin.getRightColumn().getContainer();
            if (tableToJoinMap.containsKey(rightContainer)) {
                //if so get the join the right side is involved with and update the joins in the map accordingly
                Join existingJoin = tableToJoinMap.get(rightContainer);
                join.setRightAlias(rightContainer.getName());
                join.setRight(existingJoin);
                join.setRightKey(sqlJoin.getRightColumn().getName());
                for (Map.Entry<Container, Join> entry : tableToJoinMap.entrySet()) {
                    if (entry.getValue() == existingJoin) {
                        tableToJoinMap.put(rightContainer, join);
                    }
                }
            } else {
                //if not just use the table
                Table table = new Table();
                table.setName(rightContainer.getName());
                join.setRight(table);
                join.setRightKey(sqlJoin.getRightColumn().getName());
                tableToJoinMap.put(rightContainer, join);
            }
            
        }
        
        
        
        if (join != null) {
            List<Relation> list = getRelationList(join);
            //search the list for duplicate
            for (int i = 0; i < list.size(); i++) {
                Relation relation = list.remove(i);
                
                //There is a cycle in the join and therefore mondrian cannot display it properly
                if (list.indexOf(relation) >= 0) {
                    String invalidJoinStatement = "<html>The join specified contains cycles and cannot be specified in OLAP.\n" +
                    "Either the cycle needs to be broken by dragging in the same table to duplicate it\n" +
                    "or a view can be created to represent this join specification.";
                    final String invalidJoinDialogHeader = "Invalid join specification";
                    
                    int retType = JOptionPane.showOptionDialog(panel, invalidJoinStatement, invalidJoinDialogHeader, 
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
                            new Object[]{"Create View", "Cancel"}, "Create View");
                    if (retType == JOptionPane.OK_OPTION) {
                        editPanel.setViewSelected();
                        editPanel.setSelectText(model.generateQuery());
                        return true;
                    } else {
                        return false;
                    }
                }
                
                list.add(i, relation);
            }
//            editPanel.setJoinFact(join);
        }
        return true;
    }

    public void discardChanges() {
        //do-nothing.
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }
    
}

