package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.event.*;
import java.util.*;

import ca.sqlpower.sql.DBConnectionSpec;

public class DBCSPanel extends JPanel {

	protected DBConnectionSpec dbcs;
	protected TextPanel form;

	protected JTextField dbNameField;
	protected JComboBox dbDriverField;
	protected JTextField dbUrlField;
	protected JTextField dbUserField;
	protected JPasswordField dbPassField;

	private Map jdbcDrivers;

	public DBCSPanel(DBConnectionSpec dbcs) {
		setupContents();
		setDbcs(dbcs);
	}

	public DBCSPanel() {
		setupContents();
		DBConnectionSpec dbcs = new DBConnectionSpec();
		dbcs.setName("New connection");
		setDbcs(dbcs);
	}

	/**
	 * Sets up this panel's contents (labels, textfields, etc).
	 */
	protected void setupContents() {
		setLayout(new BorderLayout());

		JComponent[] fields = new JComponent[] {dbNameField = new JTextField(),
												dbDriverField = new JComboBox(getDriverClasses()),
												dbUrlField = new JTextField(),
												dbUserField = new JTextField(),
												dbPassField = new JPasswordField()};
		String[] labels = new String[] {"Connection Name",
										"JDBC Driver",
										"JDBC URL",
										"Username",
										"Password"};

		char[] mnemonics = new char[] {'n', 'd', 'u', 'r', 'p'};
		int[] widths = new int[] {30, 30, 40, 20, 20};
		String[] tips = new String[] {"The name of this database", "The class name of the JDBC Driver", "Vendor-specific JDBC URL", "Username for this database", "Password for this database"};
		
		// update url field when user picks new driver
		dbDriverField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String t = getTemplateForDriver(dbDriverField.getSelectedItem().toString());
					if (t == null) t = "jdbc:";
					dbUrlField.setText(t);
				}
			});

		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}

	protected String[] getDriverClasses() {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return (String[]) jdbcDrivers.keySet().toArray(new String[0]);
	}
	
	protected String getTemplateForDriver(String driverClassName) {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return (String) jdbcDrivers.get(driverClassName);
	}
	
	private void setupDriverMap() {
		jdbcDrivers = new HashMap();
		jdbcDrivers.put("oracle.jdbc.driver.OracleDriver",
						"jdbc:oracle:thin:@<hostname>:1521:<instance>");
		jdbcDrivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
						"jdbc:microsoft:sqlserver://<hostname>:1433;SelectMethod=cursor");
		jdbcDrivers.put("org.postgresql.Driver",
						"jdbc:postgresql://<hostname>:5432");
		jdbcDrivers.put("ibm.sql.DB2Driver",
						"jdbc:db2:<hostname>");
	}

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current DBConnectionSpec.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public void apply() {
		dbcs.setDisplayName(dbNameField.getText());
		dbcs.setDriverClass(dbDriverField.getSelectedItem().toString());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(dbPassField.getText());
	}

	/**
	 * Does nothing right now.
	 */
	public void cancel() {
	}

	// --------------------- action -> change event proxy ---------------------
	protected List changeListeners = new LinkedList();

	public void actionPerformed(ActionEvent e) {
		System.out.println("Firing change event "+e);
		fireChange(new ChangeEvent(this));
	}

	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	public void fireChange(ChangeEvent e) {
		Iterator it = changeListeners.iterator();
		while (it.hasNext()) {
			((ChangeListener) it.next()).stateChanged(e);
		}
	}

	// ---------------- accessors and mutators ----------------

	/**
	 * Sets this DBCSPanel's fields to match those of the given dbcs,
	 * and stores a reference to the given dbcs so it can be updated
	 * when the apply() method is called.
	 */
	public void setDbcs(DBConnectionSpec dbcs) {
		dbNameField.setText(dbcs.getDisplayName());
		dbDriverField.insertItemAt(dbcs.getDriverClass(), 0);
		dbDriverField.setSelectedIndex(0);
		dbUrlField.setText(dbcs.getUrl());
		dbUserField.setText(dbcs.getUser());
		dbPassField.setText(dbcs.getPass());
		this.dbcs = dbcs;
	}

	/**
	 * Returns a reference to the current DBConnectionSpec (that is,
	 * the one that will be updated when apply() is called).
	 */
	public DBConnectionSpec getDbcs() {
		return dbcs;
	}

	/**
	 * Convenience method for generating a frame with a DBCSPanel in
	 * it.  The frame includes working new, ok, cancel, and apply
	 * buttons.
	 *
	 * @param dbcs The DBConnectionSpec to associate with this
	 * DBCSPanel.  If the user presses "Apply" or "Ok" then dbcs will
	 * be updated with the new values.  <code>null</code> is not
	 * allowed.
	 */
	public static JFrame createFrame(DBConnectionSpec dbcs) {
		if (dbcs == null) {
			throw new NullPointerException("You need to specify a DBConnectionSpec");
		}
		final JFrame frame = new JFrame();
		final JButton okButton = new JButton("Ok");
		final JButton cancelButton = new JButton("Cancel");
		final JButton applyButton = new JButton("Apply");

		final JPanel southPanel = new JPanel(new FlowLayout());
		final DBCSPanel dbcsPanel = new DBCSPanel(dbcs);

		dbcsPanel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					cancelButton.setEnabled(true);
				}
			});

		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.apply();
					frame.dispose();
				}
			});
		southPanel.add(okButton);

		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.cancel();
					frame.dispose();
				}
			});
		southPanel.add(cancelButton);

		applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelButton.setEnabled(false);
					dbcsPanel.apply();
				}
			});
		southPanel.add(applyButton);

		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(southPanel, BorderLayout.SOUTH);
		cp.add(dbcsPanel, BorderLayout.CENTER);
		frame.pack();
		return frame;
	}
}
