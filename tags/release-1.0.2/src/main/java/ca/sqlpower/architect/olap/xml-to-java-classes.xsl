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

<!-- TODO: copy xml subtree of Doc elements verbatim (to preserve HTML in comments)
-->

<xsl:template match="/">
package ca.sqlpower.architect.olap;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;
import ca.sqlpower.object.annotation.Transient;


/**
 * This is class is generated from xml-to-java-classes.xsl!  Do not alter it directly.
 */
public class MondrianModel {
<xsl:apply-templates/>
} // end of entire model
</xsl:template>



<!--
    ================ Element stuff ================
 -->

<xsl:template match="Element">
/** <xsl:copy-of select="Doc"/> */
public static class <xsl:value-of select="@type"/> extends <xsl:call-template name="superclass-of-element"/> {
    
    /**
     * Creates a new <xsl:value-of select="@type"/> with all attributes
     * set to their defaults.
     */
    public <xsl:value-of select="@type"/>() {
        setName("New <xsl:value-of select="@type"/>");
    }
    
    <xsl:call-template name="annotated-constructor">
        <xsl:with-param name="class-name" select="@type"/>
    </xsl:call-template>
    
    /**
     * Creates a new <xsl:value-of select="@type"/> with all
     * attributes copied from the given <xsl:value-of select="@type"/>.
     */
    public <xsl:value-of select="@type"/>(<xsl:value-of select="@type"/> original) {
    	super(original);
    	<xsl:for-each select="Attribute|Object">
    	this.<xsl:value-of select="@name"/> = original.get<xsl:call-template name="name-initcap"/>();
    	</xsl:for-each>
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("<xsl:value-of select="@type"/>:");
	    <xsl:for-each select="Attribute|Object">
	    retStr.append(" <xsl:value-of select="@name"/> = ");
	    retStr.append(<xsl:value-of select="@name"/>);
	    retStr.append(",");
	    </xsl:for-each>
		<xsl:for-each select="CData">
		retStr.append("text = ");
		retStr.append(text);
		retStr.append(",");
		</xsl:for-each>
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    <xsl:if test="@class">
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        </xsl:if>
	    return retStr.toString();
	}
<xsl:apply-templates/>

<xsl:call-template name="children-methods"/>
} // end of element <xsl:value-of select="@type"/>
</xsl:template>

<!-- Creates a constructor that is annotated to create a persister helper that accepts all mandatory values. -->
<xsl:template name="annotated-constructor">
<xsl:param name="class-name"/>
    /**
     * Creates a new <xsl:value-of select="@type"/> with all mandatory
     * values passed in.
     */
    @Constructor
    public <xsl:value-of select="$class-name"/>(
        @ConstructorParameter(propertyName = "name") String name
        <xsl:call-template name="annotated-constructor-arguments">
            <xsl:with-param name="class-name" select="$class-name"/>
            <xsl:with-param name="processed-names" select=". | ."/>
        </xsl:call-template>
    ) {
        this();
        setName(name);
        <xsl:call-template name="annotated-constructor-setters">
            <xsl:with-param name="class-name" select="$class-name"/>
        </xsl:call-template>
    }
</xsl:template>

<xsl:template name="annotated-constructor-arguments">
<xsl:param name="class-name"/>
<xsl:param name="processed-names"/>
<xsl:for-each select="../Element[@type = $class-name]/Attribute[@name != 'name']">
        <xsl:call-template name="annotated-constructor-argument">
            <xsl:with-param name="property-name" select="@name"/>
            <xsl:with-param name="processed-names" select="$processed-names"/>
        </xsl:call-template>
</xsl:for-each>
<xsl:for-each select="../Class[@class= $class-name]/Attribute[@name != 'name']">
        <xsl:call-template name="annotated-constructor-argument">
            <xsl:with-param name="property-name" select="@name"/>
            <xsl:with-param name="processed-names" select="$processed-names"/>
        </xsl:call-template>
</xsl:for-each>
<xsl:if test="../Element[@type = $class-name and @class]">
	<xsl:call-template name="annotated-constructor-arguments">
		<xsl:with-param name="class-name" select="@class" />
		<xsl:with-param name="processed-names" select="$processed-names | Attribute[@name != 'name']" />
	</xsl:call-template>
