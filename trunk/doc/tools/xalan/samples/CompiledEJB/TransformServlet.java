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

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Morten Jorgensen
 */
public class TransformServlet extends HttpServlet {

    // Error message used when the XSL transformation bean cannot be created
    private final static String createErrorMsg =
	"<h1>XSL transformation bean error</h1>"+
	"<p>An XSL transformation bean could not be created.</p>";

    // Transformer - "more than meets the eye".
    private TransformHome transformer;

    /**
     * Servlet initializer - look up the bean's home interface
     */
    public void init(ServletConfig config) 
	throws ServletException{
	try{
	    InitialContext context = new InitialContext();
	    Object transformRef = context.lookup("transform");
	    transformer =
		(TransformHome)PortableRemoteObject.narrow(transformRef,
							   TransformHome.class);
	} catch (Exception NamingException) {
	    NamingException.printStackTrace();
	}
    }

    /**
     * Handles "GET" HTTP requests - ie. runs the actual transformation
     */
    public void doGet (HttpServletRequest request, 
		       HttpServletResponse response) 
	throws ServletException, IOException {

	String document = request.getParameter("document");
	String translet = request.getParameter("translet");

	response.setContentType("text/html");

	PrintWriter out = response.getWriter();
	try{
	    // Get the insult from the bean
	    TransformRemote xslt = transformer.create();
	    String result = xslt.transform(document, translet);
	    out.println(result);
	} catch(Exception CreateException){
	    out.println(createErrorMsg);
	}
	out.close();
    }

    public void destroy() {
	System.out.println("Destroy");
    }
}
