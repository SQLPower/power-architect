/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/wrappers/JaxpDOMParser.java#3 $
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
*/

package org.eigenbase.xom.wrappers;

import org.eigenbase.xom.XOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * A <code>JaxpDOMParser</code> implements {@link org.eigenbase.xom.Parser} using
 * a {@link DocumentBuilder JAXP-compliant parser}.
 *
 * @author jhyde
 * @since Aug 29, 2002
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/wrappers/JaxpDOMParser.java#3 $
 **/
public class JaxpDOMParser extends GenericDOMParser {
    private DocumentBuilder builder;

    /** Creates a non-validating parser. **/
    public JaxpDOMParser() throws XOMException {
        this(false);
    }

    /** Creates a parser. **/
    public JaxpDOMParser(boolean validating) throws XOMException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);
            try {
                factory.setAttribute(VALIDATION_FEATURE, new Boolean(validating));
                factory.setAttribute(LOAD_EXTERNAL_DTD_FEATURE, new Boolean(validating));
            } catch (IllegalArgumentException e) {
                // Weblogic 6.1's parser complains 'No arguments are
                // implemented'
            }
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XOMException(e, "Error creating parser");
        } catch (FactoryConfigurationError e) {
            throw new XOMException(e, "Error creating parser");
        }
        builder.setErrorHandler(this);
        document = builder.newDocument();
    }

    protected Document parseInputSource(InputSource in) throws XOMException {
        prepareParse();
        try {
            Document document = builder.parse(in);
            handleErrors();
            return document;
        } catch (SAXException e) {
            // Display any pending errors
            handleErrors();
            throw new XOMException(e, "Document parse failed");
        } catch (IOException e) {
            // Display any pending errors
            handleErrors();
            throw new XOMException(e, "Document parse failed");
        }
    }
}

// End JaxpDOMParser.java
