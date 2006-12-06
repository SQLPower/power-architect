package ca.sqlpower.validation.swingui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.StatusIcon;
import ca.sqlpower.validation.ValidateResult;

/**
 * A Component that displays the success/failure result
 * with a textual message.
 */
public class StatusComponent extends JLabel {
    
    private static final Logger logger = Logger.getLogger(StatusComponent.class);

    /**
     * The border this component has by default.  If you want a different
     * border when you use it, just call setBorder().
     * 
     */
    private static final Border DEFAULT_BORDER =
        BorderFactory.createEmptyBorder(7, 0, 7, 0);
    
    private ValidateResult result = null;

    /**
     * Creates a new StatusComponent with no visible display, but
     * which takes up the same amount of space as it would if it
     * was displaying an icon and message.
     */
    public StatusComponent() {
        setBorder(DEFAULT_BORDER);
        setResult(null);
    }

    public void setResult(ValidateResult error) {
        result = error;

        String text;
        Icon icon;
        if (result == null) {
            text = null;
            icon = StatusIcon.getNullIcon();
        } else {
            text = result.getMessage();
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
        }
        setText(text);
        setIcon(icon);
    }

    /**
     * Takes the given text and prepends "&lt;html&gt;" before passing to JLabel's
     * setText() method.  If the given string is null or whitespace-only, it is
     * treated as a non-breaking space (to ensure the height of this component won't
     * change when it is used with and without visible text). 
     */
    @Override
    public void setText(String text) {
        if (text == null || text.trim().length() == 0) {
            text = "&nbsp;";
        }
        super.setText("<html>"+text);
    }

    public ValidateResult getResult() {
        return result;
    }
}
