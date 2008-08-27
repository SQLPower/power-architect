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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.CalculatedMember;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeGrant;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.Formula;
import ca.sqlpower.architect.olap.MondrianModel.Grant;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.HierarchyGrant;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.MemberGrant;
import ca.sqlpower.architect.olap.MondrianModel.NamedSet;
import ca.sqlpower.architect.olap.MondrianModel.Property;
import ca.sqlpower.architect.olap.MondrianModel.Role;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.SchemaGrant;
import ca.sqlpower.architect.olap.MondrianModel.Union;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.swingui.BlankIcon;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A collection of utility methods for the OLAP Swing UI.
 */
public class OSUtils {

    public static final ImageIcon CUBE_ICON = SPSUtils.createIcon("olap/cube", "Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_USAGE_ICON = SPSUtils.createIcon("olap/cubeUsage", "Cube Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_ICON = SPSUtils.createIcon("olap/dimension", "Dimension"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon FORMULA_ICON = SPSUtils.createIcon("olap/formula", "Formula"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon HIERARCHY_ICON = SPSUtils.createIcon("olap/hierarchy", "Hierarchy"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_ICON = SPSUtils.createIcon("olap/level", "Level"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_PROP_ICON = SPSUtils.createIcon("olap/levelProperty", "Level Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_ICON = SPSUtils.createIcon("olap/measure", "Measure"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_PROP_ICON = SPSUtils.createIcon("olap/measureProperty", "Measure Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon NAMED_SET_ICON = SPSUtils.createIcon("olap/namedSet", "Named Set"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SCHEMA_ICON = SPSUtils.createIcon("olap/schema", "Schema"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_GRANT_ICON = SPSUtils.createIcon("olap/securityGrant", "Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_CUBE_GRANT_ICON = SPSUtils.createIcon("olap/cubeGrant", "Cube Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_HIERARCHY_GRANT_ICON = SPSUtils.createIcon("olap/hierarchyGrant", "Hierarchy Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_SCHEMA_GRANT_ICON = SPSUtils.createIcon("olap/schemaGrant", "Schema Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_ROLE_ICON = SPSUtils.createIcon("olap/securityRole", "Role"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_UNION_ICON = SPSUtils.createIcon("olap/securityUnion", "Union"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon VIRTUAL_CUBE_ICON = SPSUtils.createIcon("olap/virtualCube", "Virtual Cube"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Returns the appropriate icon for the given OLAP object.
     * 
     * @param value
     *            The object you want an icon for
     * @return The most appropriate icon for the given object. The icon size
     *         will be 16x16, and it will have an alpha mask.  If no appropriate
     *         icon is available, a completely transparent 16x16 icon will be
     *         returned.
     */
    public static Icon iconFor(OLAPObject value) {
        if (false) {
        } else if (value instanceof CalculatedMember) {
            return FORMULA_ICON;
        } else if (value instanceof Cube) {
            return CUBE_ICON;
        } else if (value instanceof CubeUsage) {
            return CUBE_USAGE_ICON;
        } else if (value instanceof CubeDimension) {
            return DIMENSION_ICON;
        } else if (value instanceof Formula) {
            return FORMULA_ICON;
        } else if (value instanceof Hierarchy) {
            return HIERARCHY_ICON;
        } else if (value instanceof Level) {
            return LEVEL_ICON;
        } else if (value instanceof Property && value.getParent() instanceof Level) {
            return LEVEL_PROP_ICON;
        } else if (value instanceof Measure) {
            return MEASURE_ICON;
        } else if (value instanceof Property && value.getParent() instanceof Measure) {
            return MEASURE_PROP_ICON;
        } else if (value instanceof MemberGrant) {
            return SEC_GRANT_ICON;
        } else if (value instanceof NamedSet) {
            return NAMED_SET_ICON;
        } else if (value instanceof Schema) {
            return SCHEMA_ICON;
        } else if (value instanceof CubeGrant) {
            return SEC_CUBE_GRANT_ICON;
//      } else if (value instanceof DimensionGrant) {  TODO get this icon
//          return SEC_DIMENSION_GRANT_ICON;
        } else if (value instanceof HierarchyGrant) {
            return SEC_HIERARCHY_GRANT_ICON;
        } else if (value instanceof SchemaGrant) {
            return SEC_SCHEMA_GRANT_ICON;
        } else if (value instanceof Grant) {
            return SEC_GRANT_ICON;
        } else if (value instanceof Role) {
            return SEC_ROLE_ICON;
        } else if (value instanceof Union) {
            return SEC_UNION_ICON;
        } else if (value instanceof VirtualCube) {
            return VIRTUAL_CUBE_ICON;
        } else if (value instanceof VirtualCubeMeasure) {
            return MEASURE_ICON;
        }

        return BlankIcon.getInstance(16, 16);
    }
}
