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
