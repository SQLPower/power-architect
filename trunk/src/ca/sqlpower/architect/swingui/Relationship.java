package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.MouseInputAdapter;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;

import org.apache.log4j.Logger;
import java.util.*;

public class Relationship extends PlayPenComponent implements Selectable, SQLObjectListener {
	private static final Logger logger = Logger.getLogger(Relationship.class);

	protected RelationshipUI ui;
	protected SQLRelationship model;
	protected TablePane pkTable;
	protected TablePane fkTable;

	protected JPopupMenu popup;

	protected boolean selected;
	
	private PlayPenComponentListener ppcListener = new PlayPenComponentListener();

	/**
	 * The colour to highlight related columns with when this relationship is selected.
	 */
    private Color columnHighlightColour = Color.red;

	/**
	 * This constructor simply creates a Relationship component for
	 * the given SQLRelationship and adds it to the playpen.  It
	 * doesn't maniuplate the model at all.
	 */
	public Relationship(PlayPen parentPP, SQLRelationship model) throws ArchitectException {
		super(parentPP.getPlayPenContentPane());
		this.model = model;
		setPkTable(getPlayPen().findTablePane(model.getPkTable()));
		setFkTable(getPlayPen().findTablePane(model.getFkTable()));

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
		
		// requires pkTable and fkTable to be initialized
		//ui.bestConnectionPoints(); // breaks when loading a new project?

		createPopup();
	}

	protected void createPopup() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		popup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem(af.editRelationshipAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		popup.add(mi);

		mi = new JMenuItem(af.deleteSelectedAction);
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
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

	// -------------------- PlayPenComponent overrides --------------------

    public void updateUI() {
    		RelationshipUI ui = (RelationshipUI) IERelationshipUI.createUI(this);
    		ui.installUI(this);
		setUI(ui);
		revalidate();
    }

	// --------------------- SELECTABLE SUPPORT ---------------------

	protected LinkedList selectionListeners = new LinkedList();

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(SelectionEvent e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Notifying "+selectionListeners.size()
						 +" listeners of selection change");
		}
		Iterator it = selectionListeners.iterator();
		if (e.getType() == SelectionEvent.SELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemSelected(e);
			}
		} else if (e.getType() == SelectionEvent.DESELECTION_EVENT) {
			while (it.hasNext()) {
				((SelectionListener) it.next()).itemDeselected(e);
			}
		} else {
			throw new IllegalStateException("Unknown selection event type "+e.getType());
		}
	}

	public void setSelected(boolean isSelected) {
		if (selected != isSelected) {
		    try {
		        Iterator it = getModel().getChildren().iterator();
		        while (it.hasNext()) {
		            SQLRelationship.ColumnMapping m = (ColumnMapping) it.next();
		            int pkColIdx = pkTable.getModel().getColumnIndex(m.getPkColumn());
		            int fkColIdx = fkTable.getModel().getColumnIndex(m.getFkColumn());
		            pkTable.setColumnHighlight(pkColIdx, isSelected ? columnHighlightColour : null);
		            fkTable.setColumnHighlight(fkColIdx, isSelected ? columnHighlightColour : null);
		        }
		    } catch (ArchitectException e) {
		        logger.error("Couldn't modify highlights for columns in the mapping", e);
		    }
			selected = isSelected;
			fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT));
			repaint();
		}
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
			pkTable.removePlayPenComponentListener(ppcListener);
		}
		pkTable = tp;
		pkTable.addPlayPenComponentListener(ppcListener);
		// XXX: update model?
	}

	public TablePane getPkTable() {
		return pkTable;
	}

	public void setFkTable(TablePane tp) {
		if (fkTable != null) {
			fkTable.removePlayPenComponentListener(ppcListener);
		}
		fkTable = tp;
		fkTable.addPlayPenComponentListener(ppcListener);
		// XXX: update model?
	}

	public TablePane getFkTable() {
		return fkTable;
	}

	public Point getPkConnectionPoint() {
		return ui.getPkConnectionPoint();
	}

	public Point getFkConnectionPoint() {
		return ui.getFkConnectionPoint();
	}

	public void setPkConnectionPoint(Point p) {
		ui.setPkConnectionPoint(p);
		revalidate();
	}

	public void setFkConnectionPoint(Point p) {
		ui.setFkConnectionPoint(p);
		revalidate();
	}

	// ---------------- Component Listener ----------------
	private class PlayPenComponentListener implements ca.sqlpower.architect.swingui.PlayPenComponentListener {

		/* (non-Javadoc)
		 * @see ca.sqlpower.architect.swingui.PlayPenComponentListener#componentMoved(ca.sqlpower.architect.swingui.PlayPenComponentEvent)
		 */
		public void componentMoved(PlayPenComponentEvent e) {
			logger.debug("Component "+e.getPPComponent().getName()+" moved");
			if (e.getPPComponent() == pkTable || e.getPPComponent() == fkTable) {
				revalidate();
			}
		}
		
		/* (non-Javadoc)
		 * @see ca.sqlpower.architect.swingui.PlayPenComponentListener#componentResized(ca.sqlpower.architect.swingui.PlayPenComponentEvent)
		 */
		public void componentResized(PlayPenComponentEvent e) {
			logger.debug("Component "+e.getPPComponent().getName()+" changed size");
			if (e.getPPComponent() == pkTable) {
				setPkConnectionPoint(ui.closestEdgePoint(true, getPkConnectionPoint())); // true == PK
			}
			if (e.getPPComponent() == fkTable) {
				setFkConnectionPoint(ui.closestEdgePoint(false, getFkConnectionPoint())); // false == FK
			}
		}

		public void componentMoveStart(PlayPenComponentEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void componentMoveEnd(PlayPenComponentEvent e) {
			// TODO Auto-generated method stub
			
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
		 * right way, and that it snaps to a straight line when
		 * close).  Whether the PK or FK table is the target depends
		 * on the state of the {@link #movingPk} property.
		 */
		protected Point translatePoint(Point p) {
			if (movingPk) {
				p.x = p.x - r.getPkTable().getX();
				p.y = p.y - r.getPkTable().getY();
				p = r.ui.closestEdgePoint(movingPk, p);
			} else {
				p.x = p.x - r.getFkTable().getX();
				p.y = p.y - r.getFkTable().getY();
				p = r.ui.closestEdgePoint(movingPk, p);
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
        // doesn't matter
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
        // FIXME: should check if the table is too short to meet the connection point
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
        // not sure if this ever happens!
	}

    /**
     * Determines if the given rectangle is visibly touching this component.
     * 
     * @param region The region to test.
     * @return Whether or not this Relationship visibly intersects the given region
     */
    public boolean intersects(Rectangle region) {
        return ui.intersects(region);
    }
}
