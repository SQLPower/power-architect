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
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLDatabase;

public class PlayPenContentPane extends AbstractSPObject {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
	    Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
	            Arrays.asList(PlayPenComponent.class)));  
	
	/**
	 * A list of component types that are dependent on other components (see dependentComponents)
	 */
	private static final List<Class<? extends PlayPenComponent>> dependentComponentTypes = 
	    Collections.unmodifiableList(new ArrayList<Class<? extends PlayPenComponent>>(
	            Arrays.asList(Relationship.class, UsageComponent.class))); 

    private PlayPen playPen;
    
    /**
     * The list of components not dependent on any other components.
     * They are first in the children list.
     */
	private List<PlayPenComponent> components = new ArrayList<PlayPenComponent>();
	
	/**
	 * These components are dependent on the first list of components.
	 * They come after that list in terms of the overall children list.
	 * Currently stores Relationships and UsageComponents
	 */
	private List<PlayPenComponent> dependentComponents = new ArrayList<PlayPenComponent>();  
    
    private SPObject modelContainer;
    
    /**
     * Maps component listeners by the listener that was passed in 
     * (and will be passed in again  on removeComponentPropertyListener calls) 
     * to the filtered listener that is created in addComponentPropertyListener
     */
    private HashMap<SPListener, SPListener> componentListeners = new HashMap<SPListener, SPListener>();
    
    private final SPListener modelContainerListener = new AbstractSPListener() {
        public void childRemoved(SPChildEvent e) {
            if (e.getChild() == modelContainer && e.getSource().isMagicEnabled()) {
                try {
                    getParent().removeChild(PlayPenContentPane.this);                    
                } catch (ObjectDependentException ex) {
                    throw new RuntimeException(ex);
                }
                e.getSource().removeSPListener(this);
            }            
        }
    };
	
	@Constructor
	public PlayPenContentPane(@ConstructorParameter(propertyName="modelContainer") SPObject modelContainer) {
	    this("PlayPenContentPane");
	    setModelContainer(modelContainer);
	}
	
	public PlayPenContentPane() {
	    this("PlayPenContentPane");
	}
	
	public PlayPenContentPane(String name) {
	    super();
	    setName(name);
	}
	
	@Accessor
	public SPObject getModelContainer() {
	    return modelContainer;
	}
	
	@Mutator
	public void setModelContainer(SPObject modelContainer) {
	    if (this.modelContainer != null) throw new IllegalStateException(
	            "Cannot set the model container once it is already set!");
	    if (!(modelContainer instanceof SQLDatabase || modelContainer instanceof OLAPSession)) {
	        throw new IllegalArgumentException("modelContainer must either be a SQLDatabase or OLAPSession");
	    }
	    this.modelContainer = modelContainer;
	    modelContainer.getParent().addSPListener(modelContainerListener);
	    firePropertyChange("modelContainer", null, modelContainer);
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
        setPlayPenListeningToComponents();
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
        for (PlayPenComponent ppc : getChildren(PlayPenComponent.class)) {
            if (ppc.contains(p)) {
                return ppc;
            }
        }
        return null;
    }
    
    @NonBound
    public int getFirstDependentComponentIndex() {
        return components.size();
    }
    
    @NonBound
    public static boolean isDependentComponentType(Class<? extends PlayPenComponent> componentType) {
        for (Class<? extends PlayPenComponent> dependentType : dependentComponentTypes) {
            if (dependentType.isAssignableFrom(componentType)) return true;
        }
        return false;
    }

    /**
     * Fixes table pane sizes after the play pen's zoom changes (because
     * fonts render at different sizes in different zoom levels).
     */
    private class ZoomFixer implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            for (PlayPenComponent ppc : getChildren()) {
                // only table panes will need validation because they have text             
                if (!(ppc instanceof Relationship)) ppc.revalidate();
            }
        }
    }
    
    protected void addChildImpl(SPObject child, int pos) {
        PlayPenComponent ppc = (PlayPenComponent) child;
        if (dependentComponentTypes.contains(ppc.getClass())) {        
            dependentComponents.add(pos - components.size(), ppc);
        } else {
            components.add(pos, ppc);
        }
        ppc.setParent(this);
        if (getPlayPen() != null) {
            ppc.addSelectionListener(getPlayPen());
        }
        if (ppc instanceof TablePane) {
            ((TablePane) ppc).connect();
        }
        fireChildAdded(ppc.getClass(), ppc, pos);
        ppc.revalidate();
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        int index = getChildren().indexOf(child);
        boolean removed;
        if (dependentComponentTypes.contains(child.getClass())) {
            removed = dependentComponents.remove(child);
        } else {
            removed = components.remove(child);
        }        
        if (!removed) return false;
        fireChildRemoved(child.getClass(), child, index);
        child.setParent(null);
        if (playPen != null) {
            playPen.repaint();
        }
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        int offset = 0;
        for (Class<? extends SPObject> type : allowedChildTypes) {
            if (type.isAssignableFrom(childType)) {
                return offset;
            } else {
                offset += getChildren(type).size();
            }
        }
        throw new IllegalArgumentException();
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }
    
    public List<? extends PlayPenComponent> getChildren() {
        List<PlayPenComponent> children = new ArrayList<PlayPenComponent>();
        children.addAll(components);
        children.addAll(dependentComponents);
        return children;
    }
    
    @Accessor
    public ArchitectProject getParent() {
        return (ArchitectProject) super.getParent();
    }
    
    @Mutator
    public void setParent(SPObject parent) {
        if (parent instanceof ArchitectProject || parent == null) {
            super.setParent(parent);
        } else {
            throw new IllegalArgumentException("Parent of PlayPenContentPane must be " +
            		"ArchitectProject, not " + parent.getClass().getSimpleName());
        }
    }
    
    public boolean allowsChildren() {
        return true;
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
    public HashMap<SPListener, SPListener> getComponentListeners() {
        return componentListeners;
    }
    
    @NonBound
    public void setComponentListeners(HashMap<SPListener, SPListener> componentListeners) {
        this.componentListeners = componentListeners;
    }

    private void setPlayPenListeningToComponents() {
        for (PlayPenComponent ppc : getChildren()) {
            // In case it already had it
            ppc.removeSelectionListener(getPlayPen());
            ppc.addSelectionListener(getPlayPen());
        }
    }
    
}
