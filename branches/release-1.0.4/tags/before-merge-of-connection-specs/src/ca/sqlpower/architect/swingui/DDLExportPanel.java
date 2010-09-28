package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.event.*;
import ca.sqlpower.architect.ddl.*;
import java.util.*;
import org.apache.log4j.Logger;
import ca.sqlpower.sql.DBConnectionSpec;

public class DDLExportPanel extends JPanel implements ArchitectPanel {
	private static final Logger logger = Logger.getLogger(DDLExportPanel.class);

	protected SwingUIProject project;

    protected JLabel targetDBName;
    
    protected JComboBox dbType;
	
	protected JLabel catalogLabel;
	protected JTextField catalogField;

	protected JLabel schemaLabel;
	protected JTextField schemaField;

	public DDLExportPanel(SwingUIProject project) {
		this.project = project;
		setup();
		setVisible(true);
	}

	protected void setup() {
		GenericDDLGenerator ddlg = project.getDDLGenerator();
		setLayout(new FormLayout());
        add(new JLabel("Create in:"));
        
        DBConnectionSpec dbcs = project.getTargetDatabase().getConnectionSpec();
        add(targetDBName = new JLabel(dbcs == null 
                                        ? "(target connection not set up)" 
                                        : dbcs.getDisplayName()));
		Vector dbTypeList = new Vector();
		dbTypeList.add(ASUtils.lvb("Generic JDBC", GenericDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("DB2", DB2DDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("Oracle 8i/9i", OracleDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("PostgreSQL", PostgresDDLGenerator.class));
		dbTypeList.add(ASUtils.lvb("SQLServer 2000", SQLServerDDLGenerator.class));
		add(new JLabel("Generate DDL for Database Type:"));
		add(dbType = new JComboBox(dbTypeList));
		if (ddlg.getClass() == GenericDDLGenerator.class) {
			dbType.setSelectedIndex(0);
		} else if (ddlg.getClass() == DB2DDLGenerator.class) {
			dbType.setSelectedIndex(1);
		} else if (ddlg.getClass() == OracleDDLGenerator.class) {
			dbType.setSelectedIndex(2);
		} else if (ddlg.getClass() == PostgresDDLGenerator.class) {
			dbType.setSelectedIndex(3);
		} else if (ddlg.getClass() == SQLServerDDLGenerator.class) {
			dbType.setSelectedIndex(4);
		} else {
			logger.error("Unknown DDL generator class "+ddlg.getClass());
			dbType.addItem(ASUtils.lvb("Unknown Generator", ddlg.getClass()));
		}
		dbType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						setUpCatalogAndSchemaFields();
				}
			});

		add(catalogLabel = new JLabel("Target Catalog"));
		add(catalogField = new JTextField(ddlg.getTargetCatalog()));
		add(schemaLabel = new JLabel("Target Schema"));
		add(schemaField = new JTextField(ddlg.getTargetSchema()));

		setUpCatalogAndSchemaFields();
	}

	/**
	 * This method sets up the labels and enabledness of the catalog
	 * and schema text fields.  It should be called every time a new
	 * database type is chosen, and when this panel is set up for the
	 * first time.
	 */
	private void setUpCatalogAndSchemaFields() {
		Class selectedGeneratorClass = null;
		try {
			selectedGeneratorClass = (Class) ((ASUtils.LabelValueBean) dbType.getSelectedItem()).getValue();
			GenericDDLGenerator newGen = (GenericDDLGenerator) selectedGeneratorClass.newInstance();
			if (newGen.getCatalogTerm() != null) {
				catalogLabel.setText(newGen.getCatalogTerm());
				catalogLabel.setEnabled(true);
				catalogField.setEnabled(true);
			} else {
				catalogLabel.setText("(no catalog)");
				catalogLabel.setEnabled(false);
				catalogField.setEnabled(false);
			}
			
			if (newGen.getSchemaTerm() != null) {
				schemaLabel.setText(newGen.getSchemaTerm());
				schemaLabel.setEnabled(true);
				schemaField.setEnabled(true);
			} else {
				schemaLabel.setText("(no schema)");
				schemaLabel.setEnabled(false);
				schemaField.setEnabled(false);
			}
		} catch (Exception ex) {
			String message = "Couldn't create a DDL generator of the selected type";
			if (selectedGeneratorClass != null) {
				message += (":\n"+selectedGeneratorClass.getName());
			}
			logger.error(message, ex);
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
		if (selectedGeneratorClass == GenericDDLGenerator.class) {
			ddlg.setAllowConnection(true);
			DBConnectionSpec dbcs = project.getTargetDatabase().getConnectionSpec();
			if (dbcs == null
				|| dbcs.getDriverClass() == null
				|| dbcs.getDriverClass().length() == 0) {
				throw new IllegalStateException("You can't use the Generic JDBC Generator\n"
												+"until you set up the target database connection.");
			}
		} else {
			ddlg.setAllowConnection(false);
		}

		if (catalogField.isEnabled()) {
			ddlg.setTargetCatalog(catalogField.getText());
		}
		if (schemaField.isEnabled()) {
			ddlg.setTargetSchema(schemaField.getText());
		}
	}

	public void discardChanges() {
        // nothing to discard
	}
}
