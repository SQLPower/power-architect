package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * Provides all the redundant features of the LayoutNode interface.
 * You only have to implement the {@link #getBounds(Rectangle)},
 * {@link #getInboundEdges()}, and {@link #getOutboundEdges()},
 * {@link #getNodeName()}, and {@link #setBounds(int, int, int, int)}
 * methods.
 */
public abstract class AbstractLayoutNode implements LayoutNode {

    public abstract Rectangle getBounds(Rectangle b);
    public abstract List<LayoutEdge> getInboundEdges();
    public abstract List<LayoutEdge> getOutboundEdges();
    public abstract String getNodeName();
    public abstract void setBounds(int x, int i, int width, int height);

    public Rectangle getBounds() {
        return getBounds(new Rectangle());
    }

    public int getX() {
        return getBounds().x;
    }

    public int getY() {
        return getBounds().y;
    }

    public int getWidth() {
        return getBounds().width;
    }

    public int getHeight() {
        return getBounds().height;
    }

    public Point getLocation() {
        Rectangle bounds = getBounds();
        return new Point(bounds.x, bounds.y);
    }

    public void setLocation(int x, int y) {
        Rectangle bounds = getBounds();
        setBounds(x, y, bounds.width, bounds.height);
    }

    public void setLocation(Point pos) {
        Rectangle bounds = getBounds();
        setBounds(pos.x, pos.y, bounds.width, bounds.height);
    }

}
