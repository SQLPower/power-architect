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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;
import ca.sqlpower.swingui.DataEntryPanel;

public class TableEditPanel extends JPanel implements SQLObjectListener, DataEntryPanel {
    
    private static final Logger logger = Logger.getLogger(TableEditPanel.class);

    /**
     * The frame which this table edit panel resides in.
     */
    private JDialog editDialog;
	protected SQLTable table;
	JTextField name;
	JTextField pkName;
	JTextArea remarks;
	private JComboBox bgColor;
	private JComboBox fgColor;
	private JCheckBox rounded;
	private JCheckBox dashed;
	
	private final ArchitectSwingSession session;
	private final TablePane tp;
	
	public TableEditPanel(ArchitectSwingSession session, SQLTable t) {
		super(new FormLayout());
		this.session = session;
		this.tp = session.getPlayPen().findTablePane(t);
		addUndoEventListener(session.getUndoManager().getEventAdapter());
		add(new JLabel("Table Name"));
		add(name = new JTextField("", 30));
		add(new JLabel("Primary Key Name"));
		add(pkName = new JTextField("", 30));
		add(new JLabel("Remarks"));
		add(new JScrollPane(remarks = new JTextArea(4, 30)));
		remarks.setLineWrap(true);
		remarks.setWrapStyleWord(true);
		
		add(new JLabel("Table Colour"));
		ColorCellRenderer renderer = new ColorCellRenderer();
		bgColor = new JComboBox(ColorScheme.BACKGROUND_COLOURS);
        bgColor.setRenderer(renderer);
        bgColor.addItem(new Color(240, 240, 240));
		add(bgColor);
		
		add(new JLabel("Text Colour"));
		fgColor = new JComboBox(ColorScheme.FOREGROUND_COLOURS);
        fgColor.setRenderer(renderer);
        fgColor.addItem(Color.BLACK);
        add(fgColor);
        
        add(new JLabel("Dashed Lines"));
        add(dashed = new JCheckBox());
        add(new JLabel("Rounded Corners"));
        add(rounded = new JCheckBox());        
        
		editTable(t);
		try {
            ArchitectUtils.listenToHierarchy(this, session.getRootObject());
        } catch (ArchitectException e) {
            e.printStackTrace();
        }
	}

	public void editTable(SQLTable t) {
		table = t;
		name.setText(t.getName());
        try {
            if (t.getPrimaryKeyIndex() == null) {
                pkName.setEnabled(false);
            } else {
                pkName.setText(t.getPrimaryKeyName());
                pkName.setEnabled(true);
            }
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
		remarks.setText(t.getRemarks());
		name.selectAll();
		
		if (tp != null) {
    		bgColor.setSelectedItem(tp.getBackground());
    		fgColor.setSelectedItem(tp.getForeground());
    		dashed.setSelected(tp.isDashed());
    		rounded.setSelected(tp.isRounded());
		}
	}

	// --------------------- ArchitectPanel interface ------------------
	public boolean applyChanges() {
		startCompoundEdit("Table Properties Change");		
        try {	
		    StringBuffer warnings = new StringBuffer();
            //We need to check if the table name and/or primary key name is empty or not
            //if they are, we need to warn the user since it will mess up the SQLScripts we create
            if (name.getText().trim().length() == 0) {
                warnings.append("The table cannot be assigned a blank name \n");
                
            }
            if (pkName.isEnabled() &&
                    pkName.getText().trim().length() == 0) {
                warnings.append("The primary key cannot be assigned a blank name");                
            }
            
            if (warnings.toString().length() == 0) {
                
                // important: set the primary key name first, because if the primary
                // key was called (for example) new_table_pk, and the table was called
                // new_table, then the user changes the table name to cow_table, the
                // table itself will notice this pattern and automatically change its
                // primary key name to cow_table_pk.  If we set the table name first,
                // the magic still happens, but then we would overwrite the new pk name
                // with the old one from the pk name text field in this panel.
                if (pkName.isEnabled() && table.getPrimaryKeyIndex() != null) {
                    table.getPrimaryKeyIndex().setName(pkName.getText());
                }
                
                table.setName(name.getText());
                table.setRemarks(remarks.getText());   
                
                if (tp != null) {
                    tp.setBackground((Color)bgColor.getSelectedItem());
                    tp.setForeground((Color)fgColor.getSelectedItem());
                    tp.setDashed(dashed.isSelected());
                    tp.setRounded(rounded.isSelected());
                }
                return true;
            } else{
                JOptionPane.showMessageDialog(this,warnings.toString());
                //this is done so we can go back to this dialog after the error message
                return false;
            }            
		} catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        } finally {
			endCompoundEdit("Ending new compound edit event in table edit panel");
		}
	}

