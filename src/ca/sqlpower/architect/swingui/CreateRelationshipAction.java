package ca.sqlpower.architect.swingui;

import java.awt.event.*;
import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import ca.sqlpower.architect.*;

import org.apache.log4j.Logger;

public class CreateRelationshipAction extends AbstractAction
	implements ActionListener, SelectionListener {

	private static final Logger logger = Logger.getLogger(CreateRelationshipAction.class);

	protected boolean identifying;
	protected PlayPen pp;
	protected TablePane pkTable;
	protected TablePane fkTable;

	/**
	 * This property is true when we are actively creating a
	 * relationship.  The original implementation was to add and
	 * remove this action from the playpen selection listener list,
	 * but it caused ConcurrentModificationException.
	 */
	protected boolean active;

	public CreateRelationshipAction(boolean identifying) {
		super(identifying ? "New Identifying Relationship" : "New Non-Identifying Relationship",
			  ASUtils.createIcon
			  (identifying ? "NewIdentifyingRelationship" : "NewNonIdentifyingRelationship",
			   "New Relationship",
			   ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		if (identifying) {
			putValue(SHORT_DESCRIPTION, "New Identifying Relationship");
		} else {
			putValue(SHORT_DESCRIPTION, "New Non-Identifying Relationship");
		}
		this.identifying = identifying;
		setEnabled(false);
		logger.debug("(constructor) hashcode is: " + super.hashCode());
	}

	public void actionPerformed(ActionEvent evt) {
		logger.debug("the hashcode is: " + super.hashCode());
		pkTable = null;
		fkTable = null;
		logger.debug("Starting to create relationship, setting active to TRUE!");
		active = true;
		pp.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		// gets over the "can't select a selected item"
		pp.selectNone();
	}

	protected void doCreateRelationship() {
		try {
			SQLRelationship model = new SQLRelationship();
			// XXX: need to ensure uniqueness of setName(), but 
			// to_identifier should take care of this...			
			model.setName(pkTable.getModel().getName()+"_"+fkTable.getModel().getName()+"_fk"); 
			model.setIdentifying(identifying);
			model.setPkTable(pkTable.getModel());
			model.setFkTable(fkTable.getModel());

			pkTable.getModel().addExportedKey(model);
			fkTable.getModel().addImportedKey(model);
			
			// iterate over a copy of pktable's column list to avoid comodification
			// when creating a self-referencing table
			java.util.List pkColListCopy = new ArrayList(pkTable.getModel().getColumns().size());
			pkColListCopy.addAll(pkTable.getModel().getColumns());
			Iterator pkCols = pkColListCopy.iterator();
			while (pkCols.hasNext()) {
				SQLColumn pkCol = (SQLColumn) pkCols.next();
				if (pkCol.getPrimaryKeySeq() == null) break;
				SQLColumn fkCol = (SQLColumn) pkCol.clone();
				// check to see if the FK table already has this column 
				SQLColumn match = fkTable.getModel().getColumnByName(pkCol.getName());
				if (match != null) {
					// there is already a column of this name
					if (match.getType() == pkCol.getType() &&
					    match.getPrecision() == pkCol.getPrecision() &&
						match.getScale() == pkCol.getScale()) {
						// column is an exact match, so we don't have to recreate it
						fkCol = match; 
						fkCol.addReference(); // reference counting, stops column from being removed if relationship is removed					
					} else {
						// ask the user if they would like to rename the column 
						// or cancel the creation of the relationship						
						int decision = JOptionPane.showConfirmDialog(pp,
								 "The primary key column " + pkCol.getName() + " already exists " +
								 " in the child table.  Continue using new name " +
								 pkCol.getName() + "_1 ?",
								 "Column Name Conflict",
								 JOptionPane.YES_NO_OPTION);
						if (decision == JOptionPane.YES_OPTION) {
							// XXX: need to ensure uniqueness of setName(), 
							// but to_identifier in DDLGenerator should take 
							// care of this
							fkCol.setName(generateUniqueColumnName(pkCol,fkTable.getModel())); 
						} else {
							model = null;
							return; // abort the creation of this relationship
						}										
						fkTable.getModel().addColumn(fkCol);
					}
				} else {
					// no match, so we need to import this column from PK table
					fkTable.getModel().addColumn(fkCol);
				}
				
				if (identifying && fkCol.getPrimaryKeySeq() == null) {
					// add column to primary key (but only if it's not already there!!!
					fkCol.setPrimaryKeySeq(new Integer(fkTable.getModel().pkSize()));
				}
				
				model.addMapping(pkCol, fkCol);
				
				
			}
			
			Relationship r = new Relationship(pp, model);
			pp.add(r);
			r.repaint();  // XXX: shouldn't be necessary, but it is.
		} catch (ArchitectException ex) {
			logger.error("Couldn't create relationship", ex);
			JOptionPane.showMessageDialog(pp, "Couldn't create relationship: "+ex.getMessage());
		}
	}
	
	/*
	 *  Ideally, loop through until you get a unique column name...
	 */
	private String generateUniqueColumnName(SQLColumn column, SQLTable table) {		
		return column.getName() + "_1";  // XXX: fix this to be better
	}
	
	public void setPlayPen(PlayPen playpen) {
		if (pp != null) {
			pp.removeSelectionListener(this);
		}
		pp = playpen;
		if (pp != null) {
			pp.addSelectionListener(this);
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	// -------------------- SELECTION EVENTS --------------------
	
	public void itemSelected(SelectionEvent e) {
		// what address am I?
		logger.debug("00000000000 object hash code: " + super.hashCode());

	
		// ignore events unless active
		logger.debug("11111111ITEM SELECTED: " + e);

		if (!active) {
			logger.debug("222222222 not active.");
			return;
		} else {
			logger.debug("222222222 ACTIVE!!!.");			
		}

		Selectable s = e.getSelectableSource();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) {
			logger.debug("333333333 not selected.");			
			return;
		}

		if (s instanceof TablePane) {
			logger.debug("4444444444444 instance of TablePane.");						
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("555555555555 Creating relationship: PK Table is "+pkTable);
			} else {
				fkTable = (TablePane) s;
				logger.debug("66666666666666 Creating relationship: FK Table is "+fkTable);
				doCreateRelationship();  // this might fail, but still set things back to "normal"
				pp.setCursor(null);
				logger.debug("66666666666666 setting active to FALSE!");
				active = false;
			}
		} else {
			logger.debug("777777777777 not instance of TablePane.");						
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s);
		}
	}

	public void itemDeselected(SelectionEvent e) {
		// don't particularly care
	}
}
