package ca.sqlpower.architect.swingui;

import javax.swing.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.util.*;

public class Relationship extends JComponent {
	private static final Logger logger = Logger.getLogger(Relationship.class);

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
		setVisible(true);
		setBounds(1,1,1,1);
		logger.debug("Created new Relationship component with pkTable="+pkTable+"; fkTable="+fkTable);
	}

	public Relationship(PlayPen pp, TablePane pkTable, TablePane fkTable) throws ArchitectException {
		model = new SQLRelationship();
		model.setName(pkTable.getModel().getName()+"_"+fkTable.getModel().getName()+"_fk"); // XXX: need to ensure uniqueness!
		model.setPkTable(pkTable.getModel());
		model.setFkTable(fkTable.getModel());
		this.pkTable = pkTable;
		this.fkTable = fkTable;
		pkTable.getModel().addExportedKey(model);
		fkTable.getModel().addImportedKey(model);

		Iterator pkCols = pkTable.getModel().getColumns().iterator();
		while (pkCols.hasNext()) {
			SQLColumn pkCol = (SQLColumn) pkCols.next();
			if (pkCol.getPrimaryKeySeq() == null) break;
			SQLColumn fkCol = (SQLColumn) pkCol.clone();
			fkTable.getModel().addColumn(fkTable.getModel().pkSize(), fkCol); // adds to the primary key of fktable
			model.addMapping(pkCol, fkCol);
		}
		updateUI();
		setVisible(true);
		setBounds(1,1,1,1);
	}
	
	public void setUI(RelationshipUI ui) {super.setUI(ui);}

    public void updateUI() {
		setUI((RelationshipUI)UIManager.getUI(this));
		invalidate();
    }

    public String getUIClassID() {
        return RelationshipUI.UI_CLASS_ID;
    }

	public SQLRelationship getModel() {
		return model;
	}

	public TablePane getPkTable() {
		return pkTable;
	}

	public TablePane getFkTable() {
		return fkTable;
	}
}
