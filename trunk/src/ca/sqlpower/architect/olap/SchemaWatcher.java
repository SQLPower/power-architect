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

package ca.sqlpower.architect.olap;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

/**
 * A listener class that watches a Schema for name changes and removal of
 * Dimensions, CubeDimensions, and Cubes to update the referencing
 * DimensionUsages, VirtualCubeDimensions, and CubeUsages. It has a poor
 * assumption that OLAPObjects have unique names within the Schema.
 * 
 */
public class SchemaWatcher implements SPListener {

    private static final Logger logger = Logger.getLogger(SchemaWatcher.class);

    /**
     * Maps Dimension name to object for public Dimensions. Names must be kept
     * in lower case.
     */
    private final Map<String, Dimension> publicDimensions = new HashMap<String, Dimension>();

    /**
     * Maps CubeDimension "name" to object, see {@link CubeDimensionKey}. Names
     * must be kept in lower case.
     */
    private final Map<CubeDimensionKey, CubeDimension> cubeDimensions = new HashMap<CubeDimensionKey, CubeDimension>();
    
    /**
     * Maps Cube name to object. Names must be kept in lower case.
     */
    private final Map<String, Cube> cubes = new HashMap<String, Cube>();
    
    /**
     * Maps VirtualCube name to object. Names must be kept in lower case.
     */
    private final Map<String, VirtualCube> vCubes = new HashMap<String, VirtualCube>();

    /**
     * Maps Dimension to a list of DimensionUsages that references it.
     */
    private final Map<Dimension, List<DimensionUsage>> dimensionUsageMap = new HashMap<Dimension, List<DimensionUsage>>();
    
    /**
     * Maps Cube to a list of CubeUsages that references it.
     */
    private final Map<Cube, List<CubeUsage>> cubeUsageMap = new HashMap<Cube, List<CubeUsage>>();
    
    /**
     * Maps CubeDimension to a list of VirtualCubeDimensions that references it.
     */
    private final Map<CubeDimension, List<VirtualCubeDimension>> vCubeDimensionMap = new HashMap<CubeDimension, List<VirtualCubeDimension>>();

    public SchemaWatcher(Schema schema) {
        populateNameToObjectMaps(schema);
        populateParentToReferencesMaps(schema);
        
        SQLPowerUtils.listenToHierarchy(schema, this);
    }
    
    /**
     * Finds cubes, public dimensions and cube dimensions in the given parent
     * object and adds them to the appropriate name to object map. See
     * {@link #publicDimensions}, {@link #cubeDimensions}, {@link #cubes}.
     * 
     * @param parent
     *            The parent object to search through.
     */
    private void populateNameToObjectMaps(OLAPObject parent) {
        for (OLAPObject child : parent.getChildren(OLAPObject.class)) {
            if (child instanceof Cube) {
                cubes.put(child.getName().toLowerCase(), (Cube) child);
            } else if (parent instanceof Cube && child instanceof CubeDimension) {
                CubeDimensionKey cubeDimKey = new CubeDimensionKey(parent.getName(), child.getName());
                cubeDimensions.put(cubeDimKey, (CubeDimension) child);
            } else if (child instanceof Dimension) {
                publicDimensions.put(child.getName().toLowerCase(), (Dimension) child);
            } else if (child instanceof VirtualCube) {
                vCubes.put(child.getName().toLowerCase(), (VirtualCube) child);
            }
            
            if (child.allowsChildren()) {
                populateNameToObjectMaps(child);
            }
        }
    }

