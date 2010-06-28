/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.ddl.critic;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Mutator;

/**
 * The settings of a specific {@link Critic}. Includes if the critic is enabled
 * and any additional settings to decide how to criticize the object model.
 */
public abstract class CriticAndSettings extends AbstractSPObject implements Critic {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = Collections.emptyList();

    public enum Severity {
        ERROR,
        WARNING,
        IGNORE
    }
    
    /**
     * Some of the types used in our critics. Any of the critics written in a class
     * should use one of these platform types but there is no real need to restrict
     * user-defined platform types to these types in the future.
     */
    public enum StarterPlatformTypes {
        GENERIC("Generic"),
        POSTGRESQL("PostgreSQL"),
        MY_SQL("MySQL"),
        SQL_SERVER("SQL Server"),
        SQL_SERVER_2000("SQL Server 2000"),
        SQL_SERVER_2005("SQL Server 2005"),
        ORACLE("Oracle"),
        DB2("DB2"),
        H2("H2"),
        HSQLDB("HSQLDB");
        
        private final String name;

        private StarterPlatformTypes(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * Decides what level of error to display on objects.
     */
    private Severity severity;

    /**
     * Critics will be grouped by this value. While this is normally a platform
     * type it does not necessarily have to be restricted as such. This type
     * is normally from {@link StarterPlatformTypes}.
     */
    private final String platformType;

    /**
     * @param platformType
     *            A string that will group critics together. This is normally a
     *            platform type name and can come from one of the
     *            {@link StarterPlatformTypes}.
     * @param name
     *            A short name of the critic. Should give the user an idea of
     *            what the critic will warn the user about.
     */
    @Constructor
    public CriticAndSettings(
            @ConstructorParameter(propertyName="platformType") String platformType,
            @ConstructorParameter(propertyName="name") String name) {
        this.platformType = platformType;
        severity = Severity.ERROR;
        setName(name);
    }
    
    @Mutator
    public void setSeverity(Severity severity) {
        Severity oldSeverity = this.severity;
        this.severity = severity;
        firePropertyChange("severity", oldSeverity, severity);
    }

    @Accessor
    public Severity getSeverity() {
        return severity;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        return 0;
    }

    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public List<? extends SPObject> getChildren() {
        return Collections.emptyList();
    }

    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        //do nothing
    }

    @Accessor
    public String getPlatformType() {
        return platformType;
    }
    
    
}
