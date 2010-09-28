package ca.sqlpower.architect.swingui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ASUtilsDisplayDialogDemo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final JFrame jf = new JFrame("test");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new GridLayout(0, 1));
        AbstractAction action = new AbstractAction("Test ISE") {

            public void actionPerformed(ActionEvent e) {
                Throwable t = new IllegalStateException("Moo cow");
                ASUtils.showExceptionDialogNoReport(jf, "Your cow mooed one too many times", t);
            }
        };
        jf.add(new JButton(action));

        action = new AbstractAction("Test SQL") {

            public void actionPerformed(ActionEvent e) {
                Throwable t = new SQLException("Db42 Error detected");
                ASUtils.showExceptionDialogNoReport(jf, "Your database doesn't like you!\nLine 2", t);
            }
        };
        jf.add(new JButton(action));

        jf.pack();
        jf.setLocation(400, 300);
        jf.setVisible(true);
    }

}
