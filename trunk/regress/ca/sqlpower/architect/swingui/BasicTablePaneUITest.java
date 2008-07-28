/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

/**
 * Test case for BasicTablePaneUITest. Since the proper behaviour for a
 * TablePaneUI class would be dependent on the implementation, it would seem to
 * make the most sense to have a separate test class for each implementation
 */
public class BasicTablePaneUITest extends TestCase {

    private SQLTable t;

    private TablePane tp;

    private PlayPen pp;

    ArchitectSwingSession session;

    private BasicTablePaneUI ui;

    protected void setUp() throws Exception {
        super.setUp();

        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        session = context.createSession();
        t = new SQLTable(session.getTargetDatabase(), true);
        t.setName("Test Table");

        SQLColumn pk1 = new SQLColumn(t, "PKColumn1", Types.INTEGER, 10, 0);
        SQLColumn at1 = new SQLColumn(t, "AT1", Types.INTEGER, 10, 0);

        t.addColumn(0, pk1);
        t.addColumn(1, at1);

        pp = session.getPlayPen();
        tp = new TablePane(t, pp.getContentPane());

        ui = new BasicTablePaneUI();
        ui.installUI(tp);
    }

    /**
     * Test to ensure pointToColumnIndex returns COLUMN_INDEX_NONE if the point
     * is outside the group of columns. This is to test against a bug in which
     * clicking below a column would return a column index larger than the
     * highest column index.
     */
    public void testPointToColumnIndexForPointsOutsideColumns() throws Exception {
        int returnVal;
        Point testPoint = new Point(-1, -1);
        returnVal = ui.pointToItemIndex(testPoint);
        assertEquals(ContainerPane.ITEM_INDEX_NONE, returnVal);

        // If the box is 'just' outside the click area of the last column
        Font font = tp.getFont();
        FontMetrics metrics = tp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int firstColStart = fontHeight + BasicTablePaneUI.GAP + BasicTablePaneUI.BOX_LINE_THICKNESS + tp.getMargin().top;
        int numCols = tp.getModel().getColumns().size();
        testPoint.setLocation(0, firstColStart + BasicTablePaneUI.PK_GAP + fontHeight * numCols);
        returnVal = ui.pointToItemIndex(testPoint);
        assertEquals(ContainerPane.ITEM_INDEX_NONE, returnVal);

        // If the box is waaay outside the click area of the columns
        testPoint.setLocation(0, Integer.MAX_VALUE);
        returnVal = ui.pointToItemIndex(testPoint);
        assertEquals(ContainerPane.ITEM_INDEX_NONE, returnVal);
    }

}
