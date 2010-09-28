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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.AbstractPlacer;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.TableEditPanel;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * Action for creating a table and putting it in both the business
 * model and the play pen.
 */
public class CreateTableAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(CreateTableAction.class);
	
	// The default name of a table when it is initially created.
	private static final String NEW_TABLE_NAME = "New_Table";

	private final ArchitectSwingSession session;
	
	public CreateTableAction(ArchitectSwingSession session) {
        super(session,
                Messages.getString("CreateTableAction.name"), //$NON-NLS-1$
                Messages.getString("CreateTableAction.description"), //$NON-NLS-1$
                "new_table"); //$NON-NLS-1$
        this.session = session;
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
	}

	public void actionPerformed(ActionEvent evt) {
		playpen.fireCancel();
		SQLTable t = null; 
		t = new SQLTable();
		t.initFolders(true);
		t.setName(NEW_TABLE_NAME); //$NON-NLS-1$
		
		TablePane tp = new TablePane(t, playpen.getContentPane());
		TablePlacer tablePlacer = new TablePlacer(playpen, tp);
		tablePlacer.dirtyup();
	}
	
	private class TablePlacer extends AbstractPlacer {
	    
	    private final TablePane tp;

	    TablePlacer(PlayPen playpen, TablePane tp) {
	        super(playpen);
	        this.tp = tp;
	    }
	    
        @Override
        protected String getEditDialogTitle() {
            return Messages.getString("CreateTableAction.tablePropertiesDialogTitle"); //$NON-NLS-1$
        }

        @Override
        public DataEntryPanel place(final Point p) throws SQLObjectException {
            DataEntryPanel editPanel = null;

            editPanel = new TableEditPanel(playpen.getSession(), tp.getModel()) {
                @Override
                public boolean applyChanges() {
                    String warnings = generateWarnings();
                    if (warnings.length() == 0) {
                        try {          
                            session.getWorkspace().begin("Creating a SQLTable and TablePane");
                            if (super.applyChanges()) {
                                tp.setName(table.getName());
                                session.getTargetDatabase().addChild(tp.getModel());
                                playpen.selectNone();
                                playpen.addTablePane(tp, p);
                                tp.setSelected(true, SelectionEvent.SINGLE_SELECT);
                                session.getWorkspace().commit();
                                return true;
                            } else {
                                session.getWorkspace().rollback("Error creating table and table pane");
                                return false;
                            }
                        } catch (Throwable t) {
                            session.getWorkspace().rollback("Error creating table and table pane");
                            throw new RuntimeException(t);
                        }
                    } else {
                        JOptionPane.showMessageDialog(getPanel(),warnings);
                        //this is done so we can go back to this dialog after the error message
                        return false;
                    }
                }
            };
            
            ((TableEditPanel) editPanel).setNameText(NEW_TABLE_NAME);
            ((TableEditPanel) editPanel).setPhysicalNameText(NEW_TABLE_NAME);
            ((TableEditPanel) editPanel).setPkNameText(NEW_TABLE_NAME + "_pk");
            
            return editPanel;
        }
	}
}
