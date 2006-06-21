/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

/**
 * This servlet demonstrates how XSL transformations can be made available as
 * a web service. See the CompileServlet for an example on how stylesheets
 * can be pre-compiled before this servlet is invoked.
 *
 * Note that the XSLTC transformation engine is invoked through the JAXP
 * interface, using the XSLTC "use-classpath" attribute.  The
 * "use-classpath" attribute specifies to the XSLTC TransformerFactory
 * that a precompiled version of the stylesheet (translet) may be available,
 * and that that should be used in preference to recompiling the stylesheet.
 * @author Morten Jorgensen
 * @author Jacek Ambroziak
 */
public final class TransformServlet extends HttpServlet {

    /**
     * Main servlet entry point
     */
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {

	// Initialise the output writer
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();

	// Get the two paramters "class" and "source".
 String transletName = request.getParameter("class");
	String documentURI  = request.getParameter("source");

	try {
	    if ((transletName == null) || (documentURI == null)) {
	        out.println("<h1>XSL transformation error</h1>");
		out.println("The parameters <b><tt>class</tt></b> and " +
			    "<b><tt>source</tt></b> must be specified");
	    }
	    else {
                TransformerFactory tf = TransformerFactory.newInstance();
                try {
                    tf.setAttribute("use-classpath", Boolean.TRUE);
                } catch (IllegalArgumentException iae) {
                    System.err.println(
                           "Could not set XSLTC-specific TransformerFactory "
                         + "attributes.  Transformation failed.");
                }
                Transformer t =
                         tf.newTransformer(new StreamSource(transletName));

		// Start the transformation
		final long start = System.currentTimeMillis();
		t.transform(new StreamSource(documentURI),
                            new StreamResult(out));
		final long done = System.currentTimeMillis() - start;
		out.println("<!-- transformed by XSLTC in "+done+"msecs -->");
	    }
	}
	catch (Exception e) {
	    out.println("<h1>Error</h1>");
	    out.println(e.toString());
	}
    }
}
