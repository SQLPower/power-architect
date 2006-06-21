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
import java.io.StringWriter;

import java.util.StringTokenizer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * This Brazil handler demonstrates how XSL transformations can be made
 * available as a web service without using a full web server. This class
 * implements the Handler interface from the Brazil project, see:
 * http://www.sun.com/research/brazil/
 *
 * Note that the XSLTC transformation engine is invoked through the JAXP
 * interface, using the XSLTC "use-classpath" attribute.  The
 * "use-from-classpath" attribute specifies to the XSLTC TransformerFactory
 * that a precompiled version of the stylesheet (translet) may be available,
 * and that should be used in preference to recompiling the stylesheet.
 * @author Morten Jorgensen
 */
public class TransformHandler implements Handler {

    private TransformerFactory m_tf = null;

    // These two are used while parsing the parameters in the URL
    private final String PARAM_TRANSLET = "translet=";
    private final String PARAM_DOCUMENT = "document=";
    private final String PARAM_STATS = "stats=";

    // All output goes here:
    private PrintWriter m_out = null;

    /**
     * Dump an error message to output
     */
    public void errorMessage(String message, Exception e) {
	if (m_out == null) {
            return;
        }
	m_out.println("<h1>XSL transformation error</h1>"+message);
	m_out.println("<br>Exception:</br>"+e.toString());
    }

    public void errorMessage(String message) {
	if (m_out == null) return;
	m_out.println("<h1>XSL transformation error</h1>"+message);
    }

    /**
     * This method is run when the Brazil proxy is loaded
     */
    public boolean init(Server server, String prefix) {
	return true;
    }

    /**
     * This method is run for every HTTP request sent to the proxy
     */
    public boolean respond(Request request) throws IOException {

	// Initialise the output buffer
	final StringWriter sout = new StringWriter();
	m_out = new PrintWriter(sout);

	// These two hold the parameters from the URL 'translet' and 'document'
	String transletName = null;
	String document = null;
	String stats = null;

	// Get the parameters from the URL
	final StringTokenizer params = new StringTokenizer(request.query,"&");
	while (params.hasMoreElements()) {
	    final String param = params.nextToken();
	    if (param.startsWith(PARAM_TRANSLET)) {
		transletName = param.substring(PARAM_TRANSLET.length());
	    }
	    else if (param.startsWith(PARAM_DOCUMENT)) {
		document = param.substring(PARAM_DOCUMENT.length());
	    }
	    else if (param.startsWith(PARAM_STATS)) {
		stats = param.substring(PARAM_STATS.length());
	    }
	}

	try {
	    // Make sure that both parameters were specified
	    if ((transletName == null) || (document == null)) {
		errorMessage("Parameters <b><tt>translet</tt></b> and/or "+
			     "<b><tt>document</tt></b> not specified.");
	    }
	    else {
                if (m_tf == null) {
                    m_tf = TransformerFactory.newInstance();
                    try {
                        m_tf.setAttribute("use-classpath", Boolean.TRUE);
                    } catch (IllegalArgumentException iae) {
                        System.err.println(
                            "Could not set XSLTC-specific TransformerFactory "
                          + "attributes.  Transformation failed.");
                    }
                }
                Transformer t =
                     m_tf.newTransformer(new StreamSource(transletName));

		// Do the actual transformation
		final long start = System.currentTimeMillis();
		t.transform(new StreamSource(document),
                            new StreamResult(m_out));
		final long done = System.currentTimeMillis() - start;
		m_out.println("<!-- transformed by XSLTC in "+done+"ms -->");
	    }
	}
	catch (Exception e) {
	    errorMessage("Internal error.",e);
	}

	// Pass the transformation output as the HTTP response
	request.sendResponse(sout.toString());
	return true;
    }


}
