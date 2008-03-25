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
package ca.sqlpower.architect;

import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.util.ExceptionReport;

/**
 * Collection of static utility methods for Architect.
 */
public class ArchitectUtils {

	private static final Logger logger = Logger.getLogger(ArchitectUtils.class);

	/**
	 * This class is just a container for utility routines; you do not
	 * need to instantiate it.
	 */
	private ArchitectUtils() {
		// never gets called
	}

    /**
     * Performs startup tasks for the architect system.  You should call
     * this when starting the Architect.
     */
    public static void startup() {
        ExceptionReport.init();
    }

	/**
	 * Sets up the log4j logging framework.
	 */
	public static void configureLog4j() {
		String configName = "log4j.properties";
		URL config = ArchitectUtils.class.getClassLoader().getResource(configName);
		if (config == null) {
			// It is probably not a good idea to change this to use log4j logging...
			System.err.println("Warning: Couldn't find log4j config resource '"+configName+"'");
		} else {
			org.apache.log4j.PropertyConfigurator.configure(config);
			logger.info("Log4J configured successfully");
		}
	}

	/**
	 * Adds listener to source's listener list and all of source's
	 * children's listener lists recursively.
	 */
	public static void listenToHierarchy(SQLObjectListener listener, SQLObject source)
	throws ArchitectException {
		if (logger.isDebugEnabled()) logger.debug("Listening to new SQL Object "+source);
		source.addSQLObjectListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
			Iterator it = source.getChildren().iterator();
			while (it.hasNext()) {
				listenToHierarchy(listener, (SQLObject) it.next());
			}
		}

	}

	/**
	 * Adds listener to source's listener list and all of source's
	 * children's listener lists recursively.
	 */
	public static void addUndoListenerToHierarchy(UndoCompoundEventListener listener, SQLObject source)
	throws ArchitectException {
        if (logger.isDebugEnabled()) logger.debug("Undo Listening to new SQL Object "+source);
		source.addUndoEventListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
			Iterator it = source.getChildren().iterator();
			while (it.hasNext()) {
				addUndoListenerToHierarchy(listener, (SQLObject) it.next());
			}
		}

	}

	/**
	 * Calls listenToHierarchy on each element in the sources array.
	 * Does nothing if sources is null.
	 */
	public static void addUndoListenerToHierarchy(UndoCompoundEventListener listener, SQLObject[] sources)
	throws ArchitectException {
		if (sources == null) return;
		for (int i = 0; i < sources.length; i++) {
			addUndoListenerToHierarchy(listener, sources[i]);
		}
	}

	/**
	 * Calls listenToHierarchy on each element in the sources array.
	 * Does nothing if sources is null.
	 */
	public static void listenToHierarchy(SQLObjectListener listener, SQLObject[] sources)
	throws ArchitectException {
		if (sources == null) return;
		for (int i = 0; i < sources.length; i++) {
			listenToHierarchy(listener, sources[i]);
		}
	}

	/**
	 * Removes listener from source's listener list and all of source's
	 * children's listener lists recursively.
	 */
	public static void undoUnlistenToHierarchy(UndoCompoundEventListener listener, SQLObject source)
	throws ArchitectException {
        if (logger.isDebugEnabled()) logger.debug("Unlistening to SQL Object "+source);
		source.removeUndoEventListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
			Iterator it = source.getChildren().iterator();
			while (it.hasNext()) {
				SQLObject ob = (SQLObject) it.next();
				undoUnlistenToHierarchy(listener, ob);
			}
		}
	}

	/**
	 * Calls unlistenToHierarchy on each element in the sources array.
	 * Does nothing if sources is null.
	 */
	public static void undoUnlistenToHierarchy(UndoCompoundEventListener listener, SQLObject[] sources)
	throws ArchitectException {
		if (sources == null) return;
		for (int i = 0; i < sources.length; i++) {
			undoUnlistenToHierarchy(listener, sources[i]);
		}
	}

	/**
	 * Removes listener from source's listener list and all of source's
	 * children's listener lists recursively.
	 */
	public static void unlistenToHierarchy(SQLObjectListener listener, SQLObject source)
	throws ArchitectException {
        if (logger.isDebugEnabled()) logger.debug("Removing "+listener+" from listener list of "+source);
		source.removeSQLObjectListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
            if (logger.isDebugEnabled()) logger.debug("        Now removing for children: "+source.getChildren());
			Iterator it = source.getChildren().iterator();
			while (it.hasNext()) {
				SQLObject ob = (SQLObject) it.next();
				unlistenToHierarchy(listener, ob);
			}
		}
	}

	/**
	 * Calls unlistenToHierarchy on each element in the sources array.
	 * Does nothing if sources is null.
	 */
	public static void unlistenToHierarchy(SQLObjectListener listener, SQLObject[] sources)
	throws ArchitectException {
		if (sources == null) return;
		for (int i = 0; i < sources.length; i++) {
			unlistenToHierarchy(listener, sources[i]);
		}
	}

	/**
	 * Does a generic object comparison where one or both of the objects could
	 * be null.  If both objects are null, they are considered equal; if only
	 * one is null, they are not equal; otherwise they are compared using
	 * <code>o1.equals(o2)</code>.
	 */
	public static boolean areEqual(Object o1, Object o2) {
		if (o1 == o2) return true;
		else if (o1 == null || o2 == null) return false;
		else return o1.equals(o2);
	}

	/**
	 * Searches for all columns in the target database which are marked as having
	 * source columns in the given source database.
	 *
	 * @param target The database to search.  All columns of all tables in this database are searched.
	 * @param source The database to look for in the target database's columns.
	 * @return A list of all columns in the target database whose source database is the same
	 * as the given source object. Every item in the list will be of type SQLColumn.
	 */
	public static List<SQLColumn> findColumnsSourcedFromDatabase(SQLDatabase target, SQLDatabase source) throws ArchitectException {
		if (logger.isDebugEnabled()) logger.debug("Searching for dependencies on "+source+" in "+target);
		List matches = new ArrayList();
		Iterator it = target.getChildren().iterator();
		while (it.hasNext()) {
			SQLObject so = (SQLObject) it.next();
			if (logger.isDebugEnabled()) logger.debug("-->Next target item is "+so.getClass().getName()+": "+so+" ("+so.getChildCount()+" children)");
			if (so instanceof SQLTable) {
				SQLTable t = (SQLTable) so;
				Iterator cit = t.getColumns().iterator();
				while (cit.hasNext()) {
					Object next = cit.next();
					if (logger.isDebugEnabled()) logger.debug("---->Next item in columns list is a "+next.getClass().getName());
					SQLColumn col = (SQLColumn) next;
					if (col.getSourceColumn() != null && source.equals(col.getSourceColumn().getParentTable().getParentDatabase())) {
						matches.add(col);
					}
				}
			}
		}
		return matches;
	}

	/**
	 * Recursively poke the SQLDatabase until we find at least one leaf node
	 * (SQLColumn).
	 *
	 * @param source the source object (usually the database)
	 */
	public static boolean pokeDatabase(SQLObject source) throws ArchitectException {
		if (logger.isDebugEnabled()) logger.debug("HELLO my class is " + source.getClass().getName() + ", my name is + " + source.getName());
		if (source.allowsChildren()) {
			int j = 0;
			boolean done = false;
			int childCount;
			try {
			    childCount = source.getChildCount();
			} catch (ArchitectException e) {
			    // FIXME: this behaviour should be an optional part of SQLObject's populate() method, not something client code has to do
			    source.addChild(new SQLExceptionNode(e, "Error during initial database probe"));
			    childCount = 1;
			}
			while (!done && j < childCount) {
				done = pokeDatabase(source.getChild(j));
				j++;
			}
			return done;
		} else {
			return true; // found a leaf node
		}
	}

	/**
	 * Recursively count tables in the project, including ones that have not been
	 * expanded in the DBTree.
	 *
	 * @param source the source object (usually the database)
	 */
	public static int countTablesSnapshot(SQLObject so) throws ArchitectException {
		if (so instanceof SQLTable) {
			return 1;
		} else {
			int count = 0;
			Iterator it = so.getChildren().iterator();
			while (it.hasNext()) {
				count += countTablesSnapshot((SQLObject) it.next());
			}
		    return count;
		}
	}

	/**
	 * Keep in mind that if you go after anything lower than
	 * SQLTable, you will invoke a potentially expensive
	 * populate() method multiple times.
	 *
	 * @param source the source object (usually the database)
	 */
	public static List findDescendentsByClass(SQLObject so, java.lang.Class clazz, List list) throws ArchitectException {
		if (clazz.isAssignableFrom(so.getClass())) {
			list.add(so);
		} else {
			Iterator it = so.getChildren().iterator();
			while (it.hasNext()) {
				findDescendentsByClass((SQLObject) it.next(), clazz, list);
			}
		}
		return list;
	}


	/**
	 * Chop long strings down to size for display purposes
	 *
	 * @param s the input string
	 * @return the truncated string
	 */
	public static String truncateString (String s) {
		if (s == null || s.length() < 28) {
			return s;
		} else {
			return s.substring(27) + "...";
		}
	}

	/**
	 * Recursively count tables in the project, but only consider tables that
	 * have been expanded.
	 *
	 * This might be undercounting a little bit because I think this suppresses
	 * the Target Database (playpen) entries.
	 *
	 * @param source the source object (usually the database)
	 */
	public static int countTables(SQLObject so) throws ArchitectException {
		if (so instanceof SQLTable) {
			return 1;
		} else if ( (!so.allowsChildren()) || !(so.isPopulated()) || so.getChildren() == null) {
		    return 0;
		} else {
			int myCount = 0;
			Iterator it = so.getChildren().iterator();
			while (it.hasNext()) {
				myCount += countTables((SQLObject) it.next());
			}
			return myCount;
		}
	}

	/**
     * Double quotes input string for CSV if needed:
     * string contains ", \n, \r, ','
     */
    public static String quoteCSV(Object val) {
        if ( val == null ) {
            return "";
        } else if ( val instanceof String ) {
            return quoteCSVStr((String) val);
        }
        return val.toString();
    }
    public static String quoteCSVStr(String val) {
        CharSequence doubleQuote = "\"".subSequence(0,"\"".length());
        CharSequence doubleDoubleQuote = "\"\"".subSequence(0,"\"\"".length());
        CharSequence newLine = "\n".subSequence(0,"\n".length());
        CharSequence cr = "\r".subSequence(0,"\r".length());
        CharSequence comma = ",".subSequence(0,",".length());

        if ( val.contains(doubleQuote) ) {
            StringBuffer sb = new StringBuffer(doubleQuote);
            sb.append(val.replace(doubleQuote,doubleDoubleQuote));
            sb.append(doubleQuote);
            return sb.toString();
        } else if ( val.contains(newLine) ||
                val.contains(cr) ||
                val.contains(comma) ) {
            StringBuffer sb = new StringBuffer(doubleQuote);
            sb.append(val);
            sb.append(doubleQuote);
            return sb.toString();
        }
        return val;
    }
      /**
     * Pulls out all SQLTable objects which exist under the start object
     * and adds them to the given list.
     *
     * TODO make this generic so we can ask for any object type
     *
     * @param db The database to extract the tables from
     * @return A flat List consisting of all the SQLTable objects in db.
     * @throws ArchitectException
     */
    public static List<SQLTable> extractTables(SQLObject start, List addTo) throws ArchitectException {
        if (start.allowsChildren()) {
            Iterator it = start.getChildren().iterator();
            while (it.hasNext()) {
                SQLObject so = (SQLObject) it.next();
                if (so instanceof SQLTable) addTo.add(so);
                else extractTables(so, addTo);
            }
        }
        return addTo;
    }

    /**
     * Returns the parent database of <tt>so</tt> or <tt>null</tt> if <tt>so</tt>
     * doesn't have an ancestor whose class is <tt>ancestorType</tt>.
     *
     * @param so The object for whose ancestor to look. (Thanks, Winston).
     * @return The nearest ancestor of type ancestorType, or null if no such ancestor exists.
     */
    public static <T extends SQLObject> T getAncestor(SQLObject so, Class<T> ancestorType) {
        while (so != null) {
            if (so.getClass().equals(ancestorType)) return (T) so;
            so = so.getParent();
        }
        return null;
    }

    /**
     * returns the tables under the given SQLObject
     *
     */

    public static Collection<SQLTable> tablesUnder(SQLObject so) throws ArchitectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        if (so instanceof SQLTable) {
            tables.add((SQLTable) so);
        } else if (so.allowsChildren()) {
            for (SQLObject child : (List<SQLObject>) so.getChildren()) {
                tables.addAll(tablesUnder(child));
            }
        }
        return tables;
    }

    /**
     * Returns the object that contains tables in the given database.
     * Depending on platform, this could be a SQLDatabase, a SQLCatalog,
     * or a SQLSchema.  A null catName or schemName argument means that
     * catalogs or schemas are not present in the given database.
     * <p>
     * Note, all comparisons are done case-insensitively.
     * 
     * @param db The database to retrieve the table container from.
     * @param catName The name of the catalog to retrieve.  Must be null iff the
     * database does not have catalogs.
     * @param schemaName The name of the schema to retrieve.  Must be null iff the
     * database does not have schemas.
     * @return The appropriate SQLObject under db that is a parent of SQLTable objects,
     * given the catalog and schema name arguments.
     * @throws ArchitectException 
     */
    public static SQLObject getTableContainer(SQLDatabase db, String catName, String schemaName) throws ArchitectException {
        db.populate();
        logger.debug("Looking for catalog="+catName+", schema="+schemaName+" in db "+db);
        if (db.getChildType() == SQLTable.class) {
            if (catName != null || schemaName != null) {
                throw new IllegalArgumentException("Catalog or Schema name was given but neither is necessary.");
            }
            return db;
        } else if (db.getChildType() == SQLSchema.class) {
           if (catName != null) {
               throw new IllegalArgumentException("Catalog name was given but is not necessary.");
           }
           if (schemaName == null) {
               throw new IllegalArgumentException("Schema name was expected but none was given.");
           }
           
           return (SQLSchema) db.getChildByNameIgnoreCase(schemaName);
        } else if (db.getChildType() == SQLCatalog.class) {
            if (catName == null) {
                throw new IllegalArgumentException("Catalog name was expected but none was given.");
            }
            SQLCatalog tempCat = db.getCatalogByName(catName);
            
            if (tempCat == null) return null;
            
            tempCat.populate();
            
            logger.debug("Found catalog "+catName+". Child Type="+tempCat.getChildType());
            if (tempCat.getChildType() == SQLSchema.class) {
                if (schemaName == null) {
                    throw new IllegalArgumentException("Schema name was expected but none was given.");
                }
                
                return (SQLSchema) tempCat.getChildByNameIgnoreCase(schemaName);
            }
            
            if (schemaName != null) {
                throw new IllegalArgumentException("Schema name was given but is not necessary.");
            }
            
            return tempCat;
        } else if (db.getChildType() == null) {
            // special case: there are no children of db
            logger.debug("Database "+db+" has no children");
            return null;
        } else {
            throw new IllegalStateException("Unknown database child type: " + db.getChildType());
        }
    }

    /**
     * Returns true if and only if the given set of arguments would result in a
     * successful call to {@link #addSimulatedTable(SQLDatabase, String, String, String)}.
     * See that method's documentation for the meaning of the arguments.
     * 
     * @throws ArchitectException if populating any of the relevant SQLObjects fails.
     */
    public static boolean isCompatibleWithHierarchy(SQLDatabase db, String catalog, String schema, String name) throws ArchitectException {
        SQLObject schemaContainer;
        if ( catalog != null){
            if (db.isCatalogContainer()){
                schemaContainer = db.getCatalogByName(catalog);
                if (schemaContainer == null) {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            schemaContainer = db;
        }

        SQLObject tableContainer;
        if (schema != null){
            if (schemaContainer.getChildType() == SQLSchema.class){
                tableContainer = schemaContainer.getChildByName(schema);
                if (tableContainer == null) {
                    return true;
                }
            } else if (schemaContainer.getChildType() == null) {
                return true;
            } else {
                return false;
            }
        } else {
            tableContainer = schemaContainer;
        }

        if (name != null) {
            if (tableContainer.getChildType() == null || tableContainer.getChildType() == SQLTable.class){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Creates a SQLTable in the given database, optionally under a catalog and/or schema.
     * 
     * @param db The database to create the table in.
     * @param catalog The catalog that the table (or the table's schema) should be in.
     * If null, it is assumed the given database doesn't have catalogs.
     * @param schema The schema that the table should be in.  If null, it is assumed the
     * given database doesn't have schemas.
     * @param name The name of the table to create.
     * @return The table that was created
     * @throws ArchitectException If you specify catalog or schema for a database that doesn't
     * support catalogs or schemas; also if the database uses catalogs and schemas but you
     * fail to provide them.
     */
    public static SQLTable addSimulatedTable(SQLDatabase db, String catalog, String schema, String name) throws ArchitectException {
        if (db.getTableByName(catalog, schema, name) != null) {
            throw new ArchitectException("The table "+catalog+"."+schema+"."+name+" already exists");
        }
        SQLObject schemaContainer;
        if (catalog != null) {
            if (!db.isCatalogContainer()) {
                throw new ArchitectException("You tried to add a table with a catalog ancestor to a database that doesn't support catalogs.");
            }
            schemaContainer = db.getCatalogByName(catalog);
            if (schemaContainer == null) {
                schemaContainer = new SQLCatalog(db, catalog, true);
                db.addChild(schemaContainer);
            }
        } else {
            schemaContainer = db;
        }

        SQLObject tableContainer;
        if (schema != null) {
            Class<? extends SQLObject> childType = schemaContainer.getChildType();
            if ( !(childType == null || childType == SQLSchema.class) ) {
                throw new ArchitectException(
                        "The schema container ("+schemaContainer+
                        ") can't actually contain children of type SQLSchema.");
            }
            tableContainer = schemaContainer.getChildByName(schema);
            if (tableContainer == null) {
                tableContainer = new SQLSchema(schemaContainer, schema, true);
                schemaContainer.addChild(tableContainer);
            }
        } else {
            tableContainer = schemaContainer;
        }

        SQLTable newTable = new SQLTable(tableContainer, name, null, "TABLE", true);
        tableContainer.addChild(newTable);

        return newTable;
    }

    /**
     * Creates a dot-separated string of the name of the given SQLObject and the
     * names of each of its ancestors.  The top-level ancestor's name will be the
     * first name to appear in the string, and the given object's name will be
     * the last.
     */
    public static String toQualifiedName(SQLObject obj) {
        List<SQLObject> ancestors = new ArrayList<SQLObject>();
        while (obj != null) {
            ancestors.add(obj);
            obj = obj.getParent();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            sb.append(ancestors.get(i).getName());
            if (i != 0) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
    
    /**
     * Checks if the definitions of two columns are materially different.
     * Some data types (for example, DECIMAL and NUMERIC) are essentially
     * the same.  Also, the precision and scale values on DATE columns are
     * not of much consequence, but different databases report different
     * values.
     * 
     * @param targetColumn One of the columns to compare. Must not be null.
     * @param sourceColumn One of the columns to compare. Must not be null.
     * @return True iff the source and target columns are materially different
     * (as in, they are unlikely to be able to hold the same set of data as
     * each other)
     */
    public static boolean columnsDiffer(SQLColumn targetColumn, SQLColumn sourceColumn) {

        // eliminate meaningless type differences
        int targetType = compressType(targetColumn.getType());
        int sourceType = compressType(sourceColumn.getType());

        int targetPrecision = targetColumn.getPrecision();
        int sourcePrecision = sourceColumn.getPrecision();
        
        int targetScale = targetColumn.getScale();
        int sourceScale = sourceColumn.getScale();

        if (targetType == Types.DATE) {
            targetPrecision = 0;
            targetScale = 0;
        } else if (targetType == Types.INTEGER) {
            targetPrecision = 0;
            targetScale = 0;
        }

        if (sourceType == Types.DATE) {
            sourcePrecision = 0;
            sourceScale = 0;
        } else if (sourceType == Types.INTEGER) {
            sourcePrecision = 0;
            sourceScale = 0;
        }

        return (sourceType != targetType)
            || (targetPrecision != sourcePrecision)
            || (targetScale != sourceScale)
            || (targetColumn.getNullable() != sourceColumn.getNullable());
    }
    
    /**
     * Checks if the given column types materially differ. Some data 
     * types (for example, DECIMAL and NUMERIC) are essentially the same.
     * 
     * @param t1 One of the column types to compare
     * @param t2 One of the column types to compare.
     * @return True iff the given column types are materially different
     */
    public static boolean columnTypesDiffer(int t1, int t2) {
        int sourceType = compressType(t1);
        int targetType = compressType(t2);
        return sourceType != targetType;
    }
    
    /**
     * Compresses all the different kinds of essentially identical types
     * into an arbitrarily chosen one of them.  For instance, NUMERIC
     * and DECIMAL both compress to NUMERIC.
     * 
     * @param type
     * @return
     */
    private static int compressType(int type) {
        if (type == Types.DECIMAL) {
            return Types.NUMERIC;
        } else {
            return type;
        }
    }
}