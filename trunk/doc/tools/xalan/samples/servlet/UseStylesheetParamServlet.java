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

/*
Simple Servlet Example using a stylesheet parameter
 */
package servlet;
// Imported TraX classes
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

// Imported SAX classes
import org.xml.sax.SAXException;

// Imported java.io and javax.servlet classes
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class UseStylesheetParamServlet extends HttpServlet {
	

   /**
    * String representing the file separator characters for the System.
    */
    public final static String FS = System.getProperty("file.separator");
    
	PrintWriter out;
	String xslFile, xmlFile, paramValue;
	public void doGet(HttpServletRequest req,
		HttpServletResponse res)
			throws ServletException, IOException {
		try {
			res.setContentType("text/html; charset=UTF-8");
			out = res.getWriter();

      paramValue = req.getParameter("PVAL");
			xmlFile    = req.getParameter("XML");
			xslFile    = req.getParameter("XSL");
 		if (paramValue == null) {
			out.println(
			"<h1>No input for paramValue</h1>");
			return;
		}
 		if ( xmlFile == null) {
			out.println(
			"<h1>No input for xmlFile</h1>");
			return;
		}	
		if ( xslFile == null) {
			out.println(
			"<h1>No input for xslFile</h1>");
			return;
		}
        
        // get the real path for xml and xsl files;
        String ctx = getServletContext().getRealPath("") + FS;
        xslFile = ctx + xslFile;
        xmlFile = ctx + xmlFile;
         
		TransformerFactory tFactory =
			TransformerFactory.newInstance();
		Transformer transformer =
			tFactory.newTransformer(new StreamSource(xslFile));

    // Set the stylesheet parameter (named param1).
			transformer.setParameter("param1", paramValue);
    // Perform the transformation.
			transformer.transform(new StreamSource(xmlFile),
					                  new StreamResult(out));
		}		
    catch (IOException e) {			
			e.printStackTrace();
			System.exit(-1);
		}
		catch (TransformerException e) {
      e.printStackTrace(out);
			return;
		}
	}
}
