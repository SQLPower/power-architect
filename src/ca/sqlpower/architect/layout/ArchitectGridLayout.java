package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArchitectGridLayout extends AbstractLayout  {
    private boolean hasBeenCalled = false;
    private static final int LINE_SEPERATOR = 100;
    private static final int SEPERATOR = 20;
    private List<? extends LayoutNode> nodes;

    @Override
    public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle rect) {
        super.setup(nodes, edges, rect);
        this.nodes = new ArrayList<LayoutNode>(nodes);
    }
    
    public void done() {
         this.nextFrame();  
    }

    public boolean isDone() {
        return !hasBeenCalled;
    }

    public void nextFrame() {
        hasBeenCalled = true;
        Dimension d = new Dimension(frame.x,frame.y);
        int maxHeight = 0;
        for (LayoutNode node: nodes) {
            if (d.width + node.getWidth() > frame.x + frame.width && d.width != frame.x){                
                d.width = frame.x;
                d.height = d.height + maxHeight + LINE_SEPERATOR;
                maxHeight = 0;
            }
            node.setLocation(d.width,d.height);
            d.width += node.getWidth()+SEPERATOR;
            maxHeight = Math.max(maxHeight,node.getHeight());
        }
    }

}
