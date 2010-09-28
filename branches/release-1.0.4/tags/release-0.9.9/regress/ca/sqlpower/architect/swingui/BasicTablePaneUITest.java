/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        tp = new TablePane(t, pp);

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
        returnVal = ui.pointToColumnIndex(testPoint);
        assertEquals(TablePane.COLUMN_INDEX_NONE, returnVal);

        // If the box is 'just' outside the click area of the last column
        Font font = tp.getFont();
        FontMetrics metrics = tp.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        int firstColStart = fontHeight + ui.gap + ui.boxLineThickness + tp.getMargin().top;
        int numCols = tp.getModel().getColumns().size();
        testPoint.setLocation(0, firstColStart + ui.pkGap + fontHeight * numCols);
        returnVal = ui.pointToColumnIndex(testPoint);
        assertEquals(TablePane.COLUMN_INDEX_NONE, returnVal);

        // If the box is waaay outside the click area of the columns
        testPoint.setLocation(0, Integer.MAX_VALUE);
        returnVal = ui.pointToColumnIndex(testPoint);
        assertEquals(TablePane.COLUMN_INDEX_NONE, returnVal);
    }

}
