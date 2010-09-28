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
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenu;

import ca.sqlpower.architect.ArchitectProject;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.ProjectLoader;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl.ColumnVisibility;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.undo.ArchitectUndoManager;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
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
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws SQLObjectException {
        this.context = context;
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                System.out.println("Fake load file for recent menu");
            }
        };
        this.delegateSession = new ArchitectSessionImpl(context, "test");
        delegateSession.getWorkspace().setSession(this);
        project = new SwingUIProjectLoader(this);
        userSettings = context.getUserSettings();
        sourceDatabases = new DBTree(this);
        playpen = RelationalPlayPenFactory.createPlayPen(this, sourceDatabases);
        undoManager = new ArchitectUndoManager(playpen);
        olapRootObject = new OLAPRootObject(delegateSession);
        
        compareDMSettings = new CompareDMSettings();
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new SQLObjectException("SQL Error in ddlGenerator",e);
        }

        frame = new ArchitectFrame(this, null);
        frame.init();
        
        kettleJob = new KettleJob(this);
        
        printSettings = new PrintSettings();
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

    public DBTree getSourceDatabases() {
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

    public void initGUI() throws SQLObjectException {
        throw new UnsupportedOperationException("Testing session impl doesn't make GUIs");
    }

    public void initGUI(ArchitectSwingSession session) throws SQLObjectException {
        throw new UnsupportedOperationException("Testing session impl doesn't make GUIs");
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
        for (TablePane tp : getPlayPen().getTablePanes()) {
            tp.revalidate();
        }
    }

    public boolean isShowFkTag() {
        return showFkTag;
    }

    public void setShowFkTag(boolean showFkTag) {
        this.showFkTag = showFkTag;
        for (TablePane tp : getPlayPen().getTablePanes()) {
            tp.revalidate();
        }
    }

    public boolean isShowAkTag() {
        return showAkTag;
    }

    public void setShowAkTag(boolean showAkTag) {
        this.showAkTag = showAkTag;
        for (TablePane tp : getPlayPen().getTablePanes()) {
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

    public ArchitectProject getWorkspace() {
        // TODO Auto-generated method stub
        return null;
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
}
