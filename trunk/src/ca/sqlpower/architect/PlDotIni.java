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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The PlDotIni class represents the contents of a PL.INI file.
 * <p>
 * <b>Warning:</b> this file only reads (and therefore writes) files with MS-DOS line endings.
 *
 * @author fuerth
 * @version $Id$
 */
public class PlDotIni {
    
    public static final String DOS_CR_LF = "\r\n";

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
     * A list of Section and ArchitectDataSource objects, in the order they appear in the file.
     */
    private List fileSections;
    
    /**
     * The time we last read the PL.INI file.
     */
    private Date fileTime;
    
	/**
	 * Reads the PL.INI file at the given location into a new fileSections list.  Also updates the fileTime.
	 */
	public void read(File location) throws IOException {
	    final int MODE_READ_DS = 0;       // reading a data source section
	    final int MODE_READ_GENERIC = 1;  // reading a generic named section
	    int mode = MODE_READ_GENERIC;
	    
		fileSections = new ArrayList();
		ArchitectDataSource currentDS = null;
		Section currentSection = new Section(null);  // this accounts for any properties before the first named section
		fileSections.add(currentSection);
		fileTime = new Date(location.lastModified());
		
		// Can't use Reader to read this file because the encrypted passwords contain non-ASCII characters
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(location));

		byte[] lineBytes = null;

		while ((lineBytes = readLine(in)) != null) {
		    String line = new String(lineBytes);
		    logger.debug("Read in new line: "+line);
			if (line.startsWith("[Databases_")) {
				logger.debug("It's a new database connection spec!");
				currentDS =  new ArchitectDataSource();
				fileSections.add(currentDS);
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

    /**
	 * Writes out every section in the fileSections list in the 
	 * order they appear in that list.
	 * 
	 * @param location The location to write to.
	 * @throws IOException if the location is not writeable for any reason.
	 */
	public void write(File location) throws IOException {
	    OutputStream out = new BufferedOutputStream(new FileOutputStream(location));
	    write(out);
	    out.close();
	    fileTime = new Date(location.lastModified());
	}
	
	public void write(OutputStream out) throws IOException {
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
	public void writeSection(OutputStream out, String name, Map properties) throws IOException {
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

	/**
	 * Searches the list of connections for one with the given name.
	 * 
	 * @param name The Logical datbabase name to look for.
	 * @return the first ArchitectDataSource in the file whose name matches the
	 * given name, or null if no such datasource exists.
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

    /**
     * @return a sorted List of all the data sources in this pl.ini.
     */
    public List getConnections() {
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
        
        String retval = cyphertext.toString();
        
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

    /**
     * Adds a new data source to the end of this file's list of sections.
     * 
     * @param dbcs The new data source to add
     */
    public void addDataSource(ArchitectDataSource dbcs) {
        fileSections.add(dbcs);
    }

}
