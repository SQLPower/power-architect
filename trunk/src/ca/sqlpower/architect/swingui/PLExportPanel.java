package ca.sqlpower.architect.swingui;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;



import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.etl.*;

public class PLExportPanel extends JPanel implements ArchitectPanel {

    /**
     * The EditRepositoryListener reacts to presses of the editRepository button.
     */
    public class EditRepositoryListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * The EditTargetListener reacts to presses of the editTarget button.
     */
    public class EditTargetListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
        }
    }

    private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	/**
	 * This is the PLExport whose properties this panel edits.
	 */
	protected PLExport plexp;

	protected JComboBox targetConnectionsBox;
	protected JTextField targetSchema;
	protected JComboBox repositoryConnectionsBox;
	protected JTextField repositorySchema;
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;

	private JButton newTargetButton;
	// private JButton editTargetButton;
	private JButton newRepositoryButton;
	// private JButton editRepositoryButton;
	private JCheckBox runPLEngine;

	// Watch PL.INI for changes
	protected javax.swing.Timer timer;
	protected String plDotIniPath;
	
	public PLExportPanel() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		plDotIniPath = af.getUserSettings().getETLUserSettings().getPlDotIniPath(); // is this bad?
		SwingUIProject project = af.getProject();
		
		targetConnectionsBox = new JComboBox();
		targetConnectionsBox.setRenderer(new ConnectionsCellRenderer());
		refreshTargetConnections();
		targetConnectionsBox.addActionListener(new TargetListener());
		newTargetButton= new JButton("New");
		newTargetButton.addActionListener(new NewTargetListener());
		// editTargetButton= new JButton("Properties");
		// editTargetButton.addActionListener(new EditTargetListener());
		targetSchema = new JTextField();

		// 
		repositoryConnectionsBox = new JComboBox();
		repositoryConnectionsBox.setRenderer(new ConnectionsCellRenderer());
		refreshRepositoryConnections();
		repositoryConnectionsBox.addActionListener(new RepositoryListener());
		newRepositoryButton= new JButton("New");
		newRepositoryButton.addActionListener(new NewRepositoryListener());
		// editRepositoryButton= new JButton("Properties");
		// editRepositoryButton.addActionListener(new EditRepositoryListener());
		repositorySchema = new JTextField();

		//
		plFolderName = new JTextField();
		plJobId = new JTextField();
		plJobDescription = new JTextField();
		plJobComment = new JTextField();
		runPLEngine = new JCheckBox("Run PL Engine?");
		runPLEngine.setEnabled(false);

		JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        targetPanel.add(targetConnectionsBox);
		targetPanel.add(newTargetButton);
		// targetPanel.add(editTargetButton);

		JPanel repositoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        repositoryPanel.add(repositoryConnectionsBox);
		repositoryPanel.add(newRepositoryButton);
		// repositoryPanel.add(editRepositoryButton);		

		
		JComponent[] fields = new JComponent[] {targetPanel,
													targetSchema,
													new JLabel("<html>&nbsp;</html>"),													
													repositoryPanel,
													repositorySchema,
													new JLabel("<html>&nbsp;</html>"),													
													plFolderName,
													plJobId,
													plJobDescription,
													plJobComment,
													runPLEngine };
		String[] labels = new String[] {"Target Connection",
											"Target Schema",
											"<html>&nbsp;</html>",
											"Repository Connection",
											"Repository Schema",											
											"<html>&nbsp;</html>",
											"PL Folder Name",
											"PL Job Id",
											"PL Job Description",
											"PL Job Comment",
											"<html>&nbsp;</html>"}; // run PL engine?

		char[] mnemonics = new char[] {'t', 's', 'z', 'r', 'p', 'y', 'f', 'j','d','c','e'};
		int[] widths = new int[] {18, 18, 18, 18, 18, 18, 18, 18,18,18,10};
		String[] tips = new String[] {"Target Database Connection",
					              		  "Target Database Schema/Owner",
										  "",
					              		  "Repository Database Connection",
										  "Repository Schema/Owner",
										  "",
										  "The folder name for transactions",
										  "The Job unique Id",
										  "The Job Description",
										  "Comment about the Job",
										  "Run Power Loader Engine?"};

		TextPanel mainForm = new TextPanel(fields, labels, mnemonics, widths, tips);

		add(mainForm);

		/* This is messing things up, so take it out for now
		// new: add a swing timer to watch the PL.INI file and reload the database connections if
        // it notices any changes...
        timer = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
				if (PLUtils.plDotIniHasChanged(plDotIniPath)) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshTargetConnections();			
							refreshRepositoryConnections();			
						}
					});
				}
            }
        });	
		timer.start();
		*/
	}
	

	/**
	 * Sets a new PLExport object for this panel to edit.  All field
	 * values will be updated to reflect the status of the given
	 * PLExport object.
	 */
	public void setPLExport(PLExport plexp) {
		this.plexp = plexp;
		targetConnectionsBox.setSelectedItem(plexp.getTargetDataSource());
		// targetSchema.setText(plexp.getTargetSchema());
		repositoryConnectionsBox.setSelectedItem(plexp.getRepositoryDataSource());
		// repositorySchema.setText(plexp.getRepositorySchema());
		plFolderName.setText(plexp.getFolderName());
		plJobId.setText(plexp.getJobId());
		plJobDescription.setText(plexp.getJobDescription());
		plJobComment.setText(plexp.getJobComment());		
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
			ArchitectDataSource dataSource = (ArchitectDataSource) repositoryConnectionsBox.getSelectedItem();
			if (dataSource == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				repositorySchema.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				repositorySchema.setText(dataSource.get(ArchitectDataSource.PL_SCHEMA_OWNER));
			}
		}
	}
	
    /**
     * The TargetListener reacts to presses of the target button.
     */
    public class TargetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
			logger.debug("event was fired");
			ArchitectDataSource dataSource = (ArchitectDataSource) targetConnectionsBox.getSelectedItem();
			if (dataSource == null) {
				repositorySchema.setText(null);
   		    } else {
				targetSchema.setText(dataSource.get(ArchitectDataSource.PL_SCHEMA_OWNER));
			}
        }
    }	
	
	public class NewTargetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
										  "New Target Connection");
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
							targetConnectionsBox.addItem(dbcs);
							targetConnectionsBox.setSelectedItem(dbcs);
							ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni().addDataSource(dbcs);
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
	
    /**
     * The NewRepositoryListener reacts to presses of the EditRepository button.
     */
    public class NewRepositoryListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
			final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
			  "New Repository Connection");
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
					repositoryConnectionsBox.addItem(dbcs);
					repositoryConnectionsBox.setSelectedItem(dbcs);
					ArchitectFrame.getMainInstance().getUserSettings().getPlDotIni().addDataSource(dbcs);
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
	public boolean applyChanges() {
	    logger.debug("Applying changes to the PLExport object");
		plexp.setTargetDataSource((ArchitectDataSource)targetConnectionsBox.getSelectedItem());
		plexp.setRepositoryDataSource((ArchitectDataSource)repositoryConnectionsBox.getSelectedItem());
		// XXX: probably need to grab the schemas here
		
		plJobId.setText(PLUtils.toPLIdentifier(plJobId.getText()));
		plexp.setJobId(plJobId.getText());
		plFolderName.setText(PLUtils.toPLIdentifier(plFolderName.getText()));
		plexp.setFolderName(plFolderName.getText());
		plexp.setJobDescription(plJobDescription.getText());
		plexp.setJobComment(plJobComment.getText());	
		
		// XXX: maybe set the run engine field here as well?

		// Don't mangle the owner and username fields -- some databases like Postgres are case sensitive
		plexp.setTargetSchema(targetSchema.getText());
		
		return true;
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
        // nothing to discard
	}

	// ---------------- accessors and mutators ----------------
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}

	public void refreshRepositoryConnections () { // XXX: this needs to remember if something was selected		
		Vector connections = new Vector();
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			connections.add(it.next());
		}
		Object selectedConnection = repositoryConnectionsBox.getSelectedItem();
		repositoryConnectionsBox.setModel(new DefaultComboBoxModel(connections));
		if (selectedConnection != null) {
			repositoryConnectionsBox.setSelectedItem(selectedConnection);
		}
	}

	public void refreshTargetConnections () { // XXX: this needs to remember if something was selected		
		Vector connections = new Vector();
		Iterator it = ArchitectFrame.getMainInstance().getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			connections.add(it.next());
		}
		Object selectedConnection = targetConnectionsBox.getSelectedItem();
		targetConnectionsBox.setModel(new DefaultComboBoxModel(connections));
		if (selectedConnection != null) {
			targetConnectionsBox.setSelectedItem(selectedConnection);
		}
	}		
}





