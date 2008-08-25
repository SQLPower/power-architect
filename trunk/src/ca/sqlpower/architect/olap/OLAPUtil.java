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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.InlineTable;
import ca.sqlpower.architect.olap.MondrianModel.Join;
import ca.sqlpower.architect.olap.MondrianModel.RelationOrJoin;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.olap.MondrianModel.View;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;

/**
 * A collection of static utility methods for working with the OLAP classes.
 */
public class OLAPUtil {

    private OLAPUtil() {
        throw new AssertionError("Don't instantiate this class");
    }

    /**
     * Finds the OLAPSession that owns the given OLAPObject, by following
     * parent pointers successively until an ancestor of type OLAPSession
     * is encountered. If there is no OLAPSession ancestor, returns null.
     * <p>
     * Note that when we say <i>ancestor</i> in this doc comment, we include
     * the given <code>oo</code> object as an "ancestor" of itself.  In other
     * words, if you pass in an OLAPSession, you will get that same object back.
     * 
     * @param oo The object to start searching at
     * @return The nearest ancestor of type OLAPObject, or null if there is
     * no such ancestor.
     */
    public static OLAPSession getSession(OLAPObject oo) {
        while (oo != null && !(oo instanceof OLAPSession)) {
            oo = oo.getParent();
        }
        return (OLAPSession) oo;
    }

    /**
     * Mondrian uses a single string as a table name qualifier, so this method
     * helps by finding the parent schema and/or catalog and produces the correct
     * Mondrian qualifier for the given table.
     */
    public static String getQualifier(SQLTable t) {
        SQLSchema schema = t.getSchema();
        SQLCatalog catalog = t.getCatalog();
        if (catalog == null && schema == null) {
            return null;
        } else if (schema == null) {
            return catalog.getName();
        } else if (catalog == null) {
            return schema.getName();
        } else {
            return catalog.getName() + "." + schema.getName();
        }
    }
    
    /**
     * Adds the given OLAPChildListener and optional PropertyChangeListener to
     * the given root object and all of its descendants.
     * 
     * @param ocl
     *            The OLAPChildListener to add to the subtree of OLAPObjects
     *            rooted at root.
     * @param pcl
     *            The PropertyChangeListener to add to the subtree of
     *            OLAPObjects rooted at root. If you don't want to know about
     *            property changes to the nodes themselves, you can leave this
     *            parameter as null.
     */
    public static void listenToHierarchy(OLAPObject root, OLAPChildListener ocl, PropertyChangeListener pcl) {
        root.addChildListener(ocl);
        if (pcl != null) {
            root.addPropertyChangeListener(pcl);
        }
        for (OLAPObject child : root.getChildren()) {
            listenToHierarchy(child, ocl, pcl);
        }
    }

    /**
     * Removes the given OLAPChildListener and optional PropertyChangeListener
     * from the given root object and all of its descendants.
     * 
     * @param ocl
     *            The OLAPChildListener to remove from the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want.
     * @param pcl
     *            The PropertyChangeListener to add to the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want. If you weren't listening for property change events,
     *            you can leave this parameter as null. Note that this parameter
     *            is pronounced "pockle," not "pickle."
     */
    public static void unlistenToHierarchy(OLAPObject root, OLAPChildListener ocl, PropertyChangeListener pcl) {
        root.removeChildListener(ocl);
        if (pcl != null) {
            root.removePropertyChangeListener(pcl);
        }
        for (OLAPObject child : root.getChildren()) {
            unlistenToHierarchy(child, ocl, pcl);
        }
    }
    
    /**
     * {@link OLAPObject#getName()} does not always return the correct name so
     * this method helps by finding the proper name for those exceptions and
     * returns the correct value.
     * 
     * @param obj
     *            The object to find the name of.
     * 
     * @return The name of the given object, null if the object is null itself.
     */
    public static String nameFor(OLAPObject obj) {
        if (obj == null) return null;
        
        if (obj instanceof CubeUsage) {
            return ((CubeUsage) obj).getCubeName();
        } else if (obj instanceof Hierarchy) {
            // Hierarchy default name is name of its parent Dimension
            if (obj.getName() == null && obj.getParent() != null) {
                return obj.getParent().getName();
            } else {
                return obj.getName();
            }
        } else {
            return obj.getName();
        }
    }

    /**
     * Retrieves the SQLTable that represents the data source for the given
     * cube.
     * 
     * @param hierarchy
     *            the hierarchy whose data source to retrieve
     * @return The table whose columns represent the columns of cube's source
     *         table (or view, inline table, or join), or null if the hierarchy
     *         has no table selected.
     * @throws ArchitectException
     *             if populating the necessary SQLObjects fails
     */
    public static SQLTable tableForHierarchy(Hierarchy hier) throws ArchitectException {
        OLAPSession session = getSession(hier);
        SQLDatabase database = session.getDatabase();
        RelationOrJoin relation = hier.getRelation();
        
        // If this hierarchy belongs to a shared dimension, its relation is all we can get.
        // But if this hierarchy belongs to a private dimension, its cube's fact specifies
        // the default table.
        if (relation == null && hier.getParent().getParent() instanceof Cube) {
            Cube owningCube = (Cube) hier.getParent().getParent();
            relation = owningCube.getFact();
        }
        
        if (relation == null) {
            return null;
        } else if (relation instanceof Table) {
            Table table = (Table) relation;
            String qualifier = table.getSchema();
            String name = table.getName();
            if (qualifier == null || qualifier.length() == 0) {
                return (SQLTable) database.getChildByName(name);
            } else if (qualifier.contains(".")) {
                String cat = qualifier.substring(0, qualifier.indexOf('.'));
                String schema = qualifier.substring(qualifier.indexOf('.') + 1);
                return database.getTableByName(cat, schema, name);
            } else if (ArchitectUtils.isCompatibleWithHierarchy(database, qualifier, null, name)) {
                return database.getTableByName(qualifier, null, name);
            } else if (ArchitectUtils.isCompatibleWithHierarchy(database, null, qualifier, name)) {
                return database.getTableByName(null, qualifier, name);
            } else {
                return null;
            }
        } else if (relation instanceof View) {
            throw new UnsupportedOperationException("Views not implemented yet");
        } else if (relation instanceof InlineTable) {
            throw new UnsupportedOperationException("Inline tables not implemented yet");
        } else if (relation instanceof Join) {
            throw new UnsupportedOperationException("Join not implemented yet");
        } else {
            throw new IllegalStateException("Can't produce SQLTable for unknown Relation type " + relation.getClass().getName());
        }
    }

