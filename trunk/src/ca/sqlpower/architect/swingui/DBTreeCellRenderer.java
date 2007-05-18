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

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.profile.TableProfileManager;

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
    public static final ImageIcon dbProfiledIcon = ASUtils.createIcon("Database_profiled", "SQL Database", 16);
	public static final ImageIcon targetIcon = ASUtils.createIcon("Database_target", "SQL Database", 16);
	public static final ImageIcon cataIcon = ASUtils.createIcon("Catalog", "SQL Catalog", 16);
	public static final ImageIcon schemaIcon = ASUtils.createIcon("Schema", "SQL Schema", 16);
	public static final ImageIcon tableIcon = ASUtils.createIcon("Table", "SQL Table", 16);
    public static final ImageIcon tableProfiledIcon = ASUtils.createIcon("Table_profiled", "SQL Table", 16);
	public static final ImageIcon exportedKeyIcon = ASUtils.createIcon("ExportedKey", "Exported key", 16);
    public static final ImageIcon importedKeyIcon = ASUtils.createIcon("ImportedKey", "Imported key", 16);
	public static final ImageIcon ownerIcon = ASUtils.createIcon("Owner", "Owner", 16);
    public static final ImageIcon indexIcon = ASUtils.createIcon("Index", "Index", 16);
    public static final ImageIcon pkIndexIcon = ASUtils.createIcon("Index_key", "Primary Key Index", 16);
    public static final ImageIcon uniqueIndexIcon = ASUtils.createIcon("Index_unique", "Unique Index", 16);
    public static final ImageIcon columnIcon = ASUtils.createIcon("Column", "Column", 16);
    private final ArchitectSession session;
   
    
	public DBTreeCellRenderer(ArchitectSession session) {
        super();
        this.session = session;
    }


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
                setText("Project");
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
            
			SQLTable table = (SQLTable) value;
            if (((TableProfileManager)session.getProfileManager()).isTableProfiled(table)) {
                setIcon(tableProfiledIcon);
            } else {
                setIcon(tableIcon);
            }
            if ((table).getObjectType() != null) {
			    setText((table).getName()+" ("+(table).getObjectType()+")");
			} else {
			    setText((table).getName());
			}
		} else if (value instanceof SQLRelationship) {
            //XXX ARRRRRRGGGGGHHHHHHH!!!! No way of knowing which end of a relationship we're
            // looking at because the relationship has two parents.  Maybe able to do it with the row number.
            if (true) {
                setIcon(exportedKeyIcon);
            } else {
                setIcon(importedKeyIcon);
            }
		} else if (value instanceof SQLIndex) {
            SQLIndex i = (SQLIndex) value;
            if (i.isPrimaryKeyIndex()) {
                setIcon(pkIndexIcon);
            } else if (i.isUnique()) {
                setIcon(uniqueIndexIcon);
            } else {
                setIcon(indexIcon);
            }
        } else if (value instanceof SQLColumn || value instanceof Column) {
            setIcon(columnIcon);
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