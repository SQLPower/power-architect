
package ca.sqlpower.architect.olap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This is class is generated from xml-to-formatter.xsl!  Do not alter it directly.
 */
public class MondrianXMLWriter {

    private static final Logger logger = Logger.getLogger(MondrianXMLWriter.class);

    public static void write(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(f));
        write(out, schema, true, 0);
    }
    
    public static void writeXML(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(f));
        out.println("<?xml version=\"1.0\"?>");
        write(out, schema, true, 0);
    }

    public static void write(PrintWriter out, MondrianModel.Schema schema, boolean closeWriter, int indent) {
        MondrianXMLWriter writer = new MondrianXMLWriter(out);
        writer.indent = indent;
        writer.writeSchema(schema);
        out.flush();
        if (closeWriter) {
	        out.close();
	    }
    }
    
    private final PrintWriter out;
    
    private int indent;
    
    public MondrianXMLWriter(PrintWriter out) {
        this.out = out;
    }
    
    private void indentLine() {
    	for (int i = 0; i < indent; i++) {
    		out.print(" ");
    	}
    }

    private void writeStartTag(String elemName, Map<String, Object> atts) {
        indentLine();
        out.print("<" + elemName);
        for (Map.Entry<String, Object> att : atts.entrySet()) {
           if (att.getValue() != null) {
               out.print(" "+att.getKey()+"=\""+att.getValue()+"\""); 
           }
        }
    }
    
    private void foolishWrite(OLAPObject obj) {
    	if (false) {}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Schema.class)) {
     		writeSchema((MondrianModel.Schema)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Cube.class)) {
     		writeCube((MondrianModel.Cube)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.VirtualCube.class)) {
     		writeVirtualCube((MondrianModel.VirtualCube)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CubeUsages.class)) {
     		writeCubeUsages((MondrianModel.CubeUsages)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CubeUsage.class)) {
     		writeCubeUsage((MondrianModel.CubeUsage)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.VirtualCubeDimension.class)) {
     		writeVirtualCubeDimension((MondrianModel.VirtualCubeDimension)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.VirtualCubeMeasure.class)) {
     		writeVirtualCubeMeasure((MondrianModel.VirtualCubeMeasure)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.DimensionUsage.class)) {
     		writeDimensionUsage((MondrianModel.DimensionUsage)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Dimension.class)) {
     		writeDimension((MondrianModel.Dimension)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Hierarchy.class)) {
     		writeHierarchy((MondrianModel.Hierarchy)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Level.class)) {
     		writeLevel((MondrianModel.Level)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Closure.class)) {
     		writeClosure((MondrianModel.Closure)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Property.class)) {
     		writeProperty((MondrianModel.Property)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Measure.class)) {
     		writeMeasure((MondrianModel.Measure)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CalculatedMember.class)) {
     		writeCalculatedMember((MondrianModel.CalculatedMember)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CalculatedMemberProperty.class)) {
     		writeCalculatedMemberProperty((MondrianModel.CalculatedMemberProperty)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.NamedSet.class)) {
     		writeNamedSet((MondrianModel.NamedSet)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Formula.class)) {
     		writeFormula((MondrianModel.Formula)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.MemberReaderParameter.class)) {
     		writeMemberReaderParameter((MondrianModel.MemberReaderParameter)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.View.class)) {
     		writeView((MondrianModel.View)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.SQL.class)) {
     		writeSQL((MondrianModel.SQL)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Join.class)) {
     		writeJoin((MondrianModel.Join)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Table.class)) {
     		writeTable((MondrianModel.Table)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.InlineTable.class)) {
     		writeInlineTable((MondrianModel.InlineTable)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.ColumnDefs.class)) {
     		writeColumnDefs((MondrianModel.ColumnDefs)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.ColumnDef.class)) {
     		writeColumnDef((MondrianModel.ColumnDef)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Rows.class)) {
     		writeRows((MondrianModel.Rows)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Row.class)) {
     		writeRow((MondrianModel.Row)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Value.class)) {
     		writeValue((MondrianModel.Value)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggName.class)) {
     		writeAggName((MondrianModel.AggName)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggPattern.class)) {
     		writeAggPattern((MondrianModel.AggPattern)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggExclude.class)) {
     		writeAggExclude((MondrianModel.AggExclude)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggFactCount.class)) {
     		writeAggFactCount((MondrianModel.AggFactCount)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggIgnoreColumn.class)) {
     		writeAggIgnoreColumn((MondrianModel.AggIgnoreColumn)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggForeignKey.class)) {
     		writeAggForeignKey((MondrianModel.AggForeignKey)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggLevel.class)) {
     		writeAggLevel((MondrianModel.AggLevel)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggMeasure.class)) {
     		writeAggMeasure((MondrianModel.AggMeasure)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Column.class)) {
     		writeColumn((MondrianModel.Column)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.KeyExpression.class)) {
     		writeKeyExpression((MondrianModel.KeyExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.ParentExpression.class)) {
     		writeParentExpression((MondrianModel.ParentExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.OrdinalExpression.class)) {
     		writeOrdinalExpression((MondrianModel.OrdinalExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.NameExpression.class)) {
     		writeNameExpression((MondrianModel.NameExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CaptionExpression.class)) {
     		writeCaptionExpression((MondrianModel.CaptionExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.MeasureExpression.class)) {
     		writeMeasureExpression((MondrianModel.MeasureExpression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Role.class)) {
     		writeRole((MondrianModel.Role)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.SchemaGrant.class)) {
     		writeSchemaGrant((MondrianModel.SchemaGrant)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CubeGrant.class)) {
     		writeCubeGrant((MondrianModel.CubeGrant)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.DimensionGrant.class)) {
     		writeDimensionGrant((MondrianModel.DimensionGrant)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.HierarchyGrant.class)) {
     		writeHierarchyGrant((MondrianModel.HierarchyGrant)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.MemberGrant.class)) {
     		writeMemberGrant((MondrianModel.MemberGrant)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Union.class)) {
     		writeUnion((MondrianModel.Union)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.RoleUsage.class)) {
     		writeRoleUsage((MondrianModel.RoleUsage)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.UserDefinedFunction.class)) {
     		writeUserDefinedFunction((MondrianModel.UserDefinedFunction)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Parameter.class)) {
     		writeParameter((MondrianModel.Parameter)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.CubeDimension.class)) {
     		writeCubeDimension((MondrianModel.CubeDimension)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.RelationOrJoin.class)) {
     		writeRelationOrJoin((MondrianModel.RelationOrJoin)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Relation.class)) {
     		writeRelation((MondrianModel.Relation)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggTable.class)) {
     		writeAggTable((MondrianModel.AggTable)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.AggColumnName.class)) {
     		writeAggColumnName((MondrianModel.AggColumnName)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Expression.class)) {
     		writeExpression((MondrianModel.Expression)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.ExpressionView.class)) {
     		writeExpressionView((MondrianModel.ExpressionView)obj);
     	}
     	
     	else if ((obj.getClass()).equals(MondrianModel.Grant.class)) {
     		writeGrant((MondrianModel.Grant)obj);
     	}
     	
     	else {
     		logger.warn("Skipping unknown content \""+ obj.getClass()); 
     	}
    }


    public void writeSchema(MondrianModel.Schema elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("measuresCaption", elem.getMeasuresCaption());
        
        atts.put("defaultRole", elem.getDefaultRole());
        
        writeStartTag("Schema", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("parameters", elem.getParameters());
        
        arrays.put("dimensions", elem.getDimensions());
        
        arrays.put("cubes", elem.getCubes());
        
        arrays.put("virtualCubes", elem.getVirtualCubes());
        
        arrays.put("namedSets", elem.getNamedSets());
        
        arrays.put("roles", elem.getRoles());
        
        arrays.put("userDefinedFunctions", elem.getUserDefinedFunctions());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Schema>");
        }
    }
       


	private void writeCubeDimension(MondrianModel.CubeDimension elem) {
	    foolishWrite(elem);
	}
	
    private void populateCubeDimensionAttributes(MondrianModel.CubeDimension elem, Map<String, Object> atts) {
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("foreignKey", elem.getForeignKey());
        
    }
    
    private void populateCubeDimensionArrays(MondrianModel.CubeDimension elem, Map<String, Object> arrays) {
        
    }


    public void writeCube(MondrianModel.Cube elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("defaultMeasure", elem.getDefaultMeasure());
        
        atts.put("cache", elem.getCache());
        
        atts.put("enabled", elem.getEnabled());
        
        writeStartTag("Cube", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("fact", elem.getFact());
        
        arrays.put("dimensions", elem.getDimensions());
        
        arrays.put("measures", elem.getMeasures());
        
        arrays.put("calculatedMembers", elem.getCalculatedMembers());
        
        arrays.put("namedSets", elem.getNamedSets());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Cube>");
        }
    }
       


    public void writeVirtualCube(MondrianModel.VirtualCube elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("enabled", elem.getEnabled());
        
        atts.put("name", elem.getName());
        
        atts.put("defaultMeasure", elem.getDefaultMeasure());
        
        atts.put("caption", elem.getCaption());
        
        writeStartTag("VirtualCube", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("cubeUsage", elem.getCubeUsage());
        
        arrays.put("dimensions", elem.getDimensions());
        
        arrays.put("measures", elem.getMeasures());
        
        arrays.put("calculatedMembers", elem.getCalculatedMembers());
        
        arrays.put("namedSets", elem.getNamedSets());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</VirtualCube>");
        }
    }
       


    public void writeCubeUsages(MondrianModel.CubeUsages elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("CubeUsages", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("cubeUsages", elem.getCubeUsages());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CubeUsages>");
        }
    }
       


    public void writeCubeUsage(MondrianModel.CubeUsage elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("ignoreUnrelatedDimensions", elem.getIgnoreUnrelatedDimensions());
        
        writeStartTag("CubeUsage", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CubeUsage>");
        }
    }
       


    public void writeVirtualCubeDimension(MondrianModel.VirtualCubeDimension elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("name", elem.getName());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("VirtualCubeDimension", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateCubeDimensionArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</VirtualCubeDimension>");
        }
    }
       


    public void writeVirtualCubeMeasure(MondrianModel.VirtualCubeMeasure elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("name", elem.getName());
        
        atts.put("visible", elem.getVisible());
        
        writeStartTag("VirtualCubeMeasure", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</VirtualCubeMeasure>");
        }
    }
       


    public void writeDimensionUsage(MondrianModel.DimensionUsage elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("source", elem.getSource());
        
        atts.put("level", elem.getLevel());
        
        atts.put("usagePrefix", elem.getUsagePrefix());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("DimensionUsage", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateCubeDimensionArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</DimensionUsage>");
        }
    }
       


    public void writeDimension(MondrianModel.Dimension elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("type", elem.getType());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("usagePrefix", elem.getUsagePrefix());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("Dimension", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("hierarchies", elem.getHierarchies());
        
        populateCubeDimensionArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Dimension>");
        }
    }
       


    public void writeHierarchy(MondrianModel.Hierarchy elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("hasAll", elem.getHasAll());
        
        atts.put("allMemberName", elem.getAllMemberName());
        
        atts.put("allMemberCaption", elem.getAllMemberCaption());
        
        atts.put("allLevelName", elem.getAllLevelName());
        
        atts.put("primaryKey", elem.getPrimaryKey());
        
        atts.put("primaryKeyTable", elem.getPrimaryKeyTable());
        
        atts.put("defaultMember", elem.getDefaultMember());
        
        atts.put("memberReaderClass", elem.getMemberReaderClass());
        
        atts.put("caption", elem.getCaption());
        
        writeStartTag("Hierarchy", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("relation", elem.getRelation());
        
        arrays.put("levels", elem.getLevels());
        
        arrays.put("memberReaderParameters", elem.getMemberReaderParameters());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Hierarchy>");
        }
    }
       


    public void writeLevel(MondrianModel.Level elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("approxRowCount", elem.getApproxRowCount());
        
        atts.put("name", elem.getName());
        
        atts.put("table", elem.getTable());
        
        atts.put("column", elem.getColumn());
        
        atts.put("nameColumn", elem.getNameColumn());
        
        atts.put("ordinalColumn", elem.getOrdinalColumn());
        
        atts.put("parentColumn", elem.getParentColumn());
        
        atts.put("nullParentValue", elem.getNullParentValue());
        
        atts.put("type", elem.getType());
        
        atts.put("uniqueMembers", elem.getUniqueMembers());
        
        atts.put("levelType", elem.getLevelType());
        
        atts.put("hideMemberIf", elem.getHideMemberIf());
        
        atts.put("formatter", elem.getFormatter());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("captionColumn", elem.getCaptionColumn());
        
        writeStartTag("Level", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("keyExp", elem.getKeyExp());
        
        arrays.put("nameExp", elem.getNameExp());
        
        arrays.put("ordinalExp", elem.getOrdinalExp());
        
        arrays.put("parentExp", elem.getParentExp());
        
        arrays.put("closure", elem.getClosure());
        
        arrays.put("properties", elem.getProperties());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Level>");
        }
    }
       


    public void writeClosure(MondrianModel.Closure elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("parentColumn", elem.getParentColumn());
        
        atts.put("childColumn", elem.getChildColumn());
        
        writeStartTag("Closure", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("table", elem.getTable());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Closure>");
        }
    }
       


    public void writeProperty(MondrianModel.Property elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("column", elem.getColumn());
        
        atts.put("type", elem.getType());
        
        atts.put("formatter", elem.getFormatter());
        
        atts.put("caption", elem.getCaption());
        
        writeStartTag("Property", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Property>");
        }
    }
       


    public void writeMeasure(MondrianModel.Measure elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("column", elem.getColumn());
        
        atts.put("datatype", elem.getDatatype());
        
        atts.put("formatString", elem.getFormatString());
        
        atts.put("aggregator", elem.getAggregator());
        
        atts.put("formatter", elem.getFormatter());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("visible", elem.getVisible());
        
        writeStartTag("Measure", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("measureExp", elem.getMeasureExp());
        
        arrays.put("memberProperties", elem.getMemberProperties());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Measure>");
        }
    }
       


    public void writeCalculatedMember(MondrianModel.CalculatedMember elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("formatString", elem.getFormatString());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("formula", elem.getFormula());
        
        atts.put("dimension", elem.getDimension());
        
        atts.put("visible", elem.getVisible());
        
        writeStartTag("CalculatedMember", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("formulaElement", elem.getFormulaElement());
        
        arrays.put("memberProperties", elem.getMemberProperties());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CalculatedMember>");
        }
    }
       


    public void writeCalculatedMemberProperty(MondrianModel.CalculatedMemberProperty elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("expression", elem.getExpression());
        
        atts.put("value", elem.getValue());
        
        writeStartTag("CalculatedMemberProperty", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CalculatedMemberProperty>");
        }
    }
       


    public void writeNamedSet(MondrianModel.NamedSet elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("formula", elem.getFormula());
        
        writeStartTag("NamedSet", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("formulaElement", elem.getFormulaElement());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</NamedSet>");
        }
    }
       


    public void writeFormula(MondrianModel.Formula elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Formula", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        // Output the CData
        oneTag = false;
		out.println(">");
		indent++;
		indentLine();
		indent--;
        out.println(elem.getText());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Formula>");
        }
    }
       


    public void writeMemberReaderParameter(MondrianModel.MemberReaderParameter elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("value", elem.getValue());
        
        writeStartTag("MemberReaderParameter", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</MemberReaderParameter>");
        }
    }
       


	private void writeRelationOrJoin(MondrianModel.RelationOrJoin elem) {
	    foolishWrite(elem);
	}
	
    private void populateRelationOrJoinAttributes(MondrianModel.RelationOrJoin elem, Map<String, Object> atts) {
        
    }
    
    private void populateRelationOrJoinArrays(MondrianModel.RelationOrJoin elem, Map<String, Object> arrays) {
        
    }


	private void writeRelation(MondrianModel.Relation elem) {
	    foolishWrite(elem);
	}
	
    private void populateRelationAttributes(MondrianModel.Relation elem, Map<String, Object> atts) {
        
        populateRelationOrJoinAttributes(elem, atts);
        
    }
    
    private void populateRelationArrays(MondrianModel.Relation elem, Map<String, Object> arrays) {
        
        populateRelationOrJoinArrays(elem, arrays);
        
    }


    public void writeView(MondrianModel.View elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("View", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("selects", elem.getSelects());
        
        populateRelationArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</View>");
        }
    }
       


    public void writeSQL(MondrianModel.SQL elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("dialect", elem.getDialect());
        
        writeStartTag("SQL", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        // Output the CData
        oneTag = false;
		out.println(">");
		indent++;
		indentLine();
		indent--;
        out.println(elem.getText());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</SQL>");
        }
    }
       


    public void writeJoin(MondrianModel.Join elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("leftAlias", elem.getLeftAlias());
        
        atts.put("leftKey", elem.getLeftKey());
        
        atts.put("rightAlias", elem.getRightAlias());
        
        atts.put("rightKey", elem.getRightKey());
        
        populateRelationOrJoinAttributes(elem, atts);
        
        writeStartTag("Join", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("left", elem.getLeft());
        
        arrays.put("right", elem.getRight());
        
        populateRelationOrJoinArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Join>");
        }
    }
       


    public void writeTable(MondrianModel.Table elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("schema", elem.getSchema());
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("Table", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("filter", elem.getFilter());
        
        arrays.put("aggExcludes", elem.getAggExcludes());
        
        arrays.put("aggTables", elem.getAggTables());
        
        populateRelationArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Table>");
        }
    }
       


    public void writeInlineTable(MondrianModel.InlineTable elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("InlineTable", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("columnDefs", elem.getColumnDefs());
        
        arrays.put("rows", elem.getRows());
        
        populateRelationArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</InlineTable>");
        }
    }
       


    public void writeColumnDefs(MondrianModel.ColumnDefs elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("ColumnDefs", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("array", elem.getArray());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</ColumnDefs>");
        }
    }
       


    public void writeColumnDef(MondrianModel.ColumnDef elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("type", elem.getType());
        
        writeStartTag("ColumnDef", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</ColumnDef>");
        }
    }
       


    public void writeRows(MondrianModel.Rows elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Rows", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("array", elem.getArray());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Rows>");
        }
    }
       


    public void writeRow(MondrianModel.Row elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Row", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("values", elem.getValues());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Row>");
        }
    }
       


    public void writeValue(MondrianModel.Value elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        writeStartTag("Value", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        // Output the CData
        oneTag = false;
		out.println(">");
		indent++;
		indentLine();
		indent--;
        out.println(elem.getText());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Value>");
        }
    }
       


	private void writeAggTable(MondrianModel.AggTable elem) {
	    foolishWrite(elem);
	}
	
    private void populateAggTableAttributes(MondrianModel.AggTable elem, Map<String, Object> atts) {
        
        atts.put("ignorecase", elem.getIgnorecase());
        
    }
    
    private void populateAggTableArrays(MondrianModel.AggTable elem, Map<String, Object> arrays) {
        
        arrays.put("factcount", elem.getFactcount());
        
        arrays.put("ignoreColumns", elem.getIgnoreColumns());
        
        arrays.put("foreignKeys", elem.getForeignKeys());
        
        arrays.put("measures", elem.getMeasures());
        
        arrays.put("levels", elem.getLevels());
        
    }


    public void writeAggName(MondrianModel.AggName elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        populateAggTableAttributes(elem, atts);
        
        writeStartTag("AggName", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateAggTableArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggName>");
        }
    }
       


    public void writeAggPattern(MondrianModel.AggPattern elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("pattern", elem.getPattern());
        
        populateAggTableAttributes(elem, atts);
        
        writeStartTag("AggPattern", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("excludes", elem.getExcludes());
        
        populateAggTableArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggPattern>");
        }
    }
       


    public void writeAggExclude(MondrianModel.AggExclude elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("pattern", elem.getPattern());
        
        atts.put("name", elem.getName());
        
        atts.put("ignorecase", elem.getIgnorecase());
        
        writeStartTag("AggExclude", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggExclude>");
        }
    }
       


	private void writeAggColumnName(MondrianModel.AggColumnName elem) {
	    foolishWrite(elem);
	}
	
    private void populateAggColumnNameAttributes(MondrianModel.AggColumnName elem, Map<String, Object> atts) {
        
        atts.put("column", elem.getColumn());
        
    }
    
    private void populateAggColumnNameArrays(MondrianModel.AggColumnName elem, Map<String, Object> arrays) {
        
    }


    public void writeAggFactCount(MondrianModel.AggFactCount elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameAttributes(elem, atts);
        
        writeStartTag("AggFactCount", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggFactCount>");
        }
    }
       


    public void writeAggIgnoreColumn(MondrianModel.AggIgnoreColumn elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameAttributes(elem, atts);
        
        writeStartTag("AggIgnoreColumn", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggIgnoreColumn>");
        }
    }
       


    public void writeAggForeignKey(MondrianModel.AggForeignKey elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("factColumn", elem.getFactColumn());
        
        atts.put("aggColumn", elem.getAggColumn());
        
        writeStartTag("AggForeignKey", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggForeignKey>");
        }
    }
       


    public void writeAggLevel(MondrianModel.AggLevel elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        atts.put("name", elem.getName());
        
        writeStartTag("AggLevel", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggLevel>");
        }
    }
       


    public void writeAggMeasure(MondrianModel.AggMeasure elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        atts.put("name", elem.getName());
        
        writeStartTag("AggMeasure", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</AggMeasure>");
        }
    }
       


	private void writeExpression(MondrianModel.Expression elem) {
	    foolishWrite(elem);
	}
	
    private void populateExpressionAttributes(MondrianModel.Expression elem, Map<String, Object> atts) {
        
    }
    
    private void populateExpressionArrays(MondrianModel.Expression elem, Map<String, Object> arrays) {
        
    }


    public void writeColumn(MondrianModel.Column elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("table", elem.getTable());
        
        atts.put("name", elem.getName());
        
        populateExpressionAttributes(elem, atts);
        
        writeStartTag("Column", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Column>");
        }
    }
       


	private void writeExpressionView(MondrianModel.ExpressionView elem) {
	    foolishWrite(elem);
	}
	
    private void populateExpressionViewAttributes(MondrianModel.ExpressionView elem, Map<String, Object> atts) {
        
        populateExpressionAttributes(elem, atts);
        
    }
    
    private void populateExpressionViewArrays(MondrianModel.ExpressionView elem, Map<String, Object> arrays) {
        
        arrays.put("expressions", elem.getExpressions());
        
        populateExpressionArrays(elem, arrays);
        
    }


    public void writeKeyExpression(MondrianModel.KeyExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("KeyExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</KeyExpression>");
        }
    }
       


    public void writeParentExpression(MondrianModel.ParentExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("ParentExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</ParentExpression>");
        }
    }
       


    public void writeOrdinalExpression(MondrianModel.OrdinalExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("OrdinalExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</OrdinalExpression>");
        }
    }
       


    public void writeNameExpression(MondrianModel.NameExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("NameExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</NameExpression>");
        }
    }
       


    public void writeCaptionExpression(MondrianModel.CaptionExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("CaptionExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CaptionExpression>");
        }
    }
       


    public void writeMeasureExpression(MondrianModel.MeasureExpression elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("MeasureExpression", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateExpressionViewArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</MeasureExpression>");
        }
    }
       


    public void writeRole(MondrianModel.Role elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        writeStartTag("Role", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("schemaGrants", elem.getSchemaGrants());
        
        arrays.put("union", elem.getUnion());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Role>");
        }
    }
       


	private void writeGrant(MondrianModel.Grant elem) {
	    foolishWrite(elem);
	}
	
    private void populateGrantAttributes(MondrianModel.Grant elem, Map<String, Object> atts) {
        
        atts.put("access", elem.getAccess());
        
    }
    
    private void populateGrantArrays(MondrianModel.Grant elem, Map<String, Object> arrays) {
        
    }


    public void writeSchemaGrant(MondrianModel.SchemaGrant elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("SchemaGrant", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("cubeGrants", elem.getCubeGrants());
        
        populateGrantArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</SchemaGrant>");
        }
    }
       


    public void writeCubeGrant(MondrianModel.CubeGrant elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cube", elem.getCube());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("CubeGrant", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("dimensionGrants", elem.getDimensionGrants());
        
        arrays.put("hierarchyGrants", elem.getHierarchyGrants());
        
        populateGrantArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</CubeGrant>");
        }
    }
       


    public void writeDimensionGrant(MondrianModel.DimensionGrant elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("dimension", elem.getDimension());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("DimensionGrant", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        populateGrantArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</DimensionGrant>");
        }
    }
       


    public void writeHierarchyGrant(MondrianModel.HierarchyGrant elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("hierarchy", elem.getHierarchy());
        
        atts.put("topLevel", elem.getTopLevel());
        
        atts.put("bottomLevel", elem.getBottomLevel());
        
        atts.put("rollupPolicy", elem.getRollupPolicy());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("HierarchyGrant", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("memberGrants", elem.getMemberGrants());
        
        populateGrantArrays(elem, arrays);
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</HierarchyGrant>");
        }
    }
       


    public void writeMemberGrant(MondrianModel.MemberGrant elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("member", elem.getMember());
        
        atts.put("access", elem.getAccess());
        
        writeStartTag("MemberGrant", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</MemberGrant>");
        }
    }
       


    public void writeUnion(MondrianModel.Union elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Union", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
        arrays.put("roleUsages", elem.getRoleUsages());
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Union>");
        }
    }
       


    public void writeRoleUsage(MondrianModel.RoleUsage elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("roleName", elem.getRoleName());
        
        writeStartTag("RoleUsage", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</RoleUsage>");
        }
    }
       


    public void writeUserDefinedFunction(MondrianModel.UserDefinedFunction elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("className", elem.getClassName());
        
        writeStartTag("UserDefinedFunction", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</UserDefinedFunction>");
        }
    }
       


    public void writeParameter(MondrianModel.Parameter elem) {

		boolean oneTag = true;
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("description", elem.getDescription());
        
        atts.put("type", elem.getType());
        
        atts.put("modifiable", elem.getModifiable());
        
        atts.put("defaultValue", elem.getDefaultValue());
        
        writeStartTag("Parameter", atts);
        
        
        

        Map<String, Object> arrays = new LinkedHashMap<String, Object>();
        
		indent++;
	    for (Map.Entry<String, Object> array : arrays.entrySet()) {
	      		if (array.getValue() instanceof List) {
	               	List<OLAPObject> list = (List<OLAPObject>)array.getValue();
	           		if (oneTag && list.size() > 0) {
	            		out.println(">");
	            		oneTag = false;
	           		}
            		for (OLAPObject obj : list) {
	       		    	foolishWrite(obj);
		            }
	            } else if (array.getValue() instanceof OLAPObject) {
	            	if (oneTag) {
	            		out.println(">");
	            		oneTag = false;
	           		}
	               	foolishWrite((OLAPObject)array.getValue());
                }
        }
       	indent--;
        if (oneTag) {
        	out.println("/>");
        } else {
	       	indentLine();
	       	out.println("</Parameter>");
        }
    }
       

}
