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

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.swingui.SPSUtils;

public class OLAPTreeCellRenderer extends DefaultTreeCellRenderer {
    public static final ImageIcon schemaIcon = SPSUtils.createIcon("Schema", "SQL Schema", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon tableIcon = SPSUtils.createIcon("Table", "SQL Table", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon columnIcon = SPSUtils.createIcon("Column", "Column", ArchitectSwingSessionContext.ICON_SIZE); //$NON-NLS-1$ //$NON-NLS-2$
    
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
        setText(OLAPUtil.nameFor((OLAPObject) value));
        if (value instanceof Schema) {
            setIcon(schemaIcon);
        } else if (value instanceof Cube) {
            setIcon(tableIcon);
        } else if (value instanceof VirtualCube) {
            setIcon(tableIcon);
        } else if (value instanceof CubeDimension) {
            setIcon(columnIcon);
        } else if (value instanceof Measure) {
            setIcon(columnIcon);
        } else if (value instanceof VirtualCubeMeasure) {
            setIcon(columnIcon);
        } else if (value instanceof Hierarchy) {
            setIcon(columnIcon);
        } else {
            setIcon(null);
        }

        this.selected = sel;
        this.hasFocus = hasFocus;

        return this;
    }
}
