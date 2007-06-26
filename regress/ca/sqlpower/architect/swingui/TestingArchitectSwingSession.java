package ca.sqlpower.architect.swingui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.undo.UndoManager;

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
    private CreateKettleJob createKettleJob;
    
    public TestingArchitectSwingSession(ArchitectSwingSessionContext context) throws ArchitectException {
        this.context = context;
        profileManager = new TableProfileManager();
        project = new SwingUIProject(this);
        userSettings = context.getUserSettings();
        SQLDatabase ppdb = new SQLDatabase();
        playpen = new PlayPen(this, ppdb);
        List initialDBList = new ArrayList();
        initialDBList.add(playpen.getDatabase());
        sourceDatabases = new DBTree(this, initialDBList);
        undoManager = new UndoManager(playpen);
        frame = new ArchitectFrame(this, project);
        frame.init();
        compareDMSettings = new CompareDMSettings();
        try {
            ddlGenerator = new GenericDDLGenerator();
        } catch (SQLException e) {
            throw new ArchitectException("SQL Error in ddlGenerator",e);
        }
        createKettleJob = new CreateKettleJob();
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

    public CreateKettleJob getCreateKettleJob() {
        return createKettleJob;
    }

    public void setCreateKettleJobSettings(CreateKettleJob createKettleJobSettings) {
    }
}
