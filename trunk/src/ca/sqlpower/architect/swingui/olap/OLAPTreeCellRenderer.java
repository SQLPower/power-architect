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

package ca.sqlpower.architect.swingui.olap;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.swingui.SPSUtils;

public class OLAPTreeCellRenderer extends DefaultTreeCellRenderer {
    public static final ImageIcon dbIcon = SPSUtils.createIcon("Database", "SQL Database", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon dbProfiledIcon = SPSUtils.createIcon("Database_profiled", "SQL Database", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon targetIcon = SPSUtils.createIcon("Database_target", "SQL Database", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon cataIcon = SPSUtils.createIcon("Catalog", "SQL Catalog", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon schemaIcon = SPSUtils.createIcon("Schema", "SQL Schema", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon tableIcon = SPSUtils.createIcon("Table", "SQL Table", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon tableProfiledIcon = SPSUtils.createIcon("Table_profiled", "SQL Table", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon exportedKeyIcon = SPSUtils.createIcon("ExportedKey", "Exported key", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon importedKeyIcon = SPSUtils.createIcon("ImportedKey", "Imported key", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon ownerIcon = SPSUtils.createIcon("Owner", "Owner", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon indexIcon = SPSUtils.createIcon("Index", "Index", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon pkIndexIcon = SPSUtils.createIcon("Index_key", "Primary Key Index", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon uniqueIndexIcon = SPSUtils.createIcon("Index_unique", "Unique Index", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon columnIcon = SPSUtils.createIcon("Column", "Column", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
//    private final ArchitectSession session;
   

    public OLAPTreeCellRenderer(ArchitectSession session) {
        super();
//        this.session = session;
    }
    
    public OLAPTreeCellRenderer(){
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
        if (value instanceof Schema) {
            Schema schema = (Schema) value;
            setText(schema.getName() + " (Schema)");
            setIcon(schemaIcon);
        } else if (value instanceof Cube) {
            Cube cube = (Cube) value;
            setText(cube.getName() + " (Cube)");
            setIcon(tableIcon);
        } else if (value instanceof VirtualCube) {
            VirtualCube vCube = (VirtualCube) value;
            setText(vCube.getName() + " (VirtualCube)");
            setIcon(tableIcon);
        } else if (value instanceof CubeDimension) {
            CubeDimension dim = (CubeDimension) value;
            setText(dim.getName() + " (Dimension)");
            setIcon(columnIcon);
        } else if (value instanceof Measure) {
            Measure measure = (Measure) value;
            setText(measure.getName() + " (Measure)");
            setIcon(columnIcon);
        } else if (value instanceof VirtualCubeMeasure) {
            VirtualCubeMeasure measure = (VirtualCubeMeasure) value;
            setText(measure.getName() + " (VirtualCubeMeasure)");
            setIcon(columnIcon);
        } else {
            setIcon(null);
        }

        this.selected = sel;
        this.hasFocus = hasFocus;

        return this;
    }
}
