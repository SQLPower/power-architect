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

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

import sun.font.FontManager;

public abstract class TestPlayPenComponent<T extends PlayPenComponent> extends TestCase {
	
	protected PlayPen pp;
	protected ArchitectSwingSession session;
	
	/**
	 * List of properties that differ in value between copy and original of this component.
	 */
	protected Set<String> copyIgnoreProperties = new HashSet<String>();
	
	/**
	 * List of properties that are shared (as same instance) between copy
	 * and original of this component.
	 */
	protected Set<String> copySameInstanceIgnoreProperties = new HashSet<String>();

	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        session = context.createSession();
		pp = session.getPlayPen();
	}

	
	/*
	 * Test method for 'ca.sqlpower.architect.swingui.PlayPenComponent.getPlayPen()'
	 */
	public void testGetPlayPen() {
		assertEquals("Wrong playpen added ", pp, getTarget().getPlayPen());
	}
	
	/**
	 * Returns the object under test. 
	 */
	protected abstract T getTarget();

	/**
	 * Returns an object from a call to the copy constructor of the target. 
	 */
	protected abstract T getTargetCopy();
	
	/**
     * Checks that the properties of an instance from the copy constructor are equal to the original.
     * In the case of a mutable property, it also checks that they don't share the same instance.
     * 
     * @throws Exception
     */
	public void testCopyConstructor() throws Exception {
	    PlayPenComponent comp = getTarget();
	    
	    List<PropertyDescriptor> settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(comp.getClass()));
	    
	    copyIgnoreProperties.add("UI");
	    copyIgnoreProperties.add("UIClassID");
	    copyIgnoreProperties.add("background");
	    copyIgnoreProperties.add("bounds");
	    copyIgnoreProperties.add("class");
	    copyIgnoreProperties.add("fontRenderContext");
	    copyIgnoreProperties.add("height");
	    copyIgnoreProperties.add("insets");
	    copyIgnoreProperties.add("location");
	    copyIgnoreProperties.add("opaque");
	    copyIgnoreProperties.add("parent");
	    copyIgnoreProperties.add("playPen");
	    copyIgnoreProperties.add("preferredLocation");
	    copyIgnoreProperties.add("preferredSize");
	    copyIgnoreProperties.add("selected");
	    copyIgnoreProperties.add("size");
	    copyIgnoreProperties.add("toolTipText");
	    copyIgnoreProperties.add("width");
	    copyIgnoreProperties.add("x");
	    copyIgnoreProperties.add("y");
        
        // no setters for this and it depends on the playpen's font
        copyIgnoreProperties.add("font");
        
        // not so sure if this should be duplicated, it's changed as the model properties changes
        copyIgnoreProperties.add("name");
        
        // copy and original should point to same business object
        copySameInstanceIgnoreProperties.add("model");
        
	    
        // First pass: set all settable properties, because testing the duplication of
        //             an object with all its properties at their defaults is not a
        //             very convincing test of duplication!
	    for (PropertyDescriptor property : settableProperties) {
	        if (copyIgnoreProperties.contains(property.getName())) continue;
	        Object oldVal;
	        try {
	            oldVal = PropertyUtils.getSimpleProperty(comp, property.getName());
	            // check for a setter
	            if (property.getWriteMethod() != null)
	            {
	                Object newVal = getNewDifferentValue(property, oldVal);
	                BeanUtils.copyProperty(comp, property.getName(), newVal);
	            }
	        } catch (NoSuchMethodException e) {
	            System.out.println("Skipping non-settable property "+property.getName()+" on "+comp.getClass().getName());
	        }
	    }
	    // Second pass get a copy make sure all of 
	    // the origional mutable objects returned from getters are different
	    // between the two objects, but have the same values. 
	    PlayPenComponent duplicate = getTargetCopy();
	    for (PropertyDescriptor property : settableProperties) {
	        if (copyIgnoreProperties.contains(property.getName())) continue;
	        Object oldVal;
	        try {
	            oldVal = PropertyUtils.getSimpleProperty(comp, property.getName());
	            Object copyVal = PropertyUtils.getSimpleProperty(duplicate, property.getName());
	            if (oldVal == null) {
	                throw new NullPointerException("We forgot to set "+property.getName());
	            } else {
	                assertEquals("The two values for property "+property.getDisplayName() + " in " + comp.getClass().getName() + " should be equal",oldVal,copyVal);

	                if (isPropertyInstanceMutable(property) && !copySameInstanceIgnoreProperties.contains(property.getName())) {
	                    assertNotSame("Copy shares mutable property with original, property name: " + property.getDisplayName(), copyVal, oldVal);
	                }
	            }
	        } catch (NoSuchMethodException e) {
	            System.out.println("Skipping non-settable property "+property.getName()+" on "+comp.getClass().getName());
	        }
	    }
	}

	/**
	 * Returns true if an instance of the given property type is of a mutable class.
	 * Throws an exception if it lacks a case for the given type.
	 * 
	 * @param property The property that should be checked for mutability.
	 */
	private boolean isPropertyInstanceMutable(PropertyDescriptor property) {
	    if (property.getPropertyType() == String.class) {
            return false;
        } else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE) {
            return false;
        } else if (property.getPropertyType() == Integer.class || property.getPropertyType() == Integer.TYPE) {
            return false;
        } else if (property.getPropertyType() == Color.class) {
            return false;
        } else if (property.getPropertyType() == Font.class) {
            return false;
        } else if (property.getPropertyType() == Point.class) {
            return true;
        } else if (property.getPropertyType() == Insets.class) {
            return true;
        } else if (property.getPropertyType() == Set.class) {
            return true;
        } else if (property.getPropertyType() == List.class) {
            return true;
        } else if (property.getPropertyType() == TablePane.class) {
            return true;
        } else if (property.getPropertyType() == SQLTable.class) {
            return true;
        } else if (property.getPropertyType() == JPopupMenu.class) {
            return true;
        }
	    if (property.getName().equals("model")) {
	        return true;
	    }
	    throw new RuntimeException("This test case lacks a value for "
	            + property.getName() + " (type "
	            + property.getPropertyType().getName() + ") in isPropertyInstanceMutable()");
    }


    /**
     * Returns a new value that is not equal to oldVal. The
     * returned object will be a new instance compatible with oldVal.  
     * 
     * @param property The property that should be modified.
     * @param oldVal The existing value of the property to modify.  The returned value
     * will not equal this one at the time this method was first called.
     */
	private Object getNewDifferentValue(PropertyDescriptor property, Object oldVal) throws ArchitectException {
	    Object newVal; // don't init here so compiler can warn if the
	    // following code doesn't always give it a value
	    if (property.getPropertyType() == String.class) {
	        newVal = "new " + oldVal;
	    } else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE) {
	        if (oldVal == null){
	            newVal = new Boolean(false);
	        } else {
	            newVal = new Boolean(!((Boolean) oldVal).booleanValue());
	        }
	    } else if (property.getPropertyType() == Integer.class || property.getPropertyType() == Integer.TYPE) {
	        if (oldVal == null) {
	            newVal = new Integer(0);
	        } else {
	            newVal = new Integer(((Integer) oldVal).intValue() + 1);
	        }
	    } else if (property.getPropertyType() == Color.class) {
	        if (oldVal == null) {
	            newVal = new Color(0xFAC157);
	        } else {
	            Color oldColor = (Color) oldVal;
	            newVal = new Color( (oldColor.getRGB()+0xF00) % 0x1000000);
	        }
	    } else if (property.getPropertyType() == Font.class) {
	        if (oldVal == null) {
	            newVal = FontManager.getDefaultPhysicalFont();
	        } else {
	            Font oldFont = (Font) oldVal;
                newVal = new Font(oldFont.getFontName(), oldFont.getSize() + 2, oldFont.getStyle());
	        }
	    } else if (property.getPropertyType() == Point.class) {
	        if (oldVal == null) {
	            newVal = new Point();
	        } else {
	            Point oldPoint = (Point) oldVal;
	            newVal = new Point(oldPoint.x + 10, oldPoint.y + 10);
	        }
	    } else if (property.getPropertyType() == Insets.class) {
	        if (oldVal == null) {
	            newVal = new Insets(0,0,0,0);
	        } else {
	            Insets oldInsets = (Insets) oldVal;
	            newVal = new Insets(oldInsets.top + 10, oldInsets.left + 10, oldInsets.bottom + 10, oldInsets.right + 10);
	        }
	    } else if (property.getPropertyType() == Set.class) {
	        newVal = new HashSet();
	        if (property.getName().equals("hiddenColumns")) {
	            ((Set) newVal).add(new SQLColumn());
	        } else {
	            ((Set) newVal).add("Test");
	        }
	    } else if (property.getPropertyType() == List.class) {
	        newVal = new ArrayList();
	        if (property.getName().equals("selectedColumns")) {
	            ((List) newVal).add(new SQLColumn());
	        } else {
	            ((List) newVal).add("Test");
	        }
	    } else if (property.getPropertyType() == TablePane.class) {
	        SQLTable t = new SQLTable();
	        t.initFolders(true);
	        newVal = new TablePane(t, pp);
	    } else if (property.getPropertyType() == SQLTable.class) {
	        newVal = new SQLTable();
	        ((SQLTable)newVal).initFolders(true);
	    } else {
	        throw new RuntimeException("This test case lacks a value for "
	        + property.getName() + " (type "
	        + property.getPropertyType().getName() + ") in getNewDifferentValue()");
	    }

	    return newVal;
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
