package ca.sqlpower.architect.swingui;

import javax.swing.*;
import ca.sqlpower.architect.*;

public class Relationship extends JComponent {

	protected RelationshipUI ui;
	protected PlayPen pp;
	protected SQLRelationship model;
	protected TablePane pkTable;
	protected TablePane fkTable;

	static {
		UIManager.put(RelationshipUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.BasicRelationshipUI");
	}

	public Relationship(PlayPen pp, SQLRelationship model) {
		this.model = model;
		pkTable = pp.findTablePane(model.getPkTable());
		fkTable = pp.findTablePane(model.getFkTable());
		updateUI();
	}

	public Relationship(PlayPen pp, TablePane pkTable, TablePane fkTable) {
		model = new SQLRelationship();
		model.setPkTable(pkTable.getModel());
		model.setFkTable(fkTable.getModel());
		this.pkTable = pkTable;
		this.fkTable = fkTable;
		updateUI();
		setVisible(true);
		setBounds(10,10,100,100);
		// FIXME: map columns?
	}
	
	public void setUI(RelationshipUI ui) {super.setUI(ui);}

    public void updateUI() {
		setUI((RelationshipUI)UIManager.getUI(this));
		invalidate();
    }

    public String getUIClassID() {
        return RelationshipUI.UI_CLASS_ID;
    }

	public TablePane getPkTable() {
		return pkTable;
	}

	public TablePane getFkTable() {
		return fkTable;
	}
}
