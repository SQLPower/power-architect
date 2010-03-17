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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;

public class PlayPenContentPane extends AbstractSPObject {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	
	
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
	    Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
	            Arrays.asList(TablePane.class, Relationship.class, UsageComponent.class,
	                    CubePane.class, DimensionPane.class, VirtualCubePane.class)));		
	
	protected PlayPen playPen;
	private List<PlayPenComponent> children = new ArrayList<PlayPenComponent>();
	private List<Relationship> relations = new ArrayList<Relationship>();
	
	/**
	 * Maps component listeners by the listener that was passed in 
	 * (and will be passed in again  on removeComponentPropertyListener calls) 
	 * to the filtered listener that is created in addComponentPropertyListener
	 */
	private HashMap<SPListener, SPListener> componentListeners = new HashMap<SPListener, SPListener>();

	/**
	 * This boolean becomes true when a table pane has begun moving, 
	 * and will remain true until doneDragging() is called by the
	 * PlayPen's mouseReleased() method. This allows for location
	 * changes to be wrapped in a transaction.
	 */
	private boolean waitingToPersistLocation = false;
	
	@Constructor
	public PlayPenContentPane() {
	    this(null);	    
	}
	
	public PlayPenContentPane(PlayPen owner) {
	    super();
	    setPlayPen(owner);
	}
	
	/**
	 * Returns the PlayPen that this content pane belongs to.
	 */
	@Transient @Accessor
	public PlayPen getPlayPen() {
		return playPen;
	}

	@Transient @Mutator
	public void setPlayPen(PlayPen owner) {
	    if (playPen != null) {
	        throw new IllegalStateException("Cannot change PlayPen once it is already set!");
	    }
	    this.playPen = owner;
	    if (owner != null) {
	        owner.addPropertyChangeListener("zoom", new ZoomFixer()); //$NON-NLS-1$
	        firePropertyChange("playPen", null, owner);
	    }
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
	@NonBound
	public boolean isValidateRoot() {
		logger.debug("isValidateRoot returning true"); //$NON-NLS-1$
		return true;
	}

	/**
	 * Looks for tooltip text in the component under the pointer,
	 * respecting the current zoom level.
	 */
	@Transient @Accessor
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
	@NonBound
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
	@NonBound
	public int getFirstRelationIndex() {
		return children.size();
	}
	
	/**
	 * get the total number of components
	 */
	@NonBound
	public int getComponentCount() {
		return children.size()+relations.size();
	}

	/**
	 * Get a component at position i.  
	 * 
	 * Note: All relations are at the end of the list 
	 */
	@NonBound
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
	    c.setParent(this);
		if (c instanceof Relationship) {
			relations.add(i-children.size(),(Relationship)c);
		} else {
			children.add(i,c);
		}
		c.addSelectionListener(getPlayPen());
		fireChildAdded(c.getClass(), c, i);
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
		c.removeSelectionListener(getPlayPen());
		if (j < children.size()) {
			children.remove(j);
		} else {
			relations.remove(j-children.size());
		}
		fireChildRemoved(c.getClass(), c, j);
		getPlayPen().repaint(r);
	}
	
	public boolean remove(PlayPenComponent c) {
	    List<? extends PlayPenComponent> targetList;
	    if (children.contains(c)) targetList = children;
	    else if (relations.contains(c)) targetList = relations;
	    else return false;
	    
	    int i = targetList.indexOf(c);
	    targetList.remove(c);
	    fireChildRemoved(c.getClass(), c, i);
	    return true;
	    
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

	protected void addChildImpl(SPObject child, int pos) {
	    if (child instanceof Relationship) {
	        pos += children.size();
	    }
	    add((PlayPenComponent) child, pos);
	}

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return remove((PlayPenComponent) child);
    }

    @Accessor
    public ArchitectProject getParent() {
        return (ArchitectProject) super.getParent();
    }
    
    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (Relationship.class.isAssignableFrom(childType)) {
            return children.size();
        } else if (PlayPenComponent.class.isAssignableFrom(childType)) {
            return 0;
        } else {
            throw new IllegalArgumentException("Not an allowed child type");
        }
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }
    
    public List<? extends PlayPenComponent> getChildren() {
        List<PlayPenComponent> childrenList = new ArrayList<PlayPenComponent>();
        childrenList.addAll(children);
        childrenList.addAll(relations);
        return childrenList;
    }

    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalArgumentException("That is not a dependency, since this object has no depencies");
    }

    /**
     * Adds a listener to this content pane that will forward
     * a specific property's events received from its children components.
     * @param propertyName The property of the components the listener is interested in.
     * If null or an empty array, it will listen for all properties.
     * @param listener The listener that the component events will be forwarded to.
     */
    public void addComponentPropertyListener(String[] propertyNames, final SPListener listener) {
        
        final List<String> filter;
        if (propertyNames == null) {
            filter = Collections.emptyList();
        } else {
            filter = Arrays.asList(propertyNames);
        }
        
        final SPListener filteredListener = new AbstractSPListener() {
            public void propertyChanged(PropertyChangeEvent evt) {
                if (filter.size() == 0 || filter.contains(evt.getPropertyName())) {
                    listener.propertyChanged(evt);
                }
            }
        };  
        
        // Map the created listener by the listener passed in.
        componentListeners.put(listener, filteredListener);
        
        // Add listeners to all components that already existed.
        for (PlayPenComponent child : getChildren()) {
            child.addSPListener(filteredListener);
        }
        
        // Add a listener to add the created listener to future components,
        // and remove it from components when they are removed.
        addSPListener(new AbstractSPListener() {
            public void childAdded(SPChildEvent e) {
                e.getChild().addSPListener(filteredListener);
            }
            public void childRemoved(SPChildEvent e) {
                e.getChild().removeSPListener(filteredListener);
            }
        });
        
    }
        
    public void addComponentPropertyListener(String propertyName, final SPListener listener) {
        addComponentPropertyListener(new String[] {propertyName}, listener);
    }
    
    /**
     * Adds a component property listener that listens to any/all properties.
     * @param listener
     */
    public void addComponentPropertyListener(SPListener listener) {
        addComponentPropertyListener(new String[] {}, listener);
    }
    
    public void removeComponentPropertyListener(SPListener listener) {
        componentListeners.remove(listener);
    }

    @NonBound
    public boolean isWaitingToPersistLocation() {
        return waitingToPersistLocation;
    }

    public void startedDragging() {
        waitingToPersistLocation = true;
        begin("Started moving table pane");
    }
    
    /**
     * Called by PlayPen on its mouseReleased event to notify a component that
     * it may complete its location-change transaction if it has been waiting to.
     */
    public void doneDragging() {
        if (waitingToPersistLocation) {
            waitingToPersistLocation = false;
            commit();
        }
    }
    
    @NonBound
    public HashMap<SPListener, SPListener> getComponentListeners() {
        return componentListeners;
    }
    
    @NonBound
    public void setComponentListeners(HashMap<SPListener, SPListener> componentListeners) {
        this.componentListeners = componentListeners;
    }
    
}
