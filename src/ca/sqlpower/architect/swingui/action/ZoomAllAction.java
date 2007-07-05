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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPenComponent;

public class ZoomAllAction extends AbstractArchitectAction {
    public ZoomAllAction(ArchitectSwingSession session) {
        super(session, "Zoom to fit", "Zoom to fit", "zoom_fit");
    }

    public void actionPerformed(ActionEvent e) {
        Rectangle rect = null;
        if ( playpen != null ) {
            for (int i = 0; i < playpen.getContentPane().getComponentCount(); i++) {
                PlayPenComponent ppc = playpen.getContentPane().getComponent(i);
                if ( rect == null ) {
                    rect = new Rectangle(ppc.getLocation(),ppc.getSize());
                }
                else {
                    rect.add(ppc.getBounds());
                }
            }
        }
    
        if ( rect == null )
            return;
    
        double zoom = Math.min(playpen.getViewportSize().getHeight()/rect.height,
                playpen.getViewportSize().getWidth()/rect.width);
        zoom *= 0.90;
    
        playpen.setZoom(zoom);
        playpen.scrollRectToVisible(playpen.zoomRect(rect));
    }
}