/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/WrapperElementDef.java#3 $
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
// jhyde, 31 October, 2001
*/

package org.eigenbase.xom;
import java.io.PrintWriter;

/**
 * <code>WrapperElementDef</code> is an {@link ElementDef} which retains the
 * underlying XML {@link DOMWrapper}. It is used when there is no specific
 * class for this tag.
 *
 * @author jhyde
 * @since 31 October, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/WrapperElementDef.java#3 $
 **/
public class WrapperElementDef extends ElementDef
{
    DOMWrapper _def;
    Class enclosure;
    String prefix;

    public WrapperElementDef(
        DOMWrapper def, Class enclosure, String prefix)
    {
        this._def = def;
        this.enclosure = enclosure;
        this.prefix = prefix;
    }

    // implement NodeDef
    public void display(PrintWriter out, int indent)
    {
        out.print("<");
        out.print(_def.getTagName());
        String[] attributeKeys = _def.getAttributeNames();
        for (int i = 0; i < attributeKeys.length; i++) {
            String key = attributeKeys[i];
            Object value = _def.getAttribute(key);
            XOMUtil.printAtt(out, key, value.toString());
        }
        NodeDef[] children = getChildren();
        if (children.length == 0) {
            out.print("/>");
        } else {
            for (int i = 0, count = children.length; i < count; i++) {
                children[i].display(out, indent + 1);
            }
            out.print("</");
            out.print(_def.getTagName());
            out.print(">");
        }
    }

    // implement NodeDef
    public void displayXML(XMLOutput out, int indent)
    {
        out.beginNode();
        String tagName = _def.getTagName();
        out.beginBeginTag(tagName);
        String[] attributeKeys = _def.getAttributeNames();
        for (int i = 0; i < attributeKeys.length; i++) {
            String key = attributeKeys[i];
            Object value = _def.getAttribute(key);
            out.attribute(key, value.toString());
        }
        out.endBeginTag(tagName);
        NodeDef[] children = getChildren();
        for (int i = 0, count = children.length; i < count; i++) {
            NodeDef child = children[i];
            child.displayXML(out, indent + 1);
        }
        out.endTag(tagName);
    }

    // implement NodeDef
    public int getType()
    {
        return DOMWrapper.ELEMENT;
    }

    // implement NodeDef
    public String getName()
    {
        return _def.getTagName();
    }

    // implement NodeDef
    public NodeDef[] getChildren()
    {
        try {
            DOMWrapper[] children = _def.getChildren();
            NodeDef[] a = new NodeDef[children.length];
            for (int i = 0; i < a.length; i++) {
                a[i] = ElementDef.constructElement(
                    children[i], enclosure, prefix);
            }
            return a;
        } catch (XOMException e) {
            throw new AssertFailure(e, "in WrapperElementDef.getChildren");
        }
    }

    // implement NodeDef
    public DOMWrapper getWrapper()
    {
        return _def;
    }
}

// End WrapperElementDef.java
