package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class ArchitectPanelBuilder {

	public static final String OK_BUTTON_LABEL = "OK";
	public static final String CANCEL_BUTTON_LABEL = "Cancel";

	/**
	 * Build a JDialog around an object that implements ArchitectPanel, to
	 * provide consistent behaviours such as Cancel button, <ESC> to close, and
	 * so on.
	 * XXX Worry about modal vs non-modal
	 * @param arch
	 *            The ArchitectPanel implementation
	 * @param dialogParent
	 *            A Window class to be the parent, or null
	 * @param dialogTitle
	 *            The display title.
	 * @param actionButtonTitle 
	 *            The title for the OK button
	 * @return The build JDialog
	 * @param okAction Action to be invoked when the OK/action button is
	 * 	pressed; does NOT need to dismiss the dialog (we do that).
	 * @param cancelAction Action to be invoked when the cancel button is
	 * 	pressed; does NOT need to dismiss the dialog (we do that).
	 * @return
	 */
	public static JDialog createArchitectPanelDialog(
			final ArchitectPanel arch,			
			final Window dialogParent, 
			final String dialogTitle,
			final String actionButtonTitle, 
			final Action okAction,
			final Action cancelAction) {
		
		final JDialog d;
		if (dialogParent instanceof Frame) {
			d = new JDialog((Frame) dialogParent, dialogTitle);
		} else if (dialogParent instanceof Dialog) {
			d = new JDialog((Dialog) dialogParent, dialogTitle);
		} else {
			throw new IllegalArgumentException(
					"The dialogParent you gave me is not a "
							+ "Frame or Dialog (it is a "
							+ dialogParent.getClass().getName() + ")");
		}
		JComponent panel = arch.getPanel();
		JButton okButton = new JButton(okAction);
		
		Action closeAction = new CommonCloseAction(d);
		
		okButton.addActionListener(closeAction);
		okButton.setText(actionButtonTitle);

		JButton cancelButton = new JButton(cancelAction);
		cancelButton.addActionListener(closeAction);
		cancelButton.setText(CANCEL_BUTTON_LABEL);
		// Handle if the user presses Enter in the dialog - do OK action
		d.getRootPane().setDefaultButton(okButton);
		
		makeJDialogCancellable(d, cancelAction, closeAction);

		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		cp.add(panel, BorderLayout.CENTER);

		cp.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);
		cp.setBorder(Borders.DIALOG_BORDER);
		
		//d.add(cp);
		d.setContentPane(cp);
		
		// XXX maybe pass yet another argument for this?
		// d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		
		d.pack();
		return d;
	}

	/**
	 * Arrange for a JDialog to close nicely. Called with two Actions,
	 * one of which (the cancelAction) may be user-provided (and may
	 * even be null) while the other (the closeAction) is likely to be
	 * an instance of our CommonCloseAction.
	 * Sadly the Swing ActionMap does not handle multiple entries,
	 * so we have to branch out action handling to both Actions.
	 * @param d
	 * @param cancelAction
	 * @param closeAction
	 */
	public static void makeJDialogCancellable(
			final JDialog d, 
			final Action cancelAction,
			final Action closeAction) {
		
		if (closeAction == null) {
			throw new NullPointerException("closeAction may not be null");
		}
		JComponent c = (JComponent) d.getRootPane();
				
		InputMap inputMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = c.getActionMap();
		
		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
		actionMap.remove("cancel");
		actionMap.put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (cancelAction != null) {
					cancelAction.actionPerformed(e);
				}
				closeAction.actionPerformed(e);
			}			
		});
	}

	/**
	 * Build a JDialog around an object that implements ArchitectPanel, to
	 * provide consistent behaviours such as Cancel button, <ESC> to close, and
	 * so on.
	 * 
	 * @param arch
	 *            The ArchitectPanel implementation
	 * @param dialogParent
	 *            A Window class to be the parent, or null
	 * @param dialogTitle
	 *            The display title.
	 * @param actionButtonTitle 
	 *            The title for the OK button
	 * @return The built JDialog
	 */
	public static JDialog createArchitectPanelDialog(
			final ArchitectPanel arch,
			final Window dialogParent, 
			final String dialogTitle,
			final String actionButtonTitle) {

		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				arch.applyChanges();
			}
		};
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				arch.discardChanges();
			}
		};
		return createArchitectPanelDialog(arch, dialogParent, dialogTitle,
				actionButtonTitle, okAction, cancelAction);
	}
	
	/**
	 * Build a JDialog around an object that implements ArchitectPanel, to
	 * provide consistent behaviours such as Cancel button, <ESC> to close, and
	 * so on, including a default "OK" button
	 * @param arch
	 *            The ArchitectPanel implementation
	 * @param dialogParent
	 *            A Window class to be the parent, or null
	 * @param dialogTitle
	 *            The display title.
	 * @return The build JDialog
	 */
	public static JDialog createArchitectPanelDialog(
			final ArchitectPanel arch,
			final Window dialogParent, 
			final String dialogTitle) {
		
		return createArchitectPanelDialog(arch, dialogParent, dialogTitle, OK_BUTTON_LABEL);
	}
}
