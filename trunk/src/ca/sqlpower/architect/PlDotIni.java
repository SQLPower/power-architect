/*
 * Created on Jun 28, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * The PlDotIni class represents the contents of a PL.INI file; despit
 * the name, this actually represents the master list of Data Source connections,
 * and XXX should be renamed to DataSourceList.
 * <p>
 * <b>Warning:</b> this file only reads (and therefore writes) files with MS-DOS line endings,
 * regardless of platform, since the encoding of binary passwords
 * could result in a bare \n in the encryption...
 * @version $Id$
 */
public class PlDotIni implements DataSourceCollection {

    /**
     * Boolean to control whether we autosave, to prevent calling it while we're already saving.
     */
    private boolean dontAutoSave;

	/**
	 * The list of Listeners to notify when a datasource is added or removed.
	 */
	List<DatabaseListChangeListener> listeners;

	DatabaseListChangeListener saver = new DatabaseListChangeListener() {

	    public void databaseAdded(DatabaseListChangeEvent e) {
	        saveIfFileKnown();
	    }

	    public void databaseRemoved(DatabaseListChangeEvent e) {
            saveIfFileKnown();
	    }

	    private void saveIfFileKnown() {
            if (dontAutoSave)
                return;
	        if (lastFileAccessed != null) {
	            try {
	                write(lastFileAccessed);
	            } catch (IOException e) {
	                logger.error("Error auto-saving PL.INI file", e);
	            }
	        }

	    }
	};

    /**
     * Construct a PL.INI object, and set an Add Listener to save
     * the file when a database is added (bugid 1032).
     */
    public PlDotIni() {
        listeners = new ArrayList<DatabaseListChangeListener>();
        listeners.add(saver);
    }

	/**
     * The Section class represents a named section in the PL.INI file.
     */
    private static class Section {

        /** The name of this section (without the square brackets). */
        private String name;

        /**
         * The contents of this section (part before the '=' is the key, and
         * the rest of the line is the value).
         */
        private Map properties;

        /** Creates a new section with the given name and no properties. */
        public Section(String name) {
            this.name = name;
            this.properties = new HashMap();
        }

        /**
         * Puts a new property in this section, or replaces an existing
         * property which has the same key. Keys are case-sensitive.
         *
         * @param key The property's key
         * @param value The property's value
         * @return The old value of the property under this key, if one existed.
         */
        public Object put(String key, String value) {
            return properties.put(key, value);
        }

        /** Returns the whole properties map.  This is required when saving the PL.INI file. */
        public Map getPropertiesMap() {
            return properties;
        }

        /** Returns the name of this section. */
        public String getName() {
            return name;
        }
    }

    private static final Logger logger = Logger.getLogger(PlDotIni.class);

    /**
     * A list of Section and ArchitectDataSource objects, in the order they appear in the file;
     * this List contains Mixed Content (both Section and ArchitectDataSource) which is
     * A Very Bad Idea(tm) so it cannot be converted to Java 5 Generic Collection.
     */
    private final List fileSections= new ArrayList();

    /**
     * The time we last read the PL.INI file.
     */
    private long fileTime;
    boolean shuttingDown = false;
    /** Seconds to wait between checking the file */
    int WAIT_TIME = 30;

