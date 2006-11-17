package ca.sqlpower.validation.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.validation.ValidateResult;

/**
 * A demonstration of a dialog with a StatusComponent but not Validator;
 * validation is faked by toggling a boolean.
 */
public class StatusComponentDemo {

    static ValidateResult status = new ValidateResult();

    public static void main(String[] args) {
        final JFrame jx = new JFrame("Test");
        jx.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final StatusComponent statusComponent = new StatusComponent("Test");
        statusComponent.setText("Unknown error");
        status.setMessage("");
        status.setStatus(ValidateResult.Status.OK);
        statusComponent.setResult(status);
        JPanel p = new JPanel(new BorderLayout());
        jx.add(p);
        p.add(statusComponent, BorderLayout.CENTER);
        JButton toggleButton = new JButton("Toggle");
        p.add(toggleButton, BorderLayout.SOUTH);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Just cycle through all the values over and over
                switch(status.getStatus()) {
                case OK:
                    status.setStatus(ValidateResult.Status.WARN);
                    break;
                case WARN:
                    status.setStatus(ValidateResult.Status.FAIL);
                    break;
                case FAIL:
                    status.setStatus(ValidateResult.Status.OK);
                    break;
                }
                statusComponent.setResult(status);
            }
        });
        jx.pack();
        jx.setVisible(true);
    }
}
