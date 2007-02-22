package ca.sqlpower.architect.ddl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class GenericDDLWarningComponent implements DDLWarningComponent {

    private JButton quickFixButton = new JButton("Quick fix");

    private DDLWarning warning;

    public GenericDDLWarningComponent(final DDLWarning warning) {
        this.warning = warning;

        if (warning.isQuickFixable()) {
            quickFixButton.setToolTipText(warning.getQuickFixMessage());
            quickFixButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean fixed = warning.quickFix();
                    warning.setFixed(fixed);
                }
            });
        } else {
            quickFixButton.setEnabled(false);
        }
    }
    public DDLWarning getWarning() {
        return warning;
    }

    public JButton getQuickFixButton() {
        return quickFixButton;
    }
}
