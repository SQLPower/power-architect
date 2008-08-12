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

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.Deferrability;
import ca.sqlpower.architect.SQLRelationship.UpdateDeleteRule;
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

    private ArchitectSession session;
    
	public RelationshipEditPanel(ArchitectSwingSession session) {
        this.session = session;
        FormLayout layout = new FormLayout("pref, 4dlu, pref:grow, 4dlu, pref, 4dlu, pref:grow");
        layout.setColumnGroups(new int[][] { { 3, 7 } });
        DefaultFormBuilder fb = new DefaultFormBuilder(layout, logger.isDebugEnabled() ? new FormDebugPanel() : new JPanel());
        
        fb.append("Relationship Name", relationshipName = new JTextField(), 5);

		identifyingGroup = new ButtonGroup();
		fb.append("Relationship Type", identifyingButton = new JRadioButton("Identifying"), 5);
		identifyingGroup.add(identifyingButton);
		fb.append("", nonIdentifyingButton = new JRadioButton("Non-Identifying"), 5);
		identifyingGroup.add(nonIdentifyingButton);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();

        pkTypeGroup = new ButtonGroup();
        fkTypeGroup = new ButtonGroup();
        fb.nextLine();
        fb.append("Cardinality", pkTableName = new JLabel("PK Table: Unknown"));
        fb.append("", fkTableName = new JLabel("FK Table: Unknown"));

        fb.append("", pkTypeZeroToMany = new JRadioButton("Zero or More"));
		pkTypeGroup.add(pkTypeZeroToMany);
		fb.append("", fkTypeZeroToMany = new JRadioButton("Zero or More"));
		fkTypeGroup.add(fkTypeZeroToMany);

		fb.append("", pkTypeOneToMany = new JRadioButton("One or More"));
		pkTypeGroup.add(pkTypeOneToMany);
		fb.append("", fkTypeOneToMany = new JRadioButton("One or More"));
		fkTypeGroup.add(fkTypeOneToMany);

		fb.append("", pkTypeZeroOne = new JRadioButton("Zero or One"));
		pkTypeGroup.add(pkTypeZeroOne);
		fb.append("", fkTypeZeroOne = new JRadioButton("Zero or One"));
		fkTypeGroup.add(fkTypeZeroOne);

		fb.append("", pkTypeOne = new JRadioButton("Exactly One"));
		pkTypeGroup.add(pkTypeOne);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        deferrabilityGroup = new ButtonGroup();
        fb.append("Deferrability", notDeferrable = new JRadioButton("Not Deferrable"), 5);
        deferrabilityGroup.add(notDeferrable);
        fb.append("", initiallyDeferred = new JRadioButton("Deferrable, Initially Deferred"), 5);
        deferrabilityGroup.add(initiallyDeferred);
        fb.append("", initiallyImmediate = new JRadioButton("Deferrable, Initially Immediate"), 5);
        deferrabilityGroup.add(initiallyImmediate);

        fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();

        updateRuleGroup = new ButtonGroup();
        deleteRuleGroup = new ButtonGroup();
        fb.nextLine();
        fb.append("Update Rule", updateCascade = new JRadioButton("Cascade"));
        updateRuleGroup.add(updateCascade);
        fb.append("Delete Rule", deleteCascade = new JRadioButton("Cascade"));
        deleteRuleGroup.add(deleteCascade);
        
        fb.append("", updateRestrict = new JRadioButton("Restrict"));
        updateRuleGroup.add(updateRestrict);
        fb.append("", deleteRestrict = new JRadioButton("Restrict"));
        deleteRuleGroup.add(deleteRestrict);

        fb.append("", updateNoAction = new JRadioButton("No Action"));
        updateRuleGroup.add(updateNoAction);
        fb.append("", deleteNoAction = new JRadioButton("No Action"));
        deleteRuleGroup.add(deleteNoAction);

        fb.append("", updateSetNull = new JRadioButton("Set Null"));
        updateRuleGroup.add(updateSetNull);
        fb.append("", deleteSetNull = new JRadioButton("Set Null"));
        deleteRuleGroup.add(deleteSetNull);

        fb.append("", updateSetDefault = new JRadioButton("Set Default"));
        updateRuleGroup.add(updateSetDefault);
        fb.append("", deleteSetDefault = new JRadioButton("Set Default"));
        deleteRuleGroup.add(deleteSetDefault);

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
            ArchitectUtils.listenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            logger.error("Fail to add sql object listener to the edit panel.", e);
            throw new ArchitectRuntimeException(e);
        }
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	public boolean applyChanges() {
	    try {
	        ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
	    } catch (ArchitectException e) {
	        throw new ArchitectRuntimeException(e);
	    }
		relationship.startCompoundEdit("Relationship Properties Change");
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
			relationship.endCompoundEdit("Ending new compound edit event in relationship edit panel");
		}
		return true;
	}

	public void discardChanges() {
	    try {
            ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
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
        logger.debug("SQLObject children got removed: "+e);
        SQLObject[] c = e.getChildren();

        for (SQLObject obj : c) {
            if (relationship.equals(obj)) {
                try {
                    ArchitectUtils.unlistenToHierarchy(this, session.getRootObject());
                    if (editDialog != null) {
                        editDialog.dispose();
                    }
                    break;
                } catch (ArchitectException ex) {
                    throw new ArchitectRuntimeException(ex);
                }
            }
        }
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        
    }

    public void dbStructureChanged(SQLObjectEvent e) {
        
    }
    
    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
	
}
