package ca.sqlpower.architect.swingui;

// TextForm.java
// A simple label/field form panel
// from http://examples.oreilly.com/jswing2/code/ch19/TextForm.java

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * TextForm is a JPanel that supports a bunch of labelled fields with
 * keyboard mnemonics.
 *
 * <p>This code comes from http://examples.oreilly.com/jswing2/code/ch19/TextForm.java.
 */
public class TextPanel extends JPanel {

	protected JComponent[] fields;
	
	// Create a form with the specified labels, tooltips, and sizes.
	public TextPanel(JComponent[] fields, String[] labels, char[] mnemonics,
					 int[] widths, String[] tips) {
		super(new BorderLayout());
		this.fields = new JComponent[fields.length];
		System.arraycopy(fields, 0, this.fields, 0, fields.length);
		JPanel labelPanel = new JPanel(new GridLayout(labels.length, 1));
		JPanel fieldPanel = new JPanel(new GridLayout(labels.length, 1));
		add(labelPanel, BorderLayout.WEST);
		add(fieldPanel, BorderLayout.CENTER);
		
		for (int i = 0; i < labels.length; i += 1) {
			if (i < tips.length) {
				fields[i].setToolTipText(tips[i]);
			}

			if (i < widths.length && fields[i] instanceof JTextField) {
				((JTextField) fields[i]).setColumns(widths[i]);
			}
			
			JLabel lab = new JLabel(labels[i], JLabel.RIGHT);
			lab.setLabelFor(fields[i]);
			if (i < mnemonics.length) lab.setDisplayedMnemonic(mnemonics[i]);
			
			labelPanel.add(lab);
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(fields[i]);
			fieldPanel.add(p);
		}
	}
	
	public JComponent getField(int i) {
		return fields[i];
	}
	
	/**
	 * Usage example.
	 */
	public static void main(String[] args) {
		JTextField[] fields = { new JTextField(), new JTextField(), new JTextField(), new JTextField() };
		String[] labels = { "First Name", "Middle Initial", "Last Name", "Age" };
		char[] mnemonics = { 'F', 'M', 'L', 'A' };
		int[] widths = { 15, 1, 15, 3 };
		String[] descs = { "First Name", "Middle Initial", "Last Name", "Age" };
		
		final TextPanel form = new TextPanel(fields, labels, mnemonics, widths, descs);

		JButton submit = new JButton("Submit Form");
		
		submit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println(form.getField(0) + " " + form.getField(1) + ". " +
									   form.getField(2) + ", age " + form.getField(3));
				}
			});
		
		JFrame f = new JFrame("Text Form Example");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(form, BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.add(submit);
		f.getContentPane().add(p, BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);
	}
}
