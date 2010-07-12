/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JScrollPane;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.ProjectLoader;
import ca.sqlpower.architect.ProjectSettings;
import ca.sqlpower.architect.ProjectSettings.ColumnVisibility;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.DomainCategory;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.undo.ArchitectUndoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

/**
 * Minimally functional session implementation that creates but does
 * not show the architect frame, and has an empty project to start with.
 */
public class TestingArchitectSwingSession implements ArchitectSwingSession {

    private CoreUserSettings userSettings;
    private ArchitectSwingSessionContext context;
    private SwingUIProjectLoader project;
    private ArchitectFrame frame;
    private PlayPen playpen;
    private ArchitectUndoManager undoManager;
    private DBTree sourceDatabases;
    private CompareDMSettings compareDMSettings;
	private LiquibaseSettings liquibaseSettings;
    private DDLGenerator ddlGenerator;
    private KettleJob kettleJob;
    private RecentMenu recent;
    private ArchitectSession delegateSession;
    private OLAPRootObject olapRootObject;
    private PrintSettings printSettings;
    
    private boolean showPkTag = true;
    private boolean showFkTag = true;
    private boolean showAkTag = true;
    
    private boolean displayRelationshipLabel = false;
    private boolean usingLogicalNames = false;
    
    private ColumnVisibility choice = ColumnVisibility.ALL;
    private UserDefinedSQLType testType1;
    private UserDefinedSQLType testType2;
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws SQLObjectException {
        this.context = context;
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                System.out.println("Fake load file for recent menu");
            }
        };
        this.delegateSession = new ArchitectSessionImpl(context, "test", new ArchitectSwingProject());
        getWorkspace().setSession(this);
        project = new SwingUIProjectLoader(this);
        userSettings = context.getUserSettings();
        sourceDatabases = new DBTree(this);
        playpen = RelationalPlayPenFactory.createPlayPen(this, sourceDatabases);
        getWorkspace().setPlayPenContentPane(playpen.getContentPane());
        undoManager = new ArchitectUndoManager(playpen);
        olapRootObject = new OLAPRootObject();
        
        compareDMSettings = new CompareDMSettings();
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new SQLObjectException("SQL Error in ddlGenerator",e);
        }

        frame = new ArchitectFrame(context, null);
        frame.init(this, false);
        
        kettleJob = new KettleJob(this);
        
        printSettings = new PrintSettings();
        
        testType1 = new UserDefinedSQLType();
        String platform = SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM;
        testType1.setName("Test Type 1");
        testType1.setPrecision(platform, 1);
        testType1.setScale(platform, 1);
        testType1.setType(4);
        testType1.setMyNullability(DatabaseMetaData.columnNoNulls);
        testType1.setMyAutoIncrement(false);
        testType1.setPrecisionType(platform, PropertyType.VARIABLE);
        testType1.setScaleType(platform, PropertyType.VARIABLE);
        testType1.setUUID("test-type-1-uuid");
        
        testType2 = new UserDefinedSQLType();
        testType2.setName("Test Type 2");
        testType2.setPrecision(platform, 1);
        testType2.setScale(platform, 1);
        testType2.setType(12);
        testType2.setMyNullability(DatabaseMetaData.columnNoNulls);
        testType2.setMyAutoIncrement(false);
        testType2.setPrecisionType(platform, PropertyType.VARIABLE);
        testType2.setScaleType(platform, PropertyType.VARIABLE);
        testType2.setUUID("test-type-2-uuid");
    }
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context, SwingUIProjectLoader project) throws SQLObjectException {
        this(context);
    }
    
    public ArchitectFrame getArchitectFrame() {
        return frame;
    }

    public ArchitectSwingSessionContext getContext() {
        return context;
    }

    public RecentMenu getRecentMenu() {
        return recent;
    }
    
    public SwingUIProjectLoader getProjectLoader() {
        return project;
    }

    public void setProject(SwingUIProjectLoader project) {
        this.project = project;
    }
    
    public ProfileManager getProfileManager() {
        return delegateSession.getProfileManager();
    }

    public CoreUserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(CoreUserSettings argUserSettings) {
        userSettings = argUserSettings;
    }

    public boolean close() {
        return true;
    }

	public LiquibaseSettings getLiquibaseSettings() {
		return liquibaseSettings;
	}

	public void setLiquibaseSettings(LiquibaseSettings settings) {
		liquibaseSettings = settings;
	}

    public CompareDMSettings getCompareDMSettings() {
        return compareDMSettings;
    }

    public DDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }

    public String getName() {
        return null;
    }

    public PlayPen getPlayPen() {
        return playpen;
    }

    public JDialog getProfileDialog() {
        return null;
    }

    public DBTree getDBTree() {
        return sourceDatabases;
    }

    public ArchitectUndoManager getUndoManager() {
        return undoManager;
    }

    public boolean isSavingEntireSource() {
        return false;
    }

    public void setDDLGenerator(DDLGenerator generator) {
    }

    public void setName(String argName) {
    }

    public void setSavingEntireSource(boolean argSavingEntireSource) {
    }

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws SQLObjectException {
        delegateSession.setSourceDatabaseList(databases);
    }

    public KettleJob getKettleJob() {
        return kettleJob;
    }

    public void setKettleJob(KettleJob kettleJob) {
    }

    public void initGUI(ArchitectFrame frame) {
        // no-op
    }

    public void registerSwingWorker(SPSwingWorker worker) {
        
    }

    public void removeSwingWorker(SPSwingWorker worker) {
        
    }

    /**
     * Doesn't actually save the project!
     */
    public boolean saveOrSaveAs(boolean showChooser, boolean separateThread) {
        return false;
    }
    
    public boolean isNew() {
        return false;
    }

    /**
     * Always returns false.
     */
    public boolean getRelationshipLinesDirect() {
        return false;
    }

    /**
     * Does nothing.
     */
    public void setRelationshipLinesDirect(boolean direct) {
        // ignore
    }

    public SQLDatabase getTargetDatabase() {
        return delegateSession.getTargetDatabase();
    }

    public void setProjectLoader(ProjectLoader project) {
        delegateSession.setProjectLoader(project);
    }

    public SQLObjectRoot getRootObject() {
        return delegateSession.getRootObject();
    }

    public boolean isShowPkTag() {
        return showPkTag;
    }

    public void setShowPkTag(boolean showPkTag) {
        this.showPkTag = showPkTag;
        for (TablePane tp : getPlayPen().getContentPane().getChildren(TablePane.class)) {
            tp.revalidate();
        }
    }

    public boolean isShowFkTag() {
        return showFkTag;
    }

    public void setShowFkTag(boolean showFkTag) {
        this.showFkTag = showFkTag;
        for (TablePane tp : getPlayPen().getContentPane().getChildren(TablePane.class)) {
            tp.revalidate();
        }
    }

    public boolean isShowAkTag() {
        return showAkTag;
    }

    public void setShowAkTag(boolean showAkTag) {
        this.showAkTag = showAkTag;
        for (TablePane tp : getPlayPen().getContentPane().getChildren(TablePane.class)) {
            tp.revalidate();
        }
    }
    
    public void setColumnVisibility(ColumnVisibility option) {
        choice = option;
    }
    
    public  ColumnVisibility getColumnVisibility() {
        return choice;
    }
    
    public OLAPRootObject getOLAPRootObject() {
        return olapRootObject;
    }

    public void showOLAPSchemaManager(Window owner) {
    }

    public List<OLAPEditSession> getOLAPEditSessions() {
        return Collections.emptyList();
    }
    
    public OLAPEditSession getOLAPEditSession(OLAPSession olapSession) {
        return null;
    }

    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> listener) {
        // do-nothing stub
    }
    
    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSession> listener) {
        // do-nothing stub
    }

    public JMenu createDataSourcesMenu() {
        return new JMenu();
    }

    public PrintSettings getPrintSettings() {
        return printSettings;
    }

    public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType, UserPromptResponse defaultResponseType,
            Object defaultResponse, String ... buttonNames) {
        return new DefaultUserPrompterFactory().createUserPrompter(question, responseType, optionType, defaultResponseType, defaultResponse, buttonNames);
    }

    public boolean isUsingLogicalNames() {
       return usingLogicalNames;
    }

    public void setUsingLogicalNames(boolean usingLogicalNames) {
        this.usingLogicalNames = usingLogicalNames;
    }
    
    public boolean isDisplayRelationshipLabel() {
        return displayRelationshipLabel;
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDisplayRelationshipLabel(boolean displayRelationshipLabel) {
        this.displayRelationshipLabel = displayRelationshipLabel;
    }

    public Color getCustomColour(Color foregroundColor) {
        // TODO Auto-generated method stub
        return null;
    }

    public UserPrompter createDatabaseUserPrompter(String question, List<Class<? extends SPDataSource>> dsTypes,
            UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
            DataSourceCollection<SPDataSource> dsCollection, String... buttonNames) {
        // TODO Auto-generated method stub
        return new DefaultUserPrompterFactory().createDatabaseUserPrompter(question, dsTypes,
                optionType, defaultResponseType, defaultResponse, dsCollection, buttonNames);
    }

    public ArchitectSwingProject getWorkspace() {
        return (ArchitectSwingProject) delegateSession.getWorkspace();
    }

    public boolean isForegroundThread() {
        return true;
    }

    public void runInBackground(Runnable runner) {
        runner.run();
    }
    
    public void runInBackground(Runnable runner, String threadName) {
        runner.run();
    }
    
    public void runInForeground(Runnable runner) {
        runner.run();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // TODO Auto-generated method stub
        
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        // TODO Auto-generated method stub
        
    }

    public boolean isEnterpriseSession() {
        // TODO Auto-generated method stub
        return false;
    }

    public ArchitectClientSideSession getEnterpriseSession() {
        // TODO Auto-generated method stub
        return null;
    }

    public DataSourceCollection<JDBCDataSource> getDataSources() {
        return context.getPlDotIni();
    }

    public void showConnectionManager(Window owner) {
        // TODO Auto-generated method stub
        
    }

    public void showPreferenceDialog(Window owner) {
        // TODO Auto-generated method stub
        
    }

    public ProjectSettings getProjectSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<UserDefinedSQLType> getSQLTypes() {
        List<UserDefinedSQLType> list = new ArrayList<UserDefinedSQLType>();
        list.add(testType1);
        list.add(testType2);
        return list;
    }
    
    public UserDefinedSQLType findSQLTypeByUUID(String uuid) {
        if (testType1.getUUID().equals(uuid)) {
            return testType1;
        } else if (testType2.getUUID().equals(uuid)) {
            return testType2;
        } else {
            return null;
        }
    }
    
    public UserDefinedSQLType findSQLTypeByJDBCType(int type) {
        if (testType1.getType() == type) {
            return testType1;
        } else if (testType2.getType() == type) {
            return testType2;
        } else {
            return null;
        }
    }

    public <T> UserPrompter createListUserPrompter(String question, List<T> responses, T defaultResponse) {
        return new DefaultUserPrompterFactory().createListUserPrompter(question, responses, defaultResponse);
    }

    public List<DomainCategory> getDomainCategories() {
        return Collections.emptyList();
    }

    public JScrollPane getPlayPenScrollPane() {
        // TODO Auto-generated method stub
        return null;
    }

    public JComponent getProjectPanel() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPlayPenScrollPane(JScrollPane ppScrollPane) {
        // TODO Auto-generated method stub
        
    }

    public void setProjectPanel(JComponent panel) {
        // TODO Auto-generated method stub
        
    }
}
