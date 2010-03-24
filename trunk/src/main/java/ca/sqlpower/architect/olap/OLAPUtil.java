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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.InlineTable;
import ca.sqlpower.architect.olap.MondrianModel.Join;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.RelationOrJoin;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.architect.olap.MondrianModel.View;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A collection of static utility methods for working with the OLAP classes.
 */
public class OLAPUtil {
    
    private static final Logger logger = Logger.getLogger(OLAPUtil.class);

    private OLAPUtil() {
        throw new AssertionError("Don't instantiate this class");
    }

    /**
     * Finds the OLAPSession that owns the given OLAPObject, by following parent
     * pointers successively until an ancestor of type OLAPSession is
     * encountered. If there is no OLAPSession ancestor, returns null.
     * <p>
     * Note that when we say <i>ancestor</i> in this doc comment, we include
     * the given <code>oo</code> object as an "ancestor" of itself. In other
     * words, if you pass in an OLAPSession, you will get that same object back.
     * 
     * @param oo
     *            The object to start searching at
     * @return The nearest ancestor of type OLAPObject, or null if there is no
     *         such ancestor.
     */
    public static OLAPSession getSession(OLAPObject oo) {
        SPObject o = (SPObject) oo;
        while (o != null && !(o instanceof OLAPSession)) {
            o = o.getParent();
        }
        return (OLAPSession) o;
    }

    /**
     * Mondrian uses a single string as a table name qualifier, so this method
     * helps by finding the parent schema and/or catalog and produces the
     * correct Mondrian qualifier for the given table.
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
     * @param spl
     *            The {@link SPListener} to add to the subtree of OLAPObjects
     *            rooted at root. This will be informed of all child and property
     *            changes. If null no listener will be added.
     * @param cel
     *            The CompoundEditListener to add to the subtree of OLAPObjects
     *            rooted at root. If you don't want to know about compound edit
     *            groupings, you can leave this parameter as null.
     */
    public static void listenToHierarchy(
            OLAPObject root,
            SPListener spl,
            CompoundEditListener cel) {
        if (spl != null) {
            root.addSPListener(spl);
        }
        if (cel != null) {
            root.addCompoundEditListener(cel);
        }
        for (OLAPObject child : root.getChildren(OLAPObject.class)) {
            listenToHierarchy(child, spl, cel);
        }
    }

