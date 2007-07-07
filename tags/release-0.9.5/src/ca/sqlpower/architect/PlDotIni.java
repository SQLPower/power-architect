/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The PlDotIni class represents the contents of a PL.INI file; despit
 * the name, this actually represents the master list of Data Source connections.
 * <p>
 * Note, there is some confusion about whether or not section name matching should
 * be case sensitive or not.  Both approaches are taken in this class!  Vive la difference.
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
     * It has default visibility because the unit test needs to use it.
     */
    static class Section {

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
            this.properties = new LinkedHashMap();
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

        /**
         * Updates this section's contents to look like the given one.
         * Doesn't modify the name.
         */
        public void merge(Section s) {
            // get rid of deleted properties
            properties.keySet().retainAll(s.properties.keySet());
            properties.putAll(s.properties);
        }
    }

    private static final Logger logger = Logger.getLogger(PlDotIni.class);

    /**
     * A list of Section and ArchitectDataSource objects, in the order they appear in the file;
     * this List contains Mixed Content (both Section and ArchitectDataSource) which is
     * A Very Bad Idea(tm) so it cannot be converted to Java 5 Generic Collection.
     */
    private final List<Object> fileSections = new ArrayList<Object>();

    /**
     * The time we last read the PL.INI file.
     */
    private long fileTime;
    boolean shuttingDown = false;
    
    /** Seconds to wait between checking the file. */
    int WAIT_TIME = 30;

    /**
     * Thread to stat file periodically, reload if PL changed it.
     * FIXME This thread is not currently started!
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
                        logger.debug("Re-reading PL.INI file because it has been modified externally.");
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

    /**
     * Returns the requested section in this pl.ini instance.  The sections
     * are stored in the same order they appear in the file, but of course
     * there may have been sections added or removed since the file was
     * last read.
     * <p>
     * Note, this method is really only intended for testing purposes.
     * 
     * @return The returned object will be of type Section, ArchitectDataSource,
     * or ArchitectDataSourceType depending on which type of file section it
     * represents.
     */
    Object getSection(int number) {
        return fileSections.get(number);
    }

    /**
     * Returns the number of sections that were in the pl.ini file we read
     * (additions and deletions after reading the file will affect the count
     * of course).  This will include unrecognized sections and the nameless
     * section at the top of the file, so don't expect the count to be equal
     * to the number of database types plus the number of connections.
     */
    int getSectionCount() {
        return fileSections.size();
    }
    
    /**
     * The enumeration of states that the read() method's INI file parser can be in.
     */
    private enum ReadState {READ_DS, READ_GENERIC, READ_TYPE}

    public void read(File location) throws IOException {
        if (!location.canRead()) {
            throw new IOException("pl.ini file is not readable: " + location.getAbsolutePath());
        }
        fileTime =  location.lastModified();
        lastFileAccessed = location;
        read(new FileInputStream(location));
    }
    
    public void read(InputStream inStream) throws IOException {
        
        logger.info("Beginning to read/merge new pl.ini data");
        
	    ReadState mode = ReadState.READ_GENERIC;

        try {
            dontAutoSave = true;

            ArchitectDataSourceType currentType = null;
            ArchitectDataSource currentDS = null;
            Section currentSection = new Section(null);  // this accounts for any properties before the first named section

            // Can't use Reader to read this file because the encrypted passwords contain non-ASCII characters
            BufferedInputStream in = new BufferedInputStream(inStream);

            byte[] lineBytes = null;

            while ((lineBytes = readLine(in)) != null) {
                String line = new String(lineBytes);
                logger.debug("Read in new line: "+line);
                
                if (line.startsWith("[")) {
                    mergeFileData(mode, currentType, currentDS, currentSection);
                }
                if (line.startsWith("[Databases_")) {
                    logger.debug("It's a new database connection spec!" +fileSections);
                    currentDS =  new ArchitectDataSource();
                    mode = ReadState.READ_DS;
                } else if (line.startsWith("[Database Types_")) {
                    logger.debug("It's a new database type!" + fileSections);
                    currentType =  new ArchitectDataSourceType();
                    mode = ReadState.READ_TYPE;
                } else if (line.startsWith("[")) {
                    logger.debug("It's a new generic section!");
                    currentSection = new Section(line.substring(1, line.length()-1));
                    mode = ReadState.READ_GENERIC;
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

                    if (mode == ReadState.READ_DS) {
                        // passwords are special, because the spectacular obfustaction technique
                        // can create bytes that are not in the range 32-127, which causes Java to
                        // map them to chars whose numeric value isn't the byte value!
                        // So, we have to read the "encrypted" password as an array of bytes.
                        if (key.equals("PWD") && value != null) {
                            byte[] cypherBytes = new byte[lineBytes.length - equalsIdx - 1];
                            System.arraycopy(lineBytes, equalsIdx + 1, cypherBytes, 0, cypherBytes.length);
                            value = decryptPassword(9, cypherBytes);
                        }
                        currentDS.put(key, value);
                    } else if (mode == ReadState.READ_TYPE) {
                        currentType.putProperty(key, value);
                    } else if (mode == ReadState.READ_GENERIC) {
                        currentSection.put(key, value);
                    }
                }
            }
            in.close();
            mergeFileData(mode, currentType, currentDS, currentSection);

            // hook up database type hierarchy, and assign parentType pointers to data sources themselves
            for (Object o : fileSections) {
                if (o instanceof ArchitectDataSourceType) {
                    ArchitectDataSourceType dst = (ArchitectDataSourceType) o;
                    String parentTypeName = dst.getProperty(ArchitectDataSourceType.PARENT_TYPE_NAME);
                    if (parentTypeName != null) {
                        ArchitectDataSourceType parentType = getDataSourceType(parentTypeName);
                        if (parentType == null) {
                            throw new IllegalStateException(
                                    "Database type \""+dst.getName()+"\" refers to parent type \""+
                                    parentTypeName+"\", which doesn't exist");
                        }
                        dst.setParentType(parentType);
                    }
                } else if (o instanceof ArchitectDataSource) {
                    ArchitectDataSource ds = (ArchitectDataSource) o;
                    String typeName = ds.getPropertiesMap().get(ArchitectDataSource.DBCS_CONNECTION_TYPE);
                    if (typeName != null) {
                        ArchitectDataSourceType type = getDataSourceType(typeName);
                        if (type == null) {
                            throw new IllegalStateException(
                                    "Database connection \""+ds.getName()+"\" refers to database type \""+
                                    typeName+"\", which doesn't exist");
                        }
                        logger.debug("The data source type \"" + type + "\" is being set as the parent type of" + ds);
                        ds.setParentType(type);
                    }
                    
                }
            }
        } finally {
            dontAutoSave = false;
        }

		logger.info("Finished reading file.");
	}

    /**
     * A subroutine of the read() method. Merges data from any of the three types of
     * sections into the fileSections collection.
     * <p> 
     * A better approach than this would be to have ArchitectDataSourceType, ArchitectDataSource,
     * and Section all implement some interface, and then just have one merge() method.
     * 
     * @param mode File parsing mode. Determines which mergeXXX() method is called, and
     * which argument is passed in.
     * @param currentType Only used when mode = READ_TYPE
     * @param currentDS Only used when mode = READ_DS
     * @param currentSection Only used when mode = READ_GENERIC
     */
    private void mergeFileData(ReadState mode, ArchitectDataSourceType currentType, ArchitectDataSource currentDS, Section currentSection) {
        if (mode == ReadState.READ_DS) {
            mergeDataSource(currentDS);
        } else if (mode == ReadState.READ_GENERIC) {
            mergeSection(currentSection);
        } else if (mode == ReadState.READ_TYPE) {
            // special case: sometimes the parser ends up thinking there was
            // an empty ds type section at the end of the file. we can't merge it.
            if (currentType.getProperties().size() > 0) {
                mergeDataSourceType(currentType);
            }
        } else {
            throw new IllegalArgumentException("Unknown read state. Can't merge");
        }
    }

	private void mergeSection(Section currentSection) {
        logger.debug("Attempting to merge Section: \"" + currentSection.getName() + "\"");
        for (Object o : fileSections) {
            if (o instanceof Section) {
                Section s = (Section) o;
                if ( (s.getName() == null && currentSection.getName() == null)
                    || (s.getName() != null && s.getName().equals(currentSection.getName())) ) {
                    logger.debug("Found a section to merge, now merging");
                    s.merge(currentSection);
                    return;
                }
            }
        }
        
        logger.debug("Didn't find section to merge. Adding...");
        fileSections.add(currentSection);
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
        logger.debug("Writing to "+location);
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

    /**
     * Writes the data source types and the data sources of this instance
     * in the world-famous PL.INI format.  Doesn't affect the lastFileLocation
     * or anything.
     */
	public void write(OutputStream out) throws IOException {
	    
        // counting starts at 1. Yay, VB!
        int dbNum = 1;
        int typeNum = 1;

        Iterator it = fileSections.iterator();
	    while (it.hasNext()) {
	        Object next = it.next();

	        if (next instanceof Section) {
	            writeSection(out, ((Section) next).getName(), ((Section) next).getPropertiesMap());
            } else if (next instanceof ArchitectDataSource) {
                writeSection(out, "Databases_"+dbNum, ((ArchitectDataSource) next).getPropertiesMap());
                dbNum++;
            } else if (next instanceof ArchitectDataSourceType) {
                writeSection(out, "Database Types_"+typeNum, ((ArchitectDataSourceType) next).getProperties());
                typeNum++;
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

    public ArchitectDataSource getDataSource(String name) {
        Iterator it = fileSections.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof ArchitectDataSource) {
                ArchitectDataSource ds = (ArchitectDataSource) next;
                if (logger.isDebugEnabled()) {
                    logger.debug("Checking if data source "+ds+" is PL Logical connection "+name);
                }
                if (ds.getName().equals(name)) return ds;
            }
        }
        return null;
    }

    public ArchitectDataSourceType getDataSourceType(String name) {
        for (Object next : fileSections) {
            if (next instanceof ArchitectDataSourceType) {
                ArchitectDataSourceType dst = (ArchitectDataSourceType) next;
                if (logger.isDebugEnabled()) {
                    logger.debug("Checking if data source type "+dst+" is called "+name);
                }
                if (dst.getName().equals(name)) return dst;
            }
        }
        return null;
    }

    public List<ArchitectDataSourceType> getDataSourceTypes() {
        List<ArchitectDataSourceType> list = new ArrayList<ArchitectDataSourceType>();
        for (Object next : fileSections) {
            if (next instanceof ArchitectDataSourceType) {
                ArchitectDataSourceType dst = (ArchitectDataSourceType) next;
                list.add(dst);
            }
        }
        return list;
    }

    /* Creates a list of data sources by iterating over all the sections and
     * picking the ones that are ArchitectDataSource items.  Yes, this is not
     * optimal, but we can defer optimising it until someone proves it's an
     * actual performance issue.
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
	 * <code>key</code> is 9.
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
		addDataSourceImpl(dbcs);
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
                    for (Map.Entry<String, String> ent : dbcs.getPropertiesMap().entrySet()) {
                        oneDbcs.put(ent.getKey(), ent.getValue());
                    }
				    return;
				}
			}
    	}
        
        // we only get here if we didn't find a data source to update.
        addDataSourceImpl(dbcs);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.architect.DataSourceCollection#removeDataSource(ca.sqlpower.architect.ArchitectDataSource)
     */
    public void removeDataSource(ArchitectDataSource dbcs) {
        
        // need to know the index we're removing in order to fire the remove event
        // (so using an indexed for loop, not a ListIterator)
        for ( int where=0; where<fileSections.size(); where++ ) {
            Object o  = fileSections.get(where);
            if (o instanceof ArchitectDataSource) {
                ArchitectDataSource current = (ArchitectDataSource) o;
                if (current.getName().equals(dbcs.getName())) {
                    fileSections.remove(where);
                    fireRemoveEvent(where, dbcs);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("dbcs not in list");
    }

    /**
     * Copies all the properties in the given dst into the existing DataSourceType section
     * with the same name, if one exists.  Otherwise, adds the given dst as a new section.
     */
    public void mergeDataSourceType(ArchitectDataSourceType dst) {
        logger.debug("Merging data source type "+dst.getName());
        String newName = dst.getName();
        if (newName == null) {
            throw new IllegalArgumentException("Can't merge a nameless data source type: "+dst);
        }
        for (Object o : fileSections) {
            if (o instanceof ArchitectDataSourceType) {
                ArchitectDataSourceType current = (ArchitectDataSourceType) o;
                if (newName.equalsIgnoreCase(current.getName())) {
                    logger.debug("    Found it");
                    for (Map.Entry<String, String> ent : dst.getProperties().entrySet()) {
                        current.putProperty(ent.getKey(), ent.getValue());
                    }
                    return;
                }
            }
        }
        
        logger.debug("    Not found.. adding");
        addDataSourceType(dst);

    }

	/**
	 * Common code for add and merge.  Adds the given dbcs as a section, then fires an add event.
	 * @param dbcs
	 */
	private void addDataSourceImpl(ArchitectDataSource dbcs) {
		fileSections.add(dbcs);
		fireAddEvent(dbcs);
	}

    public void addDataSourceType(ArchitectDataSourceType dataSourceType) {
        fileSections.add(dataSourceType);
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
