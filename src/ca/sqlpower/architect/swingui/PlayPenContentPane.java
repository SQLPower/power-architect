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
package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.event.PlayPenComponentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenComponentListener;

public class PlayPenContentPane {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	protected PlayPen owner;
	private List<PlayPenComponent> children = new ArrayList<PlayPenComponent>();
	private List<Relationship> relations = new ArrayList<Relationship>();
	private List<PlayPenComponentListener> playPenComponentListeners = new ArrayList<PlayPenComponentListener>();
	private PlayPenComponentEventPassthrough playPenComponentEventPassthrough;


	public PlayPenContentPane(PlayPen owner) {
		this.owner = owner;
		playPenComponentEventPassthrough = new PlayPenComponentEventPassthrough();
		owner.addPropertyChangeListener("zoom", new ZoomFixer());
	}
	
	
	/**
	 * Returns the PlayPen that this content pane belongs to.
	 */
	public PlayPen getOwner() {
		return owner;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public boolean contains(int x, int y) {
		return true;
	}

	/**
	 * Returns true.
	 */
	public boolean isValidateRoot() {
		logger.debug("isValidateRoot returning true");
		return true;
	}

	/**
	 * Looks for tooltip text in the component under the pointer,
	 * respecting the current zoom level.
	 */
	public String getToolTipText(MouseEvent e) {
		String text = null;
		PlayPenComponent c = getComponentAt(e.getPoint());
		if (c != null) {
			text = c.getToolTipText();
		}
		logger.debug("Checking for tooltip component at "+e.getPoint()+" is "+c+". tooltipText is "+text);
		return text;
	}

	/**
	 *  Allows you to return the component that is at point p.  Since relations are always last 
	 *  If a non-relationship is at the same point it gets picked first. 
	 */
	public PlayPenComponent getComponentAt(Point p) {
		for (PlayPenComponent ppc : children) {
			if (ppc.contains(p)) {
				return ppc;
			}
		}
		for (Relationship ppc : relations) {
			if (ppc.contains(p)) {
				return (PlayPenComponent) ppc;
			}
		}
		return null;
	}


	/**
	 * Get the index of the first relation
	 */
	public int getFirstRelationIndex() {
		return children.size();
	}
	
	/**
	 * get the total number of components
	 */
	public int getComponentCount() {
		return children.size()+relations.size();
	}

	/**
	 * Get a component at position i.  
	 * 
	 * Note: All relations are at the end of the list 
	 */
	public PlayPenComponent getComponent(int i) {
		if (i < children.size()){
			return children.get(i);
		} else {
			return relations.get(i-children.size());
		}
		
	}

	/**
	 * Add a new component to the content pane.  
	 * 
	 * Note relations must be added after <code>getFirstrelaationIndex</code> and all others before
	 */
	public void add(PlayPenComponent c, int i) {
		if (c instanceof Relationship) {
			relations.add(i-children.size(),(Relationship)c);
		} else {
			children.add(i,c);
		}
		c.addPlayPenComponentListener(playPenComponentEventPassthrough);
		c.addSelectionListener(getOwner());
		c.revalidate();
	}

	
	/**
	 * removes the component at index j
	 */
	public void remove(int j) {
		PlayPenComponent c;
		if (j < children.size()) {
			 c= children.get(j);
		} else {
			c = relations.get(j-children.size());
		}
		
		Rectangle r = c.getBounds();
		c.removePlayPenComponentListener(playPenComponentEventPassthrough);
		c.removeSelectionListener(getOwner());
		if (j < children.size()) {
			children.remove(j);
		} else {
			relations.remove(j-children.size());
		}
		getOwner().repaint(r);
	}
	
	public void remove(PlayPenComponent c) {
		int j = children.indexOf(c);
		if ( j >= 0 ) remove(j);
		j= relations.indexOf(c);
		if (j>=0) remove(j+children.size());
	}

	/**
	 * Fixes table pane sizes after the play pen's zoom changes (because
	 * fonts render at different sizes in different zoom levels).
	 */
	private class ZoomFixer implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			for (PlayPenComponent ppc : children) {
				// only table panes will need validation because they have text
				if (! (ppc instanceof Relationship)) ppc.revalidate();
			}
		}
	}
	
	// ----------------- PlayPenComponentListener Passthrough stuff ---------------------------
	public void addPlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.add(l);
	}
	
	public void removePlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.remove(l);
	}
	
	private void refirePlayPenComponentMoved(PlayPenComponentEvent e) {
		for (PlayPenComponentListener l : playPenComponentListeners) {
			l.componentMoved(e);
		}
	}

	private void refirePlayPenComponentResized(PlayPenComponentEvent e) {
		for (PlayPenComponentListener l : playPenComponentListeners) {
			l.componentResized(e);
		}
	}
	
	private class PlayPenComponentEventPassthrough implements PlayPenComponentListener {

		public void componentMoved(PlayPenComponentEvent e) {
			refirePlayPenComponentMoved(e);
		}

		public void componentResized(PlayPenComponentEvent e) {
			refirePlayPenComponentResized(e);
		}

	}
}
