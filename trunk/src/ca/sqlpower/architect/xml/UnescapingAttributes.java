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
/**
 * 
 */
package ca.sqlpower.architect.xml;

import org.xml.sax.Attributes;


/**
 *  An implementation of Attributes that wraps around another Attributes instance.
 *  It is used to read XML attributes from an XML document that was generated using
 *  the XMLHelper class' methods.
 */
public class UnescapingAttributes implements Attributes {

    /**
     * The Attributes that this instance of UnescapingAttributes wraps around.
     */
    private final Attributes attr;
    
    /**
     * @param attributes The Attributes that this instance wraps around
     */
    public UnescapingAttributes(Attributes attributes) {
        attr = attributes;
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
     */
    public int getIndex(String uri, String localName) {
        return attr.getIndex(uri, localName);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    public int getIndex(String qName) {
        return attr.getIndex(qName);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @return
     * @see org.xml.sax.Attributes#getLength()
     */
    public int getLength() {
        return attr.getLength();
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getLocalName(int)
     */
    public String getLocalName(int index) {
        return attr.getLocalName(index);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getQName(int)
     */
    public String getQName(int index) {
        return attr.getQName(index);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getType(int)
     */
    public String getType(int index) {
        return attr.getType(index);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
     */
    public String getType(String uri, String localName) {
        return attr.getType(uri, localName);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    public String getType(String qName) {
        return attr.getType(qName);
    }

    /**
     * Delegates to the wrapped Attributes' implementation of the method.
     * 
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getURI(int)
     */
    public String getURI(int index) {
        return attr.getURI(index);
    }

    /**
     * @param index
     * @return An unescaped version of the value given from the wrapped Attributes
     * @see org.xml.sax.Attributes#getValue(int)
     */
    public String getValue(int index) {
        return XMLHelper.unescape(attr.getValue(index));
    }

    /**
     * @param uri
     * @param localName
     * @return An unescaped version of the value given from the wrapped Attributes
     * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
     */
    public String getValue(String uri, String localName) {
        return XMLHelper.unescape(attr.getValue(uri, localName));
    }

    /**
     * @param qName
     * @return An unescaped version of the value given from the wrapped Attributes
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    public String getValue(String qName) {
        return XMLHelper.unescape(attr.getValue(qName));
    }
 

}
