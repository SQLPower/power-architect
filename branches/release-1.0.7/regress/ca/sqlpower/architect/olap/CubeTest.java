/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.olap;

import junit.framework.TestCase;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Relation;
import ca.sqlpower.architect.olap.MondrianModel.Table;
import ca.sqlpower.object.CountingSPListener;

public class CubeTest extends TestCase {

    /**
     * Regression test for removing a child from an OLAP object that is a single
     * child. For some cases, like the cube, there is a child object that the
     * parent can only have one of, like the cube's fact child. When this child
     * type is removed by calling remove or setting the child to null a child
     * added event should not be fired.
     */
    public void testRemoveSingleChild() throws Exception {
        Cube cube = new Cube();
        Relation relation = new Table();
        cube.setFact(relation);
        
        CountingSPListener listener = new CountingSPListener();
        cube.addSPListener(listener);
        
        cube.removeChild(relation);
        
        assertEquals(0, listener.getChildAddedCount());
    }
}
