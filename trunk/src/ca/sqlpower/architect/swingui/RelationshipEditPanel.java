/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class RelationshipEditPanel implements DataEntryPanel {

	private static final Logger logger = Logger.getLogger(RelationshipEditPanel.class);

    /**
     * The panel that contains the editor components.
     */
    private final JPanel panel;
    
    /**
     * The relationship being edited.
     */
	private SQLRelationship relationship;

	private JTextField relationshipName;

	private ButtonGroup identifyingGroup;
	private JRadioButton identifyingButton;
	private JRadioButton nonIdentifyingButton;

	private JLabel pkTableName;
	private ButtonGroup pkTypeGroup;
	private JRadioButton pkTypeZeroToMany;
	private JRadioButton pkTypeOneToMany;
	private JRadioButton pkTypeZeroOne;
	private JRadioButton pkTypeOne;

	private JLabel fkTableName;
	private ButtonGroup fkTypeGroup;
	private JRadioButton fkTypeZeroToMany;
	private JRadioButton fkTypeOneToMany;
	private JRadioButton fkTypeZeroOne;

    private ButtonGroup deferrabilityGroup;
    private JRadioButton notDeferrable;
    private JRadioButton initiallyDeferred;
    private JRadioButton initiallyImmediate;
    
	public RelationshipEditPanel(ArchitectSwingSession session) {

        // XXX this looks suspicious. why does it work? shouldn't the events come
        //     straight from the SQLRelationship object?
        addUndoEventListener(session.getArchitectFrame().getUndoManager().getEventAdapter());
        
        FormLayout layout = new FormLayout("pref, 4dlu, pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        fb.append("Relationship Name", relationshipName = new JTextField());

		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
		identifyingGroup = new ButtonGroup();
		typePanel.add(identifyingButton = new JRadioButton("Identifying"));
		identifyingGroup.add(identifyingButton);
		typePanel.add(nonIdentifyingButton = new JRadioButton("Non-Identifying"));
		identifyingGroup.add(nonIdentifyingButton);
		fb.append("Relationship Type", typePanel);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        fb.append("Cardinality", pkTableName = new JLabel("PK Table: Unknown"));
		pkTypeGroup = new ButtonGroup();
        fb.append("", pkTypeZeroToMany = new JRadioButton("Zero or More"));
		pkTypeGroup.add(pkTypeZeroToMany);
        fb.append("", pkTypeOneToMany = new JRadioButton("One or More"));
		pkTypeGroup.add(pkTypeOneToMany);
		fb.append("", pkTypeZeroOne = new JRadioButton("Zero or One"));
		pkTypeGroup.add(pkTypeZeroOne);
		fb.append("", pkTypeOne = new JRadioButton("Exactly One"));
		pkTypeGroup.add(pkTypeOne);

		fb.nextLine();
        fb.appendRelatedComponentsGapRow();
        fb.nextLine();
		fb.append("", fkTableName = new JLabel("FK Table: Unknown"));
		fkTypeGroup = new ButtonGroup();
		fb.append("", fkTypeZeroToMany = new JRadioButton("Zero or More"));
		fkTypeGroup.add(fkTypeZeroToMany);
		fb.append("", fkTypeOneToMany = new JRadioButton("One or More"));
		fkTypeGroup.add(fkTypeOneToMany);
		fb.append("", fkTypeZeroOne = new JRadioButton("Zero or One"));
		fkTypeGroup.add(fkTypeZeroOne);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        deferrabilityGroup = new ButtonGroup();
        fb.append("Deferrability", notDeferrable = new JRadioButton("Not Deferrable"));
        deferrabilityGroup.add(notDeferrable);
        fb.append("", initiallyDeferred = new JRadioButton("Deferrable, Initially Deferred"));
        deferrabilityGroup.add(initiallyDeferred);
        fb.append("", initiallyImmediate = new JRadioButton("Deferrable, Initially Immediate"));
        deferrabilityGroup.add(initiallyImmediate);
        
		relationshipName.selectAll();
        
        fb.setDefaultDialogBorder();
        panel = fb.getPanel();
	}


	public void setRelationship(SQLRelationship r) {
		this.relationship = r;
		relationshipName.setText(r.getName());
		pkTableName.setText("PK Table: " + relationship.getPkTable().getName());
		fkTableName.setText("FK Table: " + relationship.getFkTable().getName());
		if ( r.isIdentifying()){
			identifyingButton.setSelected(true);
		} else {
			nonIdentifyingButton.setSelected(true);
		}
		int pkc = r.getPkCardinality();
		if (pkc == (SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY)){
			pkTypeZeroToMany.setSelected(true);
		} else if (pkc == (SQLRelationship.ZERO | SQLRelationship.ONE)){
			pkTypeZeroOne.setSelected(true);
		} else if (pkc == (SQLRelationship.ONE | SQLRelationship.MANY)){
			pkTypeOneToMany.setSelected(true);
		} else if (pkc == SQLRelationship.ONE) {
			pkTypeOne.setSelected(true);
		}
		int fkc = r.getFkCardinality();
		if (fkc == (SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY)){
			fkTypeZeroToMany.setSelected(true);
		} else if (fkc == (SQLRelationship.ZERO | SQLRelationship.ONE)){
			fkTypeZeroOne.setSelected(true);
		} else if (fkc == (SQLRelationship.ONE | SQLRelationship.MANY)){
			fkTypeOneToMany.setSelected(true);
		}
        
        if (r.getDeferrability() == Deferrability.NOT_DEFERRABLE) {
            notDeferrable.setSelected(true);
        } else if (r.getDeferrability() == Deferrability.INITIALLY_DEFERRED) {
            initiallyDeferred.setSelected(true);
        } else if (r.getDeferrability() == Deferrability.INITIALLY_IMMEDIATE) {
            initiallyImmediate.setSelected(true);
        }
        
		relationshipName.selectAll();
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	public boolean applyChanges() {
		startCompoundEdit("Relationship Properties Change");
		try {
			relationship.setName(relationshipName.getText());
			try {
				relationship.setIdentifying(identifyingButton.isSelected());
			} catch (ArchitectException ex) {
				logger.warn("Call to setIdentifying failed. Continuing with other properties.", ex);
			}
			
			if (pkTypeZeroOne.isSelected()) {
				relationship.setPkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE);
			} else if (pkTypeZeroToMany.isSelected()) {
				relationship.setPkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY);
			} else if (pkTypeOneToMany.isSelected()) {
				relationship.setPkCardinality(SQLRelationship.ONE | SQLRelationship.MANY);
			} else if (pkTypeOne.isSelected()) {
				relationship.setPkCardinality(SQLRelationship.ONE);
			}
			
			if (fkTypeZeroOne.isSelected()) {
				relationship.setFkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE);
			} else if (fkTypeZeroToMany.isSelected()) {
				relationship.setFkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY);
			} else if (fkTypeOneToMany.isSelected()) {
				relationship.setFkCardinality(SQLRelationship.ONE | SQLRelationship.MANY);
			}
            
            if (notDeferrable.isSelected()) {
                relationship.setDeferrability(Deferrability.NOT_DEFERRABLE);
            } else if (initiallyDeferred.isSelected()) {
                relationship.setDeferrability(Deferrability.INITIALLY_DEFERRED);
            } else if (initiallyImmediate.isSelected()) {
                relationship.setDeferrability(Deferrability.INITIALLY_IMMEDIATE);
            }
            
		} finally {
			endCompoundEdit("Ending new compound edit event in relationship edit panel");
		}
		return true;
	}

	public void discardChanges() {
	  
	}
	
	/**
	 * The list of SQLObject property change event listeners
	 * used for undo
	 */
	private LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

	
	public void addUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}
	
	private void fireUndoCompoundEvent(UndoCompoundEvent e) {
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
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START,message));
	}
	
	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END,message));
	}

	public JPanel getPanel() {
		return panel;
	}


    public boolean hasUnsavedChanges() {
        // TODO Auto-generated method stub
        return false;
    }
	
}
