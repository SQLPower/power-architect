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

package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.TestingArchitectSwingSessionContext;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLTable;

public class AlignTableActionTest extends TestCase {

    AlignTableAction alignActionHori;
    AlignTableAction alignActionVert;
    
    PlayPen pp;
    SQLTable t1;
    SQLTable t2;
    SQLTable t3;
    TablePane tp;
    TablePane tp2;
    TablePane tp3;
    
    protected void setUp() throws Exception {
        super.setUp();
        SQLDatabase db = new SQLDatabase();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession();
        alignActionHori = new AlignTableAction(session, Messages.getString("ArchitectFrame.alignTablesHorizontallyActionName"), Messages.getString("ArchitectFrame.alignTablesHorizontallyActionDescription"), true);
        alignActionVert = new AlignTableAction(session, Messages.getString("ArchitectFrame.alignTablesVerticallyActionName"), Messages.getString("ArchitectFrame.alignTablesVerticallyActionDescription"), false);
        pp = session.getPlayPen();
        t1 = new SQLTable(db,true);
        tp = new TablePane(t1,pp.getContentPane());
        pp.addTablePane(tp,new Point(100,71));
        t2 = new SQLTable(db,true);
        tp2 = new TablePane(t2,pp.getContentPane());
        pp.addTablePane(tp2,new Point(1,1));
        t3 = new SQLTable(db,true);
        tp3 = new TablePane(t3,pp.getContentPane());
        pp.addTablePane(tp3,new Point(21,43));
    }
    
    public void testSimpleAlignTableHori(){
        pp.selectAll();
        alignActionHori.actionPerformed(null);
        assertEquals("tp location incorrect", new Point(100,1), tp.getLocation());
        assertEquals("tp2 location incorrect", new Point(1,1), tp2.getLocation());
        assertEquals("tp3 location incorrect", new Point(21,1), tp3.getLocation());
    }
    
    public void testSimpleAlignTableVert(){
        pp.selectAll();
        alignActionVert.actionPerformed(null);
        assertEquals("tp location incorrect", new Point(1,71), tp.getLocation());
        assertEquals("tp2 location incorrect", new Point(1,1), tp2.getLocation());
        assertEquals("tp3 location incorrect", new Point(1,43), tp3.getLocation());
    }
    
    public void testSimpleAlignTableVertAndHori(){
        pp.selectAll();
        alignActionHori.actionPerformed(null);
        alignActionVert.actionPerformed(null);
        assertEquals("tp location incorrect", new Point(1,1), tp.getLocation());
        assertEquals("tp2 location incorrect", new Point(1,1), tp2.getLocation());
        assertEquals("tp3 location incorrect", new Point(1,1), tp3.getLocation());
    }
    
    public void testSelectSomeAlignTableHori(){
        List<SQLObject> selections = new ArrayList<SQLObject>();
        selections.add(t1);
        selections.add(t3);
        try {
            pp.selectObjects(selections);
        } catch (SQLObjectException e) {
            System.out.println("Error in selecting tables in TestAlignTableAction");
            e.printStackTrace();
        }
        alignActionHori.actionPerformed(null);
        assertEquals("tp location incorrect", new Point(100, 43), tp.getLocation());
        assertEquals("tp2 location incorrect", new Point(1,1), tp2.getLocation());
        assertEquals("tp3 location incorrect", new Point(21,43), tp3.getLocation());
       
    }
    
    public void testSelectSomeAlignTableVert(){
        List<SQLObject> selections = new ArrayList<SQLObject>();
        selections.add(t1);
        selections.add(t3);
        try {
            pp.selectObjects(selections);
        } catch (SQLObjectException e) {
            System.out.println("Error in selecting tables in TestAlignTableAction");
            e.printStackTrace();
        }
        alignActionVert.actionPerformed(null);
        assertEquals("tp location incorrect", new Point(21, 71), tp.getLocation());
        assertEquals("tp2 location incorrect", new Point(1,1), tp2.getLocation());
        assertEquals("tp3 location incorrect", new Point(21,43), tp3.getLocation());
       
    }
}
