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
package ca.sqlpower.architect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;


/**
 * Defines a type of data source.  We wanted to call this ArchitectDataSourceClass,
 * but that would be confusing in that we mean class as in class, type, genre, sort,
 * variety, breed (ahem, that's enough, Mr. Data).
 * <p>
 * Data Source types can have supertypes, from which they inherit any undefined property
 * values.
 */
public class ArchitectDataSourceType {
    
    static final Logger logger = Logger.getLogger(ArchitectDataSourceType.class);
    
    /**
     * For debugging only, we count how many times we've attempted to load
     * each class by name.  This tool will hopefully help us track down cases
     * when we are loading the same drivers for multiple connections that should
     * have been sharing the same driver classes.
     */
    private static Map<String, Integer> classLoadCounts = new HashMap<String, Integer>();
    
    /**
     * A special ClassLoader that searches the classpath associated with this
     * type of data source only. Each type of ArchitectDataSource should have
     * one of these class loaders, configured to search the database vendor's
     * jar/zip files.
     */
    public class JDBCClassLoader extends ClassLoader {

        protected JDBCClassLoader() {
            super(JDBCClassLoader.class.getClassLoader());

            logger.debug("Created new JDBC Classloader @"+System.identityHashCode(this));
            
            // classes loaded with this classloader need their own security policy,
            // because in WebStart, the allPermissions tag applies only to the
            // webstart classloader.
            // I found this code in a comment on the big ranch java saloon. It works!
            Policy.setPolicy( new Policy() {
                    public PermissionCollection
                        getPermissions(CodeSource codesource) {
                        Permissions perms = new Permissions();
                        perms.add(new AllPermission());
                        return(perms);
                    }
                    public void refresh(){
                        // no need to refresh
                    }
                });
        }
        
        /**
         * Searches the jar files listed by getJdbcJarList() for the
         * named class.  Throws ClassNotFoundException if the class can't
         * be located.
         */
        @Override
        public Class findClass(String name)
            throws ClassNotFoundException {
            
            if (logger.isDebugEnabled()) {
                Integer count = classLoadCounts.get(name);
                if (count == null) {
                    count = new Integer(1);
                } else {
                    count += 1;
                }
                classLoadCounts.put(name, count);
                logger.debug("JDBC Classloader @"+System.identityHashCode(this)+
                        ": Looking for class "+name+" (count = "+count+")");
            }

            for (String jarFileName : getJdbcJarList()) {
                try {
                    logger.debug("checking file "+jarFileName);
                    File listedFile = ArchitectUtils.jarSpecToFile(jarFileName, getParent());
                    if (listedFile == null || !listedFile.exists()) {
                        logger.debug("Skipping non-existant JAR file "+jarFileName);
                        continue;
                    }
                    JarFile jf = new JarFile(listedFile);
                    ZipEntry ent = jf.getEntry(name.replace('.','/')+".class");
                    if (ent == null) {
                        jf.close();
                        continue;
                    }
                    byte[] buf = new byte[(int) ent.getSize()];
                    InputStream is = jf.getInputStream(ent);
                    int offs = 0, n = 0;
                    while ( (n = is.read(buf, offs, buf.length-offs)) >= 0 && offs < buf.length) {
                        offs += n;
                    }
                    final int total = offs;
                    if (total != ent.getSize()) {
                        logger.warn("What gives?  ZipEntry "+ent.getName()+" is "+ent.getSize()+" bytes long, but we only read "+total+" bytes!");
                    }
                    jf.close();
                    return defineClass(name, buf, 0, buf.length);
                } catch (IOException ex) {
                    throw new ClassNotFoundException("IO Exception reading class from jar file", ex);
                }
            }
            throw new ClassNotFoundException
                ("Could not locate class "+name
                 +" in any of the JDBC Driver JAR files "+getJdbcJarList());
        }

        /**
         * Attempts to locate the named file in the same JAR files that
         * classes are loaded from.
         */
        @Override
        protected URL findResource(String name) {
            logger.debug("Looking for resource "+name);
            for (String jarName : getJdbcJarList()) {
                File listedFile = new File(jarName);
                try {
                    if (!listedFile.exists()) {
                        logger.debug("Skipping non-existant JAR file "+listedFile.getPath());
                        continue;
                    }
                    JarFile jf = new JarFile(listedFile);
                    if (jf.getEntry(name) != null) {
                        URI jarUri = listedFile.toURI();
                        return new URL("jar:"+jarUri.toURL()+"!/"+name);
                    }
                } catch (IOException ex) {
                    logger.warn("IO Exception while searching "+listedFile.getPath()
                                +" for resource "+name+". Continuing...", ex);
                }
            }
            return null;
        }
    }
    
