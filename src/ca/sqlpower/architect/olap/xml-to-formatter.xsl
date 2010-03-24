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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This is class is generated from xml-to-formatter.xsl!  Do not alter it directly.
 */
public class MondrianXMLWriter {

    private static final Logger logger = Logger.getLogger(MondrianXMLWriter.class);

    /**
     * Exports an OLAP schema to a file in the Mondrian schema xml format.
     * 
     * @param f
     *            The output file.
     * @param schema
     *            The schema to be exported.
     * @throws IOException
     *             If creating a file writer for the given file failed.
     */
    public static void exportXML(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(f, "UTF-8");
        out.println("&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;");
        MondrianXMLWriter writer = new MondrianXMLWriter(out);
        writer.writeSchema(schema);
        out.flush();
        out.close();
    }

    /**
     * Writes the xml representation of an OLAP schema to a file in the
     * Architect format. This is the same as calling
     * {@link #write(PrintWriter, MondrianModel.Schema, boolean, int, Map)} with
     * default values.
     * 
     * @param f
     *            The output file.
     * @param schema
     *            The schema to be written.
     * @return A map of the OLAPObjects in the schema and their
     *         generated ids.
     * @throws IOException
     *             If creating a file writer for the given file failed.
     */
    public static Map&lt;OLAPObject, String&gt; write(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(f, "UTF-8");
        return write(out, schema, true, 0, new IdentityHashMap&lt;OLAPObject, String&gt;());
    }

    /**
     * Writes the xml representation of an OLAP schema to a print writer in the
     * Architect format.
     * 
     * @param out
     *            The PrintWriter to write to.
     * @param schema
     *            The schema to be written.
     * @param closeWriter
     *            Indicates whether the print writer should be closed
     *            afterwards.
     * @param indent
     *            The number of spaces to start indent by.
     * @param idMap
     *            A map of OLAPObjects and corresponding ids. This map should not contain
     *            any object in the given schema. Must not be null.
     * @return An updated map from the given idMap, adding in the OLAPObjects in
     *         the given schema.
     */
    public static Map&lt;OLAPObject, String&gt; write(PrintWriter out, MondrianModel.Schema schema, boolean closeWriter,
    		int indent, Map&lt;OLAPObject, String&gt; idMap) {
        MondrianXMLWriter writer = new MondrianXMLWriter(out, indent, idMap);
        writer.writeSchema(schema);
        out.flush();
        if (closeWriter) {
	        out.close();
	    }
        return idMap;
    }
    
    private final PrintWriter out;
    
    private int indent;
    
    private Map&lt;OLAPObject, String&gt; idMap;
    
    public MondrianXMLWriter(PrintWriter out) {
        this.out = out;
    }
    
    public MondrianXMLWriter(PrintWriter out, int indent, Map&lt;OLAPObject, String&gt; idMap) {
        this(out);
        this.indent = indent;
        this.idMap = idMap;
    }
    
    private void indentLine() {
    	for (int i = 0; i &lt; indent; i++) {
    		out.print(" ");
    	}
    }

    private void writeStartTag(String elemName, Map&lt;String, Object&gt; atts) {
        indentLine();
        out.print("&lt;" + elemName);
        for (Map.Entry&lt;String, Object&gt; att : atts.entrySet()) {
           if (att.getValue() != null) {
               out.print(" "+att.getKey()+"=\""+att.getValue()+"\""); <!-- TODO: escape attribute value! -->
           }
        }
    }
    
    private void foolishWrite(OLAPObject obj) {
    	if (false) {}
     	<xsl:for-each select="Element">
     	else if ((obj.getClass()).equals(MondrianModel.<xsl:value-of select="@type"/>.class)) {
     		write<xsl:value-of select="@type"/>((MondrianModel.<xsl:value-of select="@type"/>)obj);
     	}
     	</xsl:for-each>
     	<xsl:for-each select="Class">
     	else if ((obj.getClass()).equals(MondrianModel.<xsl:value-of select="@class"/>.class)) {
     		write<xsl:value-of select="@class"/>((MondrianModel.<xsl:value-of select="@class"/>)obj);
     	}
     	</xsl:for-each>
     	else {
     		logger.warn("Skipping unknown content \""+ obj.getClass()); 
     	}
    }
<xsl:apply-templates/>
}
</xsl:template>

<xsl:template match="Element">

    public void write<xsl:value-of select="@type"/>(MondrianModel.<xsl:value-of select="@type"/> elem) {

		
		
		boolean oneTag = true;
        Map&lt;String, Object&gt; atts = new LinkedHashMap&lt;String, Object&gt;();
		if (idMap != null) {
			String id = Integer.toString(idMap.size());
			if (idMap.put(elem, id) != null) {
				throw new IllegalStateException("Duplicate OLAPObject found: " + elem);
			}
			atts.put("id", id);
		}
		
		
        <xsl:for-each select="Attribute">
        atts.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>
        <xsl:if test="@class">
        populate<xsl:value-of select="@class"/>Attributes(elem, atts);
        </xsl:if>
        
        writeStartTag("<xsl:value-of select="@type"/>", atts);
        <!--  <xsl:for-each select="Object">
        if (elem.get<xsl:call-template name="name-initcap"/>() != null) {
        	write<xsl:value-of select="@type"/>(elem.get<xsl:call-template name="name-initcap"/>());
        }
        </xsl:for-each> -->
        

        Map&lt;String, Object&gt; arrays = new LinkedHashMap&lt;String, Object&gt;();
        <xsl:for-each select="Array|Object">
        arrays.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>

        
 
        <xsl:if test="@class">
        populate<xsl:value-of select="@class"/>Arrays(elem, arrays);
        </xsl:if>

        <xsl:if test="CData">
        // Output the CData
        oneTag = false;
		out.println("&gt;");
		indent++;
		indentLine();
		indent--;
        out.println(elem.getText());
        </xsl:if>
		indent++;
	    for (Map.Entry&lt;String, Object&gt; array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List&lt;OLAPObject&gt; list = (List&lt;OLAPObject&gt;)array.getValue();
	           		if (oneTag &amp;&amp; list.size() > 0) {
	            		out.println("&gt;");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println("&gt;");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/&gt;");
        } else {
	       	indentLine();
	       	out.println("&lt;/<xsl:value-of select="@type"/>&gt;");
        }
    }
       
</xsl:template>

<xsl:template match="Class">

	private void write<xsl:value-of select="@class"/>(MondrianModel.<xsl:value-of select="@class"/> elem) {
	    foolishWrite(elem);
	}
	
    private void populate<xsl:value-of select="@class"/>Attributes(MondrianModel.<xsl:value-of select="@class"/> elem, Map&lt;String, Object&gt; atts) {
        <xsl:for-each select="Attribute">
        atts.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>
        <xsl:if test="@superclass">
        populate<xsl:value-of select="@superclass"/>Attributes(elem, atts);
        </xsl:if>
    }
    
    private void populate<xsl:value-of select="@class"/>Arrays(MondrianModel.<xsl:value-of select="@class"/> elem, Map&lt;String, Object&gt; arrays) {
        <xsl:for-each select="Array|Object">
        arrays.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>
        <xsl:if test="@superclass">
        populate<xsl:value-of select="@superclass"/>Arrays(elem, arrays);
        </xsl:if>
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
