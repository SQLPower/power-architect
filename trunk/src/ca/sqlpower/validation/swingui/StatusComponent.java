package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

import ca.sqlpower.validation.ValidateResult;

/**
 * A Component that displays the success/failure result
 * with a textual message.
 * <p>
 * XXX Change display from drawing code to nice icons!
 */
public class StatusComponent extends JLabel {

    private final static int DIAMETER = 15;

    /** A red dot */
    private static final Icon FAIL_ICON = new Icon() {

        public int getIconHeight() {
            return DIAMETER;
        }

        public int getIconWidth() {
            return DIAMETER;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.RED);
            g.fillOval(x, y, DIAMETER, DIAMETER);
        }

    };

    /** A yellow dot */
    private static final Icon WARN_ICON = new Icon() {

        public int getIconHeight() {
            return DIAMETER;
        }

        public int getIconWidth() {
            return DIAMETER;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, DIAMETER, DIAMETER);
        }

    };

    /** A blank icon of the right size, just to avoid resize flashing */
    private static final Icon NULL_ICON = new Icon() {

        public int getIconHeight() {
            return DIAMETER;
        }

        public int getIconWidth() {
            return DIAMETER;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            // no painting required for null icon
        }
    };

    private static final int X_ADJUST = 20;
    private static final int PAD = 7;
    private ValidateResult result = null;

    public StatusComponent() {
        this("");
    }

    public StatusComponent(String text) {
        super(text);
        setIcon(NULL_ICON);
    }

    public void setResult(ValidateResult error) {
        result = error;
        if (result == null) {
            setIcon(NULL_ICON);
            super.setText("");
            return;
        }

        switch(result.getStatus()) {
        case OK:
            setIcon(NULL_ICON);
            break;
        case WARN:
            setIcon(WARN_ICON);
            break;
        case FAIL:
            setIcon(FAIL_ICON);
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
