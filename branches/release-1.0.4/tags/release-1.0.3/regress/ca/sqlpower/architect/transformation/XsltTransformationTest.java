/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.transformation;

import java.io.File;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.architect.transformation.ReportTransformer;
import ca.sqlpower.architect.transformation.TransformerFactory;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;

public class XsltTransformationTest extends TestCase {

    /**
     * Tests the export panel can compile the built in template and can export a
     * simple session.
     */
    public void testExportSimpleSession() throws Exception {
        ArchitectSwingSessionContextImpl context = new ArchitectSwingSessionContextImpl("pl.regression.ini", false);
        ArchitectSwingSession session = context.createSession();
        
        SQLDatabase db = session.getTargetDatabase();
        SQLTable table = new SQLTable(db, true);
        table.setName("Test table");
        db.addTable(table);
        SQLColumn col = new SQLColumn();
        col.setName("Testing col");
        table.addColumn(col);
        
        
        final ReportTransformer transformer;
        transformer = TransformerFactory.getTransformer(null);

        File output = File.createTempFile("TestArchitectTransformer", "");
        transformer.transform("/xsltStylesheets/architect2html.xslt", output, session);

        assertTrue(output.exists());
    }
}
