package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
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

	/**
	 * This is the point where this relationship meets its PK table.
	 * The point is in the table's coordinate space.
	 */
	protected Point pkConnectionPoint;

	/**
	 * This is the point where this relationship meets its FK table.
	 * The point is in the table's coordinate space.
	 */
	protected Point fkConnectionPoint;

	static {
		UIManager.put(RelationshipUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.IERelationshipUI");
	}

	public Relationship(PlayPen pp, SQLRelationship model) throws ArchitectException {
		this (pp, pp.findTablePane(model.getPkTable()), pp.findTablePane(model.getFkTable()));
	}

	public Relationship(PlayPen pp, TablePane pkTable, TablePane fkTable) throws ArchitectException {
		updateUI();
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

		pkConnectionPoint = new Point();
		fkConnectionPoint = new Point();
		recalcConnectionPoints();

		setVisible(true);
		setBounds(1,1,1,1);
	}

    public void updateUI() {
		setUI((RelationshipUI)UIManager.getUI(this));
		invalidate();
    }

	/**
	 * Calculates the point at b - a.  This is useful if Points a and
	 * b are in the same coordinate space and you want to know the
	 * position of b relative to a.
	 */
	private Point coord(Point a, Point b) {
		return new Point(b.x - a.x, b.y - a.y);
	}

	protected void recalcConnectionPoints() {
		ui.bestConnectionPoints(pkTable, fkTable, pkConnectionPoint, fkConnectionPoint);
	}

	// -------------------- JComponent overrides -------------------
	public void paint(Graphics g) {
		recalcConnectionPoints();
		super.paint(g);
	}

	// -------------------- ACCESSORS AND MUTATORS ---------------------

	public void setUI(RelationshipUI ui) {
		super.setUI(ui);
		this.ui = ui;
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

	public Point getPkConnectionPoint() {
		return pkConnectionPoint;
	}

	public Point getFkConnectionPoint() {
		return fkConnectionPoint;
	}
}
