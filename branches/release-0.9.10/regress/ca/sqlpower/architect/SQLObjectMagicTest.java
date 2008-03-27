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
package ca.sqlpower.architect;

import junit.framework.TestCase;

/**
 * Test the MagicEnabled stuff in SQLObject
 */
public class SQLObjectMagicTest extends TestCase {

	SQLObject parent;
	SQLObject child = new SQLTable.Folder<SQLColumn>(SQLTable.Folder.COLUMNS, false);
	
	@Override
	protected void setUp() throws Exception {
        parent  = new SQLTable();
		parent.addChild(child);
	}
	
	public void testOneLevel() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
		
	}
	
	public void testMultipleDisables() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		child.setMagicEnabled(false);	// twice in a row, just to be sure :-)
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
	}
	
	public void testParentChild() {
		assertTrue(child.isMagicEnabled());
		parent.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled()); // Because parent still magic-enabled
		parent.setMagicEnabled(true);
		try {
			child.setMagicEnabled(true);		// Should object
		} catch (Throwable e) { 
			System.out.println("Caught expected: " + e);
		}
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());	// finally
	}
	
}
