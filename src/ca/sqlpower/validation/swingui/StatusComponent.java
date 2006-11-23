package ca.sqlpower.validation.swingui;

import java.awt.Dimension;

import javax.swing.JLabel;

import ca.sqlpower.architect.swingui.StatusIcon;
import ca.sqlpower.validation.ValidateResult;

/**
 * A Component that displays the success/failure result
 * with a textual message.
 */
public class StatusComponent extends JLabel {

    private static final int X_ADJUST = 20;
    private static final int PAD = 7;
    private ValidateResult result = null;

    public StatusComponent() {
        this("");
    }

    public StatusComponent(String text) {
        super(text);
        setIcon(StatusIcon.getNullIcon());
    }

    public void setResult(ValidateResult error) {
        result = error;
        if (result == null) {
            setIcon(StatusIcon.getNullIcon());
            super.setText("");
            return;
        }

        switch(result.getStatus()) {
        case OK:
            setIcon(StatusIcon.getNullIcon());
            break;
        case WARN:
            setIcon(StatusIcon.getWarnIcon());
            break;
        case FAIL:
            setIcon(StatusIcon.getFailIcon());
            break;
        }
        setText(result.getMessage());
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += X_ADJUST + PAD;
        d.height += 2 * PAD;
        return d;
    }

    public ValidateResult getResult() {
        return result;
    }
}
