package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ca.sqlpower.architect.ddl.*;
import java.io.File;
import java.util.*;
import org.apache.log4j.Logger;

public class DDLExportPanel extends JPanel implements ArchitectPanel {
	private static final Logger logger = Logger.getLogger(DDLExportPanel.class);

	protected SwingUIProject project;
	protected JCheckBox allowConnection;
	protected JTextField filename;
	protected JButton fileChooserButton;
	protected JComboBox dbType;

	public DDLExportPanel(SwingUIProject project) {
		this.project = project;
		setup();
		setVisible(true);
	}

	protected void setup() {
		GenericDDLGenerator ddlg = project.getDDLGenerator();
		setLayout(new FormLayout());
		add(new JLabel("Allow Connection?"));
		add(allowConnection = new JCheckBox());
		allowConnection.setSelected(ddlg.getAllowConnection());
		add(new JLabel("Output File"));
		JPanel p = new JPanel(new FlowLayout());
		File outFile = ddlg.getFile();
		if (outFile == null) {
			outFile = new File(System.getProperty("user.dir"), project.getName()+".ddl");
		}
		p.add(filename = new JTextField((outFile.getPath()), 35));
		p.add(fileChooserButton = new JButton("..."));
		fileChooserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.addChoosableFileFilter(ASUtils.sqlFileFilter);
					fc.setSelectedFile(new File(filename.getText()));
					int rv = fc.showDialog(DDLExportPanel.this, "Ok");
					if (rv == JFileChooser.APPROVE_OPTION) {
						filename.setText(fc.getSelectedFile().getPath());
					}
				}
			});
		add(p);

		Vector dbTypeList = new Vector();
		dbTypeList.add(ASUtils.lvb("Generic JDBC", GenericDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("DB2", DB2DDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("Oracle 8i/9i", OracleDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("SQLServer 2000", SQLServerDDLGenerator.class));
		add(new JLabel("Database Type"));
		add(dbType = new JComboBox(dbTypeList));
		if (ddlg.getClass() == GenericDDLGenerator.class) {
			dbType.setSelectedIndex(0);
		} else if (ddlg.getClass() == DB2DDLGenerator.class) {
			dbType.setSelectedIndex(1);
		} else if (ddlg.getClass() == OracleDDLGenerator.class) {
			dbType.setSelectedIndex(2);
		} else if (ddlg.getClass() == SQLServerDDLGenerator.class) {
			dbType.setSelectedIndex(3);
		} else {
			logger.error("Unknown DDL generator class "+ddlg.getClass());
			dbType.addItem(ASUtils.lvb("Unknwon Generator", ddlg.getClass()));
		}
	}

	// ------------------------ Architect Panel Stuff -------------------------
	public void applyChanges() {
		GenericDDLGenerator ddlg = project.getDDLGenerator();
		Class selectedGeneratorClass = (Class) ((ASUtils.LabelValueBean) dbType.getSelectedItem()).getValue();
		if (ddlg.getClass() != selectedGeneratorClass) {
			try {
				ddlg = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
				project.setDDLGenerator(ddlg);
			} catch (Exception ex) {
				logger.error("Problem creating user-selected DDL generator", ex);
				throw new RuntimeException("Couldn't create a DDL generator of the selected type");
			}
		}
		ddlg.setAllowConnection(allowConnection.isSelected());
		ddlg.setFile(new File(filename.getText()));
	}

	public void discardChanges() {
	}
}
