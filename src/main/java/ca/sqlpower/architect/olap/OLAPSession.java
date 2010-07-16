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

package ca.sqlpower.architect.olap;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLDatabase;

/**
 * The collection of objects that support editing and use of an OLAP schema.
 * Contains the schema as its one and only child, and belongs to an OLAPRootObject.
 */
public class OLAPSession extends OLAPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.<Class<? extends SPObject>>singletonList(Schema.class);

    /**
     * The database this session's schema uses.
     */
    private SQLDatabase database;
    
    /**
     * The schema that belongs to this session.
     */
    private final Schema schema;
    
    /**
     * Watches over the schema and makes updates to DimensionUsages,
     * VirtualCubeDimensions and CubeUsages as the object they reference makes a
     * name change or gets removed.
     */
    private final SchemaWatcher schemaWatcher;
    
    /**
     * Creates the OLAP Session for the given schema. That schema must
     * not already belong to another session.
     * 
     * @param schema The schema this session owns.
     */
    @Constructor
    public OLAPSession(
            @ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="schema") Schema schema) {
        setName("New Session");
        if (schema.getParent() != null) {
            throw new IllegalStateException(
                    "The given schema already belongs to an OLAP Session");
        }
        schema.setParent(this);
        this.schema = schema;
        schemaWatcher = new SchemaWatcher(schema);
    }

    /**
     * Returns the SQLDatabase this session's schema works with.
     * @return
     */
    @Accessor
    public SQLDatabase getDatabase() {
        return database;
    }

    /**
     * Sets the SQLDatabase this session's schema works with.
     * <p>
     * Note that many parts of the OLAP model refer to tables, columns, views,
     * and so on within one single database connection. Changing which database
     * this session's schema works with is not possible, in general, without a
     * lot of cleanup work. Currently, no effort is made in this method to
     * validate that the new database has the appropriate structure.
     * <p>
     * Similarly, if the tables and columns within this database change over
     * time, the OLAP schema may become invalid, since it will still be
     * referencing the old objects. It would make sense for this session to
     * attach SQLObjectPreEventListeners to the database and all its children,
     * although this is not currently implemented.
     * 
     * @param database
     *            The new database to use with this session's schema.
     */
    @Mutator
    public void setDatabase(SQLDatabase database) {
        SQLDatabase oldDB = this.database;
        this.database = database;
        firePropertyChange("database", oldDB, database);
    }
    
    /**
     * Returns this session's schema.
     */
    @NonProperty
    public Schema getSchema() {
        return schema;
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns a list with exactly one entry: this session's schema.
     */
    @NonProperty
    public List<Schema> getChildren() {
        return Collections.singletonList(schema);
    }
    
    /**
     * Throws an exception, because you can't add or remove children from this
     * type of OLAP Object.
     */
    @Override
    public void addChildImpl(SPObject child, int index) {
        throw new UnsupportedOperationException(
                "OLAPSession has exactly one child (the Schema) for its entire lifetime");
    }
    
    /**
     * Throws an exception, because you can't add or remove children from this
     * type of OLAP Object.
     */
    @Override
    public boolean removeChildImpl(SPObject child) {
        throw new UnsupportedOperationException(
                "OLAPSession has exactly one child (the Schema) for its entire lifetime");
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (Schema.class.equals(childType)) {
            return 0;
        } else {
            throw new IllegalArgumentException("Child type " + childType + 
                    " is not a valid child type of " + OLAPSession.class);
        }
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @Transient @Accessor
    public List<? extends SPObject> getDependencies() {
        return Collections.singletonList(database);
    }

    public void removeDependency(SPObject dependency) {
        getParent().removeOLAPSession(this);
    }
    
    @Override
    @Mutator
    public void setParent(SPObject parent) {
        if (parent != null && !(parent instanceof OLAPRootObject)) {
            throw new IllegalArgumentException("The parent of " + OLAPSession.class + 
                    "s are " + OLAPRootObject.class + "s not " + parent);
        }
        super.setParent(parent);
    }
    
    @Override
    @Accessor
    public OLAPRootObject getParent() {
        return (OLAPRootObject) super.getParent();
    }
}
