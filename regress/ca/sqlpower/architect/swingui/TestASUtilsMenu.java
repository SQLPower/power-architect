package regress.ca.sqlpower.architect.swingui;

import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ASUtils;

public class TestASUtilsMenu extends TestCase {
	
	protected JMenu fileMenu;

	public static void main(String[] args) {
		for (int i = 69; i <= 71; i++) 
			new TestASUtilsMenu().createMenuTest(i);
	}
	
	/**
	 * @throws HeadlessException
	 */
	private JFrame createMenuTest(int max) throws HeadlessException {
		JFrame jf = new JFrame();
		final JMenuBar jb = new JMenuBar();
		jf.setJMenuBar(jb);
		fileMenu = new JMenu("File");
		for (int i = 0; i <= max; i++) {
			fileMenu.add(new JMenuItem(Integer.toString(i)));
		}
		jf.setSize(400, 600);

		jb.add(fileMenu);
		ASUtils.breakLongMenu(jf, fileMenu);
		jf.setVisible(true);
		return jf;
	}
}
