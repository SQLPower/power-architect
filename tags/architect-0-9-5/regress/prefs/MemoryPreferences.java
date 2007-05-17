package prefs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * A java.util.prefs.Preferences that does NOT persist anything, so it has no effect (nor is
 * affected by!) any use of the "regular" Preferences.
 * To use, run with -Djava.util.prefs.PreferencesFactory=prefs.PreferencesFactory
 */
public class MemoryPreferences extends AbstractPreferences {
	
	private static final String STATIC_XML_FILE = "testbed/statictestprefs.xml";

	private static final Logger logger = Logger.getLogger(MemoryPreferences.class);
	
	/**
	 * The map of all data in this particular node.
	 */
	final Map<String, String> values = new HashMap<String, String>();
	
	/** The map of all Preferences nodes immediately below this node
	 */
	final Map<String,Preferences> children = new HashMap<String,Preferences>();

	private static boolean lazyLoaded;
	
	public final static String SYSTEM_PROPS_ERROR_MESSAGE =
		"Did you remember to run with -D"+PreferencesFactory.PREFS_FACTORY_SYSTEM_PROPERTY+"="+PreferencesFactory.MY_CLASS_NAME+"?";
	
	private static synchronized void lazyLoad() {
		InputStream rdr = null;
		try {
			rdr = new FileInputStream(STATIC_XML_FILE);
			Preferences.importPreferences(rdr);
			System.err.println("Warning, you are using a Preferences implementation which deliberately");
			System.err.println("violates the contract of java.util.prefs.Preferences with regard to");
			System.err.println("persistence; initial prefs loaded from " + STATIC_XML_FILE);
			System.err.println("and none of your Preferences changes will be saved!");
			lazyLoaded = true;
		} catch (Exception e) {
			System.err.println("Failed to load static preferences file " + STATIC_XML_FILE);
			e.printStackTrace();
		} finally {
			if (rdr != null)
				try {
					rdr.close();
				} catch (IOException e) {
					// CANTHAPPEN
				}
		}
	}
	
	/**
	 * Constructor, non-public, only for use by my PrefencesFactory; should only be called from
	 * the PreferencesFactory and from node() below; node() takes care of finding the full path
	 * if the incoming path is relative.
	 * @param fullPath
	 */
	MemoryPreferences(AbstractPreferences parent, String name) {
		super(parent, name);
        
        // note, logger should never be null because it's statically initialised.  However,
        // it comes out null every time we run the MatchMaker SwingSessionContextTest!  Hmm...
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(String.format("MemoryPreferences.MemoryPreferences(%s, %s)", parent, name));
        }
	}
	
	@Override
	protected void putSpi(String key, String value) {
		values.put(key, value);
	}

	@Override
	protected String getSpi(String key) {
		if (!lazyLoaded) {
			lazyLoad();
		}
		String value = values.get(key);
		logger.debug(String.format("get: %s=%s", key, value));
		return value;
	}

	@Override
	protected void removeSpi(String key) {
		values.remove(key);
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		// nothing to do here?
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return values.keySet().toArray(new String[values.size()]);
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return children.keySet().toArray(new String[children.size()]);
	}

	@Override
	protected AbstractPreferences childSpi(String name) {
		logger.debug(String.format("MemoryPreferences.node(%s)", name));
		AbstractPreferences n = new MemoryPreferences(this, name);
		children.put(name, n);
		return n;
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		// nothing to do
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		// nothing to do
	}


}
