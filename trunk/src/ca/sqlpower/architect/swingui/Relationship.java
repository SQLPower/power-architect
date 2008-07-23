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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.event.PlayPenComponentMovedEvent;
import ca.sqlpower.architect.swingui.event.RelationshipConnectionPointEvent;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;

public class Relationship extends PlayPenComponent implements Selectable, SQLObjectListener, LayoutEdge {
	private static final Logger logger = Logger.getLogger(Relationship.class);

	private SQLRelationship model;
	private TablePane pkTable;
	private TablePane fkTable;

	private JPopupMenu popup;

	private boolean selected;
	
	private TablePaneBehaviourListener tpbListener = new TablePaneBehaviourListener();
	private List<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();

    /**
	 * The colour to highlight related columns with when this relationship is selected.
	 */
    private Color columnHighlightColour = Color.red;
 
    /**
     * This constructor is only for making a copy of an existing relationship component.
     * It is not useful in general, and it doesn't even produce a fully-functional copy.
     * For instance, the copy won't have a working popup menu.
     * 
     * @param r The relationship to copy
     * @param contentPane The content pane this copy will live in
     */
	Relationship(Relationship r, PlayPenContentPane contentPane) {
		super(contentPane);
		this.model = r.model;
		this.pkTable = r.pkTable;
		this.fkTable = r.fkTable;
		this.popup =r.popup;
		this.selected = false;
		this.tpbListener = new TablePaneBehaviourListener();
		this.columnHighlightColour = r.columnHighlightColour;
		this.selectionListeners = new ArrayList<SelectionListener>();
		
		this.foregroundColor = r.getForegroundColor();
		this.backgroundColor = r.getBackgroundColor();
		
		try {
			RelationshipUI ui = (RelationshipUI) r.getUI().getClass().newInstance();
			ui.installUI(this);
			ui.setFkConnectionPoint(new Point(((RelationshipUI) r.getUI()).getFkConnectionPoint()));
			ui.setPkConnectionPoint(new Point(((RelationshipUI) r.getUI()).getPkConnectionPoint()));
			ui.setOrientation(((RelationshipUI) r.getUI()).getOrientation());
			setUI(ui);
		} catch (InstantiationException e) {
			throw new RuntimeException("Woops, couldn't invoke no-args constructor of "+r.getUI().getClass().getName()); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Woops, couldn't access no-args constructor of "+r.getUI().getClass().getName()); //$NON-NLS-1$
		}
	}

	/**
	 * This constructor simply creates a Relationship component for
	 * the given SQLRelationship and adds it to the playpen.  It
	 * doesn't maniuplate the model at all.
	 */
	public Relationship(SQLRelationship model, PlayPenContentPane parent) throws ArchitectException {
		super(parent);
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
		setBackgroundColor(Color.green);
		model.addSQLObjectListener(this);
		setToolTipText(model.getName());
		
		// requires pkTable and fkTable to be initialized
		//ui.bestConnectionPoints(); // breaks when loading a new project?
	}