</xsl:if>
<xsl:if test="../Class[@class = $class-name and @superclass]">
	<xsl:call-template name="annotated-constructor-arguments">
		<xsl:with-param name="class-name" select="@superclass" />
		<xsl:with-param name="processed-names" select="$processed-names | Attribute[@name != 'name']" />
	</xsl:call-template>
</xsl:if>
</xsl:template>

<xsl:template name="annotated-constructor-argument">
<xsl:param name="property-name"/>
<xsl:param name="processed-names"/>
    <xsl:if test="not($processed-names[@name = $property-name])">
        , @ConstructorParameter(propertyName = "<xsl:value-of select="$property-name"/>") 
        <xsl:call-template name="attribute-type"/><xsl:text> </xsl:text><xsl:value-of select="$property-name"/>
    </xsl:if>
</xsl:template>

<xsl:template name="annotated-constructor-setters">
<xsl:param name="class-name"/>
<xsl:for-each select="../Element[@type = $class-name]/Attribute">
    <xsl:if test="@name != 'name'">
        set<xsl:call-template name="name-initcap"/>(<xsl:value-of select="@name"/>);
    </xsl:if>
</xsl:for-each>
<xsl:for-each select="../Class[@class= $class-name]/Attribute">
    <xsl:if test="@name != 'name'">
        set<xsl:call-template name="name-initcap"/>(<xsl:value-of select="@name"/>);
    </xsl:if>
</xsl:for-each>
<xsl:if test="../Element[@type = $class-name and @class]">
    <xsl:call-template name="annotated-constructor-setters">
        <xsl:with-param name="class-name" select="@class" />
    </xsl:call-template>
</xsl:if>
<xsl:if test="../Class[@class = $class-name and @superclass]">
    <xsl:call-template name="annotated-constructor-setters">
        <xsl:with-param name="class-name" select="@superclass" />
    </xsl:call-template>
</xsl:if>
</xsl:template>

<!-- Returns the correct superclass for the current Element element -->
<xsl:template name="superclass-of-element">
  <xsl:choose>
    <xsl:when test="@class"><xsl:value-of select="@class"/></xsl:when>
    <xsl:otherwise>OLAPObject</xsl:otherwise>
  </xsl:choose>
</xsl:template> 


<!--
    ================ Class stuff ================
 -->

<xsl:template match="Class">
/** <xsl:copy-of select="Doc"/> */
public abstract static class <xsl:value-of select="@class"/> extends <xsl:call-template name="superclass-of-class"/> {
    
    /**
     * Creates a new <xsl:value-of select="@class"/> with all attributes
     * set to their defaults.
     */
    public <xsl:value-of select="@class"/>() {
    setName("New <xsl:value-of select="@class"/>");
    }
    
    <xsl:call-template name="annotated-constructor">
        <xsl:with-param name="class-name" select="@class"/>
    </xsl:call-template>
    
    /**
     * Creates a new <xsl:value-of select="@class"/> with all
     * attributes copied from the given <xsl:value-of select="@class"/>.
     */
    public <xsl:value-of select="@class"/>(<xsl:value-of select="@class"/> original) {
    	super(original);
    	<xsl:for-each select="Attribute|Object">
    	this.<xsl:value-of select="@name"/> = original.get<xsl:call-template name="name-initcap"/>();
    	</xsl:for-each>
    }
    
<xsl:apply-templates/>

	@Override
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("<xsl:value-of select="@class"/>:");
	    <xsl:for-each select="Attribute|Object">
	    retStr.append(" <xsl:value-of select="@name"/> = ");
	    retStr.append(<xsl:value-of select="@name"/>);
	    retStr.append(",");
	    </xsl:for-each>
		<xsl:for-each select="CData">
		retStr.append("text = ");
		retStr.append(text);
		retStr.append(",");
		</xsl:for-each>
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    <xsl:if test="@superclass">
	    retStr.append(" [inherited ");
        retStr.append(super.toString());
        retStr.append("]");
        </xsl:if>
	    return retStr.toString();
	}
<xsl:call-template name="children-methods"/>
} // end of class <xsl:value-of select="@class"/>
</xsl:template>

<!-- Returns the correct superclass for the current Class element -->
<xsl:template name="superclass-of-class">
  <xsl:choose>
    <xsl:when test="@superclass"><xsl:value-of select="@superclass"/></xsl:when>
    <xsl:otherwise>OLAPObject</xsl:otherwise>
  </xsl:choose>