    /**
     * Finds dimension usages, virtual cube dimensions and cube usages in the
     * given parent object and adds them to the appropriate parent to references
     * map. See {@link #dimensionUsageMap}, {@link #cubeUsageMap},
     * {@link #vCubeDimensionMap}.
     * 
     * @param parent
     *            The parent object to search through.
     */
    private void populateParentToReferencesMaps(OLAPObject parent) {
        for (OLAPObject child : parent.getChildren(OLAPObject.class)) {
            if (child instanceof DimensionUsage) {
                DimensionUsage du = (DimensionUsage) child;
                addToDimensionUsagesMap(du);
            } else if (child instanceof VirtualCubeDimension) {
                VirtualCubeDimension vcd = (VirtualCubeDimension) child;
                addToVCubeDimensionsMap(vcd);
            } else if (child instanceof VirtualCube) {
                VirtualCube vCube = (VirtualCube) child;
                if (vCube.getCubeUsage() != null) {
                    for (CubeUsage cu : vCube.getCubeUsage().getCubeUsages()) {
                        addToCubeUsagesMap(cu);
                    }
                }
            }

            if (child.allowsChildren()) {
                populateParentToReferencesMaps(child);
            }
        }
    }
    
    /**
     * Adds the given DimensionUsage to {@link #dimensionUsageMap}.
     * 
     * @param du The object to add, must not be null.
     */
    private void addToDimensionUsagesMap(DimensionUsage du) {
        Dimension dim = publicDimensions.get(du.getSource().toLowerCase());
        if (dim == null) {
            logger.error("Can't find the Dimension that this DimensionUsage references: " + du);
            throw new IllegalStateException("The schema structure is corrupted, invalid reference by: " + du);
        } else {
            List<DimensionUsage> dimUsages = dimensionUsageMap.get(dim);
            if (dimUsages == null) {
                dimUsages = new ArrayList<DimensionUsage>();
                dimUsages.add(du);
                dimensionUsageMap.put(dim, dimUsages);
            } else {
                dimUsages.add(du);
            }
        }
    }
    
    /**
     * Adds the given VirtualCubeDimension to {@link #vCubeDimensionMap}.
     * 
     * @param vcd The object to add, must not be null.
     */
    private void addToVCubeDimensionsMap(VirtualCubeDimension vcd) {
        CubeDimension cd;
        if (vcd.getCubeName() == null) {
            // the referenced CubeDimension was public.
            cd = publicDimensions.get(vcd.getName().toLowerCase());
        } else {
            // non-public CubeDimension referenced.
            CubeDimensionKey cubeDimKey = new CubeDimensionKey(vcd.getCubeName(), vcd.getName());
            cd = cubeDimensions.get(cubeDimKey);
        }

        if (cd == null) {
            logger.error("Can't find the CubeDimension that this VirtualCubeDimension references: " + vcd);
            throw new IllegalStateException(
                    "The schema structure is corrupted, invalid reference by: " + vcd);
        } else {
            List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(cd);
            if (vCubeDims == null) {
                vCubeDims = new ArrayList<VirtualCubeDimension>();
                vCubeDims.add(vcd);
                vCubeDimensionMap.put(cd, vCubeDims);
            } else {
                vCubeDims.add(vcd);
            }
        }
    }
    
    /**
     * Adds the given CubeUsage to {@link #cubeUsageMap}.
     * 
     * @param cu The object to add, must not be null.
     */
    private void addToCubeUsagesMap(CubeUsage cu) {
        Cube cube = cubes.get(cu.getCubeName().toLowerCase());
        if (cube == null) {
            logger.error("Can't find the Cube that this CubeUsage references: " + cu);
            throw new IllegalStateException("The schema structure is corrupted, invalid reference by: " + cu);
        } else {
            List<CubeUsage> cubeUsages = cubeUsageMap.get(cube);
            if (cubeUsages != null) {
                cubeUsages.add(cu);
            } else {
                cubeUsages = new ArrayList<CubeUsage>();
                cubeUsages.add(cu);
                cubeUsageMap.put(cube, cubeUsages);
            }
        }
    }

