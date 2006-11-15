package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;

import javax.swing.*;

public class ASUtilsDisplayDialogDemo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final JFrame jf = new JFrame("test");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AbstractAction action = new AbstractAction("Test Now") {

            public void actionPerformed(ActionEvent e) {
                Throwable t = new IllegalStateException("Woo woo");
                ASUtils.showExceptionDialogNoReport(jf, "Something went terribly wrong", t);
            }
        };
        JButton button = new JButton(action);
        jf.add(button);
        jf.setBounds(200, 200, 200, 200);
        jf.setVisible(true);
    }

}
