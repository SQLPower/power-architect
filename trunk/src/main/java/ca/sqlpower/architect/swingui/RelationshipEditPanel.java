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
import java.beans.PropertyChangeEvent;
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

import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.Deferrability;
import ca.sqlpower.sqlobject.SQLRelationship.UpdateDeleteRule;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.FormLayout;

public class RelationshipEditPanel extends ChangeListeningDataEntryPanel implements SPListener {

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
    
    private Relationship relationshipLine;
    private JComboBox relationLineColor;    
    private Color color;  
    
	public RelationshipEditPanel(Relationship r) {
        
	    relationshipLine = r;
        this.color = relationshipLine.getForegroundColor();
        
        FormLayout layout = new FormLayout("pref, 4dlu, pref:grow, 4dlu, pref, 4dlu, pref:grow, 4dlu, pref"); //$NON-NLS-1$
        layout.setColumnGroups(new int[][] { { 3, 7 } });
        DefaultFormBuilder fb = new DefaultFormBuilder(layout, logger.isDebugEnabled() ? new FormDebugPanel() : new JPanel());
        
        fb.append(Messages.getString("RelationshipEditPanel.name"), relationshipName = new JTextField(), 7); //$NON-NLS-1$
        
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
        
        JButton swapButton = new JButton(swapLabelText);
        swapButton.setIcon(SPSUtils.createIcon("arrow_refresh", "Swap Labels", ArchitectSwingSessionContext.ICON_SIZE));
        swapButton.setToolTipText("Swap Label Texts");
        fb.append(swapButton);
        fb.nextLine();
        
		identifyingGroup = new ButtonGroup();
		fb.append(Messages.getString("RelationshipEditPanel.type"), identifyingButton = new JRadioButton(Messages.getString("RelationshipEditPanel.identifying")), 7); //$NON-NLS-1$ //$NON-NLS-2$
		identifyingGroup.add(identifyingButton);
		fb.append("", nonIdentifyingButton = new JRadioButton(Messages.getString("RelationshipEditPanel.nonIdentifying")), 7); //$NON-NLS-1$ //$NON-NLS-2$
		identifyingGroup.add(nonIdentifyingButton);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();

        pkTypeGroup = new ButtonGroup();
        fkTypeGroup = new ButtonGroup();
        fb.nextLine();
        fb.append(Messages.getString("RelationshipEditPanel.cardinality"), pkTableName = new JLabel("PK Table: Unknown")); //$NON-NLS-1$ //$NON-NLS-2$
        fb.append("", fkTableName = new JLabel("FK Table: Unknown")); //$NON-NLS-1$ //$NON-NLS-2$
        fb.nextLine();

        fb.append("", pkTypeZeroToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeZeroToMany);
		fb.append("", fkTypeZeroToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeZeroToMany);
		fb.nextLine();

		fb.append("", pkTypeOneToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.oneOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeOneToMany);
		fb.append("", fkTypeOneToMany = new JRadioButton(Messages.getString("RelationshipEditPanel.oneOrMore"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeOneToMany);
		fb.nextLine();

		fb.append("", pkTypeZeroOne = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeZeroOne);
		fb.append("", fkTypeZeroOne = new JRadioButton(Messages.getString("RelationshipEditPanel.zeroOrOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		fkTypeGroup.add(fkTypeZeroOne);
		fb.nextLine();

		fb.append("", pkTypeOne = new JRadioButton(Messages.getString("RelationshipEditPanel.exactlyOne"))); //$NON-NLS-1$ //$NON-NLS-2$
		pkTypeGroup.add(pkTypeOne);

		fb.nextLine();
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        deferrabilityGroup = new ButtonGroup();
        fb.append(Messages.getString("RelationshipEditPanel.deferrability"), notDeferrable = new JRadioButton(Messages.getString("RelationshipEditPanel.notDeferrable")), 7); //$NON-NLS-1$ //$NON-NLS-2$
        deferrabilityGroup.add(notDeferrable);
        fb.append("", initiallyDeferred = new JRadioButton(Messages.getString("RelationshipEditPanel.initiallyDeferred")), 7); //$NON-NLS-1$ //$NON-NLS-2$
        deferrabilityGroup.add(initiallyDeferred);
        fb.append("", initiallyImmediate = new JRadioButton(Messages.getString("RelationshipEditPanel.initiallyImmediate")), 7); //$NON-NLS-1$ //$NON-NLS-2$
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
        fb.nextLine();
        
        fb.append("", updateRestrict = new JRadioButton(Messages.getString("RelationshipEditPanel.restrict"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateRestrict);
        fb.append("", deleteRestrict = new JRadioButton(Messages.getString("RelationshipEditPanel.restrict"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteRestrict);
        fb.nextLine();

        fb.append("", updateNoAction = new JRadioButton(Messages.getString("RelationshipEditPanel.noAction"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateNoAction);
        fb.append("", deleteNoAction = new JRadioButton(Messages.getString("RelationshipEditPanel.noAction"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteNoAction);
        fb.nextLine();

        fb.append("", updateSetNull = new JRadioButton(Messages.getString("RelationshipEditPanel.setNull"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateSetNull);
        fb.append("", deleteSetNull = new JRadioButton(Messages.getString("RelationshipEditPanel.setNull"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteSetNull);
        fb.nextLine();

        fb.append("", updateSetDefault = new JRadioButton(Messages.getString("RelationshipEditPanel.setDefault"))); //$NON-NLS-1$ //$NON-NLS-2$
        updateRuleGroup.add(updateSetDefault);
        fb.append("", deleteSetDefault = new JRadioButton(Messages.getString("RelationshipEditPanel.setDefault"))); //$NON-NLS-1$ //$NON-NLS-2$
        deleteRuleGroup.add(deleteSetDefault);
        fb.nextLine();
        
        setRelationship(r.getModel());
        
        //TODO  Doesn't work!
        relationshipName.selectAll();
        
        fb.setDefaultDialogBorder();
        panel = fb.getPanel();
	}


	private void setRelationship(SQLRelationship r) {
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
		addListeners();
	}
	
	private void addListeners() {
	    SQLPowerUtils.listenToHierarchy(relationship.getParent(), this);
        relationshipLine.addSPListener(this);    
	}
	
	private void removeListeners() {
	    SQLPowerUtils.unlistenToHierarchy(relationship.getParent(), this);
        relationshipLine.removeSPListener(this); 
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	public boolean applyChanges() {
	    removeListeners();
		try {
		    relationship.begin(Messages.getString("RelationshipEditPanel.modifyRelationshipProperties")); //$NON-NLS-1$
			relationship.setName(relationshipName.getText());
			relationship.setPhysicalName(relationshipName.getText());
			// set the parent label text of relationship lines
			relationship.setTextForParentLabel(pkLabelTextField.getText());
			// set the child label text of relationship lines
			relationship.setTextForChildLabel(fkLabelTextField.getText());
			try {
				relationship.setIdentifying(identifyingButton.isSelected());
			} catch (SQLObjectException ex) {
				logger.warn("Call to setIdentifying failed. Continuing with other properties.", ex); //$NON-NLS-1$
			}
					
			relationshipLine.setForegroundColor((Color)relationLineColor.getSelectedItem());
			
			relationship.setPkCardinality(getSelectedPKCardinality());
			relationship.setFkCardinality(getSelectedFKCardinality());
			relationship.setDeferrability(getSelectedDeferrability());            
            relationship.setUpdateRule(getSelectedUpdateRule());
            relationship.setDeleteRule(getSelectedDeleteRule());
            
            relationship.commit();
		} catch (Exception e) {
		    relationship.rollback(e.getMessage());
		    throw new RuntimeException(e);
		}
		return true;
	}

	public void discardChanges() {
	    removeListeners();
	}

	public JPanel getPanel() {
		return panel;
	}

    public boolean hasUnsavedChanges() {
        return true;
    }

    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
    
    Action customColour = new AbstractAction("Custom...") {
        public void actionPerformed(ActionEvent arg0) {
            Color colour = ArchitectSwingSessionImpl.getCustomColour(relationshipLine.getForegroundColor(), panel);
            if (colour != null) {
                if (!containsColor(Relationship.SUGGESTED_COLOURS, colour)) {
                    relationLineColor.addItem(colour);
                }
                relationLineColor.setSelectedItem(colour);
            }
        }
    };
    
    Action swapLabelText = new AbstractAction() {  
        public void actionPerformed(ActionEvent e) {
            String parentText = pkLabelTextField.getText();
            String childText = fkLabelTextField.getText();
            pkLabelTextField.setText(childText);
            fkLabelTextField.setText(parentText);
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

	private int getSelectedPKCardinality() {
	    if (pkTypeZeroOne.isSelected()) {
	        return SQLRelationship.ZERO | SQLRelationship.ONE;
	    } else if (pkTypeZeroToMany.isSelected()) {
	        return SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY;
	    } else if (pkTypeOneToMany.isSelected()) {
	        return SQLRelationship.ONE | SQLRelationship.MANY;
	    } else if (pkTypeOne.isSelected()) {
	        return SQLRelationship.ONE;
	    } else throw new IllegalStateException("No PK cardinality selected");
	}

	private int getSelectedFKCardinality() {
	    if (fkTypeZeroOne.isSelected()) {
	        return SQLRelationship.ZERO | SQLRelationship.ONE;
	    } else if (fkTypeZeroToMany.isSelected()) {
	        return SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY;
	    } else if (fkTypeOneToMany.isSelected()) {
	        return SQLRelationship.ONE | SQLRelationship.MANY;        
	    } else throw new IllegalStateException("No FK cardinality selected");
	}

	private Deferrability getSelectedDeferrability() {
	    if (notDeferrable.isSelected()) {
	        return Deferrability.NOT_DEFERRABLE;
	    } else if (initiallyDeferred.isSelected()) {
	        return Deferrability.INITIALLY_DEFERRED;
	    } else if (initiallyImmediate.isSelected()) {
	        return Deferrability.INITIALLY_IMMEDIATE;
	    } else throw new IllegalStateException("No deferrability selected");
	}

	private UpdateDeleteRule getSelectedUpdateRule() {
	    if (updateCascade.isSelected()) {
	        return UpdateDeleteRule.CASCADE;
	    } else if (updateNoAction.isSelected()) {
	        return UpdateDeleteRule.NO_ACTION;
	    } else if (updateRestrict.isSelected()) {
	        return UpdateDeleteRule.RESTRICT;
	    } else if (updateSetDefault.isSelected()) {
	        return UpdateDeleteRule.SET_DEFAULT;
	    } else if (updateSetNull.isSelected()) {
	        return UpdateDeleteRule.SET_NULL;
	    } else throw new IllegalStateException("No update rule selected");
	}

	private UpdateDeleteRule getSelectedDeleteRule() {
	    if (deleteCascade.isSelected()) {
	        return UpdateDeleteRule.CASCADE;
	    } else if (deleteNoAction.isSelected()) {
	        return UpdateDeleteRule.NO_ACTION;
	    } else if (deleteRestrict.isSelected()) {
	        return UpdateDeleteRule.RESTRICT;
	    } else if (deleteSetDefault.isSelected()) {
	        return UpdateDeleteRule.SET_DEFAULT;
	    } else if (deleteSetNull.isSelected()) {
	        return UpdateDeleteRule.SET_NULL;
	    } else throw new IllegalStateException("No delete rule selected");
	}

    /**
     * Checks to see if its respective relationship is removed from
     * playpen. If yes, exit the editing dialog window.
     */
    public void childRemoved(SPChildEvent e) {
        logger.debug("SQLObject child was removed: "+e); //$NON-NLS-1$        
        if (relationship.equals(e.getChild())) {
            removeListeners();
            if (editDialog != null) {
                editDialog.dispose();
            }
        }
    }
    
    public void childAdded(SPChildEvent e) {
        // no-op
    }

    public void propertyChanged(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        boolean error = false;
        if (e.getSource() == relationship) {
            if (property.equals("name")) {
                error = DataEntryPanelChangeUtil.incomingChange(relationshipName, e);
            } else if (property.equals("textForParentLabel")) {
                error = DataEntryPanelChangeUtil.incomingChange(pkLabelTextField, e);                
            } else if (property.equals("textForChildLabel")) {
                error = DataEntryPanelChangeUtil.incomingChange(fkLabelTextField, e);
            } else if (property.equals("identifying")) {
                error = DataEntryPanelChangeUtil.incomingChange(identifyingGroup, identifyingButton.isSelected(), e);
            } else if (property.equals("pkCardinality")) {
                error = DataEntryPanelChangeUtil.incomingChange(pkTypeGroup, getSelectedPKCardinality(), e);
            } else if (property.equals("fkCardinality")) {
                error = DataEntryPanelChangeUtil.incomingChange(fkTypeGroup, getSelectedFKCardinality(), e);
            } else if (property.equals("deferrability")) {
                error = DataEntryPanelChangeUtil.incomingChange(deferrabilityGroup, getSelectedDeferrability(), e);
            } else if (property.equals("updateRule")) {
                error = DataEntryPanelChangeUtil.incomingChange(updateRuleGroup, getSelectedUpdateRule(), e);
            } else if (property.equals("deleteRule")) {
                error = DataEntryPanelChangeUtil.incomingChange(deleteRuleGroup, getSelectedDeleteRule(), e);
            }
        } else if (e.getSource() == relationshipLine) {
            if (property.equals("foregroundColor")) {
                error = DataEntryPanelChangeUtil.incomingChange(relationLineColor, e);
            }
        }
        if (error) {
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }
    }

    public void transactionEnded(TransactionEvent e) {
        // no-op
    }

    public void transactionRollback(TransactionEvent e) {
        // no-op
    }

    public void transactionStarted(TransactionEvent e) {
        // no-op
    }
}
