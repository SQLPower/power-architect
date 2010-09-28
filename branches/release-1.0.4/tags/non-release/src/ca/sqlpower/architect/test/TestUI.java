package ca.sqlpower.architect.test;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.Types;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.sql.DBCSSource;
import ca.sqlpower.sql.XMLFileDBCSSource;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.*;

public class TestUI {

	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String TOP = "top";
	public static final String BOTTOM = "bottom";

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: TestDB xmlFile dbname");
			System.exit(1);
		}
		DBCSSource dbcsSource = new XMLFileDBCSSource(args[0]);
		DBConnectionSpec spec = DBConnectionSpec.searchListForName(dbcsSource.getDBCSList(), args[1]);
		SQLDatabase db = new SQLDatabase(spec);
		final SQLTable table = (SQLTable) db.getTables().get(0);

		final JFrame frame = new JFrame("Test UI");

		final JPanel playpen = new JPanel();
		playpen.setLayout(new FlowLayout());
		final TablePane tablePane = new TablePane(table);
		playpen.add(tablePane);

		JTree dbTree = new JTree(new DBTreeModel(db));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
											  new JScrollPane(dbTree),
											  new JScrollPane(playpen));
		frame.setContentPane(splitPane);
		
		frame.setSize(400,400);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JFrame frame2 = new JFrame("Controls");
		Box box2 = new Box(BoxLayout.Y_AXIS);
		frame2.getContentPane().add(box2);
		JButton packButton = new JButton("Pack frame");
		packButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					frame.pack();
				}
			});
		box2.add(packButton);

		JTextField addColField = new JTextField();
		addColField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					table.addColumn(new SQLColumn(table, ((JTextField) evt.getSource()).getText(),
												  Types.VARCHAR, 10, 0));
				}
			});
		box2.add(Box.createGlue());
		box2.add(new JLabel("New Column Name"));
		box2.add(addColField);
		
		final Insets margins = new Insets(1,1,1,1);
		tablePane.setMargin(margins);
		final StringBuffer currentMargin = new StringBuffer(LEFT);
		final JSlider marginSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		final Box marginBox = new Box(BoxLayout.X_AXIS);

		ActionListener buttonHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					currentMargin.setLength(0);
					currentMargin.append(e.getActionCommand());
					marginSlider.setToolTipText(currentMargin+" margin size");

					// restore correct value for new "current" margin
					if (currentMargin.toString().equals(LEFT)) marginSlider.setValue(margins.left);
					else if (currentMargin.toString().equals(RIGHT)) marginSlider.setValue(margins.right);
					else if (currentMargin.toString().equals(TOP)) marginSlider.setValue(margins.top);
					else if (currentMargin.toString().equals(BOTTOM)) marginSlider.setValue(margins.bottom);
					else System.out.println("Bad radio button value: "+currentMargin);
				}
			};

		JRadioButton leftButton = new JRadioButton("Left");
		leftButton.setActionCommand(LEFT);
		leftButton.addActionListener(buttonHandler);
		leftButton.setSelected(true);
		JRadioButton rightButton = new JRadioButton("Right");
		rightButton.setActionCommand(RIGHT);
		rightButton.addActionListener(buttonHandler);
		JRadioButton topButton = new JRadioButton("Top");
		topButton.setActionCommand(TOP);
		topButton.addActionListener(buttonHandler);
		JRadioButton bottomButton = new JRadioButton("Bottom");
		bottomButton.setActionCommand(BOTTOM);
		bottomButton.addActionListener(buttonHandler);

		ButtonGroup marginButtons = new ButtonGroup();
		marginButtons.add(leftButton);
		marginButtons.add(rightButton);
		marginButtons.add(topButton);
		marginButtons.add(bottomButton);

		JPanel marginButtonPanel = new JPanel();
		marginButtonPanel.setLayout(new GridLayout(4,1));
		marginButtonPanel.setBorder(new TitledBorder("Margin Settings"));
		marginButtonPanel.add(leftButton);
		marginButtonPanel.add(rightButton);
		marginButtonPanel.add(topButton);
		marginButtonPanel.add(bottomButton);
		
		marginBox.add(marginButtonPanel);

		marginSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					int newVal = ((JSlider) evt.getSource()).getValue();

					// move the "current" margin
					if (currentMargin.toString().equals(LEFT)) margins.left = newVal;
					else if (currentMargin.toString().equals(RIGHT)) margins.right = newVal;
					else if (currentMargin.toString().equals(TOP)) margins.top = newVal;
					else if (currentMargin.toString().equals(BOTTOM)) margins.bottom = newVal;
					else System.out.println("Bad radio button value: "+currentMargin);
					tablePane.setMargin(margins);
				}
			});
		marginBox.add(marginSlider);

		box2.add(marginBox);

		frame2.pack();
		frame2.setVisible(true);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
