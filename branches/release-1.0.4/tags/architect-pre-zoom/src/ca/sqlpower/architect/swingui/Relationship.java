package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.util.*;

public class Relationship extends JComponent implements Selectable, ComponentListener, SQLObjectListener {
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

	protected JPopupMenu popup;

	static {
		UIManager.put(RelationshipUI.UI_CLASS_ID, "ca.sqlpower.architect.swingui.IERelationshipUI");
	}

	/**
	 * This constructor simply creates a Relationship component for
	 * the given SQLRelationship and adds it to the playpen.  It
	 * doesn't maniuplate the model at all.
	 */
	public Relationship(PlayPen pp, SQLRelationship model) throws ArchitectException {
		this.pp = pp;
		this.model = model;
		setPkTable(pp.findTablePane(model.getPkTable()));
		setFkTable(pp.findTablePane(model.getFkTable()));

		setup();
	}

	/**
	 * This constructor makes a new Relationship component as well as
	 * a SQLRelationship model object.  It adds the SQLRelationship to
	 * the pk and fk table models, and adds the primary key of pkTable
	 * into the primary key of fkTable.
	 */
	public Relationship(PlayPen pp, TablePane pkTable, TablePane fkTable, boolean identifying) 
		throws ArchitectException {
		model = new SQLRelationship();
		model.setName(pkTable.getModel().getName()+"_"+fkTable.getModel().getName()+"_fk"); // XXX: need to ensure uniqueness!
		model.setIdentifying(identifying);
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

		setup();
	}

	/**
	 * All constructors have to call this after setting pp, model, pkTable, and fkTable.
	 */
	protected void setup() {
		updateUI();
		setOpaque(false);
		setBackground(Color.green);
		model.addSQLObjectListener(this);
		setToolTipText(model.getName());
		pkConnectionPoint = new Point();
		fkConnectionPoint = new Point();
		updateBounds(); // also sets bounds
		createPopup();
		setVisible(true);
	}

	protected void createPopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		popup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem(af.editRelationshipAction);
		popup.add(mi);

		mi = new JMenuItem(af.deleteSelectedAction);
		popup.add(mi);

		addMouseListener(new PopupListener());
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
		fireSelectionEvent(this);
		repaint();
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

	// --------------- mouse listener --------------------
	public static class PopupListener extends MouseAdapter {

		/**
		 * Double-click support.
		 */
		public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				ArchitectFrame.getMainInstance().editRelationshipAction.actionPerformed
					(new ActionEvent(evt.getSource(), ActionEvent.ACTION_PERFORMED, "DoubleClick"));
			}
		}

		public void mousePressed(MouseEvent evt) {
			evt.getComponent().requestFocus();
			maybeShowPopup(evt);
		}

		public void mouseReleased(MouseEvent evt) {
			maybeShowPopup(evt);

			// selection
			if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
				Relationship r = (Relationship) evt.getComponent();
				PlayPen pp = (PlayPen) r.getParent();
				pp.selectNone();
				r.setSelected(true);
			}
		}

		public void maybeShowPopup(MouseEvent evt) {
			if (evt.isPopupTrigger() && !evt.isConsumed()) {
				Relationship r = (Relationship) evt.getComponent();
				PlayPen pp = (PlayPen) r.getParent();
				pp.selectNone();
				r.setSelected(true);
				r.popup.show(r, evt.getX(), evt.getY());
			}
		}
	}

	// ------------------ sqlobject listener ----------------
	public void dbChildrenInserted(SQLObjectEvent e) {
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		if (e.getPropertyName() != null) {
			if (e.getPropertyName().equals("name")) {
				setToolTipText(model.getName());
			} else if (e.getPropertyName().equals("identifying")
					   || e.getPropertyName().equals("pkCardinality")
					   || e.getPropertyName().equals("fkCardinality")) {
				repaint();
			}
		}
	}

	public void dbStructureChanged(SQLObjectEvent e) {
	}
}
