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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import sun.font.FontManager;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTable.TransferStyles;
import ca.sqlpower.util.SQLPowerUtils;

public class TestPlayPen extends TestCase {
	ArchitectFrame af;
	private PlayPen pp;
	private SQLDatabase ppdb;
	
	private static Logger logger = Logger.getLogger(TestPlayPen.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        TestingArchitectSwingSessionContext context = new TestingArchitectSwingSessionContext();
        ArchitectSwingSession session = context.createSession(false);
		af = session.getArchitectFrame();
		pp = session.getPlayPen();
		ppdb = session.getTargetDatabase();

	}

	public void testUndoAddTable() throws SQLObjectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp.getContentPane());
		ppdb.addChild(t);

		pp.addTablePane(tp, new Point(99,98));



		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(pp.findTablePane(t));

		//Undo the add child and the move table pane
		af.getUndoManager().undo();

		assertNull(pp.findTablePane(t));
	}

	public void testRedoAddTable() throws SQLObjectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp.getContentPane());

		
		final SPObject root = SQLPowerUtils.getAncestorList(ppdb).get(0);
		root.setMagicEnabled(false);
        root.begin("Testing transaction");
		ppdb.addChild(t);
		pp.addTablePane(tp, new Point(99,98));
		root.commit();
		root.setMagicEnabled(true);

		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(ppdb.getTableByName("test_me"));
		assertNotNull(pp.findTablePane(t));
		//undo the add child and the move table pane
		System.out.println("Undo action is "+af.getUndoManager().getUndoPresentationName());
		af.getUndoManager().undo();
		System.out.println("After undo, undo action is "+af.getUndoManager().getUndoPresentationName());
		assertNull(ppdb.getTableByName("test_me"));
		assertNull(pp.findTablePane(t));
		// redo the add table and the move
		af.getUndoManager().redo();
		tp = pp.findTablePane(t);
		assertNotNull("Table pane didn't come back!", tp);
		assertEquals("Table came back, but in wrong location",
				new Point(99,98), tp.getLocation());
	}

	/**
	 * Description of the scenario: We are to reverse engineer two tables into the playpen.
	 * There exists one relationship between the two tables. After the first reverse engineering
	 * process, we reverse engineering the parent table again into the playpen. Now we shoud
	 * expect 3 tables in total lying on playpen, and 1 relationship between the first two tables.
	 */
	public void testImportTableCopyHijacksProperly() throws SQLObjectException {

		SQLDatabase sourceDB = new SQLDatabase();
		pp.getSession().getRootObject().addChild(sourceDB);
		SQLTable sourceParentTable = new SQLTable(sourceDB, true);
		sourceParentTable.setName("parent");
		sourceParentTable.addColumn(new SQLColumn(sourceParentTable, "key", Types.BOOLEAN, 1, 0));
		sourceParentTable.addToPK(sourceParentTable.getColumn(0));
		sourceDB.addChild(sourceParentTable);

		SQLTable sourceChildTable = new SQLTable(sourceDB, true);
		sourceChildTable.setName("child");
		sourceChildTable.addColumn(new SQLColumn(sourceChildTable, "key", Types.BOOLEAN, 1, 0));
		sourceDB.addChild(sourceChildTable);

		SQLRelationship sourceRel = new SQLRelationship();
		sourceRel.attachRelationship(sourceParentTable, sourceChildTable, true);

		pp.importTableCopy(sourceChildTable, new Point(10, 10), ASUtils.createDuplicateProperties(pp.getSession(), sourceChildTable));
		pp.importTableCopy(sourceParentTable, new Point(10, 10), ASUtils.createDuplicateProperties(pp.getSession(), sourceParentTable));
		pp.importTableCopy(sourceParentTable, new Point(10, 10), ASUtils.createDuplicateProperties(pp.getSession(), sourceParentTable));

		int relCount = 0;
		int tabCount = 0;
		int otherCount = 0;
		for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
			PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
			if (ppc instanceof Relationship) {
				relCount++;
			} else if (ppc instanceof TablePane) {
				tabCount++;
			} else {
				otherCount++;
			}
		}
		// The behaviour of the reverse engineering is slightly modified. When reverse engineer copy(ies)
		// of related tables to the playpen. The relationship is not pointed to the old tables.	
		assertEquals("Expected three tables in pp", 3, tabCount);
		assertEquals("Expected two relationships in pp", 1, relCount); //changed from 2 to 1
		assertEquals("Found junk in playpen", 0, otherCount);

		TablePane importedChild = pp.findTablePaneByName("child");
		assertEquals("Incorrect reference count on imported child col",
				2, importedChild.getModel().getColumn(0).getReferenceCount()); //changed from 3 to 2
	}
	
	/**
	 * Test to ensure that the self-referencing table gets imported properly into the PlayPen.
	 * @throws Exception
	 */
	public void testImportTableCopyOnSelfReferencingTable() throws Exception {
	    SQLDatabase sourceDB = new SQLDatabase();
	    pp.getSession().getRootObject().addChild(sourceDB);
        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.addToPK(table.getColumn(0));
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10), ASUtils.createDuplicateProperties(pp.getSession(), table));
        
        int relCount = 0;
        int tabCount = 0;
        int otherCount = 0;
        for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
            PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
            if (ppc instanceof Relationship) {
                relCount++;
            } else if (ppc instanceof TablePane) {
                tabCount++;
            } else {
                otherCount++;
            }
        }
        assertEquals("Expected one table in pp", 1, tabCount);
        assertEquals("Expected one relationship in pp", 1, relCount);
        assertEquals("Found junk in playpen", 0, otherCount);
	}
	
	/**
	 * Test to ensure that when importing two copies of a self-referencing table, the
	 * correct number of relationships get added, and furthermore, the relationships
	 * all point to the correct table.
	 * @throws Exception
	 */
	public void testImportTableCopyOnTwoCopiesOfSelfReferencingTable() throws Exception {
        SQLDatabase sourceDB = new SQLDatabase();
        pp.getSession().getRootObject().addChild(sourceDB);
        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.addToPK(table.getColumn(0));
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10), ASUtils.createDuplicateProperties(pp.getSession(), table));
        pp.importTableCopy(table, new Point(30, 30), ASUtils.createDuplicateProperties(pp.getSession(), table));
        
        int relCount = 0;
        int tabCount = 0;
        int otherCount = 0;
        for (int i = 0; i < pp.getPlayPenContentPane().getComponentCount(); i++) {
            PlayPenComponent ppc = pp.getPlayPenContentPane().getComponent(i);
            if (ppc instanceof Relationship) {
                relCount++;
            } else if (ppc instanceof TablePane) {
                tabCount++;
            } else {
                otherCount++;
            }
        }
        assertEquals("Expected two tables in pp", 2, tabCount);
        assertEquals("Expected two relationships in pp", 2, relCount);
        assertEquals("Found junk in playpen", 0, otherCount);
        
        for (SQLTable t: pp.getTables()) {
            List<SQLRelationship> exportedKeys = t.getExportedKeys();
            List<SQLRelationship> importedKeys = SQLRelationship.getExportedKeys(t.getImportedKeys());
            
            assertEquals("Expected only one exported key in table", 1, exportedKeys.size());
            assertEquals("Expected only one imported key in table", 1, importedKeys.size());
            
            SQLRelationship exportedKey = exportedKeys.get(0);
            SQLRelationship importedKey = importedKeys.get(0);
            
            assertEquals("Expected exported key PK and FK tables to be the same", exportedKey.getFkTable(), 
                    exportedKey.getPkTable());
            assertEquals("Expected imported key PK and FK tables to be the same", importedKey.getFkTable(), 
                    importedKey.getPkTable());
            assertEquals("Expected exported key and imported key tables to be the same", exportedKey.getPkTable(),
                    importedKey.getPkTable());
        }
    }
	
	/**
	 * Checks that the properties of an instance from the copy constructor are equal to the original.
	 * In the case of a mutable property, it also checks that they don't share the same instance.
	 * 
	 * @throws Exception
	 */
	public void testCopyConstructor() throws Exception {
	        List<PropertyDescriptor> settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(pp.getClass()));
	        Set<String> copyIgnoreProperties = new HashSet<String>();


	        copyIgnoreProperties.add("UI");
	        copyIgnoreProperties.add("UIClassID");
	        copyIgnoreProperties.add("accessibleContext");
	        copyIgnoreProperties.add("actionMap");
	        copyIgnoreProperties.add("alignmentX");
	        copyIgnoreProperties.add("alignmentY");
	        copyIgnoreProperties.add("ancestorListeners");
	        copyIgnoreProperties.add("autoscrolls");
	        copyIgnoreProperties.add("border");
	        copyIgnoreProperties.add("class");
	        copyIgnoreProperties.add("component");
	        copyIgnoreProperties.add("componentPopupMenu");
	        copyIgnoreProperties.add("containerListeners");
	        copyIgnoreProperties.add("contentPane");
	        copyIgnoreProperties.add("cursorManager");
	        copyIgnoreProperties.add("debugGraphicsOptions");
	        copyIgnoreProperties.add("doubleBuffered");
	        copyIgnoreProperties.add("enabled");
	        copyIgnoreProperties.add("focusCycleRoot");
	        copyIgnoreProperties.add("focusTraversalKeys");
	        copyIgnoreProperties.add("focusTraversalPolicy");
	        copyIgnoreProperties.add("focusTraversalPolicyProvider");
	        copyIgnoreProperties.add("focusTraversalPolicySet");
	        copyIgnoreProperties.add("focusable");
	        copyIgnoreProperties.add("fontRenderContext");
	        copyIgnoreProperties.add("graphics");
	        copyIgnoreProperties.add("height");
	        copyIgnoreProperties.add("ignoreTreeSelection");
	        copyIgnoreProperties.add("inheritsPopupMenu");
	        copyIgnoreProperties.add("inputMap");
	        copyIgnoreProperties.add("inputVerifier");
	        copyIgnoreProperties.add("insets");
	        copyIgnoreProperties.add("layout");
	        copyIgnoreProperties.add("managingFocus");
	        copyIgnoreProperties.add("maximumSize");
	        copyIgnoreProperties.add("minimumSize");
	        copyIgnoreProperties.add("mouseMode");
	        copyIgnoreProperties.add("name");
	        copyIgnoreProperties.add("nextFocusableComponent");
	        copyIgnoreProperties.add("opaque");
	        copyIgnoreProperties.add("optimizedDrawingEnabled");
	        copyIgnoreProperties.add("paintingEnabled");
	        copyIgnoreProperties.add("paintingTile");
	        copyIgnoreProperties.add("panel");
	        copyIgnoreProperties.add("playPenContentPane");	        
	        copyIgnoreProperties.add("preferredScrollableViewportSize");
	        copyIgnoreProperties.add("preferredSize");
	        copyIgnoreProperties.add("registeredKeyStrokes");
	        copyIgnoreProperties.add("requestFocusEnabled");
	        copyIgnoreProperties.add("rootPane");
	        copyIgnoreProperties.add("scrollableTracksViewportHeight");
	        copyIgnoreProperties.add("scrollableTracksViewportWidth");
	        copyIgnoreProperties.add("selectedItems");
	        copyIgnoreProperties.add("selectedRelationShips");
	        copyIgnoreProperties.add("selectedTables");
	        copyIgnoreProperties.add("session");
	        copyIgnoreProperties.add("topLevelAncestor");
	        copyIgnoreProperties.add("toolTipText");
	        copyIgnoreProperties.add("transferHandler");
	        copyIgnoreProperties.add("usedArea");
	        copyIgnoreProperties.add("validateRoot");
	        copyIgnoreProperties.add("verifyInputWhenFocusTarget");
	        copyIgnoreProperties.add("vetoableChangeListeners");
	        copyIgnoreProperties.add("viewPosition");
	        copyIgnoreProperties.add("viewportSize");
	        copyIgnoreProperties.add("visible");
	        copyIgnoreProperties.add("visibleRect");
	        copyIgnoreProperties.add("width");
	        copyIgnoreProperties.add("x");
	        copyIgnoreProperties.add("y");
	        
	        copyIgnoreProperties.add("draggingTablePanes");
	        copyIgnoreProperties.add("rubberBand");
	        
	        // These should not be copied because any new PlayPen needs
	        // different values or else it will not work on the new
	        // PlayPen.
	        copyIgnoreProperties.add("mouseZoomInAction");
	        copyIgnoreProperties.add("mouseZoomOutAction");
	        copyIgnoreProperties.add("scrollPane");
	        
	        // we're not sure if zoom should be duplicated...
	        // it might mess up printing?!?!?
	        copyIgnoreProperties.add("zoom");
	        
	        // individual lists (e.g. tables) checked instead
	        copyIgnoreProperties.add("components");
	        
	        // The copy of the play pen is for things like print preview, so we don't want to
	        // duplicate menus and other interactive features. (?)
	        copyIgnoreProperties.add("popupFactory");
	        
	        // First pass: set all settable properties, because testing the duplication of
	        //             an object with all its properties at their defaults is not a
	        //             very convincing test of duplication!
	        for (PropertyDescriptor property : settableProperties) {
	            if (copyIgnoreProperties.contains(property.getName())) continue;
	            Object oldVal;
	            try {
	                oldVal = PropertyUtils.getSimpleProperty(pp, property.getName());
	                // check for a setter
	                if (property.getWriteMethod() != null)
	                {
	                    Object newVal = getNewDifferentValue(property, oldVal);
	                    BeanUtils.copyProperty(pp, property.getName(), newVal);
	                }
	            } catch (NoSuchMethodException e) {
	                logger.warn("Skipping non-settable property "+property.getName()+" on "+pp.getClass().getName());
	            }
	        }
	        // Second pass get a copy make sure all of 
	        // the original mutable objects returned from getters are different
	        // between the two objects, but have the same values. 
	        PlayPen duplicate = new PlayPen(pp.getSession(), pp);
	        for (PropertyDescriptor property : settableProperties) {
	            logger.info(property.getName() + property.getDisplayName() + property.getShortDescription());
	            if (copyIgnoreProperties.contains(property.getName())) continue;
	            Object oldVal;
	            try {
	                oldVal = PropertyUtils.getSimpleProperty(pp, property.getName());
	                Object copyVal = PropertyUtils.getSimpleProperty(duplicate, property.getName());
	                if (oldVal == null) {
	                    throw new NullPointerException("We forgot to set "+property.getName());
	                } else {
	                    assertEquals("The two values for property "+property.getDisplayName() + " in " + pp.getClass().getName() + " should be equal",oldVal,copyVal);

	                    if (isPropertyInstanceMutable(property)) {
	                        assertNotSame("Copy shares mutable property with original, property name: " + property.getDisplayName(), copyVal, oldVal);
	                    }
	                }
	            } catch (NoSuchMethodException e) {
	                logger.warn("Skipping non-settable property "+property.getName()+" on "+pp.getClass().getName());
	            }
	        }
	}
	
	/**
	 * This tests that copying and pasting a new table into
	 * the play pen works. This also confirms that the new table
	 * copied is not the source of the columns.
	 */
	public void testPasteNewTable() throws Exception {
	    SQLTable table = new SQLTable(null, "NewTable", "Remarks", "TABLE", true);
	    table.addColumn(new SQLColumn(table, "NewCol1", Types.VARCHAR, 50, 0));
	    table.addColumn(new SQLColumn(table, "NewCol2", Types.NUMERIC, 50, 0));
	    
	    DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(pp.getSession(), table);
	    duplicateProperties.setDefaultTransferStyle(TransferStyles.COPY);
        pp.importTableCopy(table, new Point(0, 0), duplicateProperties);
	    assertEquals(1, pp.getTables().size());
	    SQLTable copy = pp.getTables().get(0);
	    assertEquals("NewTable", copy.getName());
	    assertEquals(2, copy.getColumns().size());
	    assertTrue(copy.getColumnByName("NewCol1") != null);
	    assertTrue(copy.getColumnByName("NewCol2") != null);
	    assertNull(copy.getColumnByName("NewCol1").getSourceColumn());
	    assertNull(copy.getColumnByName("NewCol2").getSourceColumn());
	}

    /**
     * This tests that copying and pasting a table with source information into
     * the play pen works. This also confirms that the new table copied has the
     * same sources as the original table and does not use the table copied from
     * as its source. Note that this is testing copy, not reverse engineering.
     */
    public void testPasteCopyTable() throws Exception {
        SQLDatabase db = new SQLDatabase();
        pp.getSession().getRootObject().addChild(db);
        SQLColumn sourceCol = new SQLColumn();
        SQLTable sourceTable = new SQLTable(db, true);
        sourceTable.addColumn(sourceCol);
        SQLTable table = new SQLTable(db, "NewTable", "Remarks", "TABLE", true);
        final SQLColumn col1 = new SQLColumn(table, "NewCol1", Types.VARCHAR, 50, 0);
        col1.setSourceColumn(sourceCol);
        table.addColumn(col1);
        final SQLColumn col2 = new SQLColumn(table, "NewCol2", Types.NUMERIC, 50, 0);
        col2.setSourceColumn(sourceCol);
        table.addColumn(col2);
        
        DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(pp.getSession(), table);
        duplicateProperties.setDefaultTransferStyle(TransferStyles.COPY);
        pp.importTableCopy(table, new Point(0, 0), duplicateProperties);
        assertEquals(1, pp.getTables().size());
        SQLTable copy = pp.getTables().get(0);
        assertEquals("NewTable", copy.getName());
        assertEquals(2, copy.getColumns().size());
        assertTrue(copy.getColumnByName("NewCol1") != null);
        assertTrue(copy.getColumnByName("NewCol2") != null);
        assertEquals(sourceCol, copy.getColumnByName("NewCol1").getSourceColumn());
        assertEquals(sourceCol, copy.getColumnByName("NewCol2").getSourceColumn());
    }
    
    /**
     * This tests that copying and pasting a table with source information into
     * the play pen works. This also confirms that the new table copied uses
     * the table reverse engineered from as its source.
     */
    public void testPasteReverseEngineeredTable() throws Exception {
        SQLDatabase db = new SQLDatabase();
        pp.getSession().getRootObject().addChild(db);
        SQLColumn sourceCol = new SQLColumn();
        SQLTable sourceTable = new SQLTable(db, true);
        sourceTable.addColumn(sourceCol);
        SQLTable table = new SQLTable(db, "NewTable", "Remarks", "TABLE", true);
        final SQLColumn col1 = new SQLColumn(table, "NewCol1", Types.VARCHAR, 50, 0);
        col1.setSourceColumn(sourceCol);
        table.addColumn(col1);
        final SQLColumn col2 = new SQLColumn(table, "NewCol2", Types.NUMERIC, 50, 0);
        col2.setSourceColumn(sourceCol);
        table.addColumn(col2);
        
        DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(pp.getSession(), table);
        pp.importTableCopy(table, new Point(0, 0), duplicateProperties);
        assertEquals(1, pp.getTables().size());
        SQLTable copy = pp.getTables().get(0);
        assertEquals("NewTable", copy.getName());
        assertEquals(2, copy.getColumns().size());
        assertTrue(copy.getColumnByName("NewCol1") != null);
        assertTrue(copy.getColumnByName("NewCol2") != null);
        assertEquals(col1, copy.getColumnByName("NewCol1").getSourceColumn());
        assertEquals(col2, copy.getColumnByName("NewCol2").getSourceColumn());
    }
    
    /**
     * This tests that copying and pasting a table from one session to another
     * session within the same context will add the data source to the new session
     * and add the table properly.
     */
    public void testPasteTableAcrossSessions() throws Exception {
        ArchitectSwingSessionContext context = pp.getSession().getContext();
        context.setPlDotIniPath("pl.regression.ini");
        DataSourceCollection<JDBCDataSource> pl = context.getPlDotIni();
        JDBCDataSource ds = pl.getDataSource("regression_test");
        Connection con = ds.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("Create table newtable (newcol1 varchar(50), newcol2 varchar(50))");
        stmt.close();
        con.close();
        
        SQLDatabase db = new SQLDatabase(ds);
        SQLTable table = db.getTableByName("newtable");
        
        //New play pen in same context
        PlayPen newPP = pp.getSession().getContext().createSession().getPlayPen();
        
        DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(newPP.getSession(), table);
        newPP.importTableCopy(table, new Point(0, 0), duplicateProperties);
        assertEquals(1, newPP.getTables().size());
        SQLTable copy = newPP.getTables().get(0);
        assertEquals("NEWTABLE", copy.getName().toUpperCase());
        assertEquals(2, copy.getColumns().size());
        assertTrue(copy.getColumnByName("NewCol1") != null);
        assertTrue(copy.getColumnByName("NewCol2") != null);
        assertTrue(table.getColumnByName("newcol1") != copy.getColumnByName("NewCol1").getSourceColumn());
        assertTrue(table.getColumnByName("newcol2") != copy.getColumnByName("NewCol2").getSourceColumn());
    }
	
    /**
     * Returns a new value that is not equal to oldVal. The
     * returned object will be a new instance compatible with oldVal.  
     * 
     * @param property The property that should be modified.
     * @param oldVal The existing value of the property to modify.  The returned value
     * will not equal this one at the time this method was first called.
     */
	private Object getNewDifferentValue(PropertyDescriptor property, Object oldVal) {
	    Object newVal; // don't init here so compiler can warn if the
	    // following code doesn't always give it a value
	    if (property.getPropertyType() == String.class) {
	        newVal = "new " + oldVal;
	    } else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE) {
	        if (oldVal == null){
	            newVal = new Boolean(false);
	        } else {
	            newVal = new Boolean(!((Boolean) oldVal).booleanValue());
	        }
	    } else if (property.getPropertyType() == Double.class || property.getPropertyType() == Double.TYPE) {
	        if (oldVal == null) {
	            newVal = new Double(0);
	        } else {
	            newVal = new Double(((Double) oldVal).doubleValue() + 1);
	        }
	    } else if (property.getPropertyType() == Integer.class || property.getPropertyType() == Integer.TYPE) {
            if (oldVal == null) {
                newVal = new Integer(0);
            } else {
                newVal = new Integer(((Integer) oldVal).intValue() + 1);
            }
        } else if (property.getPropertyType() == Color.class) {
	        if (oldVal == null) {
	            newVal = new Color(0xFAC157);
	        } else {
	            Color oldColor = (Color) oldVal;
	            newVal = new Color((oldColor.getRGB()+0xF00) % 0x1000000);
	        }
	    } else if (property.getPropertyType() == Font.class) {
	        if (oldVal == null) {
	            newVal = FontManager.getDefaultPhysicalFont();
	        } else {
	            Font oldFont = (Font) oldVal;
	            newVal = new Font(oldFont.getFontName(), oldFont.getSize() + 2, oldFont.getStyle());
	        }
	    } else if (property.getPropertyType() == Set.class) {
	        newVal = new HashSet();
	        ((Set) newVal).add("test");
	    } else if (property.getPropertyType() == List.class) {
	        newVal = new ArrayList();
	        ((List) newVal).add("test");
	    } else if (property.getPropertyType() == Point.class) {
	        newVal = new Point(1,3);
	    } else {
	        throw new RuntimeException("This test case lacks a value for "
	                + property.getName() + " (type "
	                + property.getPropertyType().getName() + ") in getNewDifferentValue()");
	    }

	    return newVal;
	}
	
	/**
     * Returns true if an instance of the given property type is of a mutable class.
     * Throws an exception if it lacks a case for the given type.
     * 
     * @param property The property that should be checked for mutability.
     */
	private boolean isPropertyInstanceMutable(PropertyDescriptor property) {
        if (property.getPropertyType() == String.class) {
            return false;
        } else if (Enum.class.isAssignableFrom(property.getPropertyType())) {
            return false;
        } else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE) {
            return false;
        } else if (property.getPropertyType() == Double.class || property.getPropertyType() == Double.TYPE) {
            return false;
        } else if (property.getPropertyType() == Integer.class || property.getPropertyType() == Integer.TYPE) {
            return false;
        } else if (property.getPropertyType() == Color.class) {
            return false;
        } else if (property.getPropertyType() == Font.class) {
            return false;
        } else if (property.getPropertyType() == Set.class) {
            return true;
        } else if (property.getPropertyType() == List.class) {
            return true;
        } else if (property.getPropertyType() == Point.class) {
            return true;
        } else {
            throw new RuntimeException("This test case lacks a value for "
                    + property.getName() + " (type "
                    + property.getPropertyType().getName() + ") in isPropertyInstanceMutable()");
        }
    }
}