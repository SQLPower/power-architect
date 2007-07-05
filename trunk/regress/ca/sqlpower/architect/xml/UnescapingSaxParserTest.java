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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UnescapingSaxParserTest extends TestCase {

    /**
     * This is the escaping parser implementation we're testing.
     */
    private UnescapingSaxParser unescapingParser;
    
    /**
     * This is a stub that we create for the escaping parser to
     * wrap.  We can examine this stub in the test cases to make
     * sure the escaping parser wrapper is behaving properly.
     */
    private StubSAXParser stubParser;
    
    protected void setUp() throws Exception {
        super.setUp();
        stubParser = new StubSAXParser();
        unescapingParser = new UnescapingSaxParser(stubParser);
    }

    public void testGetXMLReader() throws SAXException {
        assertEquals(UnescapingXMLReader.class, unescapingParser.getXMLReader().getClass());
    }

    public void testParseFileDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((File) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputSourceDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputSource) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputStreamDefaultHandlerString() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputStream) null, handler, "");
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputStreamDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputStream) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseStringDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((String) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

}
