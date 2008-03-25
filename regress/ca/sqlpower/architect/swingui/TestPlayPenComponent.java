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

import junit.framework.TestCase;

public class TestPlayPenComponent extends TestCase {
	
	PlayPenComponentImpl component;
	PlayPen pp;
	PlayPenComponentEventCounter eventCounter;

	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
		pp = new PlayPen(session);
		component = new PlayPenComponentImpl(pp.getPlayPenContentPane() );
		eventCounter = new PlayPenComponentEventCounter();
	}

	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getPlayPen()'
	 */
	public void testGetPlayPen() {
		
		assertEquals("Wrong playpen added ", pp,component.getPlayPen());

	}

	
	public void testMovement() {
		component.addPlayPenComponentListener( eventCounter);
		assertEquals("" +
				"We started out with the wrong number of events", 0,eventCounter.getEvents() );
		//component.setMoving(true);
		//assertEquals("We did not generate a move start event",1,eventCounter.getStarts());
		pp.startCompoundEdit("Starting move");
		component.setLocation(1,1);
		component.setLocation(2,2);
		pp.endCompoundEdit("Ending move");
		assertEquals("Even in Compound edits should still generate a move event for each setLocation",2,eventCounter.getMoved());
		//component.setMoving(false);
		//assertEquals("We did not generate a move end event",1,eventCounter.getEnds());
		
		component.setLocation(3,3);
		//assertEquals("We did not generate a move start event",2,eventCounter.getStarts());
		assertEquals("We did not generate move events",3,eventCounter.getMoved());
		//assertEquals("We did not generate a move end event",2,eventCounter.getEnds());
	}
	
	
	//TODO Add test cases for these functions
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getUI()'
	 */
	public void testGetUI() {

		
	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setUI(PlayPenComponentUI)'
	 */
	public void testSetUI() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.showPopup(JPopupMenu, Point)'
	 */
	public void testShowPopup() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.revalidate()'
	 */
	public void testRevalidate() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setBounds(int, int, int, int)'
	 */
	public void testSetBounds() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getBounds()'
	 */
	public void testGetBounds() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getBounds(Rectangle)'
	 */
	public void testGetBoundsRectangle() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getSize()'
	 */
	public void testGetSize() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getPreferredLocation()'
	 */
	public void testGetPreferredLocation() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getLocation()'
	 */
	public void testGetLocation() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getLocation(Point)'
	 */
	public void testGetLocationPoint() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setLocation(Point)'
	 */
	public void testSetLocationPoint() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setLocation(int, int)'
	 */
	public void testSetLocationIntInt() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.repaint()'
	 */
	public void testRepaint() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.repaint(Rectangle)'
	 */
	public void testRepaintRectangle() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getName()'
	 */
	public void testGetName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setName(String)'
	 */
	public void testSetName() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.addPropertyChangeListener(PropertyChangeListener)'
	 */
	public void testAddPropertyChangeListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.removePropertyChangeListener(PropertyChangeListener)'
	 */
	public void testRemovePropertyChangeListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.firePropertyChange(String, Object, Object)'
	 */
	public void testFirePropertyChangeStringObjectObject() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.firePropertyChange(String, int, int)'
	 */
	public void testFirePropertyChangeStringIntInt() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.addPlayPenComponentListener(PlayPenComponentListener)'
	 */
	public void testAddPlayPenComponentListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.removePlayPenComponentListener(PlayPenComponentListener)'
	 */
	public void testRemovePlayPenComponentListener() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.firePlayPenComponentMoved(Point, Point)'
	 */
	public void testFirePlayPenComponentMoved() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.firePlayPenComponentResized()'
	 */
	public void testFirePlayPenComponentResized() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getX()'
	 */
	public void testGetX() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getY()'
	 */
	public void testGetY() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getWidth()'
	 */
	public void testGetWidth() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getHeight()'
	 */
	public void testGetHeight() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getInsets()'
	 */
	public void testGetInsets() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setInsets(Insets)'
	 */
	public void testSetInsets() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.repaint(long, int, int, int, int)'
	 */
	public void testRepaintLongIntIntIntInt() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.isOpaque()'
	 */
	public void testIsOpaque() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setOpaque(boolean)'
	 */
	public void testSetOpaque() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getBackground()'
	 */
	public void testGetBackground() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setBackground(Color)'
	 */
	public void testSetBackground() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getForeground()'
	 */
	public void testGetForeground() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setForeground(Color)'
	 */
	public void testSetForeground() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getToolTipText()'
	 */
	public void testGetToolTipText() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setToolTipText(String)'
	 */
	public void testSetToolTipText() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getFont()'
	 */
	public void testGetFont() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getFontMetrics(Font)'
	 */
	public void testGetFontMetrics() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getFontRenderContext()'
	 */
	public void testGetFontRenderContext() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.contains(Point)'
	 */
	public void testContains() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.paint(Graphics2D)'
	 */
	public void testPaint() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getPreferredSize()'
	 */
	public void testGetPreferredSize() {

	}

	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.setSize(Dimension)'
	 */
	public void testSetSize() {

	}

}
