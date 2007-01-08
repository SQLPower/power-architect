package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
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

        // Include the Power*Architect Icon!
        String realPath = "/icons/architect128.png";
        java.net.URL imgURL = ASUtils.class.getResource(realPath);

        if (imgURL != null) {
            ImageIcon imageIcon = new ImageIcon(imgURL, "Architect Logo");
            add(new JLabel(imageIcon));
        }

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