    /**
     * Thread to stat file periodically, reload if PL changed it.
     * XXX This thread is not currently started!
     */
    Thread monitor = new Thread() {
		public void run() {
			while (!shuttingDown) {
				try {
					Thread.sleep(WAIT_TIME * 1000);
					if (lastFileAccessed == null) {
						continue;
					}
					long newFileTime = lastFileAccessed.lastModified();
					if (newFileTime != fileTime) {
						read(lastFileAccessed);
						fileTime = newFileTime;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

    File lastFileAccessed;

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#read(java.io.File)
     */
	public void read(File location) throws IOException {
        //FIXME: This method needs to be called in more places, ie the constructor?
	    final int MODE_READ_DS = 0;       // reading a data source section
	    final int MODE_READ_GENERIC = 1;  // reading a generic named section
	    int mode = MODE_READ_GENERIC;

        try {
            dontAutoSave = true;
            if (!location.canRead()) {
                throw new IllegalArgumentException("pl.ini file cannot be read: " + location.getAbsolutePath());
            }
            lastFileAccessed = location;

            ArchitectDataSource currentDS = null;
            Section currentSection = new Section(null);  // this accounts for any properties before the first named section
            fileSections.add(currentSection);
            fileTime =  location.lastModified();

            // Can't use Reader to read this file because the encrypted passwords contain non-ASCII characters
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(location));

            byte[] lineBytes = null;

            while ((lineBytes = readLine(in)) != null) {
                String line = new String(lineBytes);
                logger.debug("Read in new line: "+line);
                if (line.startsWith("[Databases_")) {
                    logger.debug("It's a new database connection spec!");
                    currentDS =  new ArchitectDataSource();
                    add(currentDS);
                    mode = MODE_READ_DS;
                } else if (line.startsWith("[")) {
                    logger.debug("It's a new generic section!");
                    currentSection = new Section(line.substring(1, line.length()-1));
                    fileSections.add(currentSection);
                    mode = MODE_READ_GENERIC;
                } else {
                    String key;
                    String value;
                    int equalsIdx = line.indexOf('=');
                    if (equalsIdx > 0) {
                        key = line.substring(0, equalsIdx);
                        value = line.substring(equalsIdx+1, line.length());
                    } else {
                        key = line;
                        value = null;
                    }
                    logger.debug("key="+key+",val="+value);

                    if (mode == MODE_READ_DS) {
                        if (key.equals("PWD") && value != null) {
                            byte[] cypherBytes = new byte[lineBytes.length - equalsIdx - 1];
                            System.arraycopy(lineBytes, equalsIdx + 1, cypherBytes, 0, cypherBytes.length);
                            value = decryptPassword(9, cypherBytes);
                        }
                        currentDS.put(key, value);
                    } else if (mode == MODE_READ_GENERIC) {
                        currentSection.put(key, value);
                    }
                }
            }
            in.close();
        } finally {
            dontAutoSave = false;
        }

		if (logger.isDebugEnabled()) logger.debug("Finished reading file. Parsed contents:\n"+toString());
	}

	/**
	 * Reads bytes from the input stream until a CRLF pair or end-of-file is encountered.
	 * If a line is longer than some arbitrary maximum (currently 10000 bytes), it will
	 * be split into pieces not larger than that size and returned as separate lines.
	 * In this case, an error will be logged to the class's logger.
	 *
	 * <p>Note: We think that we require CRLF line ends because the encrypted password
	 * could contain a bare CR or LF, which we don't want to interpret as an end-of-line.
	 *
     * @param in The input stream to read.
     * @return All of the bytes read except the terminating CRLF.  If there are no more
     * bytes to read (because in is already at end-of-file) then this method returns null.
     */
    private byte[] readLine(BufferedInputStream in) throws IOException {
        final int MAX_LINE_LENGTH = 10000;
        byte[] buffer = new byte[MAX_LINE_LENGTH];
        int lineSize = 0;
        int ch;
        while ( (ch = in.read()) != -1 && lineSize < MAX_LINE_LENGTH) {
            buffer[lineSize] = (byte) ch;
            lineSize++;
            if (lineSize >= 2 && buffer[lineSize-2] == '\r' && buffer[lineSize-1] == '\n') {
                lineSize -= 2;
                break;
            }
        }

        // check for end of file
        if (ch == -1 && lineSize == 0) return null;

        // check for split lines
        if (lineSize == MAX_LINE_LENGTH) logger.error("Maximum line size exceeded while reading pl.ini.  Line will be split up.");

        byte chopBuffer[] = new byte[lineSize];
        System.arraycopy(buffer, 0, chopBuffer, 0, lineSize);
        return chopBuffer;
    }

    public void write() throws IOException {
        if (lastFileAccessed == null) {
            throw new IllegalStateException("Can't determine location for saving");
        }
        write(lastFileAccessed);
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#write(java.io.File)
     */
	public void write(File location) throws IOException {
        try {
            dontAutoSave = true;
    	    OutputStream out = new BufferedOutputStream(new FileOutputStream(location));
    	    write(out);
    	    out.close();
            lastFileAccessed = location;
    	    fileTime = location.lastModified();
        } finally {
            dontAutoSave = false;
        }
	}

	private void write(OutputStream out) throws IOException {
	    int dbNum = 1;
	    Iterator it = fileSections.iterator();
	    while (it.hasNext()) {
	        Object next = it.next();

	        if (next instanceof Section) {
	            writeSection(out, ((Section) next).getName(), ((Section) next).getPropertiesMap());
	        } else if (next instanceof ArchitectDataSource) {
	            writeSection(out, "Databases_"+dbNum, ((ArchitectDataSource) next).getPropertiesMap());
	            dbNum++;
	        } else if (next == null) {
	            logger.error("write: Null section");
	        } else {
	            logger.error("write: Unknown section type: "+next.getClass().getName());
	        }
	    }
	}

	/**
	 * Writes out the named section header, followed by all the properties, one per line.  Each
	 * line is terminated with a CRLF, regardless of the current platform default.
	 *
	 * @param out The output stream to write to.
	 * @param name The name of the section.
	 * @param properties The properties to output in this section.
	 * @throws IOException when writing to the given stream fails.
	 */
	private void writeSection(OutputStream out, String name, Map properties) throws IOException {
	    if (name != null) {
	        String sectionHeading = "["+name+"]" + DOS_CR_LF;
	        out.write(sectionHeading.getBytes());
	    }

	    // output LOGICAL first (if it exists)
	    String s = null;
	    if ((s = (String) properties.get("Logical")) != null) {
	        out.write("Logical".getBytes());
            out.write("=".getBytes());
            out.write(s.getBytes());
	        out.write(DOS_CR_LF.getBytes());
	    }

	    // now get everything else, and ignore the LOGICAL property
	    Iterator it = properties.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry ent = (Map.Entry) it.next();
	        if (!ent.getKey().equals("Logical")) {
	        	out.write(((String) ent.getKey()).getBytes());
	        	if (ent.getValue() != null) {
	        		byte[] val;
	        		if (ent.getKey().equals("PWD")) {
	        			val = encryptPassword(9, ((String) ent.getValue()));
	        		} else {
	        			val = ((String) ent.getValue()).getBytes();
	        		}
	        		out.write("=".getBytes());
	        		out.write(val);
	        	}
	        	out.write(DOS_CR_LF.getBytes());
	        }
	    }
	}

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#getDataSource(java.lang.String)
     */
	public ArchitectDataSource getDataSource(String name) {
	    Iterator it = fileSections.iterator();
	    while (it.hasNext()) {
	        Object next = it.next();
	        if (next instanceof ArchitectDataSource) {
	            ArchitectDataSource ds = (ArchitectDataSource) next;
	            logger.debug("Checking if data source "+ds+" is PL Logical connection "+name);
	            if (ds.getName().equals(name)) return ds;
	        }
	    }
	    return null;
	}

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#getConnections()
     */
    public List<ArchitectDataSource> getConnections() {
        List<ArchitectDataSource> connections = new ArrayList<ArchitectDataSource>();
	    Iterator it = fileSections.iterator();
	    while (it.hasNext()) {
	        Object next = it.next();
	        if (next instanceof ArchitectDataSource) {
	            connections.add((ArchitectDataSource) next);
	        }
	    }
        Collections.sort(connections, new ArchitectDataSource.DefaultComparator());
        return connections;
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#toString()
     */
    public String toString() {
        OutputStream out = new ByteArrayOutputStream();
        try {
            write(out);
        } catch (IOException e) {
            return "PlDotIni: toString: Couldn't create string description: "+e.getMessage();
        }
        return out.toString();
    }

	/**
	 * Encrypts a PL.INI password.  The correct argument for
	 * <code>number</code> is 9.
	 */
    private byte[] encryptPassword(int key, String plaintext) {
        byte[] cyphertext = new byte[plaintext.length()];
        int temp;

        for (int i = 0; i < plaintext.length(); i++) {
            temp = plaintext.charAt(i);
            if (i % 2 == 1) {
                // if odd (even in VB's 1-based indexing)
                temp = temp - key;
            } else {
                temp = temp + key;
            }

            temp = temp ^ (10 - key);
            cyphertext[i] = ((byte) temp);
        }

        if (logger.isDebugEnabled()) {
            StringBuffer nums = new StringBuffer();
            for (int i = 0; i < cyphertext.length; i++) {
                nums.append((int) cyphertext[i]);
                nums.append(' ');
            }
            logger.debug("Encrypt: Plaintext: \""+plaintext+"\"; cyphertext=("+nums+")");
        }

        return cyphertext;
    }

	/**
	 * Decrypts a PL.INI password.  The correct argument for
	 * <code>number</code> is 9.
	 */
	public static String decryptPassword(int number, byte[] cyphertext) {
		StringBuffer plaintext = new StringBuffer(cyphertext.length);

		for (int i = 0, n = cyphertext.length; i < n; i++) {
			int temp = (( ((int) cyphertext[i]) & 0x00ff) ^ (10 - number));

			if (i % 2 == 1) {
				temp += number;
			} else {
				temp -= number;
			}
			plaintext.append((char) temp);
		}

		if (logger.isDebugEnabled()) {
            StringBuffer nums = new StringBuffer();
            for (int i = 0; i < cyphertext.length; i++) {
                nums.append((int) cyphertext[i]);
                nums.append(' ');
            }
            logger.debug("Decrypt: cyphertext=("+nums+"); Plaintext: \""+plaintext+"\"");
        }

        return plaintext.toString();
	}

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#addDataSource(ca.sqlpower.architect.ArchitectDataSource)
     */
	public void addDataSource(ArchitectDataSource dbcs) {
		String newName = dbcs.getDisplayName();
		for (Object o : fileSections) {
			if (o instanceof ArchitectDataSource) {
				ArchitectDataSource oneDbcs = (ArchitectDataSource) o;
				if (newName.equalsIgnoreCase(oneDbcs.getDisplayName())) {
					throw new IllegalArgumentException(
							"There is already a datasource with the name " + newName);
				}
			}
		}
		add(dbcs);
    }

	/* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#mergeDataSource(ca.sqlpower.architect.ArchitectDataSource)
     */
    public void mergeDataSource(ArchitectDataSource dbcs) {
    	String newName = dbcs.getDisplayName();
    	for (Object o : fileSections) {
    		if (o instanceof ArchitectDataSource) {
				ArchitectDataSource oneDbcs = (ArchitectDataSource) o;
				if (newName.equalsIgnoreCase(oneDbcs.getDisplayName())) {
					try {
						BeanUtils.copyProperties(oneDbcs, dbcs);
						return;
					} catch (IllegalAccessException e) {
						throw new RuntimeException("Can't merge DBCS: " + e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException("Can't merge DBCS: " + e);
					}
				}
			}
    	}
        add(dbcs);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#removeDataSource(ca.sqlpower.architect.ArchitectDataSource)
     */
    public void removeDataSource(ArchitectDataSource dbcs) {
        for ( int where=0; where<fileSections.size(); where++ ) {
            Object o  = fileSections.get(where);
            if (o instanceof ArchitectDataSource) {
                if ( o.equals(dbcs) ) {
                    fileSections.remove(where);
                    fireRemoveEvent(where, dbcs);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("dbcs not in list");
    }

	/**
	 * Common code for add and merge.
	 * @param dbcs
	 */
	private void add(ArchitectDataSource dbcs) {
		fileSections.add(dbcs);
		fireAddEvent(dbcs);
	}

    private void fireAddEvent(ArchitectDataSource dbcs) {
		int index = fileSections.size()-1;
		DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, index, dbcs);
    	synchronized(listeners) {
			for(DatabaseListChangeListener listener : listeners) {
				listener.databaseAdded(e);
			}
		}
	}

    private void fireRemoveEvent(int i, ArchitectDataSource dbcs) {
    	DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, i, dbcs);
    	synchronized(listeners) {
			for(DatabaseListChangeListener listener : listeners) {
				listener.databaseRemoved(e);
			}
		}
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#addDatabaseListChangeListener(ca.sqlpower.architect.DatabaseListChangeListener)
     */
    public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
    	synchronized(listeners) {
    		listeners.add(l);
    	}
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#removeDatabaseListChangeListener(ca.sqlpower.architect.DatabaseListChangeListener)
     */
    public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
    	synchronized(listeners) {
    		listeners.remove(l);
    	}
    }


}
