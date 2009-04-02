/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.dbtree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A transferable implementation that passes a reference to an array of SQLObjects.
 */
public class SQLObjectSelection implements Transferable {

    /**
     * Data flavour that indicates a JVM-local reference to an array containing
     * 0 or more SQLObjects.
     */
    public static final DataFlavor LOCAL_SQLOBJECT_ARRAY_FLAVOUR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Lca.sqlpower.sqlobject.SQLObject;\"", "Local Array of SQLObject");
    
    /**
     * Data flavour that indicates a reference to a string array containing a string
     * representation of 0 or more SQLObjects.
     */
    public static final DataFlavor SQLOBJECT_NAMES = new DataFlavor(String[].class, "List of selected object names");
    
    /**
     * Data flavour that indicates a reference to a string containing the names
     * of SQLObjects separated by a newline character.
     */
    public static final DataFlavor MULTILINE_SQLOBJECT_NAMES = new DataFlavor(String.class, "List of selected object names as one string separated by newlines");
    
    /**
     * These are the actual SQLObjects we're transfering.
     */
    private final SQLObject[] sqlObjects;
    
    /**
     * These are the names of the SQLObjects we're transfering.
     */
    private final String[] sqlObjectNames;
    
    /**
     * Thesea re the names of the SQLObjects we're transfering separated by newline characters.
     */
    private final String sqlObjectNamesNewlineSeparated;
    
    /**
     * Tracks if this SQLObject is still local to the JVM it was created in. If
     * this transferable is sent to the system's clipboard then it is no longer
     * local and the SQLObject array cannot be used to copy or paste into
     * other applications.
     * <p>
     * If another type of transferable is made this may need to be moved to an
     * abstract class that is the parent of each transferable to make managing this
     * simpler.
     */
    private boolean isLocal = true;

    /**
     * Creates a new transferable for the given collection of SQLObjects.
     * 
     * @param sqlObjects
     *            converted to an array in the order the elements are returned
     *            by the iterator.
     */
    public SQLObjectSelection(Collection<SQLObject> sqlObjects) {
        this(sqlObjects.toArray(new SQLObject[sqlObjects.size()]));
    }

    /**
     * Creates a new transferable for the given array of SQLObjects.
     * 
     * @param sqlObjects
     *            the exact array that will be returned by
     *            {@link #getTransferData(DataFlavor)}. Must not be null, but
     *            can have a length of 0.
     */
    public SQLObjectSelection(SQLObject[] sqlObjects) {
        if (sqlObjects == null) {
            throw new NullPointerException("Can't transfer a null array. Try an empty one instead!");
        }
        this.sqlObjects = sqlObjects;
        sqlObjectNames = getObjectNames(sqlObjects);
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (SQLObject o : sqlObjects) {
            if (!first) sb.append("\n");
            sb.append(getObjectName(o));
            first = false;
        }
        sqlObjectNamesNewlineSeparated = sb.toString();
    }
    
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (flavor == LOCAL_SQLOBJECT_ARRAY_FLAVOUR) {
            return sqlObjects;
        } else if (flavor == SQLOBJECT_NAMES) {
            return sqlObjectNames;
        } else if (flavor == MULTILINE_SQLOBJECT_NAMES) {
            return sqlObjectNamesNewlineSeparated;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        if (isLocal) {
            return new DataFlavor[] { LOCAL_SQLOBJECT_ARRAY_FLAVOUR, SQLOBJECT_NAMES, MULTILINE_SQLOBJECT_NAMES };
        } else {
            return new DataFlavor[] {SQLOBJECT_NAMES, MULTILINE_SQLOBJECT_NAMES };
        }
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ((isLocal && flavor == LOCAL_SQLOBJECT_ARRAY_FLAVOUR) || flavor == SQLOBJECT_NAMES || flavor == MULTILINE_SQLOBJECT_NAMES);
    }
    
    /**
     * Takes the tree paths to store the names of the SQLObjects into an ArrayList which will be used as a flavor.
     */
     private String[] getObjectNames(SQLObject[] objects){
         String[] nodeNames = new String[objects.length];
         for (int i = 0; i < objects.length; i++) {
             final SQLObject object = objects[i];
             nodeNames[i] = getObjectName(object);
         }
         return nodeNames;
     }

     /**
      * Takes the tree paths to store the name of a SQLObject which will be used in a flavor.
      */
    private String getObjectName(final SQLObject object) {
        String nodeName;
        if (object instanceof SQLTable) {
            nodeName = ((SQLTable) object).toQualifiedName();
        } else if (object instanceof SQLColumn) {
            nodeName = ((SQLColumn) object).getParentTable().getName() + "." + ((SQLColumn) object).getName();
        } else {
            nodeName = object.getName();
        }
        return nodeName;
    }
     
     @Override
    public String toString() {
        return Arrays.toString(sqlObjectNames);
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }
}
