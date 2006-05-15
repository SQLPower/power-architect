package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Point;
import java.sql.Types;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

public class TestRelationship extends TestCase {

	Relationship rel;
	PlayPen pp;
    TablePane tp1;
    TablePane tp2;
	
	protected void setUp() throws Exception {
		super.setUp();
		pp = ArchitectFrame.getMainInstance().getProject().getPlayPen();
		SQLTable t1 = new SQLTable(pp.getDatabase(), true);
        t1.addColumn(new SQLColumn(t1, "pkcol_1", Types.INTEGER, 10,0));
        t1.addColumn(new SQLColumn(t1, "fkcol_1", Types.INTEGER, 10,0));
        t1.getColumnByName("pkcol_1").setPrimaryKeySeq(0);

		pp.getDatabase().addChild(t1);
		pp.addTablePane(tp1 = new TablePane(t1, pp), new Point(0,0));
		SQLTable t2 = new SQLTable(pp.getDatabase(), true);
        t2.addColumn(new SQLColumn(t2, "col_1", Types.INTEGER, 10,0));
        t2.addColumn(new SQLColumn(t2, "fkcol", Types.INTEGER, 10,0));      

		pp.getDatabase().addChild(t2);
		pp.addTablePane(tp2 = new TablePane(t2, pp), new Point(0,0));
		SQLRelationship sqlrel = new SQLRelationship();
		sqlrel.attachRelationship(t1, t2, false);
        sqlrel.addMapping(t1.getColumnByName("pkcol_1"), 
                t2.getColumnByName("fkcol"));
		rel = new Relationship(pp, sqlrel);
	}
	
	public void testCopyConstructor() {
		PlayPen newpp = new PlayPen(pp.getDatabase());
		Relationship rel2 = new Relationship(rel, newpp.getContentPane(), null, null);
		assertNotSame("The new relationship component has the same UI delegate as the original", rel.getUI(), rel2.getUI());
	}

    public void testHighlightWithRelationshipTypeChange() throws ArchitectException {               
        rel.setSelected(true);
        assertEquals(Color.RED,tp1.getColumnHighlight(0));
        assertEquals(Color.RED,tp2.getColumnHighlight(1));
        assertEquals(tp2.getForeground(), tp2.getColumnHighlight(0));
        rel.setSelected(false);
        
        assertEquals(tp1.getForeground(), tp1.getColumnHighlight(0));
        assertEquals(tp2.getForeground(), tp2.getColumnHighlight(1));
        assertEquals(tp2.getForeground(), tp2.getColumnHighlight(0));
        
        rel.setSelected(true);
        rel.getModel().setIdentifying(true);       
        
        assertEquals(Color.RED,tp1.getColumnHighlight(0));
        SQLColumn fkCol = tp2.getModel().getColumnByName("fkcol");
        assertEquals(0, tp2.getModel().getColumnIndex(fkCol));
        assertEquals(Color.RED,tp2.getColumnHighlight(0));
        assertEquals(tp2.getForeground(), tp2.getColumnHighlight(1));      
    }
}
