package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import ca.sqlpower.architect.*;
import java.util.*;
import java.sql.DatabaseMetaData;
import org.apache.log4j.Logger;

public class RelationshipEditPanel extends JPanel
	implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(RelationshipEditPanel.class);

	protected SQLRelationship relationship;

	protected JTextField relationshipName;

	protected ButtonGroup identifyingGroup;
	protected JRadioButton identifyingButton;
	protected JRadioButton nonIdentifyingButton;

	protected JLabel pkTableName;
	protected ButtonGroup pkTypeGroup;
	protected JRadioButton pkTypeZeroToMany;
	protected JRadioButton pkTypeOneToMany;
	protected JRadioButton pkTypeZeroOne;

	protected JLabel fkTableName;
	protected ButtonGroup fkTypeGroup;
	protected JRadioButton fkTypeZeroToMany;
	protected JRadioButton fkTypeOneToMany;
	protected JRadioButton fkTypeZeroOne;

	public RelationshipEditPanel() {
		super(new BorderLayout());
		JPanel topPanel = new JPanel(new FormLayout());
		topPanel.add(new JLabel("Relationship Name"));
		topPanel.add(relationshipName = new JTextField());
		topPanel.add(new JLabel("Relationship Type"));
		JPanel typePanel = new JPanel();

		identifyingGroup = new ButtonGroup();
		typePanel.add(identifyingButton = new JRadioButton("Identifying"));
		identifyingGroup.add(identifyingButton);
		typePanel.add(nonIdentifyingButton = new JRadioButton("Non-Identifying"));
		identifyingGroup.add(nonIdentifyingButton);
		topPanel.add(typePanel);
		add(topPanel, BorderLayout.NORTH);
		
		JPanel pkPanel = new JPanel(new FormLayout());
		pkPanel.setBorder(new TitledBorder("Primary Key End"));
		pkPanel.add(new JLabel("PK Table"));
		pkPanel.add(pkTableName = new JLabel("Unknown"));
		pkTypeGroup = new ButtonGroup();
		pkPanel.add(new JLabel(""));
		pkPanel.add(pkTypeZeroToMany = new JRadioButton("Zero or More"));
		pkTypeGroup.add(pkTypeZeroToMany);
		pkPanel.add(new JLabel("Cardinality"));
		pkPanel.add(pkTypeOneToMany = new JRadioButton("One or More"));
		pkTypeGroup.add(pkTypeOneToMany);
		pkPanel.add(new JLabel(""));
		pkPanel.add(pkTypeZeroOne = new JRadioButton("Zero or One"));
		pkTypeGroup.add(pkTypeZeroOne);
		add(pkPanel, BorderLayout.WEST);

		JPanel fkPanel = new JPanel(new FormLayout());
		fkPanel.setBorder(new TitledBorder("Foreign Key End"));
		fkPanel.add(new JLabel("FK Table"));
		fkPanel.add(fkTableName = new JLabel("Unknown"));
		fkTypeGroup = new ButtonGroup();
		fkPanel.add(new JLabel(""));
		fkPanel.add(fkTypeZeroToMany = new JRadioButton("Zero or More"));
		fkTypeGroup.add(fkTypeZeroToMany);
		fkPanel.add(new JLabel("Cardinality"));
		fkPanel.add(fkTypeOneToMany = new JRadioButton("One or More"));
		fkTypeGroup.add(fkTypeOneToMany);
		fkPanel.add(new JLabel(""));
		fkPanel.add(fkTypeZeroOne = new JRadioButton("Zero or One"));
		fkTypeGroup.add(fkTypeZeroOne);
		add(fkPanel, BorderLayout.EAST);		
	}


	protected void setRelationship(SQLRelationship r) {
		this.relationship = r;
		relationshipName.setText(r.getName());
		pkTableName.setText(relationship.getPkTable().getName());
		fkTableName.setText(relationship.getFkTable().getName());
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
		}
		int fkc = r.getFkCardinality();
		if (fkc == (SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY)){
			fkTypeZeroToMany.setSelected(true);
		} else if (fkc == (SQLRelationship.ZERO | SQLRelationship.ONE)){
			fkTypeZeroOne.setSelected(true);
		} else if (fkc == (SQLRelationship.ONE | SQLRelationship.MANY)){
			fkTypeOneToMany.setSelected(true);
		}
	}

	protected void cleanup() {
		
	}

	// ------------------ ARCHITECT PANEL INTERFACE ---------------------
	
	/**
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 */
	public void applyChanges() {
		relationship.setName(relationshipName.getText());
		relationship.setIdentifying(identifyingButton.isSelected());

		if (pkTypeZeroOne.isSelected()) {
			relationship.setPkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE);
		} else if (pkTypeZeroToMany.isSelected()) {
			relationship.setPkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY);
		} else if (pkTypeOneToMany.isSelected()) {
			relationship.setPkCardinality(SQLRelationship.ONE | SQLRelationship.MANY);
		}

		if (fkTypeZeroOne.isSelected()) {
			relationship.setFkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE);
		} else if (fkTypeZeroToMany.isSelected()) {
			relationship.setFkCardinality(SQLRelationship.ZERO | SQLRelationship.ONE | SQLRelationship.MANY);
		} else if (fkTypeOneToMany.isSelected()) {
			relationship.setFkCardinality(SQLRelationship.ONE | SQLRelationship.MANY);
		}
		cleanup();
	}

	/**
	 * Does nothing because this version of ColumnEditPanel works
	 * directly on the live data.
	 *
	 * <p>XXX: in architect version 2, this will undo the changes to
	 * the model.
	 */
	public void discardChanges() {
		cleanup();
	}
}
