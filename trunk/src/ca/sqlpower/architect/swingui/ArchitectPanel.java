package ca.sqlpower.architect.swingui;

import javax.swing.JComponent;

/**
 * The ArchitectPanel interface defines the contract between a panel
 * of components that help the user edit the data model and its parent
 * frame.  Classes that implement ArchitectPanel require that exactly
 * one of the two methods {@link #applyChanges()} or {@link
 * #discardChanges()} are called at the end of the panel's lifetime.
 * After affecting the model in the specified way, these methods will
 * free up resources associated with the panel (such as removing the
 * panel from listener lists).  After calling {@link #applyChanges()}
 * or {@link #discardChanges()} on an instance of ArchitectPanel, it
 * may not be used anymore.
 *
 * <p>Remember that it's important to call one of these methods
 * (usually discardChanges()) when a containing frame's window gets
 * closed by the native window system.
 */
public interface ArchitectPanel {

	/**
	 * An OK button in the panel's containing frame should invoke this
	 * method.
	 */
	public boolean applyChanges();

	/**
	 * A cancel button in the panel's containing frame should invoke
	 * this method.
	 */
	public void discardChanges();
	
	/**
	 * @return This ArchitectPanel's JPanel
	 */
	public JComponent getPanel();
}
