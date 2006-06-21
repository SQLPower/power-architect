<?xml version="1.0"?>
<!--Namespaces are global if you set them in the stylesheet element-->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"   
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:my-ext="ext2"
    extension-element-prefixes="my-ext">
    
  <!--The component and its script are in the xalan namespace and define the implementation-->
  <xalan:component prefix="my-ext" elements="timelapse" functions="getdate">
    <xalan:script lang="javascript">
      var multiplier=1;
      // Extension element implementations always take two arguments. The first
      // argument is the XSL Processor context; the second argument is the element.
      function timelapse(xslProcessorContext, elem)
      {
        multiplier=parseInt(elem.getAttribute("multiplier"));
        // The element return value is placed in the result tree.
        // If you do not want a return value, return null.
        return null;
      }
      function getdate(numdays)
      {
        var d = new Date();
        d.setDate(d.getDate() + parseInt(numdays*multiplier));
        return d.toLocaleString();
      }
    </xalan:script>
  </xalan:component>
      
  <xsl:template match="deadline">
    <p><my-ext:timelapse multiplier="2"/>We have received your enquiry and will 
      respond by <xsl:value-of select="my-ext:getdate(string(@numdays))"/></p>
  </xsl:template>

</xsl:stylesheet>