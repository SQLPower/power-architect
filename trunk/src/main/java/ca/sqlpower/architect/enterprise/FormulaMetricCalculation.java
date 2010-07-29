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

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;

/**
 * This {@link SPObject} represents a formula or metric calculation.
 */
public class FormulaMetricCalculation extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.emptyList();
    
    /**
     * {@link #getFormula()}
     */
    private String formula;
    
    /**
     * {@link #getDescription()}
     */
    private String description;

    /**
     * Creates a new formula or metric calculation.
     * 
     * @param name
     *            The name of the formula. This value cannot be null.
     * @param formula
     *            The formula. This value cannot be null.
     * @param description
     *            The description of the formula. This value cannot be null.
     */
    @Constructor
    public FormulaMetricCalculation(
            @ConstructorParameter(propertyName="name") String name,
            @ConstructorParameter(propertyName="formula") String formula,
            @ConstructorParameter(propertyName="description") String description) {
        setName(name);
        this.formula = formula;
        this.description = description;
    }

    /**
     * Copy constructor.
     * 
     * @param formula
     *            The {@link FormulaMetricCalculation} to copy.
     */
    public FormulaMetricCalculation(FormulaMetricCalculation formula) {
        this(formula.getName(), formula.getFormula(), formula.getDescription());
    }

    @Override
    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void removeDependency(SPObject dependency) {
        // No operation.
    }

    @Override
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    /**
     * Gets the formula.
     * @return The {@link String} formula.
     */
    @Accessor(isInteresting=true)
    public String getFormula() {
        return formula;
    }

    /**
     * Sets the formula.
     * 
     * @param formula
     *            The {@link String} formula. This value cannot be null.
     */
    @Mutator
    public void setFormula(String formula) {
        String oldFormula = this.formula;
        this.formula = formula;
        firePropertyChange("formula", oldFormula, formula);
    }

    /**
     * Gets the description of this formula.
     * 
     * @return The description of the formula.
     */
    @Accessor(isInteresting=true)
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this formula.
     * 
     * @param description
     *            The {@link String} description of the formula.
     */
    @Mutator
    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        firePropertyChange("description", oldDescription, description);
    }

}
