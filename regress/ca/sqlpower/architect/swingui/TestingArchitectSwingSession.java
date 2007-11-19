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
package ca.sqlpower.architect.swingui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreProject;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.TableProfileManager;
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
    private TableProfileManager profileManager;
    private CompareDMSettings compareDMSettings;
    private DDLGenerator ddlGenerator;
    private KettleJob kettleJob;
    private RecentMenu recent;
    private ArchitectSession delegateSession;
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws ArchitectException {
        this.context = context;
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                System.out.println("Fake load file for recent menu");
            }
        };
        this.delegateSession = new ArchitectSessionImpl(context, "test");
        profileManager = new TableProfileManager();
        project = new SwingUIProject(this);
        userSettings = context.getUserSettings();
        playpen = new PlayPen(this);
        rootObject = new SQLObjectRoot();
        rootObject.addChild(getTargetDatabase());
        sourceDatabases = new DBTree(this);
        undoManager = new UndoManager(playpen);
        
        compareDMSettings = new CompareDMSettings();
        
        frame = new ArchitectFrame(this, project);
        frame.init();
        
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new ArchitectException("SQL Error in ddlGenerator",e);
        }
        kettleJob = new KettleJob();
        
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
}
