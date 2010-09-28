package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import ca.sqlpower.architect.*;
import org.apache.log4j.Logger;

public class EditColumnAction extends AbstractAction implements ActionListener {
	private static final Logger logger = Logger.getLogger(EditColumnAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	protected PlayPen pp;

	protected JDialog editDialog;
	protected JButton okButton;
	protected JButton cancelButton;
	protected ActionListener okCancelListener;
	protected ColumnEditPanel columnEditPanel;

	public EditColumnAction() {
		super("Column Properties...",
			  ASUtils.createIcon("ColumnProperties",
								 "Column Properties",
								 ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Column Properties");
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelection();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			try {
				int idx = tp.getSelectedColumnIndex();
				if (editDialog != null) {
					columnEditPanel.setModel(tp.getModel());
					columnEditPanel.selectColumn(idx);
					editDialog.setTitle("Edit columns of "+tp.getModel().getName());
					editDialog.setVisible(true);
					editDialog.requestFocus();
				} else {
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout(12,12));
					panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
					columnEditPanel = new ColumnEditPanel(tp.getModel(), idx);
					panel.add(columnEditPanel, BorderLayout.CENTER);
					
					JPanel buttonPanel = new JPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
					okCancelListener = new OkCancelListener();
					okButton = new JButton("Ok");
					okButton.addActionListener(okCancelListener);
					buttonPanel.add(okButton);
					cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(okCancelListener);
					cancelButton.setEnabled(false); //XXX: need to implement cancel
					buttonPanel.add(cancelButton);
					panel.add(buttonPanel, BorderLayout.SOUTH);
					
					editDialog = new JDialog(ArchitectFrame.getMainInstance(),
											 "Edit columns of "+tp.getModel().getName());
					panel.setOpaque(true);
					editDialog.setContentPane(panel);
					editDialog.pack();
					editDialog.setVisible(true);
				}
			} catch (ArchitectException e) {
				JOptionPane.showMessageDialog(tp, "Error finding the selected column");
				logger.error("Error finding the selected column", e);
				cleanup();
			}
		} else {
			JOptionPane.showMessageDialog((JComponent) invoker,
										  "The selected item type is not recognised");
			cleanup();
		}
	}

	class OkCancelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				columnEditPanel.applyChanges();
				cleanup();
			} else if (e.getSource() == cancelButton) {
				columnEditPanel.discardChanges();
				cleanup();
			} else {
				logger.error("Recieved action event from unknown source: "+e);
			}
		}
	}

	/**
	 * Permanently closes the edit dialog.
	 */
	protected void cleanup() {
		if (editDialog != null) {
			editDialog.setVisible(false);
			editDialog.dispose();
			editDialog = null;
		}
	}

	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}

}