	protected void createPopup() {
		ArchitectFrame af = getPlayPen().getSession().getArchitectFrame();
		popup = new JPopupMenu();

		JMenuItem mi;

		mi = new JMenuItem(af.getEditRelationshipAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		popup.add(mi);

		mi = new JMenuItem(af.getDeleteSelectedAction());
		mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
		popup.add(mi);
		
		mi = new JMenuItem(af.getReverseRelationshipAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        popup.add(mi);
        
        JMenu setFocusToRelatedTables = new JMenu(Messages.getString("Relationship.setFocusMenu")); //$NON-NLS-1$
        mi = new JMenuItem();
        mi.setAction(af.getFocusToParentAction()); 
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        setFocusToRelatedTables.add(mi);
        mi = new JMenuItem();
        mi.setAction(af.getFocusToChildAction());
        mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
        setFocusToRelatedTables.add(mi);
        popup.add(setFocusToRelatedTables);
        
        if (logger.isDebugEnabled()) {
            mi = new JMenuItem(new AbstractAction("Show Mappings") { //$NON-NLS-1$

                public void actionPerformed(ActionEvent e) {
                    StringBuffer componentList = new StringBuffer();
                    for ( ColumnMapping columnMap : getModel().getMappings()) {
                        
                        componentList.append(columnMap).append("\n"); //$NON-NLS-1$
                    }
                    JOptionPane.showMessageDialog(getPlayPen(), new JScrollPane(new JTextArea(componentList.toString())));
                }
                
            });
 
            popup.add(mi);
        }
	}

	public Point getPreferredLocation() {
		return ((RelationshipUI) getUI()).getPreferredLocation();
	}
	
	@Override
	public String toString() {
		return "Relationship: "+model; //$NON-NLS-1$
	}

	// -------------------- PlayPenComponent overrides --------------------

    public void updateUI() {
    		RelationshipUI ui = (RelationshipUI) IERelationshipUI.createUI(this);
    		ui.installUI(this);
		setUI(ui);
		revalidate();
    }

	// --------------------- SELECTABLE SUPPORT ---------------------

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}
	
	protected void fireSelectionEvent(SelectionEvent e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Notifying "+selectionListeners.size() //$NON-NLS-1$
						 +" listeners of selection change"); //$NON-NLS-1$
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
			throw new IllegalStateException("Unknown selection event type "+e.getType()); //$NON-NLS-1$
		}
	}

	public void setSelected(boolean isSelected,int multiSelectType) {
		if (selected != isSelected) {
		    try {
		        Iterator it = getModel().getChildren().iterator();
		        while (it.hasNext()) {
		            SQLRelationship.ColumnMapping m = (ColumnMapping) it.next();
		            
                    if (isSelected) {
                        pkTable.addColumnHighlight(m.getPkColumn(), columnHighlightColour);
                        fkTable.addColumnHighlight(m.getFkColumn(), columnHighlightColour);
                    } else {
                        pkTable.removeColumnHighlight(m.getPkColumn(), columnHighlightColour);
                        fkTable.removeColumnHighlight(m.getFkColumn(), columnHighlightColour);
                    }
		        }
		    } catch (ArchitectException e) {
		        logger.error("Couldn't modify highlights for columns in the mapping", e); //$NON-NLS-1$
		    }
			selected = isSelected;
			fireSelectionEvent(new SelectionEvent(this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT,SelectionEvent.SINGLE_SELECT));
			repaint();
		}
	}

	public boolean isSelected() {
		return selected;
	}

	// -------------------- ACCESSORS AND MUTATORS ---------------------

    public String getUIClassID() {
        return RelationshipUI.UI_CLASS_ID;
    }

	public SQLRelationship getModel() {
		return model;
	}

	public void setPkTable(TablePane tp) {
		if (pkTable != null) {
			pkTable.removePropertyChangeListener(tpbListener);
		}
		pkTable = tp;
		pkTable.addPropertyChangeListener(tpbListener);
		// XXX: update model?
	}

	public TablePane getPkTable() {
		return pkTable;
	}

	public void setFkTable(TablePane tp) {
		if (fkTable != null) {
			fkTable.removePropertyChangeListener(tpbListener);
		}
		fkTable = tp;
		fkTable.addPropertyChangeListener(tpbListener);
		// XXX: update model?
	}

	public TablePane getFkTable() {
		return fkTable;
	}

	public Point getPkConnectionPoint() {
		return ((RelationshipUI) getUI()).getPkConnectionPoint();
	}

	public Point getFkConnectionPoint() {
		return ((RelationshipUI) getUI()).getFkConnectionPoint();
	}

	public void setPkConnectionPoint(Point p) {
		((RelationshipUI) getUI()).setPkConnectionPoint(p);
		revalidate();
	}

	public void setFkConnectionPoint(Point p) {
		((RelationshipUI) getUI()).setFkConnectionPoint(p);
		revalidate();
	}

	// ---------------- Component Listener ----------------
	private class TablePaneBehaviourListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            /* (non-Javadoc)
             * @see ca.sqlpower.architect.swingui.PlayPenComponentListener#componentResized(ca.sqlpower.architect.swingui.PlayPenComponentEvent)
             */
            if(evt instanceof PlayPenComponentMovedEvent) {
                PlayPenComponentMovedEvent e = (PlayPenComponentMovedEvent)evt;
                logger.debug("Component "+e.getPPComponent().getName()+" moved"); //$NON-NLS-1$ //$NON-NLS-2$
                if (e.getPPComponent() == pkTable || e.getPPComponent() == fkTable) {
                    revalidate();
                }
            }
            
            /* (non-Javadoc)
             * @see ca.sqlpower.architect.swingui.PlayPenComponentListener#componentResized(ca.sqlpower.architect.swingui.PlayPenComponentEvent)
             */
            else if(evt instanceof PlayPenComponent.PlayPenComponentResizedEvent) {
                PlayPenComponentResizedEvent e = (PlayPenComponentResizedEvent)evt;
                logger.debug("Component "+((PlayPenComponent)e.getSource()).getName()+" changed size"); //$NON-NLS-1$ //$NON-NLS-2$
                if (((PlayPenComponent)e.getSource()) == pkTable) {
                    setPkConnectionPoint(((RelationshipUI) getUI()).closestEdgePoint(true, getPkConnectionPoint())); // true == PK
                }
                if (((PlayPenComponent)e.getSource()) == fkTable) {
                    setFkConnectionPoint(((RelationshipUI) getUI()).closestEdgePoint(false, getFkConnectionPoint())); // false == FK
                }
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
		protected Point startingPk;
		protected Point startingFk;
		protected boolean movingPk;

		public RelationshipDecorationMover(Relationship r, boolean movePk) {
			this.r = r;
			this.movingPk = movePk;
			this.startingPk = new Point(r.getPkConnectionPoint().x, r.getPkConnectionPoint().y);
			this.startingFk = new Point(r.getFkConnectionPoint().x, r.getFkConnectionPoint().y);
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
				p = ((RelationshipUI) r.getUI()).closestEdgePoint(movingPk, p);
			} else {
				p.x = p.x - r.getFkTable().getX();
				p.y = p.y - r.getFkTable().getY();
				p = ((RelationshipUI) r.getUI()).closestEdgePoint(movingPk, p);
			}
			return p;
		}

		/**
		 * Cleans up this mover (it will no longer track mouse motion,
		 * and will become eligible for garbage collection unless this
		 * instance's creator saved a reference).
		 */
		public void mouseReleased(MouseEvent e) {
		    if (!(r.getPkConnectionPoint().equals(startingPk)) || !(r.getFkConnectionPoint().equals(startingFk))) 
		        r.firePropertyChange(new RelationshipConnectionPointEvent(r, new Point[] {this.startingPk, this.startingFk}, 
		                new Point[] {r.getPkConnectionPoint(),  r.getFkConnectionPoint()}));
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
        if (isSelected()) {
            for (SQLObject newChild : e.getChildren()) {
                SQLRelationship.ColumnMapping cm = (ColumnMapping) newChild;
                pkTable.addColumnHighlight(cm.getPkColumn(), columnHighlightColour); 
                fkTable.addColumnHighlight(cm.getFkColumn(), columnHighlightColour);
            }
        }
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
        if (isSelected()) {
            for (SQLObject oldChild : e.getChildren()) {
                SQLRelationship.ColumnMapping cm = (ColumnMapping) oldChild;
                pkTable.removeColumnHighlight(cm.getPkColumn(), columnHighlightColour);
                fkTable.removeColumnHighlight(cm.getFkColumn(), columnHighlightColour);
            }
        }
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		if (e.getPropertyName() != null) {
			if (e.getPropertyName().equals("name")) { //$NON-NLS-1$
				setToolTipText(model.getName());
			} else if (e.getPropertyName().equals("identifying") //$NON-NLS-1$
					   || e.getPropertyName().equals("pkCardinality") //$NON-NLS-1$
					   || e.getPropertyName().equals("fkCardinality")) { //$NON-NLS-1$
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
        return ((RelationshipUI) getUI()).intersects(region);
    }

    
    // ------- LayoutEdge methods --------

    public LayoutNode getHeadNode() {
        return fkTable;
    }

    public LayoutNode getTailNode() {
        return pkTable;
    }

    public JPopupMenu getPopup() {
        // Lazy load popup if it isn't created
        // We don't create it in the constructor because the
        // ArchitectFrame instance required won't exist at that time.
        if (popup == null) {
            createPopup();
        }
        return popup;
    }
    
    public boolean isStraightLine() {
        PlayPen pp = getPlayPen();
        if (pp != null) {
            ArchitectSwingSession session = pp.getSession();
            if (session != null) {
                return session.getRelationshipLinesDirect();
            }
        }
        return false;
    }

    /**
     * Sets the connectionPoints of the relationship from an array of
     * points. Currently, it's only accessed when undo/redo movement
     * of relationship connection points.
     * 
     * @param connectionPoints, size of 2. The first element is a point 
     *          representing the pk connection point, and the second
     *          element is a point representing the fk connection point.
     */
    public void setConnectionPoints(Point[] connectionPoints) {
        // XXX This shouldn't be here, but it's here to fix a bug with the relationship
        // orientation when performing undo/redo after moving connection points.
        updateUI();
        setPkConnectionPoint(connectionPoints[0]);
        setFkConnectionPoint(connectionPoints[1]);
    }

}
