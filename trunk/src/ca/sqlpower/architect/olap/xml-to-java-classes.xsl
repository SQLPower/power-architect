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

<!-- TODO: copy xml subtree of Doc elements verbatim (to preserve HTML in comments) -->

<xsl:template match="/">
package ca.sqlpower.architect.olap;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


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
    }
    
	public String toString() {
		StringBuilder retStr = new StringBuilder();
		retStr.append("<xsl:value-of select="@type"/>:");
	    <xsl:for-each select="Attribute|Object">
	    retStr.append(" <xsl:value-of select="@name"/> = ");
	    retStr.append(<xsl:value-of select="@name"/>);
	    retStr.append(",");
	    </xsl:for-each>
	    retStr = retStr.deleteCharAt(retStr.length()-1);
	    return retStr.toString();
	}
<xsl:apply-templates/>

<xsl:call-template name="children-methods"/>
} // end of element <xsl:value-of select="@type"/>
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
	    retStr = retStr.deleteCharAt(retStr.length()-1);
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

<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property) -->
<xsl:template match="Attribute|Object">
    /** <xsl:copy-of select="Doc"/> */
    private <xsl:call-template name="attribute-type"/> /* */ <xsl:value-of select="@name"/>;
    
    public <xsl:call-template name="attribute-type"/> /* */ get<xsl:call-template name="name-initcap"/>() {
        return <xsl:value-of select="@name"/>;
    }
    
    public void set<xsl:call-template name="name-initcap"/>(<xsl:call-template name="attribute-type"/> /* */ newval) {
        <xsl:call-template name="attribute-type"/> /* */ oldval = <xsl:value-of select="@name"/>;
        <xsl:value-of select="@name"/> = newval;
        pcs.firePropertyChange("<xsl:value-of select="@name"/>", oldval, newval);
    }
</xsl:template>

<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property) -->
<xsl:template match="CData">
	private String text;
	
	public String getText() {
		return text;
	}
	
	public void setText(String newval) {
		String oldval = text;
		text = newval;
		pcs.firePropertyChange("text", oldval, newval);
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
        fireChildAdded(<xsl:value-of select="@type"/>.class, overallPosition, newChild);
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
            removedItem.setParent(null);
            int overallPosition = childPositionOffset(<xsl:value-of select="@type"/>.class) + pos;
            fireChildRemoved(<xsl:value-of select="@type"/>.class, overallPosition, removedItem);
        }
        return removedItem;
    }

    public List&lt;<xsl:value-of select="@type"/>&gt; get<xsl:call-template name="name-initcap"/>() {
        return Collections.unmodifiableList(<xsl:value-of select="@name"/>);
    }
    
</xsl:template>

<xsl:template name="children-methods">
    <xsl:choose>
      <xsl:when test="Array">
    @Override
    public List&lt;OLAPObject&gt; getChildren() {
        /* This might be noticeably more efficient if we use a data structure (ConcatenatedList?) that holds
         * each list and implements optimized get() and iterator() methods instead of just making a new
         * ArrayList with a copy of the union of all the other lists as we are now. */
        List&lt;OLAPObject&gt; children = new ArrayList&lt;OLAPObject&gt;();
        <xsl:for-each select="Array">
        children.addAll(<xsl:value-of select="@name"/>);
        </xsl:for-each>
        return Collections.unmodifiableList(children);
    }
    
    @Override
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
    private int childPositionOffset(Class&lt;? extends OLAPObject&gt; childClass) {
        int offset = 0;
        <xsl:for-each select="Array">
        if (childClass == <xsl:value-of select="@type"/>.class) return offset;
        offset += <xsl:value-of select="@name"/>.size();
        </xsl:for-each>
        return offset;
    }
    </xsl:when>
    <xsl:otherwise>
    @Override
    public List&lt;OLAPObject&gt; getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean allowsChildren() {
        return false;
    }
    </xsl:otherwise>
  </xsl:choose>
    @Override
    public void addChild(OLAPObject child) {
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
            add<xsl:call-template name="name-initcap-nonplural"/>((<xsl:value-of select="@type"/>) child);
        </xsl:for-each>
        <xsl:for-each select="Object">
        } else if (child instanceof <xsl:value-of select="@type"/>) {
            set<xsl:call-template name="name-initcap"/>((<xsl:value-of select="@type"/>) child);
        </xsl:for-each>
        } else {
            super.addChild(child);
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
