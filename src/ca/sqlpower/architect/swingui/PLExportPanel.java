package ca.sqlpower.architect.swingui;

import javax.swing.*;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;



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
		connectionsBox.setRenderer(new ConnectionsCellRenderer());
		refreshConnections();
		
		// listen for changes and update user, password, etc.
		connectionsBox.addActionListener(new RepositoryListener());

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

		JComponent[] engineFields = new JComponent[] {plUserName = new JTextField(),
													  plPassword = new JPasswordField(),
													  runPLEngine};

		String[] engineLabels = new String[] {"PL User Name",
											  "PL Password",
											  "Run Engine"};

		char[] engineMnemonics = new char[] {'u', 'p', 'e'};
		int[] engineWidths = new int[] {18, 18, 10};
		String[] engineTips = new String[] {"PowerLoader User Name",
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
							refreshConnections();			
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
		connectionsBox.setSelectedItem(plexp.getRepositoryDataSource());
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

	public class RepositoryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			logger.debug("event was fired");
			ArchitectDataSource dataSource = (ArchitectDataSource) connectionsBox.getSelectedItem();
			if (dataSource == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				plRepOwner.setText(null);
				plUserName.setText(null);
				plPassword.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				plRepOwner.setText(dataSource.get(ArchitectDataSource.PL_SCHEMA_OWNER));
				plUserName.setText(dataSource.get(ArchitectDataSource.PL_UID));
				plPassword.setText(dataSource.get(ArchitectDataSource.PL_PWD));
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
		if (item == null) plexp.setRepositoryDataSource(null); 
		else plexp.setRepositoryDataSource((ArchitectDataSource) item.getValue());

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

	public String getPlRepOwner(){
		return PLUtils.toPLIdentifier(plRepOwner.getText());
	}	 
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}

	public void refreshConnections () { // XXX: this needs to remember if something was selected
		connections = new Vector();
		connections.add(ASUtils.lvb("(Select Architect Connection)", null));
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			connections.add(it.next());
		}
		Object selectedConnection = connectionsBox.getSelectedItem();
		connectionsBox.setModel(new DefaultComboBoxModel(connections));
		if (selectedConnection != null) {
			connectionsBox.setSelectedItem(selectedConnection);
		}
	}

	class ConnectionsCellRenderer extends JLabel implements ListCellRenderer {
	    public ConnectionsCellRenderer() {
	        setOpaque(true);
	    }
	    public Component getListCellRendererComponent(
	        JList list,
	        Object value,
	        int index,
	        boolean isSelected,
	        boolean cellHasFocus)
	    {
	    	ArchitectDataSource dataSource = (ArchitectDataSource) value;
	    	setText(dataSource.get(ArchitectDataSource.PL_LOGICAL));
	        return this;
	    }
	}	
	
}


