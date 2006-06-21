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
import java.net.URL;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/*
 * This sample applies the todo.xsl stylesheet to the
 * todo.xml XML document, and returns the transformation
 * output (HTML) to the client browser.
 *
 * IMPORTANT: For this to work, you must place todo.xsl and todo.xml 
 * in the servlet root directory for documents.
 *
 */

public class SimpleXSLTServlet extends HttpServlet {

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
    throws ServletException, IOException, java.net.MalformedURLException
  {
    // The servlet returns HTML.
    response.setContentType("text/html; charset=UTF-8");    
    // Output goes in the response stream.
    PrintWriter out = response.getWriter();
    try
    {	
      TransformerFactory tFactory = TransformerFactory.newInstance();
      //get the real path for xml and xsl files.
      String ctx = getServletContext().getRealPath("") + FS;
      // Get the XML input document and the stylesheet.
      Source xmlSource = new StreamSource(new URL("file", "", ctx+"birds.xml").openStream());
      Source xslSource = new StreamSource(new URL("file", "", ctx+"birds.xsl").openStream());
      // Generate the transformer.
      Transformer transformer = tFactory.newTransformer(xslSource);
      // Perform the transformation, sending the output to the response.
      transformer.transform(xmlSource, new StreamResult(out));
    }
    catch (Exception e)
    {
      out.write(e.getMessage());
      e.printStackTrace(out);    
    }
    out.close();
  }
  
}
