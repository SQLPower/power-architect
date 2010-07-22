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

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ddl.DB2DDLGenerator;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.H2DDLGenerator;
import ca.sqlpower.architect.ddl.HSQLDBDDLGenerator;
import ca.sqlpower.architect.ddl.MySqlDDLGenerator;
import ca.sqlpower.architect.ddl.OracleDDLGenerator;
import ca.sqlpower.architect.ddl.PostgresDDLGenerator;
import ca.sqlpower.architect.ddl.SQLServer2000DDLGenerator;
import ca.sqlpower.architect.ddl.SQLServer2005DDLGenerator;
import ca.sqlpower.architect.ddl.SQLServerDDLGenerator;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
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
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
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
        GENERIC("Generic", DDLGenerator.class),
        POSTGRESQL("PostgreSQL", PostgresDDLGenerator.class),
        MY_SQL("MySQL", MySqlDDLGenerator.class),
        SQL_SERVER("SQL Server", SQLServerDDLGenerator.class),
        SQL_SERVER_2000("SQL Server 2000", SQLServer2000DDLGenerator.class),
        SQL_SERVER_2005("SQL Server 2005", SQLServer2005DDLGenerator.class),
        ORACLE("Oracle", OracleDDLGenerator.class),
        DB2("DB2", DB2DDLGenerator.class),
        H2("H2", H2DDLGenerator.class),
        HSQLDB("HSQLDB", HSQLDBDDLGenerator.class),
        /**
         * All configuration critics can belong to this group. These errors
         * are in a special class because they are not model specific and
         * you may have errors when connecting or forward engineering.
         * Configuration critics also should not be set to a warning.
         */
        CONFIGURATION("Configuration", DDLGenerator.class);

        /**
         * Human readable group name of the platform type which the critics using will
         * be grouped by.
         */
        private final String name;

        /**
         * DDLGenerators associated with the platform. Some critic groups may
         * only be meant to be executed if you are looking at forward
         * engineering to a specific platform. If the group is only meant for a
         * specific set of DDL generators provide their classes or a super class
         * of only those types. If you want all of the platforms to be
         * associated with the set of critics use the interface.
         */
        private final Class<? extends DDLGenerator>[] associatedGenerators;

        private StarterPlatformTypes(String name, Class<? extends DDLGenerator> ... associatedGenerators) {
            this.name = name;
            this.associatedGenerators = associatedGenerators;
        }
        
        public String getName() {
            return name;
        }

        /**
         * Finds the starter platform type by name. Not all critic group names
         * have to be one of the starter types so this may be null.
         */
        public static StarterPlatformTypes getByGroupName(String name) {
            for (StarterPlatformTypes type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * Returns true if this group is associated with the given generator
         * class. (ie, if it is or extends a class that is connected to this
         * group.)
         */
        public boolean isAssociated(Class<? extends DDLGenerator> associatedGenerator) {
            for (Class<? extends DDLGenerator> generatorClass : associatedGenerators) {
                if (generatorClass.isAssignableFrom(associatedGenerator)) {
                    return true;
                }
            }
            return false;
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
     * If true the critic has been started and is not allowed to be started
     * again until it has been ended.
     */
    private boolean started;

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

    /**
     * Most critics need to do nothing in terms of state. Only select critics
     * should ever need to override this method.
     */
    public void start() {
        if (started) throw new IllegalStateException("The critic " + getName() + 
                " has been started already.");
        started = true;
    }
    
    /**
     * Most critics need to do nothing in terms of state. Only select critics
     * should ever need to override this method.
     */
    public void end() {
        started = false;
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
    
    @Accessor
    @Override
    public CriticGrouping getParent() {
        return (CriticGrouping) super.getParent();
    }
    
    @Mutator
    @Override
    public void setParent(SPObject parent) {
        if (parent != null && !(parent instanceof CriticGrouping)) {
            throw new IllegalArgumentException("The parent of a critic must be a CriticGrouping.");
        }
        super.setParent(parent);
    }
    
    /**
     * Walks up the tree to the session and returns it as a swing session. The
     * critic system only exists in the swing version of the Architect as it is
     * not core functionality.
     * 
     * @return The session as a swing session if the critic is connected to a
     *         session. This may return null if the critic is not connected.
     */
    protected ArchitectSwingSession getSession() {
        CriticGrouping grouping = getParent();
        if (grouping == null) return null;
        CriticManager manager = grouping.getParent();
        if (manager == null) return null;
        ArchitectProject project = manager.getParent();
        if (project == null) return null;
        ArchitectSwingSession session = (ArchitectSwingSession) project.getSession();
        return session;
    }

}
