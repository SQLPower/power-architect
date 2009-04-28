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
package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectEvent;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectListener;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.FormLayout;

public class RelationshipEditPanel implements SQLObjectListener, DataEntryPanel {

	private static final Logger logger = Logger.getLogger(RelationshipEditPanel.class);

    /**
     * The panel that contains the editor components.
     */
    private final JPanel panel;
    
    /**
     * The relationship being edited.
     */
	private SQLRelationship relationship;
	
	/**
	 * The frame which this relationship editing dialog resides in.
	 */
	private JDialog editDialog;

	private JTextField relationshipName;
	private JTextField pkLabelTextField;
	private JTextField fkLabelTextField;

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
    
    private ButtonGroup updateRuleGroup;
    private JRadioButton updateCascade;
    private JRadioButton updateRestrict;
    private JRadioButton updateSetNull;
    private JRadioButton updateNoAction;
    private JRadioButton updateSetDefault;

    private ButtonGroup deleteRuleGroup;
    private JRadioButton deleteCascade;
    private JRadioButton deleteRestrict;
    private JRadioButton deleteSetNull;
    private JRadioButton deleteNoAction;
    private JRadioButton deleteSetDefault;
    
    private JComboBox relationLineColor;

    private ArchitectSession session;
    
    private Color color;

    private List<Relationship> relationshipLines;
    
