package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;

import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.ddl.*;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class CompareDMAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(CompareDMAction.class);

	protected ArchitectFrame architectFrame;

	public CompareDMAction() {
		super("Compare DM");
		architectFrame = ArchitectFrame.getMainInstance();
		putValue(SHORT_DESCRIPTION, "Compare Data Models");
	}

	public void actionPerformed(ActionEvent e) {
		final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
									  "Compare Data Models");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		
		final CompareDMPanel compareDMPanel = new CompareDMPanel();
		cp.add(compareDMPanel, BorderLayout.CENTER);

//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel buttonPanel = compareDMPanel.getButtonPanel();
	
		
		
		JButton okButton = new JButton(compareDMPanel.getStartCompareAction());
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					d.setVisible(false);
				}
			});
		buttonPanel.add(cancelButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		
		
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
		d.setVisible(true);
	}

}
