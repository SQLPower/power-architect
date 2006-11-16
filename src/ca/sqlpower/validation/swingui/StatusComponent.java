package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

import ca.sqlpower.validation.Status;

/**
 * A Component that displays the success/failure status
 * with a textual message.
 * <p>
 * XXX Change display from drawing code to nice icons!
 */
public class StatusComponent extends JLabel {

    /** A red dot */
    private static final Icon FAIL_ICON = new Icon() {

        private final static int DIAMETER = 15;
        
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

        private final static int DIAMETER = 15;
        
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
    
    private static final int X_ADJUST = 20;
    private static final int Y_ADJUST = -15;
    private static final int PAD = 7;
    private Status status = Status.OK;
    
    // private ImageIcon errorIcon = ASUtils.createIcon("general/Error",
    //        "Error Icon", 16);

    public StatusComponent() {
        this("");
    }


    public StatusComponent(String text) {
        super(text);        

    }


    /**
     * Set the text to be displayed; note that it is not necessary
     * to nullify the text when the error is cleared, as calling
     * setStatus(Status.OK) suppresses display of the text.
     */
    public void setText(String text) {
        super.setText(text);
    }

    public void setStatus(Status error) {
        this.status = error;
        switch(status) {
        case OK:
            setIcon(null);
            return;
        case WARN:
            setIcon(WARN_ICON);
            break;
        case FAIL:
            setIcon(FAIL_ICON);
            break;
        }
        repaint();
    }

    /**
     * Draw the error icon and the message if needed;
     * note that the text will not be drawn if !isError().
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */

    protected void paintComponent(Graphics g) {        
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += X_ADJUST;
        d.width += PAD;
        d.height += 2 * PAD;
        return d;
    }
}
