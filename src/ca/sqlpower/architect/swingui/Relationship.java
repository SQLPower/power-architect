package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.util.*;

public class Relationship extends JComponent implements Selectable, ComponentListener {
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
		setOpaque(false);
		setBackground(Color.green);
		model = new SQLRelationship();
		model.setName(pkTable.getModel().getName()+"_"+fkTable.getModel().getName()+"_fk"); // XXX: need to ensure uniqueness!
		model.setPkTable(pkTable.getModel());
		model.setFkTable(fkTable.getModel());

		setPkTable(pkTable);
		setFkTable(fkTable);

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
		updateBounds(); // also sets bounds

		setVisible(true);
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

	protected void updateBounds() {
		ui.updateBounds();
	}

	// -------------------- JComponent overrides -------------------

	// --------------------- SELECTABLE SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(Selectable source) {
		SelectionEvent e = new SelectionEvent(source);
		logger.debug("Notifying "+selectionListeners.size()+" listeners of selection change");
		Iterator it = selectionListeners.iterator();
		while (it.hasNext()) {
			((SelectionListener) it.next()).itemSelected(e);
		}
	}

	protected boolean selected;

	public void setSelected(boolean isSelected) {
		selected = isSelected;
	}

	public boolean isSelected() {
		return selected;
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

	public void setPkTable(TablePane tp) {
		if (pkTable != null) {
			pkTable.removeComponentListener(this);
		}
		pkTable = tp;
		pkTable.addComponentListener(this);
		// XXX: update model?
	}

	public TablePane getPkTable() {
		return pkTable;
	}

	public void setFkTable(TablePane tp) {
		if (fkTable != null) {
			fkTable.removeComponentListener(this);
		}
		fkTable = tp;
		fkTable.addComponentListener(this);
		// XXX: update model?
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

	// ---------------- Component Listener ----------------

	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentMoved(ComponentEvent e) {
		logger.debug("Component "+e.getComponent().getName()+" moved");
		if (e.getComponent() == pkTable || e.getComponent() == fkTable) {
			updateBounds();
		}
	}

	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentResized(ComponentEvent e) {
		logger.debug("Component "+e.getComponent().getName()+" changed size");
		if (e.getComponent() == pkTable || e.getComponent() == fkTable) {
			updateBounds();
		}
	}

	public void componentShown(ComponentEvent e) {
	}
	
	public void componentHidden(ComponentEvent e) {
	}
}
