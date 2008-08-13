
package ca.sqlpower.architect.olap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * This is class is generated from xml-to-parser.xsl!  Do not alter it directly.
 */
public class MondrianXMLReader {

    private static final Logger logger = Logger.getLogger(MondrianXMLReader.class);

    public static OLAPObject parse(File f, boolean mondrianMode) throws IOException, SAXException {
        return parse(null, null, new FileInputStream(f), mondrianMode);
    }

    public static OLAPObject parse(OLAPRootObject rootObj, Map dbIdMap, File f, boolean mondrianMode) throws IOException, SAXException {
        return parse(rootObj, dbIdMap, new FileInputStream(f), mondrianMode);
    }

    public static OLAPObject parse(OLAPRootObject rootObj, Map dbIdMap, InputStream in, boolean mondrianMode) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler(rootObj, dbIdMap, mondrianMode);
        reader.setContentHandler(handler);
        InputSource is = new InputSource(in);
        reader.parse(is);
        return handler.root;
    }

    private static class MondrianSAXHandler extends DefaultHandler {
        private Stack<OLAPObject> context = new Stack<OLAPObject>();
        private Locator locator;
        private OLAPObject root;
        private StringBuilder text;
        
        private Attributes currentOSessionAtts;
        
        private boolean inOlap;
       
        private final Map dbIdMap;
        private final boolean mondrianMode;
       
        public MondrianSAXHandler(OLAPRootObject rootObj, Map dbIdMap, boolean mondrianMode) {
            if (rootObj != null) {
                this.root = rootObj;
            }
            this.dbIdMap = dbIdMap;
            this.mondrianMode = mondrianMode;
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
	                currentOSessionAtts = atts;
	                pushElem = false;
	                currentElement = null;               
	           
	            } else if (qName.equals("Schema")) {
                    MondrianModel.Schema elem = new MondrianModel.Schema();
                    currentElement = elem;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                        if (false) {
                        
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
                            for (int i = 0; i < currentOSessionAtts.getLength(); i++) {
                                String aname = currentOSessionAtts.getQName(i);
                                String aval = currentOSessionAtts.getValue(i);
                                if (aname.equals("dbcs-ref")) {
                                    osession.setDatabase((SQLDatabase) dbIdMap.get(aval));
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
                        if (mondrianMode) {
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
            } else if ((context.peek() instanceof OLAPRootObject && !mondrianMode)||
            		(context.peek() instanceof MondrianModel.Schema && mondrianMode)) {
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
