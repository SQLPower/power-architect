package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.event.MouseInputAdapter;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;
import java.util.*;

public class Relationship extends PlayPenComponent implements Selectable, ComponentListener, SQLObjectListener {
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

	protected MouseListener mouseListener;

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
		pkConnectionPoint = new Point();
		fkConnectionPoint = new Point();
		updateUI();
		setOpaque(false);
		setBackground(Color.green);
		model.addSQLObjectListener(this);
		setToolTipText(model.getName());
		
		ui.bestConnectionPoints(pkTable, fkTable,
								pkConnectionPoint,  // gets updated (in pktable-space)
								fkConnectionPoint); // gets updated (in fktable-space)

		createPopup();
		setVisible(true);
		mouseListener = new MouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	protected void createPopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		popup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem(af.editRelationshipAction);
		popup.add(mi);

		mi = new JMenuItem(af.deleteSelectedAction);
		popup.add(mi);
	}

	/**
	 * Calculates the point at b - a.  This is useful if Points a and
	 * b are in the same coordinate space and you want to know the
	 * position of b relative to a.
	 */
	private Point coord(Point a, Point b) {
		return new Point(b.x - a.x, b.y - a.y);
	}

	public Point getPreferredLocation() {
		return ui.getPreferredLocation();
	}

	// -------------------- JComponent overrides --------------------

    public void updateUI() {
		setUI((RelationshipUI)UIManager.getUI(this));
		invalidate();
    }

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
		this.ui = ui;
		super.setUI(ui);
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

	public void setPkConnectionPoint(Point p) {
		pkConnectionPoint = p;
		revalidate();
	}

	public void setFkConnectionPoint(Point p) {
		fkConnectionPoint = p;
		revalidate();
	}

	// ---------------- Component Listener ----------------

	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentMoved(ComponentEvent e) {
		logger.debug("Component "+e.getComponent().getName()+" moved");
		if (e.getComponent() == pkTable || e.getComponent() == fkTable) {
			revalidate();
		}
	}

	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentResized(ComponentEvent e) {
		logger.debug("Component "+e.getComponent().getName()+" changed size");
		if (e.getComponent() == pkTable || e.getComponent() == fkTable) {
			revalidate();
		}
	}

	public void componentShown(ComponentEvent e) {
	}
	
	public void componentHidden(ComponentEvent e) {
	}

	// ------------------ MOUSE LISTENER --------------------
	protected class MouseListener extends MouseInputAdapter {

		/**
		 * Double-click support.
		 */
		public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				ArchitectFrame.getMainInstance().editRelationshipAction.actionPerformed
					(new ActionEvent(evt.getSource(),
									 ActionEvent.ACTION_PERFORMED,
									 "DoubleClick"));
			}
		}
		
		public void mousePressed(MouseEvent evt) {
			evt.getComponent().requestFocus();
			maybeShowPopup(evt);

			if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
				// selection
				Relationship r = (Relationship) evt.getComponent();
				PlayPen pp = (PlayPen) r.getPlayPen();
				pp.selectNone();
				r.setSelected(true);

				// moving pk/fk decoration
				Point p = evt.getPoint();
				boolean overPkDec = ui.isOverPkDecoration(p);
				if (overPkDec || ui.isOverFkDecoration(p)) {
					new RelationshipDecorationMover(r, overPkDec);
				}
			}
		}
		
		public void mouseReleased(MouseEvent evt) {
			maybeShowPopup(evt);
		}

		public void maybeShowPopup(MouseEvent evt) {
			if (evt.isPopupTrigger() && !evt.isConsumed()) {
				Relationship r = (Relationship) evt.getComponent();
				PlayPen pp = (PlayPen) r.getPlayPen();
				pp.selectNone();
				r.setSelected(true);
				r.showPopup(r.popup, evt.getPoint());
			}
		}
	}

	/**
	 * The RelationshipDecorationMover responds to mouse events on the
	 * relationship by moving either the PK or FK connection point so
	 * it is near the mouse's current position.  It ceases this
	 * activity when a mouse button is released.
	 *
	 * <p>The normal way to create a RelationshipDecorationMover is like this:
	 * <pre>
	 *  new RelationshipDecorationMover(myRelationship, &lt;true|false&gt;);
	 * </pre>
	 * note that no reference to the object is saved; it will cleanly dispose 
	 * itself when a mouse button is lifted and hence become eligible for garbage
	 * collection.
	 */
	protected static class RelationshipDecorationMover extends MouseInputAdapter {

		protected Relationship r;
		protected boolean movingPk;

		public RelationshipDecorationMover(Relationship r, boolean movePk) {
			this.r = r;
			this.movingPk = movePk;
			r.getPlayPen().addMouseMotionListener(this);
			r.getPlayPen().addMouseListener(this);
			r.getPlayPen().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		/**
		 * Moves either the PK or FK decoration (depending on the
		 * {@link #movingPk} flag) so it is as close to the mouse
		 * pointer as possible, while still being attached to an edge
		 * of the parent (for PK) or child (for FK) table.
		 */
		public void mouseMoved(MouseEvent e) {
			Point p = new Point(e.getPoint());
			r.getPlayPen().unzoomPoint(p);
			if (movingPk) {
				r.setPkConnectionPoint(translatePoint(p));
			} else {
				r.setFkConnectionPoint(translatePoint(p));
			}
		}

		/**
		 * Forwards to {@link #mouseMoved}.
		 */
		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
		}

		/**
		 * Translates the given point from Relationship coordinates
		 * into PKTable or FKTable coordinates, with the help of the
		 * Relationship's UI delegate (which ensures the decoration
		 * still lines up with the table's edge, and that it faces the
		 * right way).  Whether the PK or FK table is the target
		 * depends on the state of the {@link #movingPk} property.
		 */
		protected Point translatePoint(Point p) {
			if (movingPk) {
				p.x = p.x - r.getPkTable().getX();
				p.y = p.y - r.getPkTable().getY();
				p = r.ui.closestEdgePoint(r.getPkTable(), p);
			} else {
				p.x = p.x - r.getFkTable().getX();
				p.y = p.y - r.getFkTable().getY();
				p = r.ui.closestEdgePoint(r.getFkTable(), p);
			}
			return p;
		}

		/**
		 * Cleans up this mover (it will no longer track mouse motion,
		 * and will become eligible for garbage collection unless this
		 * instance's creator saved a reference).
		 */
		public void mouseReleased(MouseEvent e) {
			cleanup();
		}

		protected void cleanup() {
			r.getPlayPen().removeMouseMotionListener(this);
			r.getPlayPen().removeMouseListener(this);
			r.getPlayPen().setCursor(null);
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
