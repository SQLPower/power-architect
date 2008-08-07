
package ca.sqlpower.architect.olap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MondrianXMLWriter {

    private static final Logger logger = Logger.getLogger(MondrianXMLWriter.class);

    public static void write(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(f));
        write(out, schema);
        out.close();
    }

    public static void write(PrintWriter out, MondrianModel.Schema schema) {
        MondrianXMLWriter writer = new MondrianXMLWriter(out);
        writer.writeSchema(schema);
        out.flush();
    }
    
    private final PrintWriter out;
    
    private int indent;
    
    public MondrianXMLWriter(PrintWriter out) {
        this.out = out;
    }

    private void writeStartTag(String elemName, Map<String, Object> atts) {
        out.print("<" + elemName);
        for (Map.Entry<String, Object> att : atts.entrySet()) {
           if (att.getValue() != null) {
               out.print(" "+att.getKey()+"=\""+att.getValue()+"\""); 
           }
        }
        out.println(">");
    }


    public void writeSchema(MondrianModel.Schema elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("measuresCaption", elem.getMeasuresCaption());
        
        atts.put("defaultRole", elem.getDefaultRole());
        
        writeStartTag("Schema", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Schema>");
    }
       

    private void populateCubeDimensionAttributes(MondrianModel.CubeDimension elem, Map<String, Object> atts) {
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("foreignKey", elem.getForeignKey());
        
    }


    public void writeCube(MondrianModel.Cube elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("defaultMeasure", elem.getDefaultMeasure());
        
        atts.put("cache", elem.getCache());
        
        atts.put("enabled", elem.getEnabled());
        
        writeStartTag("Cube", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Cube>");
    }
       


    public void writeVirtualCube(MondrianModel.VirtualCube elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("enabled", elem.getEnabled());
        
        atts.put("name", elem.getName());
        
        atts.put("defaultMeasure", elem.getDefaultMeasure());
        
        atts.put("caption", elem.getCaption());
        
        writeStartTag("VirtualCube", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</VirtualCube>");
    }
       


    public void writeCubeUsages(MondrianModel.CubeUsages elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("CubeUsages", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CubeUsages>");
    }
       


    public void writeCubeUsage(MondrianModel.CubeUsage elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("ignoreUnrelatedDimensions", elem.getIgnoreUnrelatedDimensions());
        
        writeStartTag("CubeUsage", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CubeUsage>");
    }
       


    public void writeVirtualCubeDimension(MondrianModel.VirtualCubeDimension elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("name", elem.getName());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("VirtualCubeDimension", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</VirtualCubeDimension>");
    }
       


    public void writeVirtualCubeMeasure(MondrianModel.VirtualCubeMeasure elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cubeName", elem.getCubeName());
        
        atts.put("name", elem.getName());
        
        atts.put("visible", elem.getVisible());
        
        writeStartTag("VirtualCubeMeasure", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</VirtualCubeMeasure>");
    }
       


    public void writeDimensionUsage(MondrianModel.DimensionUsage elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("source", elem.getSource());
        
        atts.put("level", elem.getLevel());
        
        atts.put("usagePrefix", elem.getUsagePrefix());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("DimensionUsage", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</DimensionUsage>");
    }
       


    public void writeDimension(MondrianModel.Dimension elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("type", elem.getType());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("usagePrefix", elem.getUsagePrefix());
        
        populateCubeDimensionAttributes(elem, atts);
        
        writeStartTag("Dimension", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Dimension>");
    }
       


    public void writeHierarchy(MondrianModel.Hierarchy elem) {
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
        
        indent++;
        // content here
        indent--;
        
        out.println("</Hierarchy>");
    }
       


    public void writeLevel(MondrianModel.Level elem) {
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
        
        indent++;
        // content here
        indent--;
        
        out.println("</Level>");
    }
       


    public void writeClosure(MondrianModel.Closure elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("parentColumn", elem.getParentColumn());
        
        atts.put("childColumn", elem.getChildColumn());
        
        writeStartTag("Closure", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Closure>");
    }
       


    public void writeProperty(MondrianModel.Property elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("column", elem.getColumn());
        
        atts.put("type", elem.getType());
        
        atts.put("formatter", elem.getFormatter());
        
        atts.put("caption", elem.getCaption());
        
        writeStartTag("Property", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Property>");
    }
       


    public void writeMeasure(MondrianModel.Measure elem) {
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
        
        indent++;
        // content here
        indent--;
        
        out.println("</Measure>");
    }
       


    public void writeCalculatedMember(MondrianModel.CalculatedMember elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("formatString", elem.getFormatString());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("formula", elem.getFormula());
        
        atts.put("dimension", elem.getDimension());
        
        atts.put("visible", elem.getVisible());
        
        writeStartTag("CalculatedMember", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CalculatedMember>");
    }
       


    public void writeCalculatedMemberProperty(MondrianModel.CalculatedMemberProperty elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("caption", elem.getCaption());
        
        atts.put("expression", elem.getExpression());
        
        atts.put("value", elem.getValue());
        
        writeStartTag("CalculatedMemberProperty", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CalculatedMemberProperty>");
    }
       


    public void writeNamedSet(MondrianModel.NamedSet elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("formula", elem.getFormula());
        
        writeStartTag("NamedSet", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</NamedSet>");
    }
       


    public void writeFormula(MondrianModel.Formula elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Formula", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Formula>");
    }
       


    public void writeMemberReaderParameter(MondrianModel.MemberReaderParameter elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("value", elem.getValue());
        
        writeStartTag("MemberReaderParameter", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</MemberReaderParameter>");
    }
       

    private void populateRelationOrJoinAttributes(MondrianModel.RelationOrJoin elem, Map<String, Object> atts) {
        
    }

    private void populateRelationAttributes(MondrianModel.Relation elem, Map<String, Object> atts) {
        
        populateRelationOrJoinAttributes(elem, atts);
        
    }


    public void writeView(MondrianModel.View elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("View", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</View>");
    }
       


    public void writeSQL(MondrianModel.SQL elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("dialect", elem.getDialect());
        
        writeStartTag("SQL", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</SQL>");
    }
       


    public void writeJoin(MondrianModel.Join elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("leftAlias", elem.getLeftAlias());
        
        atts.put("leftKey", elem.getLeftKey());
        
        atts.put("rightAlias", elem.getRightAlias());
        
        atts.put("rightKey", elem.getRightKey());
        
        populateRelationOrJoinAttributes(elem, atts);
        
        writeStartTag("Join", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Join>");
    }
       


    public void writeTable(MondrianModel.Table elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("schema", elem.getSchema());
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("Table", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Table>");
    }
       


    public void writeInlineTable(MondrianModel.InlineTable elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("alias", elem.getAlias());
        
        populateRelationAttributes(elem, atts);
        
        writeStartTag("InlineTable", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</InlineTable>");
    }
       


    public void writeColumnDefs(MondrianModel.ColumnDefs elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("ColumnDefs", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</ColumnDefs>");
    }
       


    public void writeColumnDef(MondrianModel.ColumnDef elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("type", elem.getType());
        
        writeStartTag("ColumnDef", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</ColumnDef>");
    }
       


    public void writeRows(MondrianModel.Rows elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Rows", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Rows>");
    }
       


    public void writeRow(MondrianModel.Row elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Row", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Row>");
    }
       


    public void writeValue(MondrianModel.Value elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        writeStartTag("Value", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Value>");
    }
       

    private void populateAggTableAttributes(MondrianModel.AggTable elem, Map<String, Object> atts) {
        
        atts.put("ignorecase", elem.getIgnorecase());
        
    }


    public void writeAggName(MondrianModel.AggName elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        populateAggTableAttributes(elem, atts);
        
        writeStartTag("AggName", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggName>");
    }
       


    public void writeAggPattern(MondrianModel.AggPattern elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("pattern", elem.getPattern());
        
        populateAggTableAttributes(elem, atts);
        
        writeStartTag("AggPattern", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggPattern>");
    }
       


    public void writeAggExclude(MondrianModel.AggExclude elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("pattern", elem.getPattern());
        
        atts.put("name", elem.getName());
        
        atts.put("ignorecase", elem.getIgnorecase());
        
        writeStartTag("AggExclude", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggExclude>");
    }
       

    private void populateAggColumnNameAttributes(MondrianModel.AggColumnName elem, Map<String, Object> atts) {
        
        atts.put("column", elem.getColumn());
        
    }


    public void writeAggFactCount(MondrianModel.AggFactCount elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameAttributes(elem, atts);
        
        writeStartTag("AggFactCount", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggFactCount>");
    }
       


    public void writeAggIgnoreColumn(MondrianModel.AggIgnoreColumn elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateAggColumnNameAttributes(elem, atts);
        
        writeStartTag("AggIgnoreColumn", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggIgnoreColumn>");
    }
       


    public void writeAggForeignKey(MondrianModel.AggForeignKey elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("factColumn", elem.getFactColumn());
        
        atts.put("aggColumn", elem.getAggColumn());
        
        writeStartTag("AggForeignKey", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggForeignKey>");
    }
       


    public void writeAggLevel(MondrianModel.AggLevel elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        atts.put("name", elem.getName());
        
        writeStartTag("AggLevel", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggLevel>");
    }
       


    public void writeAggMeasure(MondrianModel.AggMeasure elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("column", elem.getColumn());
        
        atts.put("name", elem.getName());
        
        writeStartTag("AggMeasure", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</AggMeasure>");
    }
       

    private void populateExpressionAttributes(MondrianModel.Expression elem, Map<String, Object> atts) {
        
    }


    public void writeColumn(MondrianModel.Column elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("table", elem.getTable());
        
        atts.put("name", elem.getName());
        
        populateExpressionAttributes(elem, atts);
        
        writeStartTag("Column", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Column>");
    }
       

    private void populateExpressionViewAttributes(MondrianModel.ExpressionView elem, Map<String, Object> atts) {
        
        populateExpressionAttributes(elem, atts);
        
    }


    public void writeKeyExpression(MondrianModel.KeyExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("KeyExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</KeyExpression>");
    }
       


    public void writeParentExpression(MondrianModel.ParentExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("ParentExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</ParentExpression>");
    }
       


    public void writeOrdinalExpression(MondrianModel.OrdinalExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("OrdinalExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</OrdinalExpression>");
    }
       


    public void writeNameExpression(MondrianModel.NameExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("NameExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</NameExpression>");
    }
       


    public void writeCaptionExpression(MondrianModel.CaptionExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("CaptionExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CaptionExpression>");
    }
       


    public void writeMeasureExpression(MondrianModel.MeasureExpression elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateExpressionViewAttributes(elem, atts);
        
        writeStartTag("MeasureExpression", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</MeasureExpression>");
    }
       


    public void writeRole(MondrianModel.Role elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        writeStartTag("Role", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Role>");
    }
       

    private void populateGrantAttributes(MondrianModel.Grant elem, Map<String, Object> atts) {
        
        atts.put("access", elem.getAccess());
        
    }


    public void writeSchemaGrant(MondrianModel.SchemaGrant elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("SchemaGrant", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</SchemaGrant>");
    }
       


    public void writeCubeGrant(MondrianModel.CubeGrant elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("cube", elem.getCube());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("CubeGrant", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</CubeGrant>");
    }
       


    public void writeDimensionGrant(MondrianModel.DimensionGrant elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("dimension", elem.getDimension());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("DimensionGrant", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</DimensionGrant>");
    }
       


    public void writeHierarchyGrant(MondrianModel.HierarchyGrant elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("hierarchy", elem.getHierarchy());
        
        atts.put("topLevel", elem.getTopLevel());
        
        atts.put("bottomLevel", elem.getBottomLevel());
        
        atts.put("rollupPolicy", elem.getRollupPolicy());
        
        populateGrantAttributes(elem, atts);
        
        writeStartTag("HierarchyGrant", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</HierarchyGrant>");
    }
       


    public void writeMemberGrant(MondrianModel.MemberGrant elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("member", elem.getMember());
        
        atts.put("access", elem.getAccess());
        
        writeStartTag("MemberGrant", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</MemberGrant>");
    }
       


    public void writeUnion(MondrianModel.Union elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        writeStartTag("Union", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Union>");
    }
       


    public void writeRoleUsage(MondrianModel.RoleUsage elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("roleName", elem.getRoleName());
        
        writeStartTag("RoleUsage", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</RoleUsage>");
    }
       


    public void writeUserDefinedFunction(MondrianModel.UserDefinedFunction elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("className", elem.getClassName());
        
        writeStartTag("UserDefinedFunction", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</UserDefinedFunction>");
    }
       


    public void writeParameter(MondrianModel.Parameter elem) {
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        
        atts.put("name", elem.getName());
        
        atts.put("description", elem.getDescription());
        
        atts.put("type", elem.getType());
        
        atts.put("modifiable", elem.getModifiable());
        
        atts.put("defaultValue", elem.getDefaultValue());
        
        writeStartTag("Parameter", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("</Parameter>");
    }
       

}
