/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/DefWalker.java#3 $
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
// dsommerfield, 18 February, 2001
*/

package org.eigenbase.xom;

import java.lang.reflect.Array;
import java.util.Vector;

/**
 * DefWalker is a class designed to help users of plugin elements and elements
 * with content type ANY.  It walks through an array of ElementDef, searching
 * for and returning portions as the correct types.
 */
public class DefWalker {

    private NodeDef[] defs;
    private int pos;

    /**
     * Construct a DefWalker, attaching it to a NodeDef array and
     * specifying a PrintWriter to display error messages for later
     * consumption.
     * @param defs a NodeDef array to walk.  All returned objects
     * come from this array.
     */
    public DefWalker(NodeDef[] defs)
    {
        this.defs = defs;
        pos = 0;
    }

    /**
     * Returns the next node in the defs array, but only if it matches
     * the provided class <i>elemType</i>.
     * @param elemType the Class of NodeDef to expect.  This class will
     * always be assignable from the returned object.
     * @throws XOMException if there are no more nodes in the defs
     * array or if the next node is of an incorrect type.
     */
    public NodeDef expect(Class elemType)
        throws XOMException
    {
        if(pos >= defs.length)
            throw new XOMException("Expecting a Node of type "
                                      + elemType.getName() + " but no "
                                      + "Nodes remain.");
        if(!(elemType.isAssignableFrom(defs[pos].getClass())))
            throw new XOMException("Expecting a Node of type "
                                      + elemType.getName() + " but "
                                      + "found a Node of type "
                                      + defs[pos].getClass().getName());
        return defs[pos++];
    }

    /**
     * Returns a portion of the remaining nodes in the defs array as an
     * array.  All nodes in the array will be of the specified class
     * <i>elemType</i>.  The nodes are returned as a generic NodeDef[]
     * array and may need to be explicitly converted to an array of the
     * appropriate type by the caller.
     * @param elemType the Class of NodeDef to expect and return.  This
     * class will always be assignable from each returned object in the
     * array.
     */
    public NodeDef[] expectArray(Class elemType)
    {
        Vector found = new Vector();
        while(pos < defs.length &&
              elemType.isAssignableFrom(defs[pos].getClass()))
            found.addElement(defs[pos++]);

        NodeDef[] ret = new NodeDef[found.size()];
        for(int i=0; i<found.size(); i++)
            ret[i] = (NodeDef)(found.elementAt(i));
        return ret;
    }

    /**
     * Returns a portion of the remaining nodes in the defs array as an
     * array.  All nodes in the array will be of the specified class
     * <i>elemType</i>.  The nodes are in an array of the specified type,
     * which will be returned as an object (which must be cast to the
     * appropriate array type by the caller when needed.
     * @param elemType the Class of NodeDef to expect and return.  This
     * class will always be assignable from each returned object in the
     * array.
     */
    public Object expectTypeArray(Class elemType)
    {
        Vector found = new Vector();
        while(pos < defs.length &&
              elemType.isAssignableFrom(defs[pos].getClass()))
            found.addElement(defs[pos++]);

        Object ret = Array.newInstance(elemType, found.size());
        for(int i=0; i<found.size(); i++)
            Array.set(ret, i, found.elementAt(i));
        return ret;
    }
}


// End DefWalker.java
