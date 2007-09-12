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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
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
    private TableProfileManager profileManager;
    private CompareDMSettings compareDMSettings;
    private GenericDDLGenerator ddlGenerator;
    private KettleJob kettleJob;
    private RecentMenu recent;
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws ArchitectException {
        this.context = context;
        this.recent = new RecentMenu(this.getClass()) {
            @Override
            public void loadFile(String fileName) throws IOException {
                System.out.println("Fake load file for recent menu");
            }
        };
        profileManager = new TableProfileManager();
        project = new SwingUIProject(this);
        userSettings = context.getUserSettings();
        SQLDatabase ppdb = new SQLDatabase();
        playpen = new PlayPen(this, ppdb);
        List initialDBList = new ArrayList();
        initialDBList.add(playpen.getDatabase());
        sourceDatabases = new DBTree(this, initialDBList);
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
        // TODO Auto-generated method stub
        
    }

    public CompareDMSettings getCompareDMSettings() {
        return compareDMSettings;
    }

    public GenericDDLGenerator getDDLGenerator() {
        return ddlGenerator;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public PlayPen getPlayPen() {
        return playpen;
    }

    public JDialog getProfileDialog() {
        // TODO Auto-generated method stub
        return null;
    }

    public DBTree getSourceDatabases() {
        return sourceDatabases;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public boolean isSavingEntireSource() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setDDLGenerator(GenericDDLGenerator generator) {
        // TODO Auto-generated method stub
        
    }

    public void setName(String argName) {
        // TODO Auto-generated method stub
        
    }

    public void setSavingEntireSource(boolean argSavingEntireSource) {
        // TODO Auto-generated method stub
        
    }

    public void setSourceDatabaseList(List databases) throws ArchitectException {
        this.sourceDatabases.setModel(new DBTreeModel(databases,this));
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
}
