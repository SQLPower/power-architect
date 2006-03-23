package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ddl.DB2DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.ddl.OracleDDLGenerator;
import ca.sqlpower.architect.ddl.PostgresDDLGenerator;
import ca.sqlpower.architect.ddl.SQLServerDDLGenerator;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;


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
        
        ArchitectDataSource dbcs = project.getTargetDatabase().getDataSource();
        add(targetDBName = new JLabel(dbcs == null 
                                        ? "(target connection not set up)" 
                                        : dbcs.getDisplayName()));
		
		add(new JLabel("Generate DDL for Database Type:"));
		Vector<LabelValueBean> ddlTypes =DDLUtils.getDDLTypes();
		add(dbType = new JComboBox(ddlTypes));
		LabelValueBean unknownGenerator = ASUtils.lvb("Unknown Generator", ddlg.getClass());
		dbType.addItem(unknownGenerator);
		dbType.setSelectedItem(unknownGenerator);
		for (LabelValueBean lvb : ddlTypes)
		{
		
			
			if (ddlg.getClass() == lvb.getValue() && lvb != unknownGenerator)
			{
				dbType.setSelectedItem(lvb);
				
			}
			
		}
		if (dbType.getSelectedItem() != unknownGenerator)
		{
			// remove the unknown generator if we have a known generator
			dbType.removeItem(unknownGenerator);
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
				catalogField.setText(null);
				catalogField.setEnabled(false);
			}
			
			if (newGen.getSchemaTerm() != null) {
				schemaLabel.setText(newGen.getSchemaTerm());
				schemaLabel.setEnabled(true);
				schemaField.setEnabled(true);
			} else {
				schemaLabel.setText("(no schema)");
				schemaLabel.setEnabled(false);
				schemaField.setText(null);
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
	public boolean applyChanges() {
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
			ArchitectDataSource dbcs = project.getTargetDatabase().getDataSource();
			if (dbcs == null
				|| dbcs.getDriverClass() == null
				|| dbcs.getDriverClass().length() == 0) {

				JOptionPane.showMessageDialog
				(this,"You can't use the Generic JDBC Generator\n"
						+"until you set up the target database connection.");
								
				ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
				
				return false;
			}
		} else {
			ddlg.setAllowConnection(false);
		}

		if (catalogField.isEnabled()) {
			if (catalogField.getText() == null || catalogField.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog
				(this,"Please provide a valid database catalog.");
				return false;
			} else {	
				ddlg.setTargetCatalog(catalogField.getText());
			}
		}
		
		if (schemaField.isEnabled()) {
			if (schemaField.getText() == null || schemaField.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog
				(this,"Please provide a valid schema name.");
				return false;
			} else {	
				ddlg.setTargetSchema(schemaField.getText());
			}
		}
			
		return true;
		
	}

	public void discardChanges() {
        // nothing to discard
	}
}
