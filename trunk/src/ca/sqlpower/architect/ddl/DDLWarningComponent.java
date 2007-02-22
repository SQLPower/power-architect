package ca.sqlpower.architect.ddl;

import javax.swing.JComponent;

/**
 * A UI component that will display a DDLWarning and provide
 * the user with a GUI of some type to correct the error.
 */
public interface DDLWarningComponent {

    /**
     * Return the Runnable that will apply the changes.
     */
    public Runnable getChangeApplicator();
    /**
     * Return the associated visual component
     */
    public JComponent getComponent();
    /**
     * Return the DDLWarning object
     */
    public DDLWarning getWarning();

    /** Do something - apply the user's changes */
    public void applyChanges();
}
