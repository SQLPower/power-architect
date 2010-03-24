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

import java.net.URL;

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

/**
 * A collection of utility methods for the OLAP Swing UI. Also the place
 * where we keep all the icons.
 */
public class OSUtils {

    public static final ImageIcon CUBE_ICON = createIcon("cube", "Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_ADD_ICON = createIcon("cubeAdd", "New Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_USAGE_ICON = createIcon("cubeUsage", "Cube Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon CUBE_USAGE_ADD_ICON = createIcon("cubeUsageAdd", "New Cube Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_ICON = createIcon("dimension", "Dimension"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_ADD_ICON = createIcon("dimensionAdd", "New Dimension"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_USAGE_ICON = createIcon("dimensionUsage", "Dimension Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon DIMENSION_USAGE_ADD_ICON = createIcon("dimensionUsageAdd", "New Dimension Usage"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon FORMULA_ICON = createIcon("formula", "Formula"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon FORMULA_ADD_ICON = createIcon("formulaAdd", "New Formula"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon HIERARCHY_ICON = createIcon("hierarchy", "Hierarchy"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon HIERARCHY_ADD_ICON = createIcon("hierarchyAdd", "New Hierarchy"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_ICON = createIcon("level", "Level"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_ADD_ICON = createIcon("levelAdd", "New Level"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_PROP_ICON = createIcon("levelProperty", "Level Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon LEVEL_PROP_ADD_ICON = createIcon("levelPropertyAdd", "New Level Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_ICON = createIcon("measure", "Measure"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_ADD_ICON = createIcon("measureAdd", "New Measure"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_PROP_ICON = createIcon("measureProperty", "Measure Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon MEASURE_PROP_ADD_ICON = createIcon("measurePropertyAdd", "New Measure Property"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon NAMED_SET_ICON = createIcon("namedSet", "Named Set"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon NAMED_SET_ADD_ICON = createIcon("namedSetAdd", "New Named Set"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SCHEMA_ICON = createIcon("schema", "Schema"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SCHEMA_ADD_ICON = createIcon("schemaAdd", "New Schema"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_GRANT_ICON = createIcon("securityGrant", "Grant"); //$NON-NLS-1$ //$NON-NLS-2$
//    public static final ImageIcon SEC_GRANT_ADD_ICON = createIcon("securityGrantAdd", "New Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_CUBE_GRANT_ICON = createIcon("cubeGrant", "Cube Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_CUBE_GRANT_ADD_ICON = createIcon("cubeGrantAdd", "New Cube Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_HIERARCHY_GRANT_ICON = createIcon("hierarchyGrant", "Hierarchy Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_HIERARCHY_GRANT_ADD_ICON = createIcon("hierarchyGrantAdd", "New Hierarchy Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_SCHEMA_GRANT_ICON = createIcon("schemaGrant", "Schema Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_SCHEMA_GRANT_ADD_ICON = createIcon("schemaGrantAdd", "New Schema Grant"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_ROLE_ICON = createIcon("securityRole", "Role"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_ROLE_ADD_ICON = createIcon("securityRoleAdd", "New Role"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_UNION_ICON = createIcon("securityUnion", "Union"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon SEC_UNION_ADD_ICON = createIcon("securityUnionAdd", "New Union"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon VIRTUAL_CUBE_ICON = createIcon("virtualCube", "Virtual Cube"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final ImageIcon VIRTUAL_CUBE_ADD_ICON = createIcon("virtualCubeAdd", "New Virtual Cube"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final ImageIcon SCHEMA_EXPORT_ICON = createIcon("schemaExport", "Export Schema"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ImageIcon createIcon(String resourceName, String description) {
        URL iconUrl = OSUtils.class.getResource(
                "/ca/sqlpower/swingui/olap/" + resourceName + ".png");
        if (iconUrl == null) {
            throw new RuntimeException("Missing icon " + resourceName);
        } else {
            return new ImageIcon(iconUrl, description);
        }
    }
    
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
            if (addVariant) {
                throw new UnsupportedOperationException("Don't have icon for security grant add");
            }
            return SEC_GRANT_ICON;
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
            if (addVariant) {
                throw new UnsupportedOperationException("Don't have icon for security grant add");
            }
            return SEC_GRANT_ICON;
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
