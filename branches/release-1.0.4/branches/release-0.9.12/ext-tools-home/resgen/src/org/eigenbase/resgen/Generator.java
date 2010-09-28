/*
// $Id: //open/util/resgen/src/org/eigenbase/resgen/Generator.java#3 $
// Package org.eigenbase.resgen is an i18n resource generator.
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
package org.eigenbase.resgen;

import java.io.PrintWriter;

/**
 * A generator converts a set of resource definitions to a piece of code.
 *
 * @author jhyde
 * @since 19 September, 2005
 * @version $Id: //open/util/resgen/src/org/eigenbase/resgen/Generator.java#3 $
 */
interface Generator
{
    /**
     * Configures whether this generator will output comments that may be
     * submitted to a source code management system.  In general, it
     * squelches comments indicating the file should not be checked in as
     * well as comments change with each generation of the file (thereby
     * avoiding merge conflicts).
     * 
     * @param enabled
     */
    void setScmSafeComments(boolean enabled);
    
    /**
     * Generates a class containing a line for each resource.
     */
    void generateModule(
        ResourceGen generator,
        ResourceDef.ResourceBundle resourceList,
        PrintWriter pw);
}

// End Generator.java
