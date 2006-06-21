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

import java.io.OutputStreamWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Transform {

    public static void main(String[] args){
        Transform app = new Transform();
        app.run(args);
    }

    /**
     * Asks the TransformerFactory to try to load a precompiled version of
     * the translet from the class path to construct a Transformer object.
     * The translet performs the transformation on behalf of the
     * Transformer.transform() method.
     */
    public void run(String[] args){
        String xml = args[0];
        String transletURI = args[1];

        try {
            // Set XSLTC's TransformerFactory implementation as the default
            System.setProperty("javax.xml.transform.TransformerFactory",
                         "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("use-classpath", Boolean.TRUE);
	    Transformer transformer = tf.newTransformer(
                                               new StreamSource(transletURI));

	    StreamSource document = new StreamSource(xml);
            StreamResult result = new StreamResult(
                                           new OutputStreamWriter(System.out));
            transformer.transform(document, result);
        }
	catch (Exception e) {
            System.err.println("Exception: " + e); 
	    e.printStackTrace();
        }
        System.exit(0);
    }

    public void usage() {
        System.err.println("Usage: run <xml_file> <xsl_file>");
        System.exit(1);
    }

}
