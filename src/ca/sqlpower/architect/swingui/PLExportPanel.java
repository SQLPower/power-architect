package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class PLExportPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	protected DBConnectionSpec dbcs;
	protected TextPanel form;
	protected Vector history;
	protected JComboBox historyBox;
	protected JTextField plODBCSourceName;
	protected JTextField plRepOwner;
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;
	protected JTextField plOutputTableOwner;
	protected JCheckBox  runPLEngine;
	private Map jdbcDrivers;
	private JButton newConnButton;
	
	public PLExportPanel() {
		setLayout(new BorderLayout());
		ArchitectFrame af = ArchitectFrame.getMainInstance();

		newConnButton= new JButton("New");
		newConnButton.addActionListener(new NewConnectionListener());

		List connectionHistory = af.prefs.getConnections();
		history = new Vector();
		history.add(ASUtils.lvb("(Select PL Connection)", null));
		Iterator it = connectionHistory.iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			history.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		historyBox = new JComboBox(history);
		historyBox.addActionListener(new HistoryBoxListener());
		plODBCSourceName = new JTextField();
		plODBCSourceName.addFocusListener(new ODBCSourceFocusListener());
		runPLEngine = new JCheckBox();
		runPLEngine.setEnabled(false);
		
		JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        connectPanel.add(historyBox);
		connectPanel.add(newConnButton);

		JComponent[] fields = new JComponent[] {connectPanel,
												plODBCSourceName,
												plRepOwner = new JTextField(),
												plFolderName = new JTextField(),
												plJobId = new JTextField(),
												plJobDescription = new JTextField(),
												plJobComment = new JTextField(),
												plOutputTableOwner = new JTextField(),
												runPLEngine };
		String[] labels = new String[] {"PL Connection",
										"PL ODBC Source Name",
										"PL Repository Owner",
										"Folder Name",
										"Job Id",
										"Job Description",
										"Job Comment",
										"Output Table Owner",
										"Run Engine"};

		char[] mnemonics = new char[] {'p', 's', 'r', 'f', 'i', 'd', 'c', 'o','e'};
		int[] widths = new int[] {30, 30, 20, 30, 30, 40, 60, 20,10};
		String[] tips = new String[] {"A list of connections you have made in the past",
									  "ODBC Source Name connection for PL",
									  "Owner of PL Repository",
									  "The folder name for transactions",
									  "The Job unique Id",
		                              "The Job Description",
		                              "Comment about the Job",
		                              "Owner(Schema) for output transaction tables",
									  "CheckIt if you want to run the engine"};
		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}

	public class HistoryBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) historyBox.getSelectedItem();
			if (lvb.getValue() != null) {
				dbcs = (DBConnectionSpec) lvb.getValue();
			}
		}
	}

	public class ODBCSourceFocusListener  implements FocusListener {
		public ODBCSourceFocusListener(){
			//I'm interested in lost focus 
		}
	    public void focusGained(FocusEvent e) {
			// nothing
		}

		public void focusLost(FocusEvent e) {
			if( ((plODBCSourceName.getText()).equals(null)) || ((plODBCSourceName.getText()).length() == 0 )){
				runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
			} else {
				runPLEngine.setEnabled(true);
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
				plr.add(dbcsPanel, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcs = new DBConnectionSpec();
							dbcsPanel.applyChanges(dbcs);
							history.add(ASUtils.lvb(dbcs.getDisplayName(), dbcs));
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
				d.setVisible(true);
			}
	}
	
	/**
	 * Returns the index of findMe in the DBCS history vector.  0
	 * means not found because 0 is the reserved index for new
	 * connections not in the list.
	 */
	public int findHistoryConnection(DBConnectionSpec findMe) {
		int i = 0;
		Iterator it = history.iterator();
		while (it.hasNext()) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) it.next();
			if (lvb.getValue() == findMe) {
				return i;
			}
			i++;
		}
		return 0;
	}

	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current DBConnectionSpec.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public void applyChanges() {
		String name = plFolderName.getText();
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
	}

	// ---------------- accessors and mutators ----------------

	/**
	 * returns values typed in panel
	 */
	 public String getPlODBCSourceName(){
		 return plODBCSourceName.getText();
	 }	 
	 
	 public String getPlRepOwner(){
		 return plRepOwner.getText();
	 }	 
	 
	 public String getPlFolderName(){
		 return plFolderName.getText();
	 }

	 public String getPlJobId(){
		 return plJobId.getText();
	 }
	 
	 public String getPlJobDescription(){
		 return plJobDescription.getText();
	 }
	 
	 public String getPlJobComment(){
		 return plJobComment.getText();
	 }
	 
	 public String getPlOutputTableOwner(){
		 return plOutputTableOwner.getText();
	 }
	 
	 public boolean isSelectedRunPLEngine(){
		 return runPLEngine.isSelected();
	 }
	 
	/**
	 * Returns a reference to the current DBConnectionSpec (that is,
	 * the one that will be updated when apply() is called).
	 */
	public DBConnectionSpec getDbcs() {
		return dbcs;
	}
}
