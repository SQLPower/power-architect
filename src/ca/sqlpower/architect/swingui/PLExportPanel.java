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
import java.io.*;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class PLExportPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	protected DBConnectionSpec dbcs;
	protected Vector history;
	protected Vector plodbc;

	// Left-hand side fields
	protected JComboBox historyBox;
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
	protected JCheckBox  runPLEngine;

	protected String     projName;
	private Map jdbcDrivers;
	private JButton newConnButton;
	
	public PLExportPanel() {
		setLayout(new GridLayout(1,2));
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		SwingUIProject project = af.getProject();
		projName = new String(project.getName());
		newConnButton= new JButton("New");
		newConnButton.addActionListener(new NewConnectionListener());

		List connectionHistory = af.prefs.getConnections();

		String plIniPath = af.getUserSettings().getETLUserSettings().getPlDotIniPath();
		List plOdbcCon = new ArrayList();
		try {
			if (plIniPath != null) {
				plOdbcCon = getPLDBConnection(plIniPath);
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

		history = new Vector();
		history.add(ASUtils.lvb("(Select PL Connection)", null));
		Iterator it = connectionHistory.iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			history.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		historyBox = new JComboBox(history);
		historyBox.addActionListener(new HistoryBoxListener());

		plodbc = new Vector();
		plodbc.add(ASUtils.lvb("(Select PL ODBC Connection)", null));
		Iterator itO = plOdbcCon.iterator();
		while (itO.hasNext()) {
			PLdbConn plcon = (PLdbConn) itO.next();
			plodbc.add(ASUtils.lvb(plcon.getLogical(), plcon));
		}

		plODBCSourceBox = new JComboBox(plodbc);
		plODBCSourceBox.addActionListener(new ODBCSourceListener());

		runPLEngine = new JCheckBox();
		runPLEngine.setEnabled(false);

		plFolderName = new JTextField(toPLIdentifier(projName)+"_Folder");

		plJobId      = new JTextField(toPLIdentifier(projName)+"_Job");

		JPanel jdbcPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jdbcPanel.add(historyBox);
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

	public class HistoryBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) historyBox.getSelectedItem();
			if (lvb.getValue() != null) {
				dbcs = (DBConnectionSpec) lvb.getValue();
			}
		}
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
				PLdbConn pldbcon = (PLdbConn) lvb.getValue();
				plRepOwner.setText(pldbcon.getPlsOwner());
				plUserName.setText(pldbcon.getUid());
				plPassword.setText(decryptPlIniPassword(9, pldbcon.getPwd()));
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
							historyBox.addItem(ASUtils.lvb(dbcs.getDisplayName(), dbcs));
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

	public List getPLDBConnection(String plDotIniPath) throws FileNotFoundException, IOException {
		List odbc = new ArrayList();
		PLdbConn plconn = null;
		File inputFile = new File(plDotIniPath);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		String line = null;

		while ((line = readLine(in)) != null) {
			if (line.startsWith("[Databases")) {
				plconn =  new PLdbConn();
				odbc.add(plconn);
			} else if (plconn != null) {
				int equalsIdx = line.indexOf('=');
				if (equalsIdx > 0) {
					String key = line.substring(0, equalsIdx);
					String value = line.substring(equalsIdx+1, line.length());
					plconn.setProperty(key, value);
				} else {
					logger.debug("pl.ini entry lacks = sign: "+line);
				}
			} else {
				logger.debug("Skipping "+line);
			}
		}
		in.close();
		return odbc;
	}

	/**
	 * Mangles the given string into a valid PL identifier (no spaces,
	 * at most 80 characters long, all uppercase).
	 */
	public static String toPLIdentifier(String text) {
		final int MAX_PLID_LENGTH = 80;
		if (text.length() > MAX_PLID_LENGTH) text = text.substring(0, MAX_PLID_LENGTH);
		StringBuffer plid = new StringBuffer(text.toUpperCase());
		for (int i = 0, n = plid.length(); i < n; i++) {
			if (Character.isWhitespace(plid.charAt(i))) {
				plid.setCharAt(i, '_');
			}
		}
		return plid.toString();
	}

	protected String readLine(InputStream in) throws IOException {
		StringBuffer line = new StringBuffer(80);

		for (;;) {
			int ch = in.read();
			if (ch == -1 || ch == '\r') break;
			if (ch != '\n') line.append((char) ch);
		}

		if (line.length() == 0) {
			return null;
		} else {
			return line.toString();
		}
	}

// Function DecryptPassword(pnNumber As Byte, psEncryptedPassword As String) As String
// Dim lsPassword As String
// Dim lnCounter As Byte
// Dim lnTemp As Integer

//     lnCounter = 1
//     Do Until lnCounter = Len(psEncryptedPassword) + 1
//         lnTemp = Asc(Mid(psEncryptedPassword, lnCounter, 1)) Xor (10 - pnNumber)

//         If lnCounter Mod 2 = 0 Then 'see if even
//             lnTemp = lnTemp + pnNumber
//         Else
//             lnTemp = lnTemp - pnNumber
//         End If

//         lsPassword = lsPassword & Chr$(lnTemp)

//         lnCounter = lnCounter + 1
//     Loop

//     DecryptPassword = lsPassword

// End Function

	/**
	 * Decrypts a PL.INI password.  The correct argument for
	 * <code>number</code> is 9.
	 */
	public static String decryptPlIniPassword(int number, String encryptedPassword) {
		StringBuffer password = new StringBuffer(encryptedPassword.length());
		
		for (int i = 0, n = encryptedPassword.length(); i < n; i++) {
			logger.debug("input char = "+(int)encryptedPassword.charAt(i));
			int temp = ((encryptedPassword.charAt(i) & 0x00ff) ^ (10 - number));

			if (i % 2 == 1) {
				temp += number;
			} else {
				temp -= number;
			}
			
			password.append((char) temp);
		}

		return password.toString();
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
	 * Returns the PLdbConn object representing the currently-selected
	 * PL.INI database entry.
	 */
	public PLdbConn getSelectedPlDatabase() {
		return (PLdbConn) ((ASUtils.LabelValueBean) plODBCSourceBox.getSelectedItem()).getValue();
	}

	/**
	 * returns values typed in panel
	 */
	public String getPlODBCSourceName() {
		PLdbConn conn = getSelectedPlDatabase();
		if (conn != null) {
			return conn.getLogical();
		} else {
			return null;
		}
	}
	
	public String getPlRepOwner(){
		return toPLIdentifier(plRepOwner.getText());
	}	 
	
	public String getPlFolderName(){
		return toPLIdentifier(plFolderName.getText());
	}
	
	public String getPlJobId(){
		return toPLIdentifier(plJobId.getText());
	}
	
	public String getPlJobDescription(){
		return plJobDescription.getText();
	}
	
	public String getPlJobComment(){
		return plJobComment.getText();
	}
	
	public String getPlOutputTableOwner(){
		return toPLIdentifier(plOutputTableOwner.getText());
	}
	
	public boolean isSelectedRunPLEngine(){
		return runPLEngine.isSelected();
	}
	
	public String getPlUserName() {
		return toPLIdentifier(plUserName.getText());
	}

	public String getPlPassword() {
		return plPassword.getText();
	}
	
	/**
	 * Returns a reference to the current DBConnectionSpec (that is,
	 * the one that will be updated when apply() is called).
	 */
	public DBConnectionSpec getDbcs() {
		return dbcs;
	}
}
