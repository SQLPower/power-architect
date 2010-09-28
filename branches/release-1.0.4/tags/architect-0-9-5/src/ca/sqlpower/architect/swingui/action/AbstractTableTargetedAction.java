package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingConstants;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public abstract class AbstractTableTargetedAction extends AbstractAction implements SelectionListener {
    /**
     * The PlayPen instance that owns this Action.
     */
    protected PlayPen pp;

    /**
     * The DBTree instance that is associated with this Action.
     */
    protected DBTree dbt; 
    

    public AbstractTableTargetedAction() {
        super();
    }

    public AbstractTableTargetedAction(String name, Icon icon) {
        super(name, icon);
    }

    public AbstractTableTargetedAction(String name) {
        super(name);
    }
    
    public void actionPerformed(ActionEvent evt) {
        try {
            if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN)) {
                List selection = pp.getSelectedItems();
                if (selection.size() < 1) {
                    JOptionPane.showMessageDialog(pp, "Select a table (by clicking on it) and try again.");
                } else if (selection.size() > 1) {
                    JOptionPane.showMessageDialog(pp, "You have selected multiple items, but you can only edit one at a time.");
                } else if (selection.get(0) instanceof TablePane) {
                    TablePane tp = (TablePane) selection.get(0);
                    processTablePane(tp);
                } else {
                    JOptionPane.showMessageDialog(pp, "The selected item type is not recognised");
                }
            } else if (evt.getActionCommand().equals(ArchitectSwingConstants.ACTION_COMMAND_SRC_DBTREE)) {
                TreePath [] selections = dbt.getSelectionPaths();
                if (selections == null || selections.length != 1) {
                    JOptionPane.showMessageDialog(dbt, "To indicate where you would like to insert a column, please select a single item.");
                } else {
                    TreePath tp = selections[0];
                    SQLObject so = (SQLObject) tp.getLastPathComponent();
                    processSQLObject(so);
                }
            } else {
                JOptionPane.showMessageDialog(
                        null, "InsertColumnAction: Unknown Action Command \"" + 
                        evt.getActionCommand() + "\"",
                        "Internal Architect Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ArchitectException ex) {
            ASUtils.showExceptionDialog("Column could not be inserted:\n" + ex.getMessage(), ex);
        }
    }
    

    abstract void processTablePane(TablePane tp) throws ArchitectException;
    abstract void processSQLObject(SQLObject so) throws ArchitectException;

    public void setPlayPen(PlayPen pp) {
        if (this.pp != null) {
            this.pp.removeSelectionListener(this);
        } 
        this.pp = pp;
        pp.addSelectionListener(this);
        
        setupAction(pp.getSelectedItems());
    }

    public void setDBTree(DBTree newDBT) {
        this.dbt = newDBT;
        // do I need to add a selection listener here?
    }
    
    
    public void setupAction(List selectedItems) {
        if (selectedItems.size() == 0) {
            disableAction();
        } else {
            Selectable item = (Selectable) selectedItems.get(0);
            if (item instanceof TablePane)              
                setEnabled(true);
        }
    }
    
    public abstract void disableAction();
        
    public void itemSelected(SelectionEvent e) {
        setupAction(pp.getSelectedItems());
        
    }

    public void itemDeselected(SelectionEvent e) {
        setupAction(pp.getSelectedItems());
    }
}
