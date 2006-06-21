/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package servlet;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;
import java.net.URL;

import org.xml.sax.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/*
 * This sample takes input parameters in the request URL: a URL
 * parameter for the XML input, an xslURL parameter for the stylesheet,
 * and optional stylesheet parameters.
 * To run the equivalent of SimplestXSLServlet (with the documents in the
 * servlet document root directory), the request URL is
 * http://<server/servletpath>servlet.SimpleXSLServlet?URL=file:todo.xml&xslURL=file:todo.xsl
 *
 * Using a stylesheet Processing Instruction:
 * If the XML document includes a stylesheet PI that you want to use, 
 * omit the xslURL parameter.
 *
 * Sending stylesheet parameters: 
 * If, for example, a servlet takes a stylesheet parameter named param1
 * param1 that you want to set to foo, include param1=foo in the URL.
 */

public class XSLTServletWithParams extends HttpServlet {

  /**
   * String representing the file separator characters for the System.
   */
  public final static String FS = System.getProperty("file.separator");
  
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }

  public void doGet (HttpServletRequest request,
                     HttpServletResponse response)
    throws ServletException, IOException
  {
    // The servlet returns HTML; charset is UTF8.
    // See ApplyXSLT.getContentType() to get output properties from <xsl:output>.
    response.setContentType("text/html; charset=UTF-8"); 
    PrintWriter out = response.getWriter();
    try
    {	
      TransformerFactory tFactory = TransformerFactory.newInstance();
      // Get params from URL.
      String xml = getRequestParam(request, "URL");
      String xsl = getRequestParam(request, "xslURL");
      Source xmlSource = null;
      Source xslSource = null;
      Transformer transformer = null;
//get the real path for xml and xsl files.
      String ctx = getServletContext().getRealPath("") + FS;
      
      // Get the XML input document.
      if (xml != null && xml.length()> 0)
        xmlSource = new StreamSource(new URL("file", "", ctx + xml).openStream());
      // Get the stylesheet.
      if (xsl != null && xsl.length()> 0)
        xslSource = new StreamSource(new URL("file", "", ctx + xsl).openStream());
      if (xmlSource != null) // We have an XML input document.
      {
        if (xslSource == null) // If no stylesheet, look for PI in XML input document.
        {
     	    String media= null , title = null, charset = null;
          xslSource = tFactory.getAssociatedStylesheet(xmlSource,media, title, charset);
        }
        if (xslSource != null) // Now do we have a stylesheet?
        {
          transformer = tFactory.newTransformer(xslSource);
          setParameters(transformer, request); // Set stylesheet params.
          // Perform the transformation.
          transformer.transform(xmlSource, new StreamResult(out)); 
        }
        else
          out.write("No Stylesheet!");
      }
      else
        out.write("No XML Input Document!");
    }
    catch (Exception e)
    {
      e.printStackTrace(out);    
    }
    out.close();
  }
  
  // Get parameters from the request URL.
  String getRequestParam(HttpServletRequest request, String param)
  {
	  if (request != null) 
    { 
	    String paramVal = request.getParameter(param); 
		  return paramVal;
	  }
	  return null;
  }
  
  // Set stylesheet parameters from the request URL.
  void setParameters(Transformer transformer, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String) paramNames.nextElement();
      try
      {
        String paramVal = request.getParameter(paramName);
        if (paramVal != null)
          transformer.setParameter(paramName, paramVal);                                            
      }
      catch (Exception e)
      {
      }
    }
  }  
}
