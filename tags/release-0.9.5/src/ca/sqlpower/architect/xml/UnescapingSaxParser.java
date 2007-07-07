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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**\
 *  An implementation of SAXParser that wraps around another SAXParster.
 *  It is used to parse XML documents that were generated using the XMLHelper class. 
 *
 */
public class UnescapingSaxParser extends SAXParser {

    private static Logger logger = Logger.getLogger(UnescapingSaxParser.class);
    
    /**
     * The SAXParser that this instance of UnescapingSaxParser wraps around.
     */
    private final SAXParser parser;
    
    /**
     * An instance of UnescapingXMLReader that wraps around an XMLReader instance
     */
    private final UnescapingXMLReader reader;
    
    /**
     * Constructor which gets the wrapped SAXParser from a SAXParserFactory.
     */
    public UnescapingSaxParser() throws ParserConfigurationException, SAXException {
        this(SAXParserFactory.newInstance().newSAXParser());
    }

    /**
     * Constructor which sets the wrapped SAXParser to the given parser.
     * 
     * @param parser SAXParser that will be wrapped
     */
    public UnescapingSaxParser(SAXParser parser) throws ParserConfigurationException, SAXException {
        if (parser == null) {
            throw new NullPointerException("Passed a null parser into UnescapingSaxParser constructor");
        }
        
        logger.debug("Calling UnescapingSaxParser constructer");
        this.parser = parser;
        reader = new UnescapingXMLReader(parser.getXMLReader());
    }
    
    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean equals(Object obj) {
        return parser.equals(obj);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Parser getParser() throws SAXException {
        return parser.getParser();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return parser.getProperty(name);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Schema getSchema() {
        return parser.getSchema();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public XMLReader getXMLReader() throws SAXException {
        return reader;
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public int hashCode() {
        return parser.hashCode();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isNamespaceAware() {
        return parser.isNamespaceAware();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isValidating() {
        return parser.isValidating();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isXIncludeAware() {
        return parser.isXIncludeAware();
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the File 'f' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(File f, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(f, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputSource 'is' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(InputSource is, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputSource 'is', the UnescapingDefaultHandler, and the String 'systemid'
     * into the parse() method of the wrapped SAXParser.
     */
    public void parse(InputStream is, DefaultHandler dh, String systemId) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh), systemId);
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputStream 'is' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the String 'uri' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(String uri, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(uri, new UnescapingDefaultHandler(dh));
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public void reset() {
        parser.reset();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        parser.setProperty(name, value);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public String toString() {
        return parser.toString();
    }
}
