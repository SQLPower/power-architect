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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class PlayPenContentPane {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	protected PlayPen owner;
	private List<PlayPenComponent> children = new ArrayList<PlayPenComponent>();
	private List<Relationship> relations = new ArrayList<Relationship>();
	private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
	private PropertyChangeEventPassthrough propertyChangeEventPassthrough;


	public PlayPenContentPane(PlayPen owner) {
		this.owner = owner;
		propertyChangeEventPassthrough = new PropertyChangeEventPassthrough();
		owner.addPropertyChangeListener("zoom", new ZoomFixer()); //$NON-NLS-1$
	}
	
	
	/**
	 * Returns the PlayPen that this content pane belongs to.
	 */
	public PlayPen getOwner() {
		return owner;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public boolean contains(int x, int y) {
		return true;
	}

	/**
	 * Returns true.
	 */
	public boolean isValidateRoot() {
		logger.debug("isValidateRoot returning true"); //$NON-NLS-1$
		return true;
	}

	/**
	 * Looks for tooltip text in the component under the pointer,
	 * respecting the current zoom level.
	 */
	public String getToolTipText(MouseEvent e) {
		String text = null;
		PlayPenComponent c = getComponentAt(e.getPoint());
		if (c != null) {
			text = c.getToolTipText();
		}
		logger.debug("Checking for tooltip component at "+e.getPoint()+" is "+c+". tooltipText is "+text); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return text;
	}

	/**
	 *  Allows you to return the component that is at point p.  Since relations are always last 
	 *  If a non-relationship is at the same point it gets picked first. 
	 */
	public PlayPenComponent getComponentAt(Point p) {
		for (PlayPenComponent ppc : children) {
			if (ppc.contains(p)) {
				return ppc;
			}
		}
		for (Relationship ppc : relations) {
			if (ppc.contains(p)) {
				return (PlayPenComponent) ppc;
			}
		}
		return null;
	}


	/**
	 * Get the index of the first relation
	 */
	public int getFirstRelationIndex() {
		return children.size();
	}
	
	/**
	 * get the total number of components
	 */
	public int getComponentCount() {
		return children.size()+relations.size();
	}

	/**
	 * Get a component at position i.  
	 * 
	 * Note: All relations are at the end of the list 
	 */
	public PlayPenComponent getComponent(int i) {
		if (i < children.size()){
			return children.get(i);
		} else {
			return relations.get(i-children.size());
		}
		
	}

	/**
	 * Add a new component to the content pane.  
	 * 
	 * Note relations must be added after <code>getFirstrelaationIndex</code> and all others before
	 */
	public void add(PlayPenComponent c, int i) {
		if (c instanceof Relationship) {
			relations.add(i-children.size(),(Relationship)c);
		} else {
			children.add(i,c);
		}
		c.addPropertyChangeListener(propertyChangeEventPassthrough);
		c.addSelectionListener(getOwner());
		c.revalidate();
	}

	
	/**
	 * removes the component at index j
	 */
	public void remove(int j) {
		PlayPenComponent c;
		if (j < children.size()) {
			 c= children.get(j);
		} else {
			c = relations.get(j-children.size());
		}
		
		Rectangle r = c.getBounds();
		c.removePropertyChangeListener(propertyChangeEventPassthrough);
		c.removeSelectionListener(getOwner());
		if (j < children.size()) {
			children.remove(j);
		} else {
			relations.remove(j-children.size());
		}
		getOwner().repaint(r);
	}
	
	public void remove(PlayPenComponent c) {
		int j = children.indexOf(c);
		if ( j >= 0 ) remove(j);
		j= relations.indexOf(c);
		if (j>=0) remove(j+children.size());
	}

	/**
	 * Fixes table pane sizes after the play pen's zoom changes (because
	 * fonts render at different sizes in different zoom levels).
	 */
	private class ZoomFixer implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			for (PlayPenComponent ppc : children) {
				// only table panes will need validation because they have text
				if (! (ppc instanceof Relationship)) ppc.revalidate();
			}
		}
	}
	
	// ----------------- PropertyChangeListener Passthrough stuff ---------------------------
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
	    propertyChangeListeners.add(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
	    propertyChangeListeners.remove(l);
	}
	
	private void refirePropertyChanged(PropertyChangeEvent e) {
	    for (PropertyChangeListener l : propertyChangeListeners) {
	        l.propertyChange(e);
	    }
	}
	
	private class PropertyChangeEventPassthrough implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            refirePropertyChanged(e);
        }
	}
}
