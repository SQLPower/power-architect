package ca.sqlpower.architect.test;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.Types;
import java.util.*;

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
	protected JFrame dbcsDialog = null;
	protected DBConnectionSpec dbcs;

	public TestUI(DBConnectionSpec spec) throws ArchitectException {
		super("UI Test Frame");

		dbcs = spec;
		SQLDatabase db = new SQLDatabase(spec);

		playpen = new PlayPen(SQLDatabase.getPlayPenInstance());

		ArrayList dblist = new ArrayList(1);
		dblist.add(db);
		dblist.add(SQLDatabase.getPlayPenInstance());
		dbTree = new DBTree(dblist);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
											  new JScrollPane(dbTree),
											  new JScrollPane(playpen));
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(splitPane);
		cp.setOpaque(true);
		setContentPane(cp);
		pack();
		splitPane.setDividerLocation(dbTree.getPreferredSize().width);

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
					if (table != null) {
						table.addColumn(new SQLColumn(table,
													  ((JTextField) evt.getSource()).getText(),
													  Types.VARCHAR, 10, 0));
					}
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
		TestUI frame = new TestUI(spec);
		frame.setSize(400,400);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


}
