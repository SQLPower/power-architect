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
// Imported TraX classes
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.xalan.lib.sql.DefaultConnectionPool;
import org.apache.xalan.lib.sql.ConnectionPoolManager;


// Imported java classes
import java.io.StringReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *  Use the TraX interface to perform a transformation in the simplest manner possible
 *  (3 statements).
 */
public class ExternalConnection
{
	public static void main(String[] args)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException, IOException
  {

  // Create a connection to the database server
  // Up the connection pool count for testing
  DefaultConnectionPool cp = new DefaultConnectionPool();
  cp.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
  cp.setURL("jdbc:derby:sampleDB");
  //cp.setUser("sa");
  //cp.setPassword("");
  cp.setMinConnections(10);
  cp.setPoolEnabled(true);

  // Now let's register our connection pool so we can use
  // in a stylesheet
  ConnectionPoolManager pm = new ConnectionPoolManager();
  pm.registerPool("extpool", cp);


  // Use the static TransformerFactory.newInstance() method to instantiate
  // a TransformerFactory. The javax.xml.transform.TransformerFactory
  // system property setting determines the actual class to instantiate --
  // org.apache.xalan.transformer.TransformerImpl.
	TransformerFactory tFactory = TransformerFactory.newInstance();

  // Grab the Name of the Stylesheet from the commad line
  if (args.length == 0)
  {
    System.out.println("You must provide the path and name to a stylesheet to process");
    System.exit(0);
  }
  
  String stylesheet = args[0];
  System.out.println("Transforming Stylesheet " + stylesheet);
  
	// Use the TransformerFactory to instantiate a Transformer that will work with
	// the stylesheet you specify. This method call also processes the stylesheet
  // into a compiled Templates object.
	Transformer transformer = tFactory.newTransformer(
        new StreamSource(stylesheet));

	// For this transformation, all the required information is in the stylesheet, so generate 
  // a minimal XML source document for the input.
  // Note: the command-line processor (org.apache.xalan.xslt.Process) uses this strategy when 
  // the user does not provide an -IN parameter.
  StringReader reader =
              new StringReader("<?xml version=\"1.0\"?> <doc/>");

  // Use the Transformer to apply the associated Templates object to an XML document
	// and write the output to a file.
	transformer.transform(
        new StreamSource(reader),
        new StreamResult(new FileOutputStream("dbtest-out.html")));

	System.out.println("************* The result is in dbtest-out.html *************");
  
  cp.setPoolEnabled(false);
  }
}
