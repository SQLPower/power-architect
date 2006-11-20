package ca.sqlpower.validation.swingui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.Icon;
import javax.swing.JLabel;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.validation.ValidateResult;

/**
 * A Component that displays the success/failure result
 * with a textual message.
 * <p>
 * XXX Change display from drawing code to nice icons!
 */
public class StatusComponent extends JLabel {

    private final static int DIAMETER = 15;

    private static ImageObserver dummyObserver = new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    };

    /** A red dot */
    private static final Icon FAIL_ICON = new Icon() {
        final Image myImage = ASUtils.createIcon("stat_err_", "Failure", 16).getImage();

        public int getIconHeight() {
            return DIAMETER;
        }

        public int getIconWidth() {
            return DIAMETER;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
        }

    };

    /** A yellow dot */
    private static final Icon WARN_ICON = new Icon() {
        Image myImage = ASUtils.createIcon("stat_warn_", "Failure", 16).getImage();

        public int getIconHeight() {
            return DIAMETER;
        }

        public int getIconWidth() {
            return DIAMETER;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
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
