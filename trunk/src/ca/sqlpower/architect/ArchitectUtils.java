package ca.sqlpower.architect;

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

import java.net.URL;

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
	 * Sets up the log4j logging framework.
	 */
	public static void configureLog4j() {
		String configName = "log4j.properties";
		URL config = ArchitectUtils.class.getClassLoader().getResource(configName);
		if (config == null) {
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
	public static void unlistenToHierarchy(SQLObjectListener listener, SQLObject source)
	throws ArchitectException {
		logger.debug("Unlistening to SQL Object "+source);
		source.removeSQLObjectListener(listener);
		if (source.isPopulated() && source.allowsChildren()) {
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
	 * 
	 * XXX: look thise up from somewhere (i.e. don't hard code them)
	 * 
	 */
	public static Map getDriverTemplateMap () {
		Map drivers = new HashMap();
		drivers.put("oracle.jdbc.driver.OracleDriver",
					"jdbc:oracle:thin:@<Hostname>:<Port:1521>:<Instance>");
		drivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
					"jdbc:microsoft:sqlserver://<Hostname>:<Port:1433>;SelectMethod=cursor");
		drivers.put("org.postgresql.Driver",
					"jdbc:postgresql://<Hostname>:<Port:5432>/<Database>");
		drivers.put("ibm.sql.DB2Driver",
					"jdbc:db2:<Hostname>");
		return drivers;
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
	 * string be safe for use as an XML attribute value enclosed in
	 * double quotes.
	 */
	public static String escapeXML(String src) {
		StringBuffer sb = new StringBuffer(src.length()+10);  // arbitrary amount of extra space
		char ch;
		for (int i = 0, n = src.length(); i < n; i++) {
			switch (ch = src.charAt(i)) {
			case '"':
				sb.append("&#x22;");
				break;
				
			case '&':
				sb.append("&#x26;");
				break;
				
			case '<':
				sb.append("&#x3C;");
				break;
				
			default:
				sb.append(ch);
				break;
			}
		}
		return sb.toString();
	}
}