    public static final String JDBC_DRIVER = "JDBC Driver Class";
    public static final String JDBC_URL = "JDBC URL";
    public static final String JDBC_JAR_BASE = "JDBC JAR";
    public static final String JDBC_JAR_COUNT = "JDBC JAR Count";
    public static final String TYPE_NAME = "Name";
    public static final String PARENT_TYPE_NAME = "Parent Type";
    public static final String PL_DB_TYPE = "PL Type";
    public static final String COMMENT = "Comment";
    public static final String DDL_GENERATOR = "DDL Generator";
    
    /**
     * This type's parent type.  This value will be null if this type has no
     * parent.
     */
    private ArchitectDataSourceType parentType;
    
    /**
     * This type's properties.  There are a set of property
     * names that we know what they mean, but instances of this class carry
     * all the property name=value pairs that get thrown at them so that
     * we don't end up deleting new properties when reading then saving over
     * a file with an older version of the app.
     */
    private Map<String,String> properties = new LinkedHashMap<String, String>();
    
    /**
     * The Class Loader that is responsible for finding and defining the JDBC
     * driver classes from the database vendor, for this connection type only.
     */
    private ClassLoader classLoader = new JDBCClassLoader();
    
    /**
     * Deletgate class for supporting the bound properties of this class.
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * Creates a new default data source type.
     */
    public ArchitectDataSourceType() {
        super();
    }
    
    public String getComment() {
        return getProperty(COMMENT);
    }
    
    public void setComment(String comment) {
        putPropertyImpl("comment", COMMENT, comment);
    }
    
    public String getPlDbType() {
        return getProperty(PL_DB_TYPE);
    }
    
    public void setPlDbType(String type) {
        putPropertyImpl("plDbType", PL_DB_TYPE, type);
    }
    
    public String getJdbcDriver() {
        return getProperty(JDBC_DRIVER);
    }
    
    public void setJdbcDriver(String jdbcDriver) {
        putPropertyImpl("jdbcDriver", JDBC_DRIVER, jdbcDriver);
    }
    
    /**
     * Returns an unmodifiable list of all the JAR file paths
     * associated with this data source type.  This list is not
     * guaranteed to stay up-to-date with additional jars that get
     * added to this ds type after you call this method.
     * <p>
     * @return An unmodifiable list of jar file pathnames.
     */
    public List<String> getJdbcJarList() {
        return Collections.unmodifiableList(makeModifiableJdbcJarList());
    }
    
