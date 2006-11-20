package ca.sqlpower.validation.swingui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.validation.DemoTernaryRegExValidator;
import ca.sqlpower.validation.RegExValidator;
import ca.sqlpower.validation.Validator;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * A complete demonstration of the Validation system,
 */
public class ValidationDemo {

    public static void main(String[] args) {

        final JFrame jf = new JFrame("Demo");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton dialogButton = new JButton("Show dialog");
        jf.add(dialogButton);
        jf.setBounds(200, 200, 200, 200);
        jf.setVisible(true);

        dialogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog(jf, "Dialog");
                final StatusComponent display = new StatusComponent();
                final FormValidationHandler validateHandler = new FormValidationHandler(display);
                dialog.add(display, BorderLayout.NORTH);

                JPanel midPanel  = new JPanel(new GridLayout(0, 1 ,5, 5));

                // GUI component references that get used n times
                JPanel p;
                final JTextField tf1;
                JTextField tf;

                // SECTION ONE
                p = new JPanel();
                p.add(new JLabel("Text (\\d+)"));
                tf1 = new JTextField(20);
                p.add(tf1);
                midPanel.add(p);

                // what we came here for #1!!
                Validator v = new RegExValidator("\\d+");
                validateHandler.addValidateObject(tf1,v);

                // SECTION TWO
                p = new JPanel();
                p.add(new JLabel("Text (word)"));
                tf = new JTextField(20);
                p.add(tf);

                midPanel.add(p);

                // what we came here for #2!!
                Validator v2 = new RegExValidator("\\w+", "Must be one word");
                validateHandler.addValidateObject(tf,v2);

                // SECTION THREE
                p = new JPanel();
                p.add(new JLabel("OK|WARN|FAIL"));
                tf = new JTextField(20);
                p.add(tf);

                midPanel.add(p);

                // what we came here for #2!!
                Validator v3 = new DemoTernaryRegExValidator();
                validateHandler.addValidateObject(tf,v3);

                dialog.add(midPanel, BorderLayout.CENTER);

                ButtonBarBuilder bPanel = new ButtonBarBuilder();
                bPanel.addGridded(new JButton(new AbstractAction("Insert 123 in numbers"){
                    public void actionPerformed(ActionEvent e) {
                        tf1.setText("123");
                    }}));
                bPanel.addGridded(new JButton(new AbstractAction("Insert abc in numbers"){
                    public void actionPerformed(ActionEvent e) {
                        tf1.setText("abc");
                    }}));

                dialog.add(bPanel.getPanel(), BorderLayout.SOUTH);
                JOptionPane pane;

                dialog.pack();
                dialog.setLocation(200, 200);
                dialog.setVisible(true);
            }

        });

    }

}
