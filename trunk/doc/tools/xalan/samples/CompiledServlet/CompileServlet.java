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

import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xalan.xsltc.compiler.XSLTC;

/**
 * @author Morten Jorgensen
 * @author Jacek Ambroziak
 */
public class CompileServlet extends HttpServlet {

    /**
     * Main servlet entry point. The servlet reads a stylesheet from the
     * URI specified by the "sheet" parameter. The compiled Java class
     * ends up in the CWD of the web server (a better solution would be
     * to have an environment variable point to a translet directory).
     */
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {

	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
		
	String stylesheetName = request.getParameter("sheet");
	
	out.println("<html><head>");
	out.println("<title>Servlet Stylesheet Compilation</title>");
	out.println("</head><body>");

	if (stylesheetName == null) {
	    out.println("<h1>Compilation error</h1>");
	    out.println("The parameter <b><tt>sheet</tt></b> "+
			"must be specified");
	}
	else {
	    XSLTC xsltc = new XSLTC();

	    xsltc.init();
	    xsltc.compile(new URL(stylesheetName));
	    out.println("<h1>Compilation successful</h1>");
	    out.println("The stylesheet was compiled into the translet "+
			"class "+xsltc.getClassName() + " and is now "+
			"available for transformations on this server.");
	}
	out.println("</body></html>");
    }
}