    /**
     * Creates a list of the Jar files tracked by this data source type.  Although
     * the list is modifiable, it is your own independant copy of the jar list, and 
     * modifying it will have no effect on the actual list of jars tracked by this
     * instance.
     */
    private List<String> makeModifiableJdbcJarList() {
        int count = getJdbcJarCount();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            String key = JDBC_JAR_BASE+"_"+i;
            String value = getProperty(key);
            logger.debug("Found jar \""+value+"\" under key \""+key+"\"");
            list.add(value);
        }
        return list;
    }

    /**
     * Replaces the current list of JDBC driver jar files with a copy of the given list.
     * <p>
     * Warning: this method does not presently cause any events to be fired, although
     * a future revision hopefully will.
     */
    public void setJdbcJarList(List<String> jdbcJarList) {
        int count = jdbcJarList.size();
        setJdbcJarCount(count);
        int i = 0;
        for (String jar : jdbcJarList) {
            properties.put(JDBC_JAR_BASE+"_"+i, jar);
            i++;
        }
    }
    
    /**
     * Adds the JDBC driver jar path name to the internal list.
     * <p>
     * Warning: this method does not presently cause any events to be fired, although
     * a future revision hopefully will.
     */
    public void addJdbcJar(String jarPath) {
        int count = getJdbcJarCount();
        properties.put(JDBC_JAR_BASE+"_"+count, jarPath);
        setJdbcJarCount(count + 1);
    }

    private void setJdbcJarCount(int count) {
        putPropertyImpl("jdbcJarCount", JDBC_JAR_COUNT, String.valueOf(count));
    }

    private int getJdbcJarCount() {
        String jarCountString = getProperty(JDBC_JAR_COUNT);
        if (jarCountString == null) {
            return 0;
        } else {
            return Integer.parseInt(jarCountString);
        }
    }
    
    public String getJdbcUrl() {
        return getProperty(JDBC_URL);
    }
    
    public void setJdbcUrl(String jdbcUrl) {
        putPropertyImpl("jdbcUrl", JDBC_URL, jdbcUrl);
    }
    
    /**
     * For each property in the template, if the property has a default value
     * its property name and the default value will be put into the map otherwise
     * the property name and an empty string will be stored.
     */
    public Map<String, String> retrieveURLDefaults() {
        String template = getProperty(JDBC_URL);
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (template == null) return map;
        
        int searchFrom = 0;
        while (template.indexOf('<', searchFrom) >= 0) {
            int openBrace = template.indexOf('<', searchFrom);
            searchFrom = openBrace + 1;
            int colon = template.indexOf(':', searchFrom);
            int closeBrace = template.indexOf('>', searchFrom);
            if (colon >= 0 && colon < closeBrace) {
                map.put(template.substring(openBrace+1, colon), template.substring(colon+1, closeBrace));
            } else if (closeBrace >=0) {
                map.put(template.substring(openBrace+1, closeBrace), "");
            }
            searchFrom = closeBrace++;
        }
        return map;
    }
    
    /**
     * This method takes a url and matches it to the template pattern that is stored.
     * The returned map contains a key, value pair for each property in the template 
     * and the value itself.
     */
    public Map<String, String> retrieveURLParsing(String url) {
        String template = getProperty(JDBC_URL);
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (template == null) return map;
        String reTemplate = template.replaceAll("<.*?>", "(.*)");
        logger.debug("Regex of template is "+reTemplate);
        Pattern p = Pattern.compile(reTemplate);
        Matcher m = p.matcher(url);
        if (m.find()) {
            int searchFrom = 0;
            for (int g = 1; g <= m.groupCount(); g++) {
                int openBrace = template.indexOf('<', searchFrom);
                searchFrom = openBrace + 1;
                int colon = template.indexOf(':', searchFrom);
                int closeBrace = template.indexOf('>', searchFrom);
                if (colon >= 0 && colon < closeBrace) {
                    map.put(template.substring(openBrace+1, colon), m.group(g));
                } else if (closeBrace >=0) {
                    map.put(template.substring(openBrace+1, closeBrace), m.group(g));
                }
                searchFrom = closeBrace++;
            }
        }
        
        logger.debug("The map! dun dun dun: " + map.toString());

        return map;
    }
    
    public String getName() {
        return getProperty(TYPE_NAME);
    }
    
    public void setName(String name) {
        putPropertyImpl("name", TYPE_NAME, name);
    }

    public String getDDLGeneratorClass() {
        return getProperty(DDL_GENERATOR);
    }
    
    public void setDDLGeneratorClass(String className) {
        putPropertyImpl("DDLGeneratorClass", DDL_GENERATOR, className);
    }

    public ArchitectDataSourceType getParentType() {
        return parentType;
    }
    
    public void setParentType(ArchitectDataSourceType parentType) {
        this.parentType = parentType;
    }
    
    /**
     * Returns all the properties of this data source type.  This will not
     * include any inherited values, so unless you're trying to save this data source
     * to a file or something, you'd probably prefer to use one of the getter methods.
     * Also, you really, really shouldn't modify the map you get back.
     * 
     * @throws UnsupportedOperationException if you ignored our nice warning and tried
     * to modify the returned map.
     */
    Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    /**
     * Adds or replaces a value in the property map.
     */
    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public ClassLoader getJdbcClassLoader() {
        return classLoader;
    }
    
    /**
     * Gets a property from this data source type, checking with the supertype
     * when this type doesn't have a value for the requested property.
     * 
     * @param key The property name to look up. Null isn't allowed.
     */
    public String getProperty(String key) {
        String value = properties.get(key);
        if (value != null) {
            return value;
        } else if (parentType != null) {
            return parentType.getProperty(key);
        } else {
            return null;
        }
    }

    /**
     * Removes the jar file's path from the list of jar files.  Has no effect
     * if the named JAR file is not in the list.
     * 
     * @param path the path you want to remove
     */
    public void removeJdbcJar(String path) {
        List<String> jdbcJars = makeModifiableJdbcJarList();
        jdbcJars.remove(path);
        setJdbcJarList(jdbcJars);
    }
    
    @Override
    public String toString() {
        return "DataSourceType: "+properties;
    }

    /**
     * Checks that all prerequisites for making a connection to this type of database
     * have been met. Currently, this only checks that the driver class field is filled
     * in.
     * 
     * @throws SQLException if there are unmet prerequisites.  The exception message will
     * explain which prerequisite is not met.
     */
    public void checkConnectPrereqs() throws SQLException {
        if (getJdbcDriver() == null
                || getJdbcDriver().trim().length() == 0) {
            throw new SQLException("Data Source Type \""+getName()+"\" has no JDBC Driver class specified.");
        }
    }

    /**
     * Modifies the value of the named property, firing a change event if the
     * new value differs from the pre-existing one.
     * 
     * @param javaPropName The name of the JavaBeans property you are modifying
     * @param plPropName The name of PL.INI the property to set/update (this is also
     * the key in the in-memory properties map)
     * @param propValue The new value for the property
     */
    private void putPropertyImpl(String javaPropName, String plPropName, String propValue) {
        String oldValue = properties.get(plPropName);
        properties.put(plPropName, propValue);
        firePropertyChange(javaPropName, oldValue, propValue);
    }
    
    // ---------------- Methods that delegate to the PropertyChangeSupport -----------------
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
