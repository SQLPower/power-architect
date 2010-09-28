/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/NodeDef.java#3 $
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
// jhyde, 11 October, 2001
*/

package org.eigenbase.xom;
import java.io.PrintWriter;

/**
 * <code>NodeDef</code> represents a node in a parse tree. It is a base class
 * for {@link ElementDef}, {@link TextDef}, etc.
 *
 * @author jhyde
 * @since 11 October, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/NodeDef.java#3 $
 **/
public interface NodeDef {

    /**
     * Returns the name of this node's tag.
     **/
    String getName();

    /**
     * Returns the type of this element (see {@link DOMWrapper#getType}).
     **/
    int getType();

    /**
     * Returns the text inside this node.
     **/
    String getText();

    /**
     * Returns the children of this node.
     **/
    NodeDef[] getChildren();

    /**
     * Outputs this element definition in XML to any XMLOutput.
     * @param out the XMLOutput class to display the XML
     **/
    void displayXML(XMLOutput out, int indent);

    /**
     * Outputs this node to any PrintWriter,
     * in a formatted fashion with automatic indenting.
     * @param out the PrintWriter to which to write this NodeDef.
     * @param indent the indentation level for the printout.
     */
    void display(PrintWriter out, int indent);

    /**
     * Retrieves the {@link DOMWrapper} which was used to create this
     * node. Only works if this nodes's {@link MetaDef.Element#keepDef} was
     * set; otherwise, returns <code>null</code>.
     **/
    DOMWrapper getWrapper();
}


// End NodeDef.java
