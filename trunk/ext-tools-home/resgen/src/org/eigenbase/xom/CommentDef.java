/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/CommentDef.java#3 $
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
// jhyde, 11 October, 2001
*/

package org.eigenbase.xom;
import java.io.PrintWriter;

/**
 * todo:
 *
 * @author jhyde
 * @since 11 October, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/CommentDef.java#3 $
 **/
public class CommentDef extends TextDef {

    public CommentDef()
    {
        super();
    }

    public CommentDef(String s)
    {
        super(s);
    }

    public CommentDef(DOMWrapper _def) throws XOMException
    {
        super(_def);
    }

    // override ElementDef
    public int getType()
    {
        return DOMWrapper.COMMENT;
    }

    // implement NodeDef
    public void display(PrintWriter pw, int indent)
    {
        pw.print("<!-- ");
        pw.print(s);
        pw.print(" -->");
    }

    // implement NodeDef
    public void displayXML(XMLOutput out, int indent)
    {
        out.beginNode();
        out.print("<!-- ");
        out.print(s);
        out.print(" -->");
    }
}


// End CommentDef.java
