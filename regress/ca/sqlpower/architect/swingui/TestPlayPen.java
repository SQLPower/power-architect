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
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;

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

	public void testUndoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp);
		ppdb.addChild(t);

		pp.addTablePane(tp, new Point(99,98));



		// this isn't the point of the test, but adding the tablepane has to work!
		assertNotNull(pp.findTablePane(t));

		//Undo the add child and the move table pane
		af.getUndoManager().undo();

		assertNull(pp.findTablePane(t));
	}

	public void testRedoAddTable() throws ArchitectException {
		SQLTable t = new SQLTable(ppdb, "test_me", "", "TABLE", true);

		TablePane tp = new TablePane(t, pp);

		ppdb.addChild(t);
		pp.addTablePane(tp, new Point(99,98));

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
	public void testImportTableCopyHijacksProperly() throws ArchitectException {

		SQLDatabase sourceDB = new SQLDatabase();

		SQLTable sourceParentTable = new SQLTable(sourceDB, true);
		sourceParentTable.setName("parent");
		sourceParentTable.addColumn(new SQLColumn(sourceParentTable, "key", Types.BOOLEAN, 1, 0));
		sourceParentTable.getColumn(0).setPrimaryKeySeq(0);
		sourceDB.addChild(sourceParentTable);

		SQLTable sourceChildTable = new SQLTable(sourceDB, true);
		sourceChildTable.setName("child");
		sourceChildTable.addColumn(new SQLColumn(sourceChildTable, "key", Types.BOOLEAN, 1, 0));
		sourceDB.addChild(sourceChildTable);

		SQLRelationship sourceRel = new SQLRelationship();
		sourceRel.attachRelationship(sourceParentTable, sourceChildTable, true);

		pp.importTableCopy(sourceChildTable, new Point(10, 10));
		pp.importTableCopy(sourceParentTable, new Point(10, 10));
		pp.importTableCopy(sourceParentTable, new Point(10, 10));

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

        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.getColumn(0).setPrimaryKeySeq(0);
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10));
        
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

        SQLTable table = new SQLTable(sourceDB, true);
        table.setName("self_ref");
        SQLColumn pkCol = new SQLColumn(table, "key", Types.INTEGER, 10, 0);
        table.addColumn(pkCol);
        table.getColumn(0).setPrimaryKeySeq(0);
        SQLColumn fkCol = new SQLColumn(table, "self_ref_column", Types.INTEGER, 10, 0);
        table.addColumn(fkCol);
        
        SQLRelationship rel = new SQLRelationship();
        rel.attachRelationship(table, table, false);
        rel.addMapping(pkCol, fkCol);
        sourceDB.addChild(table);
        
        pp.importTableCopy(table, new Point(10, 10));
        pp.importTableCopy(table, new Point(30, 30));
        
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
            List<SQLRelationship> importedKeys = t.getImportedKeys();
            
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
	        
	        // we're not sure if zoom should be duplicated...
	        // it might mess up printing?!?!?
	        copyIgnoreProperties.add("zoom");
	        
	        // individual lists (e.g. tables) checked instead
	        copyIgnoreProperties.add("components");
	        
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
	            if (copyIgnoreProperties.contains(property.getName())) continue;
	            Object oldVal;
	            try {
	                oldVal = PropertyUtils.getSimpleProperty(pp, property.getName());
	                Object copyVal = PropertyUtils.getSimpleProperty(duplicate, property.getName());
	                if (oldVal == null) {
	                    throw new NullPointerException("We forgot to set "+property.getName());
	                } else {
	                    assertEquals("The two values for property "+property.getDisplayName() + " in " + pp.getClass().getName() + " should be equal",oldVal,copyVal);

	                    // should only be applicable to mutable properties
	                    assertNotSame("Copy shares mutable property with original, property name: " + property.getDisplayName(), copyVal, oldVal);
	                }
	            } catch (NoSuchMethodException e) {
	                logger.warn("Skipping non-settable property "+property.getName()+" on "+pp.getClass().getName());
	            }
	        }
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
	                + property.getPropertyType().getName() + ")");
	    }

	    return newVal;
	}
}