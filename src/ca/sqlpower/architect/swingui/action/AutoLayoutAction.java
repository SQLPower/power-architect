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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Icon;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.ArchitectLayout;
import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.architect.layout.LayoutNode;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ContainerPane;
import ca.sqlpower.architect.swingui.LayoutAnimator;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;

public class AutoLayoutAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	private boolean animationEnabled = true;

	private ArchitectLayout layout;

	private int framesPerSecond = 25;

    public AutoLayoutAction(ArchitectSwingSession session, PlayPen playPen, String name, String description, String iconResourceName) {
        super(session, playPen, name, description, iconResourceName);
    }

    public AutoLayoutAction(ArchitectSwingSession session, PlayPen playPen, String name, String description, Icon icon) {
        super(session, playPen, name, description, icon);
    }

	public void actionPerformed(ActionEvent evt) {
        
	    logger.debug("Auto layout action starting...");
	    
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

        logger.debug("Created new Layout instance: " + layout);

        List<LayoutNode> nodes = new ArrayList<LayoutNode>();
        for (ContainerPane<?, ?> cp : getPlayPen().getSelectedContainers()) {
            if (cp instanceof LayoutNode) {
                nodes.add((LayoutNode) cp);
            }
        }
        
        // XXX the following block of code is a bit disturbing. Needs refactoring after we figure out what it's supposed to do.
        List<LayoutNode> notLaidOut = extractLayoutNodes(playpen);
        notLaidOut.removeAll(nodes);
        Point layoutAreaOffset = new Point();
        if (nodes.size() == 0 || nodes.size() == 1) {
            nodes = extractLayoutNodes(playpen);
        } else if (nodes.size() != extractLayoutNodes(playpen).size()){
            int maxWidth = 0;
            for (LayoutNode tp : notLaidOut) {
                int width = tp.getWidth() + tp.getX();
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
            layoutAreaOffset = new Point(maxWidth,0);
        }

        List<LayoutEdge> edges = extractLayoutEdges(playpen);
        logger.debug("About to do layout. nodes=" + nodes); //$NON-NLS-1$
        logger.debug("About to do layout. edges=" + edges); //$NON-NLS-1$


        Rectangle layoutArea = new Rectangle(layoutAreaOffset, layout.getNewArea(nodes));
        layout.setup(nodes, edges, layoutArea);
        LayoutAnimator anim = new LayoutAnimator(playpen, layout);
        anim.setAnimationEnabled(animationEnabled);
        anim.setFramesPerSecond(framesPerSecond);
        anim.startAnimation();
        
        Clip clip;
        try {
            if ( (evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/sounds/boingoingoingoingoing.wav")));
                clip.start();
            }
        } catch (Exception ex) {
            logger.debug("Couldn't play sound. Sigh.", ex);
        }
	}

    private static List<LayoutNode> extractLayoutNodes(PlayPen pp) {
        List<LayoutNode> nodes = new ArrayList<LayoutNode>();
        for (PlayPenComponent ppc : pp.getPlayPenComponents()) {
            if (ppc instanceof LayoutNode) {
                nodes.add((LayoutNode) ppc);
            }
        }
        return nodes;
    }

    private static List<LayoutEdge> extractLayoutEdges(PlayPen pp) {
        List<LayoutEdge> edges = new ArrayList<LayoutEdge>();
        for (PlayPenComponent ppc : pp.getPlayPenComponents()) {
            if (ppc instanceof LayoutEdge) {
                edges.add((LayoutEdge) ppc);
            }
        }
        return edges;
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