    public void childAdded(SPChildEvent e) {
        if (e.getSource() instanceof Cube && e.getChild() instanceof CubeDimension) {
            Cube c = (Cube) e.getSource();
            CubeDimension cd = (CubeDimension) e.getChild();
            cubeDimensions.put(new CubeDimensionKey(c.getName(), cd.getName()), cd);

            if (e.getChild() instanceof DimensionUsage) {
                DimensionUsage du = (DimensionUsage) e.getChild();
                addToDimensionUsagesMap(du);
            }
        } else if (e.getChild() instanceof Dimension) {
            Dimension d = (Dimension) e.getChild();
            publicDimensions.put(d.getName().toLowerCase(), d);
        } else if (e.getChild() instanceof Cube) {
            Cube c = (Cube) e.getChild();
            cubes.put(c.getName().toLowerCase(), c);
            
            // go through the CubeDimensions within the added Cube.
            for (CubeDimension cd : c.getDimensions()) {
                cubeDimensions.put(new CubeDimensionKey(c.getName(), cd.getName()), cd);

                if (e.getChild() instanceof DimensionUsage) {
                    DimensionUsage du = (DimensionUsage) e.getChild();
                    addToDimensionUsagesMap(du);
                }
            }
        } else if (e.getChild() instanceof VirtualCubeDimension) {
            VirtualCubeDimension vcd = (VirtualCubeDimension) e.getChild();
            addToVCubeDimensionsMap(vcd);
        } else if (e.getChild() instanceof CubeUsage) {
            CubeUsage cu = (CubeUsage) e.getChild();
            addToCubeUsagesMap(cu);
        } else if (e.getChild() instanceof VirtualCube) {
            VirtualCube vc = (VirtualCube) e.getChild();
            vCubes.put(vc.getName().toLowerCase(), vc);
            
            CubeUsages cubeUsages = vc.getCubeUsage();
            
            // CubeUsages are a property of VirtualCube instead of a child. This means
            // OLAPUtil.listenToHierarchy() doesn't pick them up. So we have to listen
            // and unlisten to this tree of OLAPObjects separately.
            if (cubeUsages != null) {
                for (CubeUsage cu : cubeUsages.getCubeUsages()) {
                    addToCubeUsagesMap(cu);
                }
                SQLPowerUtils.listenToHierarchy(cubeUsages, this);
            }
            vc.addSPListener(this);
        }

        SQLPowerUtils.listenToHierarchy(e.getChild(), this);
    }

