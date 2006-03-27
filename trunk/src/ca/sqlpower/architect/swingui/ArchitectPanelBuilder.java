package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class ArchitectPanelBuilder {

	public static JDialog createArchitectPanelDialog(final ArchitectPanel arch,
			final Window dialogParent, final String dialogTitle,
			final String actionButtonTitle, final Action okAction,
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
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
				d.dispose();
			}
		});
		okButton.setText(actionButtonTitle);

		JButton cancelButton = new JButton(cancelAction);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
				d.dispose();
			}
		});
		cancelButton.setText("Cancel");
		
		d.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				System.out.println("Pressed key " + key);
				super.keyTyped(e);
			}
		});

		JPanel cp = new JPanel(new BorderLayout());
		cp.add(panel, BorderLayout.CENTER);

		cp.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton),
				BorderLayout.SOUTH);
		cp.setBorder(Borders.DIALOG_BORDER);

		d.add(cp);

		d.pack();
		return d;
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
	 * @return The build JDialog
	 */
	public static JDialog createArchitectPanelDialog(final ArchitectPanel arch,
			final Window dialogParent, final String dialogTitle,
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
}