</xsl:template> 


<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property)
-->
<xsl:template match="Attribute">
    /** <xsl:copy-of select="Doc"/> */
    private <xsl:call-template name="attribute-type"/> /* */ <xsl:value-of select="@name"/>;
    
    @Accessor
    public <xsl:call-template name="attribute-type"/> /* */ get<xsl:call-template name="name-initcap"/>() {
        return <xsl:value-of select="@name"/>;
    }
    
    @Mutator
    public void set<xsl:call-template name="name-initcap"/>(<xsl:call-template name="attribute-type"/> /* */ newval) {
        <xsl:call-template name="attribute-type"/> /* */ oldval = <xsl:value-of select="@name"/>;
        <xsl:value-of select="@name"/> = newval;
        firePropertyChange("<xsl:value-of select="@name"/>", oldval, newval);
    }
</xsl:template>

<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property)-->
<!-- This is different than attribute because objects considered children of the so the-->
<!-- Parent must be set. -->
<xsl:template match="Object">
    /** <xsl:copy-of select="Doc"/> */
    private <xsl:call-template name="attribute-type"/> /* */ <xsl:value-of select="@name"/>;
    
    @NonProperty
    public <xsl:call-template name="attribute-type"/> /* */ get<xsl:call-template name="name-initcap"/>() {
        return <xsl:value-of select="@name"/>;
    }
    
    @NonProperty
    public void set<xsl:call-template name="name-initcap"/>(<xsl:call-template name="attribute-type"/> /* */ newval) {
        <xsl:call-template name="attribute-type"/> /* */ oldval = <xsl:value-of select="@name"/>;
        if (oldval == newval) {
        	return;
        }
        int overallPosition = childPositionOffset(<xsl:call-template name="attribute-type"/>.class);
        if (<xsl:value-of select="@name"/> != null) {
            fireChildRemoved(<xsl:call-template name="attribute-type"/>.class, oldval, overallPosition);
        }
        <xsl:value-of select="@name"/> = newval;
        if (<xsl:value-of select="@name"/> != null) {
            <xsl:value-of select="@name"/>.setParent(this);
            fireChildAdded(<xsl:call-template name="attribute-type"/>.class, <xsl:value-of select="@name"/>, overallPosition);
        }
	}
</xsl:template>

<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property)
-->
<xsl:template match="CData">
	private String text;
	
	@Accessor
	public String getText() {
		return text;
	}
	
	@Mutator
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		firePropertyChange("text", oldval, newval);
	}

</xsl:template>

