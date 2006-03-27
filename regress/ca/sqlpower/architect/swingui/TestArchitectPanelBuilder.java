package regress.ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * Test the ASUtils.createArchitectPanelDialog() method.
 */
public class TestArchitectPanelBuilder {
	static class Foo extends JPanel implements ArchitectPanel {
		Foo() {
			add(new JLabel("This is just a test"));
		}

		public boolean applyChanges() {
			System.out.println("You applied your changes");
			return false;
		}

		public void discardChanges() {
			System.out.println("You cancelled your changes");
		}

		public JComponent getPanel() {
			return this;
		}
	};

	public static void main(String[] args) {
		Foo architectPanel = new Foo();
		JFrame frame = new JFrame("Test Main Program");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JDialog dlg = 
			ArchitectPanelBuilder.createArchitectPanelDialog(
					architectPanel, frame, "Test", "OK Dudes");
		
		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("OK action actionPerformed called");
			}
		};
		
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("cancel action actionPerformed called");
			}
		};
		
		final JDialog dlg2 = 
			ArchitectPanelBuilder.createArchitectPanelDialog(
					architectPanel, frame,
					"Test with actions pass in",
					"OK Dudes2",
					okAction,
					cancelAction);
		
		frame.add(new JLabel("This is the test program's main window"));
		
		
		
		JButton test1Button = new JButton();
		test1Button.setText("Test1");
		
		JButton test2Button = new JButton();
		test2Button.setText("Test2");
		
		test1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.setVisible(true);
			}
		});
		
		test2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg2.setVisible(true);
			}
		});
		
		
		JPanel cp = new JPanel(new BorderLayout());
		
		cp.add(ButtonBarFactory.buildOKCancelBar(
				test1Button,
				test2Button),
				BorderLayout.SOUTH);
		cp.setBorder(Borders.DIALOG_BORDER);
		
		frame.add(cp);
		

		
		
		frame.setSize(250, 250);
		frame.setVisible(true);
	}
}
