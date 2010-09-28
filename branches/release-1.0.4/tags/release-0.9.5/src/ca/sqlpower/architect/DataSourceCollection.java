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