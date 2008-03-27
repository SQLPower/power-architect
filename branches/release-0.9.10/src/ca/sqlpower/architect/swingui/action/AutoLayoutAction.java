/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

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

    public AutoLayoutAction(ArchitectSwingSession session, String name, String description, String iconResourceName) {
        super(session, name, description, iconResourceName);
    }

    public AutoLayoutAction(ArchitectSwingSession session, String name, String description, Icon icon) {
        super(session, name, description, icon);
    }

	public void actionPerformed(ActionEvent evt) {
        
        // This funny construction creates a new instance of the current
        // type of layout.  It would be better to ask client code for a
        // layout factory, then use the factory to make new instances for
        // us.  Or just require implementations of ArchitectLayout to be
        // reusable.. every call to setUp could reset the layout.
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
