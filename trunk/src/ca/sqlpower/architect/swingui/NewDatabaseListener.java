package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectDataSource;



public class NewDatabaseListener implements ActionListener {

	private ArchitectFrame frame;
	private String title;
	private JComboBox comboBox;
	
	public NewDatabaseListener(ArchitectFrame frame, String title, JComboBox comboBox) {
		super();
		this.frame = frame;
		this.title = title;
		this.comboBox = comboBox;
	}

	public void actionPerformed(ActionEvent e) {
		
		final JDialog d = new JDialog(frame,title );
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
				
				if ( comboBox != null ) {
					comboBox.addItem(dbcs);
					comboBox.setSelectedItem(dbcs);
				}
							
				frame.getUserSettings().getPlDotIni().addDataSource(dbcs);
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
		d.setLocationRelativeTo(frame);
		d.setVisible(true);
	}
	
}
