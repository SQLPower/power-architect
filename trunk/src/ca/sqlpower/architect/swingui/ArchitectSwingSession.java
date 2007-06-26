package ca.sqlpower.architect.swingui;

import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.undo.UndoManager;

/**
 * The ArchitectSwingSession interface provides methods that are applicable
 * to a Swing UI invocation of the Architect.  It extends the ArchitectSession
 * interface, which provides information about a session with the core (non-UI specific)
 * objects.
 */
public interface ArchitectSwingSession extends ArchitectSession {

    /**
     * Returns the project associated with this session.  The project
     * holds the playpen objects, and can save and load itself in an
     * XML format.
     */
    public SwingUIProject getProject();

    /**
     * Returns the context that created this session.
     */
    public ArchitectSwingSessionContext getContext();

    /**
     * Returns the ArchitectFrame created in this session. 
     */
    public ArchitectFrame getArchitectFrame();
    
    /**
     * Gets the value of sourceDatabases
     *
     * @return the value of sourceDatabases
     */
    public DBTree getSourceDatabases();
    
    /**
     * Gets the value of playPen
     *
     * @return the value of playPen
     */
    public PlayPen getPlayPen();
    
    /**
     * Gets the UndoManager keeping track of changes in this session
     */
    public UndoManager getUndoManager();
    
    public CompareDMSettings getCompareDMSettings();
    
    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName();
    
    public GenericDDLGenerator getDDLGenerator();
    
    public JDialog getProfileDialog();
    
    /**
     * This is a common handler for all actions that must
     * occur when switching projects, e.g., dispose dialogs, 
     * shut down running threads, etc. 
     * <p>
     * currently mostly a placeholder
     */
    public void close();
    
    public void setDDLGenerator(GenericDDLGenerator generator);
    
    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName);
    

    /**
     * See {@link #savingEntireSource}.
     *
     * @return the value of savingEntireSource
     */
    public boolean isSavingEntireSource();

    /**
     * See {@link #savingEntireSource}.
     *
     * @param argSavingEntireSource Value to assign to this.savingEntireSource
     */
    public void setSavingEntireSource(boolean argSavingEntireSource);
    
    /**
     *  Replaces the entire list of source databases for this session.
     *  This method is used reflectively by the code that does loading and saving,
     *  so DON'T DELETE THIS METHOD even if it looks like it's unused.
     * 
     * @param databases
     * @throws ArchitectException
     */
    public void setSourceDatabaseList(List databases) throws ArchitectException;
    
    public CreateKettleJob getCreateKettleJob();

    public void setCreateKettleJobSettings(CreateKettleJob createKettleJobSettings);
}
