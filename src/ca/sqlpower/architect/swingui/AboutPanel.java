package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectUtils;

public class AboutPanel extends JPanel implements ArchitectPanel {

	public JLabel content;

	public AboutPanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new FlowLayout());
        
        // XXX This should include the new Power*Architect Icon!
		content = new JLabel("<html>Power*Architect "+
		                    ArchitectUtils.APP_VERSION+"<br><br>" +
							"Copyright 2003-2006 SQL Power Group Inc.<br>" +
							"</html>");
		add(content);
	}

	public boolean applyChanges() {
		return true;
        // nothing to apply
	}

	public void discardChanges() {
        // nothing to discard
	}

	public JPanel getPanel() {
		return this;
	}
}