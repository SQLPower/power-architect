
package ca.sqlpower.architect.util;


import ca.sqlpower.architect.olap.MondrianModel.*;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.GenericNewValueMaker;


/**
 * This is class is generated from xml-to-java-classes.xsl!  Do not alter it directly.
 */
public class MondrianNewValueMaker extends GenericNewValueMaker {

    public MondrianNewValueMaker(final SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        super(root, dsCollection);
    }
    
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        if (valueType == Schema.class) {
            Schema schema = new Schema();
            getRootObject().addChild(schema, 0);
            return schema;
        } else

    if (valueType == CubeDimension.class) {
        
        return makeNewValue(VirtualCubeDimension.class, oldVal, propName);
    } else

    if (valueType == Cube.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of Cube");
        Cube agg = new Cube();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == VirtualCube.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of VirtualCube");
        VirtualCube agg = new VirtualCube();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == CubeUsages.class) {
        VirtualCube parent = (VirtualCube) makeNewValue(VirtualCube.class, null, "Parent of CubeUsages");
        CubeUsages agg = new CubeUsages();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == CubeUsage.class) {
        CubeUsages parent = (CubeUsages) makeNewValue(CubeUsages.class, null, "Parent of CubeUsage");
        CubeUsage agg = new CubeUsage();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == VirtualCubeDimension.class) {
        VirtualCube parent = (VirtualCube) makeNewValue(VirtualCube.class, null, "Parent of VirtualCubeDimension");
        VirtualCubeDimension agg = new VirtualCubeDimension();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == VirtualCubeMeasure.class) {
        VirtualCube parent = (VirtualCube) makeNewValue(VirtualCube.class, null, "Parent of VirtualCubeMeasure");
        VirtualCubeMeasure agg = new VirtualCubeMeasure();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == DimensionUsage.class) {
        Cube parent = (Cube) makeNewValue(Cube.class, null, "Parent of CubeDimension");
        DimensionUsage agg = new DimensionUsage();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Dimension.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of Dimension");
        Dimension agg = new Dimension();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Hierarchy.class) {
        Dimension parent = (Dimension) makeNewValue(Dimension.class, null, "Parent of Hierarchy");
        Hierarchy agg = new Hierarchy();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Level.class) {
        Hierarchy parent = (Hierarchy) makeNewValue(Hierarchy.class, null, "Parent of Level");
        Level agg = new Level();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Closure.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of Closure");
        Closure agg = new Closure();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Property.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of Property");
        Property agg = new Property();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Measure.class) {
        Cube parent = (Cube) makeNewValue(Cube.class, null, "Parent of Measure");
        Measure agg = new Measure();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == CalculatedMember.class) {
        Cube parent = (Cube) makeNewValue(Cube.class, null, "Parent of CalculatedMember");
        CalculatedMember agg = new CalculatedMember();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == CalculatedMemberProperty.class) {
        Measure parent = (Measure) makeNewValue(Measure.class, null, "Parent of CalculatedMemberProperty");
        CalculatedMemberProperty agg = new CalculatedMemberProperty();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == NamedSet.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of NamedSet");
        NamedSet agg = new NamedSet();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Formula.class) {
        CalculatedMember parent = (CalculatedMember) makeNewValue(CalculatedMember.class, null, "Parent of Formula");
        Formula agg = new Formula();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == MemberReaderParameter.class) {
        Hierarchy parent = (Hierarchy) makeNewValue(Hierarchy.class, null, "Parent of MemberReaderParameter");
        MemberReaderParameter agg = new MemberReaderParameter();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == RelationOrJoin.class) {
        
        return makeNewValue(Join.class, oldVal, propName);
    } else

    if (valueType == Relation.class) {
        
        return makeNewValue(View.class, oldVal, propName);
    } else

    if (valueType == View.class) {
        Cube parent = (Cube) makeNewValue(Cube.class, null, "Parent of Relation");
        View agg = new View();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == SQL.class) {
        View parent = (View) makeNewValue(View.class, null, "Parent of SQL");
        SQL agg = new SQL();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Join.class) {
        Hierarchy parent = (Hierarchy) makeNewValue(Hierarchy.class, null, "Parent of RelationOrJoin");
        Join agg = new Join();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Table.class) {
        Closure parent = (Closure) makeNewValue(Closure.class, null, "Parent of Table");
        Table agg = new Table();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == InlineTable.class) {
        Cube parent = (Cube) makeNewValue(Cube.class, null, "Parent of Relation");
        InlineTable agg = new InlineTable();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == ColumnDefs.class) {
        InlineTable parent = (InlineTable) makeNewValue(InlineTable.class, null, "Parent of ColumnDefs");
        ColumnDefs agg = new ColumnDefs();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == ColumnDef.class) {
        ColumnDefs parent = (ColumnDefs) makeNewValue(ColumnDefs.class, null, "Parent of ColumnDef");
        ColumnDef agg = new ColumnDef();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Rows.class) {
        InlineTable parent = (InlineTable) makeNewValue(InlineTable.class, null, "Parent of Rows");
        Rows agg = new Rows();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Row.class) {
        Rows parent = (Rows) makeNewValue(Rows.class, null, "Parent of Row");
        Row agg = new Row();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Value.class) {
        Row parent = (Row) makeNewValue(Row.class, null, "Parent of Value");
        Value agg = new Value();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggTable.class) {
        
        return makeNewValue(AggName.class, oldVal, propName);
    } else

