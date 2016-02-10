/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.etl.kettle;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.sql.JDBCDataSource;

public class KettleSettings extends AbstractSPObject {

    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List <Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();
    
    /**
     * The name of the Kettle job
     */
    private String jobName = "";
    
    /**
     * The name of the target schema
     */
    private String schemaName = "";
    
    /**
     * The path to store the Kettle job at
     */
    private String filePath = "";
    
    /**
     * The default join type for Kettle. The join types are stored as int as the values
     * are in an array in Kettle.
     */
    private int joinType = 0;
    
    /**
     * The SPDataSource representation of the database with the Kettle repository we want to save to
     */
    private JDBCDataSource repository;
    
    /**
     * A flag that determines whether we will save the job to an xml file or a Kettle repository 
     */
    private boolean savingToFile = true;
    
    @Constructor
    public KettleSettings() {
        setName("Kettle Settings");
    }
    
    @Accessor
    public String getJobName() {
        return jobName;
    }

    @Mutator
    public void setJobName(String v) {
        String oldJobName = jobName;
        jobName = v;
        firePropertyChange("jobName", oldJobName, jobName);
    }

    @Accessor
    public String getSchemaName() {
        return schemaName;
    }

    @Mutator
    public void setSchemaName(String v) {
        String oldSchemaName = schemaName;
        schemaName = v;
        firePropertyChange("schemaName", oldSchemaName, schemaName);
    }

    @Accessor
    public String getFilePath() {
        return filePath;
    }

    @Mutator
    public void setFilePath(String v) {
        String oldFilePath = filePath;
        filePath = v;
        firePropertyChange("filePath", oldFilePath, filePath);
    }

    @Accessor
    public int getJoinType() {
        return joinType;
    }

    @Mutator
    public void setJoinType(int v) {
        int oldJoinType = joinType;
        joinType = v;
        firePropertyChange("joinType", oldJoinType, joinType);
    }

    @Accessor
    public JDBCDataSource getRepository() {
        return repository;
    }

    @Mutator
    public void setRepository(JDBCDataSource v) {
        JDBCDataSource oldRepository = repository;
        repository = v;
        firePropertyChange("repository", oldRepository, repository);
    }

    @Accessor
    public boolean isSavingToFile() {
        return savingToFile;
    }

    @Mutator
    public void setSavingToFile(boolean v) {
        boolean oldSavingToFile = savingToFile;
        savingToFile = v;
        firePropertyChange("savingToFile", oldSavingToFile, savingToFile);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
    }
}
