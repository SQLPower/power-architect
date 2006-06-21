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

import java.io.OutputStreamWriter;

import javax.xml.namespace.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * JAXP 1.3 XPath API sample.
 * 
 * Basic utility for applying an XPath expression to an input xml file and printing
 * the evaluation result, using JAXP 1.3 XPath API.
 * 
 * Takes 3 arguments:
 * (1) an xml file name
 * (2) an XPath expression to apply to the input document
 * (3) the return type, which is one of the following Strings:
 *     num, bool, str, node, nodeset
 * 
 * Examples:
 * 	java ApplyXPathJAXP foo.xml /doc/name[1]/@last str
 * 	java ApplyXPathJAXP foo.xml /doc/name nodeset
 */
public class ApplyXPathJAXP
{
    public static void main(String[] args)
    {
    	QName returnType = null;
    	
        if (args.length != 3)
        {
            System.err.println("Usage: java ApplyXPathAPI xml_file xpath_expression type");
        }
        
        InputSource xml = new InputSource(args[0]);
        String expr = args[1];
        
        // set the return type
        if (args[2].equals("num")) returnType = XPathConstants.NUMBER;
        else if (args[2].equals("bool")) returnType = XPathConstants.BOOLEAN;
        else if (args[2].equals("str")) returnType = XPathConstants.STRING;
        else if (args[2].equals("node")) returnType = XPathConstants.NODE;
        else if (args[2].equals("nodeset")) returnType = XPathConstants.NODESET;
        else
          System.err.println("Invalid return type: " + args[2]);
        
        // Create a new XPath
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        
        Object result = null;
        try {
          // compile the XPath expression
          XPathExpression xpathExpr = xpath.compile(expr);
          
          // Evaluate the XPath expression against the input document
          result = xpathExpr.evaluate(xml, returnType);
          
          // Print the result to System.out.
          printResult(result);
        }
        catch (Exception e) {
          e.printStackTrace();
        }        
    }
    
    /**
     * Print the type and value of the evaluation result.
     */
    static void printResult(Object result)
      throws Exception
    {
        if (result instanceof Double) {
            System.out.println("Result type: double");
            System.out.println("Value: " + result);
        }
        else if (result instanceof Boolean) {
            System.out.println("Result type: boolean");
            System.out.println("Value: " + ((Boolean)result).booleanValue());
        }	
        else if (result instanceof String) {
            System.out.println("Result type: String");
             System.out.println("Value: " + result);
        }
        else if (result instanceof Node) {
            Node node = (Node)result;
            System.out.println("Result type: Node");
            System.out.println("<output>");
            printNode(node);
            System.out.println("</output>");
        }
        else if (result instanceof NodeList) {
            NodeList nodelist = (NodeList)result;
            System.out.println("Result type: NodeList");
            System.out.println("<output>");
            printNodeList(nodelist);
            System.out.println("</output>");
        }
    }

    /** Decide if the node is text, and so must be handled specially */
    static boolean isTextNode(Node n) 
    {
      if (n == null)
        return false;
      short nodeType = n.getNodeType();
      return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
    }
    
    static void printNode(Node node) 
      throws Exception
    {
      if (isTextNode(node)) {
        System.out.println(node.getNodeValue());       
      }
      else {
        // Set up an identity transformer to use as serializer.
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(System.out)));
      }        
       
    }
    
    static void printNodeList(NodeList nodelist) 
      throws Exception
    {
      Node n;
      
      // Set up an identity transformer to use as serializer.
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      
      for (int i = 0; i < nodelist.getLength(); i++)
      {         
	n = nodelist.item(i);
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
    }
}