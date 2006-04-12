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
	
	/** For testing the ArchitectPanelBuilder with the default Actions
	 */
	static class TestPanel extends JPanel implements ArchitectPanel {
		TestPanel() {
			setLayout(new BorderLayout());
			add(new JLabel("This is just a test"), BorderLayout.CENTER);
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
	}

	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Test Main Program");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JDialog dlgWithDefaultActions = 
			ArchitectPanelBuilder.createArchitectPanelDialog(
					new TestPanel(), frame, "Test", "OK Dudes");
		
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
					new TestPanel(), frame,
					"Test with actions passed in",
					"OK Dudes",
					okAction,
					cancelAction);
		
		frame.add(
			new JLabel("This is the test program's main window",
			JLabel.CENTER),
			BorderLayout.NORTH);	
		
		JButton test1Button = new JButton();
		test1Button.setText("Test Default Actions");
		
		JButton test2Button = new JButton();
		test2Button.setText("Test Caller-Provided Actions");
		
		test1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlgWithDefaultActions.setVisible(true);
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
		
		frame.add(cp, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}
}
