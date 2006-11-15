package ca.sqlpower.validation.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;

import ca.sqlpower.validation.Status;

/**
 * A Component that displays the success/failure status
 * with a textual message.
 * <p>
 * XXX Change from boolean to enum Status.
 */
public class StatusComponent extends JComponent {

    private static final int X_ADJUST = 20;
    private static final int Y_ADJUST = -15;
    private static final int PAD = 7;
    private Status status = Status.OK;
    final private JLabel label;
    // private ImageIcon errorIcon = ASUtils.createIcon("general/Error",
    //        "Error Icon", 16);

    public StatusComponent() {
        this(new JLabel("*"));
    }

    public StatusComponent(String text) {
        this(new JLabel(text));
    }

    public StatusComponent(JLabel label) {
        setLayout(null);
        this.label = label;
        add(label);
        // XXX FIXME the label is not really drawing, we are, so
        // can we get rid of the label?
        label.setBackground(Color.GREEN);
        label.setLocation(X_ADJUST, Y_ADJUST);
        label.setSize(label.getPreferredSize());
    }


    /**
     * Set the text to be displayed; note that it is not necessary
     * to nullify the text when the error is cleared, as calling
     * setError(false) suppresses display of the text.
     */
    public void setText(String text) {
        // System.out.printf("StatusComponent.setText(%s)", text);
        label.setText(text);
        label.repaint();
    }

    public void setStatus(Status error) {
        this.status = error;
        repaint();
    }

    /**
     * Draw the error icon and the message if needed;
     * note that the text will not be drawn if !isError().
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */

    protected void paintComponent(Graphics g) {
        Insets insets = getInsets();
        Color drawColor = null;
        switch(status) {
        case OK:
            return;
        case WARN:
            drawColor = Color.YELLOW;
            break;
        case FAIL:
            drawColor = Color.RED;
            break;
        }

        Color oldColor = g.getColor();
        g.setColor(drawColor);
        //g.drawImage(errorIcon.getImage(), 0, 0, 16, 16, this);
        g.fillOval(insets.left, 0-insets.top, 16, 16);
        g.drawString(label.getText(), X_ADJUST, 16);
        g.setColor(oldColor);
        super.paintComponent(g);
        label.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = label.getPreferredSize();
        d.width += X_ADJUST;
        d.width += PAD;
        d.height += 2 * PAD;
        return d;
    }
}
