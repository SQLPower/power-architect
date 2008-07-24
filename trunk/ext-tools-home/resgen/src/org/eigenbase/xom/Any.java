/*
// $Id: //open/util/resgen/src/org/eigenbase/xom/Any.java#3 $
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

/**
 * An element which has 'Any' content.
 *
 * @author jhyde
 * @since 31 October, 2001
 * @version $Id: //open/util/resgen/src/org/eigenbase/xom/Any.java#3 $
 **/
public interface Any {

    NodeDef[] getChildren();
    void setChildren(NodeDef[] children);
}


// End Any.java
