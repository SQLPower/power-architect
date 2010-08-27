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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.StarterPlatformTypes;
import ca.sqlpower.architect.ddl.critic.impl.AlphaNumericNameCritic;
import ca.sqlpower.architect.ddl.critic.impl.AlphaNumericSequenceNameCritic;
import ca.sqlpower.architect.ddl.critic.impl.DB2UnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.DuplicateNameCritic;
import ca.sqlpower.architect.ddl.critic.impl.EmptyRelationshipCritic;
import ca.sqlpower.architect.ddl.critic.impl.H2UnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.HSQLDBUnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.MySQLCommentCritic;
import ca.sqlpower.architect.ddl.critic.impl.MySQLReservedWordsCritic;
import ca.sqlpower.architect.ddl.critic.impl.MySQLUnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.OraclePhysicalNameCritic;
import ca.sqlpower.architect.ddl.critic.impl.OracleReservedWordsCritic;
import ca.sqlpower.architect.ddl.critic.impl.OracleUnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.PostgreSQLReservedWordsCritic;
import ca.sqlpower.architect.ddl.critic.impl.PrimaryKeyCritic;
import ca.sqlpower.architect.ddl.critic.impl.RelationshipMappingTypeCritic;
import ca.sqlpower.architect.ddl.critic.impl.SQLServer2000UnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.SQLServer2005UnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.SQLServerReservedWordsCritic;
import ca.sqlpower.architect.ddl.critic.impl.SQLServerUnsupportedFeaturesCritic;
import ca.sqlpower.architect.ddl.critic.impl.SetDefaultOnColumnWithNoDefaultCritic;
import ca.sqlpower.architect.ddl.critic.impl.SetNullOnNonNullableColumnCritic;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;

/**
 * A collection of settings that defines what critics are enabled in the system
 * and their settings to decide how to critique the object model.
 */
public class CriticManager extends AbstractSPObject {
    
    /**
     * Defines an absolute ordering of the child types of this class.
     * 
     * IMPORTANT!: When changing this, ensure you maintain the order specified by {@link #getChildren()}
     */
    public static final List<Class<? extends SPObject>> allowedChildTypes = 
        Collections.<Class<? extends SPObject>>singletonList(CriticGrouping.class);

    /**
     * These are the critics that the critic manager will start with when it is
     * first created.
     */
    private final List<CriticAndSettings> STARTING_CRITICS = 
        Collections.unmodifiableList(Arrays.asList(
                //generics
                new PrimaryKeyCritic(),
                new RelationshipMappingTypeCritic(),
                new EmptyRelationshipCritic(),
                new AlphaNumericNameCritic(),
                new AlphaNumericSequenceNameCritic(),
                new SetDefaultOnColumnWithNoDefaultCritic(),
                new SetNullOnNonNullableColumnCritic(),
                new DuplicateNameCritic(),
                //DB2
                new DB2UnsupportedFeaturesCritic(),
                //H2
                new H2UnsupportedFeaturesCritic(),
                //HSQLDB
                new HSQLDBUnsupportedFeaturesCritic(),
                //MySQL
                new MySQLCommentCritic(),
                new MySQLReservedWordsCritic(),
                new MySQLUnsupportedFeaturesCritic(),
                //Oracle
                new OraclePhysicalNameCritic(), 
                new OracleReservedWordsCritic(),
                new OracleUnsupportedFeaturesCritic(),
                //Postgres
                new PostgreSQLReservedWordsCritic(),
                //SQL Server
                new SQLServerUnsupportedFeaturesCritic(),
                new SQLServerReservedWordsCritic(),
                //SQL Server 2000
                new SQLServer2000UnsupportedFeaturesCritic(),
                //SQL Server 2005
                new SQLServer2005UnsupportedFeaturesCritic()
                ));

    /**
     * All of the critic groups known to this system.
     */
    private final List<CriticGrouping> criticGroupings = new ArrayList<CriticGrouping>();
    
    @Constructor
    public CriticManager() {
        setName("Critic Manager");
    }

    /**
     * Returns the list of starting critics. This list is final and will always
     * return the critics in the same order.
     */
    @Transient @Accessor
    public List<CriticAndSettings> getStartingCritics() {
        return STARTING_CRITICS;
    }

    /**
     * Call this method to register the critics that come by default with
     * Architect.
     */
    public void registerStartingCritics() {
        for (CriticAndSettings criticType : STARTING_CRITICS) {
            registerCritic(criticType);
        }
        loadDefaults();
    }

    /**
     * Used to add a critic to the manager. Once registered the critic can have
     * properties set on it to decide the error level and other properties. If
     * the critic has already been registered this will do nothing.
     * <p>
     * The {@link CriticFactory} must be able to make critics of this type to be
     * able to actually use the critics in practice.
     * 
     * @param criticClass
     *            The class that defines the critic. Will be used by the
     *            {@link CriticFactory} to create new critics.
     * @param platformType
     *            Normally one of the values defined in
     *            {@link StarterPlatformTypes} but can really be any value. It
     *            is used to group critics defined in the system logically.
     */
    public void registerCritic(CriticAndSettings critic) {
        for (CriticGrouping grouping : criticGroupings) {
            if (grouping.getPlatformType().equals(critic.getPlatformType())) {
                //already registered
                grouping.addChild(critic, grouping.getSettings().size());
                return;
            }
        }
        final CriticGrouping newGrouping = new CriticGrouping(critic.getPlatformType());
        addChild(newGrouping, criticGroupings.size());
        newGrouping.addChild(critic, 0);
    }
    
    /**
     * Do a full critique of all objects the critic manager currently knows
     * are important to validate. These criticisms are immutable after they
     * are created.
     */
    public List<Criticism> criticize() {
        return criticize(null);
    }