    public void childRemoved(SPChildEvent e) {
        if (e.getSource() instanceof Cube && e.getChild() instanceof CubeDimension) {
            CubeDimension cd = (CubeDimension) e.getChild();
            Cube c = (Cube) e.getSource();
            cubeDimensions.remove(new CubeDimensionKey(c.getName(), cd.getName()));
            
            // remove the VirtualCubeDimensions referencing this CubeDimension.
            List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(cd);
            if (vCubeDims != null) {
                for (VirtualCubeDimension vcd: new ArrayList<VirtualCubeDimension>(vCubeDims)) {
                    VirtualCube vCube = (VirtualCube) vcd.getParent();
                    vCube.removeDimension(vcd);
                }
            }
            vCubeDimensionMap.remove(cd);

            if (e.getChild() instanceof DimensionUsage) {
                DimensionUsage du = (DimensionUsage) e.getChild();
                for (List<DimensionUsage> dimUsages : dimensionUsageMap.values()) {
                    dimUsages.remove(du);
                }
            }
        } else if (e.getChild() instanceof Dimension) {
            Dimension dim = (Dimension) e.getChild();
            publicDimensions.remove(dim.getName().toLowerCase());
            
            // remove the DimensionUsages referencing this Dimension.
            List<DimensionUsage> dimUsages = dimensionUsageMap.get(dim);
            if (dimUsages != null) {
                for (DimensionUsage du : new ArrayList<DimensionUsage>(dimUsages)) {
                    Cube c = (Cube) du.getParent();
                    c.removeDimension(du);
                }
            }
            dimensionUsageMap.remove(dim);
            
            // remove the VirtualCubeDimensions referencing this Dimension.
            List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(dim);
            if (vCubeDims != null) {
                for (VirtualCubeDimension vcd : new ArrayList<VirtualCubeDimension>(vCubeDims)) {
                    VirtualCube vCube = (VirtualCube) vcd.getParent();
                    vCube.removeDimension(vcd);
                }
            }
            vCubeDimensionMap.remove(dim);
        } else if (e.getChild() instanceof Cube) {
            Cube cube = (Cube) e.getChild();
            cubes.remove(cube.getName().toLowerCase());
            
            // remove the CubeUsages referencing this Cube.
            List<CubeUsage> cubeUsages = cubeUsageMap.get(cube);
            if (cubeUsages != null) {
                for (CubeUsage cu : new ArrayList<CubeUsage>(cubeUsages)) {
                    CubeUsages c = (CubeUsages) cu.getParent();
                    c.removeCubeUsage(cu);
                }
            }
            cubeUsageMap.remove(cube);

            // remove the VirtualCubeDimensions referencing CubeDimensions within the Cube.
            for (CubeDimension cd : cube.getDimensions()) {
                List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(cd);
                if (vCubeDims != null) {
                    for (VirtualCubeDimension vcd: new ArrayList<VirtualCubeDimension>(vCubeDims)) {
                        VirtualCube vCube = (VirtualCube) vcd.getParent();
                        vCube.removeDimension(vcd);
                    }
                }
                vCubeDimensionMap.remove(cd);
            }
        } else if (e.getChild() instanceof CubeUsage) {
            CubeUsage cu = (CubeUsage) e.getChild();
            for (List<CubeUsage> cubeUsages : cubeUsageMap.values()) {
                cubeUsages.remove(cu);
            }
        } else if (e.getChild() instanceof VirtualCube) {
            VirtualCube vc = (VirtualCube) e.getChild();
            vCubes.remove(vc.getName().toLowerCase());
            
            // CubeUsages are a property of VirtualCube instead of a child. This means
            // OLAPUtil.listenToHierarchy() doesn't pick them up. So we have to listen
            // and unlisten to this tree of OLAPObjects separately.
            CubeUsages cubeUsages = vc.getCubeUsage();
            if (cubeUsages != null) {
                for (List<CubeUsage> usagesList : cubeUsageMap.values()) {
                    usagesList.removeAll(cubeUsages.getCubeUsages());
                }
                SQLPowerUtils.unlistenToHierarchy(cubeUsages, this);
            }
            vc.removeSPListener(this);
        } else if (e.getChild() instanceof VirtualCubeDimension) {
            for (List<VirtualCubeDimension> vCubeDims : vCubeDimensionMap.values()) {
                vCubeDims.remove(e.getChild());
            }
        }

        SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
    }

