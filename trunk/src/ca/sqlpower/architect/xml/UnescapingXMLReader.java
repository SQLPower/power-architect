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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * An implementation of XMLReader wraps around another XMLReader instance. 
 */
public class UnescapingXMLReader implements XMLReader {

    private static final Logger logger = Logger.getLogger(UnescapingXMLReader.class);
    
    /**
     * The XMLReader that this instance of UnescapingXMLReader wraps around.
     */
    private final XMLReader reader;
    
    /**
     * @param xmlReader The XMLReader that this class wraps around.
     */
    public UnescapingXMLReader(XMLReader xmlReader) {
        reader = xmlReader;
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public ContentHandler getContentHandler() {
        return reader.getContentHandler();
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public DTDHandler getDTDHandler() {
        return reader.getDTDHandler();
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public EntityResolver getEntityResolver() {
        return reader.getEntityResolver();
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public ErrorHandler getErrorHandler() {
        return reader.getErrorHandler();
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return reader.getFeature(name);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return reader.getProperty(name);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void parse(InputSource input) throws IOException, SAXException {
        reader.parse(input);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void parse(String systemId) throws IOException, SAXException {
        reader.parse(systemId);
    }

    /**
     * Takes the passed in ContentHandler and wraps it in an UnescapingDefaultHandler.
     * Takes the UnescapingDefaultHandler and passes into the wrapped XMLReader's
     * implementation of the setContentHandler().
     */
    public void setContentHandler(ContentHandler handler) {
        logger.debug("setContentHandler was called.  Wrapping given handler.");
        reader.setContentHandler(new UnescapingDefaultHandler(handler));
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void setDTDHandler(DTDHandler handler) {
        reader.setDTDHandler(handler);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void setEntityResolver(EntityResolver resolver) {
        reader.setEntityResolver(resolver);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void setErrorHandler(ErrorHandler handler) {
        reader.setErrorHandler(handler);
    }
    
    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setFeature(name, value);
    }

    /**
     * Delegates to the wrapped XMLReader's implementation of the method.
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setProperty(name, value);
    }
}
