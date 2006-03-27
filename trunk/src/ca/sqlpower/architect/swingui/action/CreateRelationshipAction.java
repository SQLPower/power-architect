package ca.sqlpower.architect.swingui.action;

import java.awt.event.*;
import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import ca.sqlpower.architect.*;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.Selectable;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEvent.EventTypes;

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
			   ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
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

	static public void doCreateRelationship(SQLTable pkTable,SQLTable fkTable,PlayPen pp, boolean identifying) {
		try {
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(pkTable,EventTypes.RELATIONSHIP_START,"Starting the creation of a relationship"));
			SQLRelationship model = new SQLRelationship();
			// XXX: need to ensure uniqueness of setName(), but 
			// to_identifier should take care of this...			
			model.setName(pkTable.getName()+"_"+fkTable.getName()+"_fk"); 
			model.setIdentifying(identifying);
			model.setPkTable(pkTable);
			model.setFkTable(fkTable);
			
			fkTable.setSecondaryChangeMode(true);
			
			pkTable.addExportedKey(model);
			fkTable.addImportedKey(model);
			
			boolean askAboutHijackTypeMismatch = false;
			
			// iterate over a copy of pktable's column list to avoid comodification
			// when creating a self-referencing table
			java.util.List pkColListCopy = new ArrayList(pkTable.getColumns().size());
			pkColListCopy.addAll(pkTable.getColumns());
			Iterator pkCols = pkColListCopy.iterator();
			while (pkCols.hasNext()) {
				SQLColumn pkCol = (SQLColumn) pkCols.next();
				if (pkCol.getPrimaryKeySeq() == null) break;
				
				SQLColumn fkCol;
				SQLColumn match = fkTable.getColumnByName(pkCol.getName());
				if (match != null) {
					// does the matching column have a compatible data type?
					if (match.getType() == pkCol.getType() &&
					    match.getPrecision() == pkCol.getPrecision() &&
						match.getScale() == pkCol.getScale()) {
						// column is an exact match, so we don't have to recreate it
						fkCol = match; 
					} else {
						fkCol = (SQLColumn) pkCol.clone();
						askAboutHijackTypeMismatch = true;
					}
				} else {
					// no match, so we need to import this column from PK table
					fkCol = (SQLColumn) pkCol.clone();
				}
				
				try {
					fkCol.setSecondaryChangeMode(true);
					
					if (askAboutHijackTypeMismatch) {
						String newColName = generateUniqueColumnName(pkCol,fkTable);
						int decision = JOptionPane.showConfirmDialog(pp,
								 "The primary key column " + pkCol.getName() + " already exists\n" +
								 " in the child table, but has an incompatible type.  Continue using new name\n" +
								 newColName + "?",
								 "Column Name Conflict",
								 JOptionPane.YES_NO_OPTION);
						if (decision == JOptionPane.YES_OPTION) {
							fkCol.setName(newColName); 
						} else {
							model = null;
							return;
						}
					}
					
					fkTable.addColumn(fkCol);
					
					if (identifying && fkCol.getPrimaryKeySeq() == null) {
						fkCol.setPrimaryKeySeq(new Integer(fkTable.getPkSize()));
					}
					
					model.addMapping(pkCol, fkCol);
				} finally {
					fkCol.setSecondaryChangeMode(false);
				}
				
			}
			
			Relationship r = new Relationship(pp, model);
			pp.addRelationship(r);
			r.revalidate();
		} catch (ArchitectException ex) {
			
			logger.error("Couldn't create relationship", ex);
			JOptionPane.showMessageDialog(pp, "Couldn't create relationship: "+ex.getMessage());
		} finally {
			fkTable.setSecondaryChangeMode(false);
			pp.fireUndoCompoundEvent(new UndoCompoundEvent(pkTable,EventTypes.RELATIONSHIP_END,"Ending the creation of a relationship"));
		}
	}
	
	/*
	 *  Ideally, loop through until you get a unique column name...
	 */
	private static String generateUniqueColumnName(SQLColumn column, SQLTable table) {		
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
	
		if (!active) return;

		Selectable s = e.getSelectableSource();

		// don't care when tables (or anything else) are deselected
		if (!s.isSelected()) return;

		if (s instanceof TablePane) {
			if (pkTable == null) {
				pkTable = (TablePane) s;
				logger.debug("Creating relationship: PK Table is "+pkTable);
			} else {
				fkTable = (TablePane) s;
				logger.debug("Creating relationship: FK Table is "+fkTable);
				doCreateRelationship(pkTable.getModel(),fkTable.getModel(),pp,identifying);  // this might fail, but still set things back to "normal"
				pp.setCursor(null);
				active = false;
			}
		} else {
			if (logger.isDebugEnabled())
				logger.debug("The user clicked on a non-table component: "+s);
		}
	}

	public void itemDeselected(SelectionEvent e) {
		// don't particularly care
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
