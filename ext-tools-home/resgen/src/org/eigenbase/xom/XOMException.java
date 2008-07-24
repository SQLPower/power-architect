/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/XOMException.java#3 $
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
// jhyde, 18 June, 2001
*/

package org.eigenbase.xom;

/**
 * XOMException extends Exception and provides detailed error messages for
 * xom-specific exceptions.
 */
public class XOMException extends Exception {

    /**
     * Constructs a XOM exception with no message.
     */
    public XOMException()
    {
        super(null,null);
    }

    /**
     * Constructs an exception with a detailed message.
     *
     *@param s - a detailed message describing the specific error
     */
    public XOMException(String s)
    {
        super(s,null);
    }

    /**
     * Constructs an exception based on another exception, so that
     * the exceptions may be chained.
     * @param cause the exception on which this one is based.
     * @param s a message for this portion of the exception.
     */
    public XOMException(Throwable cause, String s)
    {
        super(s,cause);
    }
}

// End XOMException.java
