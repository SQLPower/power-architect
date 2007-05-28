package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.Icon;

public abstract class StatusIcon implements Icon {

    private final static int DIAMETER = 15;

    public int getIconHeight() {
        return DIAMETER;
    }

    public int getIconWidth() {
        return DIAMETER;
    }
    private static ImageObserver dummyObserver = new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    };

    /** An error icon */
    private static final Icon FAIL_ICON = new StatusIcon() {
        final Image myImage = ASUtils.createIcon("stat_err_", "Failure", ArchitectFrame.DEFAULT_ICON_SIZE).getImage();
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
        }
    };

    /** A Warning Icon */
    private static final Icon WARN_ICON = new StatusIcon() {
        Image myImage = ASUtils.createIcon("stat_warn_", "Failure", ArchitectFrame.DEFAULT_ICON_SIZE).getImage();
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
        }
    };

    /** A blank icon of the right size, just to avoid resize flashing */
    private static final Icon NULL_ICON = new StatusIcon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // no painting required for null icon
        }
    };

    public static Icon getFailIcon() {
        return FAIL_ICON;
    }

    public static Icon getNullIcon() {
        return NULL_ICON;
    }

    public static Icon getWarnIcon() {
        return WARN_ICON;
    }

}
