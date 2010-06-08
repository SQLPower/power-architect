package ca.sqlpower.architect.swingui.critic;

import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ddl.critic.CriticGrouping;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel for setting properties of a group of critics.
 */
public class CriticGroupingPanel implements DataEntryPanel {

    /**
     * A tree cell editor that allows editing the {@link CriticAndSettings} in the
     * tree in the {@link CriticGroupingPanel}.
     */
    private class GroupingTreeCellEditor implements TreeCellEditor {
        
        private final List<CellEditorListener> editorListeners = new ArrayList<CellEditorListener>();

        private SPObject lastSelectedPath;

        public GroupingTreeCellEditor(final JTree tree) {
            tree.addTreeSelectionListener(new TreeSelectionListener() {
            
                public void valueChanged(TreeSelectionEvent e) {
                    lastSelectedPath = (SPObject) e.getNewLeadSelectionPath().getLastPathComponent();
                }
            });
        }
        
        public boolean stopCellEditing() {
            return true;
        }
    
        public boolean shouldSelectCell(EventObject e) {
            return true;
        }
    
        public void removeCellEditorListener(CellEditorListener l) {
            editorListeners.remove(l);
        }
    
        public boolean isCellEditable(EventObject e) {
            if (lastSelectedPath != null && 
                    lastSelectedPath instanceof CriticAndSettings) return true;
            return false;
        }
    
        public Object getCellEditorValue() {
            //No value to really edit as we are using a whole panel to do the edit.
            return null;
        }
    
        public void cancelCellEditing() {
            //nothing to do.
        }
    
        public void addCellEditorListener(CellEditorListener l) {
            editorListeners.add(l);
        }
    
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row) {
            if (value instanceof CriticAndSettings) {
                return settingsPanels.get((CriticAndSettings) value).getPanel();
            }
            return null;
        }
        
        public void setLastSelectedPath(SPObject lastSelectedPath) {
            this.lastSelectedPath = lastSelectedPath;
        }
    }

    /**
     * The grouping we may change by the time we apply changes. No changes
     * will occur to this object until apply changes is called.
     */
    private final CriticGrouping grouping;
    
    /**
     * The main panel of this grouping editor.
     */
    private final JPanel panel;

    /**
     * This check box tracks if the selection of critics in this group
     * should be enabled or not.
     */
    private final JCheckBox enabledCheckbox;

    /**
     * Tree model to be used with a {@link CriticGrouping}. Can be used with a
     * renderer to display all of the settings editors of a single
     * {@link CriticGrouping}.
     * TODO Pull a generic tree model off of this tree model for later use.
     */
    private final TreeModel treeModel = new TreeModel() {

        /**
         * Tree listeners.
         */
        private final List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
    
        public void valueForPathChanged(TreePath path, Object newValue) {
            throw new UnsupportedOperationException("This should not be allowed at this point. " +
            		"Only when users can create their own critics will this need to change.");
        }
    
        public void removeTreeModelListener(TreeModelListener l) {
            treeListeners.remove(l);
        }
    
        public boolean isLeaf(Object node) {
            if (node instanceof CriticAndSettings) return true;
            return false;
        }
    
        public Object getRoot() {
            return grouping;
        }
    
        public int getIndexOfChild(Object parent, Object child) {
            return ((SPObject) parent).getChildren().indexOf(child);
        }
    
        public int getChildCount(Object parent) {
            return ((SPObject) parent).getChildren().size();
        }
    
        public Object getChild(Object parent, int index) {
            return ((SPObject) parent).getChildren().get(index);
        }
    
        public void addTreeModelListener(TreeModelListener l) {
            treeListeners.add(l);
        }
    };

    /**
     * Displays the settings editor panels in a tree for easier navigation.
     */
    private final TreeCellRenderer treeCellRenderer = new TreeCellRenderer() {
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            if (value instanceof CriticGrouping) {
                return new JLabel(((CriticGrouping) value).getPlatformType());
            } else if (value instanceof CriticAndSettings) {
                JComponent thisSettingsPanel = settingsPanels.get((CriticAndSettings) value).getPanel();
                return thisSettingsPanel;
            }
            return null;
        }
    };
    
    /**
     * Each of the settings grouped by this class's grouping will have a data entry panel
     * associated with it.
     */
    private final Map<CriticAndSettings, DataEntryPanel> settingsPanels = 
        new HashMap<CriticAndSettings, DataEntryPanel>();

    public CriticGroupingPanel(CriticGrouping grouping) {
        this.grouping = grouping;

        panel = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref, 5dlu, pref:grow", "top:pref"), panel);
        for (CriticAndSettings settings : grouping.getSettings()) {
            final CriticSettingsPanel settingsPanel = new CriticSettingsPanel(settings);
            settingsPanels.put(settings, settingsPanel);
        }
        
        enabledCheckbox = new JCheckBox();
        enabledCheckbox.setSelected(grouping.isEnabled());
        builder.append(enabledCheckbox);
        
        JTree settingsTree = new JTree(treeModel);
        settingsTree.setCellRenderer(treeCellRenderer);
        settingsTree.setRowHeight(0);
        settingsTree.setShowsRootHandles(true);
        GroupingTreeCellEditor treeCellEditor = new GroupingTreeCellEditor(settingsTree);
        settingsTree.setCellEditor(treeCellEditor);
        settingsTree.setEditable(true);
        
        //Makes the first element in the tree editable so the user doesn't
        //have to click the first time to start editing and then a second time to
        //make changes.
        treeCellEditor.setLastSelectedPath(settingsPanels.keySet().iterator().next());
        treeCellEditor.isCellEditable(new EventObject(settingsTree));
        
        settingsTree.setBackground(panel.getBackground());
        builder.append(settingsTree);
    }
    
    public boolean applyChanges() {
        grouping.setEnabled(enabledCheckbox.isSelected());
        for (DataEntryPanel panel : settingsPanels.values()) {
            if (!panel.applyChanges()) return false;
        }
        return true;
    }

    public void discardChanges() {
        for (DataEntryPanel panel : settingsPanels.values()) {
            panel.discardChanges();
        }
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        if (enabledCheckbox.isSelected() != grouping.isEnabled()) return true;
        for (DataEntryPanel panel : settingsPanels.values()) {
            if (panel.hasUnsavedChanges()) return true;
        }
        return false;
    }
    
}