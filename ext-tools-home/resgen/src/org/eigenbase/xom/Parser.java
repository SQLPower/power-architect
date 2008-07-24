/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/Parser.java#3 $
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
// jhyde, 2 August, 2001
*/

package org.eigenbase.xom;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * The <code>Parser</code> interface abstracts the behavior which the
 * <code>org.eigenbase.xom</code> package needs from an XML parser.
 *
 * <p>If you don't care which implementation you get, call {@link
 * XOMUtil#createDefaultParser} to create a parser.</p>
 *
 * @author jhyde
 * @since 2 August, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/Parser.java#3 $
 **/
public interface Parser {
    /** Parses a string and returns a wrapped element. */
    DOMWrapper parse(String sXml) throws XOMException;
    /** Parses an input stream and returns a wrapped element. **/
    DOMWrapper parse(InputStream is) throws XOMException;
    /** Parses the contents of a URL and returns a wrapped element .**/
    DOMWrapper parse(URL url) throws XOMException;
    /** Parses the contents of a reader and returns a wrapped element. **/
    DOMWrapper parse(Reader reader) throws XOMException;
    /** Creates a wrapper representing an XML element. **/
    DOMWrapper create(String tagName);
}

// End Parser.java
