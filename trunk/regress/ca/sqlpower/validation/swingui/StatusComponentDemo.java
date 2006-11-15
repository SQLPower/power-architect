package ca.sqlpower.validation.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.validation.Status;

/**
 * A demonstration of a dialog with a StatusComponent but not Validator;
 * validation is faked by toggling a boolean.
 */
public class StatusComponentDemo {

    static Status status = Status.FAIL;

    public static void main(String[] args) {
        final JFrame jx = new JFrame("Test");
        jx.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final StatusComponent statusComponent = new StatusComponent("Test");
        statusComponent.setText("Unknown error");
        statusComponent.setStatus(status);
        JPanel p = new JPanel(new BorderLayout());
        jx.add(p);
        p.add(statusComponent, BorderLayout.CENTER);
        JButton toggleButton = new JButton("Toggle");
        p.add(toggleButton, BorderLayout.SOUTH);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Just cycle through all the values over and over
                switch(status) {
                case OK:
                    status = Status.WARN; break;
                case WARN:
                    status = Status.FAIL; break;
                case FAIL:
                    status = Status.OK; break;
                }
                statusComponent.setStatus(status);
            }
        });
        jx.pack();
        jx.setVisible(true);
    }
}
