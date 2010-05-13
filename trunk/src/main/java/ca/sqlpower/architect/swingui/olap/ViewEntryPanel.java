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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.tree.TreeModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This entry panel will create a view builder based on the 
 * SQLQueryUIComponents in the library.
 */
class ViewEntryPanel implements DataEntryPanel {
    
    /**
     * The main panel of this data entry panel
     */
    private JSplitPane splitPane;
    
    /**
     * The text area users will enter a select statement into.
     */
    private RSyntaxTextArea queryArea;

    /**
     * The query components used to create the view. This needs
     * to be closed when the entry panel goes away or else connections
     * will be leaked.
     */
    private SQLQueryUIComponents queryComponents;

    private final CubeEditPanel cubeEditPanel;

    public ViewEntryPanel(ArchitectSwingSession session, SQLDatabase db, CubeEditPanel cubeEditPanel) {
        this.cubeEditPanel = cubeEditPanel;
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu:grow, pref, 3dlu, pref", "pref, fill:pref:grow"));
        
        queryComponents = new SQLQueryUIComponents(session, session.getDataSources(), session, builder.getPanel());
        queryComponents.getRowLimitSpinner().setValue(Integer.valueOf(1000));
        queryComponents.getDatabaseComboBox().setSelectedItem(db.getDataSource());
        
        JToolBar toolbar = new JToolBar();
        toolbar.add(queryComponents.getPrevQueryButton());
        toolbar.add(queryComponents.getNextQueryButton());
        toolbar.addSeparator();
        toolbar.add(queryComponents.getExecuteButton());
        toolbar.add(queryComponents.getStopButton());
        toolbar.add(queryComponents.getClearButton());
        toolbar.addSeparator();
        toolbar.add(queryComponents.getUndoButton());
        toolbar.add(queryComponents.getRedoButton());
        toolbar.addSeparator();
        toolbar.add(new JLabel(db.getName()));
        builder.append(toolbar);
        builder.append("Row Limit", queryComponents.getRowLimitSpinner());
        builder.nextLine();
        
        queryArea = queryComponents.getQueryArea();
        builder.append(new JScrollPane(queryArea), 5);
        queryArea.setText(cubeEditPanel.getSelectText());
        
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(builder.getPanel());
        rightSplitPane.setBottomComponent(queryComponents.getResultTabPane());
        rightSplitPane.setPreferredSize(new Dimension((int) Math.max(400, rightSplitPane.getPreferredSize().getWidth()), (int) Math.max(500, rightSplitPane.getPreferredSize().getHeight())));
        rightSplitPane.setResizeWeight(0.5);
        
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
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(tree);
        splitPane.setRightComponent(rightSplitPane);
        splitPane.setResizeWeight(0.2);
        
    }

    public boolean applyChanges() {
        cubeEditPanel.setSelectText(queryArea.getText());
        cleanup();
        return true;
    }

    public void discardChanges() {
        cleanup();
    }

    private void cleanup() {
        queryComponents.closingDialogOwner();
    }
    
    public JComponent getPanel() {
        return splitPane;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }
    
}