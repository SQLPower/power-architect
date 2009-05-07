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

package ca.sqlpower.architect.swingui.olap;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.tree.TreeModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.SQL;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.olap.MondrianModel.View;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidatableDataEntryPanel;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CubeEditPanel implements ValidatableDataEntryPanel {
    
    /**
     * This entry panel will create a view builder based on the 
     * SQLQueryUIComponents in the library.
     */
    private class ViewEntryPanel implements DataEntryPanel {
        
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

        public ViewEntryPanel(ArchitectSwingSession session) {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu:grow, pref, 3dlu, pref", "pref, fill:pref:grow"));
            
            SQLDatabase db = OSUtils.getAncestor(CubeEditPanel.this.cube, OLAPSession.class).getDatabase();
            queryComponents = new SQLQueryUIComponents(session, session.getContext().getPlDotIni(), session, builder.getPanel());
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
            queryArea.setText(selectStatements.getText());
            
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
                treeModel = new DBTreeModel(root);
                tree = new DBTree(session);
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
            selectStatements.setText(queryArea.getText());
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
    
    private final Cube cube;
    private final JPanel panel;
    private JTextField nameField;
    private JTextField captionField;
    private JComboBox defMeasure;
    private JComboBox tableChooser;
    
    private JTextArea selectStatements;
    private JTextField viewAlias;
    
    /**
     * Validation handler for errors in the dialog
     */
    private FormValidationHandler handler;
    private StatusComponent status = new StatusComponent();
    private JRadioButton tableRadioButton;
    private JRadioButton viewRadioButton;
    
    /**
     * Creates a new property editor for the given OLAP Cube. 
     * 
     * @param cube The data model of the cube to edit
     */
    public CubeEditPanel(Cube cube, final ArchitectSwingSession session) throws SQLObjectException {
        this.cube = cube;
        
        List<SQLTable> tables = OLAPUtil.getAvailableTables(cube);
        
        FormLayout layout = new FormLayout(
                "left:max(40dlu;pref), 3dlu, 80dlu:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.append(status, 3);
        builder.append("Name", nameField = new JTextField(cube.getName()));
        builder.append("Caption", captionField = new JTextField(cube.getCaption()));
        // default measure is optional so we need to add in a null option
        List<Measure> measures = new ArrayList<Measure>(cube.getMeasures());
        measures.add(0, null);
        builder.append("Default Measure", defMeasure = new JComboBox(measures.toArray()));
        defMeasure.setRenderer(new OLAPObjectListCellRenderer());
        for (Measure ms : cube.getMeasures()) {
            if (ms.getName().equals(cube.getDefaultMeasure())) {
                defMeasure.setSelectedItem(ms);
                break;
            }
        }
        
        builder.appendSeparator("Fact Table");
        tableChooser = new JComboBox(new Vector<SQLTable>(tables));

        Action radioButtonsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                tableChooser.setEnabled(tableRadioButton.isSelected());
                selectStatements.setEnabled(viewRadioButton.isSelected());
                viewAlias.setEnabled(viewRadioButton.isSelected());
            }
        };
        
        tableRadioButton = new JRadioButton();
        tableRadioButton.setAction(radioButtonsAction);
        tableRadioButton.setText("Use Existing Table");
        viewRadioButton = new JRadioButton();
        viewRadioButton.setAction(radioButtonsAction);
        viewRadioButton.setText("Use View");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(tableRadioButton);
        buttonGroup.add(viewRadioButton);
        
        builder.append(tableRadioButton, 3); 
        builder.append(tableChooser, 3);
        builder.append(viewRadioButton, 3); 
        builder.append("Alias", viewAlias = new JTextField());
        builder.append(new JLabel("SELECT Statements"), 3);
        builder.append(new JScrollPane(selectStatements = new JTextArea("", 4, 30)), 3);
        builder.append(new JButton(new AbstractAction("Edit...") {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(new ViewEntryPanel(session), null, "View Builder", "OK");
                dialog.pack();
                dialog.setVisible(true);        
            }
        }));
        selectStatements.setLineWrap(true);
        selectStatements.setEditable(false);
        
        if (cube.getFact() instanceof View) {
            viewRadioButton.doClick();
            tableRadioButton.setEnabled(false);
            tableChooser.setEnabled(false);
            for (SQL sql : ((View) cube.getFact()).getSelects()) {
                selectStatements.append(sql.getText() + "\n"); 
            }
        } else if (tables.isEmpty()) {
            tableChooser.addItem("Database has no tables");
            viewRadioButton.doClick();
            tableRadioButton.setEnabled(false);
            tableChooser.setEnabled(false);
        } else {
            SQLTable t = OLAPUtil.tableForCube(cube);
            //if SQLTable t is not found, then either cube.fact is not defined, or cube.fact is a view
            if (tables.contains(t)) {
                tableChooser.setSelectedItem(t);
                tableRadioButton.doClick();
            } else if (cube.getFact() != null){
                viewRadioButton.doClick();
            } else {
                tableRadioButton.doClick();
            }
        }
        
        panel = builder.getPanel();
        
        handler = new FormValidationHandler(status);
        Validator validator = new OLAPObjectNameValidator(cube.getParent(), cube, false);
        handler.addValidateObject(nameField, validator);
        
        selectStatements.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(new ViewEntryPanel(session), null, "View Builder", "OK");
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    public boolean applyChanges() {
        try {
            cube.startCompoundEdit("Modify cube properties");
            if (tableRadioButton.isSelected()) {
                if (tableChooser.isEnabled()) {
                    SQLTable table = (SQLTable) tableChooser.getSelectedItem();
                    if (table != null) {
                        Table t = new Table();
                        t.setName(table.getName());
                        t.setSchema(OLAPUtil.getQualifier(table));
                        cube.setFact(t);
                    }
                }
            } else if (viewRadioButton.isSelected()) {
                View view = new View();
                view.setAlias(viewAlias.getText());
                SQL sql = new SQL();
                sql.setText(selectStatements.getText());
                view.addSelect(sql);
                cube.setFact(view);
            }
            cube.setName(nameField.getText());
            if (!(captionField.getText().equals(""))) {
                cube.setCaption(captionField.getText());
            } else {
                cube.setCaption(null);
            }
            Measure ms = (Measure) defMeasure.getSelectedItem();
            cube.setDefaultMeasure(ms == null ? null : ms.getName());
        } finally {
            cube.endCompoundEdit();
        }
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }

    public ValidationHandler getValidationHandler() {
        return handler;
    }
}
