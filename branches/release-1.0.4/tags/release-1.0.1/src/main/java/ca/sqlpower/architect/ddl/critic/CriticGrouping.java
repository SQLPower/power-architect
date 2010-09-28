/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.ddl.critic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;

/**
 * Critics can be grouped and enabled or disabled as a group. This gives the
 * ability to set a group of critics to be enabled or disabled and to persist
 * this setting.
 */
public class CriticGrouping extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.<Class<? extends SPObject>>singletonList(CriticAndSettings.class);
    
    /**
     * Contains all of the settings for each critic known to the group. Each
     * critic in this list must have the same {@link CriticAndSettings#getPlatformType()} value.
     */
    private final List<CriticAndSettings> settings = new ArrayList<CriticAndSettings>();

    /**
     * Flags if the critics in this group should be enabled or disabled as a
     * group. The error level or setting them individually to be disabled can
     * be done in the individual settings as well.
     */
    private boolean enabled = true;

    /**
     * All critics in this group must have the same platform type that must
     * match this platform type.
     */
    private final String platformType;

    /**
     * @param platformType Cannot be null.
     */
    @Constructor
    public CriticGrouping(
            @ConstructorParameter(propertyName="platformType") String platformType) {
        this.platformType = platformType;
        setName("Critic group for platform " + platformType);
    }

    /**
     * {@link CriticAndSettings} added to this grouping must match in {@link #platformType}.
     */
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof CriticAndSettings) {
            final CriticAndSettings critic = (CriticAndSettings) child;
            if (!getPlatformType().equals(critic.getPlatformType())) {
                throw new IllegalArgumentException("The platform type " + critic.getPlatformType() + 
                        " does not match this groups type of " + getPlatformType());
            }
            settings.add(index, critic);
            critic.setParent(this);
            fireChildAdded(CriticAndSettings.class, child, index);
        } else {
            throw new IllegalStateException("Invalid child type " + child);
        }
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        int index = settings.indexOf(child);
        boolean removed = settings.remove(child);
        if (removed) {
            fireChildRemoved(CriticAndSettings.class, child, index);
            child.setParent(null);
        }
        return removed;
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        return Collections.unmodifiableList(settings);
    }

    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        for (CriticAndSettings setting : settings) {
            setting.removeDependency(dependency);
        }
    }

    @Mutator
    public void setEnabled(boolean enabled) {
        boolean oldVal = this.enabled;
        this.enabled = enabled;
        firePropertyChange("enabled", oldVal, enabled);
    }

    @Accessor
    public boolean isEnabled() {
        return enabled;
    }

    @Accessor
    public String getPlatformType() {
        return platformType;
    }
    
    @NonProperty
    public List<CriticAndSettings> getSettings() {
        return Collections.unmodifiableList(settings);
    }
    
    @Override
    @Accessor
    public CriticManager getParent() {
        return (CriticManager) super.getParent();
    }
    
    @Override
    @Mutator
    public void setParent(SPObject parent) {
        if (parent != null && !(parent instanceof CriticManager)) {
            throw new IllegalArgumentException("Critic groups must be a child of a critic manager.");
        }
        super.setParent(parent);
    }
}
