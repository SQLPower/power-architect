package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import ca.sqlpower.architect.*;
import java.util.*;

public class ColumnEditPanel extends JPanel {

	JList columns;

	public ColumnEditPanel(SQLColumn col) throws ArchitectException {
		setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		setLayout(new BorderLayout(2,2));
		columns = new JList(new Vector(col.getParent().getChildren()));
		add(new JScrollPane(columns), BorderLayout.WEST);

		JPanel centerBox = new JPanel();
		centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
		centerBox.add(Box.createVerticalGlue());
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FormLayout(2, 3));
		centerPanel.setBorder(BorderFactory.createTitledBorder("Column Properties"));
		centerPanel.add(new JLabel("Name"));
		centerPanel.add(new JTextField("name"));
		centerPanel.add(new JLabel("Type"));
		centerPanel.add(new JTextField("type"));
		centerPanel.add(new JLabel("Precision"));
		centerPanel.add(new JTextField("precision"));
		centerPanel.add(new JLabel("Remarks"));
		centerPanel.add(new JTextField("remarks"));
		Dimension maxSize = centerPanel.getLayout().preferredLayoutSize(centerPanel);
		maxSize.width = Integer.MAX_VALUE;
		centerPanel.setMaximumSize(maxSize);
		centerBox.add(centerPanel);
		centerBox.add(Box.createVerticalGlue());
		add(centerBox, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		southPanel.add(new JButton("Ok"));
		southPanel.add(new JButton("Cancel"));
		add(southPanel, BorderLayout.SOUTH);
	}
}
