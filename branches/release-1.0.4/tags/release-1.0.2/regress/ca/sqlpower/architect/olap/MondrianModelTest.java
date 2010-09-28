
package ca.sqlpower.architect.olap;


import ca.sqlpower.architect.olap.MondrianModel.*;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;


/**
 * This is class is generated from xml-to-persister-tests.xsl!  Do not alter it directly.
 */
public class MondrianModelTest {



public static class SchemaTest extends PersistedSPObjectTest {

        private Schema objectUnderTest;

        public SchemaTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Schema) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Schema.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Parameter
Cube.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CubeTest extends PersistedSPObjectTest {

        private Cube objectUnderTest;

        public CubeTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Cube) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Cube.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: CubeDimension
Measure.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class VirtualCubeTest extends PersistedSPObjectTest {

        private VirtualCube objectUnderTest;

        public VirtualCubeTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (VirtualCube) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(VirtualCube.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: VirtualCubeDimension
VirtualCubeDimension.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CubeUsagesTest extends PersistedSPObjectTest {

        private CubeUsages objectUnderTest;

        public CubeUsagesTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (CubeUsages) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(CubeUsages.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: CubeUsage
CubeUsage.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CubeUsageTest extends PersistedSPObjectTest {

        private CubeUsage objectUnderTest;

        public CubeUsageTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (CubeUsage) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(CubeUsage.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class VirtualCubeDimensionTest extends PersistedSPObjectTest {

        private VirtualCubeDimension objectUnderTest;

        public VirtualCubeDimensionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (VirtualCubeDimension) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(VirtualCubeDimension.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: CubeDimension

        null;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class VirtualCubeMeasureTest extends PersistedSPObjectTest {

        private VirtualCubeMeasure objectUnderTest;

        public VirtualCubeMeasureTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (VirtualCubeMeasure) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(VirtualCubeMeasure.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class DimensionUsageTest extends PersistedSPObjectTest {

        private DimensionUsage objectUnderTest;

        public DimensionUsageTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (DimensionUsage) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(DimensionUsage.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: CubeDimension

        null;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class DimensionTest extends PersistedSPObjectTest {

        private Dimension objectUnderTest;

        public DimensionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Dimension) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Dimension.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Hierarchy
Hierarchy.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class HierarchyTest extends PersistedSPObjectTest {

        private Hierarchy objectUnderTest;

        public HierarchyTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Hierarchy) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Hierarchy.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Level
Level.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class LevelTest extends PersistedSPObjectTest {

        private Level objectUnderTest;

        public LevelTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Level) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Level.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Property
Property.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ClosureTest extends PersistedSPObjectTest {

        private Closure objectUnderTest;

        public ClosureTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Closure) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Closure.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Table
Table.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class PropertyTest extends PersistedSPObjectTest {

        private Property objectUnderTest;

        public PropertyTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Property) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Property.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class MeasureTest extends PersistedSPObjectTest {

        private Measure objectUnderTest;

        public MeasureTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Measure) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Measure.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: CalculatedMemberProperty
CalculatedMemberProperty.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CalculatedMemberTest extends PersistedSPObjectTest {

        private CalculatedMember objectUnderTest;

        public CalculatedMemberTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (CalculatedMember) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(CalculatedMember.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: CalculatedMemberProperty
CalculatedMemberProperty.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CalculatedMemberPropertyTest extends PersistedSPObjectTest {

        private CalculatedMemberProperty objectUnderTest;

        public CalculatedMemberPropertyTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (CalculatedMemberProperty) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(CalculatedMemberProperty.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class NamedSetTest extends PersistedSPObjectTest {

        private NamedSet objectUnderTest;

        public NamedSetTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (NamedSet) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(NamedSet.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Formula
Formula.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class FormulaTest extends PersistedSPObjectTest {

        private Formula objectUnderTest;

        public FormulaTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Formula) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Formula.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class MemberReaderParameterTest extends PersistedSPObjectTest {

        private MemberReaderParameter objectUnderTest;

        public MemberReaderParameterTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (MemberReaderParameter) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(MemberReaderParameter.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ViewTest extends PersistedSPObjectTest {

        private View objectUnderTest;

        public ViewTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (View) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(View.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class SQLTest extends PersistedSPObjectTest {

        private SQL objectUnderTest;

        public SQLTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (SQL) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(SQL.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class JoinTest extends PersistedSPObjectTest {

        private Join objectUnderTest;

        public JoinTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Join) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Join.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: RelationOrJoin

//child descendant: RelationOrJoin
Join.class;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class TableTest extends PersistedSPObjectTest {

        private Table objectUnderTest;

        public TableTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Table) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Table.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: AggExclude
AggExclude.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class InlineTableTest extends PersistedSPObjectTest {

        private InlineTable objectUnderTest;

        public InlineTableTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (InlineTable) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(InlineTable.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: ColumnDefs
ColumnDefs.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ColumnDefsTest extends PersistedSPObjectTest {

        private ColumnDefs objectUnderTest;

        public ColumnDefsTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (ColumnDefs) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(ColumnDefs.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: ColumnDef
ColumnDef.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ColumnDefTest extends PersistedSPObjectTest {

        private ColumnDef objectUnderTest;

        public ColumnDefTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (ColumnDef) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(ColumnDef.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class RowsTest extends PersistedSPObjectTest {

        private Rows objectUnderTest;

        public RowsTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Rows) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Rows.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Row
Row.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class RowTest extends PersistedSPObjectTest {

        private Row objectUnderTest;

        public RowTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Row) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Row.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: Value
Value.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ValueTest extends PersistedSPObjectTest {

        private Value objectUnderTest;

        public ValueTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Value) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Value.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggNameTest extends PersistedSPObjectTest {

        private AggName objectUnderTest;

        public AggNameTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggName) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggName.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: AggTable

//child: AggIgnoreColumn
AggIgnoreColumn.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggPatternTest extends PersistedSPObjectTest {

        private AggPattern objectUnderTest;

        public AggPatternTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggPattern) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggPattern.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: AggExclude
AggExclude.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggExcludeTest extends PersistedSPObjectTest {

        private AggExclude objectUnderTest;

        public AggExcludeTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggExclude) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggExclude.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggFactCountTest extends PersistedSPObjectTest {

        private AggFactCount objectUnderTest;

        public AggFactCountTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggFactCount) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggFactCount.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: AggColumnName

        null;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggIgnoreColumnTest extends PersistedSPObjectTest {

        private AggIgnoreColumn objectUnderTest;

        public AggIgnoreColumnTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggIgnoreColumn) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggIgnoreColumn.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: AggColumnName

        null;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggForeignKeyTest extends PersistedSPObjectTest {

        private AggForeignKey objectUnderTest;

        public AggForeignKeyTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggForeignKey) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggForeignKey.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggLevelTest extends PersistedSPObjectTest {

        private AggLevel objectUnderTest;

        public AggLevelTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggLevel) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggLevel.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class AggMeasureTest extends PersistedSPObjectTest {

        private AggMeasure objectUnderTest;

        public AggMeasureTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (AggMeasure) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(AggMeasure.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class KeyExpressionTest extends PersistedSPObjectTest {

        private KeyExpression objectUnderTest;

        public KeyExpressionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (KeyExpression) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(KeyExpression.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: ExpressionView

//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ParentExpressionTest extends PersistedSPObjectTest {

        private ParentExpression objectUnderTest;

        public ParentExpressionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (ParentExpression) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(ParentExpression.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: ExpressionView

//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class OrdinalExpressionTest extends PersistedSPObjectTest {

        private OrdinalExpression objectUnderTest;

        public OrdinalExpressionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (OrdinalExpression) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(OrdinalExpression.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: ExpressionView

//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class NameExpressionTest extends PersistedSPObjectTest {

        private NameExpression objectUnderTest;

        public NameExpressionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (NameExpression) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(NameExpression.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: ExpressionView

//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class MeasureExpressionTest extends PersistedSPObjectTest {

        private MeasureExpression objectUnderTest;

        public MeasureExpressionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (MeasureExpression) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(MeasureExpression.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: ExpressionView

//child: SQL
SQL.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class RoleTest extends PersistedSPObjectTest {

        private Role objectUnderTest;

        public RoleTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Role) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Role.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: SchemaGrant
SchemaGrant.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class SchemaGrantTest extends PersistedSPObjectTest {

        private SchemaGrant objectUnderTest;

        public SchemaGrantTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (SchemaGrant) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(SchemaGrant.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: CubeGrant
CubeGrant.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class CubeGrantTest extends PersistedSPObjectTest {

        private CubeGrant objectUnderTest;

        public CubeGrantTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (CubeGrant) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(CubeGrant.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: DimensionGrant
DimensionGrant.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class DimensionGrantTest extends PersistedSPObjectTest {

        private DimensionGrant objectUnderTest;

        public DimensionGrantTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (DimensionGrant) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(DimensionGrant.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//superclass: Grant

        null;
    
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class HierarchyGrantTest extends PersistedSPObjectTest {

        private HierarchyGrant objectUnderTest;

        public HierarchyGrantTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (HierarchyGrant) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(HierarchyGrant.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: MemberGrant
MemberGrant.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class MemberGrantTest extends PersistedSPObjectTest {

        private MemberGrant objectUnderTest;

        public MemberGrantTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (MemberGrant) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(MemberGrant.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class UnionTest extends PersistedSPObjectTest {

        private Union objectUnderTest;

        public UnionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Union) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Union.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
//child: RoleUsage
RoleUsage.class;
     
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class RoleUsageTest extends PersistedSPObjectTest {

        private RoleUsage objectUnderTest;

        public RoleUsageTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (RoleUsage) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(RoleUsage.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class UserDefinedFunctionTest extends PersistedSPObjectTest {

        private UserDefinedFunction objectUnderTest;

        public UserDefinedFunctionTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (UserDefinedFunction) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(UserDefinedFunction.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


public static class ParameterTest extends PersistedSPObjectTest {

        private Parameter objectUnderTest;

        public ParameterTest(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (Parameter) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(Parameter.class, null, "object under test");
        }

        @Override
        protected Class<? extends SPObject> getChildClassType() {
            return 
            
                null;
                
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }


} // end of entire model