    /**
     * Removes the given OLAPChildListener and optional PropertyChangeListener
     * from the given root object and all of its descendants.
     * 
     * @param spl
     *            The {@link SPListener} to remove from the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want.
     * @param cel
     *            The CompoundEditListener to add to the subtree of
     *            OLAPObjects rooted at root. It is not an error if the listener
     *            is not registered with any or all of the objects in the
     *            subtree, so it's safe to call this with the root of the tree
     *            if you want. If you weren't listening for compound edit
     *            events, you can leave this parameter as null.
     */
    public static void unlistenToHierarchy(
            OLAPObject root,
            SPListener spl,
            CompoundEditListener cel) {
        root.removeSPListener(spl);
        if (cel != null) {
            root.removeCompoundEditListener(cel);
        }
        for (OLAPObject child : root.getChildren(OLAPObject.class)) {
            unlistenToHierarchy(child, spl, cel);
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
        if (obj == null)
            return null;

        if (obj instanceof CubeUsage) {
            return ((CubeUsage) obj).getCubeName();
        } else if (obj instanceof Hierarchy) {
            // Hierarchy default name is name of its parent Dimension
            if (obj.getName() == null && obj.getParent() != null) {
                return obj.getParent().getName();
            } else {
                return obj.getName();
            }
        } else if (obj.getName() != null && obj.getName().trim().length() > 0) {
            return obj.getName();
        } else {
            return obj.getClass().getSimpleName();
        }
    }

    /**
     * Retrieves the SQLTable that represents the data source for the given
     * hierarchy.
     * 
     * @param hierarchy
     *            the hierarchy whose data source to retrieve
     * @return The table whose columns represent the columns of hierarchy's source
     *         table (or view, inline table, or join), or null if the hierarchy
     *         has no table selected.
     * @throws SQLObjectException
     *             if populating the necessary SQLObjects fails
     */
    public static SQLTable tableForHierarchy(Hierarchy hier) throws SQLObjectException {
        OLAPSession session = getSession(hier);
        SQLDatabase database = session.getDatabase();
        RelationOrJoin relation = hier.getRelation();

        // If this hierarchy belongs to a shared dimension, its relation is all
        // we can get. But if this hierarchy belongs to a private dimension, its cube's fact
        // specifies the default table.
        if (relation == null && hier.getParent().getParent() instanceof Cube) {
            Cube owningCube = (Cube) hier.getParent().getParent();
            relation = owningCube.getFact();
        }

        return tableForRelationOrJoin(database, relation);
    }
    
    /**
     * Retrieves the SQLTable that represents the data source for the given cube.
     * 
     * @param cube The cube whose data source to retrieve.
     * @return The table whose columns represent the columns of cube's source
     *         table (or view, inline table, or join), or null if the cube
     *         has no table set.
     * @throws SQLObjectException
     *             if populating the necessary SQLObjects fails.
     */             
    public static SQLTable tableForCube(Cube cube) throws SQLObjectException {
        OLAPSession session = getSession(cube);
        SQLDatabase database = session.getDatabase();
        
        RelationOrJoin relation = cube.getFact();
        return tableForRelationOrJoin(database, relation);
    }
    
    /**
     * Helper method to find the SQLTable that a RelationOrJoin represents.
     * 
     * @param database The database to search for the table in.
     * @param relationOrJoin The RelationOrJoin identifying the table.
     * @return The SQLTable that the given relation represents, or null if non
     *         found.
     * @throws SQLObjectException
     *             if populating the necessary SQLObjects fails
     */
    private static SQLTable tableForRelationOrJoin(SQLDatabase database, RelationOrJoin relationOrJoin) throws SQLObjectException {
        if (relationOrJoin == null) {
            return null;
        } else if (relationOrJoin instanceof Table) {
            return getSQLTableFromOLAPTable(database, (Table) relationOrJoin);
        } else if (relationOrJoin instanceof View) {
            if (((View) relationOrJoin).getSelects().isEmpty()) {
                return null;
            }
            String sql = ((View) relationOrJoin).getSelects().get(0).getText(); //TODO: Handle having multiple selects in one relation.
            return getSQLTableFromSQLString(database, sql);
        } else if (relationOrJoin instanceof InlineTable) {
            throw new UnsupportedOperationException("Inline tables not implemented yet");
        } else if (relationOrJoin instanceof Join) {
            Join join = (Join) relationOrJoin;
            return getSQLTableFromSQLString(database, generateSQLFromJoin(join));
        } else {
            throw new IllegalStateException("Can't produce SQLTable for unknown Relation type " +
                    relationOrJoin.getClass().getName());
        }
    }

    private static SQLTable getSQLTableFromSQLString(SQLDatabase database, String sql) throws SQLObjectException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = database.getConnection();
            stmt = con.createStatement();
            stmt.setMaxRows(0);
            rs = stmt.executeQuery(sql);
            SQLTable table = new SQLTable(database, true);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                SQLColumn column = new SQLColumn(table, 
                        rsmd.getColumnLabel(i), rsmd.getColumnType(i), 
                        rsmd.getPrecision(i), rsmd.getScale(i));
                table.addColumn(column);
            }
            
