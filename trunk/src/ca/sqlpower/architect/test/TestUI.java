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

public class TestUI extends JFrame {

	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String TOP = "top";
	public static final String BOTTOM = "bottom";

	protected SQLTable table = null;
	protected JPanel playpen = null;
	protected JTree dbTree = null;
	protected PrintDialogFrame printDialog = null;

	public TestUI(SQLDatabase db) throws ArchitectException {
		super("UI Test Frame");

		table = (SQLTable) db.getTables().get(0);

		playpen = new PlayPen();

		dbTree = new DBTree(db);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
											  new JScrollPane(dbTree),
											  new JScrollPane(playpen));
		setContentPane(splitPane);
		
		JFrame controlsFrame = createControlsFrame();
		controlsFrame.pack();
		controlsFrame.setVisible(true);
		controlsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected JFrame createControlsFrame() throws ArchitectException {
		JFrame controlsFrame = new JFrame("Controls");
		Box box2 = new Box(BoxLayout.Y_AXIS);
		controlsFrame.getContentPane().add(box2);
		JButton packButton = new JButton("Pack frame");
		packButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					pack(); // packs the enclosing TestUI frame
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
		
		printDialog = new PrintDialogFrame();
		JButton printButton = new JButton("Print...");
		printButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					printDialog.setVisible(true);
				}
			});
		box2.add(printButton);

		return controlsFrame;
	}

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: TestDB xmlFile dbname");
			System.exit(1);
		}
		DBCSSource dbcsSource = new XMLFileDBCSSource(args[0]);
		DBConnectionSpec spec = DBConnectionSpec.searchListForName(dbcsSource.getDBCSList(), args[1]);
		SQLDatabase db = new SQLDatabase(spec);

		TestUI frame = new TestUI(db);
		frame.setSize(400,400);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


}
