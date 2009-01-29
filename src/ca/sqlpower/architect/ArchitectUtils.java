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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;

/**
 * Collection of static utility methods for Architect.
 */
public class ArchitectUtils {

	public static final Logger logger = Logger.getLogger(ArchitectUtils.class);

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
	public static List<SQLColumn> findColumnsSourcedFromDatabase(SQLDatabase target, SQLDatabase source) throws SQLObjectException {
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
	 * Recursively count tables in the project, including ones that have not been
	 * expanded in the DBTree.
	 *
	 * @param source the source object (usually the database)
	 */
	public static int countTablesSnapshot(SQLObject so) throws SQLObjectException {
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
	public static <T extends SQLObject>
	List<T> findDescendentsByClass(SQLObject so, java.lang.Class<T> clazz, List<T> addTo)
	throws SQLObjectException {
		if (clazz == so.getClass()) {
			addTo.add(clazz.cast(so));
		} else {
			for (SQLObject child : (List<? extends SQLObject>) so.getChildren()) {
				findDescendentsByClass(child, clazz, addTo);
			}
		}
		return addTo;
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
	public static int countTables(SQLObject so) throws SQLObjectException {
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
     * @throws SQLObjectException 
     */
    public static SQLObject getTableContainer(SQLDatabase db, String catName, String schemaName) throws SQLObjectException {
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
     * @throws SQLObjectException if populating any of the relevant SQLObjects fails.
     */
    public static boolean isCompatibleWithHierarchy(SQLDatabase db, String catalog, String schema, String name) throws SQLObjectException {
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
     * @throws SQLObjectException If you specify catalog or schema for a database that doesn't
     * support catalogs or schemas; also if the database uses catalogs and schemas but you
     * fail to provide them.
     */
    public static SQLTable addSimulatedTable(SQLDatabase db, String catalog, String schema, String name) throws SQLObjectException {
        if (db.getTableByName(catalog, schema, name) != null) {
            throw new SQLObjectException("The table "+catalog+"."+schema+"."+name+" already exists");
        }
        SQLObject schemaContainer;
        if (catalog != null) {
            if (!db.isCatalogContainer()) {
                throw new SQLObjectException("You tried to add a table with a catalog ancestor to a database that doesn't support catalogs.");
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
                throw new SQLObjectException(
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

    /**
     * Finds the nearest common ancestor of all SQLObjects passed in. For
     * example, if a bunch of columns from the same table are passed in, this
     * method will return that table's columns folder. If a bunch of columns
     * from different tables in the same schema are passed in, this method
     * returns the database, catalog, or schema the tables belong to.
     * 
     * @param items The items to find the common ancestor of
     * @return
     */
    public static SQLObject findCommonAncestor(Collection<? extends SQLObject> items) {
        
        // first build up the full ancestory of one randomly chosen item
        List<SQLObject> commonAncestors = ancestorList(items.iterator().next());
        logger.debug("Initial ancestor list: " + commonAncestors);
        
        // now prune the ancestor list to the largest common prefix with each item
        for (SQLObject item : items) {
            List<SQLObject> itemAncestors = ancestorList(item);
            logger.debug("       Comparing with: " + itemAncestors);
            
            Iterator<SQLObject> cit = commonAncestors.iterator();
            Iterator<SQLObject> iit = itemAncestors.iterator();
            while (cit.hasNext() && iit.hasNext()) {
                if (cit.next() != iit.next()) {
                    cit.remove();
                    break;
                }
            }
            
            // remove all remaining items in the common list because they're not in common with this item
            while (cit.hasNext()) {
                cit.next();
                cit.remove();
            }
            logger.debug("     After this prune: " + commonAncestors);
        }
        
        SQLObject commonAncestor = commonAncestors.get(commonAncestors.size() - 1);
        logger.debug("Returning: " + commonAncestor);
        return commonAncestor;
    }
    
    private static List<SQLObject> ancestorList(SQLObject so) {
        List<SQLObject> ancestors = new LinkedList<SQLObject>();
        while (so != null) {
            ancestors.add(0, so);
            so = so.getParent();
        }
        return ancestors;
    }

    /**
     * This will check if the path of the PlDotIni file is valid. If it is not,
     * it will display a message asking the user to either browse for the file
     * or create a new file.
     * 
     * @param plDotIniPath
     *            The path defined to be the location of the pl.ini file.
     * @return The valid pl.ini path. This may be different from the one given
     *         if the user changed it.
     */
    public static String checkForValidPlDotIni(String plDotIniPath, String projectName) throws SQLObjectException {
        while (!isPlDotIniPathValid(plDotIniPath)) {
            String message;
            String[] options = new String[] {"Browse", "Create"};
            if (plDotIniPath == null) {
                message = "location is not set";
            } else if (new File(plDotIniPath).isFile()) {
                message = "file \n\n\""+plDotIniPath+"\"\n\n could not be read";
            } else {
                message = "file \n\n\""+plDotIniPath+"\"\n\n does not exist";
            }
            int choice = JOptionPane.showOptionDialog(null,   // blocking wait
                    "The " + projectName + " keeps its list of database connections" +
                    "\nin a file called PL.INI.  Your PL.INI "+message+"." +
                    "\n\nYou can browse for an existing PL.INI file on your system" +
                    "\nor allow the " + projectName + " to create a new one in your home directory." +
                    "\n\nHint: If you are a Power*Loader Suite user, you should browse for" +
                    "\nan existing PL.INI in your Power*Loader installation directory.",
                    "Missing PL.INI", 0, JOptionPane.INFORMATION_MESSAGE, null, options, null);
            File newPlIniFile;
            if (choice == JOptionPane.CLOSED_OPTION) {
                throw new SQLObjectException("Can't start without a pl.ini file");
            } else if (choice == 0) {
                
                // Don't use recent files menu for default dir here.. we're looking for PL.INI
                JFileChooser fc = new JFileChooser();
                
                fc.setFileFilter(SPSUtils.INI_FILE_FILTER);
                fc.setDialogTitle("Locate your PL.INI file");
                int fcChoice = fc.showOpenDialog(null);       // blocking wait
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    newPlIniFile = fc.getSelectedFile();
                } else {
                    newPlIniFile = null;
                }
            } else if (choice == 1) {
                newPlIniFile = new File(System.getProperty("user.home"), "pl.ini");
            } else
                throw new SQLObjectException("Unexpected return from JOptionPane.showOptionDialog to get pl.ini");

            if (newPlIniFile != null) try {
                newPlIniFile.createNewFile();
                return newPlIniFile.getPath();
            } catch (IOException e1) {
                logger.error("Caught IO exception while creating empty PL.INI at \""
                        +newPlIniFile.getPath()+"\"", e1);
                ASUtils.showExceptionDialogNoReport("Failed to create file \""+newPlIniFile.getPath()+"\".", e1);
            }
        }
        return plDotIniPath;
    }
    
    /**
     * This is a helper method for checkForValidPlDotIni().
     */
    private static boolean isPlDotIniPathValid(String plDotIniPath) {
        logger.debug("Checking pl.ini path: "+plDotIniPath);
        String path = plDotIniPath;
        if (path == null) {
            return false;
        } else {
            File f = new File(path);
            return (f.canRead() && f.isFile());
        }
    }
}