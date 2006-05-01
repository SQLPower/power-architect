package ca.sqlpower.architect.swingui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.swingui.event.CatalogComboBoxListener;
import ca.sqlpower.architect.swingui.event.DatabaseComboBoxListener;
import ca.sqlpower.architect.swingui.event.NewDatabaseListener;

public class DatabaseSelector {

	private JComboBox connectionsBox;
	private JComboBox schemaBox;
	private JComboBox catalogBox;
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JButton newButton;
	private DatabaseComboBoxListener dcl;
	private JPanel parent;
	
	public DatabaseSelector(JComboBox connectionsBox, JComboBox schemaBox, JComboBox catalogBox, JButton newButton, JProgressBar progressBar, JLabel progressLabel,JPanel parent) {
		this.connectionsBox = connectionsBox;
		this.schemaBox = schemaBox;
		this.catalogBox = catalogBox;
		this.progressBar = progressBar;
		this.progressLabel = progressLabel;
		this.parent = parent;
		
		setup();
	}

	public DatabaseSelector(JProgressBar progressBar, JLabel progressLabel,JPanel parent) {
		this.progressBar = progressBar;
		this.progressLabel = progressLabel;
		this.parent = parent;
		
		newButton = new JButton(" New ");
		connectionsBox = new JComboBox();
		catalogBox = new JComboBox();
		schemaBox = new JComboBox();
		setup();
		
		
	}

	public void setup(){ 
		connectionsBox.setModel(new ConnectionComboBoxModel());
		connectionsBox.setRenderer(new DataSourceRenderer());
		newButton.addActionListener(
				new NewDatabaseListener(ArchitectFrame.getMainInstance(),
								"New Database",
								connectionsBox));
		dcl = new DatabaseComboBoxListener(
				parent,
				connectionsBox,
				catalogBox,
				schemaBox,
				progressBar);
		

		connectionsBox.addActionListener(dcl);
		
		catalogBox.addActionListener(new CatalogComboBoxListener(parent, 
				connectionsBox, catalogBox, schemaBox));
	}
	
	public JButton getNewButton() {
		return newButton;
	}

	public void setNewButton(JButton newButton) {
		this.newButton = newButton;
	}

	public JPanel getParent() {
		return parent;
	}

	public void setParent(JPanel parent) {
		this.parent = parent;
	}

	public JComboBox getCatalogBox() {
		return catalogBox;
	}

	public void setCatalogBox(JComboBox catalogBox) {
		this.catalogBox = catalogBox;
	}

	public JComboBox getConnectionsBox() {
		return connectionsBox;
	}

	public void setConnectionsBox(JComboBox connectionsBox) {
		this.connectionsBox = connectionsBox;
	}

	public JLabel getProgressLabel() {
		return progressLabel;
	}

	public void setProgressLabel(JLabel label) {
		this.progressLabel = label;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public JComboBox getSchemaBox() {
		return schemaBox;
	}

	public void setSchemaBox(JComboBox schemaBox) {
		this.schemaBox = schemaBox;
	}
}
