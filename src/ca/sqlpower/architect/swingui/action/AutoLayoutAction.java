/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