<xsl:template name="attribute-type">
  <xsl:choose>
    <xsl:when test="@type"><xsl:value-of select="@type"/></xsl:when>
    <xsl:otherwise>String</xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="Array">
    /** <xsl:copy-of select="Doc"/> */
    private final List&lt;<xsl:value-of select="@type"/>&gt; <xsl:value-of select="@name"/> = new ArrayList&lt;<xsl:value-of select="@type"/>&gt;();
    
    /** Adds the given child object at the specified position, firing an OLAPChildEvent.
     * <p><xsl:copy-of select="Doc"/></p>
     */
    public void add<xsl:call-template name="name-initcap-nonplural"/>(int pos, <xsl:value-of select="@type"/> newChild) {
        <xsl:value-of select="@name"/>.add(pos, newChild);
        newChild.setParent(this);
        int overallPosition = childPositionOffset(<xsl:value-of select="@type"/>.class) + pos;
        fireChildAdded(<xsl:value-of select="@type"/>.class, newChild, overallPosition);
    }

    /** Adds the given child object at the end of the child list, firing an OLAPChildEvent.
     * <p><xsl:copy-of select="Doc"/></p> */
    public void add<xsl:call-template name="name-initcap-nonplural"/>(<xsl:value-of select="@type"/> newChild) {
        add<xsl:call-template name="name-initcap-nonplural"/>(<xsl:value-of select="@name"/>.size(), newChild);
    }
    
    /** 
     * Removes the given child object, firing an OLAPChildEvent if the child was found.
     *
     * @return true if the item was removed (because it was in the list); false if the item was not removed.
     */
    public boolean remove<xsl:call-template name="name-initcap-nonplural"/>(<xsl:value-of select="@type"/> removeChild) {
        int pos = <xsl:value-of select="@name"/>.indexOf(removeChild);
        if (pos != -1) {
            remove<xsl:call-template name="name-initcap-nonplural"/>(pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the child object at the given position, firing an OLAPChildEvent.
     *
     * @return The item that was removed.
     */
    public <xsl:value-of select="@type"/> remove<xsl:call-template name="name-initcap-nonplural"/>(int pos) {
        <xsl:value-of select="@type"/> removedItem = <xsl:value-of select="@name"/>.remove(pos);
        if (removedItem != null) {
            int overallPosition = childPositionOffset(<xsl:value-of select="@type"/>.class) + pos;
            fireChildRemoved(<xsl:value-of select="@type"/>.class, removedItem, overallPosition);
            removedItem.setParent(null);
        }
        return removedItem;
    }

    @NonProperty
    public List&lt;<xsl:value-of select="@type"/>&gt; get<xsl:call-template name="name-initcap"/>() {
        return Collections.unmodifiableList(<xsl:value-of select="@name"/>);
    }
    
</xsl:template>

<xsl:template name="children-methods">

    @Transient @Accessor
    public List&lt;Class&lt;? extends SPObject&gt;&gt; getAllowedChildTypes() {
        return allowedChildTypes;
    }

    public void removeDependency(SPObject dependency) {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");        
    }
    
    @Transient @Accessor
    public List&lt;? extends SPObject&gt; getDependencies() {
        throw new IllegalStateException("Dependency management has not been setup for " + 
            "OLAP objects because they reference each other by name.");
    }

    <xsl:choose>
      <xsl:when test="Array|Object">
    
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List&lt;Class&lt;? extends SPObject&gt;&gt; allowedChildTypes;
    static {
        @SuppressWarnings("unchecked")
        List&lt;Class&lt;? extends SPObject&gt;&gt; childTypes = new ArrayList&lt;Class&lt;? extends SPObject&gt;&gt;(
                    Arrays.asList(
            <xsl:for-each select="Array">
                <xsl:value-of select="@type"/>.class<xsl:if test="position()!=last()">, </xsl:if>
            </xsl:for-each>
            <xsl:variable name="arraySize" select="count(Array)" />
            <xsl:variable name="objectSize" select="count(Object)" />
            <xsl:if test="$arraySize > 0 and $objectSize > 0">,</xsl:if>
            <xsl:for-each select="Object">
                <xsl:value-of select="@type"/>.class<xsl:if test="position()!=last()">, </xsl:if>
            </xsl:for-each>));  
        <xsl:choose>
            <xsl:when test="name() = 'Element' and @class">childTypes.addAll(<xsl:value-of select="@class"/>.allowedChildTypes);</xsl:when>
            <xsl:when test="name() = 'Class' and @superclass">childTypes.addAll(<xsl:value-of select="@superclass"/>.allowedChildTypes);</xsl:when>
        </xsl:choose>
        allowedChildTypes = Collections.unmodifiableList(childTypes);
        
    }
      
    @NonProperty
    public List&lt;SPObject&gt; getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List&lt;SPObject&gt; children = new ArrayList&lt;SPObject&gt;();
        <xsl:for-each select="Array">
        children.addAll(<xsl:value-of select="@name"/>);
        </xsl:for-each>
        <xsl:for-each select="Object">
        if (<xsl:value-of select="@name"/> != null) {
        	children.add(<xsl:value-of select="@name"/>);
        }
        </xsl:for-each>
        return Collections.unmodifiableList(children);
    }
    
    public boolean allowsChildren() {
        return true;
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class&lt;? extends SPObject&gt; childClass) {
        int offset = 0;
        <xsl:for-each select="Array">
        if (<xsl:value-of select="@type"/>.class.isAssignableFrom(childClass)) return offset;
        offset += <xsl:value-of select="@name"/>.size();
        </xsl:for-each>
        <xsl:for-each select="Object">
        if (<xsl:value-of select="@type"/>.class.isAssignableFrom(childClass)) return offset;
        offset += 1;
        </xsl:for-each>
        return offset + super.childPositionOffset(childClass);
    }
    </xsl:when>
    <xsl:otherwise>
    /**
     * Defines an absolute ordering of the child types of this class.
     */
    public static final List&lt;Class&lt;? extends SPObject&gt;&gt; allowedChildTypes =
    <xsl:choose>
        <xsl:when test="name() = 'Element' and @class"><xsl:value-of select="@class"/>.allowedChildTypes;</xsl:when>
        <xsl:when test="name() = 'Class' and @superclass"><xsl:value-of select="@superclass"/>.allowedChildTypes;</xsl:when>
        <xsl:otherwise>Collections.emptyList();</xsl:otherwise>
    </xsl:choose>
        
    @NonProperty
    public List&lt;SPObject&gt; getChildren() {
        return <xsl:choose>
        <xsl:when test="name() = 'Element' and @class">super.getChildren();</xsl:when>
        <xsl:when test="name() = 'Class' and @superclass">super.getChildren();</xsl:when>
        <xsl:otherwise>Collections.emptyList();</xsl:otherwise>
    </xsl:choose>
    }
    
    public boolean allowsChildren() {
        return <xsl:choose>
        <xsl:when test="name() = 'Element' and @class">super.allowsChildren();</xsl:when>
        <xsl:when test="name() = 'Class' and @superclass">super.allowsChildren();</xsl:when>
        <xsl:otherwise>false;</xsl:otherwise>
    </xsl:choose>
    }
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    public int childPositionOffset(Class&lt;? extends SPObject&gt; childClass) {
        return super.childPositionOffset(childClass);
    }
    </xsl:otherwise>
  </xsl:choose>
    @Override
    public void addChildImpl(SPObject child, int index) {
		<xsl:choose>
			<xsl:when test="@type='Join'">
		if (false) {
		
		} else if (child instanceof RelationOrJoin) {
            if (getLeft() == null) {
                setLeft((RelationOrJoin) child);
            } else if (getRight() == null) {
                setRight((RelationOrJoin) child);
            }
        } else {
        	super.addChild(child);
        }
			</xsl:when>
			<xsl:otherwise>
        if (false) {
        <xsl:for-each select="Array">
        } else if (child instanceof <xsl:value-of select="@type"/>) {
            int offset = childPositionOffset(<xsl:value-of select="@type"/>.class);
            if (index &lt; 0 || index &gt; <xsl:value-of select="@name"/>.size()) {
                throw new IllegalArgumentException(
                    "Index out of bounds for this child type. " +
                    "You gave: " + index +
                    ". min= " + 0 +
                    "; max=" + <xsl:value-of select="@name"/>.size());
            }
            add<xsl:call-template name="name-initcap-nonplural"/>(index, (<xsl:value-of select="@type"/>) child);
        </xsl:for-each>
        <xsl:for-each select="Object">
        } else if (child instanceof <xsl:value-of select="@type"/>) {
            set<xsl:call-template name="name-initcap"/>((<xsl:value-of select="@type"/>) child);
        </xsl:for-each>
        } else {
            super.addChildImpl(child, index);
        }
			</xsl:otherwise>
		</xsl:choose>
    }
    
    @Override
    public boolean removeChildImpl(SPObject child) {
		<xsl:choose>
			<xsl:when test="@type='Join'">
		if (false) {
			return false;
		} else if (child instanceof RelationOrJoin) {
            if (getLeft() == child) {
            	setLeft(null);
            	return true;
            } else if (getRight() == child) {
                setRight(null);
                return true;
            }
            return false;
        } else {
        	return super.removeChildImpl(child);
        }
			</xsl:when>
			<xsl:otherwise>
        if (false) {
        	return false;
        <xsl:for-each select="Array">
        } else if (child instanceof <xsl:value-of select="@type"/>) {
            return remove<xsl:call-template name="name-initcap-nonplural"/>((<xsl:value-of select="@type"/>) child);
        </xsl:for-each>
        <xsl:for-each select="Object">
        } else if (child instanceof <xsl:value-of select="@type"/>) {
            set<xsl:call-template name="name-initcap"/>(null);
            return true;
        </xsl:for-each>
        } else {
            return super.removeChildImpl(child);
        }
			</xsl:otherwise>    
		</xsl:choose>    
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
    <xsl:when test="$initcap = 'Array'">Array</xsl:when>
    <xsl:otherwise><xsl:value-of select="substring($initcap, 1, string-length($initcap)-1)"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="Doc">
  <!-- this is handled directly by parent element's template -->
</xsl:template>

<xsl:template match="Code">
<!-- the inline code makes assumptions about the code generator that aren't true for this generator.
     so we omit the inline code.
 -->
</xsl:template>

</xsl:transform>