	public RelationshipEditPanel(ArchitectSwingSession session) {
	    this.session = session;
        
        relationshipLines = session.getPlayPen().getSelectedRelationShips();
        //Since now can only select one relationship to edit at the same time,
        //so the number of selected relationships is only 1. 
        this.color = relationshipLines.get(0).getForegroundColor();
        
        FormLayout layout = new FormLayout("pref, 4dlu, pref:grow, 4dlu, pref, 4dlu, pref:grow"); //$NON-NLS-1$
        layout.setColumnGroups(new int[][] { { 3, 7 } });
        DefaultFormBuilder fb = new DefaultFormBuilder(layout, logger.isDebugEnabled() ? new FormDebugPanel() : new JPanel());
        
        fb.append(Messages.getString("RelationshipEditPanel.name"), relationshipName = new JTextField(), 5); //$NON-NLS-1$
        
        fb.nextLine();
        fb.append(Messages.getString("RelationshipEditPanel.lineColour"), relationLineColor = new JComboBox(Relationship.SUGGESTED_COLOURS)); //$NON-NLS-1$
        ColorCellRenderer renderer = new ColorCellRenderer(40, 20);
        relationLineColor.setRenderer(renderer);
        if (!containsColor(Relationship.SUGGESTED_COLOURS, color)) {
            relationLineColor.addItem(color);
            relationLineColor.setSelectedItem(color);
        }
        fb.append(new JButton(customColour));
        
        fb.nextLine();
        fb.append(Messages.getString("RelationshipEditPanel.pkLabel"), pkLabelTextField = new JTextField());
        fb.append(Messages.getString("RelationshipEditPanel.fkLabel"), fkLabelTextField = new JTextField());
        
		identifyingGroup = new ButtonGroup();
		fb.append(Messages.getString("RelationshipEditPanel.type"), identifyingButton = new JRadioButton(Messages.getString("RelationshipEditPanel.identifying")), 5); //$NON-NLS-1$ //$NON-NLS-2$
		identifyingGroup.add(identifyingButton);
		fb.append("", nonIdentifyingButton = new JRadioButton(Messages.getString("RelationshipEditPanel.nonIdentifying")), 5); //$NON-NLS-1$ //$NON-NLS-2$
		identifyingGroup.add(nonIdentifyingButton);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();

        pkTypeGroup = new ButtonGroup();
        fkTypeGroup = new ButtonGroup();
        fb.nextLine();
        fb.append(Messages.getString("RelationshipEditPanel.cardinality"), pkTableName = new JLabel("PK Table: Unknown")); //$NON-NLS-1$ //$NON-NLS-2$
        fb.append("", fkTableName = new JLabel("FK Table: Unknown")); //$NON-NLS-1$ //$NON-NLS-2$

        fb.append("", pkTypeZeroToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeZeroToMany);
		fb.append("", fkTypeZeroToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeZeroToMany);

		fb.append("", pkTypeOneToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.oneOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeOneToMany);
		fb.append("", fkTypeOneToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.oneOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeOneToMany);

		fb.append("", pkTypeZeroOne = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeZeroOne);
		fb.append("", fkTypeZeroOne = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeZeroOne);

		fb.append("", pkTypeOne = new JRadioButton(Messages.getString("RelationshipEditPanel.exactlyOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeOne);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        deferrabilityGroup = new ButtonGroup();
        fb.append(Messages.getString("RelationshipEditPanel.deferrability"), notDeferrable = new JRadioButton(Messages.getString("RelationshipEditPanel.notDeferrable")), 5); //$NON-NLS-1$ //$NON-NLS-2$
        deferrabilityGroup.add(notDeferrable);
        fb.append("", initiallyDeferred = new JRadioButton(Messages.getString("RelationshipEditPanel.initiallyDeferred")), 5); //$NON-NLS-1$ //$NON-NLS-2$
        deferrabilityGroup.add(initiallyDeferred);
        fb.append("", initiallyImmediate = new JRadioButton(Messages.getString("RelationshipEditPanel.initiallyImmediate")), 5); //$NON-NLS-1$ //$NON-NLS-2$
        deferrabilityGroup.add(initiallyImmediate);

        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();

        updateRuleGroup = new ButtonGroup();
        deleteRuleGroup = new ButtonGroup();
        fb.nextLine();
        fb.append(Messages.getString("RelationshipEditPanel.updateRule"), updateCascade = new JRadioButton(Messages.getString("RelationshipEditPanel.cascade"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateCascade);
        fb.append(Messages.getString("RelationshipEditPanel.deleteRule"), deleteCascade = new JRadioButton(Messages.getString("RelationshipEditPanel.cascade"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteCascade);
        
        fb.append("", updateRestrict = new JRadioButton(Messages.getString("RelationshipEditPanel.restrict"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateRestrict);
        fb.append("", deleteRestrict = new JRadioButton(Messages.getString("RelationshipEditPanel.restrict"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteRestrict);

        fb.append("", updateNoAction = new JRadioButton(Messages.getString("RelationshipEditPanel.noAction"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateNoAction);
        fb.append("", deleteNoAction = new JRadioButton(Messages.getString("RelationshipEditPanel.noAction"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteNoAction);

        fb.append("", updateSetNull = new JRadioButton(Messages.getString("RelationshipEditPanel.setNull"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateSetNull);
        fb.append("", deleteSetNull = new JRadioButton(Messages.getString("RelationshipEditPanel.setNull"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteSetNull);

        fb.append("", updateSetDefault = new JRadioButton(Messages.getString("RelationshipEditPanel.setDefault"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateSetDefault);
        fb.append("", deleteSetDefault = new JRadioButton(Messages.getString("RelationshipEditPanel.setDefault"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteSetDefault);
        
        //TODO  Doesn't work!
        relationshipName.selectAll();
        
        fb.setDefaultDialogBorder();
        panel = fb.getPanel();
	}


	public void setRelationship(SQLRelationship r) {
		this.relationship = r;
		relationshipName.setText(r.getName());
		pkLabelTextField.setText(r.getTextForParentLabel());
		fkLabelTextField.setText(r.getTextForChildLabel());
        relationLineColor.setSelectedItem(color);
		pkTableName.setText(Messages.getString("RelationshipEditPanel.pkTable", relationship.getPkTable().getName())); //$NON-NLS-1$
		fkTableName.setText(Messages.getString("RelationshipEditPanel.fkTable", relationship.getFkTable().getName())); //$NON-NLS-1$
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
        
        if (r.getUpdateRule() == UpdateDeleteRule.CASCADE) {
            updateCascade.setSelected(true);
        } else if (r.getUpdateRule() == UpdateDeleteRule.NO_ACTION) {
            updateNoAction.setSelected(true);
        } else if (r.getUpdateRule() == UpdateDeleteRule.RESTRICT) {
            updateRestrict.setSelected(true);
        } else if (r.getUpdateRule() == UpdateDeleteRule.SET_DEFAULT) {
            updateSetDefault.setSelected(true);
        } else if (r.getUpdateRule() == UpdateDeleteRule.SET_NULL) {
            updateSetNull.setSelected(true);
        }

        if (r.getDeleteRule() == UpdateDeleteRule.CASCADE) {
            deleteCascade.setSelected(true);
        } else if (r.getDeleteRule() == UpdateDeleteRule.NO_ACTION) {
            deleteNoAction.setSelected(true);
        } else if (r.getDeleteRule() == UpdateDeleteRule.RESTRICT) {
            deleteRestrict.setSelected(true);
        } else if (r.getDeleteRule() == UpdateDeleteRule.SET_DEFAULT) {
            deleteSetDefault.setSelected(true);
        } else if (r.getDeleteRule() == UpdateDeleteRule.SET_NULL) {
            deleteSetNull.setSelected(true);
        }

		relationshipName.selectAll();
		
		try {
            SQLObjectUtils.listenToHierarchy(this, session.getRootObject());
        } catch (SQLObjectException e) {
            logger.error("Fail to add sql object listener to the edit panel.", e); //$NON-NLS-1$
            throw new SQLObjectRuntimeException(e);
        }
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	public boolean applyChanges() {
	    try {
	        SQLObjectUtils.unlistenToHierarchy(this, session.getRootObject());
	    } catch (SQLObjectException e) {
	        throw new SQLObjectRuntimeException(e);
	    }
		relationship.startCompoundEdit(Messages.getString("RelationshipEditPanel.modifyRelationshipProperties")); //$NON-NLS-1$
		try {
			relationship.setName(relationshipName.getText());
			// set the parent label text of relationship lines
			relationship.setTextForParentLabel(pkLabelTextField.getText());
			// set the child label text of relationship lines
			relationship.setTextForChildLabel(fkLabelTextField.getText());
			try {
				relationship.setIdentifying(identifyingButton.isSelected());
			} catch (SQLObjectException ex) {
				logger.warn("Call to setIdentifying failed. Continuing with other properties.", ex); //$NON-NLS-1$
			}
			
			for(Relationship r: relationshipLines) {
			    // set the color of relationship lines
                r.setForegroundColor((Color)relationLineColor.getSelectedItem());
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
            
            if (updateCascade.isSelected()) {
                relationship.setUpdateRule(UpdateDeleteRule.CASCADE);
            } else if (updateNoAction.isSelected()) {
                relationship.setUpdateRule(UpdateDeleteRule.NO_ACTION);
            } else if (updateRestrict.isSelected()) {
                relationship.setUpdateRule(UpdateDeleteRule.RESTRICT);
            } else if (updateSetDefault.isSelected()) {
                relationship.setUpdateRule(UpdateDeleteRule.SET_DEFAULT);
            } else if (updateSetNull.isSelected()) {
                relationship.setUpdateRule(UpdateDeleteRule.SET_NULL);
            }

            if (deleteCascade.isSelected()) {
                relationship.setDeleteRule(UpdateDeleteRule.CASCADE);
            } else if (deleteNoAction.isSelected()) {
                relationship.setDeleteRule(UpdateDeleteRule.NO_ACTION);
            } else if (deleteRestrict.isSelected()) {
                relationship.setDeleteRule(UpdateDeleteRule.RESTRICT);
            } else if (deleteSetDefault.isSelected()) {
                relationship.setDeleteRule(UpdateDeleteRule.SET_DEFAULT);
            } else if (deleteSetNull.isSelected()) {
                relationship.setDeleteRule(UpdateDeleteRule.SET_NULL);
            }
            
		} finally {
			relationship.endCompoundEdit(Messages.getString("RelationshipEditPanel.modifyRelationshipProperties")); //$NON-NLS-1$
		}
		return true;
	}

	public void discardChanges() {
	    try {
            SQLObjectUtils.unlistenToHierarchy(this, session.getRootObject());
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        }
	}

	public JPanel getPanel() {
		return panel;
	}

    public boolean hasUnsavedChanges() {
        return true;
    }

    public void dbChildrenInserted(SQLObjectEvent e) {
        
    }

    /**
     * Checks to see if its respective relationship is removed from
     * playpen. If yes, exit the editing dialog window.
     */
    public void dbChildrenRemoved(SQLObjectEvent e) {
        logger.debug("SQLObject children got removed: "+e); //$NON-NLS-1$
        SQLObject[] c = e.getChildren();

        for (SQLObject obj : c) {
            if (relationship.equals(obj)) {
                try {
                    SQLObjectUtils.unlistenToHierarchy(this, session.getRootObject());
                    if (editDialog != null) {
                        editDialog.dispose();
                    }
                    break;
                } catch (SQLObjectException ex) {
                    throw new SQLObjectRuntimeException(ex);
                }
            }
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        
    }

    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
    
    Action customColour = new AbstractAction("Custom Colour...") {
        public void actionPerformed(ActionEvent arg0) {
            Color colour = session.getCustomColour(relationshipLines.get(0).getForegroundColor());
            if (colour != null && !containsColor(Relationship.SUGGESTED_COLOURS, colour)) {
                relationLineColor.addItem(colour);
                relationLineColor.setSelectedItem(colour);
            }
        }
    };

	private boolean containsColor(Vector<Color> colorSet, Color color) {
	    boolean contains = false;
	    for (Color eachColor : colorSet) {
	        if (eachColor.equals(color)) {
	            contains = true;
	        }
	    }
	    return contains;
	}
}
