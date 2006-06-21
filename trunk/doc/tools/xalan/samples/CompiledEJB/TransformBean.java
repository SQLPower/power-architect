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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @author Morten Jorgensen
 */
public class TransformBean implements SessionBean {

    private SessionContext m_context = null;
    
    private final static String nullErrorMsg =
	"<h1>XSL transformation error</h1>"+
	"<p>'null' parameters sent to the XSL transformation bean's "+
	"<tt>transform(String document, String translet)</tt> method.</p>";

    private static final String NAMESPACE_FEATURE =
	"http://xml.org/sax/features/namespaces";

    /**
     * Generates HTML from a basic error message and an exception
     */
    private void errorMsg(PrintWriter out, Exception e, String msg) {
	out.println("<h1>Error</h1>");
	out.println("<p>"+msg+"</p><br>");
	out.println(e.toString());
    }

    /**
     * Main bean entry point
     */
    public String transform(String document, String transletName) {

	// Initialise the output stream
	final StringWriter sout = new StringWriter();
	final PrintWriter out = new PrintWriter(sout);

	try {
	    if ((document == null) || (transletName == null)) {
		out.println(nullErrorMsg);
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

                // Do the actual transformation
                final long start = System.currentTimeMillis();
                t.transform(new StreamSource(document),
                            new StreamResult(out));
                final long done = System.currentTimeMillis() - start;
                out.println("<!-- transformed by XSLTC in "+done+"msecs -->");
	    }
	}

	catch (Exception e) {
	    errorMsg(out, e, "Impossible state reached.");
	}

	// Now close up the sink, and return the HTML output in the
	// StringWrite object as a string.
	out.close();
	return sout.toString();
    }

    /**
     *
     */
    public void setSessionContext(SessionContext context) {
	m_context = context;
    }

    // General EJB entry points
    public void ejbCreate() { }
    public void ejbRemove() { }
    public void ejbActivate() { }
    public void ejbPassivate() { }
    public void ejbLoad() { }
    public void ejbStore() { }
}
