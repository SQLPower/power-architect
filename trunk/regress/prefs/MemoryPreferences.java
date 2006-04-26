package prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * A java.util.prefs.Preferences that does NOT persist anything, so it has no effect (nor is
 * affected by!) any use of the "regular" Preferences.
 * XXX re-implement subclassing from AbstractPreferences, remove numerous methods that way.
 */
public class MemoryPreferences extends Preferences {
	
	private static final String THIS_IMPLEMENTATION_DOES_NOT_SUPPORT_PERSISTENCE = "This implementation does not support export";

	/**
	 * The path to this node, from the root
	 */
	final String fullPath;
	
	/**
	 * the Name of this node, from its parent
	 */
	final String name;
	
	/**
	 * The set of all nodes in this tree
	 */
	final Map<String, Preferences> allNodes;
	
	/**
	 * The map of all data in this particular node.
	 */
	final Map<String, Object> values = new HashMap<String, Object>();
	
	/**
	 * The set of all nodes under this node
	 */
	final Map<String, Preferences> childNodes = new HashMap<String, Preferences>();

	private final boolean isSystemNode;
	
	static {
		System.err.println("Warning, you are using a Preferences implementation which deliberately");
		System.err.println("violates the contract of java.util.prefs.Preferences with regard to");
		System.err.println("persistence; none of your Preferences changes will be saved!");
	}
	
	/**
	 * Constructor, non-public, only for use by my PrefencesFactory; should only be called from
	 * the PreferencesFactory and from node() below; node() takes care of finding the full path
	 * if the incoming path is relative.
	 * @param fullPath
	 */
	MemoryPreferences(Map<String,Preferences> map, boolean isSystem, String fullPath) {
		System.out.printf("MemoryPreferences.MemoryPreferences(%s, %s)%n", map, fullPath);
		
		this.allNodes = map;

		this.fullPath = fullPath;

		this.name = getNamePart(fullPath);
		this.isSystemNode = isSystem;
		allNodes.put(fullPath, this);
	}
	
	/**
	 * Returns the specified node, creating it (and any intervening nodes) if necessary.
	 * @see java.util.prefs.Preferences#node(java.lang.String)
	 */
	@Override
	public Preferences node(String pathName) {
		System.out.printf("MemoryPreferences.node(%s)%n", pathName);
		String fullPath = (isAbsolutePath(pathName) ? pathName : parent() + "/" + pathName);
		if (allNodes.get(fullPath) != null)
			return allNodes.get(fullPath);
		// XXX must create intervening nodes here, or they may fail to get created
		return new MemoryPreferences(allNodes, isSystemNode, fullPath);
	}

	@Override
	public boolean nodeExists(String pathName) throws BackingStoreException {
		return allNodes.get(pathName) != null;
	}
	
	static String getDirectoryPart(String name) {
		int ix = name.lastIndexOf('/');
		if (ix == -1)
			return name;
		return name.substring(0, ix);
	}
	
	static String getNamePart(String name) {
		int ix = name.lastIndexOf('/');
		if (ix == -1)
			return name;
		return name.substring(ix+1);
	}
	
	static boolean isAbsolutePath(String path) {
		return path.charAt(0) == '/';
	}
	
	@Override
	public void put(String key, String value) {
		put(key, (Object)value);
	}
	
	void put(String key, Object value) {
		if (key.length() > MAX_KEY_LENGTH )
			throw new IllegalArgumentException("key length may not exceed " + MAX_KEY_LENGTH);
		values.put(key, value);
	}

	@Override
	public String get(String key, String def) {
		if (key == null) {
			throw new NullPointerException("Key may not be null");
		}
		if (key.length() > MAX_KEY_LENGTH )
			throw new IllegalArgumentException("key length may not exceed " + MAX_KEY_LENGTH);
		
		return (String)(values.get(key) != null ? values.get(key) : def);
	}

	@Override
	public void remove(String key) {
		values.remove(key);
	}

	@Override
	public void clear() throws BackingStoreException {
		values.clear();
	}

	@Override
	public void putInt(String key, int value) {
		values.put(key, Integer.toString(value));
	}

	@Override
	public int getInt(String key, int def) {		
		String val = (String) values.get(key);
		return val != null ? Integer.parseInt(val) : def;
	}

	@Override
	public void putLong(String key, long value) {
		values.put(key, Long.toString(value));
	}

	@Override
	public long getLong(String key, long def) {
		String val = (String) values.get(key);
		return val != null ? Long.parseLong(val) : def;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		values.put(key, Boolean.toString(value));
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		String val = (String) values.get(key);
		return val != null ? Boolean.parseBoolean(val) : def;
	}

	@Override
	public void putFloat(String key, float value) {
		values.put(key, Float.toString(value));
	}

	@Override
	public float getFloat(String key, float def) {
		String val = (String) values.get(key);
		return val != null ? Float.parseFloat(val) : def;
	}

	@Override
	public void putDouble(String key, double value) {
		values.put(key, Double.toString(value));
	}

	@Override
	public double getDouble(String key, double def) {
		String val = (String) values.get(key);
		return val != null ? Double.parseDouble(val) : def;
	}

	@Override
	public void putByteArray(String key, byte[] value) {
		values.put(key, value);
	}

	@Override
	public byte[] getByteArray(String key, byte[] def) {
		if (values.get(key) == null)
			return def;
		return (byte[]) values.get(key);
	}

	@Override
	public String[] keys() throws BackingStoreException {
		return values.keySet().toArray(new String[values.size()]);
	}

	@Override
	public String[] childrenNames() throws BackingStoreException {
		return childNodes.keySet().toArray(new String[childNodes.size()]);
	}

	@Override
	public Preferences parent() {
		System.out.println("MemoryPreferences.parent()");
		return node(getDirectoryPart(fullPath));
	}



	@Override
	public void removeNode() throws BackingStoreException {
		allNodes.remove(this);
	}

	@Override
	public String name() {		
		return name;
	}

	@Override
	public String absolutePath() {
		return fullPath;
	}

	@Override
	public boolean isUserNode() {		
		return !isSystemNode;
	}

	@Override
	public String toString() {		
		return String.format("MemoryPreferencesNode(%s)", absolutePath());
	}

	@Override
	public void flush() throws BackingStoreException {
		// null
	}

	@Override
	public void sync() throws BackingStoreException {
		// null
	}

	@Override
	public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
		System.out.println("MemoryPreferences.addPreferenceChangeListener()");
	}

	@Override
	public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
		System.out.println("MemoryPreferences.removePreferenceChangeListener()");
	}

	@Override
	public void addNodeChangeListener(NodeChangeListener ncl) {
		System.out.println("MemoryPreferences.addNodeChangeListener()");
	}

	@Override
	public void removeNodeChangeListener(NodeChangeListener ncl) {
		System.out.println("MemoryPreferences.removeNodeChangeListener()");
	}

	@Override
	public void exportNode(OutputStream os) throws IOException,
			BackingStoreException {
		System.out.println("MemoryPreferences.exportNode()");
		throw new BackingStoreException(THIS_IMPLEMENTATION_DOES_NOT_SUPPORT_PERSISTENCE);
	}

	@Override
	public void exportSubtree(OutputStream os) throws IOException,
			BackingStoreException {
		System.out.println("MemoryPreferences.exportSubtree()");
		throw new BackingStoreException(THIS_IMPLEMENTATION_DOES_NOT_SUPPORT_PERSISTENCE);
	}


}
