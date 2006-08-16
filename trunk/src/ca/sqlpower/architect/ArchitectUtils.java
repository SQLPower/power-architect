package ca.sqlpower.architect;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DB2DDLGenerator;
import ca.sqlpower.architect.ddl.OracleDDLGenerator;
import ca.sqlpower.architect.ddl.PostgresDDLGenerator;
import ca.sqlpower.architect.ddl.SQLServerDDLGenerator;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

/**
 * Collection of static utility methods for Architect.
 */
public class ArchitectUtils {
	
	private static final Logger logger = Logger.getLogger(ArchitectUtils.class);
    public static final String APP_VERSION_MAJOR = "1";
    public static final String APP_VERSION_MINOR = "0";
    public static final String APP_VERSION_TINY = "20";
    public static final String APP_VERSION = APP_VERSION_MAJOR+"."+
                                            APP_VERSION_MINOR+"." +
                                            APP_VERSION_TINY;
    /**
     * The System.currentTimeMillis when this class was loaded.
     */
    private static final long startupTimeMillis = System.currentTimeMillis();
	
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
        // By virtue of referencing this class, calling this method early in the app's
        // startup sequence will cause the startupTimeMillis to init itself.
        
        // there's nothing else to do, really.
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
		logger.debug("Listening to new SQL Object "+source);
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
		logger.debug("Undo Listening to new SQL Object "+source);
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
		logger.debug("Unlistening to SQL Object "+source);
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
		logger.debug("Removing "+listener+" from listener list of "+source);
		source.removeSQLObjectListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
			logger.debug("        Now removing for children: "+source.getChildren());
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
	public static List findColumnsSourcedFromDatabase(SQLDatabase target, SQLDatabase source) throws ArchitectException {
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
					if (source.equals(col.getSourceColumn().getParentTable().getParentDatabase())) {
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
	 * Map from JDBC Driver Class name to the template used to create database URLs
	 * XXX: look thise up from somewhere (i.e. don't hard code them)
	 */
	public static Map<String,String> getDriverTemplateMap() {
		Map<String,String> drivers = new HashMap<String,String>();
		drivers.put("oracle.jdbc.driver.OracleDriver",
					"jdbc:oracle:thin:@<Hostname>:<Port:1521>:<Instance>");
		drivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
					"jdbc:microsoft:sqlserver://<Hostname>:<Port:1433>;SelectMethod=cursor");
		drivers.put("org.postgresql.Driver",
					"jdbc:postgresql://<Hostname>:<Port:5432>/<Database>");
		drivers.put("ibm.sql.DB2Driver",
					"jdbc:db2:<Hostname>");
        drivers.put("org.apache.derby.jdbc.EmbeddedDriver",
                    "jdbc:derby:<Database>;create=true");
        drivers.put("org.hsqldb.jdbcDriver", 
                    "jdbc:hsqldb:<Database>");
		drivers.put("ca.sqlpower.architect.MockJDBCDriver",
					"jdbc:mock:catalogTerm=<Catalog Term:Catalog>&schemaTerm=<Schema Term:Schema>");
		return drivers;
	}
	
	/**
	 * Map from driver class name to a short name for the database.
	 * XXX: look thise up from somewhere (i.e. don't hard code them)
	 */
	public static Map<String,String> getDriverTypeMap() {
		Map<String,String> driverSystems = new HashMap<String,String>();
		driverSystems.put("oracle.jdbc.driver.OracleDriver", "ORACLE");
		driverSystems.put("com.microsoft.jdbc.sqlserver.SQLServerDriver", "SQL SERVER");
		driverSystems.put("org.postgresql.Driver", "POSTGRES");
		driverSystems.put("ibm.sql.DB2Driver", "DB2");
        driverSystems.put("org.apache.derby.jdbc.EmbeddedDriver", "DERBY");
        driverSystems.put("org.hsqldb.jdbcDriver", "HSQLDB");
		driverSystems.put("ca.sqlpower.architect.MockJDBCDriver", "OTHER");
		return driverSystems;
	}
	
	/**
	 * 
	 * XXX: look thise up from somewhere (i.e. don't hard code them)
	 * 
	 */
	public static Map getDriverDDLGeneratorMap () {
		Map drivers = new HashMap();
		drivers.put("oracle.jdbc.driver.OracleDriver",
				OracleDDLGenerator.class);
		drivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
				SQLServerDDLGenerator.class);
		drivers.put("org.postgresql.Driver",
				PostgresDDLGenerator.class);
		drivers.put("ibm.sql.DB2Driver",
				DB2DDLGenerator.class);
		return drivers;		
	}
	
	/**
	 * Replaces double quotes, ampersands, and less-than signs with
	 * their character reference equivalents.  This makes the returned
	 * string be safe for use as an XML content data or as an attribute value 
	 * enclosed in double quotes. From the XML Spec at http://www.w3.org/TR/REC-xml/#sec-predefined-ent:
	 * 4.6 Predefined Entities
	 * "Definition: Entity and character references may both be used to escape the left angle bracket, ampersand, 
	 * and other delimiters. A set of general entities (amp, lt, gt, apos, quot) is specified for this purpose...]
	 * All XML processors must recognize these entities whether they are declared or not. For interoperability, 
	 * valid XML documents should declare these entities..."
	 */
	public static String escapeXML(String src) {
        if (src == null) return "";
		StringBuffer sb = new StringBuffer(src.length()+10);  // arbitrary amount of extra space
		char ch;
		for (int i = 0, n = src.length(); i < n; i++) {
			switch (ch = src.charAt(i)) {
			case '\'':
				sb.append("&apos;");
				break;
				
			case '"':
				sb.append("&quot;");
				break;
				
			case '&':
				sb.append("&amp;");
				break;
				
			case '<':
				sb.append("&lt;");
				break;
				
			case '>':
				sb.append("&gt;");
				break;
				
			default:
				sb.append(ch);
				break;
			}
		}
		return sb.toString();
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
     * Returns the number of milliseconds since the Architect was launched.  This count
     * will only be accurate if the Architect's startup mechanism called {@link #startup()}
     * (The Swing UI startup mechanism does this).
     */
    public static long getAppUptime() {
        return System.currentTimeMillis() - startupTimeMillis;
    }
    
    /**
     * Returns the parent database of <tt>so</tt> or <tt>null</tt> if <tt>so</tt>
     * doesn't have an ancestor of type {@link SQLDatabase}.
     * 
     * @param so The object for whose database ancestor to look. (Thanks, Winston).
     * @return The nearest ancestor of type SQLDatabase, or null if no such ancestor exists.
     */
    public static SQLDatabase getParentDatabase(SQLObject so) {
        while (so != null) {
            if (so instanceof SQLDatabase) return (SQLDatabase) so;
            so = so.getParent();
        }
        return null;
    }
}
