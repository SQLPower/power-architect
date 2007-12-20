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
package ca.sqlpower.architect.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class FruchtermanReingoldForceLayout extends AbstractLayout {
	private static final Logger logger = Logger.getLogger(FruchtermanReingoldForceLayout.class);

	private ArrayList<LayoutNode> nodes;
    private ArrayList<LayoutNode> orphanedTables = new ArrayList<LayoutNode>();
	private ArrayList<LayoutEdge> edges;

	/**
	 * The radius that is kept clear
	 */
	private double k;

	/**
	 * The maximum movement 
	 */
	private double temp;

	/**
	 * Spacing multiplier
	 */
	private static final double SPACING_MULTIPLIER = 2;
	
	/**
	 *  The number of frames in a row that are considered stopped
	 */
	private int stoppedFrames;

	/**
	 * The amount of movement at the begining of the frame
	 * or away from the edge of the playpen
	 */
	private int  baseLineJitter;
	
	/**
	 * Used to be able to stop the algoithm prematurely
	 */
	private boolean overrideDone;

    private int frameNum = 0;
    
    private static final int ORPHAN_BUFFER = 50;
    
	public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle frame) {
		
	    this.frame = new Rectangle(frame);
              
	    //nodes.removeAll(noRelationTables);
		this.nodes = new ArrayList<LayoutNode>(nodes);
        
        for (LayoutNode tp: nodes) {
            if (tp.getOutboundEdges().size() == 0 && tp.getInboundEdges().size() == 0){
                orphanedTables.add(tp);
            }
        }
        
        this.nodes.removeAll(orphanedTables);
        
		this.edges = new ArrayList<LayoutEdge>(edges);

		
		baseLineJitter =10;
		temp = 1000*nodes.size();
		
		stoppedFrames =0;
		overrideDone = false;
		k = getEmptyRadius();
	}

	/**
	 * get the magnitude of a point
	 * @param p
	 * @return the magnitude
	 */
	public double magnitude(Point p) {
		return p.distance(0, 0);
	}

	/**
	 * Checks and sees if the program stops
	 */
	public boolean isDone() {
		if (stoppedFrames > 5 || (nodes.size() + orphanedTables.size() <2) || overrideDone) {

			return true;
		}
		return false;
	}

	/**
	 * Performs the next step of the spring layout
	 */
	public void nextFrame() {
        
        if (frameNum == 0) {
            ArchitectGridLayout gl = new ArchitectGridLayout();
            gl.setup(orphanedTables, Collections.EMPTY_LIST, frame);
            gl.done();
            
            int maxy = 0;
            for (LayoutNode tp : orphanedTables) {
                maxy = Math.max(tp.getY() + tp.getHeight(), maxy);
            }
            int orphanStartY = frame.height - maxy;
            logger.debug("max y is "+maxy+". orphanStartY is "+orphanStartY);
            for (LayoutNode tp : orphanedTables) {
                tp.setBounds(tp.getX(), tp.getY() + orphanStartY, tp.getWidth(), tp.getHeight());
            }
            frame.height -= maxy + ORPHAN_BUFFER;
        }
		
		HashMap<LayoutNode, Point> displacement;
		displacement = new HashMap<LayoutNode, Point>();
		
		for (LayoutNode tp : this.nodes) {
			displacement.put(tp, new Point((int)Math.round(Math.random()*baseLineJitter-baseLineJitter/2),+(int)Math.round(Math.random()*baseLineJitter-baseLineJitter/2)));
		}
		// Calculate repulsive forces
		if (nodes != null && !isDone()) {
			for (int ii = 0; ii < nodes.size(); ii++) {
				LayoutNode v = nodes.get(ii);
				Point disp = displacement.get(v);
				for (int jj = 0; jj < nodes.size(); jj++) {
					LayoutNode u = nodes.get(jj);
					if (u == v) continue;
                    
					while (v.getLocation().equals(u.getLocation())) {
					    v.setLocation((int) Math.round(Math.random()*5-3),
					                  (int) Math.round(Math.random()*5-3));
					}
					
					Point delta = displacementBetween(u, v);
					
					if (delta.distance(0,0) < 5*k) {
					    disp.translate(
					      (int) Math.round(delta.x / magnitude(delta) * repulsiveForce(magnitude(delta), u, v, k)),
					      (int) Math.round(delta.y / magnitude(delta) * repulsiveForce(magnitude(delta), u, v, k)));
					}

				}

			}
			// calculate Attractive force

			for (LayoutEdge e : edges) {
            
				LayoutNode v = e.getTailNode();
				LayoutNode u = e.getHeadNode();
				Point delta = displacementBetween(u, v);
                                 
				Point vDisp = displacement.get(v);
				if (vDisp == null) {
					break;
				}
				Point uDisp = displacement.get(u);
				if (uDisp == null) {
					break;
				}
				if(uDisp.equals(vDisp)) {
					vDisp.translate((int)Math.random()+1,(int)Math.random()+1 );
				}
				
				vDisp.translate(
                        -(int) Math.round(0.5 * (delta.x / magnitude(delta) * attractiveForce(delta, k))),
                        -(int) Math.round(0.5 * (delta.y / magnitude(delta) * attractiveForce(delta, k))));

				uDisp.translate(
                        (int) Math.round(0.5 * (delta.x / magnitude(delta) * attractiveForce(delta, k))),
                        (int) Math.round(0.5 * (delta.y / magnitude(delta) * attractiveForce(delta, k))));
			}

			boolean done = true;
			// add the forces to the current location
			for (LayoutNode v : nodes) {
				Point pos = (Point) v.getLocation().clone();
				Point disp = displacement.get(v);
				pos.translate(
                        (int) Math.round(disp.x / magnitude(disp) * Math.min(magnitude(disp), getTemp())),
                        (int) Math.round(disp.y / magnitude(disp) * Math.min(magnitude(disp), getTemp())));

                if (logger.isDebugEnabled()) {
                    logger.debug("Unmodified move x:"+pos.x+", y:"+pos.y);
                }
                
                int xJitter = (int) Math.round(Math.random()*baseLineJitter);
                int yJitter = (int) Math.round(Math.random()*baseLineJitter);
                pos.x = Math.min(Math.max(frame.x + xJitter, pos.x),
                                 getRightBoundary() - v.getWidth() - xJitter);
				pos.y = Math.min(Math.max(frame.y + yJitter, pos.y),
                                 getLowerBoundary() - v.getHeight() - yJitter);
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Moving table "+v+" to position "+pos);
                }

                if (pos.distance(v.getLocation()) > 2) {
					done = false;
				}
				v.setLocation(pos);
			}
			if (done) {
				stoppedFrames++;
			}
		}
		this.cool();
        frameNum++;
	}

    private Point displacementBetween(LayoutNode u, LayoutNode v) {
        Point dist = null;
     
        if (dist == null) {
            dist = new Point(
                    (v.getLocation().x + v.getWidth()/2) - (u.getLocation().x + u.getWidth()/2),
                    (v.getLocation().y + v.getHeight()/2) - (u.getLocation().y + u.getHeight()/2));
        }
        return dist;
    }
    
	/**
	 * Calculate the attractive force
	 * @param distance The distance between two nodes
	 * @param emptyRadius  The radius we want to keep clear
	 * @return the force between the two nodes
	 */
	protected double attractiveForce(Point distance, double emptyRadius) {
		double force;
		force =  (distance.x *distance.x + distance.y*distance.y) / emptyRadius;
		

		return force;
	}

	/**
	 * Calculate the Repulsive force
	 * @param distance The distance between two nodes
	 * @param emptyRadius  The radius we want to keep clear
	 * @return the force between the two nodes
	 */
	protected double repulsiveForce(double distance,LayoutNode u, LayoutNode v, double emptyRadius) {
        final double baseForce = .1;
        final double tableSizes = u.getHeight() + u.getWidth() + v.getHeight() + v.getWidth();
		double force;
		force = baseForce * tableSizes *( emptyRadius * emptyRadius ) / (distance * distance );

		return force;
	}
 

	/**
	 * The clear radius between two nodes
	 * @param pp a playen
	 * @return
	 */
	public double getEmptyRadius() {
			double radius =0;
			for (LayoutNode tp : nodes) {
				Rectangle b = tp.getBounds();
				radius +=b.height;
				radius +=b.width;
			}
            radius = radius/(nodes.size()); 
			return radius*SPACING_MULTIPLIER;
	}

	public void done() {
		overrideDone = true;
	}

	/**
	 * Cool the temperature
	 *
	 */
	public void cool() {	
		temp = temp/1.1;
	}
	
	public ArrayList<LayoutEdge> getEdges() {
		return edges;
	}

	public double getK() {
		return k;
	}

	public ArrayList<LayoutNode> getNodes() {
		return nodes;
	}

	public double getTemp() {
		return temp;
	}

	public int getH() {
		return frame.height;
	}

	public int getRightBoundary(){
		return frame.x+frame.width;
		
	}
	
	public int getLowerBoundary(){
		return frame.y+frame.height;
	}
	
	public int getW() {
		return frame.width;
	}

}
