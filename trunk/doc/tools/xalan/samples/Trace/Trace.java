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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.trace.PrintTraceListener;
import org.apache.xalan.trace.TraceManager;
import org.apache.xalan.transformer.TransformerImpl;

/**
 * Sample for demonstrating Xalan "trace" interface.
 * Usage: run in Trace directory: java Trace
 * For an extensions trace sample, run in extensions
 * directory: java Trace 3-java-namespace
 */
public class Trace
{	
  public static void main (String[] args)
	  throws java.io.IOException, 
			 TransformerException, TransformerConfigurationException,
			 java.util.TooManyListenersException, 
			 org.xml.sax.SAXException			 
  {
    String fileName = "foo";
    if (args.length > 0)
      fileName = args[0];

    // Set up a PrintTraceListener object to print to a file.
    java.io.FileWriter fw = new java.io.FileWriter("events.log");  
    java.io.PrintWriter pw = new java.io.PrintWriter(fw, true);
    PrintTraceListener ptl = new PrintTraceListener(pw);

    // Print information as each node is 'executed' in the stylesheet.
    ptl.m_traceElements = true;
    // Print information after each result-tree generation event.
    ptl.m_traceGeneration = true;
    // Print information after each selection event.
    ptl.m_traceSelection = true;
    // Print information whenever a template is invoked.
    ptl.m_traceTemplates = true;
    // Print information whenever an extension call is made.
    ptl.m_traceExtension = true;

    // Set up the transformation    
   	TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(new StreamSource(fileName + ".xsl"));

    // Cast the Transformer object to TransformerImpl.
    if (transformer instanceof TransformerImpl) 
	  {
      TransformerImpl transformerImpl = (TransformerImpl)transformer;
      // Register the TraceListener with a TraceManager associated 
      // with the TransformerImpl.
      TraceManager trMgr = transformerImpl.getTraceManager();
      trMgr.addTraceListener(ptl);
                     
      // Perform the transformation --printing information to
      // the events log during the process.
      transformer.transform
                         ( new StreamSource(fileName + ".xml"), 
                           new StreamResult(new java.io.FileWriter(fileName + ".out")) );
    }
    // Close the PrintWriter and FileWriter.
    pw.close();
    fw.close();
   	System.out.println("**The output is in " + fileName + ".out; the log is in events.log ****");	
    
  }
}
