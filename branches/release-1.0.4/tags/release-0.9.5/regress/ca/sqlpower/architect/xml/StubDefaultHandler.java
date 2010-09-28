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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StubDefaultHandler extends DefaultHandler {
    
    /**
     * The Attributes that are passed into the stub implementation of the
     * startElement method. For example, the tests will check the class
     * of the attribute in UnescapingDefaultHandlerTest to ensure that a
     * UnescapingAttributes is being passed into the startElements method.
     * Access is package-private so that the tests can access it directly.
     */
    Attributes attr;
    
    /**
     * The arguments that get passed into the stub implementation of the
     * characters method. For example, we will check the values of these in
     * UnescapingDefaultHandlerTest to make sure the right values are being passed
     * in UnescapingDefaultHandler's implementation of the characters method.
     * Access is package-private so that the tests can access it directly.
     */
    String string;
    
    /**
     * The most recent start arg value passed to characters().
     * Access is package-private so that the tests can access it directly.
     */
    int start;
    
    /**
     * The most recent length arg value passed to characters().
     * Access is package-private so that the tests can access it directly.
     */
    int length;
  
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.string = new String(ch);
        this.start = start;
        this.length = length;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        attr = attributes;
    }
}
