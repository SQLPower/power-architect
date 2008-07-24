/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/wrappers/XercesDOMParser.java#3 $
// Package org.eigenbase.xom is an XML Object Mapper.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2001-2005 Kana Software, Inc. and others.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 2 of the License, or (at your
// option) any later version approved by The Eigenbase Project.
//
// This library is distributed in the hope that it will be useful, 
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// klo, 1 August, 2001
*/

package org.eigenbase.xom.wrappers;

// We may not need this class, but I want to commit it with the initial checkin.
// If it turns out to be something we need, we'll have to add Xerces to the
// Architect's buildlib and put it on the eclipse classpath.

//import org.eigenbase.xom.DOMWrapper;
//import org.eigenbase.xom.XOMException;
//import org.apache.xerces.dom.DocumentImpl;
//import org.apache.xerces.parsers.DOMParser;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import java.io.IOException;
//
///**
// * This private helper class presents a GenericDOMParser using Xerces, with
// * simple error handling appropriate for a testing environment.
// */
//
//public class XercesDOMParser extends GenericDOMParser {
//    private DOMParser parser;
//
//    /**
//     * Constructs a non-validating Xerces DOM Parser.
//     */
//    public XercesDOMParser() throws XOMException {
//        this(false);
//    }
//
//    /**
//     * Constructs a Xerces DOM Parser.
//     * @param validate whether to enable validation
//     */
//    public XercesDOMParser(boolean validate) throws XOMException {
//        parser = new DOMParser();
//        try {
//            if (!validate) {
//                parser.setFeature(VALIDATION_FEATURE, false);
//                parser.setFeature(LOAD_EXTERNAL_DTD_FEATURE, false);
//            }
//        } catch (SAXException e) {
//            throw new XOMException(e, "Error setting up validation");
//        }
//
//        parser.setErrorHandler(this);
//        document = new DocumentImpl();
//    }
//
//    // implement GenericDOMParser
//    protected Document parseInputSource(InputSource in) throws XOMException {
//        prepareParse();
//        try {
//            parser.parse(in);
//        } catch (SAXException ex) {
//            // Display any pending errors
//            handleErrors();
//            throw new XOMException(ex, "Document parse failed");
//        } catch (IOException ex) {
//            // Display any pending errors
//            handleErrors();
//            throw new XOMException(ex, "Document parse failed");
//        }
//
//        handleErrors();
//        return parser.getDocument();
//    }
//
//    // implement Parser
//    public DOMWrapper create(String tagName) {
//        Node node = document.createElement(tagName);
//        return new W3CDOMWrapper(node);
//    }
//}
//
//// End XercesDOMParser.java
