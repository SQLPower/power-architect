/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
// This file uses 4 space indents, no tabs.

import java.io.FileInputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.InputSource;

/**
 *  Very basic utility for applying the DOM L3 XPath API (currently in Last Call)
 *  to an xml file and printing information about the execution of the XPath object 
 *  and the nodes it finds.
 *  Takes 2 arguments:
 *     (1) an xml filename
 *     (2) an XPath expression to apply to the file
 *  Examples:
 *     java ApplyXPathDOM foo.xml /
 *     java ApplyXPathDOM foo.xml /doc/name[1]/@last
 *
 *<p>See also the <a href='http://www.w3.org/TR/2004/NOTE-DOM-Level-3-XPath-20040226'>Document Object Model (DOM) Level 3 XPath Specification</a>.</p>
 * @see XPathEvaluator
 * 
 */
public class ApplyXPathDOM
{
  protected String filename = null;
  protected String xpath = null;

  /** Process input args and execute the XPath.  */
  public void doMain(String[] args)
    throws Exception
  {
    filename = args[0];
    xpath = args[1];

    if ((filename != null) && (filename.length() > 0)
        && (xpath != null) && (xpath.length() > 0))
    {
      // Tell that we're loading classes and parsing, so the time it 
      // takes to do this doesn't get confused with the time to do 
      // the actual query and serialization.
      System.out.println("Loading classes, parsing "+filename+", and setting up serializer");
      
      // Set up a DOM tree to query.
      InputSource in = new InputSource(new FileInputStream(filename));
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      Document doc = dfactory.newDocumentBuilder().parse(in);
      
      // Set up an identity transformer to use as serializer.
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      // Use the DOM L3 XPath API to apply the xpath expression to the doc.
      System.out.println("Querying DOM using "+xpath);
      
      // Create an XPath evaluator and pass in the document.
      XPathEvaluator evaluator = new XPathEvaluatorImpl(doc);
      XPathNSResolver resolver = evaluator.createNSResolver(doc);
      
      // Evaluate the xpath expression
      XPathResult result = (XPathResult)evaluator.evaluate(xpath, doc, resolver, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
      

      // Serialize the found nodes to System.out.
      System.out.println("<output>");
                  
      Node n;
      while ((n = result.iterateNext())!= null)
      {         
        if (isTextNode(n)) {
	    // DOM may have more than one node corresponding to a 
	    // single XPath text node.  Coalesce all contiguous text nodes
	    // at this level
	    StringBuffer sb = new StringBuffer(n.getNodeValue());
	    for (
	      Node nn = n.getNextSibling(); 
	      isTextNode(nn);
	      nn = nn.getNextSibling()
	    ) {
	      sb.append(nn.getNodeValue());
	    }
	    System.out.print(sb);
	}
	else {
         serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(System.out)));
	}
        System.out.println();
      }
      System.out.println("</output>");
    }
    else
    {
      System.out.println("Bad input args: " + filename + ", " + xpath);
    }
  }
  
  /** Decide if the node is text, and so must be handled specially */
  static boolean isTextNode(Node n) {
    if (n == null)
      return false;
    short nodeType = n.getNodeType();
    return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
  }

  /** Main method to run from the command line.    */
  public static void main (String[] args)
    throws Exception
  {
    if (args.length != 2)
    {
      System.out.println("java ApplyXPathDOM filename.xml xpath\n"
                         + "Reads filename.xml and applies the xpath; prints the nodelist found.");
      return;
    }
        
    ApplyXPathDOM app = new ApplyXPathDOM();
    app.doMain(args);
  }	
  
} // end of class ApplyXPathDOM

