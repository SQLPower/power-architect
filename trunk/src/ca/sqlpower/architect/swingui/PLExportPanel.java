package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.architect.etl.*;

public class PLExportPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	/**
	 * This is the PLExport whose properties this panel edits.
	 */
	protected PLExport plexp;

	protected Vector connections;
	protected Vector plodbc;

	// Left-hand side fields
	protected JComboBox connectionsBox;
	protected JTextField plRepOwner;
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;
	protected JTextField plOutputTableOwner;

	// Right-hand side fields
	protected JComboBox plOdbcTargetRepositoryBox;
	protected JTextField plUserName;
	protected JPasswordField plPassword;
	protected JCheckBox runPLEngine;

	protected String projName;
	private Map jdbcDrivers;
	private JButton newConnButton;
	
	public PLExportPanel() {
		setLayout(new GridLayout(1,2));
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		SwingUIProject project = af.getProject();
		projName = new String(project.getName());
		newConnButton= new JButton("New");
		newConnButton.addActionListener(new NewConnectionListener());

		String plIniPath = af.getUserSettings().getETLUserSettings().getPlDotIniPath();
		List plConnectionSpecs = new ArrayList();
		try {
			if (plIniPath != null) {
				plConnectionSpecs = PLUtils.parsePlDotIni(plIniPath);
			} else {
				JOptionPane.showMessageDialog
					(this, "Warning: You have not set the PL.INI file location."
					 +"\nThe list of PL Connections will be empty.");
			}
		} catch (FileNotFoundException ie) {
			JOptionPane.showMessageDialog
				(this, "PL database config file not found in specified path:\n"
				 +plIniPath+"\nThe list of PL Connections will be empty.");
		} catch (IOException ie){
			JOptionPane.showMessageDialog(this, "Error reading PL.ini file "+plIniPath
										  +"\nThe list of PL Connections will be empty.");
		}    

		connections = new Vector();
		connections.add(ASUtils.lvb("(Select Architect Connection)", null));
		Iterator it = af.getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			connections.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		connectionsBox = new JComboBox(connections);

		plodbc = new Vector();
		plodbc.add(ASUtils.lvb("(Select PL ODBC Connection)", null));
		it = plConnectionSpecs.iterator();
		while (it.hasNext()) {
			PLConnectionSpec plcon = (PLConnectionSpec) it.next();
			plodbc.add(ASUtils.lvb(plcon.getLogical(), plcon));
		}

		plOdbcTargetRepositoryBox = new JComboBox(plodbc);
		plOdbcTargetRepositoryBox.addActionListener(new OdbcTargetRepositoryListener());

		runPLEngine = new JCheckBox();
		runPLEngine.setEnabled(false);

		JPanel jdbcPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jdbcPanel.add(connectionsBox);
		jdbcPanel.add(newConnButton);

		JComponent[] jdbcFields = new JComponent[] {jdbcPanel,
													plRepOwner = new JTextField(),
													plFolderName = new JTextField(),
													plJobId = new JTextField(),
													plJobDescription = new JTextField(),
													plJobComment = new JTextField(),
													plOutputTableOwner = new JTextField()};
		String[] jdbcLabels = new String[] {"Architect Connection Name",
											"PL Repository Owner",
											"PL Folder Name",
											"PL Job Id",
											"PL Job Description",
											"PL Job Comment",
											"Target Schema Owner"};

		char[] jdbcMnemonics = new char[] {'t', 'r', 'f', 'i', 'd', 'c', 'o'};
		int[] jdbcWidths = new int[] {18, 18, 18, 18, 18, 18, 18, 18,10};
		String[] jdbcTips = new String[] {"Target database and PL repository",
										  "Owner of PL Repository",
										  "The folder name for transactions",
										  "The Job unique Id",
										  "The Job Description",
										  "Comment about the Job",
										  "Owner (Schema) for output transaction tables"};

		TextPanel jdbcForm = new TextPanel(jdbcFields, jdbcLabels, jdbcMnemonics, jdbcWidths, jdbcTips);

		JComponent[] engineFields = new JComponent[] {plOdbcTargetRepositoryBox,
													  plUserName = new JTextField(),
													  plPassword = new JPasswordField(),
													  runPLEngine};

		String[] engineLabels = new String[] {"PL.INI Logical Database Name",
											  "PL User Name",
											  "PL Password",
											  "Run Engine"};

		char[] engineMnemonics = new char[] {'l', 'u', 'p', 'e'};
		int[] engineWidths = new int[] {18, 18, 18, 10};
		String[] engineTips = new String[] {"ODBC Source Name connection for PL",
											"PowerLoader User Name",
											"PowerLoader Password",
											"Run PL Engine immediately?"};
		TextPanel engineForm = new TextPanel(engineFields, engineLabels, engineMnemonics, engineWidths, engineTips);

		add(jdbcForm);
		add(engineForm);
	}

	/**
	 * Sets a new PLExport object for this panel to edit.  All field
	 * values will be updated to reflect the status of the given
	 * PLExport object.
	 */
	public void setPLExport(PLExport plexp) {
		this.plexp = plexp;
		connectionsBox.setSelectedItem(plexp.getPlDBCS());
		plRepOwner.setText(plexp.getPlUsername());
		plFolderName.setText(plexp.getFolderName());
		plJobId.setText(plexp.getJobId());
		plJobDescription.setText(plexp.getJobDescription());
		plJobComment.setText(plexp.getJobComment());
		plOutputTableOwner.setText(plexp.getOutputTableOwner());
	}
	
	/**
	 * Returns the PLExport object that this panel is editting.  Call
	 * applyChanges() to update it to the current values displayed on
	 * the panel.
	 */
	public PLExport getPLExport() {
		return plexp;
	}

	public class OdbcTargetRepositoryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plOdbcTargetRepositoryBox.getSelectedItem();
			if (lvb.getValue() == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				plRepOwner.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				PLConnectionSpec pldbcon = (PLConnectionSpec) lvb.getValue();
				plRepOwner.setText(pldbcon.getPlsOwner());
				plUserName.setText(pldbcon.getUid());
				plPassword.setText(pldbcon.getPwd());
			}
		}
	}

	
	public class NewConnectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
										  "New PL Repository Connection");
				JPanel plr = new JPanel(new BorderLayout(12,12));
				plr.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
				final DBCSPanel dbcsPanel = new DBCSPanel();
				dbcsPanel.setDbcs(new DBConnectionSpec());
				plr.add(dbcsPanel, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.applyChanges();
							DBConnectionSpec dbcs = dbcsPanel.getDbcs();
							ASUtils.LabelValueBean connLvb = ASUtils.lvb(dbcs.getDisplayName(), dbcs);
							connectionsBox.addItem(connLvb);
							connectionsBox.setSelectedItem(connLvb);
							ArchitectFrame.getMainInstance().getUserSettings().getConnections().add(dbcs);
							d.setVisible(false);
						}
					});
				buttonPanel.add(okButton);

				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.discardChanges();
							d.setVisible(false);
						}
					});
				buttonPanel.add(cancelButton);
				plr.add(buttonPanel, BorderLayout.SOUTH);
				d.setContentPane(plr);
				d.pack();
				d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
				d.setVisible(true);
			}
	}
	
	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Converts the fields that contain PL Identifiers into the
	 * correct format (using PLUtils.toPLIdentifier) and then sets all
	 * the properties of plexp to their values in this panel's input
	 * fields.
	 */
	public void applyChanges() {
		plJobId.setText(PLUtils.toPLIdentifier(plJobId.getText()));
		plexp.setJobId(plJobId.getText());

		plFolderName.setText(PLUtils.toPLIdentifier(plFolderName.getText()));
		plexp.setFolderName(plFolderName.getText());

		plexp.setJobDescription(plJobDescription.getText());

		plexp.setJobComment(plJobComment.getText());

		ASUtils.LabelValueBean item = (ASUtils.LabelValueBean) connectionsBox.getSelectedItem();
		if (item == null) plexp.setPlDBCS(null); 
		else plexp.setPlDBCS((DBConnectionSpec) item.getValue());

		plOutputTableOwner.setText(PLUtils.toPLIdentifier(plOutputTableOwner.getText()));
		plexp.setOutputTableOwner(plOutputTableOwner.getText());

		plUserName.setText(PLUtils.toPLIdentifier(plUserName.getText()));
		plexp.setPlUsername(plUserName.getText());

		// this approach prevents the deprecation warning, but is
		// doing exactly what the deprecation of getText() was meant
		// to prevent!
		plexp.setPlPassword(new String(plPassword.getPassword()));
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
	}

	// ---------------- accessors and mutators ----------------

	public PLConnectionSpec getPLConnectionSpec() {
		ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plOdbcTargetRepositoryBox.getSelectedItem();
		return (PLConnectionSpec) lvb.getValue();
	}

	public String getPlRepOwner(){
		return PLUtils.toPLIdentifier(plRepOwner.getText());
	}	 
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}
}
