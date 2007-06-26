package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.LayoutAnimator;
import ca.sqlpower.architect.swingui.PlayPen;

public class AutoLayoutAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	private boolean animationEnabled = true;

	private ArchitectLayout layout;

	private int framesPerSecond = 25;

	public AutoLayoutAction(ArchitectSwingSession session) {
		super(session, "Auto Layout", "Automatic Layout", "auto_layout");
	}

	public void actionPerformed(ActionEvent evt) {
        
        // not sure what the hell is up with this.
        try {
            layout = layout.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // ok, if it's broken, let's just fail silently and pretend the user missed the button. (that was sarcasm)
		if (layout != null) {
			List<? extends LayoutNode> tablePanes = new ArrayList(playpen.getSelectedTables());
            List<LayoutNode> notLaidOut = new ArrayList<LayoutNode>(playpen.getTablePanes());
            notLaidOut.removeAll(tablePanes);
 			Point layoutAreaOffset = new Point();
			if (tablePanes.size() == 0 || tablePanes.size() == 1) {
				tablePanes = playpen.getTablePanes();
			} else if (tablePanes.size() != playpen.getTablePanes().size()){
				int maxWidth =0;
				for (LayoutNode tp : notLaidOut){
					int width = tp.getWidth()+tp.getX();
					if (width > maxWidth) {
						maxWidth = width;
					}
				}
				layoutAreaOffset = new Point(maxWidth,0);
			}

			List<? extends LayoutEdge> relationships = playpen.getRelationships();
			logger.debug("About to do layout. tablePanes="+tablePanes);
			logger.debug("About to do layout. relationships="+relationships);


			Rectangle layoutArea = new Rectangle(layoutAreaOffset, layout.getNewArea(tablePanes));
			layout.setup(tablePanes, relationships, layoutArea);
            LayoutAnimator anim = new LayoutAnimator(playpen, layout);
            anim.setAnimationEnabled(animationEnabled);
            anim.setFramesPerSecond(framesPerSecond);
			anim.startAnimation();
		}
	}

	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}

	public ArchitectLayout getLayout() {
		return layout;
	}

	public void setLayout(ArchitectLayout layout) {
		this.layout = layout;
	}
    
    /**
     * FIXME: Not sure if this is needed anywhere outside of testing. 
     */
    public PlayPen getPlayPen() {
        return this.playpen;
    }
}
