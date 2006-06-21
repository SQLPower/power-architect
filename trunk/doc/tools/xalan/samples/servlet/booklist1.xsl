<?xml version="1.0"?>
<!-- 5630-A23, 5630-A22, (C) Copyright IBM Corporation, 1997, 2000 -->
<!-- All rights reserved. Licensed Materials Property of IBM -->
<!-- Note to US Government users: Documentation related to restricted rights -->
<!-- Use, duplication or disclosure is subject to restrictions set forth in GSA ADP Schedule with IBM Corp. -->
<!-- This page may contain other proprietary notices and copyright information, the terms of which must be observed and followed. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40">
<xsl:output method="html" indent="no"/>

	<!--begin template rule-->

	<!--pattern-->

	<xsl:template match="/">

		<!--action-->

		<HEAD font-size="24pt" color="red">
                  <h1>
                        <p>Books Checked Out</p>
                  </h1>
  	        </HEAD>
                <BODY>
			<xsl:apply-templates/>
                </BODY>

	</xsl:template>

        <xsl:template match="BOOK">
        <p>
            <xsl:value-of select="TITLE"/>
        </p>
	</xsl:template>

	<!--end template rule-->

</xsl:stylesheet>
