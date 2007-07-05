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
package ca.sqlpower.architect.ddl;

import junit.framework.TestCase;

public class TestDDLUtils extends TestCase {


	
	public TestDDLUtils() {
		
	}
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
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
