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
 * A collection of utility methods for the OLAP Swing UI. Also the place
 * where we keep all the icons.
 */
public class OSUtils {

    public static final ImageIcon CUBE_ICON = SPSUtils.createIcon("olap/cube", "Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_ADD_ICON = SPSUtils.createIcon("olap/cubeAdd", "New Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_USAGE_ICON = SPSUtils.createIcon("olap/cubeUsage", "Cube Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_USAGE_ADD_ICON = SPSUtils.createIcon("olap/cubeUsageAdd", "New Cube Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_ICON = SPSUtils.createIcon("olap/dimension", "Dimension"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_ADD_ICON = SPSUtils.createIcon("olap/dimensionAdd", "New Dimension"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon FORMULA_ICON = SPSUtils.createIcon("olap/formula", "Formula"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon FORMULA_ADD_ICON = SPSUtils.createIcon("olap/formulaAdd", "New Formula"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon HIERARCHY_ICON = SPSUtils.createIcon("olap/hierarchy", "Hierarchy"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon HIERARCHY_ADD_ICON = SPSUtils.createIcon("olap/hierarchyAdd", "New Hierarchy"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_ICON = SPSUtils.createIcon("olap/level", "Level"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_ADD_ICON = SPSUtils.createIcon("olap/levelAdd", "New Level"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_PROP_ICON = SPSUtils.createIcon("olap/levelProperty", "Level Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_PROP_ADD_ICON = SPSUtils.createIcon("olap/levelPropertyAdd", "New Level Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_ICON = SPSUtils.createIcon("olap/measure", "Measure"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_ADD_ICON = SPSUtils.createIcon("olap/measureAdd", "New Measure"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_PROP_ICON = SPSUtils.createIcon("olap/measureProperty", "Measure Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_PROP_ADD_ICON = SPSUtils.createIcon("olap/measurePropertyAdd", "New Measure Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon NAMED_SET_ICON = SPSUtils.createIcon("olap/namedSet", "Named Set"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon NAMED_SET_ADD_ICON = SPSUtils.createIcon("olap/namedSetAdd", "New Named Set"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SCHEMA_ICON = SPSUtils.createIcon("olap/schema", "Schema"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SCHEMA_ADD_ICON = SPSUtils.createIcon("olap/schemaAdd", "New Schema"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_GRANT_ICON = SPSUtils.createIcon("olap/securityGrant", "Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_GRANT_ADD_ICON = SPSUtils.createIcon("olap/securityGrantAdd", "New Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_CUBE_GRANT_ICON = SPSUtils.createIcon("olap/cubeGrant", "Cube Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_CUBE_GRANT_ADD_ICON = SPSUtils.createIcon("olap/cubeGrantAdd", "New Cube Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_HIERARCHY_GRANT_ICON = SPSUtils.createIcon("olap/hierarchyGrant", "Hierarchy Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_HIERARCHY_GRANT_ADD_ICON = SPSUtils.createIcon("olap/hierarchyGrantAdd", "New Hierarchy Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_SCHEMA_GRANT_ICON = SPSUtils.createIcon("olap/schemaGrant", "Schema Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_SCHEMA_GRANT_ADD_ICON = SPSUtils.createIcon("olap/schemaGrantAdd", "New Schema Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_ROLE_ICON = SPSUtils.createIcon("olap/securityRole", "Role"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_ROLE_ADD_ICON = SPSUtils.createIcon("olap/securityRoleAdd", "New Role"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_UNION_ICON = SPSUtils.createIcon("olap/securityUnion", "Union"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_UNION_ADD_ICON = SPSUtils.createIcon("olap/securityUnionAdd", "New Union"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon VIRTUAL_CUBE_ICON = SPSUtils.createIcon("olap/virtualCube", "Virtual Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon VIRTUAL_CUBE_ADD_ICON = SPSUtils.createIcon("olap/virtualCubeAdd", "New Virtual Cube"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final ImageIcon SCHEMA_EXPORT_ICON = SPSUtils.createIcon("olap/schemaExport", "Export Schema"); //$NON-NLS-1$ //$NON-NLS-2$

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
        return iconFor(value, false);
    }

    /**
     * Returns the appropriate icon for the given OLAP object.
     * 
     * @param value
     *            The object you want an icon for
     * @param addVariant
     *            true for the variant of the icon with an "add" badge, false
     *            for the undecorated icon.
     * @return The most appropriate icon for the given object. The icon size
     *         will be 16x16, and it will have an alpha mask. If no appropriate
     *         icon is available, a completely transparent 16x16 icon will be
     *         returned.
     */
    public static Icon iconFor(OLAPObject value, boolean addVariant) {
        if (false) {
        } else if (value instanceof CalculatedMember) {
            return addVariant ? FORMULA_ADD_ICON : FORMULA_ICON;
        } else if (value instanceof Cube) {
            return addVariant ? CUBE_ADD_ICON : CUBE_ICON;
        } else if (value instanceof CubeUsage) {
            return addVariant ? CUBE_USAGE_ADD_ICON : CUBE_USAGE_ICON;
        } else if (value instanceof CubeDimension) {
            return addVariant ? DIMENSION_ADD_ICON : DIMENSION_ICON;
        } else if (value instanceof Formula) {
            return addVariant ? FORMULA_ADD_ICON : FORMULA_ICON;
        } else if (value instanceof Hierarchy) {
            return addVariant ? HIERARCHY_ADD_ICON : HIERARCHY_ICON;
        } else if (value instanceof Level) {
            return addVariant ? LEVEL_ADD_ICON : LEVEL_ICON;
        } else if (value instanceof Property && value.getParent() instanceof Level) {
            return addVariant ? LEVEL_PROP_ADD_ICON : LEVEL_PROP_ICON;
        } else if (value instanceof Measure) {
            return addVariant ? MEASURE_ADD_ICON : MEASURE_ICON;
        } else if (value instanceof Property && value.getParent() instanceof Measure) {
            return addVariant ? MEASURE_PROP_ADD_ICON : MEASURE_PROP_ICON;
        } else if (value instanceof MemberGrant) {
            return addVariant ? SEC_GRANT_ADD_ICON : SEC_GRANT_ICON;
        } else if (value instanceof NamedSet) {
            return addVariant ? NAMED_SET_ADD_ICON : NAMED_SET_ICON;
        } else if (value instanceof Schema) {
            return addVariant ? SCHEMA_ADD_ICON : SCHEMA_ICON;
        } else if (value instanceof CubeGrant) {
            return addVariant ? SEC_CUBE_GRANT_ADD_ICON : SEC_CUBE_GRANT_ICON;
//      } else if (value instanceof DimensionGrant) {  TODO get this icon
//          return addVariant ? SEC_DIMENSION_GRANT_ADD_ICON : SEC_DIMENSION_GRANT_ICON;
        } else if (value instanceof HierarchyGrant) {
            return addVariant ? SEC_HIERARCHY_GRANT_ADD_ICON : SEC_HIERARCHY_GRANT_ICON;
        } else if (value instanceof SchemaGrant) {
            return addVariant ? SEC_SCHEMA_GRANT_ADD_ICON : SEC_SCHEMA_GRANT_ICON;
        } else if (value instanceof Grant) {
            return addVariant ? SEC_GRANT_ADD_ICON : SEC_GRANT_ICON;
        } else if (value instanceof Role) {
            return addVariant ? SEC_ROLE_ADD_ICON : SEC_ROLE_ICON;
        } else if (value instanceof Union) {
            return addVariant ? SEC_UNION_ADD_ICON : SEC_UNION_ICON;
        } else if (value instanceof VirtualCube) {
            return addVariant ? VIRTUAL_CUBE_ADD_ICON : VIRTUAL_CUBE_ICON;
        } else if (value instanceof VirtualCubeMeasure) {
            return addVariant ? MEASURE_ADD_ICON : MEASURE_ICON;
        }

        return BlankIcon.getInstance(16, 16);
    }
}
