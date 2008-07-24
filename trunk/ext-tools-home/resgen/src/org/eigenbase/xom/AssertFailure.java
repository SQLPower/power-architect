/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/AssertFailure.java#3 $
// Package org.eigenbase.xom is an XML Object Mapper.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2000-2005 Kana Software, Inc. and others.
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
// jhyde, 3 December, 2001
*/

package org.eigenbase.xom;

/**
 * todo:
 *
 * @author jhyde
 * @since 3 December, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/AssertFailure.java#3 $
 **/
public class AssertFailure extends RuntimeException {
    /** Construct an AssertFailure with no message */
    public AssertFailure() {
        super();
    }

    /** Construct an AssertFailure with a simple detail message. */
    public AssertFailure(String s) {
        super(s);
    }

    /** Construct an AssertFailure from an exception.  This indicates an
     * unexpected exception of another type.  We'll fill in the stack trace
     * when printing the message. */
    public AssertFailure(Throwable th) {
        super("unexpected exception:\n" +
              th.fillInStackTrace().toString());
    }

    /** Similar to the previous constructor, except allows a custom message on
     * top of the exception */
    public AssertFailure(Throwable th, String s) {
        super(s + ":\n" +
              th.fillInStackTrace().toString());
    }
}

// End AssertFailure.java
