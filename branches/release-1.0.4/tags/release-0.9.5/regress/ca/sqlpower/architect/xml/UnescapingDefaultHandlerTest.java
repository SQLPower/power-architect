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
package ca.sqlpower.architect.xml;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class UnescapingDefaultHandlerTest extends TestCase {

    /**
     * The DefaultHandler implementation that we're testing here
     */
    private UnescapingDefaultHandler defaultHandler;
    
    /**
     * A stub implementaion of DefaultHandler that we're wrapping with the UnescapingDefaultHandler
     */
    private StubDefaultHandler stubHandler;
    
    protected void setUp() throws Exception {
        super.setUp();
        stubHandler = new StubDefaultHandler();
        defaultHandler = new UnescapingDefaultHandler(stubHandler);
    }

    public void testCharacters() throws SAXException {
        String testString = "abc\\u0000123";
        
        defaultHandler.characters(testString.toCharArray(), 2, 10);
        
        // Note, these test for correct behaviour given the current
        // implementation.  If we modify or improve the implementation,
        // this test may fail even though the implementation is correct.
        assertEquals("c\u0000123", stubHandler.string);
        assertEquals(0, stubHandler.start);
        assertEquals(5, stubHandler.length);
    }

    public void testStartElementStringStringStringAttributes() throws SAXException {
        StubAttributes attr = new StubAttributes();
        defaultHandler.startElement("", "", "", attr);
        
        assertEquals(UnescapingAttributes.class, stubHandler.attr.getClass());
    }

}
