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

	protected JFrame editFrame;
	protected JButton okButton;
	protected JButton cancelButton;
	protected ActionListener okCancelListener;
	protected ColumnEditPanel columnEditPanel;

	public EditColumnAction(PlayPen pp) {
		super("Edit Column");
		this.pp = pp;
	}

	public void actionPerformed(ActionEvent evt) {
		Selectable invoker = pp.getSelectedChild();
		if (invoker instanceof TablePane) {
			TablePane tp = (TablePane) invoker;
			try {
				int idx = tp.getSelectedColumnIndex();
				if (editFrame != null) {
					columnEditPanel.setModel(tp.getModel());
					columnEditPanel.selectColumn(idx);
					editFrame.setTitle("Edit columns of "+tp.getModel().getName());
					editFrame.requestFocus();
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
					buttonPanel.add(cancelButton);
					panel.add(buttonPanel, BorderLayout.SOUTH);
					
					editFrame = new JFrame("Edit columns of "+tp.getModel().getName());
					panel.setOpaque(true);
					editFrame.setContentPane(panel);
					editFrame.pack();
					editFrame.setVisible(true);
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
	 * Permanently closes the edit frame.
	 */
	protected void cleanup() {
		if (editFrame != null) {
			editFrame.setVisible(false);
			editFrame.dispose();
			editFrame = null;
		}
	}
}
