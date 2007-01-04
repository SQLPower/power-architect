/*
 * Created on Nov 28, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;

/**
 * The DBTreeCellRenderer renders nodes of a JTree which are of
 * type SQLObject.  This class is much older than November 2006; it
 * was pulled out of the DBTree.java compilation unit into its own
 * file on this date so it could be used more naturally as the cell
 * renderer for a different JTree.
 *
 * @author fuerth
 * @version $Id$
 */
public class DBTreeCellRenderer extends DefaultTreeCellRenderer {
	public static final ImageIcon dbIcon = ASUtils.createIcon("Database", "SQL Database", 16);
	public static final ImageIcon targetIcon = ASUtils.createIcon("TargetDatabaseArrow", "SQL Database", 16);
	public static final ImageIcon cataIcon = ASUtils.createIcon("Catalog", "SQL Catalog", 16);
	public static final ImageIcon schemaIcon = ASUtils.createIcon("Schema", "SQL Schema", 16);
	public static final ImageIcon tableIcon = ASUtils.createIcon("Table", "SQL Table", 16);
	public static final ImageIcon keyIcon = ASUtils.createIcon("ExportedKey", "Exported key", 16);
	public static final ImageIcon ownerIcon = ASUtils.createIcon("Owner", "Owner", 16);

	public Component getTreeCellRendererComponent(JTree tree,
												  Object value,
												  boolean sel,
												  boolean expanded,
												  boolean leaf,
												  int row,
												  boolean hasFocus) {
		setText(value.toString());
		if (value instanceof SQLDatabase) {
			SQLDatabase db = (SQLDatabase) value;
			if (db.isPlayPenDatabase()) {
				setIcon(targetIcon);
				if (db.getName() == null || db.getName().length() == 0) {
					setText("Project");
				} else {
					setText("Project ("+db.getName()+")");
				}
			} else {
				setIcon(dbIcon);
			}
		} else if (value instanceof SQLCatalog) {
			if (((SQLCatalog) value).getNativeTerm().equals("owner")) {
				setIcon(ownerIcon);
			} else if (((SQLCatalog) value).getNativeTerm().equals("database")) {
				setIcon(dbIcon);
			} else if (((SQLCatalog) value).getNativeTerm().equals("schema")) {
				setIcon(schemaIcon);
			} else {
				setIcon(cataIcon);
			}
		} else if (value instanceof SQLSchema) {
			if (((SQLSchema) value).getNativeTerm().equals("owner")) {
				setIcon(ownerIcon);
			} else {
				setIcon(schemaIcon);
			}
		} else if (value instanceof SQLTable) {
			setIcon(tableIcon);
			if (((SQLTable) value).getObjectType() != null) {
			    setText(((SQLTable) value).getName()+" ("+((SQLTable) value).getObjectType()+")");
			} else {
			    setText(((SQLTable) value).getName());
			}
		} else if (value instanceof SQLRelationship) {
			setIcon(keyIcon);
		} else {
			setIcon(null);
		}

		this.selected = sel;
		this.hasFocus = hasFocus;

		if (value instanceof SQLObject) {
		    if (((SQLObject) value).isPopulated()) {
		        setForeground(Color.black);
		    } else {
		        setForeground(Color.lightGray);
		    }
		}
		return this;
	}
}