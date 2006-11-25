package ca.sqlpower.validation.swingui;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.StatusIcon;
import ca.sqlpower.validation.ValidateResult;

/**
 * A Component that displays the success/failure result
 * with a textual message.
 */
public class StatusComponent extends JLabel {
    
    private static final Logger logger = Logger.getLogger(StatusComponent.class);

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
        Point p = new Point(0,0);
        SwingUtilities.convertPointToScreen(p, this);
        logger.debug("     location on screen="+p);
        result = error;

        String text;
        Icon icon;
        if (result == null) {
            icon = StatusIcon.getNullIcon();
            text = "";
        } else {
            switch(result.getStatus()) {
            case OK:
                icon = StatusIcon.getNullIcon();
                break;
            case WARN:
                icon = StatusIcon.getWarnIcon();
                break;
            case FAIL:
                icon = StatusIcon.getFailIcon();
                break;
            default:
                icon = StatusIcon.getNullIcon();
            }
            text = result.getMessage();
        }
        setText(text);
        setIcon(icon);
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
