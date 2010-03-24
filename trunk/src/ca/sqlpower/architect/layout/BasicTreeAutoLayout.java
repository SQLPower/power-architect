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
package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class BasicTreeAutoLayout extends AbstractLayout {
	private static final Logger logger = Logger.getLogger(BasicTreeAutoLayout.class);

	private boolean animationEnabled = true;

	/**
	 * The number of frames to try for in the repositioning animation.
	 */
	private int numFramesInAnim = 50;
	
	private Map<LayoutNode, Point> newLocations;
	private Map<LayoutNode, Point> origLocations;

	private int frame =0;
	public BasicTreeAutoLayout() {

	}

    /**
     * 
     * @param nodes
     * @param startPoint
     * @param alreadyDone A Map of nodes to their final positions (as calculated by this layout).
     * @return
     */
	private Point doRecursiveLayout(Collection<? extends LayoutNode> nodes, Point startPoint, Map<LayoutNode,Point> alreadyDone) {
		Rectangle b = new Rectangle();
		int x = startPoint.x;
		int y = startPoint.y;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Starting layout. nodeList="+nodes+"; startPoint="+startPoint);
        }
        
		for (LayoutNode node : nodes) {
			if (alreadyDone.containsKey(node)) continue;
			
			// place this table
			node.getBounds(b);
			Point newLoc = new Point(x, y);
			alreadyDone.put(node, newLoc);

			List<LayoutNode> adjacentNodes = new ArrayList<LayoutNode>();
			for (LayoutEdge edge : node.getOutboundEdges()) {
			    LayoutNode adjacentNode = edge.getHeadNode();
                if (!alreadyDone.containsKey(adjacentNode)) {
                    adjacentNodes.add(adjacentNode);
                }
			}
            for (LayoutEdge edge : node.getInboundEdges()) {
                LayoutNode adjacentNode = edge.getTailNode();
                if (!alreadyDone.containsKey(adjacentNode)) {
                    adjacentNodes.add(adjacentNode);
                }
            }

			// place the related tables to the right
			Point finishPoint = doRecursiveLayout(adjacentNodes, new Point(x + b.width + 60, y), alreadyDone);
			
			x = startPoint.x;
			y = Math.max(y + b.height + 10, finishPoint.y);
		}
		return new Point(x, y);
	}
	
	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}

	@Override
	public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle frame) {
		origLocations = new HashMap<LayoutNode, Point>();
		
		for (LayoutNode node : nodes) {
			origLocations.put(node, node.getLocation());
		}
		Point p = new Point();
		newLocations = new HashMap<LayoutNode, Point>();
        
		doRecursiveLayout(nodes, p, newLocations);

		if (logger.isDebugEnabled()) {
		    for (Map.Entry<LayoutNode, Point> entry : newLocations.entrySet()) {
		        LayoutNode node = entry.getKey();
		        Point newLoc = entry.getValue();
		        Point oldLoc = origLocations.get(node);

		        logger.debug("Table "+node.getNodeName()+": old="+oldLoc.x+","+oldLoc.y+"; new="+newLoc.x+","+newLoc.y);
		    }
		}
	}

	public void done() {
		for (Map.Entry<LayoutNode, Point> entry : newLocations.entrySet()) {
			entry.getKey().setLocation(entry.getValue().x, entry.getValue().y);
		}
		frame = numFramesInAnim;
	}

	public boolean isDone() {
		return frame >= numFramesInAnim;
	}

	public void nextFrame() {
		frame++;
		double progress = ((double) frame) / ((double) numFramesInAnim);
		logger.debug(progress);
		for (Map.Entry<LayoutNode, Point> entry : newLocations.entrySet()) {
            LayoutNode node = entry.getKey();
			Point newLoc = entry.getValue();
			Point oldLoc = origLocations.get(node);
			
			int x = (int) (oldLoc.x + (double) (newLoc.x - oldLoc.x) * progress);
			int y = (int) (oldLoc.y + (double) (newLoc.y - oldLoc.y) * progress);
			
			node.setLocation(x, y);
		}
	}
}
