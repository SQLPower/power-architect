<%@ page language="java" contentType="text/html" %>
<%@ page import="javax.xml.transform.*"%>
<%@ page import="javax.xml.transform.stream.*"%>
<html>
<head>
<title>JSP sample passing a parameter to XSL</title>
</head>
<body>
<%! String FS = System.getProperty("file.separator"); %>
<%
/**
 * This JSP uses PMA to set param1 in the
*  foo.xsl stylesheet before using the
 * stylesheet to transform foo.xml
 * and outputing the result.
 *
 * Invoke the jsp from the appropriate
 * context for your servlet/jsp server.
 * For example: http://localhost:8080/samples/jspSample.jsp?PMA=HellowWorld!&XML=foo.xml&XSL=foo.xsl
 * This example assumes that foo.xsl and foo.xml
 * are in the same directory. 
 * Output should be Hello (from foo.xsml) and HelloWorld!
 * (value of param1 in foo.xsl).

 *@author Paul Campbell seapwc@halcyon.com
 *@version $Id: jspSample.jsp,v 1.2 2003/02/20 18:41:45 ilene Exp $
 */
 

String paramValue = request.getParameter("PMA");
String xmlFile    = request.getParameter("XML");
String xslFile    = request.getParameter("XSL");

// get the real path for xml and xsl files;
	String ctx = getServletContext().getRealPath("") + FS;
	xslFile = ctx + xslFile;
	xmlFile = ctx + xmlFile;

TransformerFactory tFactory = 
	TransformerFactory.newInstance();
Transformer transformer =
	tFactory.newTransformer(new StreamSource(xslFile));
	transformer.setParameter("param1", paramValue);
	transformer.transform(
		 new StreamSource(xmlFile), new StreamResult(out));
%>
</body>
</html>
