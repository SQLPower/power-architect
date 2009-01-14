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

package ca.sqlpower.architect.swingui.dbtree;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.swingui.ComposedIcon;

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
    
    private static final Logger logger = Logger.getLogger(DBTreeCellRenderer.class);
    
    public static final ImageIcon DB_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Database16.png"));
	public static final ImageIcon TARGET_DB_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Database_target16.png"));
	public static final ImageIcon CATALOG_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Catalog16.png"));
	public static final ImageIcon SCHEMA_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Schema16.png"));
	public static final ImageIcon TABLE_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Table16.png"));
	public static final ImageIcon EXPORTED_KEY_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/ExportedKey16.png"));
    public static final ImageIcon IMPORTED_KEY_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/ImportedKey16.png"));
	public static final ImageIcon OWNER_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Owner16.png"));
    public static final ImageIcon INDEX_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Index16.png"));
    public static final ImageIcon PK_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Index_key16.png"));
    public static final ImageIcon UNIQUE_INDEX_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Index_unique16.png"));
    public static final ImageIcon COLUMN_ICON = new ImageIcon(DBTreeCellRenderer.class.getResource("icons/Column16.png"));
    public static final ImageIcon ERROR_BADGE = new ImageIcon(DBTreeCellRenderer.class.getResource("/icons/parts/noAccess.png"));
   
    private final List<IconFilter> iconFilterChain = new ArrayList<IconFilter>();
    
	public DBTreeCellRenderer() {
        super();
    }

    public Component getTreeCellRendererComponent(JTree tree,
												  Object value,
												  boolean sel,
												  boolean expanded,
												  boolean leaf,
												  int row,
												  boolean hasFocus) {
		setText(value.toString());
	    setToolTipText(getText());
	    
        if (value instanceof SQLDatabase) {
			SQLDatabase db = (SQLDatabase) value;
			if (db.isPlayPenDatabase()) {
				setIcon(TARGET_DB_ICON);
			} else {
				setIcon(DB_ICON);
			}
		} else if (value instanceof SQLCatalog) {
			if (((SQLCatalog) value).getNativeTerm().equals("owner")) { //$NON-NLS-1$
				setIcon(OWNER_ICON);
			} else if (((SQLCatalog) value).getNativeTerm().equals("database")) { //$NON-NLS-1$
				setIcon(DB_ICON);
			} else if (((SQLCatalog) value).getNativeTerm().equals("schema")) { //$NON-NLS-1$
				setIcon(SCHEMA_ICON);
			} else {
				setIcon(CATALOG_ICON);
			}
		} else if (value instanceof SQLSchema) {
			if (((SQLSchema) value).getNativeTerm().equals("owner")) { //$NON-NLS-1$
				setIcon(OWNER_ICON);
			} else {
				setIcon(SCHEMA_ICON);
			}
		} else if (value instanceof SQLTable) {
		    setIcon(TABLE_ICON);
			
		    SQLTable table = (SQLTable) value;
            if ((table).getObjectType() != null) {
			    setText((table).getName()+" ("+(table).getObjectType()+")"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
			    setText((table).getName());
			}
		} else if (value instanceof SQLRelationship) {
            //XXX ARRRRRRGGGGGHHHHHHH!!!! No way of knowing which end of a relationship we're
            // looking at because the relationship has two parents.  Maybe able to do it with the row number.
            if (true) {
                setIcon(EXPORTED_KEY_ICON);
            } else {
                setIcon(IMPORTED_KEY_ICON);
            }
		} else if (value instanceof SQLIndex) {
            SQLIndex i = (SQLIndex) value;
            if (i.isPrimaryKeyIndex()) {
                setIcon(PK_ICON);
            } else if (i.isUnique()) {
                setIcon(UNIQUE_INDEX_ICON);
            } else {
                setIcon(INDEX_ICON);
            }
        } else if (value instanceof SQLColumn) {
            tagColumn((SQLColumn)value);
            setIcon(COLUMN_ICON);
        } else if (value instanceof Column) {
            Column col = (Column) value;
            logger.debug("Column has properties " + col);
            if (col.getColumn() != null) {
                tagColumn((col).getColumn());
            }
            setIcon(COLUMN_ICON);
        } else {
			setIcon(null);
		}
        
        if (value instanceof SQLObject && ((SQLObject) value).getChildrenInaccessibleReason() != null) {
            logger.debug("Children are not accessible from the node " + ((SQLObject) value).getName());
            if (getIcon() == null) {
                setIcon(ERROR_BADGE);
            } else {
                setIcon(new ComposedIcon(Arrays.asList(getIcon(), ERROR_BADGE)));
            }
            setToolTipText("Inaccessible: " + ((SQLObject) value).getChildrenInaccessibleReason());
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
	    
	    if (value instanceof SQLObject || value == null) {
	        for (IconFilter filter : getIconFilterChain()) {
	            setIcon(filter.filterIcon(getIcon(), (SQLObject) value));
	        }
	    }
	    
		return this;
	}
    
     /**
     * Determines what tag to append to the given column
     */
    private void tagColumn(SQLColumn col) {
        StringBuffer tag = new StringBuffer();
        StringBuffer fullTag = new StringBuffer();
        boolean isPK = col.isPrimaryKey();
        boolean isFK = col.isForeignKey();
        boolean isAK = col.isUniqueIndexed() && !isPK;
        boolean emptyTag = true;
        if (isPK) {
            tag.append("P"); //$NON-NLS-1$
            emptyTag = false;
        } 
        if (isFK) {
            tag.append("F"); //$NON-NLS-1$
            emptyTag = false;
        }
        if (isAK) {
            tag.append("A"); //$NON-NLS-1$
            emptyTag = false;
        }
        
        if (!emptyTag) {
            tag.append("K"); //$NON-NLS-1$
            fullTag.append("  [ "); //$NON-NLS-1$
            fullTag.append(tag);
            fullTag.append(" ]"); //$NON-NLS-1$
            setText(getText() + fullTag.toString());
        }
    }

    /**
     * Adds the given icon filter to the end of the filter chain. The filter
     * will be invoked after all currently existing filters on this renderer.
     * 
     * @param filter
     *            The filter to add. Must not be null.
     */
    public void addIconFilter(IconFilter filter) {
        if (filter == null) {
            throw new NullPointerException("Null icon filters not allowed");
        }
        iconFilterChain.add(filter);
    }

    /**
     * Removed the given icon filter chain from this renderer's filter chain. If
     * the given filter is not in the list, calling this method has no effect.
     * 
     * @param filter
     *            The filter to remove
     * @return True if the filter was in the list (so it has been removed);
     *         false if the filter was not in the list (so the list remains
     *         unchanged).
     */
    public boolean removeIconFilter(IconFilter filter) {
        return iconFilterChain.remove(filter);
    }
    
    /**
     * Returns a read-only view of this renderer's filter chain. The filters
     * are invoked in the order that they exist in this list.
     */
    public List<IconFilter> getIconFilterChain() {
        return Collections.unmodifiableList(iconFilterChain);
    }
}