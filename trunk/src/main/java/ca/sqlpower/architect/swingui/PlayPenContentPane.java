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
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.swingui.critic.ModelBadge;
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
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLDatabase;

public class PlayPenContentPane extends AbstractSPObject {
    private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);

    @SuppressWarnings("unchecked")
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections
            .unmodifiableList(new ArrayList<Class<? extends SPObject>>(Arrays.asList(PlayPenComponent.class)));

    /**
     * A list of component types that are dependent on other components (see
     * dependentComponents)
     */
    private static final List<Class<? extends PlayPenComponent>> dependentComponentTypes = Collections
            .unmodifiableList(new ArrayList<Class<? extends PlayPenComponent>>(Arrays.asList(Relationship.class,
                    UsageComponent.class)));

    private PlayPen playPen;

    /**
     * The list of components not dependent on any other components. They are
     * first in the children list.
     */
    private List<PlayPenComponent> components = new ArrayList<PlayPenComponent>();

    /**
     * These components are dependent on the first list of components. They come
     * after that list in terms of the overall children list. Currently stores
     * Relationships and UsageComponents
     */
    private List<PlayPenComponent> dependentComponents = new ArrayList<PlayPenComponent>();

    /**
     * The object this content pane is displaying information about. Must be one
     * of either SQLDatabase or OLAPSession and it must have a valid parent to
     * attach a listener to which watches for this model being removed.
     */
    private final SPObject modelContainer;

    /**
     * Maps component listeners by the listener that was passed in (and will be
     * passed in again on removeComponentPropertyListener calls) to the filtered
     * listener that is created in addComponentPropertyListener
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

    private final SPListener componentBoundChanges = new AbstractSPListener() {

        @Override
        public void propertyChanged(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("bounds") && playPen != null) {
                playPen.revalidate();
            }
        }

    };

    /**
     * Each badge in this list marks a UI object to have criticisms on the UI or
     * model object that is the subject of the badge. These badges are transient
     * and so stored here instead of in the content pane where they would then
     * be persisted.
     * <p>
     * IMPORTANT NOTE! These badges are NOT part of the objects returned by
     * getChildren. The badges are generated based on critics and neither the
     * badges or the criticisms are to be persisted, instead they are generated.
     * These badges may look like children but they are closer to being a kind
     * of transient child.
     * <p>
     * At this point we only allow one badge per model object but we may want to
     * change this in the future.
     */
    private final Map<Object, ModelBadge> badges = new HashMap<Object, ModelBadge>();

    /**
     * @param modelContainer
     *            Must be either a SQLDatabase or OLAPSession and must not be
     *            null. If the model is an OLAPSession it must also have a valid
     *            parent to listen to for removing the content pane correctly if
     *            the model is removed.
     */
    @Constructor
    public PlayPenContentPane(@ConstructorParameter(propertyName = "modelContainer") SPObject modelContainer) {
        super();
        setName("PlayPenContentPane");
        if (!(modelContainer instanceof SQLDatabase || modelContainer instanceof OLAPSession)) {
            throw new IllegalArgumentException("modelContainer must either be a SQLDatabase or OLAPSession");
        }
        this.modelContainer = modelContainer;
        if (modelContainer instanceof OLAPSession) {
            modelContainer.getParent().addSPListener(modelContainerListener);
        }
    }

    @Accessor
    public SPObject getModelContainer() {
        return modelContainer;
    }

    /**
     * Returns the PlayPen that this content pane belongs to.
     */
    @Transient
    @Accessor
    public PlayPen getPlayPen() {
        return playPen;
    }

    @Transient
    @Mutator
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
     * Looks for tooltip text in the component under the pointer, respecting the
     * current zoom level. Returns null if there is no object at the mouse event
     * for the tool tip.
     */
    @Transient
    @Accessor
    public String getToolTipText(MouseEvent e) {
        String text = null;
        PlayPenComponent c = getComponentAt(e.getPoint());
        if (c != null) {
            text = c.getToolTipText();
        }
        logger.debug("Checking for tooltip component at " + e.getPoint() + " is " + c + ". tooltipText is " + text); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return text;
    }

    /**
     * Allows you to return the component that is at point p. Since relations
     * are always last If a non-relationship is at the same point it gets picked
     * first.
     */
    @NonBound
    public PlayPenComponent getComponentAt(Point p) {
        for (PlayPenComponent ppc : getAllChildren()) {
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
            if (dependentType.isAssignableFrom(componentType))
                return true;
        }
        return false;
    }

    /**
     * Fixes table pane sizes after the play pen's zoom changes (because fonts
     * render at different sizes in different zoom levels).
     */
    private class ZoomFixer implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                begin("Revalidating all UI components.");
                for (PlayPenComponent ppc : getChildren()) {
                    // only table panes will need validation because they have
                    // text
                    if (!(ppc instanceof Relationship))
                        ppc.revalidate();
                }
                commit();
            } catch (Throwable t) {
                rollback("Failed to revalidate UI. " + t.getMessage());
                throw new RuntimeException(t);
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
        ppc.addSPListener(componentBoundChanges);
        ppc.connect();
        fireChildAdded(ppc.getClass(), ppc, pos);
        ppc.revalidate();
        if (playPen != null) {
            playPen.revalidate();
        }
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
        if (!removed)
            return false;
        fireChildRemoved(child.getClass(), child, index);
        child.setParent(null);
        if (getPlayPen() != null) {
            ((PlayPenComponent) child).removeSelectionListener(getPlayPen());
        }
        child.removeSPListener(componentBoundChanges);
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

    /**
     * Returns all of the 'children' of this content pane including the
     * transient {@link ModelBadge}s.
     */
    @NonProperty
    public List<? extends PlayPenComponent> getAllChildren() {
        List<PlayPenComponent> children = new ArrayList<PlayPenComponent>();
        children.addAll(components);
        children.addAll(badges.values());
        children.addAll(dependentComponents);
        return children;
    }

    @Accessor
    public ArchitectSwingProject getParent() {
        return (ArchitectSwingProject) super.getParent();
    }

    @Mutator
    public void setParent(SPObject parent) {
        if (parent instanceof ArchitectSwingProject || parent == null) {
            super.setParent(parent);
        } else {
            throw new IllegalArgumentException("Parent of PlayPenContentPane must be " + "ArchitectProject, not " +
                    parent.getClass().getSimpleName());
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
     * Adds a listener to this content pane that will forward a specific
     * property's events received from its children components.
     * 
     * @param propertyName
     *            The property of the components the listener is interested in.
     *            If null or an empty array, it will listen for all properties.
     * @param listener
     *            The listener that the component events will be forwarded to.
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
        addComponentPropertyListener(new String[] { propertyName }, listener);
    }

    /**
     * Adds a component property listener that listens to any/all properties.
     * 
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

    /**
     * Removes a badge from the content pane. The badges are a child type
     * that is transient so this method does not fire child events that would
     * cause persist calls.
     */
    public void removeModelBadge(ModelBadge badge) {
        badges.remove(badge.getSubject());
        badge.cleanup();
    }

    /**
     * Adds a badge to the content pane. The badges are a child type that
     * is transient so this method does not fire child events that would cause
     * persist calls.
     */
    public void addModelBadge(ModelBadge badge) {
        badge.setParent(PlayPenContentPane.this);
        badges.put(badge.getSubject(), badge);
    }

    /**
     * Returns the badge on the subject in the UI. This may return null if there
     * is no badge on the subject we are looking for.
     */
    public ModelBadge getBadge(Object subject) {
        return badges.get(subject);
    }
}