    /**
     * Returns a list of criticisms calculated by critics in this manager based
     * on the objects the manager knows to criticize. These criticisms are
     * immutable after they are created.
     * 
     * @param generatorClass
     *            The generator type we will be using to create DDL with. Will
     *            limit some of the enabled critics to only use critics
     *            associated with this DDL generators of this type. If null all
     *            enabled critics will be used.
     */
    public List<Criticism> criticize(Class<? extends DDLGenerator> generatorClass) {
        List<Criticism> criticisms = new ArrayList<Criticism>();
        criticisms.addAll(criticize(generatorClass, getParent().getTargetDatabase()));
        for (JDBCDataSourceType dsType : getParent().getSession().getDataSources().getDataSourceTypes()) {
            criticisms.addAll(criticize(generatorClass, dsType));
        }
        for (JDBCDataSource ds : getParent().getSession().getDataSources().getConnections()) {
            criticisms.addAll(criticize(generatorClass, ds));
        }
        return criticisms;
    }

    /**
     * Returns a list of criticisms calculated by critics in this manager based
     * on the object passed to them. These criticisms are immutable after they
     * are created.
     * 
     * @param generatorClass
     *            The generator type we will be using to create DDL with. Will
     *            limit some of the enabled critics to only use critics
     *            associated with this DDL generators of this type. If null all
     *            enabled critics will be used.
     */
    private List<Criticism> criticize(Class<? extends DDLGenerator> generatorClass, Object root) {
        List<Critic> critics = new ArrayList<Critic>();
        for (CriticGrouping grouping : criticGroupings) {
            if (!grouping.isEnabled()) continue;
            final StarterPlatformTypes starterTypeByGroupName = StarterPlatformTypes.getByGroupName(grouping.getPlatformType());
            if (generatorClass != null && starterTypeByGroupName != null && 
                    !starterTypeByGroupName.isAssociated(generatorClass)) continue;
            for (CriticAndSettings singleSettings : grouping.getSettings()) {
                if (Severity.IGNORE.equals(singleSettings.getSeverity())) continue;
                critics.add(singleSettings);
            }
        }
        Criticizer criticizer = new Criticizer(critics);
        return Collections.unmodifiableList(criticizer.criticize(root));
        
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof CriticGrouping) {
            final CriticGrouping critic = (CriticGrouping) child;
            criticGroupings.add(index, critic);
            critic.setParent(this);
            fireChildAdded(CriticGrouping.class, child, index);
        } else {
            throw new IllegalStateException("Invalid child type " + child);
        }
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        int index = criticGroupings.indexOf(child);
        boolean removed = criticGroupings.remove(child);
        if (removed) {
            fireChildRemoved(CriticAndSettings.class, child, index);
        }
        return removed;
    }

    @Transient @Accessor
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
        return allowedChildTypes;
    }

    @NonProperty
    public List<? extends SPObject> getChildren() {
        return Collections.unmodifiableList(criticGroupings);
    }

    @NonBound
    public List<? extends SPObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(SPObject dependency) {
        for (CriticGrouping grouping : criticGroupings) {
            grouping.removeDependency(dependency);
        }
    }
    
    @NonProperty
    public List<CriticGrouping> getCriticGroupings() {
        return Collections.unmodifiableList(criticGroupings);
    }

    /**
     * Clears all registered critics and their groups from the manager.
     */
    public void clear() {
        try {
            begin("Clearing manager");
            for (int i = criticGroupings.size() - 1; i >= 0; i--) {
                removeChild(criticGroupings.get(i));
            }
            commit();
        } catch (Throwable t) {
            rollback(t.getMessage());
            throw new RuntimeException(t);
        }
    }
    
    @Mutator
    @Override
    public void setParent(SPObject parent) {
        if (!(parent instanceof ArchitectProject)) {
            throw new IllegalArgumentException("The parent of a critic manager must be some " +
            		"kind of architect project.");
        }
        super.setParent(parent);
    }
    
    @Accessor
    @Override
    public ArchitectProject getParent() {
        return (ArchitectProject) super.getParent();
    }

    /**
     * Call this method to save the current settings of this manager to be the
     * default setup of the critic manager on new projects.
     * <p>
     * Changes to this method must match {@link #loadDefaults()}.
     * 
     * @throws BackingStoreException
     *             If there is an exception with Java Prefs.
     */
    public void saveAsDefaults() throws BackingStoreException {
        Preferences prefs = Preferences.userNodeForPackage(CriticManager.class);
        prefs.clear();
        for (CriticGrouping group : getCriticGroupings()) {
            prefs.putBoolean(CriticGrouping.class.getSimpleName() + "." + group.getPlatformType() + ".enabled", group.isEnabled());
            //Currently only one instance of each critic type exists. This may change
            //in the future in which case we will need a different identifier other than
            //its class or name.
            for (CriticAndSettings settings : group.getSettings()) {
                prefs.put(settings.getClass().getSimpleName() + ".severity", settings.getSeverity().name());
            }
        }
        prefs.flush();
    }
    
    /**
     * Call this method to load the current settings stored in prefs to be the
     * default settings of this critic manager.
     * <p>
     * Changes to this method must match {@link #saveAsDefaults()}.
     */
    public void loadDefaults() {
        Preferences prefs = Preferences.userNodeForPackage(CriticManager.class);
        for (CriticGrouping group : getCriticGroupings()) {
            group.setEnabled(prefs.getBoolean(CriticGrouping.class.getSimpleName() + "." + group.getPlatformType() + ".enabled", true));
            for (CriticAndSettings settings : group.getSettings()) {
                String severity = prefs.get(settings.getClass().getSimpleName() + ".severity", Severity.ERROR.name());
                settings.setSeverity(Severity.valueOf(severity));
            }
        }
    }
}