	public void discardChanges() {
	    // TODO revert the changes made
	}
	
	/**
	 * The list of SQLObject property change event listeners
	 * used for undo
	 */
	protected LinkedList<UndoCompoundEventListener> undoEventListeners = new LinkedList<UndoCompoundEventListener>();

	
	public void addUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.add(l);
	}

	public void removeUndoEventListener(UndoCompoundEventListener l) {
		undoEventListeners.remove(l);
	}
	
	protected void fireUndoCompoundEvent(UndoCompoundEvent e) {
		Iterator it = undoEventListeners.iterator();
		
		if (e.getType().isStartEvent()) {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditStart(e);
			}
		} else {
			while (it.hasNext()) {
				((UndoCompoundEventListener) it.next()).compoundEditEnd(e);
			}
		} 
	}

	public void startCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_START,message));
	}
	
	public void endCompoundEdit(String message){
		fireUndoCompoundEvent(new UndoCompoundEvent(EventTypes.COMPOUND_EDIT_END,message));
	}
	
	public JPanel getPanel() {
		return this;
	}

    public String getNameText() {
        return name.getText();
    }

    public void setNameText(String newName) {
        name.setText(newName);
    }

    public String getPkNameText() {
        return pkName.getText();
    }

    public void setPkNameText(String newPkName) {
        pkName.setText(newPkName);
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    // -----------------Methods from SQLObjectListener------------------- //
    
    public void dbChildrenInserted(SQLObjectEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void dbChildrenRemoved(SQLObjectEvent e) {
        logger.debug("SQLObject children got removed: "+e);
        boolean itemDeleted = false;
        SQLObject[] c = e.getChildren();
        
        for (int i = 0; i < c.length; i++) {            
            try {
                if(this.table.equals(c[i])) {
                    itemDeleted = true;
                    break;
                }
            } catch (Exception ex) {
                logger.error("Could not compare the removed sql objects.", ex);
            }
        }
        if(itemDeleted) {
            if(this.editDialog != null) {
                this.editDialog.setVisible(false);
                this.editDialog.dispose();
            }
            itemDeleted = false;
        }        
    }

    public void dbObjectChanged(SQLObjectEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void dbStructureChanged(SQLObjectEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void setEditDialog(JDialog editDialog) {
        this.editDialog = editDialog;
    }
    
    /**
     * Renders a rectangle of colour in a list cell.  The colour is determined
     * by the list item value, which must be of type java.awt.Color.
     */
    private class ColorCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            if (value == null) {
                value = Color.BLACK;
            }
            setBackground((Color) value);
            setOpaque(true);
            setPreferredSize(new Dimension(40, 20));
            setIcon(new ColorIcon((Color) value));
            return this;
        }
    }
    
    /**
     * This class converts a Color into an icon that has width of 85 pixels
     * and height of 50 pixels.
     */
    private class ColorIcon implements Icon {
        private int HEIGHT = 20;
        private int WIDTH = 40;
        
        private Color colour;
     
        public ColorIcon(Color colour) {
            this.colour = colour;
        }
     
        public int getIconHeight() {
            return HEIGHT;
        }
     
        public int getIconWidth() {
            return WIDTH;
        }
     
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(colour);
            g.fillRect(x, y, WIDTH - 1, HEIGHT - 1);
        }
    }
}
