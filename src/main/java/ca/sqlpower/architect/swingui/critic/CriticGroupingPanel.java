package ca.sqlpower.architect.swingui.critic;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ddl.critic.CriticAndSettings;
import ca.sqlpower.architect.ddl.critic.CriticGrouping;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
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
            return true;
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
            return tree.getCellRenderer().getTreeCellRendererComponent(tree, value, 
                    isSelected, expanded, leaf, row, false);
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
     */
    private final CriticSettingsTreeModel treeModel;
    
    private class CriticSettingsTreeModel implements TreeModel {
        
        /**
         * Tree listeners.
         */
        private final List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
        
        private final PropertyChangeListener criticSettingsListener = new PropertyChangeListener() {
            
            public void propertyChange(PropertyChangeEvent evt) {
                final CriticSettingsPanel source = (CriticSettingsPanel) evt.getSource();
                for (int i = treeListeners.size() - 1; i >= 0; i--) {
                    treeListeners.get(i).treeNodesChanged(new TreeModelEvent(evt.getSource(), new TreePath(new Object[]{grouping, source.getSettings()})));
                }
            }
        };

        private final CriticGrouping grouping;        
        
        public CriticSettingsTreeModel(CriticGrouping grouping, Collection<CriticSettingsPanel> panels) {
            this.grouping = grouping;
            for (CriticSettingsPanel childPanel : panels) {
                childPanel.addPropertyChangeListener(criticSettingsListener);
            }
        }
    
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
        
        public void repaintTreeNodes() {
            for (CriticSettingsPanel settingsPanel : settingsPanels.values()) {
                for (int i = treeListeners.size() - 1; i >= 0; i--) {
                    treeListeners.get(i).treeNodesChanged(new TreeModelEvent(
                            CriticGroupingPanel.this, new Object[]{grouping, settingsPanel.getSettings()}));
                }
            }
        }
    }

    
    /**
     * Displays the settings editor panels in a tree for easier navigation.
     */
    private final TreeCellRenderer treeCellRenderer = new TreeCellRenderer() {
        
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            if (value instanceof CriticGrouping) {
                return new JLabel(((CriticGrouping) value).getPlatformType());
            } else if (value instanceof CriticAndSettings) {
                int preferredCriticPanelSize = 0;
                for (CriticSettingsPanel panel : settingsPanels.values()) {
                    preferredCriticPanelSize = Math.max(preferredCriticPanelSize, panel.getPanel().getPreferredSize().width);
                }
                preferredCriticPanelSize = Math.max(parentPanel.getPreferredCriticPanelSize(), preferredCriticPanelSize);
                parentPanel.setPreferredCriticPanelSize(preferredCriticPanelSize);
                JComponent thisSettingsPanel = settingsPanels.get((CriticAndSettings) value).getPanel();
                thisSettingsPanel.setPreferredSize(new Dimension(preferredCriticPanelSize, thisSettingsPanel.getPreferredSize().height));
                for (Component c : thisSettingsPanel.getComponents()) {
                    c.setEnabled(tree.isEnabled());
                }
                return thisSettingsPanel;
            }
            return null;
        }
    };
    
    /**
     * Each of the settings grouped by this class's grouping will have a data entry panel
     * associated with it.
     */
    private final Map<CriticAndSettings, CriticSettingsPanel> settingsPanels = 
        new HashMap<CriticAndSettings, CriticSettingsPanel>();
    
    /**
     * Added to the group this panel displays to update the checkbox of the group.
     */
    private final SPListener groupListener = new AbstractSPListener() {
        
        public void propertyChanged(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("enabled")) {
                enabledCheckbox.setSelected((Boolean) evt.getNewValue());
            }
        }
    };

    /**
     * The parent panel to this grouping panel. One special need for this panel
     * is the critic settings panels need to line up with the panels in other
     * grouping panels.
     */
    private final CriticManagerPanel parentPanel;
    
    public CriticGroupingPanel(CriticGrouping grouping, CriticManagerPanel parentPanel) {
        this.grouping = grouping;
        this.parentPanel = parentPanel;

        panel = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref, 0dlu, pref:grow", "top:pref"), panel);
        for (CriticAndSettings settings : grouping.getSettings()) {
            final CriticSettingsPanel settingsPanel = 
                parentPanel.getSettingsPanelFactory().createCriticsSettingsPanel(settings);
            settingsPanels.put(settings, settingsPanel);
        }
        
        treeModel = new CriticSettingsTreeModel(grouping, settingsPanels.values());
        
        enabledCheckbox = new JCheckBox();
        enabledCheckbox.setSelected(grouping.isEnabled());
        builder.append(enabledCheckbox);
        
        grouping.addSPListener(groupListener);
        
        final JTree settingsTree = new JTree(treeModel);
        settingsTree.setCellRenderer(treeCellRenderer);
        settingsTree.setRowHeight(0);
        settingsTree.setShowsRootHandles(true);
        GroupingTreeCellEditor treeCellEditor = new GroupingTreeCellEditor();
        settingsTree.setCellEditor(treeCellEditor);
        settingsTree.setEditable(true);
        
        treeCellEditor.isCellEditable(new EventObject(settingsTree));
        
        settingsTree.setBackground(panel.getBackground());
        builder.append(settingsTree);
        
        enabledCheckbox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settingsTree.setEnabled(enabledCheckbox.isSelected());
                settingsTree.setEditable(enabledCheckbox.isSelected());
            }
        });
        settingsTree.setEnabled(enabledCheckbox.isSelected());
        settingsTree.setEditable(enabledCheckbox.isSelected());
        
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
    
    /**
     * Call when disposing of this panel.
     */
    public void cleanup() {
        grouping.removeSPListener(groupListener);
        for (CriticSettingsPanel panel : settingsPanels.values()) {
            panel.cleanup();
        }
    }
    
    public void revalidateTree() {
        treeModel.repaintTreeNodes();
    }
}