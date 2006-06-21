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

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class Compile {

    public static void main(String[] args){
        Compile app = new Compile();
        app.run(args[0]);
    }

    /**
     * Compiles an XSL stylesheet into a translet, wraps the translet
     * inside a Templates object and dumps it to a file.
     */
    public void run(String xsl) {
        try {
            // Set XSLTC's TransformerFactory implementation as the default
            System.setProperty("javax.xml.transform.TransformerFactory",
                         "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");

	    // Get an input stream for the XSL stylesheet
	    StreamSource stylesheet = new StreamSource(xsl);

	    // The TransformerFactory will compile the stylesheet and
	    // put the translet classes inside the Templates object
	    TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("generate-translet", Boolean.TRUE);
	    Templates templates = factory.newTemplates(stylesheet);
        }
	catch (Exception e) {
            System.err.println("Exception: " + e); 
	    e.printStackTrace();
        }
        System.exit(0);
    }

    private void usage() {
        System.err.println("Usage: compile <xsl_file>");
        System.exit(1);
    }

}
