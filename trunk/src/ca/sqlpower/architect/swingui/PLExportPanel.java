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
	protected JComboBox plODBCSourceBox;
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
					 +"\nThe PL Connection box will be empty.");
			}
		} catch (FileNotFoundException ie) {
			JOptionPane.showMessageDialog
				(this, "PL database config file not found in specified path:\n"
				 +plIniPath+"\nThe PL Connection box will be empty.");
		} catch (IOException ie){
			JOptionPane.showMessageDialog(this, "Error reading PL.ini file "+plIniPath
										  +"\nThe PL Connection box will be empty.");
		}    

		connections = new Vector();
		connections.add(ASUtils.lvb("(Select PL Connection)", null));
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

		plODBCSourceBox = new JComboBox(plodbc);
		plODBCSourceBox.addActionListener(new ODBCSourceListener());

		runPLEngine = new JCheckBox();
		runPLEngine.setEnabled(false);

		plFolderName = new JTextField(PLUtils.toPLIdentifier(projName)+"_Folder");

		plJobId      = new JTextField(PLUtils.toPLIdentifier(projName)+"_Job");

		JPanel jdbcPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jdbcPanel.add(connectionsBox);
		jdbcPanel.add(newConnButton);

		JComponent[] jdbcFields = new JComponent[] {jdbcPanel,
													plRepOwner = new JTextField(),
													plFolderName,
													plJobId,
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

		JComponent[] engineFields = new JComponent[] {plODBCSourceBox,
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

	public class ODBCSourceListener  implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plODBCSourceBox.getSelectedItem();
			if (lvb.getValue() == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				plRepOwner.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				PLConnectionSpec pldbcon = (PLConnectionSpec) lvb.getValue();
				plRepOwner.setText(pldbcon.getPlsOwner());
				plUserName.setText(pldbcon.getUid());
				plPassword.setText(PLUtils.decryptPlIniPassword(9, pldbcon.getPwd()));
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
	 * Does nothing right now.
	 */
	public void applyChanges() {
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
	}

	// ---------------- accessors and mutators ----------------

	/**
	 * Returns the selected DBConnectionSpec from the combo box, or
	 * null if the "choose a connection" item is selected.
	 */
	public DBConnectionSpec getTargetDBCS() {
		ASUtils.LabelValueBean item = (ASUtils.LabelValueBean) connectionsBox.getSelectedItem();
		if (item == null) return null;
		else return (DBConnectionSpec) item.getValue();
	}

	/**
	 * Returns the PLConnectionSpec object representing the currently-selected
	 * PL.INI database entry.
	 */
	public PLConnectionSpec getSelectedPlDatabase() {
		return (PLConnectionSpec) ((ASUtils.LabelValueBean) plODBCSourceBox.getSelectedItem()).getValue();
	}

	/**
	 * returns values typed in panel
	 */
	public String getPlODBCSourceName() {
		PLConnectionSpec conn = getSelectedPlDatabase();
		if (conn != null) {
			return conn.getLogical();
		} else {
			return null;
		}
	}
	
	public String getPlRepOwner(){
		return PLUtils.toPLIdentifier(plRepOwner.getText());
	}	 
	
	public String getPlFolderName(){
		return PLUtils.toPLIdentifier(plFolderName.getText());
	}
	
	public String getPlJobId(){
		return PLUtils.toPLIdentifier(plJobId.getText());
	}
	
	public String getPlJobDescription(){
		return plJobDescription.getText();
	}
	
	public String getPlJobComment(){
		return plJobComment.getText();
	}
	
	public String getPlOutputTableOwner(){
		return PLUtils.toPLIdentifier(plOutputTableOwner.getText());
	}
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}
	
	public String getPlUserName() {
		return PLUtils.toPLIdentifier(plUserName.getText());
	}

	public String getPlPassword() {
		// this approach prevents the deprecation warning, but is
		// doing exactly what the deprecation of getText() was meant
		// to prevent!
		return new String(plPassword.getPassword());
	}
}
