<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:transform 
     version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output 
  encoding="iso-8859-1" 
  method="text" 
  indent="no"
  standalone="yes"
  omit-xml-declaration="yes"
/>

<xsl:strip-space elements="*"/>

<xsl:template match="/Model">
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
            Map&lt;String, SQLObject&gt; dbIdMap, Map&lt;String, OLAPObject&gt; olapIdMap) throws IOException, SAXException {
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
    public static OLAPObject parse(InputStream in, OLAPRootObject rootObj, Map&lt;String, SQLObject&gt; dbIdMap,
            Map&lt;String, OLAPObject&gt; olapIdMap) throws IOException, SAXException {
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
        final Map&lt;String, Dimension&gt; publicDimensions = new HashMap&lt;String, Dimension&gt;();
        // maps non-public "Dimension name" to object, used to hookup VirtualCubeDimensions.
        final Map&lt;CubeDimensionKey, CubeDimension&gt; cubeDimensionMap = new HashMap&lt;CubeDimensionKey, CubeDimension&gt;();
        // maps Cube name to object, used to hookup CubeUsages.
        final Map&lt;String, Cube&gt; cubes = new HashMap&lt;String, Cube&gt;();
        
        // usages maps used to hookup removal listeners.
        final Map&lt;Dimension, List&lt;DimensionUsage&gt;&gt; dimensionUsageMap = new HashMap&lt;Dimension, List&lt;DimensionUsage&gt;&gt;();
        final Map&lt;Cube, List&lt;CubeUsage&gt;&gt; cubeUsageMap = new HashMap&lt;Cube, List&lt;CubeUsage&gt;&gt;();

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
                    List&lt;DimensionUsage&gt; dimUsages = dimensionUsageMap.get(dim);
                    if (dimUsages != null) {
                        for (DimensionUsage du : dimUsages) {
                            Cube c = (Cube) du.getParent();
                            c.removeDimension(du);
                        }
                    }
                } else if (e.getChild() instanceof Cube) {
                    Cube cube = (Cube) e.getChild();
                    List&lt;CubeUsage&gt; cubeUsages = cubeUsageMap.get(cube);
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
    private static void findDimensionNames(OLAPObject parent, Map&lt;String, Dimension&gt; dimensions,
            Map&lt;CubeDimensionKey, CubeDimension&gt; cubeDimensionMap) {
        for (OLAPObject child : parent.getChildren()) {
            if (parent instanceof Cube &amp;&amp; child instanceof CubeDimension) {
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
    private static void findCubeNames(OLAPObject parent, Map&lt;String, Cube&gt; cubes) {
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
    private static void recursiveDimensionHookupListeners(OLAPObject parent, Map&lt;String, Dimension&gt; dimensions,
            Map&lt;Dimension, List&lt;DimensionUsage&gt;&gt; dimensionUsageMap,
            Map&lt;CubeDimensionKey, CubeDimension&gt; cubeDimensionMap) {
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
                    List&lt;DimensionUsage&gt; dimUsages = dimensionUsageMap.get(dim);
                    if (dimUsages == null) {
                        // first DimensionUsage to reference this Dimension,
                        // make a new list and add as new entry.
                        dimUsages = new ArrayList&lt;DimensionUsage&gt;();
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
    private static void recursiveCubeHookupListeners(OLAPObject parent, Map&lt;String, Cube&gt; cubes,
            Map&lt;Cube, List&lt;CubeUsage&gt;&gt; cubeUsageMap) {
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
                            List&lt;CubeUsage&gt; cubeUsages = cubeUsageMap.get(cube);
                            if (cubeUsages != null) {
                                cubeUsages.add(cu);
                            } else {
                                cubeUsages = new ArrayList&lt;CubeUsage&gt;();
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
       
    }

    private static class MondrianSAXHandler extends DefaultHandler {
        private Stack&lt;OLAPObject&gt; context = new Stack&lt;OLAPObject&gt;();
        private Locator locator;
        private OLAPObject root;
        private StringBuilder text;
        
        private Map&lt;String, String&gt;  currentOSessionAtts;
        
        private boolean inOlap;
       
        private final boolean importMode;
        
        private Map&lt;String, SQLObject&gt; dbIdMap;
        private Map&lt;String, OLAPObject&gt; olapIdMap;
        
        public MondrianSAXHandler() {
            this.importMode = true;
        }
       
        public MondrianSAXHandler(OLAPRootObject rootObj, Map&lt;String, SQLObject&gt; dbIdMap, Map&lt;String, OLAPObject&gt; olapIdMap) {
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
	                currentOSessionAtts = new HashMap&lt;String, String&gt;();
	                for (int i = 0; i &lt; atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        currentOSessionAtts.put(aname, aval);
	                }
	                pushElem = false;
	                currentElement = null;                  
	           <xsl:for-each select="Element">
	            } else if (qName.equals("<xsl:value-of select="@type"/>")) {
                    MondrianModel.<xsl:value-of select="@type"/> elem = new MondrianModel.<xsl:value-of select="@type"/>();
                    currentElement = elem;
                    for (int i = 0; i &lt; atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null &amp;&amp; aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        <xsl:for-each select="Attribute">
                        } else if (aname.equals("<xsl:value-of select="@name"/>")) {
                             <xsl:choose>
                              <xsl:when test="@type = 'Boolean'">
                                elem.set<xsl:call-template name="name-initcap"/>(Boolean.valueOf(aval));
                              </xsl:when>
                              <xsl:otherwise>
                                elem.set<xsl:call-template name="name-initcap"/>(aval);
                              </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        } else {
			               <xsl:choose>
			                 <xsl:when test="@class">
			                   handle<xsl:value-of select="@class"/>Attribute(elem, aname, aval);
			                 </xsl:when>
			                 <xsl:otherwise>
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 </xsl:otherwise>
			               </xsl:choose>
                        }
                    }
            </xsl:for-each>
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
                                } else if (olapIdMap != null &amp;&amp; aname.equals("id")) {
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
                for (int i = start; i &lt; length+start; i++) {
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
            } else if ((context.peek() instanceof OLAPRootObject &amp;&amp; !importMode)||
            		(context.peek() instanceof MondrianModel.Schema &amp;&amp; importMode)) {
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
       <xsl:apply-templates select="Class"/>
    }
}
</xsl:template>

<xsl:template match="Class">
	    private void handle<xsl:value-of select="@class"/>Attribute(MondrianModel.<xsl:value-of select="@class"/> elem, String aname, String aval) {
	        if (false) {
	        <xsl:for-each select="Attribute">
	        } else if (aname.equals("<xsl:value-of select="@name"/>")) {
	            <xsl:choose>
	              <xsl:when test="@type = 'Boolean'">
	                elem.set<xsl:call-template name="name-initcap"/>(Boolean.valueOf(aval));
	              </xsl:when>
	              <xsl:otherwise>
	                elem.set<xsl:call-template name="name-initcap"/>(aval);
	              </xsl:otherwise>
	            </xsl:choose>
	        </xsl:for-each>
	        } else {
	          <xsl:choose>
	            <xsl:when test="@superclass">
	              handle<xsl:value-of select="@superclass"/>Attribute(elem, aname, aval);
	            </xsl:when>
	            <xsl:otherwise>
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            </xsl:otherwise>
	          </xsl:choose>
	        }
	    }
</xsl:template>

<!-- Returns the initcap version of the "name" attribute of the current element -->
<xsl:template name="name-initcap">
  <xsl:value-of select="concat(translate(substring(@name,1,1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring(@name, 2))"/>
</xsl:template>

<!-- Returns the initcap version of the depluralized version of the "name" attribute of the current element -->
<xsl:template name="name-initcap-nonplural">
  <xsl:variable name="initcap" select="concat(translate(substring(@name,1,1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring(@name, 2))"/>
  <xsl:choose>
    <xsl:when test="$initcap = 'Hierarchies'">Hierarchy</xsl:when>
    <xsl:when test="$initcap = 'Properties'">Property</xsl:when>
    <xsl:otherwise><xsl:value-of select="substring($initcap, 1, string-length($initcap)-1)"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="Doc">
  <!-- not applicable -->
</xsl:template>

<xsl:template match="Code">
  <!-- not applicable -->
</xsl:template>

</xsl:transform>