    if (valueType == AggName.class) {
        Table parent = (Table) makeNewValue(Table.class, null, "Parent of AggTable");
        AggName agg = new AggName();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggPattern.class) {
        Table parent = (Table) makeNewValue(Table.class, null, "Parent of AggTable");
        AggPattern agg = new AggPattern();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggExclude.class) {
        Table parent = (Table) makeNewValue(Table.class, null, "Parent of AggExclude");
        AggExclude agg = new AggExclude();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggColumnName.class) {
        
        return makeNewValue(AggFactCount.class, oldVal, propName);
    } else

    if (valueType == AggFactCount.class) {
        AggName parent = (AggName) makeNewValue(AggName.class, null, "Parent of AggFactCount");
        AggFactCount agg = new AggFactCount();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggIgnoreColumn.class) {
        AggName parent = (AggName) makeNewValue(AggName.class, null, "Parent of AggIgnoreColumn");
        AggIgnoreColumn agg = new AggIgnoreColumn();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggForeignKey.class) {
        AggName parent = (AggName) makeNewValue(AggName.class, null, "Parent of AggForeignKey");
        AggForeignKey agg = new AggForeignKey();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggLevel.class) {
        AggName parent = (AggName) makeNewValue(AggName.class, null, "Parent of AggLevel");
        AggLevel agg = new AggLevel();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == AggMeasure.class) {
        AggName parent = (AggName) makeNewValue(AggName.class, null, "Parent of AggMeasure");
        AggMeasure agg = new AggMeasure();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Expression.class) {
        
        return makeNewValue(Column.class, oldVal, propName);
    } else

    if (valueType == ExpressionView.class) {
        
        return makeNewValue(KeyExpression.class, oldVal, propName);
    } else

    if (valueType == KeyExpression.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of KeyExpression");
        KeyExpression agg = new KeyExpression();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == ParentExpression.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of ParentExpression");
        ParentExpression agg = new ParentExpression();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == OrdinalExpression.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of OrdinalExpression");
        OrdinalExpression agg = new OrdinalExpression();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == NameExpression.class) {
        Level parent = (Level) makeNewValue(Level.class, null, "Parent of NameExpression");
        NameExpression agg = new NameExpression();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == MeasureExpression.class) {
        Measure parent = (Measure) makeNewValue(Measure.class, null, "Parent of MeasureExpression");
        MeasureExpression agg = new MeasureExpression();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Role.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of Role");
        Role agg = new Role();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Grant.class) {
        
        return makeNewValue(SchemaGrant.class, oldVal, propName);
    } else

    if (valueType == SchemaGrant.class) {
        Role parent = (Role) makeNewValue(Role.class, null, "Parent of SchemaGrant");
        SchemaGrant agg = new SchemaGrant();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == CubeGrant.class) {
        SchemaGrant parent = (SchemaGrant) makeNewValue(SchemaGrant.class, null, "Parent of CubeGrant");
        CubeGrant agg = new CubeGrant();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == DimensionGrant.class) {
        CubeGrant parent = (CubeGrant) makeNewValue(CubeGrant.class, null, "Parent of DimensionGrant");
        DimensionGrant agg = new DimensionGrant();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == HierarchyGrant.class) {
        CubeGrant parent = (CubeGrant) makeNewValue(CubeGrant.class, null, "Parent of HierarchyGrant");
        HierarchyGrant agg = new HierarchyGrant();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == MemberGrant.class) {
        HierarchyGrant parent = (HierarchyGrant) makeNewValue(HierarchyGrant.class, null, "Parent of MemberGrant");
        MemberGrant agg = new MemberGrant();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Union.class) {
        Role parent = (Role) makeNewValue(Role.class, null, "Parent of Union");
        Union agg = new Union();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == RoleUsage.class) {
        Union parent = (Union) makeNewValue(Union.class, null, "Parent of RoleUsage");
        RoleUsage agg = new RoleUsage();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == UserDefinedFunction.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of UserDefinedFunction");
        UserDefinedFunction agg = new UserDefinedFunction();
        parent.addChild(agg);
        return agg;
    } else

    if (valueType == Parameter.class) {
        Schema parent = (Schema) makeNewValue(Schema.class, null, "Parent of Parameter");
        Parameter agg = new Parameter();
        parent.addChild(agg);
        return agg;
    } else

        {
            throw new IllegalStateException("Unknown class type " + valueType);
        }
    }
} // end of entire model