    /**
     * Compiles a list of all accessible tables in the database being used by
     * this OLAP object's session.
     * 
     * @param obj
     *            An object in the session you want the table list for.
     * @return The list of all accessible tables. If the OLAP session doesn't
     *         have a database chosen, an empty list is returned.
     * @throws ArchitectException
     *             If there is a problem populating the list of tables (for example,
     *             the database might not be reachable).
     */
    public static List<SQLTable> getAvailableTables(OLAPObject obj) throws ArchitectException {
        OLAPSession osession = OLAPUtil.getSession(obj);
        SQLDatabase db = osession.getDatabase();
        List<SQLTable> tables;
        if (db != null) {
            tables = ArchitectUtils.findDescendentsByClass(db, SQLTable.class, new ArrayList<SQLTable>());
        } else {
            tables = Collections.emptyList();
        }
        return tables;
    }
    
    /**
     * Finds and returns the Cube that the given CubeUsage references.
     * 
     * @param CubeUsage
     *            The CubeUsage to search by, OLAPSession ancestor must not be
     *            null.
     * 
     * @return The Cube that the CubeUsage references, or null if not found.
     */
    public static Cube findReferencedCube(CubeUsage cu) {
        OLAPSession olapSession = getSession(cu);
        if (olapSession == null) {
            throw new IllegalArgumentException("Can't find OLAPSession ancestor: " + cu);
        }
        return olapSession.findCube(cu.getCubeName());
    } 
    
    /**
     * Finds and returns the base Dimension that the given CubeDimension
     * references. It is safe to call this method with a Dimension as parameter,
     * it will just return the same object.
     * 
     * @param cubeDim
     *            The CubeDimension to search by, OLAPSession ancestor must not
     *            be null.
     * @return The base Dimension that the CubeDimension represents, or null if
     *         not found.
     */
    public static Dimension findReferencedDimension(CubeDimension cubeDim) {
        OLAPSession olapSession = getSession(cubeDim);
        if (olapSession == null) {
            throw new IllegalArgumentException("Can't find OLAPSession ancestor: " + cubeDim);
        }
        
        if (cubeDim instanceof Dimension) {
            return (Dimension) cubeDim;
        } else if (cubeDim instanceof DimensionUsage) {
            String dimName = ((DimensionUsage) cubeDim).getSource();
            return olapSession.findPublicDimension(dimName);
        } else if (cubeDim instanceof VirtualCubeDimension) {
            VirtualCubeDimension vCubeDim = (VirtualCubeDimension) cubeDim;
            if (vCubeDim.getCubeName() == null) {
                return olapSession.findPublicDimension(vCubeDim.getName());
            } else {
                CubeDimension cd = olapSession.findCubeDimension(vCubeDim.getCubeName(), vCubeDim.getName());
                
                if (cd == null) {
                    return null;
                } else if (cd instanceof Dimension) {
                    return (Dimension) cd;
                } else if (cd instanceof DimensionUsage) {
                    DimensionUsage du = (DimensionUsage) cd;
                    return olapSession.findPublicDimension(du.getSource());
                } else {
                    throw new IllegalStateException("Invalid reference by VirtualCubeDimension: " + vCubeDim);
                }
            }
        } else {
            // How do you enter the 4th dimension?!?!? ;)
            throw new UnsupportedOperationException("CubeDimension of type " + cubeDim.getClass() + " not recognized.");
        }
    }
    
    /**
     * Checks if the given name would be unique for the given OLAPObject.
     * 
     * @param oo
     *            The OLAPObject to check, OLAPSession ancestor must not be
     *            null.
     * @param name
     *            The name to check for.
     * @return True if the given object is the only object in its schema with
     *         the given name, false otherwise.
     */
    public static boolean isNameUnique(OLAPObject oo, String name) {
        OLAPSession olapSession = getSession(oo);
        if (olapSession == null) {
            throw new IllegalArgumentException("Can't find OLAPSession ancestor: " + oo);
        }
        
        OLAPObject foundObj;
        if (oo instanceof Cube) {
            foundObj = olapSession.findCube(name);
        } else if (oo instanceof Dimension && oo.getParent() instanceof Schema) {
            foundObj = olapSession.findPublicDimension(name);
        } else if (oo instanceof CubeDimension && oo.getParent() instanceof Cube) {
            foundObj = olapSession.findCubeDimension(oo.getParent().getName(), oo.getName());
        } else {
            return true;
        }
        
        return foundObj == null || foundObj == oo;
    }
}
