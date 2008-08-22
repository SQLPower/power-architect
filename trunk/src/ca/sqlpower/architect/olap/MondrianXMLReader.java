
package ca.sqlpower.architect.olap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;

/**
 * This is class is generated from xml-to-parser.xsl!  Do not alter it directly.
 */
public class MondrianXMLReader {

    private static final Logger logger = Logger.getLogger(MondrianXMLReader.class);

    /**
     * Imports an OLAP schema from a Mondrian schema xml file.
     * 
     * @param f
     *            The file to read from.
     * @return The Schema that will be populated with the objects from the file.
     * @throws IOException
     *             If the file could not be read.
     * @throws SAXException
     *             If the xml in the file is malformed.
     */
    public static OLAPObject importXML(File f) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler();
        reader.setContentHandler(handler);
        InputSource is = new InputSource(new FileInputStream(f));
        reader.parse(is);
        hookupListeners(handler.root);        
        return handler.root;
    }

    /**
     * Reads in the OLAPObjects from an Architect file. This is essentially the
     * same as calling {@link #parse(InputStream, OLAPRootObject, Map, Map)}.
     * 
     * @param f
     *            The file to load from.
     * @param rootObj
     *            The OLAPRootObject that will be populated with all the
     *            OLAPObjects from the file, must not be null.
     * @param dbIdMap
     *            A map containing references to SQLDatabases from the architect
     *            project, must not be null.
     * @param olapIdMap
     *            A map that will be populated with the OLAPObjects from the
     *            file and their generated ids, must not be null.
     * @return The OLAPRootObject that will be populated with the objects from
     *         the file.
     * @throws IOException
     *             If the file could not be read.
     * @throws SAXException
     *             If the xml in the file is malformed.
     */
    public static OLAPObject parse(File f, OLAPRootObject rootObj,
            Map<String, SQLObject> dbIdMap, Map<String, OLAPObject> olapIdMap) throws IOException, SAXException {
        return parse(new FileInputStream(f), rootObj, dbIdMap, olapIdMap);
    }

    /**
     * Reads in OLAPObjects from an InputStream in the Architect OLAP format.
     * 
     * @param in
     *            The InputStream to read from, must support mark.
     * @param rootObj
     *            The OLAPRootObject that will be populated with all the
     *            OLAPObjects from the file, must not be null.
     * @param dbIdMap
     *            A map containing references to SQLDatabases from the architect
     *            project, must not be null.
     * @param olapIdMap
     *            A map that will be populated with the OLAPObjects from the
     *            file and their generated ids, must not be null.
     * @return The OLAPRootObject that will be populated with the objects from
     *         the file.
     * @throws IOException
     *             If the input stream could not be read.
     * @throws SAXException
     *             If the xml in the input stream is malformed.
     */
    public static OLAPObject parse(InputStream in, OLAPRootObject rootObj, Map<String, SQLObject> dbIdMap,
            Map<String, OLAPObject> olapIdMap) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler(rootObj, dbIdMap, olapIdMap);
        reader.setContentHandler(handler);
        InputSource is = new InputSource(in);
        reader.parse(is);
        hookupListeners(handler.root);
        return handler.root;
    }
    
    /**
     * Attaches listeners to all the Cube and Dimensions in the given OLAP model.
     * See {@link #hookupListeneresToSchema(Schema)}.
     * 
     * @param root
     *            The root of the OLAP model.
     * 
     */
    private static void hookupListeners(OLAPObject root) {
        if (root instanceof Schema) {
            hookupListeneresToSchema((Schema) root);
        } else if (root instanceof OLAPRootObject) {
            for (OLAPSession osession : ((OLAPRootObject) root).getChildren()) {
                hookupListeneresToSchema(osession.getSchema());
            }
        } else {
            logger.warn("Unkown root for OLAP model, skipping listener hookup: " + root);
        }
    }
    
    /**
     * Attaches listeners to all the Cube and Dimensions in the given Schema
     * so that changes will cause the CubeUsages, DimensionUsages or
     * VirtualCubeDimension that references them to update.
     * 
     * @param schema
     *            The schema to hookup listeners for.
     * 
     */
    private static void hookupListeneresToSchema(Schema schema) {
        // maps public Dimension name to object, used to hookup DimensionUsages.
        final Map<String, Dimension> publicDimensions = new HashMap<String, Dimension>();
        // maps non-public "Dimension name" to object, used to hookup VirtualCubeDimensions.
        final Map<CubeDimensionKey, CubeDimension> cubeDimensionMap = new HashMap<CubeDimensionKey, CubeDimension>();
        // maps Cube name to object, used to hookup CubeUsages.
        final Map<String, Cube> cubes = new HashMap<String, Cube>();
        
        // usages maps used to hookup removal listeners.
        final Map<Dimension, List<DimensionUsage>> dimensionUsageMap = new HashMap<Dimension, List<DimensionUsage>>();
        final Map<Cube, List<CubeUsage>> cubeUsageMap = new HashMap<Cube, List<CubeUsage>>();

        // first populate the maps that map object name to object.
        findDimensionNames(schema, publicDimensions, cubeDimensionMap);
        findCubeNames(schema, cubes);
        
        // then hookup listeners that monitors name changes and populate maps that map object to usages.
        recursiveDimensionHookupListeners(schema, publicDimensions, dimensionUsageMap, cubeDimensionMap);
        recursiveCubeHookupListeners(schema, cubes, cubeUsageMap);
        
        // now hookup listeners that will remove usages when the referenced object is removed.
        schema.addChildListener(new OLAPChildListener(){
            public void olapChildAdded(OLAPChildEvent e) {
                // do nothing.
            }
            
            public void olapChildRemoved(OLAPChildEvent e) {
                if (e.getChild() instanceof Dimension) {
                    Dimension dim = (Dimension) e.getChild();
                    List<DimensionUsage> dimUsages = dimensionUsageMap.get(dim);
                    if (dimUsages != null) {
                        for (DimensionUsage du : dimUsages) {
                            Cube c = (Cube) du.getParent();
                            c.removeDimension(du);
                        }
                    }
                } else if (e.getChild() instanceof Cube) {
                    Cube cube = (Cube) e.getChild();
                    List<CubeUsage> cubeUsages = cubeUsageMap.get(cube);
                    if (cubeUsages != null) {
                        for (CubeUsage cu : cubeUsages) {
                            CubeUsages c = (CubeUsages) cu.getParent();
                            c.removeCubeUsage(cu);
                        }
                    }
                }
            }
            
        });
    }

    /**
     * Recursively populates the given maps with the "names" of Dimensions in
     * the parent object.
     * 
     * @param parent
     *            The parent object to search through.
     * @param dimensions
     *            The map of public Dimension names to Dimension objects that
     *            will be populated.
     * @param cubeDimensionMap
     *            The map of non-public Dimension "names" to CubeDimension
     *            objects that will be populated. See {@link CubeDimensionKey}
     *            about "names".
     */
    private static void findDimensionNames(OLAPObject parent, Map<String, Dimension> dimensions,
            Map<CubeDimensionKey, CubeDimension> cubeDimensionMap) {
        for (OLAPObject child : parent.getChildren()) {
            if (parent instanceof Cube && child instanceof CubeDimension) {
                CubeDimensionKey cubeDimKey = new CubeDimensionKey(parent.getName(), child.getName());
                cubeDimensionMap.put(cubeDimKey, (CubeDimension) child);
            } else if (child instanceof Dimension) {
                dimensions.put(child.getName(), (Dimension) child);
            } else if (child.allowsChildren()) {
                findDimensionNames(child, dimensions, cubeDimensionMap);
            }
        }
    }
    
    /**
     * Recursively populates the given map with the names of Cubes in the parent
     * object.
     * 
     * @param parent
     *            The parent object to search through.
     * @param cubes
     *            The map of Cube names to Cube objects that will be populated.
     */
    private static void findCubeNames(OLAPObject parent, Map<String, Cube> cubes) {
        for (OLAPObject child : parent.getChildren()) {
            if (child instanceof Cube) {
                cubes.put(child.getName(), (Cube) child);
            } else if (child.allowsChildren()) {
                findCubeNames(child, cubes);
            }
        }
    }
    
    /**
     * Hooks up listeners to Dimensions in the given parent object, so that the
     * DimensionUsage or VirtualCubeDimension that references the Dimension will
     * update from name changes. Also populates the maps with Dimension to
     * referencing DimensionUsages.
     * 
     * @param parent
     *            The parent object to search through.
     * @param dimensions
     *            Map of public Dimension names to objects.
     * @param dimensionUsageMap
     *            Map of Dimension to referencing DimensionUsages that will be
     *            populated.
     * @param cubeDimensionMap
     *            Map of non-public Dimension "names" to objects, see
     *            {@link CubeDimensionKey} about "names".
     */
    private static void recursiveDimensionHookupListeners(OLAPObject parent, Map<String, Dimension> dimensions,
            Map<Dimension, List<DimensionUsage>> dimensionUsageMap,
            Map<CubeDimensionKey, CubeDimension> cubeDimensionMap) {
        for (OLAPObject child : parent.getChildren()) {
            if (child instanceof DimensionUsage) {
                // DimensionUsage can only reference public dimensions, find it
                // from that list.
                final DimensionUsage du = (DimensionUsage) child;
                Dimension dim = dimensions.get(du.getSource());
                if (dim == null) {
                    logger.error("Can't find the Dimension that this DimensionUsage references: " + du);
                    throw new IllegalStateException("The xml is corrupted, invalid reference by: " + du);
                } else {
                    // Hookup listeners to watch for Dimension name changes, to
                    // update DimensionUsage's dimension reference. Updates the
                    // source reference and its own name to match
                    // the Dimension's.
                    dim.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("name".equals(evt.getPropertyName())) {
                                du.setSource((String) evt.getNewValue());
                                du.setName((String) evt.getNewValue());
                            }
                        }
                    });

                    // add to map that will be used for removal listeners.
                    List<DimensionUsage> dimUsages = dimensionUsageMap.get(dim);
                    if (dimUsages == null) {
                        // first DimensionUsage to reference this Dimension,
                        // make a new list and add as new entry.
                        dimUsages = new ArrayList<DimensionUsage>();
                        dimUsages.add(du);
                        dimensionUsageMap.put(dim, dimUsages);
                    } else {
                        // add to the list in the entry.
                        dimUsages.add(du);
                    }
                }
            } else if (child instanceof VirtualCubeDimension) {
                // VirtualCubeDimensions are complicated...
                final VirtualCubeDimension vcd = (VirtualCubeDimension) child;
                Dimension dim;

                if (vcd.getCubeName() == null) {
                    // public dimension, find it from the public dimension list.
                    dim = dimensions.get(vcd.getName());
                } else {
                    // non-public dimension, find the CubeDimension that it
                    // references first.
                    CubeDimensionKey cubeDimKey = new CubeDimensionKey(vcd.getCubeName(), vcd.getName());
                    CubeDimension cd = cubeDimensionMap.get(cubeDimKey);

                    if (cd == null) {
                        logger.error("Can't find the CubeDimension that this VirtualCubeDimension references: " + vcd);
                        throw new IllegalStateException(
                                "The xml is corrupted, invalid reference by: " + vcd);
                    }

                    if (cd instanceof Dimension) {
                        // the VirtualCubeDimension references a private
                        // Dimension in a Cube
                        dim = (Dimension) cd;
                    } else if (cd instanceof DimensionUsage) {
                        // the VirtualCubeDimension references a DimensionUsage,
                        // need to find the referenced public Dimension.
                        DimensionUsage du = (DimensionUsage) cd;
                        dim = dimensions.get(du.getSource());
                    } else {
                        logger.warn("Unknown type of CubeDimension, skipping listener hookup: " + cd);
                        continue;
                    }

                    // need to hookup listeners to watch Cube name changes, to
                    // update VirtualCubeDimension cube reference.
                    Cube c = (Cube) cd.getParent();
                    c.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("name".equals(evt.getPropertyName())) {
                                vcd.setCubeName((String) evt.getNewValue());
                            }
                        }
                    });
                }

                if (dim == null) {
                    logger.error("Can't find the Dimension that this VirtualCubeDimension references: " + vcd);
                    throw new IllegalStateException(
                            "The xml is corrupted, invalid reference by: " + vcd);
                } else {
                    // hookup listeners to watch Dimension name changes, to
                    // update VirtualCubeDimension dimension reference.
                    dim.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("name".equals(evt.getPropertyName())) {
                                vcd.setName((String) evt.getNewValue());
                            }
                        }
                    });
                }
            } else if (child.allowsChildren()) {
                recursiveDimensionHookupListeners(child, dimensions, dimensionUsageMap, cubeDimensionMap);
            }
        }
    }
    
    /**
     * Hookup listeners to Cubes in the given parent object, so that the
     * CubeUsage that references the Cube will update from name changes. Also
     * populates the maps with Cube to referencing CubeUsages.
     * 
     * @param parent
     *            The parent object to search through.
     * @param cubes
     *            Maps Cube names to objects.
     * @param cubeUsageMap
     *            Map of Cube to CubeUsages that will be populated.
     */
    private static void recursiveCubeHookupListeners(OLAPObject parent, Map<String, Cube> cubes,
            Map<Cube, List<CubeUsage>> cubeUsageMap) {
        for (OLAPObject child : parent.getChildren()) {
            if (child instanceof VirtualCube) {
                VirtualCube vCube = (VirtualCube) child;
                if (vCube.getCubeUsage() != null) {
                    for (final CubeUsage cu : vCube.getCubeUsage().getCubeUsages()) {
                        Cube cube = cubes.get(cu.getCubeName());
                        if (cube == null) {
                            logger.error("Can't find the Cube that this CubeUsage references: " + cu);
                            throw new IllegalStateException("The xml is corrupted, invalid reference by: " + cu);
                        } else {
                            // hookup listener to Cube for name changes to
                            // update the CubeUsage.
                            cube.addPropertyChangeListener(new PropertyChangeListener() {
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if ("name".equals(evt.getPropertyName())) {
                                        cu.setCubeName((String) evt.getNewValue());
                                    }
                                }
                            });

                            // add to map that will be used for removal
                            // listeners.
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
                }
            } else if (child.allowsChildren()) {
                recursiveCubeHookupListeners(child, cubes, cubeUsageMap);
            }
        }
    }
    
    /**
     * A composite key class that holds the cubeName and name properties in a
     * VirtualCubeDimension. The cubeName property identifies the name of Cube
     * that holds the CubeDimension and the name property identifies the name of
     * the CubeDimenion. This will form the key used to find the CubeDimension
     * that a VirtualCubeDimension is referencing.
     * 
     */
    private static class CubeDimensionKey {
        private final String cubeName;
        private final String dimensionName;
        
        public CubeDimensionKey(String cubeName, String dimensionName) {
            this.cubeName = cubeName;
            this.dimensionName = dimensionName;
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

    private static class MondrianSAXHandler extends DefaultHandler {
        private Stack<OLAPObject> context = new Stack<OLAPObject>();
        private Locator locator;
        private OLAPObject root;
        private StringBuilder text;
        
        private Map<String, String>  currentOSessionAtts;
        
        private boolean inOlap;
       
        private final boolean importMode;
        
        private Map<String, SQLObject> dbIdMap;
        private Map<String, OLAPObject> olapIdMap;
        
        public MondrianSAXHandler() {
            this.importMode = true;
        }
       
        public MondrianSAXHandler(OLAPRootObject rootObj, Map<String, SQLObject> dbIdMap, Map<String, OLAPObject> olapIdMap) {
            this.importMode = false;
            this.root = rootObj;
            this.dbIdMap = dbIdMap;
            this.olapIdMap = olapIdMap;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            try {
	            boolean pushElem = true;
	            OLAPObject currentElement;
	            
	            if (qName.equals("olap") || qName.equals("Schema")) {
	            	inOlap = true;
	           	}
	           	if (!inOlap) return;
	            
	            if (qName.equals("olap")) {
	                currentElement = root;
	                inOlap = true;
	            } else if (qName.equals("olap-session")) {
	                currentOSessionAtts = new HashMap<String, String>();
	                for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        currentOSessionAtts.put(aname, aval);
	                }
	                pushElem = false;
	                currentElement = null;                  
	           
	            } else if (qName.equals("Schema")) {
                    MondrianModel.Schema elem = new MondrianModel.Schema();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("measuresCaption")) {
                             
                                elem.setMeasuresCaption(aval);
                              
                        } else if (aname.equals("defaultRole")) {
                             
                                elem.setDefaultRole(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Cube")) {
                    MondrianModel.Cube elem = new MondrianModel.Cube();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("defaultMeasure")) {
                             
                                elem.setDefaultMeasure(aval);
                              
                        } else if (aname.equals("cache")) {
                             
                                elem.setCache(Boolean.valueOf(aval));
                              
                        } else if (aname.equals("enabled")) {
                             
                                elem.setEnabled(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("VirtualCube")) {
                    MondrianModel.VirtualCube elem = new MondrianModel.VirtualCube();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("enabled")) {
                             
                                elem.setEnabled(Boolean.valueOf(aval));
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("defaultMeasure")) {
                             
                                elem.setDefaultMeasure(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("CubeUsages")) {
                    MondrianModel.CubeUsages elem = new MondrianModel.CubeUsages();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("CubeUsage")) {
                    MondrianModel.CubeUsage elem = new MondrianModel.CubeUsage();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("cubeName")) {
                             
                                elem.setCubeName(aval);
                              
                        } else if (aname.equals("ignoreUnrelatedDimensions")) {
                             
                                elem.setIgnoreUnrelatedDimensions(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("VirtualCubeDimension")) {
                    MondrianModel.VirtualCubeDimension elem = new MondrianModel.VirtualCubeDimension();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("cubeName")) {
                             
                                elem.setCubeName(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   handleCubeDimensionAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("VirtualCubeMeasure")) {
                    MondrianModel.VirtualCubeMeasure elem = new MondrianModel.VirtualCubeMeasure();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("cubeName")) {
                             
                                elem.setCubeName(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("visible")) {
                             
                                elem.setVisible(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("DimensionUsage")) {
                    MondrianModel.DimensionUsage elem = new MondrianModel.DimensionUsage();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("source")) {
                             
                                elem.setSource(aval);
                              
                        } else if (aname.equals("level")) {
                             
                                elem.setLevel(aval);
                              
                        } else if (aname.equals("usagePrefix")) {
                             
                                elem.setUsagePrefix(aval);
                              
                        } else {
			               
			                   handleCubeDimensionAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("Dimension")) {
                    MondrianModel.Dimension elem = new MondrianModel.Dimension();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("type")) {
                             
                                elem.setType(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("usagePrefix")) {
                             
                                elem.setUsagePrefix(aval);
                              
                        } else {
			               
			                   handleCubeDimensionAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("Hierarchy")) {
                    MondrianModel.Hierarchy elem = new MondrianModel.Hierarchy();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("hasAll")) {
                             
                                elem.setHasAll(Boolean.valueOf(aval));
                              
                        } else if (aname.equals("allMemberName")) {
                             
                                elem.setAllMemberName(aval);
                              
                        } else if (aname.equals("allMemberCaption")) {
                             
                                elem.setAllMemberCaption(aval);
                              
                        } else if (aname.equals("allLevelName")) {
                             
                                elem.setAllLevelName(aval);
                              
                        } else if (aname.equals("primaryKey")) {
                             
                                elem.setPrimaryKey(aval);
                              
                        } else if (aname.equals("primaryKeyTable")) {
                             
                                elem.setPrimaryKeyTable(aval);
                              
                        } else if (aname.equals("defaultMember")) {
                             
                                elem.setDefaultMember(aval);
                              
                        } else if (aname.equals("memberReaderClass")) {
                             
                                elem.setMemberReaderClass(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Level")) {
                    MondrianModel.Level elem = new MondrianModel.Level();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("approxRowCount")) {
                             
                                elem.setApproxRowCount(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("table")) {
                             
                                elem.setTable(aval);
                              
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else if (aname.equals("nameColumn")) {
                             
                                elem.setNameColumn(aval);
                              
                        } else if (aname.equals("ordinalColumn")) {
                             
                                elem.setOrdinalColumn(aval);
                              
                        } else if (aname.equals("parentColumn")) {
                             
                                elem.setParentColumn(aval);
                              
                        } else if (aname.equals("nullParentValue")) {
                             
                                elem.setNullParentValue(aval);
                              
                        } else if (aname.equals("type")) {
                             
                                elem.setType(aval);
                              
                        } else if (aname.equals("uniqueMembers")) {
                             
                                elem.setUniqueMembers(Boolean.valueOf(aval));
                              
                        } else if (aname.equals("levelType")) {
                             
                                elem.setLevelType(aval);
                              
                        } else if (aname.equals("hideMemberIf")) {
                             
                                elem.setHideMemberIf(aval);
                              
                        } else if (aname.equals("formatter")) {
                             
                                elem.setFormatter(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("captionColumn")) {
                             
                                elem.setCaptionColumn(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Closure")) {
                    MondrianModel.Closure elem = new MondrianModel.Closure();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("parentColumn")) {
                             
                                elem.setParentColumn(aval);
                              
                        } else if (aname.equals("childColumn")) {
                             
                                elem.setChildColumn(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Property")) {
                    MondrianModel.Property elem = new MondrianModel.Property();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else if (aname.equals("type")) {
                             
                                elem.setType(aval);
                              
                        } else if (aname.equals("formatter")) {
                             
                                elem.setFormatter(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Measure")) {
                    MondrianModel.Measure elem = new MondrianModel.Measure();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else if (aname.equals("datatype")) {
                             
                                elem.setDatatype(aval);
                              
                        } else if (aname.equals("formatString")) {
                             
                                elem.setFormatString(aval);
                              
                        } else if (aname.equals("aggregator")) {
                             
                                elem.setAggregator(aval);
                              
                        } else if (aname.equals("formatter")) {
                             
                                elem.setFormatter(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("visible")) {
                             
                                elem.setVisible(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("CalculatedMember")) {
                    MondrianModel.CalculatedMember elem = new MondrianModel.CalculatedMember();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("formatString")) {
                             
                                elem.setFormatString(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("formula")) {
                             
                                elem.setFormula(aval);
                              
                        } else if (aname.equals("dimension")) {
                             
                                elem.setDimension(aval);
                              
                        } else if (aname.equals("visible")) {
                             
                                elem.setVisible(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("CalculatedMemberProperty")) {
                    MondrianModel.CalculatedMemberProperty elem = new MondrianModel.CalculatedMemberProperty();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("caption")) {
                             
                                elem.setCaption(aval);
                              
                        } else if (aname.equals("expression")) {
                             
                                elem.setExpression(aval);
                              
                        } else if (aname.equals("value")) {
                             
                                elem.setValue(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("NamedSet")) {
                    MondrianModel.NamedSet elem = new MondrianModel.NamedSet();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("formula")) {
                             
                                elem.setFormula(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Formula")) {
                    MondrianModel.Formula elem = new MondrianModel.Formula();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("MemberReaderParameter")) {
                    MondrianModel.MemberReaderParameter elem = new MondrianModel.MemberReaderParameter();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("value")) {
                             
                                elem.setValue(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("View")) {
                    MondrianModel.View elem = new MondrianModel.View();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("alias")) {
                             
                                elem.setAlias(aval);
                              
                        } else {
			               
			                   handleRelationAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("SQL")) {
                    MondrianModel.SQL elem = new MondrianModel.SQL();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("dialect")) {
                             
                                elem.setDialect(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Join")) {
                    MondrianModel.Join elem = new MondrianModel.Join();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("leftAlias")) {
                             
                                elem.setLeftAlias(aval);
                              
                        } else if (aname.equals("leftKey")) {
                             
                                elem.setLeftKey(aval);
                              
                        } else if (aname.equals("rightAlias")) {
                             
                                elem.setRightAlias(aval);
                              
                        } else if (aname.equals("rightKey")) {
                             
                                elem.setRightKey(aval);
                              
                        } else {
			               
			                   handleRelationOrJoinAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("Table")) {
                    MondrianModel.Table elem = new MondrianModel.Table();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("schema")) {
                             
                                elem.setSchema(aval);
                              
                        } else if (aname.equals("alias")) {
                             
                                elem.setAlias(aval);
                              
                        } else {
			               
			                   handleRelationAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("InlineTable")) {
                    MondrianModel.InlineTable elem = new MondrianModel.InlineTable();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("alias")) {
                             
                                elem.setAlias(aval);
                              
                        } else {
			               
			                   handleRelationAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("ColumnDefs")) {
                    MondrianModel.ColumnDefs elem = new MondrianModel.ColumnDefs();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("ColumnDef")) {
                    MondrianModel.ColumnDef elem = new MondrianModel.ColumnDef();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("type")) {
                             
                                elem.setType(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Rows")) {
                    MondrianModel.Rows elem = new MondrianModel.Rows();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Row")) {
                    MondrianModel.Row elem = new MondrianModel.Row();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Value")) {
                    MondrianModel.Value elem = new MondrianModel.Value();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("AggName")) {
                    MondrianModel.AggName elem = new MondrianModel.AggName();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   handleAggTableAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("AggPattern")) {
                    MondrianModel.AggPattern elem = new MondrianModel.AggPattern();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("pattern")) {
                             
                                elem.setPattern(aval);
                              
                        } else {
			               
			                   handleAggTableAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("AggExclude")) {
                    MondrianModel.AggExclude elem = new MondrianModel.AggExclude();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("pattern")) {
                             
                                elem.setPattern(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("ignorecase")) {
                             
                                elem.setIgnorecase(Boolean.valueOf(aval));
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("AggFactCount")) {
                    MondrianModel.AggFactCount elem = new MondrianModel.AggFactCount();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleAggColumnNameAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("AggIgnoreColumn")) {
                    MondrianModel.AggIgnoreColumn elem = new MondrianModel.AggIgnoreColumn();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleAggColumnNameAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("AggForeignKey")) {
                    MondrianModel.AggForeignKey elem = new MondrianModel.AggForeignKey();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("factColumn")) {
                             
                                elem.setFactColumn(aval);
                              
                        } else if (aname.equals("aggColumn")) {
                             
                                elem.setAggColumn(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("AggLevel")) {
                    MondrianModel.AggLevel elem = new MondrianModel.AggLevel();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("AggMeasure")) {
                    MondrianModel.AggMeasure elem = new MondrianModel.AggMeasure();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("column")) {
                             
                                elem.setColumn(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Column")) {
                    MondrianModel.Column elem = new MondrianModel.Column();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("table")) {
                             
                                elem.setTable(aval);
                              
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   handleExpressionAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("KeyExpression")) {
                    MondrianModel.KeyExpression elem = new MondrianModel.KeyExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("ParentExpression")) {
                    MondrianModel.ParentExpression elem = new MondrianModel.ParentExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("OrdinalExpression")) {
                    MondrianModel.OrdinalExpression elem = new MondrianModel.OrdinalExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("NameExpression")) {
                    MondrianModel.NameExpression elem = new MondrianModel.NameExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("CaptionExpression")) {
                    MondrianModel.CaptionExpression elem = new MondrianModel.CaptionExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("MeasureExpression")) {
                    MondrianModel.MeasureExpression elem = new MondrianModel.MeasureExpression();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleExpressionViewAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("Role")) {
                    MondrianModel.Role elem = new MondrianModel.Role();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("SchemaGrant")) {
                    MondrianModel.SchemaGrant elem = new MondrianModel.SchemaGrant();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   handleGrantAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("CubeGrant")) {
                    MondrianModel.CubeGrant elem = new MondrianModel.CubeGrant();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("cube")) {
                             
                                elem.setCube(aval);
                              
                        } else {
			               
			                   handleGrantAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("DimensionGrant")) {
                    MondrianModel.DimensionGrant elem = new MondrianModel.DimensionGrant();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("dimension")) {
                             
                                elem.setDimension(aval);
                              
                        } else {
			               
			                   handleGrantAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("HierarchyGrant")) {
                    MondrianModel.HierarchyGrant elem = new MondrianModel.HierarchyGrant();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("hierarchy")) {
                             
                                elem.setHierarchy(aval);
                              
                        } else if (aname.equals("topLevel")) {
                             
                                elem.setTopLevel(aval);
                              
                        } else if (aname.equals("bottomLevel")) {
                             
                                elem.setBottomLevel(aval);
                              
                        } else if (aname.equals("rollupPolicy")) {
                             
                                elem.setRollupPolicy(aval);
                              
                        } else {
			               
			                   handleGrantAttribute(elem, aname, aval);
			                 
                        }
                    }
            
	            } else if (qName.equals("MemberGrant")) {
                    MondrianModel.MemberGrant elem = new MondrianModel.MemberGrant();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("member")) {
                             
                                elem.setMember(aval);
                              
                        } else if (aname.equals("access")) {
                             
                                elem.setAccess(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Union")) {
                    MondrianModel.Union elem = new MondrianModel.Union();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("RoleUsage")) {
                    MondrianModel.RoleUsage elem = new MondrianModel.RoleUsage();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("roleName")) {
                             
                                elem.setRoleName(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("UserDefinedFunction")) {
                    MondrianModel.UserDefinedFunction elem = new MondrianModel.UserDefinedFunction();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("className")) {
                             
                                elem.setClassName(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else if (qName.equals("Parameter")) {
                    MondrianModel.Parameter elem = new MondrianModel.Parameter();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null && aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        
                        } else if (aname.equals("name")) {
                             
                                elem.setName(aval);
                              
                        } else if (aname.equals("description")) {
                             
                                elem.setDescription(aval);
                              
                        } else if (aname.equals("type")) {
                             
                                elem.setType(aval);
                              
                        } else if (aname.equals("modifiable")) {
                             
                                elem.setModifiable(Boolean.valueOf(aval));
                              
                        } else if (aname.equals("defaultValue")) {
                             
                                elem.setDefaultValue(aval);
                              
                        } else {
			               
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 
                        }
                    }
            
	            } else {
	                pushElem = false;
	                currentElement = null;
	                if (inOlap) {
	                	logger.warn("Unknown element type \"" + qName + "\" at locator: " + locator);
	                } else {
	                	logger.debug("Unknown element type \"" + qName + "\" at locator: " + locator);	
	                }
	            }
	            if (pushElem) {
                    if (!context.isEmpty()) {
                        if (currentElement instanceof MondrianModel.Schema) {
                            OLAPSession osession = new OLAPSession((MondrianModel.Schema) currentElement);
                            for (String aname : currentOSessionAtts.keySet()) {
                                String aval = currentOSessionAtts.get(aname);
                                if (aname.equals("db-ref")) {
                                    osession.setDatabase((SQLDatabase) dbIdMap.get(aval));
                                } else if (olapIdMap != null && aname.equals("id")) {
                       				olapIdMap.put(aval, osession);
                                } else {
                                    logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+OLAPSession.class+"\"");
                                }
                            }
                            context.peek().addChild(osession);
                            context.push(osession);
                        } else {
                            context.peek().addChild(currentElement);
                        }
                    } else {
                        if (importMode) {
                            root = (MondrianModel.Schema) currentElement;
                        }
                    }
                    context.push(currentElement);
                    logger.debug("Pushed " + currentElement);
	            }
	        } catch (Exception ex) {
	            throw new SAXException("Error at Line: "+locator.getLineNumber()+", Column: "+locator.getColumnNumber(), ex);
	        }
        }

        @Override
        public void characters (char ch[], int start, int length)
        throws SAXException
        {
            if (context.isEmpty()) return;
            if (text == null) {
                text = new StringBuilder();
            }
            OLAPObject elem = context.peek();
            if (elem instanceof MondrianModel.Value || elem instanceof MondrianModel.SQL || elem instanceof MondrianModel.Formula) {
                for (int i = start; i < length+start; i++) {
                    text.append(ch[i]);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (context.isEmpty()) return;
        	if (context.peek() instanceof MondrianModel.Value) {
                ((MondrianModel.Value) context.peek()).setText(text.toString().trim());
            } else if (context.peek() instanceof MondrianModel.SQL) {
                ((MondrianModel.SQL) context.peek()).setText(text.toString().trim());
            } else if (context.peek() instanceof MondrianModel.Formula) {
                ((MondrianModel.Formula) context.peek()).setText(text.toString().trim());
            } else if ((context.peek() instanceof OLAPRootObject && !importMode)||
            		(context.peek() instanceof MondrianModel.Schema && importMode)) {
            	inOlap = false;
            }
            text = null;
            OLAPObject popped = context.pop();
            logger.debug("Popped " + popped);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
       
	    private void handleCubeDimensionAttribute(MondrianModel.CubeDimension elem, String aname, String aval) {
	        if (false) {
	        
	        } else if (aname.equals("name")) {
	            
	                elem.setName(aval);
	              
	        } else if (aname.equals("caption")) {
	            
	                elem.setCaption(aval);
	              
	        } else if (aname.equals("foreignKey")) {
	            
	                elem.setForeignKey(aval);
	              
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

	    private void handleRelationOrJoinAttribute(MondrianModel.RelationOrJoin elem, String aname, String aval) {
	        if (false) {
	        
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

	    private void handleRelationAttribute(MondrianModel.Relation elem, String aname, String aval) {
	        if (false) {
	        
	        } else {
	          
	              handleRelationOrJoinAttribute(elem, aname, aval);
	            
	        }
	    }

	    private void handleAggTableAttribute(MondrianModel.AggTable elem, String aname, String aval) {
	        if (false) {
	        
	        } else if (aname.equals("ignorecase")) {
	            
	                elem.setIgnorecase(Boolean.valueOf(aval));
	              
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

	    private void handleAggColumnNameAttribute(MondrianModel.AggColumnName elem, String aname, String aval) {
	        if (false) {
	        
	        } else if (aname.equals("column")) {
	            
	                elem.setColumn(aval);
	              
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

	    private void handleExpressionAttribute(MondrianModel.Expression elem, String aname, String aval) {
	        if (false) {
	        
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

	    private void handleExpressionViewAttribute(MondrianModel.ExpressionView elem, String aname, String aval) {
	        if (false) {
	        
	        } else {
	          
	              handleExpressionAttribute(elem, aname, aval);
	            
	        }
	    }

	    private void handleGrantAttribute(MondrianModel.Grant elem, String aname, String aval) {
	        if (false) {
	        
	        } else if (aname.equals("access")) {
	            
	                elem.setAccess(aval);
	              
	        } else {
	          
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            
	        }
	    }

    }
}
