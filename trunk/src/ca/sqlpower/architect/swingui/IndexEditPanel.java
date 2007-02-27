package ca.sqlpower.architect.swingui;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

public class IndexEditPanel extends JPanel implements ArchitectPanel {
    protected SQLIndex index;
    JTextField name;

    public IndexEditPanel(SQLIndex index) {
        super(new FormLayout());
        addUndoEventListener(ArchitectFrame.getMainInstance().getProject().getUndoManager().getEventAdapter());
        add(new JLabel("Index Name"));
        add(name = new JTextField("", 30));
        editIndex(index);
    }

    public void editIndex(SQLIndex index) {
        this.index = index;
        name.setText(index.getName());
    }

    // --------------------- ArchitectPanel interface ------------------
    public boolean applyChanges() {
        startCompoundEdit("Index Properties Change");       
        try {   
            StringBuffer warnings = new StringBuffer();
            //We need to check if the index name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append("The index cannot be assigned a blank name \n");
                
            }
            
            if (warnings.toString().length() == 0){
                //The operation is successful
                index.setName(name.getText());             
                return true;
            } else{
                JOptionPane.showMessageDialog(this,warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }            
        } finally {
            endCompoundEdit("Ending new compound edit event in index edit panel");
        }
    }

    public void discardChanges() {
    }
    
    /**
     * The list of SQLObject property change event listeners
     * used for undo
     */
    protected LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

    
    public void addUndoEventListener(UndoCompoundEventListener l) {
        undoEventListeners.add(l);
    }

    public void removeUndoEventListener(UndoCompoundEventListener l) {
        undoEventListeners.remove(l);
    }
    
    protected void fireUndoCompoundEvent(UndoCompoundEvent e) {
        Iterator it = undoEventListeners.iterator();
        
        if (e.getType().isStartEvent()) {
            while (it.hasNext()) {
                ((UndoCompoundEventListener) it.next()).compoundEditStart(e);
            }
        } else {
            while (it.hasNext()) {
                ((UndoCompoundEventListener) it.next()).compoundEditEnd(e);
            }
        } 
    }

    public void startCompoundEdit(String message){
        fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_START,message));
    }
    
    public void endCompoundEdit(String message){
        fireUndoCompoundEvent(new UndoCompoundEvent(this,EventTypes.COMPOUND_EDIT_END,message));
    }
    
    public JPanel getPanel() {
        return this;
    }

    public String getNameText() {
        return name.getText();
    }

    public void setNameText(String newName) {
        name.setText(newName);
    }
}
