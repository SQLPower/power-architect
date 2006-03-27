package ca.sqlpower.architect.swingui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

public class AboutPanel extends JPanel implements ArchitectPanel {

	public JLabel content;

	public AboutPanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new FlowLayout());
		content = new JLabel("<html>Power*Architect 1.0<br><br>"
							 +"Copyright 2003-2004 SQL Power Group Inc.<br>"
							 +"</html>");
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