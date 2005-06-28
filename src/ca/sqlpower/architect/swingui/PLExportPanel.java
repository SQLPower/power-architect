package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.etl.*;

public class PLExportPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	/**
	 * This is the PLExport whose properties this panel edits.
	 */
	protected PLExport plexp;

	protected Vector connections;

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

	// Watch PL.INI for changes
	protected javax.swing.Timer timer;
	protected String plDotIniPath;

	
	public PLExportPanel() {
		setLayout(new GridLayout(1,2));
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		plDotIniPath = af.getUserSettings().getETLUserSettings().getPlDotIniPath(); // is this bad?
		SwingUIProject project = af.getProject();
		projName = new String(project.getName());
		newConnButton= new JButton("New");
		newConnButton.addActionListener(new NewConnectionListener());

		// initialize the JDBC connections combobox
		connectionsBox = new JComboBox();
		refreshJdbcConnections();

		// initialize the PL ODBC connections combobox
		plOdbcTargetRepositoryBox = new JComboBox(new DefaultComboBoxModel(getPlOdbcTargets()));
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

		// new: add a swing timer to watch the PL.INI file and reload the database connections if
        // it notices any changes...
        timer = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
				if (PLUtils.plDotIniHasChanged(plDotIniPath)) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshPlOdbcTargets();			
						}
					});
				}
            }
        });	
		timer.start();
	}

	/**
	 * Sets a new PLExport object for this panel to edit.  All field
	 * values will be updated to reflect the status of the given
	 * PLExport object.
	 */
	public void setPLExport(PLExport plexp) {
		this.plexp = plexp;
		connectionsBox.setSelectedItem(plexp.getRepositoryDBCS());
		plRepOwner.setText(plexp.getPlUsername());
		plFolderName.setText(plexp.getFolderName());
		plJobId.setText(plexp.getJobId());
		plJobDescription.setText(plexp.getJobDescription());
		plJobComment.setText(plexp.getJobComment());
		plOutputTableOwner.setText(plexp.getTargetSchema());
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
			logger.debug("event was fired");
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plOdbcTargetRepositoryBox.getSelectedItem();
			if (lvb.getValue() == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				plRepOwner.setText(null);
				plUserName.setText(null);
				plPassword.setText(null);
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
				dbcsPanel.setDbcs(new ArchitectDataSource());
				plr.add(dbcsPanel, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.applyChanges();
							ArchitectDataSource dbcs = dbcsPanel.getDbcs();
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
		if (item == null) plexp.setRepositoryDBCS(null); 
		else plexp.setRepositoryDBCS((ArchitectDataSource) item.getValue());

		// Don't mangle the owner and username fields -- some databases like Postgres are case sensitive
		plexp.setTargetSchema(plOutputTableOwner.getText());

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
        // nothing to discard
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

	private Vector getPlOdbcTargets() {
		Vector v = new Vector();
		List plConnectionSpecs = new ArrayList();
		try {
			if (plDotIniPath != null) {
				plConnectionSpecs = PLUtils.parsePlDotIni(plDotIniPath);
			} else {
				JOptionPane.showMessageDialog
					(this, "Warning: You have not set the PL.INI file location."
					 +"\nThe list of PL Connections will be empty.");
			}
		} catch (FileNotFoundException ie) {
			JOptionPane.showMessageDialog
				(this, "PL database config file not found in specified path:\n"
				 +plDotIniPath+"\nThe list of PL Connections will be empty.");
		} catch (IOException ie){
			JOptionPane.showMessageDialog(this, "Error reading PL.ini file "+plDotIniPath
										  +"\nThe list of PL Connections will be empty.");
		}    
		v.add(ASUtils.lvb("(Select PL ODBC Connection)", null));
		Iterator it = plConnectionSpecs.iterator();
		while (it.hasNext()) {
			PLConnectionSpec plcon = (PLConnectionSpec) it.next();
			v.add(ASUtils.lvb(plcon.getLogical(), plcon));
		}
		return v;		
	}

	private void refreshPlOdbcTargets() {
		// reload the PL ODBC connections from PL.INI
		String sObject = plOdbcTargetRepositoryBox.getModel().getSelectedItem().toString();
		logger.debug("the selected object was: " + sObject);
		DefaultComboBoxModel dcbm = new DefaultComboBoxModel(getPlOdbcTargets());					
		plOdbcTargetRepositoryBox.setModel(dcbm);
		boolean selectedExists = false;
		for (int i = 0; i < dcbm.getSize(); i++) {
			String s = (String) dcbm.getElementAt(i).toString();
			if (s.equals(sObject)) {
				plOdbcTargetRepositoryBox.setSelectedIndex(i);
				selectedExists = true;
			}
			logger.debug("item + " + i + " is: " + dcbm.getElementAt(i));
		}
		if (selectedExists == false) {
			logger.debug("connection is gone!  select another!");
			plOdbcTargetRepositoryBox.setSelectedIndex(0);
			// pop up a window explaining what happened...
			JOptionPane.showMessageDialog(ArchitectFrame.getMainInstance(),
    			"The PL ODBC connection you selected has been removed by another process.  Please select another.",
    			"Power*Architect",
    			JOptionPane.WARNING_MESSAGE);			
		}
	}		

	public void refreshJdbcConnections () {
		connections = new Vector();
		connections.add(ASUtils.lvb("(Select Architect Connection)", null));
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			ArchitectDataSource spec = (ArchitectDataSource) it.next();
			connections.add(ASUtils.lvb(spec.getDisplayName(), spec));
			logger.debug("adding connection: " + spec.getDisplayName());
		}
		connectionsBox.setModel(new DefaultComboBoxModel(connections));
	}
}
