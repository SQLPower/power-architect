package ca.sqlpower.validation.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.validation.DemoTernaryRegExValidator;
import ca.sqlpower.validation.RegExValidator;
import ca.sqlpower.validation.Validator;

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
                JDialog dialog = new JDialog(jf, "Dialog");
                StatusComponent status = new StatusComponent();
                dialog.add(status, BorderLayout.NORTH);

                JPanel midPanel  = new JPanel();

                // SECTION ONE
                JPanel p = new JPanel();
                JLabel textLabel = new JLabel("Text (\\d+)");
                p.add(textLabel);
                JTextField tf = new JTextField(20);
                p.add(tf);
                textLabel.setLabelFor(tf);
                midPanel.add(p);

                // what we came here for #1!!
                Validator v = new RegExValidator("\\d+");
                new TextComponentValidationHandler(v, status, tf);

                // SECTION TWO
                JPanel q = new JPanel();
                q.add(new JLabel("Text (word)"));
                JTextField tf2 = new JTextField(20);
                q.add(tf2);

                midPanel.add(q);

                // what we came here for #2!!
                Validator v2 = new RegExValidator("\\w+", "Must be one word");
                new TextComponentValidationHandler(v2, status, tf2);

                // SECTION THREE
                JPanel r = new JPanel();
                r.add(new JLabel("OK|PASS|FAIL"));
                JTextField tf3 = new JTextField(20);
                r.add(tf3);

                midPanel.add(r);

                // what we came here for #2!!
                Validator v3 = new DemoTernaryRegExValidator();
                new TextComponentValidationHandler(v3, status, tf3);

                dialog.add(midPanel, BorderLayout.CENTER);

                dialog.pack();
                dialog.setLocation(200, 200);
                dialog.setVisible(true);
            }

        });

    }

}
