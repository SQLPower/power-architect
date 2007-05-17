package ca.sqlpower.architect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataSourceCollection {

    public static final String DOS_CR_LF = "\r\n";

    /**
     * Reads the PL.INI file at the given location into a new fileSections list.
     * Also updates the fileTime.
     */
    public void read(File location) throws IOException;

    /**
     * Reads a PL.INI-style data stream into a new fileSections list.
     * Does not update the fileTime, or start a thread to reload the file
     * (since there isn't a file that we know of).
     */
    public void read(InputStream in) throws IOException;

    /**
     * Writes out the file as {@link #write(File)} does, using the
     * same location as the last file that was successfully read or
     * written using this instance.
     * 
     * @throws IOException if the file can't be written.
     * @throws IllegalStateException if the file to save to can't be determined. 
     */
    public void write() throws IOException;
    
    /**
     * Writes out every section in the fileSections list in the
     * order they appear in that list.
     *
     * @param location The location to write to.
     * @throws IOException if the location is not writeable for any reason.
     */
    public void write(File location) throws IOException;

    /**
     * Searches the list of connections for one with the given name.
     *
     * @param name The Logical datbabase name to look for.
     * @return the first ArchitectDataSource in the file whose name matches the
     * given name, or null if no such datasource exists.
     */
    public ArchitectDataSource getDataSource(String name);

    /**
     * @return a sorted List of all the data sources in this pl.ini.
     */
    public List<ArchitectDataSource> getConnections();

    public String toString();

    /**
     * Adds a new data source to the end of this file's list of sections.
     * Fires an add event.
     *
     * @param dbcs The new data source to add
     */
    public void addDataSource(ArchitectDataSource dbcs);

    /**
     * Make sure an ArchitectDataSource is in the master list; either copy its properties
     * to one with the same name found in the list, OR, add it to the list.
     * Matching is performed by logical name and is case insensitive.  If the data
     * source is added (rather than updated), there will be an add event.
     * @param dbcs
     */
    public void mergeDataSource(ArchitectDataSource dbcs);

    public void removeDataSource(ArchitectDataSource dbcs);

    /**
     * Returns an unmodifiable list of all data source types in this
     * collection of data sources.
     */
    public List<ArchitectDataSourceType> getDataSourceTypes();
    
    /**
     * Adds the new data source type to this collection.  See also
     * {@link #mergeDataSourceType(ArchitectDataSourceType)}
     * for a method that can update an existing entry.
     */
    void addDataSourceType(ArchitectDataSourceType dataSourceType);
    
    /**
     * Adds or updates the given data source type properties in this data source collection.
     * Matching is performed by name and is case insensitive.
     * @param dst
     */
    public void mergeDataSourceType(ArchitectDataSourceType dst);
    
    public void addDatabaseListChangeListener(DatabaseListChangeListener l);

    public void removeDatabaseListChangeListener(DatabaseListChangeListener l);


}