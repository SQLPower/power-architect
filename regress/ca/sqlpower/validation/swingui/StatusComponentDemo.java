package ca.sqlpower.validation.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

/**
 * A demonstration of a dialog with a StatusComponent but not Validator;
 * validation is faked by toggling a boolean.
 */
public class StatusComponentDemo {

    static ValidateResult status = ValidateResult.createValidateResult(Status.OK, "");
    static Status innerStatus = Status.OK;

    public static void main(String[] args) {
        final JFrame jx = new JFrame("Test");
        jx.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final StatusComponent statusComponent = new StatusComponent("Test");
        statusComponent.setText("Unknown error");
        statusComponent.setResult(status);
        JPanel p = new JPanel(new BorderLayout());
        jx.add(p);
        p.add(statusComponent, BorderLayout.CENTER);
        JButton toggleButton = new JButton("Toggle");
        p.add(toggleButton, BorderLayout.SOUTH);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Just cycle through all the values over and over
                switch(statusComponent.getResult().getStatus()) {
                case OK:
                    status = ValidateResult.createValidateResult(
                            Status.WARN, "Warning");
                    break;
                case WARN:
                    status = ValidateResult.createValidateResult(
                            Status.FAIL, "Failure");
                    break;
                case FAIL:
                    status = ValidateResult.createValidateResult(
                            Status.OK, "Swell");
                    break;
                }
                statusComponent.setResult(status);
            }
        });
        jx.pack();
        jx.setLocation(400, 400);
        jx.setVisible(true);
    }
}
