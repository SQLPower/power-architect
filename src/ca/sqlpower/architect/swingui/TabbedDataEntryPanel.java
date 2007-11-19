/*
 * Created on Jul 31, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import ca.sqlpower.swingui.DataEntryPanel;

/**
 * The TabbedDataEntryPanel aggregates one or more DataEntryPanel
 * instances into a single DataEntryPanel with one tab for each
 * sub-panel.  It provides applyChanges and discardChanges methods
 * that "do the right thing" by broadcasting the corresponding request
 * to all the panels being managed by the current instance.
 */
public class TabbedDataEntryPanel implements DataEntryPanel {

    /**
     * The tabbed pane that holds all the GUIs added via addTab().
     */
    private final JTabbedPane tabbedPane = new JTabbedPane();
    
    /**
     * All of the data entry panels represented in the tabbed pane.
     */
    private final List<DataEntryPanel> panels = new ArrayList<DataEntryPanel>();
    
    public TabbedDataEntryPanel() {
    }
    
    /**
     * Adds the given panel as a tab of this panel.
     * 
     * @param name The string to attach to the tab for the given panel
     * @param panel The panel to manage
     */
    public void addTab(String name, DataEntryPanel panel) {
        tabbedPane.addTab(name, panel.getPanel());
        panels.add(panel);
    }
    
    /**
     * Applies changes to every panel managed by this instance.
     * 
     * @return True if and only if each panel's applyChanges() returned success.
     */
    public boolean applyChanges() {
        boolean success = true;
        for (DataEntryPanel panel : panels) {
            success &= panel.applyChanges();
        }
        return success;
    }

    /**
     * Discards changes on every panel managed by this instance.
     */
    public void discardChanges() {
        for (DataEntryPanel panel : panels) {
            panel.discardChanges();
        }
    }

    /**
     * Returns the tabbed pane that aggregates all the panels managed
     * by this instance.
     */
    public JComponent getPanel() {
        return tabbedPane;
    }

    public boolean hasUnsavedChanges() {
        // TODO Auto-generated method stub
        return false;
    }

}