            return table;
        } catch (SQLException e) {
            logger.error("Running the statement " + sql + " on the database " + database.getName() + " caused an exception.", e);
            throw new RuntimeException("Creating a view on the database " + database.getName() + " caused an exception.", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("Exception while trying to close a result set used to create a view's table.", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error("Exception while trying to close a statement used to create a view's table.", e);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    logger.error("Exception while trying to close a connection used to create a view's table.", e);
                }
            }
        }
    }

    /**
     * Search the database and retrieve the matching SQLTable(Relational) object
     * thats logically equivalent to the given Table(OLAP representation)
     * 
     * @param table Table object representing a table in a relational database
     * @return a SQLTable in SQLDatabase representing the given table
     * @throws SQLObjectException
     */
    public static SQLTable getSQLTableFromOLAPTable(SQLDatabase database, Table table) throws SQLObjectException {
        String qualifier = table.getSchema();
        String name = table.getName();
        if (qualifier == null || qualifier.length() == 0) {
            return database.getChildByName(name, SQLTable.class);
        } else if (qualifier.contains(".")) {
            String cat = qualifier.substring(0, qualifier.indexOf('.'));
            String schema = qualifier.substring(qualifier.indexOf('.') + 1);
            return database.getTableByName(cat, schema, name);
        } else if (SQLObjectUtils.isCompatibleWithHierarchy(database, qualifier, null, name)) {
            return database.getTableByName(qualifier, null, name);
        } else if (SQLObjectUtils.isCompatibleWithHierarchy(database, null, qualifier, name)) {
            return database.getTableByName(null, qualifier, name);
        } else {
            return null;
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
     * @throws SQLObjectException
     *             If there is a problem populating the list of tables (for
     *             example, the database might not be reachable).
     */
    public static List<SQLTable> getAvailableTables(OLAPObject obj) throws SQLObjectException {
        OLAPSession osession = OLAPUtil.getSession(obj);
        SQLDatabase db = osession.getDatabase();
        List<SQLTable> tables;
        if (db != null) {
            tables = SQLObjectUtils.findDescendentsByClass(db, SQLTable.class, new ArrayList<SQLTable>());
        } else {
            tables = Collections.emptyList();
        }
        return tables;
    }

    /**
     * Finds and returns the Cube that the given CubeUsage references.
     * 
     * @param vCube
     *            Parent of the CubeUsage, used to find OLAPSession ancestor.
     * @param CubeUsage
     *            The CubeUsage to search by.
     * 
     * @return The Cube that the CubeUsage references, or null if not found.
     */
    public static Cube findReferencedCube(VirtualCube vCube, CubeUsage cu) {
        OLAPSession olapSession = getSession(vCube);
        if (olapSession == null) {
            throw new IllegalArgumentException("Can't find OLAPSession ancestor: " + cu);
        }
        for (Cube c : olapSession.getSchema().getCubes()) {
            if (c.getName().equalsIgnoreCase(cu.getCubeName())) {
                return c;
            }
        }
        return null;
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
            for (Dimension d : olapSession.getSchema().getDimensions()) {
                if (d.getName().equalsIgnoreCase(dimName)) {
                    return d;
                }
            }
        } else if (cubeDim instanceof VirtualCubeDimension) {
            VirtualCubeDimension vCubeDim = (VirtualCubeDimension) cubeDim;
            if (vCubeDim.getCubeName() == null) {
                String dimName = vCubeDim.getName();
                for (Dimension d : olapSession.getSchema().getDimensions()) {
                    if (d.getName().equalsIgnoreCase(dimName)) {
                        return d;
                    }
                }
            } else {
                for (Cube c : olapSession.getSchema().getCubes()) {
                    if (c.getName().equalsIgnoreCase(vCubeDim.getCubeName())) {
                        for (CubeDimension cd : c.getDimensions()) {
                            if (cd.getName().equalsIgnoreCase(vCubeDim.getName())) {
                                if (cd instanceof Dimension) {
                                    return (Dimension) cd;
                                } else if (cd instanceof DimensionUsage) {
                                    DimensionUsage du = (DimensionUsage) cd;
                                    return findReferencedDimension(du);
                                } else {
                                    throw new IllegalStateException("Invalid reference by VirtualCubeDimension: " + vCubeDim);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // How do you enter the 4th dimension?!?!? ;)
            throw new UnsupportedOperationException("CubeDimension of type " + cubeDim.getClass() + " not recognized.");
        }
        return null;
    }

    /**
     * Checks if the name is unique for an OLAPObject, relies on
     * {@link OLAPObject#getName()} for name comparisons, case insensitive.
     * 
     * @param parent
     *            The object that will be the parent.
     * @param type
     *            The type of the object.
     * @param name
     *            The name to check for, can be null.
     * @return False if any child of the given type from the parent has the
     *         given name, true otherwise.
     */
    public static boolean isNameUnique(OLAPObject parent, Class<? extends OLAPObject> type, String name) {
        OLAPSession olapSession = getSession(parent);
        if (olapSession == null) {
            throw new IllegalArgumentException("Can't find OLAPSession ancestor: " + parent);
        }

        if (type == Cube.class) {
            for (Cube c : olapSession.getSchema().getCubes()) {
                if (c.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == Dimension.class && parent instanceof Schema) {
            for (Dimension d : ((Schema) parent).getDimensions()) {
                if (d.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == Dimension.class && parent instanceof Cube) {
            for (CubeDimension cd : ((Cube) parent).getDimensions()) {
                if (cd.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == VirtualCube.class) {
            for (VirtualCube vCube : olapSession.getSchema().getVirtualCubes()) {
                if (vCube.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == Measure.class) {
            Cube c = (Cube) parent;
            for (Measure m : c.getMeasures()) {
                if (m.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == Level.class) {
            Hierarchy h = (Hierarchy) parent;
            for (Level l : h.getLevels()) {
                if (l.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == Hierarchy.class) {
            Dimension d = (Dimension) parent;
            for (Hierarchy h : d.getHierarchies()) {
                // null name is the same as having the parent's name.
                if (name == null || name.equalsIgnoreCase(parent.getName())) {
                    if (h.getName() == null || h.getName().equalsIgnoreCase(parent.getName())) {
                        return false;
                    }
                } else if (name.equalsIgnoreCase(h.getName())) {
                    return false;
                }
            }
        } else if (type == CubeUsage.class) {
            VirtualCube vCube = (VirtualCube) parent;
            for (CubeUsage usage : vCube.getCubeUsage().getCubeUsages()) {
                if (usage.getCubeName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        } else if (type == DimensionUsage.class) {
            Cube c = (Cube) parent;
            for (CubeDimension cd : c.getDimensions()) {
                if (cd.getName().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    
    public static String generateSQLFromJoin(Join join) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        
        if (join.getLeft() instanceof Table) {
            Table leftTable = (Table) join.getLeft();
            if (leftTable.getSchema() != null) {
                sql.append(leftTable.getSchema()).append(".");
            }
            sql.append(leftTable.getName());
            sql.append(" ");
            if (leftTable.getAlias() != null) {
                sql.append(leftTable.getAlias());
            }
        } else if (join.getLeft() instanceof Join) {
            sql.append("(");
            sql.append(generateSQLFromJoin((Join)join.getLeft()));
            sql.append(") AS ").append(join.getLeftAlias());
        } else {
            throw new UnsupportedOperationException("Join on a " + join.getLeft().getClass() + " is not supported");
        }
        
        sql.append(" JOIN ");
        
        if (join.getRight() instanceof Table) {
            Table rightTable = (Table) join.getRight();
            if (rightTable.getSchema() != null) {
                sql.append(rightTable.getSchema()).append(".");
            }
            sql.append(rightTable.getName());
            sql.append(" ");
            if (rightTable.getAlias() != null) {
                sql.append(rightTable.getAlias());
            }
        } else if (join.getRight() instanceof Join) {
            sql.append("(");
            sql.append(generateSQLFromJoin((Join)join.getRight()));
            sql.append(") AS ").append(join.getRightAlias());
        } else {
            throw new UnsupportedOperationException("Join on a " + join.getRight().getClass() + " is not supported");
        }
        
        sql.append(" ON ");
        if (join.getLeftAlias() != null) {
            sql.append(join.getLeftAlias()).append(".");
        } else if (join.getLeft() instanceof Table && ((Table)join.getLeft()).getName() != null) {
            sql.append(((Table)join.getLeft()).getName()).append(".");
        }
        sql.append(join.getLeftKey());
        sql.append("=");
        if (join.getRightAlias() != null) {
            sql.append(join.getRightAlias()).append(".");
        } else if (join.getRight() instanceof Table && ((Table)join.getRight()).getName() != null) {
            sql.append(((Table)join.getRight()).getName()).append(".");
        }
        sql.append(join.getRightKey());
        
        return sql.toString();
    }
}
