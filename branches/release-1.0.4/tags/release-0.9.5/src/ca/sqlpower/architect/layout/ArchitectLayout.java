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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

/**
 * The ArchitectLayout interface is a generic interface to a collection of
 * graph layout algorithms.  Its features include optional animation of the layout,
 * and setting an overall maximum boundary for the laid-out graph.
 * <p>
 * Example for using this interface:
 * <pre>
 *   ArchitectLayout layout = new MyFavouriteLayout();
 *   layout.setup(myNodes, myEdges, myMaximumBounds);
 *   while (!layout.isDone()) {
 *     layout.nextFrame();
 *     myComponent.paint();
 *   }
 * </pre>
 *
 * @author matt
 */
public interface ArchitectLayout {

	/**
	 * Sets up the layout algoritm.  You have to call this before attempting
     * to use an ArchitectLayout instance to perform a layout.
	 * 
     * TODO change the Rectangle into a point.  This should indicate where to start
     * but the layout should determine dimensions 
     * 
	 * @param nodes  The list of entities to be placed on  
	 * @param preferedFrames The prefered number of animation frames, may be ignored by the layout algorithm
	 */
	public void setup(Collection<? extends LayoutNode> nodes, Collection<? extends LayoutEdge> edges, Rectangle frame);
	
	public Dimension getNewArea(List<? extends LayoutNode> nodes);
	
	/**
	 * Interrupts the algorithm and node bounds in a consistent state.
	 */
	public void done();
	
	/**
	 * Returns true iff the layout is finished.
	 * 
	 * @return true if the layout algorithm is finished with the layout, false otherwise
	 */
	public boolean isDone();
	
	/**
	 * Updates all the node locations to correspond with the positions they should
     * have in the next frame.
	 */
	public void nextFrame();

}
