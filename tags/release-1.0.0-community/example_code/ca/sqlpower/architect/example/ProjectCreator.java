/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.example;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.Types;

import javax.swing.SwingUtilities;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;

/**
 * A simple class with a main method that demonstrates how to create all the
 * various types of SQL Object from scratch, assemble them into a playpen
 * database, then save them out as a new project.
 * 
 * @author Jonathan Fuerth
 */
public class ProjectCreator implements Runnable {

    private ArchitectSwingSessionContext sessionContext;
    private ArchitectSwingSession session;

    /**
     * Entry point for this demo. Call with "java ca.sqlpower.architect.example.ProjectCreator".
     * 
     * @param args Ignored.
     */
    public static void main(String[] args) {
        
        // Since we are using the Swing session, we must run everything
        // on the AWT event dispatch thread
        SwingUtilities.invokeLater(new ProjectCreator());
    }
    
    /**
     * This method is expected to be invoked in the AWT event dispatch thread.
     * Calling the {@link #main(String[])} method accomplishes this.
     */
    public void run() {
        try {
            // here we create the session context and a session within it,
            // then call another method to do all the interesting work
            sessionContext = new ArchitectSwingSessionContextImpl();
            session = sessionContext.createSession();

            createSampleProject();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            // if you don't close the session, the VM will be left running
            session.close();
        }
    }

    /**
     * This is the interesting part that shows you how to use the Architect's
     * API to create a project and save it.
     * 
     * @throws SQLObjectException not expected to happen in this scenario
     * @throws IOException if saving to the project file fails
     */
    void createSampleProject() throws SQLObjectException, IOException {
        
        // We will be adding the objects we create to this playpen database.
        SQLDatabase ppdb = session.getTargetDatabase();
        
        // Make a few tables
        SQLTable person = new SQLTable(
                ppdb,     // will disappear in a future version of the API
                "person",
                "This table represents people",
                "TABLE",
                true);
        ppdb.addChild(person);
        
        SQLTable address = new SQLTable(
                ppdb,     // will disappear in a future version of the API
                "address",
                "This table represents places where people live or work",
                "TABLE",
                true);
        ppdb.addChild(address);
        
        // Here's how to add columns to tables
        // (note the parent table constructor arg will be removed in the future)
        SQLColumn col;
        col = new SQLColumn(person, "person_id", Types.INTEGER, 10, 0);
        person.addColumn(col);
        
        // make person_id a primary key
        col.setNullable(DatabaseMetaData.columnNoNulls);
        person.addToPK(col);
        
        col = new SQLColumn(person, "name", Types.VARCHAR, 100, 0);
        person.addColumn(col);
        
        col = new SQLColumn(address, "address_id", Types.INTEGER, 10, 0);
        address.addColumn(col);

        // make address_id a primary key
        col.setNullable(DatabaseMetaData.columnNoNulls);
        person.addToPK(col);

        // make a foreign key that maps person.person_id to address.person_id
        SQLRelationship rel = new SQLRelationship();
        rel.setName("person_address_fk");
        rel.attachRelationship(person, address, true);
        
        col = new SQLColumn(address, "street_address", Types.VARCHAR, 100, 0);
        address.addColumn(col);
        
        col = new SQLColumn(address, "city", Types.VARCHAR, 100, 0);
        address.addColumn(col);

        // create an index on city
        SQLIndex idx = new SQLIndex();
        idx.setName("address_city_idx");
        idx.addIndexColumn(col, AscendDescend.UNSPECIFIED);
        address.addIndex(idx);
        
        col = new SQLColumn(address, "province", Types.VARCHAR, 100, 0);
        address.addColumn(col);
        

        // create a playpen (GUI) object for each table and relationship
        PlayPen pp = session.getPlayPen();
        int x = 10;
        int y = 10;
        for (SQLTable table : ppdb.getTables()) {
            TablePane tp = new TablePane(table, pp.getContentPane());
            pp.addTablePane(tp, new Point(x, y));
            x += tp.getPreferredSize().width + 10;
        }
        
        for (SQLRelationship sr : ppdb.getRelationships()) {
            Relationship r = new Relationship(sr, pp.getContentPane());
            pp.addRelationship(r);
        }
        
        
        // save the project to a file
        File file = new File("project_creator_output.architect");
        FileOutputStream out = new FileOutputStream(file);
        try {
            session.getProjectLoader().save(out, "utf-8");
            System.out.println("Saved example project to " + file.getAbsolutePath());
        } finally {
            out.close();
        }
        
    }
}
