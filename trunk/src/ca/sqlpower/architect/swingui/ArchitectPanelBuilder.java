package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class ArchitectPanelBuilder {
	static Logger logger = Logger.getLogger(ArchitectPanelBuilder.class);
	public static final String OK_BUTTON_LABEL = "OK";
	public static final String CANCEL_BUTTON_LABEL = "Cancel";
    
    /**
     * Tries very hard to create a JDialog which is owned by the parent
     * Window of the given component.  However, if the component does not
     * have a Window ancestor, or the component has a Window ancestor that
     * is not a Frame or Dialog, this method instead creates an unparented
     * JDialog which is always-on-top.
     *
     * @param owningComponent The component that should own this dialog.
     * @param title The title for the dialog.
     * @return A JDialog that is
     * @author Jonathan Fuerth (donated to SQL Power on October 31, 2006)
     */
    public static JDialog makeOwnedDialog(Component owningComponent,
            String title) {
        Window owner = SwingUtilities.getWindowAncestor(owningComponent);
        if (owner instanceof Frame) {
            return new JDialog((Frame) owner, title);
        } else if (owner instanceof Dialog) {
            return new JDialog((Dialog) owner, title);
        } else {
            JDialog d = new JDialog();
            d.setTitle(title);
            d.setAlwaysOnTop(true);
            return d;
        }
    }

	/**
	 * Build a JDialog around an object that implements ArchitectPanel, to
	 * provide consistent behaviours such as Cancel button, <ESC> to close, and
	 * so on.
	 * XXX Worry about modal vs non-modal
	 * @param arch
	 *            The ArchitectPanel implementation
	 * @param dialogParent
	 *            A Window object to be the dialog's parent
	 * @param dialogTitle
	 *            The dialog title.
	 * @param actionButtonTitle
	 *            The label text for the OK button
	 * @return The new JDialog, which has the panel in it along with OK and Cancel buttons
	 * @param okAction Action to be invoked when the OK/action button is
	 * 	pressed; does NOT need to dismiss the dialog (we do that if applyChanges() returns true).
	 * @param cancelAction Action to be invoked when the cancel button is
	 * 	pressed; does NOT need to dismiss the dialog.
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

        if ( dialogParent == null ) {
            d = new JDialog();
            warnAboutNullParentedDialog();
        } else if (dialogParent instanceof Frame) {
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


		JButton okButton = new JDefaultButton(okAction);
		okButton.setText(actionButtonTitle);
		// In all cases we have to close the dialog.
		Action closeAction = new CommonCloseAction(d);

        //If the user passes in a non-null cancelAction, we have that as the closeAction
        if (cancelAction != null){
			closeAction = cancelAction;
		} else {
			logger.debug("WARNING using a null cancel action.  You probably want to use action so you can cleanup");
		}

		okButton.addActionListener(closeAction);
		makeJDialogCancellable(d, closeAction);
		okButton.addActionListener(new CommonCloseAction(d));
		JButton cancelButton = new JDefaultButton(cancelAction);
		cancelButton.setText(CANCEL_BUTTON_LABEL);
		cancelButton.addActionListener(closeAction);
		cancelButton.addActionListener(new CommonCloseAction(d));

		// Handle if the user presses Enter in the dialog - do OK action
		d.getRootPane().setDefaultButton(okButton);


		// Now build the GUI.
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

    private static void warnAboutNullParentedDialog() {
        if (logger.isDebugEnabled()) {
            JOptionPane.showMessageDialog(null,
                  "This action called createArchitectPanelDialog with DialogParent == null!");
        }
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
	 * Arrange for an existing JDialog to close nicely. Called with an Action,
	 * which will become the cancelAction of the dialog.
	 * Note: we explicitly close the dialog from this code.
	 * @param d
	 * @param cancelAction or null for nothing
	 */
	public static void makeJDialogCancellable(
			final JDialog d,
			final Action cancelAction) {

		JComponent c = (JComponent) d.getRootPane();

		InputMap inputMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = c.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
		actionMap.put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
                if ( cancelAction != null ) {
                    cancelAction.actionPerformed(e);
                }
				d.setVisible(false);
				d.dispose();
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
    public static JDialog createSingleButtonArchitectPanelDialog(
            final ArchitectPanel arch,
            final Window dialogParent,
            final String dialogTitle,
            final String actionButtonTitle) {

        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                arch.applyChanges();
            }
        };

        return createSingleButtonArchitectPanelDialog(arch, dialogParent, dialogTitle,
                actionButtonTitle, okAction );
    }


    /**
     * Build a JDialog around an object that implements ArchitectPanel, to
     * provide consistent behaviours such as Cancel button, <ESC> to close, and
     * so on.
     * XXX Worry about modal vs non-modal
     * @param arch
     *            The ArchitectPanel implementation
     * @param dialogParent
     *            A Window object to be the dialog's parent
     * @param dialogTitle
     *            The dialog title.
     * @param actionButtonTitle
     *            The label text for the OK button
     * @param okAction Action to be invoked when the OK/action button is
     *  pressed; does NOT need to dismiss the dialog (we do that if applyChanges() returns true).
     * @return The new JDialog, which has the panel in it along with OK and Cancel buttons
     */
    public static JDialog createSingleButtonArchitectPanelDialog(
            final ArchitectPanel arch,
            final Window dialogParent,
            final String dialogTitle,
            final String actionButtonTitle,
            final Action okAction ) {

        final JDialog d;

        if ( dialogParent == null ) {
            d = new JDialog();
            warnAboutNullParentedDialog();
        } else if (dialogParent instanceof Frame) {
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


        JButton okButton = new JDefaultButton(okAction);
        okButton.setText(actionButtonTitle);
        // In all cases we have to close the dialog.
        Action closeAction = new CommonCloseAction(d);
        okButton.addActionListener(closeAction);
        makeJDialogCancellable(d, closeAction);
        okButton.addActionListener(new CommonCloseAction(d));

        // Handle if the user presses Enter in the dialog - do OK action
        d.getRootPane().setDefaultButton(okButton);


        // Now build the GUI.
        JPanel cp = new JPanel(new BorderLayout());
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        cp.add(panel, BorderLayout.CENTER);

        cp.add(ButtonBarFactory.buildCenteredBar(okButton),
                BorderLayout.SOUTH);
        cp.setBorder(Borders.DIALOG_BORDER);

        //d.add(cp);
        d.setContentPane(cp);

        // XXX maybe pass yet another argument for this?
        // d.setLocationRelativeTo(ArchitectFrame.getMainInstance());

        d.pack();
        return d;
    }
    

}
