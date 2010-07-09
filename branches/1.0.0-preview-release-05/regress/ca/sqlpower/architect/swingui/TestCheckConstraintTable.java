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

package ca.sqlpower.architect.swingui;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import junit.framework.TestCase;
import ca.sqlpower.sqlobject.SQLCheckConstraint;
import ca.sqlpower.sqlobject.SQLCheckConstraintContainer;
import ca.sqlpower.testutil.GenericNewValueMaker;
import ca.sqlpower.testutil.NewValueMaker;
import ca.sqlpower.testutil.SPObjectRoot;

public class TestCheckConstraintTable extends TestCase {
    
    private SPObjectRoot root;
    
    private NewValueMaker newValueMaker;
    
    private SQLCheckConstraintContainer container;
    
    private CheckConstraintTable table;
    
    private CheckConstraintTableModelListener listener;
    
    @Override
    protected void setUp() throws Exception {
        root = new SPObjectRoot();
        newValueMaker = new GenericNewValueMaker(root);
        container = (SQLCheckConstraintContainer) newValueMaker.makeNewValue(SQLCheckConstraintContainer.class, null, "SQLCheckConstraintContainer for TestCheckConstraintTable");
        table = new CheckConstraintTable();
        listener = new CheckConstraintTableModelListener(container);
        table.getModel().addTableModelListener(listener);
    }
    
    public void testTableUsesCorrectTableModel() throws Exception {
        TableModel model = table.getModel();
        assertTrue(model instanceof DefaultTableModel);
        assertEquals(2, table.getColumnCount());
        assertEquals(0, ((DefaultTableModel) model).getRowCount());
    }
    
    public void testAddRowCausesAddCheckConstraint() throws Exception {
        int constraintCount = container.getCheckConstraints().size();
        assertEquals(0, constraintCount);
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String name = (String) newValueMaker.makeNewValue(String.class, null, "SQLCheckConstraint name");
        String constraint = (String) newValueMaker.makeNewValue(String.class, null, "SQLCheckConstraint constraint condition");
        String[] row = {name, constraint};
        model.addRow(row);
        
        assertEquals((String) model.getValueAt(0, 0), container.getCheckConstraints().get(0).getName());
        assertEquals((String) model.getValueAt(0, 1), container.getCheckConstraints().get(0).getConstraint());
        
        assertEquals(constraintCount + 1, container.getCheckConstraints().size());
        assertEquals(name, container.getCheckConstraints().get(0).getName());
        assertEquals(constraint, container.getCheckConstraints().get(0).getConstraint());
    }
    
    public void testRemoveRowCausesRemoveCheckConstraint() throws Exception {
        testAddRowCausesAddCheckConstraint();
        int constraintCount = container.getCheckConstraints().size();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.removeRow(0);
        assertEquals(0, model.getRowCount());
        assertEquals(constraintCount - 1, container.getCheckConstraints().size());
    }
    
    public void testUpdateRowCausesUpdateCheckConstraint() throws Exception {
        testAddRowCausesAddCheckConstraint();
        int constraintCount = container.getCheckConstraints().size();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        String oldName = (String) model.getValueAt(0, 0);
        String newName = (String) newValueMaker.makeNewValue(String.class, oldName, "SQLCheckConstraint new name");
        model.setValueAt(newName, 0, 0);
        assertEquals(constraintCount, container.getCheckConstraints().size());
        assertEquals(newName, container.getCheckConstraints().get(0).getName());
        
        String oldConstraint = (String) model.getValueAt(0, 0);
        String newConstraint = (String) newValueMaker.makeNewValue(String.class, oldConstraint, "SQLCheckConstraint new constraint");
        model.setValueAt(newConstraint, 0, 1);
        assertEquals(constraintCount, container.getCheckConstraints().size());
        assertEquals(newConstraint, container.getCheckConstraints().get(0).getConstraint());
    }
    
    public void testAddOrUpdateRowWithBlankNameOrConstraintHasNoEffect() throws Exception {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String name = (String) newValueMaker.makeNewValue(String.class, null, "SQLCheckConstraint name");
        String constraint = (String) newValueMaker.makeNewValue(String.class, null, "SQLCheckConstraint constraint condition");
        
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        String[] blankNameRow = {"   ", constraint};
        model.addRow(blankNameRow);
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        String[] blankConstraintRow = {name, "   "};
        model.addRow(blankConstraintRow);
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        String[] blankNameAndConstraintRow = {" ", " "};
        model.addRow(blankNameAndConstraintRow);
        assertEquals(0, model.getRowCount());
        assertTrue(container.getCheckConstraints().isEmpty());
        
        testAddRowCausesAddCheckConstraint();
        model.setValueAt(" ", 0, 0);
        assertEquals(1, model.getRowCount());
        assertEquals(1, container.getCheckConstraints().size());
        model.setValueAt(" ", 0, 1);
        assertEquals(1, model.getRowCount());
        assertEquals(1, container.getCheckConstraints().size());
    }
    
    public void testAddRowWithDuplicateNameHasNoEffect() throws Exception {
        testAddRowCausesAddCheckConstraint();
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        SQLCheckConstraint checkConstraint = container.getCheckConstraints().get(0);
        String oldName = checkConstraint.getName();
        String oldConstraint = checkConstraint.getConstraint();
        
        String[] row = {oldName, "new " + oldConstraint};
        model.addRow(row);
        assertEquals(1, model.getRowCount());
        assertEquals(1, container.getCheckConstraints().size());
        assertEquals(oldName, container.getCheckConstraints().get(0).getName());
        assertEquals(oldConstraint, container.getCheckConstraints().get(0).getConstraint());
    }
    
}
