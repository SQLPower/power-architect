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
package ca.sqlpower.architect.ddl;

import junit.framework.TestCase;

public class TestDDLUtils extends TestCase {

	public TestDDLUtils() {
	}
	
	public void testToQualifiedName(){
		String sampleName= "Some Name";
		String sampleCatalog="Catalog";
		String sampleSchema = "Schema";
		
		assertEquals("Qualified name incorrect when name only is passed in",DDLUtils.toQualifiedName(null,null,sampleName),sampleName);
		assertEquals("Qualified name incorrect when schema and name are passed in",DDLUtils.toQualifiedName(null,sampleSchema,sampleName),sampleSchema+"."+sampleName);
		assertEquals("Qualified name incorrect when catalog and name are passed in",DDLUtils.toQualifiedName(sampleCatalog,null,sampleName),sampleCatalog+"."+sampleName);
		assertEquals("Qualified name incorrect when all three parameters are passed in",DDLUtils.toQualifiedName(sampleCatalog,sampleSchema,sampleName),sampleCatalog+"."+sampleSchema+"."+sampleName);
		
	}
	
}
