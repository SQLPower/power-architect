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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.helpers.DefaultHandler;

public class XMLHelperTest extends TestCase {

    private XMLHelper helper;
    
    protected void setUp() throws Exception {
        super.setUp();
        helper = new XMLHelper();
    }

    /**
     * Asserts that unescaping the escaped string produces the unescaped
     * string, and vice-versa.
     */
    private void checkBothWays(String unescaped, String escaped) {
        assertEquals(unescaped, XMLHelper.unescape(escaped));
        assertEquals(escaped, XMLHelper.escape(unescaped));
    }
    
    /**
     * Test that the null character is properly escaped and unescaped 
     *
     */
    public void testNull() {
        String escaped = "My \\u0000 String";
        String unescaped = "My \u0000 String";
        checkBothWays(unescaped, escaped);
    }

    /**
     * Test that a low control character is properly escaped and unescaped 
     *
     */
    public void testLowControl() {
        String escaped = "My \\u0008 String";
        String unescaped = "My \u0008 String";
        checkBothWays(unescaped, escaped);
    }

    /**
     * Test that an unpaired surrogate character is properly escaped and unescaped 
     *
     */
    public void testUnpairedSurrogate() {
        String escaped = "My \\ud800 String";
        String unescaped = "My \ud800 String";
        checkBothWays(unescaped, escaped);
    }

    /**
     * Test to ensure that escape doesn't escape any legal characters
     *
     */
    public void testAllLegalXMLCharsString() {
        StringBuilder testString = new StringBuilder();
          
        for (int a = 0; a < 0x10000; a++) {
            // If a is the hex code for a legal XML character, then append
            if (a == 0x09 || a == 0x0a || a == 0x0d ||
                    (a >= 0x20 && a <= 0xd7ff) ||
                    (a >= 0xe000 && a <=0xfffd))
            testString.append((char)a);
        }
        
        // Expecting that the 'escaped' string and the original to be the same
        checkBothWays(testString.toString(), testString.toString());
    }

    /**
     *  Take a string of all legal Java characters, and ensure that it generates 
     *  a string of legal XML characters
     */
    public void testAllLegalJavaCharsMakeWellFormedXML() throws Exception {
        StringBuilder testString = new StringBuilder();

        testString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        testString.append("<all-java-chars>");
        for (int a = 0; a < 0x10000; a++) {
            if (a == '<') {
                testString.append("&lt;");
            } else if (a == '&') {
                testString.append("&amp;");
            } else {
                testString.append((char) a);
            }
        }
        testString.append("</all-java-chars>");

        String escaped = XMLHelper.escape(testString.toString());
        byte[] escapedBytes = escaped.getBytes("utf-8");
        
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        DefaultHandler dummyHandler = new DefaultHandler();
        
        parser.parse(new ByteArrayInputStream(escapedBytes), dummyHandler, "fake_document.xml");
        // there shouldn't have been an exception. if there was, the escaping
        // process missed an illegal xml character
    }
    
    /**
     *  Test to ensure that a bare backslash is not escaped
     */
    public void testEscapeBareBackslash() {
        String unescaped = "My \\ String";
        checkBothWays(unescaped, unescaped);
    }
    
    /**
     *  Test to ensure that a bare backslash is not escaped
     */
    public void testEscapeBackslashedU() {
        String escaped = "My \\u005cu String";
        String unescaped = "My \\u String";
        checkBothWays(unescaped, escaped);
    }
}
