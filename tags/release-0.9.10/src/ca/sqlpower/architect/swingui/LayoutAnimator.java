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
package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import ca.sqlpower.architect.layout.ArchitectLayout;

public class LayoutAnimator implements ActionListener {

	private PlayPen pp;
	private Timer timer;
	private ArchitectLayout layout;
    private boolean animationEnabled = true;
    private int framesPerSecond = 15;


    public LayoutAnimator(PlayPen pp, ArchitectLayout layout) {
		this.pp = pp;
		this.layout = layout;
	}
	
    public void startAnimation() {
        pp.startCompoundEdit("Auto Layout");
        if (!animationEnabled) {
            layout.done();
        } else {
            timer = new Timer( (int) (1.0 / ((double) framesPerSecond) * 1000.0), null);
            timer.addActionListener(this);
            timer.start();
        }
    }
    
	public void actionPerformed(ActionEvent e) {
		if (layout.isDone()) {
			timer.stop();
			layout.done();
			pp.endCompoundEdit("Layout animation finished");
		} else {
			layout.nextFrame();
			pp.revalidate();
		}
	}
    
	public ArchitectLayout getLayout() {
		return layout;
	}
    
	public boolean isAnimationEnabled() {
	    return animationEnabled;
	}
	
	public void setAnimationEnabled(boolean animationEnabled) {
	    this.animationEnabled = animationEnabled;
	}
	
	public int getFramesPerSecond() {
	    return framesPerSecond;
	}
	
	public void setFramesPerSecond(int framesPerSecond) {
	    this.framesPerSecond = framesPerSecond;
	}
}
