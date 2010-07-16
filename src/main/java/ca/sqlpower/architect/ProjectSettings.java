/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;

/**
 * A class that stores project settings (accessed by File -> Project Settings...),
 * and fires property change events when they are changed (to be listened to by
 * an ArchitectSwingSession so it can update the UI). It has the ability
 * to persist these properties to and from a server workspace.
 *
 * Two settings in the ProjectSettingsPanel are not contained in here,
 * because they are ProfileManagerSettings. 
 */

public class ProjectSettings extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();
    
    private boolean savingEntireSource = false;
    
    private boolean displayRelationshipLabel = true;

    private boolean relationshipLinesDirect = false;
    
    private boolean usingLogicalNames = true;

    private boolean showPkTag = true;
    private boolean showFkTag = true;
    private boolean showAkTag = true;
    
    private ColumnVisibility columnVisibility = ColumnVisibility.ALL;
    
    public static enum ColumnVisibility {
        ALL, 
        PK, 
        PK_FK, 
        PK_FK_UNIQUE, 
        PK_FK_UNIQUE_INDEXED;
    }
    
    @Constructor
    public ProjectSettings() {
        super();
        setName("Project Settings");
    }

    @Accessor(isInteresting=true)
    public boolean isSavingEntireSource() {
        return savingEntireSource;        
    }

    @Mutator
    public void setSavingEntireSource(boolean savingEntireSource) {
        boolean oldValue = this.savingEntireSource;
        this.savingEntireSource = savingEntireSource;
        firePropertyChange("savingEntireSource", oldValue, savingEntireSource);
    }

    @Accessor(isInteresting=true)
    public boolean isDisplayRelationshipLabel() {
        return displayRelationshipLabel;
    }

    @Mutator
    public void setDisplayRelationshipLabel(boolean displayRelationshipLabel) {
        boolean oldValue = this.displayRelationshipLabel;
        this.displayRelationshipLabel = displayRelationshipLabel;
        firePropertyChange("displayRelationshipLabel", oldValue, displayRelationshipLabel);
    }

    @Accessor(isInteresting=true)
    public boolean isRelationshipLinesDirect() {
        return relationshipLinesDirect;
    }

    @Mutator
    public void setRelationshipLinesDirect(boolean relationshipLinesDirect) {
        boolean oldValue = this.relationshipLinesDirect;
        this.relationshipLinesDirect = relationshipLinesDirect;
        firePropertyChange("relationshipLinesDirect", oldValue, relationshipLinesDirect);
    }

    @Accessor(isInteresting=true)
    public boolean isUsingLogicalNames() {
        return usingLogicalNames;
    }

    @Mutator
    public void setUsingLogicalNames(boolean usingLogicalNames) {
        boolean oldValue = this.usingLogicalNames;
        this.usingLogicalNames = usingLogicalNames;
        firePropertyChange("usingLogicalNames", oldValue, usingLogicalNames);
    }

    @Accessor(isInteresting=true)
    public boolean isShowPkTag() {
        return showPkTag;
    }

    @Mutator
    public void setShowPkTag(boolean showPkTag) {
        boolean oldValue = this.showPkTag;
        this.showPkTag = showPkTag;
        firePropertyChange("showPkTag", oldValue, showPkTag);
    }

    @Accessor(isInteresting=true)
    public boolean isShowFkTag() {
        return showFkTag;
    }
    
    @Mutator
    public void setShowFkTag(boolean showFkTag) {
        boolean oldValue = this.showFkTag;
        this.showFkTag = showFkTag;
        firePropertyChange("showFkTag", oldValue, showFkTag);
    }

    @Accessor(isInteresting=true)
    public boolean isShowAkTag() {
        return showAkTag;
    }

    @Mutator
    public void setShowAkTag(boolean showAkTag) {
        boolean oldValue = this.showAkTag;
        this.showAkTag = showAkTag;
        firePropertyChange("showAkTag", oldValue, showAkTag);
    }

    @Accessor(isInteresting=true)
    public ColumnVisibility getColumnVisibility() {
        return columnVisibility;
    }

    @Mutator
    public void setColumnVisibility(ColumnVisibility columnVisibility) {
        ColumnVisibility oldValue = this.columnVisibility;
        this.columnVisibility = columnVisibility;
        firePropertyChange("columnVisibility", oldValue, columnVisibility);
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalArgumentException("This object does not have dependencies");
    }
    
}
