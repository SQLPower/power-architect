package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;

/**
 * The PrintDialogFrame lets the user specify multi-page printouts by
 * scaling the work area to any size.
 *
 * <p>Functional requirements:
 * <ul>
 *  <li>work area can scale to any size (large or small)
 *  <li>real-time preview of what the printout will look like, whether or not it spans pages
 * </ul>
 */
public class PrintDialogFrame extends JFrame {

	public PrintDialogFrame() {
		super("Print");
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(200,200));
		mainPanel.add(new JLabel("Stuff goes here"));
		cp.add(mainPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.add(new JButton("Print"));
		southPanel.add(new JButton("Cancel"));
		cp.add(southPanel, BorderLayout.SOUTH);

		pack();
	}

}
