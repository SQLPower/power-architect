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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException; 

  /**
   * Show how to transform a DOM tree into another DOM tree.  
   * This uses the javax.xml.parsers to parse both an XSL file 
   * and the XML file into a DOM, and create an output DOM.
   */
public class DOM2DOM
{
	public static void main(String[] args)
    throws TransformerException, TransformerConfigurationException, FileNotFoundException,
           ParserConfigurationException, SAXException, IOException
  {    
	  TransformerFactory tFactory = TransformerFactory.newInstance();

    if(tFactory.getFeature(DOMSource.FEATURE) && tFactory.getFeature(DOMResult.FEATURE))
    {
      //Instantiate a DocumentBuilderFactory.
      DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

      // And setNamespaceAware, which is required when parsing xsl files
      dFactory.setNamespaceAware(true);
      
      //Use the DocumentBuilderFactory to create a DocumentBuilder.
      DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
      
      //Use the DocumentBuilder to parse the XSL stylesheet.
      Document xslDoc = dBuilder.parse("birds.xsl");

      // Use the DOM Document to define a DOMSource object.
      DOMSource xslDomSource = new DOMSource(xslDoc);

      // Set the systemId: note this is actually a URL, not a local filename
      xslDomSource.setSystemId("birds.xsl");

      // Process the stylesheet DOMSource and generate a Transformer.
      Transformer transformer = tFactory.newTransformer(xslDomSource);

      //Use the DocumentBuilder to parse the XML input.
      Document xmlDoc = dBuilder.parse("birds.xml");
      
      // Use the DOM Document to define a DOMSource object.
      DOMSource xmlDomSource = new DOMSource(xmlDoc);
      
      // Set the base URI for the DOMSource so any relative URIs it contains can
      // be resolved.
      xmlDomSource.setSystemId("birds.xml");
      
      // Create an empty DOMResult for the Result.
      DOMResult domResult = new DOMResult();
  
  	  // Perform the transformation, placing the output in the DOMResult.
      transformer.transform(xmlDomSource, domResult);
	  
	    //Instantiate an Xalan XML serializer and use it to serialize the output DOM to System.out
	    // using the default output format, except for indent="yes"
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);                             
      serializer.setOutputStream(System.out);
      serializer.asDOMSerializer().serialize(domResult.getNode());
	}
    else
    {
      throw new org.xml.sax.SAXNotSupportedException("DOM node processing not supported!");
    }
  }
}
