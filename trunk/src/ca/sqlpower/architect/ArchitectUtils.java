package ca.sqlpower.architect;

import java.util.Iterator;
import org.apache.log4j.Logger;
import java.net.URL;

public class ArchitectUtils {

	private static final Logger logger = Logger.getLogger(ArchitectUtils.class);

	/**
	 * This class is just a container for utility routines; you do not
	 * need to instantiate it.
	 */
	private ArchitectUtils() {}

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
}
