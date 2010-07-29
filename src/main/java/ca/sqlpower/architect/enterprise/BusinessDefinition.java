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

package ca.sqlpower.architect.enterprise;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;

/**
 * This business definition object associates a business term to its definition.
 * Essentially, this is a dictionary entry for business words used within an
 * organization.
 */
public class BusinessDefinition extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.emptyList();
    
    /**
     * {@link #getTerm()}
     */
    private String term;
    
    /**
     * {@link #getDefinition()}
     */
    private String definition;

    /**
     * Creates a new {@link BusinessDefinition}.
     * 
     * @param term
     *            The business term.
     * @param definition
     *            The definition for the business term.
     */
    @Constructor
    public BusinessDefinition(
            @ConstructorParameter(propertyName="term") String term,
            @ConstructorParameter(propertyName="definition") String definition) {
        setTerm(term);
        this.definition = definition;
    }

    /**
     * Copy constructor for {@link BusinessDefinition}.
     * 
     * @param busDef
     *            The {@link BusinessDefinition} to copy.
     */
    public BusinessDefinition(BusinessDefinition busDef) {
        this(busDef.getTerm(), busDef.getDefinition());
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        // No operation.
    }

    @NonProperty
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    @NonProperty
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    /**
     * Gets the business term.
     * 
     * @return The {@link String} business term.
     */
    @Accessor(isInteresting=true)
    public String getTerm() {
        return term;
    }

    /**
     * Sets the business term. Also sets the name to be the same as the term.
     * 
     * @param term
     *            The {@link String} business term.
     */
    @Mutator
    public void setTerm(String term) {
        setName(term);
    }
    
    /**
     * Gets the business definition for this term.
     * @return The {@link String} business definition for this term.
     */
    @Accessor(isInteresting=true)
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the business definition for this term.
     * 
     * @param definition
     *            The {@link String} business definition for this term.
     */
    @Mutator
    public void setDefinition(String definition) {
        String oldDefinition = this.definition;
        this.definition = definition;
        firePropertyChange("definition", oldDefinition, definition);
    }

    /**
     * Overridden so that the name and term properties are the same.
     */
    @Override @Mutator
    public void setName(String name) {
        begin("Setting name and term of business definition.");
        super.setName(name);
        
        String oldTerm = this.term;
        this.term = name;
        firePropertyChange("term", oldTerm, name);
        commit();
    }
    
    @Override @Accessor
    public ArchitectProject getParent() {
        return (ArchitectProject) super.getParent();
    }
    
    @Override @Mutator
    public void setParent(SPObject parent) {
        if (parent != null && 
                !ArchitectProject.class.isAssignableFrom(parent.getClass())) {
            throw new IllegalArgumentException("The parent of a " + 
                    BusinessDefinition.class.getSimpleName() + " must be an " + 
                    ArchitectProject.class.getSimpleName() + ".");
        }
        super.setParent(parent);
    }

}
