
package ca.sqlpower.architect.olap;

public class MondrianModel {

/** A schema is a collection of cubes and virtual cubes.
            It can also contain shared dimensions (for use by those
            cubes), named sets, roles, and declarations of
            user-defined functions. */ 
public static class Schema extends OLAPObject {
    
    /**
     * Creates a new Schema with all attributes
     * set to their defaults.
     */
    public Schema() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ measuresCaption;
    
    public String /* */ getMeasuresCaption() {
        return measuresCaption;
    }
    
    public void setMeasuresCaption(String /* */ newval) {
        String /* */ oldval = measuresCaption;
        measuresCaption = newval;
        pcs.firePropertyChange("measuresCaption", oldval, newval);
    }

    private String /* */ defaultRole;
    
    public String /* */ getDefaultRole() {
        return defaultRole;
    }
    
    public void setDefaultRole(String /* */ newval) {
        String /* */ oldval = defaultRole;
        defaultRole = newval;
        pcs.firePropertyChange("defaultRole", oldval, newval);
    }

} // end of element Schema
/** 
            A CubeDimension is either a usage of a Dimension ('shared
            dimension', in MSOLAP parlance), or a 'private dimension'.
         */ 
public static class CubeDimension extends OLAPObject {
    
    /**
     * Creates a new CubeDimension with all attributes
     * set to their defaults.
     */
    public CubeDimension() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ foreignKey;
    
    public String /* */ getForeignKey() {
        return foreignKey;
    }
    
    public void setForeignKey(String /* */ newval) {
        String /* */ oldval = foreignKey;
        foreignKey = newval;
        pcs.firePropertyChange("foreignKey", oldval, newval);
    }

} // end of class CubeDimension
/** 
            Definition of a cube.
         */ 
public static class Cube extends OLAPObject {
    
    /**
     * Creates a new Cube with all attributes
     * set to their defaults.
     */
    public Cube() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ defaultMeasure;
    
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        pcs.firePropertyChange("defaultMeasure", oldval, newval);
    }

    private Boolean /* */ cache;
    
    public Boolean /* */ getCache() {
        return cache;
    }
    
    public void setCache(Boolean /* */ newval) {
        Boolean /* */ oldval = cache;
        cache = newval;
        pcs.firePropertyChange("cache", oldval, newval);
    }

    private Boolean /* */ enabled;
    
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        pcs.firePropertyChange("enabled", oldval, newval);
    }

} // end of element Cube
/** 
            A VirtualCube is a set of dimensions and
            measures gleaned from other cubes.
         */ 
public static class VirtualCube extends OLAPObject {
    
    /**
     * Creates a new VirtualCube with all attributes
     * set to their defaults.
     */
    public VirtualCube() {
    }
    

    private Boolean /* */ enabled;
    
