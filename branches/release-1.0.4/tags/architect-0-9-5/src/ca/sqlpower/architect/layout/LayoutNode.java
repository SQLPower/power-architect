package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public interface LayoutNode {

    String getNodeName();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    Rectangle getBounds(Rectangle b);
    Rectangle getBounds();
    void setBounds(int x, int i, int width, int height);
    Point getLocation();
    void setLocation(int i, int j);
    void setLocation(Point pos);
    List<LayoutEdge> getOutboundEdges();
    List<LayoutEdge> getInboundEdges();

}
