package ca.sqlpower.architect.swingui;

// TextForm.java
// A simple label/field form panel
// from http://examples.oreilly.com/jswing2/code/ch19/TextForm.java

import javax.swing.*;

import org.apache.log4j.Logger;

import java.awt.event.*;
import java.awt.*;

/**
 * TextForm is a JPanel that supports a bunch of labelled fields with
 * keyboard mnemonics.
 *
 * <p>This code came from
 * http://examples.oreilly.com/jswing2/code/ch19/TextForm.java, but
 * has local modifications.
 */
public class TextPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(TextPanel.class);

	protected JComponent[] fields;
	protected JComponent[] labels;
	
	// Create a form with the specified labels, tooltips, and sizes.
	public TextPanel(JComponent[] aFields, String[] aLabels, char[] mnemonics,
					 int[] widths, String[] tips) {
		super(new FormLayout(8, 8));

		fields = new JComponent[aFields.length];
		System.arraycopy(aFields, 0, fields, 0, aFields.length);
		labels = new JComponent[aLabels.length];
		
		for (int i = 0; i < aLabels.length; i += 1) {
			if (i < tips.length) {				
				fields[i].setToolTipText(tips[i]);
			}

			if (i < widths.length && fields[i] instanceof JTextField) {
				((JTextField) fields[i]).setColumns(widths[i]);
			}
			
			JLabel lab = new JLabel(aLabels[i], JLabel.RIGHT);
			lab.setLabelFor(fields[i]);
			if (i < mnemonics.length) {
				lab.setDisplayedMnemonic(mnemonics[i]);
			}
			
			labels[i] = lab;
			
			add(lab);
			//JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//p.add(fields[i]);
			//add(p);
			add(fields[i]);
		}
	}
	
	public JComponent getField(int i) {
		return fields[i];
	}
	
	public JComponent getLabel(int i) {
		return labels[i];
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
					logger.info(form.getField(0) + " " + form.getField(1) + ". " +
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
