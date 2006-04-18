package regress.ca.sqlpower.architect.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ASUtils;

public class TestASUtilsMenu extends TestCase {
	
	protected JMenu fileMenu;

	public static void main(String[] args) {
		for (int i = 48; i <= 51; i++) 
			new TestASUtilsMenu().createMenuTest(i);
	}
	
	/**
	 * @throws HeadlessException
	 */
	private JFrame createMenuTest(int max) throws HeadlessException {
		final JFrame jf = new JFrame();
		jf.setSize(400, 600);
		final JMenuBar jb = new JMenuBar();
		jf.setJMenuBar(jb);
		fileMenu = new JMenu("File");
		for (int i = 0; i <= max; i++) {
			if ( i==10 || i == 9 ) {
				final JMenuItem jmi = new JMenuItem(Integer.toString(i));
				fileMenu.add(jmi);
				jmi.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						System.out.println("jf.bounds="+jf.getBounds());
						System.out.println("jmi:"+jmi.getBounds());
					}
					
				});
			}
			else {
				fileMenu.add(new JMenuItem(Integer.toString(i)));
			}
		}

		jb.add(fileMenu);
		ASUtils.breakLongMenu(jf, fileMenu);
		jf.setVisible(true);
		return jf;
	}
}