    public void propertyChanged(PropertyChangeEvent evt) {
        if ("cubeUsage".equals(evt.getPropertyName())) {
            // This handles {@link VirtualCube#setCubeUsage(CubeUsages)}
            // explicitly because CubeUsages are a property of VirtualCube
            // instead of a child.
            CubeUsages oldUsages = (CubeUsages) evt.getOldValue();
            CubeUsages newUsages = (CubeUsages) evt.getNewValue();

            if (oldUsages != null) {
                for (List<CubeUsage> usagesList : cubeUsageMap.values()) {
                    usagesList.removeAll(oldUsages.getCubeUsages());
                }
                SQLPowerUtils.unlistenToHierarchy(oldUsages, this);
            }
            
            if (newUsages != null) {
                for (CubeUsage cu : newUsages.getCubeUsages()) {
                    addToCubeUsagesMap(cu);
                }
                SQLPowerUtils.listenToHierarchy(newUsages, this);
            }
        } else if ("name".equals(evt.getPropertyName())) {
            if (evt.getSource() instanceof Dimension) {
                Dimension dim = (Dimension) evt.getSource();
                String oldName = (String) evt.getOldValue();
                String newName = (String) evt.getNewValue();

                if (dim.getParent() instanceof Schema) {
                    // update DimensionUsages referencing this public dimension.
                    List<DimensionUsage> dimUsages = dimensionUsageMap.get(dim);
                    if (dimUsages != null) {
                        for (DimensionUsage du : dimUsages) {
                            du.setSource(newName);
                            du.setName(newName);
                        }
                    }
                    
                    publicDimensions.remove(oldName.toLowerCase());
                    publicDimensions.put(newName.toLowerCase(), dim);
                } else if (dim.getParent() instanceof Cube) {
                    String cubeName = dim.getParent().getName();
                    cubeDimensions.remove(new CubeDimensionKey(cubeName, dim.getName()));
                    cubeDimensions.put(new CubeDimensionKey(cubeName, newName), dim);
                } else {
                    logger.warn("Unexpected parent of " + dim + " :" + dim.getParent());
                }
                
                // update VirtualCubeDimensions referencing this dimension (both public and non-public).
                List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(dim);
                if (vCubeDims != null) {
                    for (VirtualCubeDimension vcd : vCubeDims) {
                        vcd.setName((String) evt.getNewValue());
                    }
                }
            } else if (evt.getSource() instanceof Cube) {
                Cube cube = (Cube) evt.getSource();
                String oldName = (String) evt.getOldValue();
                String newName = (String) evt.getNewValue();
                
                // update VirtualCubeDimensions referencing CubeDimensions of this Cube.
                for (CubeDimension cubeDim : cube.getDimensions()) {
                    List<VirtualCubeDimension> vCubeDims = vCubeDimensionMap.get(cubeDim);
                    if (vCubeDims != null) {
                        for (VirtualCubeDimension vcd : vCubeDims) {
                            vcd.setCubeName(newName);
                        }
                    }
                }
                
                // update CubeUsages referencing this Cube.
                List<CubeUsage> cubeUsages = cubeUsageMap.get(cube);
                if (cubeUsages != null) {
                    for (CubeUsage cu : cubeUsages) {
                        cu.setCubeName(newName);
                    }
                }
                
                cubes.remove(oldName.toLowerCase());
                cubes.put(newName.toLowerCase(), cube);
            } else if (evt.getSource() instanceof VirtualCube) {
                VirtualCube vCube = (VirtualCube) evt.getSource();
                String oldName = (String) evt.getOldValue();
                String newName = (String) evt.getNewValue();
                
                vCubes.remove(oldName.toLowerCase());
                vCubes.put(newName.toLowerCase(), vCube);
            }
        }
    }
    
    /**
     * A composite key class that holds the cubeName and name properties in a
     * VirtualCubeDimension. The cubeName property identifies the name of Cube
     * that holds the CubeDimension and the name property identifies the name of
     * the CubeDimenion. Both strings are converted to lower case at
     * constructor. This will form the key used to find the CubeDimension that a
     * VirtualCubeDimension is referencing.
     * 
     */
    private class CubeDimensionKey {
        private final String cubeName;
        private final String dimensionName;

        public CubeDimensionKey(String cubeName, String dimensionName) {
            this.cubeName = cubeName.toLowerCase();
            this.dimensionName = dimensionName.toLowerCase();
        }

        public String getCubeName() {
            return cubeName;
        }

        public String getDimensionName() {
            return dimensionName;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CubeDimensionKey)) {
                return false;
            }

            if (this == obj) {
                return true;
            }

            final CubeDimensionKey other = (CubeDimensionKey) obj;
            if (getCubeName() == null) {
                if (other.getCubeName() != null) {
                    return false;
                }
            } else if (!getCubeName().equals(other.getCubeName())) {
                return false;
            }

            if (getDimensionName() == null) {
                if (other.getDimensionName() != null) {
                    return false;
                }
            } else if (!getDimensionName().equals(other.getDimensionName())) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 0;
            result = PRIME * result + ((getCubeName() == null) ? 0 : getCubeName().hashCode());
            result = PRIME * result + ((getDimensionName() == null) ? 0 : getDimensionName().hashCode());
            return result;
        }

        @Override
        public String toString() {
            return getCubeName() + "." + getDimensionName();
        }
    }
    
    public void transactionEnded(TransactionEvent e) {
        //no-op        
    }
    
    public void transactionRollback(TransactionEvent e) {
        //no-op
    }
    
    public void transactionStarted(TransactionEvent e) {
        //no-op        
    }
}
