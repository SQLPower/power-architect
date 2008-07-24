/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/TextDef.java#3 $
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
// jhyde, 5 October, 2001
*/

package org.eigenbase.xom;
import java.io.PrintWriter;

/**
 * A <code>TextDef</code> represents piece of textual data in an XML document.
 * Free text (such as <code>Some text</code>) is represented by an actual
 * <code>TextDef</code>; comments (such as <code>&lt-- a comment --&gt;</code>)
 * by derived class {@link CommentDef}; and CDATA sections (such as
 * <code>&lt;![CDATA[Some text]]&gt;</code>) by derived class {@link CdataDef}.
 *
 * @author jhyde
 * @since 5 October, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/TextDef.java#3 $
 **/
public class TextDef implements NodeDef {

    public String s;

    /**
     * Whether to print the data as is -- never quote as a CDATA
     * section. Useful if the fragment contains a valid XML string.
     **/
    boolean asIs;

    public TextDef()
    {
    }

    public TextDef(String s)
    {
        this.s = s;
    }

    public TextDef(String s, boolean asIs)
    {
        this.s = s;
        this.asIs = asIs;
    }

    public TextDef(org.eigenbase.xom.DOMWrapper _def)
        throws org.eigenbase.xom.XOMException
    {
        switch (_def.getType()) {
        case DOMWrapper.FREETEXT:
        case DOMWrapper.CDATA:
        case DOMWrapper.COMMENT:
            break;
        default:
            throw new XOMException(
                "cannot make CDATA/PCDATA element from a " + _def.getType());
        }
        this.s = _def.getText();
    }

    // override ElementDef
    public String getName()
    {
        return null;
    }

    // override ElementDef
    public String getText()
    {
        return s;
    }

    // implement NodeDef
    public NodeDef[] getChildren()
    {
        return XOMUtil.emptyNodeArray;
    }

    // implement NodeDef
    public DOMWrapper getWrapper()
    {
        return null;
    }

    // implement NodeDef
    public int getType()
    {
        return DOMWrapper.FREETEXT;
    }

    // implement NodeDef
    public void display(PrintWriter pw, int indent)
    {
        pw.print(s);
    }

    // override NodeDef
    public void displayXML(XMLOutput out, int indent)
    {
        if (out.getIgnorePcdata()) {
            return;
        }
        out.beginNode();
        if (asIs) {
            out.print(s);
        } else {
            boolean quote = true;
            out.cdata(s, quote);
        }
    }
}


// End TextDef.java
