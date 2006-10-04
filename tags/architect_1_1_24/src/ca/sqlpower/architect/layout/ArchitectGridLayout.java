package ca.sqlpower.architect.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;

public class ArchitectGridLayout extends AbstractLayout  {
    private boolean hasBeenCalled = false;
    private static final int LINE_SEPERATOR = 100;
    private static final int SEPERATOR = 20;
    List<TablePane> tables;

    @Override
    public void setup(List<TablePane> nodes, List<Relationship> edges, Rectangle rect) {
        super.setup(nodes, edges, rect);
        tables = nodes;
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
        for (TablePane tp: tables) {
            if (d.width + tp.getWidth() > frame.x + frame.width && d.width != frame.x){                
                d.width = frame.x;
                d.height = d.height + maxHeight + LINE_SEPERATOR;
                maxHeight = 0;
            }
            tp.setLocation(d.width,d.height);
            d.width += tp.getWidth()+SEPERATOR;
            maxHeight = Math.max(maxHeight,tp.getHeight());
        }
    }

}
