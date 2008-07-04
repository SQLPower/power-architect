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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.AlwaysOKUserPrompter;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreProject;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.UserPrompter;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * Minimally functional session implementation that creates but does
 * not show the architect frame, and has an empty project to start with.
 */
public class TestingArchitectSwingSession implements ArchitectSwingSession {

    private CoreUserSettings userSettings;
    private ArchitectSwingSessionContext context;
    private SwingUIProject project;
    private ArchitectFrame frame;
    private PlayPen playpen;
    private UndoManager undoManager;
    private DBTree sourceDatabases;
    private SQLObjectRoot rootObject;
    private ProfileManager profileManager;
    private CompareDMSettings compareDMSettings;
    private DDLGenerator ddlGenerator;
    private KettleJob kettleJob;
    private RecentMenu recent;
    private ArchitectSession delegateSession;
    
    private boolean showPkTag = true;
    private boolean showFkTag = true;
    private boolean showAkTag = true;
    
    protected boolean showPrimary = true;
    protected boolean showForeign = true;
    protected boolean showIndexed = true;
    protected boolean showUnique = true;
    protected boolean showTheRest = true;
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws ArchitectException {
        this.context = context;
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                System.out.println("Fake load file for recent menu");
            }
        };
        this.delegateSession = new ArchitectSessionImpl(context, "test");
        profileManager = new ProfileManagerImpl(this);
        project = new SwingUIProject(this);
        userSettings = context.getUserSettings();
        rootObject = new SQLObjectRoot();
        playpen = new PlayPen(this);
        rootObject.addChild(getTargetDatabase());
        sourceDatabases = new DBTree(this);
        undoManager = new UndoManager(playpen);
        
        compareDMSettings = new CompareDMSettings();
        
        frame = new ArchitectFrame(this, null);
        frame.init();
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new ArchitectException("SQL Error in ddlGenerator",e);
        }
        kettleJob = new KettleJob(this);
        
    }
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context, SwingUIProject project) throws ArchitectException {
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
    
    public SwingUIProject getProject() {
        return project;
    }

    public void setProject(SwingUIProject project) {
        this.project = project;
    }
    
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public CoreUserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(CoreUserSettings argUserSettings) {
        userSettings = argUserSettings;
    }

    public void close() {
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

    public UndoManager getUndoManager() {
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

    public void setSourceDatabaseList(List<SQLDatabase> databases) throws ArchitectException {
        while (rootObject.getChildCount() > 0) {
            rootObject.removeChild(rootObject.getChildCount() - 1);
        }
        
        for (SQLDatabase db : databases) {
            rootObject.addChild(db);
        }
    }

    public KettleJob getKettleJob() {
        return kettleJob;
    }

    public void setKettleJob(KettleJob kettleJob) {
    }

    public void initGUI() throws ArchitectException {
        throw new UnsupportedOperationException("Testing session impl doesn't make GUIs");
    }

    public void initGUI(ArchitectSwingSession session) throws ArchitectException {
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

    public void setProject(CoreProject project) {
        delegateSession.setProject(project);
    }

    public SQLObjectRoot getRootObject() {
        return rootObject;
    }

    public UserPrompter createUserPrompter(String question, String okText, String notOkText, String cancelText) {
        return new AlwaysOKUserPrompter();
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
    
    public boolean isShowPrimary() {
        return showPrimary;
    }

    public void setShowPrimary(boolean showPrimary) {
        this.showPrimary = showPrimary;
    }

    public boolean isShowForeign() {
        return showForeign;
    }

    public void setShowForeign(boolean showForeign) {
        this.showForeign = showForeign;
    }

    public boolean isShowIndexed() {
        return showIndexed;
    }

    public void setShowIndexed(boolean showIndexed) {
        this.showIndexed = showIndexed;
    }

    public boolean isShowUnique() {
        return showUnique;
    }

    public void setShowUnique(boolean showUnique) {
        this.showUnique = showUnique;
    }

    public boolean isShowTheRest() {
        return showTheRest;
    }

    public void setShowTheRest(boolean showTheRest) {
        this.showTheRest = showTheRest;
    }


}
