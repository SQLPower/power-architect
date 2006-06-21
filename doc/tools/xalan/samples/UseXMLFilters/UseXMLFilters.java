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

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

  /**
   * This example shows how to chain a series of transformations by
   * piping SAX events from one Transformer to another. Each Transformer
   * operates as a SAX2 XMLFilter/XMLReader.
   */
public class UseXMLFilters
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
    // Instantiate  a TransformerFactory.
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    // Determine whether the TransformerFactory supports The use uf SAXSource 
    // and SAXResult
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    { 
      // Cast the TransformerFactory to SAXTransformerFactory.
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
  	  // Create an XMLFilter for each stylesheet.
      XMLFilter xmlFilter1 = saxTFactory.newXMLFilter(new StreamSource("foo1.xsl"));
      XMLFilter xmlFilter2 = saxTFactory.newXMLFilter(new StreamSource("foo2.xsl"));
      XMLFilter xmlFilter3 = saxTFactory.newXMLFilter(new StreamSource("foo3.xsl"));
    
      // Create an XMLReader.
	    XMLReader reader = XMLReaderFactory.createXMLReader();
    
      // xmlFilter1 uses the XMLReader as its reader.
      xmlFilter1.setParent(reader);
    
      // xmlFilter2 uses xmlFilter1 as its reader.
      xmlFilter2.setParent(xmlFilter1);
    
      // xmlFilter3 uses xmlFilter2 as its reader.
      xmlFilter3.setParent(xmlFilter2);
    
      // xmlFilter3 outputs SAX events to the serializer.
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no"); 
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);                      
      serializer.setOutputStream(System.out);
      xmlFilter3.setContentHandler(serializer.asContentHandler());

  	  // Perform the series of transformations as follows:
	    //   - transformer3 gets its parent (transformer2) as the XMLReader/XMLFilter
	    //     and calls transformer2.parse(new InputSource("foo.xml")).
      //   - transformer2 gets its parent (transformer1) as the XMLReader/XMLFilter
	    //     and calls transformer1.parse(new InputSource("foo.xml")). 
      //   - transformer1 gets its parent (reader, a SAXParser) as the XMLReader 
      //     and calls reader.parse(new InputSource("foo.xml")).
	    //   - reader parses the XML document and sends the SAX parse events to transformer1, 
	    //     which performs transformation 1 and sends the output to transformer2.
  	  //   - transformer2 parses the transformation 1 output, performs transformation 2, and 
	    //     sends the output to transformer3.
	    //   - transformer3 parses the transformation 2 output, performs transformation 3,
  	  //     and sends the output to the serializer.
      xmlFilter3.parse(new InputSource("foo.xml"));
    }
  }
}