    public Boolean /* */ getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean /* */ newval) {
        Boolean /* */ oldval = enabled;
        enabled = newval;
        pcs.firePropertyChange("enabled", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ defaultMeasure;
    
    public String /* */ getDefaultMeasure() {
        return defaultMeasure;
    }
    
    public void setDefaultMeasure(String /* */ newval) {
        String /* */ oldval = defaultMeasure;
        defaultMeasure = newval;
        pcs.firePropertyChange("defaultMeasure", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

} // end of element VirtualCube
/** 
            List of base cubes used by the virtual cube.
         */ 
public static class CubeUsages extends OLAPObject {
    
    /**
     * Creates a new CubeUsages with all attributes
     * set to their defaults.
     */
    public CubeUsages() {
    }
    

} // end of element CubeUsages
/**  */ 
public static class CubeUsage extends OLAPObject {
    
    /**
     * Creates a new CubeUsage with all attributes
     * set to their defaults.
     */
    public CubeUsage() {
    }
    

    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    private Boolean /* */ ignoreUnrelatedDimensions;
    
    public Boolean /* */ getIgnoreUnrelatedDimensions() {
        return ignoreUnrelatedDimensions;
    }
    
    public void setIgnoreUnrelatedDimensions(Boolean /* */ newval) {
        Boolean /* */ oldval = ignoreUnrelatedDimensions;
        ignoreUnrelatedDimensions = newval;
        pcs.firePropertyChange("ignoreUnrelatedDimensions", oldval, newval);
    }

} // end of element CubeUsage
/** 
            A VirtualCubeDimension is a usage of a Dimension in a VirtualCube.
         */ 
public static class VirtualCubeDimension extends CubeDimension {
    
    /**
     * Creates a new VirtualCubeDimension with all attributes
     * set to their defaults.
     */
    public VirtualCubeDimension() {
    }
    

    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element VirtualCubeDimension
/** 
            A VirtualCubeMeasure is a usage of a Measure in a VirtualCube.
         */ 
public static class VirtualCubeMeasure extends OLAPObject {
    
    /**
     * Creates a new VirtualCubeMeasure with all attributes
     * set to their defaults.
     */
    public VirtualCubeMeasure() {
    }
    

    private String /* */ cubeName;
    
    public String /* */ getCubeName() {
        return cubeName;
    }
    
    public void setCubeName(String /* */ newval) {
        String /* */ oldval = cubeName;
        cubeName = newval;
        pcs.firePropertyChange("cubeName", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

} // end of element VirtualCubeMeasure
/** 
            A DimensionUsage is usage of a shared
            Dimension within the context of a cube.
         */ 
public static class DimensionUsage extends CubeDimension {
    
    /**
     * Creates a new DimensionUsage with all attributes
     * set to their defaults.
     */
    public DimensionUsage() {
    }
    

    private String /* */ source;
    
    public String /* */ getSource() {
        return source;
    }
    
    public void setSource(String /* */ newval) {
        String /* */ oldval = source;
        source = newval;
        pcs.firePropertyChange("source", oldval, newval);
    }

    private String /* */ level;
    
    public String /* */ getLevel() {
        return level;
    }
    
    public void setLevel(String /* */ newval) {
        String /* */ oldval = level;
        level = newval;
        pcs.firePropertyChange("level", oldval, newval);
    }

    private String /* */ usagePrefix;
    
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        pcs.firePropertyChange("usagePrefix", oldval, newval);
    }

} // end of element DimensionUsage
/** 
            A Dimension is a collection of hierarchies. There are
            two kinds: a public dimension belongs to a
            Schema, and be used by several cubes; a
            private dimension belongs to a
            Cube. The foreignKey field is only
            applicable to private dimensions.
         */ 
public static class Dimension extends CubeDimension {
    
    /**
     * Creates a new Dimension with all attributes
     * set to their defaults.
     */
    public Dimension() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ usagePrefix;
    
    public String /* */ getUsagePrefix() {
        return usagePrefix;
    }
    
    public void setUsagePrefix(String /* */ newval) {
        String /* */ oldval = usagePrefix;
        usagePrefix = newval;
        pcs.firePropertyChange("usagePrefix", oldval, newval);
    }

} // end of element Dimension
/** 
            Defines a hierarchy.

            You must specify at most one <Relation>
            or memberReaderClass. If you specify none, the
            hierarchy is assumed to come from the same fact table of the
            current cube.
         */ 
public static class Hierarchy extends OLAPObject {
    
    /**
     * Creates a new Hierarchy with all attributes
     * set to their defaults.
     */
    public Hierarchy() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private Boolean /* */ hasAll;
    
    public Boolean /* */ getHasAll() {
        return hasAll;
    }
    
    public void setHasAll(Boolean /* */ newval) {
        Boolean /* */ oldval = hasAll;
        hasAll = newval;
        pcs.firePropertyChange("hasAll", oldval, newval);
    }

    private String /* */ allMemberName;
    
    public String /* */ getAllMemberName() {
        return allMemberName;
    }
    
    public void setAllMemberName(String /* */ newval) {
        String /* */ oldval = allMemberName;
        allMemberName = newval;
        pcs.firePropertyChange("allMemberName", oldval, newval);
    }

    private String /* */ allMemberCaption;
    
    public String /* */ getAllMemberCaption() {
        return allMemberCaption;
    }
    
    public void setAllMemberCaption(String /* */ newval) {
        String /* */ oldval = allMemberCaption;
        allMemberCaption = newval;
        pcs.firePropertyChange("allMemberCaption", oldval, newval);
    }

    private String /* */ allLevelName;
    
    public String /* */ getAllLevelName() {
        return allLevelName;
    }
    
    public void setAllLevelName(String /* */ newval) {
        String /* */ oldval = allLevelName;
        allLevelName = newval;
        pcs.firePropertyChange("allLevelName", oldval, newval);
    }

    private String /* */ primaryKey;
    
    public String /* */ getPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(String /* */ newval) {
        String /* */ oldval = primaryKey;
        primaryKey = newval;
        pcs.firePropertyChange("primaryKey", oldval, newval);
    }

    private String /* */ primaryKeyTable;
    
    public String /* */ getPrimaryKeyTable() {
        return primaryKeyTable;
    }
    
    public void setPrimaryKeyTable(String /* */ newval) {
        String /* */ oldval = primaryKeyTable;
        primaryKeyTable = newval;
        pcs.firePropertyChange("primaryKeyTable", oldval, newval);
    }

    private String /* */ defaultMember;
    
    public String /* */ getDefaultMember() {
        return defaultMember;
    }
    
    public void setDefaultMember(String /* */ newval) {
        String /* */ oldval = defaultMember;
        defaultMember = newval;
        pcs.firePropertyChange("defaultMember", oldval, newval);
    }

    private String /* */ memberReaderClass;
    
    public String /* */ getMemberReaderClass() {
        return memberReaderClass;
    }
    
    public void setMemberReaderClass(String /* */ newval) {
        String /* */ oldval = memberReaderClass;
        memberReaderClass = newval;
        pcs.firePropertyChange("memberReaderClass", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

} // end of element Hierarchy
/**  */ 
public static class Level extends OLAPObject {
    
    /**
     * Creates a new Level with all attributes
     * set to their defaults.
     */
    public Level() {
    }
    

    private String /* */ approxRowCount;
    
    public String /* */ getApproxRowCount() {
        return approxRowCount;
    }
    
    public void setApproxRowCount(String /* */ newval) {
        String /* */ oldval = approxRowCount;
        approxRowCount = newval;
        pcs.firePropertyChange("approxRowCount", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ table;
    
    public String /* */ getTable() {
        return table;
    }
    
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        pcs.firePropertyChange("table", oldval, newval);
    }

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    private String /* */ nameColumn;
    
    public String /* */ getNameColumn() {
        return nameColumn;
    }
    
    public void setNameColumn(String /* */ newval) {
        String /* */ oldval = nameColumn;
        nameColumn = newval;
        pcs.firePropertyChange("nameColumn", oldval, newval);
    }

    private String /* */ ordinalColumn;
    
    public String /* */ getOrdinalColumn() {
        return ordinalColumn;
    }
    
    public void setOrdinalColumn(String /* */ newval) {
        String /* */ oldval = ordinalColumn;
        ordinalColumn = newval;
        pcs.firePropertyChange("ordinalColumn", oldval, newval);
    }

    private String /* */ parentColumn;
    
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        pcs.firePropertyChange("parentColumn", oldval, newval);
    }

    private String /* */ nullParentValue;
    
    public String /* */ getNullParentValue() {
        return nullParentValue;
    }
    
    public void setNullParentValue(String /* */ newval) {
        String /* */ oldval = nullParentValue;
        nullParentValue = newval;
        pcs.firePropertyChange("nullParentValue", oldval, newval);
    }

    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    private Boolean /* */ uniqueMembers;
    
    public Boolean /* */ getUniqueMembers() {
        return uniqueMembers;
    }
    
    public void setUniqueMembers(Boolean /* */ newval) {
        Boolean /* */ oldval = uniqueMembers;
        uniqueMembers = newval;
        pcs.firePropertyChange("uniqueMembers", oldval, newval);
    }

    private String /* */ levelType;
    
    public String /* */ getLevelType() {
        return levelType;
    }
    
    public void setLevelType(String /* */ newval) {
        String /* */ oldval = levelType;
        levelType = newval;
        pcs.firePropertyChange("levelType", oldval, newval);
    }

    private String /* */ hideMemberIf;
    
    public String /* */ getHideMemberIf() {
        return hideMemberIf;
    }
    
    public void setHideMemberIf(String /* */ newval) {
        String /* */ oldval = hideMemberIf;
        hideMemberIf = newval;
        pcs.firePropertyChange("hideMemberIf", oldval, newval);
    }

    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ captionColumn;
    
    public String /* */ getCaptionColumn() {
        return captionColumn;
    }
    
    public void setCaptionColumn(String /* */ newval) {
        String /* */ oldval = captionColumn;
        captionColumn = newval;
        pcs.firePropertyChange("captionColumn", oldval, newval);
    }

} // end of element Level
/** 
            Specifies the transitive closure of a parent-child hierarchy.
            Optional, but recommended for better performance.
            The closure is provided as a set of (parent/child) pairs:
            since it is the transitive closure these are actually (ancestor/descendant) pairs.
         */ 
public static class Closure extends OLAPObject {
    
    /**
     * Creates a new Closure with all attributes
     * set to their defaults.
     */
    public Closure() {
    }
    

    private String /* */ parentColumn;
    
    public String /* */ getParentColumn() {
        return parentColumn;
    }
    
    public void setParentColumn(String /* */ newval) {
        String /* */ oldval = parentColumn;
        parentColumn = newval;
        pcs.firePropertyChange("parentColumn", oldval, newval);
    }

    private String /* */ childColumn;
    
    public String /* */ getChildColumn() {
        return childColumn;
    }
    
    public void setChildColumn(String /* */ newval) {
        String /* */ oldval = childColumn;
        childColumn = newval;
        pcs.firePropertyChange("childColumn", oldval, newval);
    }

} // end of element Closure
/** 
            Member property.
         */ 
public static class Property extends OLAPObject {
    
    /**
     * Creates a new Property with all attributes
     * set to their defaults.
     */
    public Property() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

} // end of element Property
/**  */ 
public static class Measure extends OLAPObject {
    
    /**
     * Creates a new Measure with all attributes
     * set to their defaults.
     */
    public Measure() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    private String /* */ datatype;
    
    public String /* */ getDatatype() {
        return datatype;
    }
    
    public void setDatatype(String /* */ newval) {
        String /* */ oldval = datatype;
        datatype = newval;
        pcs.firePropertyChange("datatype", oldval, newval);
    }

    private String /* */ formatString;
    
    public String /* */ getFormatString() {
        return formatString;
    }
    
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        pcs.firePropertyChange("formatString", oldval, newval);
    }

    private String /* */ aggregator;
    
    public String /* */ getAggregator() {
        return aggregator;
    }
    
    public void setAggregator(String /* */ newval) {
        String /* */ oldval = aggregator;
        aggregator = newval;
        pcs.firePropertyChange("aggregator", oldval, newval);
    }

    private String /* */ formatter;
    
    public String /* */ getFormatter() {
        return formatter;
    }
    
    public void setFormatter(String /* */ newval) {
        String /* */ oldval = formatter;
        formatter = newval;
        pcs.firePropertyChange("formatter", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

} // end of element Measure
/**  */ 
public static class CalculatedMember extends OLAPObject {
    
    /**
     * Creates a new CalculatedMember with all attributes
     * set to their defaults.
     */
    public CalculatedMember() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ formatString;
    
    public String /* */ getFormatString() {
        return formatString;
    }
    
    public void setFormatString(String /* */ newval) {
        String /* */ oldval = formatString;
        formatString = newval;
        pcs.firePropertyChange("formatString", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ formula;
    
    public String /* */ getFormula() {
        return formula;
    }
    
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        pcs.firePropertyChange("formula", oldval, newval);
    }

    private String /* */ dimension;
    
    public String /* */ getDimension() {
        return dimension;
    }
    
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        pcs.firePropertyChange("dimension", oldval, newval);
    }

    private Boolean /* */ visible;
    
    public Boolean /* */ getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean /* */ newval) {
        Boolean /* */ oldval = visible;
        visible = newval;
        pcs.firePropertyChange("visible", oldval, newval);
    }

} // end of element CalculatedMember
/** 
            Property of a calculated member defined against a cube.
            It must have either an expression or a value.
         */ 
public static class CalculatedMemberProperty extends OLAPObject {
    
    /**
     * Creates a new CalculatedMemberProperty with all attributes
     * set to their defaults.
     */
    public CalculatedMemberProperty() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ caption;
    
    public String /* */ getCaption() {
        return caption;
    }
    
    public void setCaption(String /* */ newval) {
        String /* */ oldval = caption;
        caption = newval;
        pcs.firePropertyChange("caption", oldval, newval);
    }

    private String /* */ expression;
    
    public String /* */ getExpression() {
        return expression;
    }
    
    public void setExpression(String /* */ newval) {
        String /* */ oldval = expression;
        expression = newval;
        pcs.firePropertyChange("expression", oldval, newval);
    }

    private String /* */ value;
    
    public String /* */ getValue() {
        return value;
    }
    
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        pcs.firePropertyChange("value", oldval, newval);
    }

} // end of element CalculatedMemberProperty
/** 
            <p>Defines a named set which can be used in queries in the
            same way as a set defined using a WITH SET clause.</p>

            <p>A named set can be defined against a particular cube,
            or can be global to a schema. If it is defined against a
            cube, it is only available to queries which use that cube.</p>

            <p>A named set defined against a cube is not inherited by
            a virtual cubes defined against that cube. (But you can
            define a named set against a virtual cube.)</p>

            <p>A named set defined against a schema is available in
            all cubes and virtual cubes in that schema. However, it is
            only valid if the cube contains dimensions with the names
            required to make the formula valid.</p>

         */ 
public static class NamedSet extends OLAPObject {
    
    /**
     * Creates a new NamedSet with all attributes
     * set to their defaults.
     */
    public NamedSet() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ formula;
    
    public String /* */ getFormula() {
        return formula;
    }
    
    public void setFormula(String /* */ newval) {
        String /* */ oldval = formula;
        formula = newval;
        pcs.firePropertyChange("formula", oldval, newval);
    }

} // end of element NamedSet
/**  */ 
public static class Formula extends OLAPObject {
    
    /**
     * Creates a new Formula with all attributes
     * set to their defaults.
     */
    public Formula() {
    }
    

} // end of element Formula
/** Not used */ 
public static class MemberReaderParameter extends OLAPObject {
    
    /**
     * Creates a new MemberReaderParameter with all attributes
     * set to their defaults.
     */
    public MemberReaderParameter() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ value;
    
    public String /* */ getValue() {
        return value;
    }
    
    public void setValue(String /* */ newval) {
        String /* */ oldval = value;
        value = newval;
        pcs.firePropertyChange("value", oldval, newval);
    }

} // end of element MemberReaderParameter
/** A table or a join */ 
public static class RelationOrJoin extends OLAPObject {
    
    /**
     * Creates a new RelationOrJoin with all attributes
     * set to their defaults.
     */
    public RelationOrJoin() {
    }
    

} // end of class RelationOrJoin
/** A table, inline table or view */ 
public static class Relation extends RelationOrJoin {
    
    /**
     * Creates a new Relation with all attributes
     * set to their defaults.
     */
    public Relation() {
    }
    

} // end of class Relation
/** 
            A collection of SQL statements, one per dialect.
         */ 
public static class View extends Relation {
    
    /**
     * Creates a new View with all attributes
     * set to their defaults.
     */
    public View() {
    }
    

    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

} // end of element View
/**  */ 
public static class SQL extends OLAPObject {
    
    /**
     * Creates a new SQL with all attributes
     * set to their defaults.
     */
    public SQL() {
    }
    

    private String /* */ dialect;
    
    public String /* */ getDialect() {
        return dialect;
    }
    
    public void setDialect(String /* */ newval) {
        String /* */ oldval = dialect;
        dialect = newval;
        pcs.firePropertyChange("dialect", oldval, newval);
    }

} // end of element SQL
/**  */ 
public static class Join extends RelationOrJoin {
    
    /**
     * Creates a new Join with all attributes
     * set to their defaults.
     */
    public Join() {
    }
    

    private String /* */ leftAlias;
    
    public String /* */ getLeftAlias() {
        return leftAlias;
    }
    
    public void setLeftAlias(String /* */ newval) {
        String /* */ oldval = leftAlias;
        leftAlias = newval;
        pcs.firePropertyChange("leftAlias", oldval, newval);
    }

    private String /* */ leftKey;
    
    public String /* */ getLeftKey() {
        return leftKey;
    }
    
    public void setLeftKey(String /* */ newval) {
        String /* */ oldval = leftKey;
        leftKey = newval;
        pcs.firePropertyChange("leftKey", oldval, newval);
    }

    private String /* */ rightAlias;
    
    public String /* */ getRightAlias() {
        return rightAlias;
    }
    
    public void setRightAlias(String /* */ newval) {
        String /* */ oldval = rightAlias;
        rightAlias = newval;
        pcs.firePropertyChange("rightAlias", oldval, newval);
    }

    private String /* */ rightKey;
    
    public String /* */ getRightKey() {
        return rightKey;
    }
    
    public void setRightKey(String /* */ newval) {
        String /* */ oldval = rightKey;
        rightKey = newval;
        pcs.firePropertyChange("rightKey", oldval, newval);
    }

} // end of element Join
/**  */ 
public static class Table extends Relation {
    
    /**
     * Creates a new Table with all attributes
     * set to their defaults.
     */
    public Table() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ schema;
    
    public String /* */ getSchema() {
        return schema;
    }
    
    public void setSchema(String /* */ newval) {
        String /* */ oldval = schema;
        schema = newval;
        pcs.firePropertyChange("schema", oldval, newval);
    }

    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

} // end of element Table
/**  */ 
public static class InlineTable extends Relation {
    
    /**
     * Creates a new InlineTable with all attributes
     * set to their defaults.
     */
    public InlineTable() {
    }
    

    private String /* */ alias;
    
    public String /* */ getAlias() {
        return alias;
    }
    
    public void setAlias(String /* */ newval) {
        String /* */ oldval = alias;
        alias = newval;
        pcs.firePropertyChange("alias", oldval, newval);
    }

} // end of element InlineTable
/** Holder for an array of ColumnDef elements */ 
public static class ColumnDefs extends OLAPObject {
    
    /**
     * Creates a new ColumnDefs with all attributes
     * set to their defaults.
     */
    public ColumnDefs() {
    }
    

} // end of element ColumnDefs
/** 
            Column definition for an inline table.
         */ 
public static class ColumnDef extends OLAPObject {
    
    /**
     * Creates a new ColumnDef with all attributes
     * set to their defaults.
     */
    public ColumnDef() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

} // end of element ColumnDef
/** Holder for an array of Row elements */ 
public static class Rows extends OLAPObject {
    
    /**
     * Creates a new Rows with all attributes
     * set to their defaults.
     */
    public Rows() {
    }
    

} // end of element Rows
/** 
            Row definition for an inline table.
            Must have one Column for each ColumnDef in the InlineTable.
         */ 
public static class Row extends OLAPObject {
    
    /**
     * Creates a new Row with all attributes
     * set to their defaults.
     */
    public Row() {
    }
    

} // end of element Row
/** 
            Column value for an inline table.
            The CDATA holds the value of the column.
         */ 
public static class Value extends OLAPObject {
    
    /**
     * Creates a new Value with all attributes
     * set to their defaults.
     */
    public Value() {
    }
    

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

} // end of element Value
/** 
            A definition of an aggregate table for a base fact table.
            This aggregate table must be in the same schema as the
            base fact table.
         */ 
public static class AggTable extends OLAPObject {
    
    /**
     * Creates a new AggTable with all attributes
     * set to their defaults.
     */
    public AggTable() {
    }
    

    private Boolean /* */ ignorecase;
    
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        pcs.firePropertyChange("ignorecase", oldval, newval);
    }

} // end of class AggTable
/**  */ 
public static class AggName extends AggTable {
    
    /**
     * Creates a new AggName with all attributes
     * set to their defaults.
     */
    public AggName() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element AggName
/**  */ 
public static class AggPattern extends AggTable {
    
    /**
     * Creates a new AggPattern with all attributes
     * set to their defaults.
     */
    public AggPattern() {
    }
    

    private String /* */ pattern;
    
    public String /* */ getPattern() {
        return pattern;
    }
    
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        pcs.firePropertyChange("pattern", oldval, newval);
    }

} // end of element AggPattern
/**  */ 
public static class AggExclude extends OLAPObject {
    
    /**
     * Creates a new AggExclude with all attributes
     * set to their defaults.
     */
    public AggExclude() {
    }
    

    private String /* */ pattern;
    
    public String /* */ getPattern() {
        return pattern;
    }
    
    public void setPattern(String /* */ newval) {
        String /* */ oldval = pattern;
        pattern = newval;
        pcs.firePropertyChange("pattern", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private Boolean /* */ ignorecase;
    
    public Boolean /* */ getIgnorecase() {
        return ignorecase;
    }
    
    public void setIgnorecase(Boolean /* */ newval) {
        Boolean /* */ oldval = ignorecase;
        ignorecase = newval;
        pcs.firePropertyChange("ignorecase", oldval, newval);
    }

} // end of element AggExclude
/**  */ 
public static class AggColumnName extends OLAPObject {
    
    /**
     * Creates a new AggColumnName with all attributes
     * set to their defaults.
     */
    public AggColumnName() {
    }
    

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

} // end of class AggColumnName
/**  */ 
public static class AggFactCount extends AggColumnName {
    
    /**
     * Creates a new AggFactCount with all attributes
     * set to their defaults.
     */
    public AggFactCount() {
    }
    

} // end of element AggFactCount
/**  */ 
public static class AggIgnoreColumn extends AggColumnName {
    
    /**
     * Creates a new AggIgnoreColumn with all attributes
     * set to their defaults.
     */
    public AggIgnoreColumn() {
    }
    

} // end of element AggIgnoreColumn
/** 
            The name of the column mapping from base fact table foreign key
            to aggregate table foreign key.
         */ 
public static class AggForeignKey extends OLAPObject {
    
    /**
     * Creates a new AggForeignKey with all attributes
     * set to their defaults.
     */
    public AggForeignKey() {
    }
    

    private String /* */ factColumn;
    
    public String /* */ getFactColumn() {
        return factColumn;
    }
    
    public void setFactColumn(String /* */ newval) {
        String /* */ oldval = factColumn;
        factColumn = newval;
        pcs.firePropertyChange("factColumn", oldval, newval);
    }

    private String /* */ aggColumn;
    
    public String /* */ getAggColumn() {
        return aggColumn;
    }
    
    public void setAggColumn(String /* */ newval) {
        String /* */ oldval = aggColumn;
        aggColumn = newval;
        pcs.firePropertyChange("aggColumn", oldval, newval);
    }

} // end of element AggForeignKey
/**  */ 
public static class AggLevel extends OLAPObject {
    
    /**
     * Creates a new AggLevel with all attributes
     * set to their defaults.
     */
    public AggLevel() {
    }
    

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element AggLevel
/**  */ 
public static class AggMeasure extends OLAPObject {
    
    /**
     * Creates a new AggMeasure with all attributes
     * set to their defaults.
     */
    public AggMeasure() {
    }
    

    private String /* */ column;
    
    public String /* */ getColumn() {
        return column;
    }
    
    public void setColumn(String /* */ newval) {
        String /* */ oldval = column;
        column = newval;
        pcs.firePropertyChange("column", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element AggMeasure
/**  */ 
public static class Expression extends OLAPObject {
    
    /**
     * Creates a new Expression with all attributes
     * set to their defaults.
     */
    public Expression() {
    }
    

} // end of class Expression
/**  */ 
public static class Column extends Expression {
    
    /**
     * Creates a new Column with all attributes
     * set to their defaults.
     */
    public Column() {
    }
    

    private String /* */ table;
    
    public String /* */ getTable() {
        return table;
    }
    
    public void setTable(String /* */ newval) {
        String /* */ oldval = table;
        table = newval;
        pcs.firePropertyChange("table", oldval, newval);
    }

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element Column
/** 
            A collection of SQL expressions, one per dialect.
         */ 
public static class ExpressionView extends Expression {
    
    /**
     * Creates a new ExpressionView with all attributes
     * set to their defaults.
     */
    public ExpressionView() {
    }
    

} // end of class ExpressionView
/**  */ 
public static class KeyExpression extends ExpressionView {
    
    /**
     * Creates a new KeyExpression with all attributes
     * set to their defaults.
     */
    public KeyExpression() {
    }
    

} // end of element KeyExpression
/**  */ 
public static class ParentExpression extends ExpressionView {
    
    /**
     * Creates a new ParentExpression with all attributes
     * set to their defaults.
     */
    public ParentExpression() {
    }
    

} // end of element ParentExpression
/**  */ 
public static class OrdinalExpression extends ExpressionView {
    
    /**
     * Creates a new OrdinalExpression with all attributes
     * set to their defaults.
     */
    public OrdinalExpression() {
    }
    

} // end of element OrdinalExpression
/**  */ 
public static class NameExpression extends ExpressionView {
    
    /**
     * Creates a new NameExpression with all attributes
     * set to their defaults.
     */
    public NameExpression() {
    }
    

} // end of element NameExpression
/**  */ 
public static class CaptionExpression extends ExpressionView {
    
    /**
     * Creates a new CaptionExpression with all attributes
     * set to their defaults.
     */
    public CaptionExpression() {
    }
    

} // end of element CaptionExpression
/**  */ 
public static class MeasureExpression extends ExpressionView {
    
    /**
     * Creates a new MeasureExpression with all attributes
     * set to their defaults.
     */
    public MeasureExpression() {
    }
    

} // end of element MeasureExpression
/** 
            A role defines an access-control profile. It has a series of grants
            (or denials) for schema elements.
         */ 
public static class Role extends OLAPObject {
    
    /**
     * Creates a new Role with all attributes
     * set to their defaults.
     */
    public Role() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

} // end of element Role
/**  */ 
public static class Grant extends OLAPObject {
    
    /**
     * Creates a new Grant with all attributes
     * set to their defaults.
     */
    public Grant() {
    }
    

    private String /* */ access;
    
    public String /* */ getAccess() {
        return access;
    }
    
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        pcs.firePropertyChange("access", oldval, newval);
    }

} // end of class Grant
/** 
            Grants (or denies) this role access to this schema.
            access may be "all", "all_dimensions", or "none".
            If access is "all_dimensions", the role has access
            to all dimensions but still needs explicit access to cubes.
            See mondrian.olap.Role#grant(mondrian.olap.Schema,int).
         */ 
public static class SchemaGrant extends Grant {
    
    /**
     * Creates a new SchemaGrant with all attributes
     * set to their defaults.
     */
    public SchemaGrant() {
    }
    

} // end of element SchemaGrant
/** 
            Grants (or denies) this role access to a cube.
            access may be "all" or "none".
            See mondrian.olap.Role#grant(mondrian.olap.Cube,int).
         */ 
public static class CubeGrant extends Grant {
    
    /**
     * Creates a new CubeGrant with all attributes
     * set to their defaults.
     */
    public CubeGrant() {
    }
    

    private String /* */ cube;
    
    public String /* */ getCube() {
        return cube;
    }
    
    public void setCube(String /* */ newval) {
        String /* */ oldval = cube;
        cube = newval;
        pcs.firePropertyChange("cube", oldval, newval);
    }

} // end of element CubeGrant
/** 
            Grants (or denies) this role access to a dimension.
            access may be "all" or "none".
            Note that a role is implicitly given access to a dimension when it
            is given acess to a cube.
            See also the "all_dimensions" option of the "SchemaGrant" element.
            See mondrian.olap.Role#grant(mondrian.olap.Dimension,int).
         */ 
public static class DimensionGrant extends Grant {
    
    /**
     * Creates a new DimensionGrant with all attributes
     * set to their defaults.
     */
    public DimensionGrant() {
    }
    

    private String /* */ dimension;
    
    public String /* */ getDimension() {
        return dimension;
    }
    
    public void setDimension(String /* */ newval) {
        String /* */ oldval = dimension;
        dimension = newval;
        pcs.firePropertyChange("dimension", oldval, newval);
    }

} // end of element DimensionGrant
/** 
            Grants (or denies) this role access to a hierarchy.
            access may be "all", "custom" or "none".
            If access is "custom", you may also specify the
            attributes topLevel, bottomLevel, and
            the member grants.
            See mondrian.olap.Role#grant(mondrian.olap.Hierarchy, int, mondrian.olap.Level).
         */ 
public static class HierarchyGrant extends Grant {
    
    /**
     * Creates a new HierarchyGrant with all attributes
     * set to their defaults.
     */
    public HierarchyGrant() {
    }
    

    private String /* */ hierarchy;
    
    public String /* */ getHierarchy() {
        return hierarchy;
    }
    
    public void setHierarchy(String /* */ newval) {
        String /* */ oldval = hierarchy;
        hierarchy = newval;
        pcs.firePropertyChange("hierarchy", oldval, newval);
    }

    private String /* */ topLevel;
    
    public String /* */ getTopLevel() {
        return topLevel;
    }
    
    public void setTopLevel(String /* */ newval) {
        String /* */ oldval = topLevel;
        topLevel = newval;
        pcs.firePropertyChange("topLevel", oldval, newval);
    }

    private String /* */ bottomLevel;
    
    public String /* */ getBottomLevel() {
        return bottomLevel;
    }
    
    public void setBottomLevel(String /* */ newval) {
        String /* */ oldval = bottomLevel;
        bottomLevel = newval;
        pcs.firePropertyChange("bottomLevel", oldval, newval);
    }

    private String /* */ rollupPolicy;
    
    public String /* */ getRollupPolicy() {
        return rollupPolicy;
    }
    
    public void setRollupPolicy(String /* */ newval) {
        String /* */ oldval = rollupPolicy;
        rollupPolicy = newval;
        pcs.firePropertyChange("rollupPolicy", oldval, newval);
    }

} // end of element HierarchyGrant
/** 
            Grants (or denies) this role access to a member.
            The children of this member inherit that access.
            You can implicitly see a member if you can see any of its children.
            See mondrian.olap.Role#grant(mondrian.olap.Member,int).
         */ 
public static class MemberGrant extends OLAPObject {
    
    /**
     * Creates a new MemberGrant with all attributes
     * set to their defaults.
     */
    public MemberGrant() {
    }
    

    private String /* */ member;
    
    public String /* */ getMember() {
        return member;
    }
    
    public void setMember(String /* */ newval) {
        String /* */ oldval = member;
        member = newval;
        pcs.firePropertyChange("member", oldval, newval);
    }

    private String /* */ access;
    
    public String /* */ getAccess() {
        return access;
    }
    
    public void setAccess(String /* */ newval) {
        String /* */ oldval = access;
        access = newval;
        pcs.firePropertyChange("access", oldval, newval);
    }

} // end of element MemberGrant
/** 
            Body of a Role definition which defines a Role to be the union
            of several Roles. The RoleUsage elements must refer to Roles that
            have been declared earlier in this schema file.
         */ 
public static class Union extends OLAPObject {
    
    /**
     * Creates a new Union with all attributes
     * set to their defaults.
     */
    public Union() {
    }
    

} // end of element Union
/** 
            Usage of a Role in a union Role.
         */ 
public static class RoleUsage extends OLAPObject {
    
    /**
     * Creates a new RoleUsage with all attributes
     * set to their defaults.
     */
    public RoleUsage() {
    }
    

    private String /* */ roleName;
    
    public String /* */ getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String /* */ newval) {
        String /* */ oldval = roleName;
        roleName = newval;
        pcs.firePropertyChange("roleName", oldval, newval);
    }

} // end of element RoleUsage
/** 
            A UserDefinedFunction is a function which
            extends the MDX language. It must be implemented by a Java
            class which implements the interface
            mondrian.spi.UserDefinedFunction.
         */ 
public static class UserDefinedFunction extends OLAPObject {
    
    /**
     * Creates a new UserDefinedFunction with all attributes
     * set to their defaults.
     */
    public UserDefinedFunction() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ className;
    
    public String /* */ getClassName() {
        return className;
    }
    
    public void setClassName(String /* */ newval) {
        String /* */ oldval = className;
        className = newval;
        pcs.firePropertyChange("className", oldval, newval);
    }

} // end of element UserDefinedFunction
/** 
            A Parameter defines a schema parameter.
            It can be referenced from an MDX statement using the ParamRef
            function and, if not final, its value can be overridden.
         */ 
public static class Parameter extends OLAPObject {
    
    /**
     * Creates a new Parameter with all attributes
     * set to their defaults.
     */
    public Parameter() {
    }
    

    private String /* */ name;
    
    public String /* */ getName() {
        return name;
    }
    
    public void setName(String /* */ newval) {
        String /* */ oldval = name;
        name = newval;
        pcs.firePropertyChange("name", oldval, newval);
    }

    private String /* */ description;
    
    public String /* */ getDescription() {
        return description;
    }
    
    public void setDescription(String /* */ newval) {
        String /* */ oldval = description;
        description = newval;
        pcs.firePropertyChange("description", oldval, newval);
    }

    private String /* */ type;
    
    public String /* */ getType() {
        return type;
    }
    
    public void setType(String /* */ newval) {
        String /* */ oldval = type;
        type = newval;
        pcs.firePropertyChange("type", oldval, newval);
    }

    private Boolean /* */ modifiable;
    
    public Boolean /* */ getModifiable() {
        return modifiable;
    }
    
    public void setModifiable(Boolean /* */ newval) {
        Boolean /* */ oldval = modifiable;
        modifiable = newval;
        pcs.firePropertyChange("modifiable", oldval, newval);
    }

    private String /* */ defaultValue;
    
    public String /* */ getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String /* */ newval) {
        String /* */ oldval = defaultValue;
        defaultValue = newval;
        pcs.firePropertyChange("defaultValue", oldval, newval);
    }

} // end of element Parameter
} // end of entire model
