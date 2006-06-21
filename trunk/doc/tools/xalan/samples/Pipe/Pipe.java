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
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

  /**
   * This example shows how to chain a series of transformations by
   * piping SAX events from one Transformer to another. Each Transformer
   * operates as a SAX2 XMLFilter/XMLReader.
   */
public class Pipe
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
      // Create a TransformerHandler for each stylesheet.
      TransformerHandler tHandler1 = saxTFactory.newTransformerHandler(new StreamSource("foo1.xsl"));
      TransformerHandler tHandler2 = saxTFactory.newTransformerHandler(new StreamSource("foo2.xsl"));
      TransformerHandler tHandler3 = saxTFactory.newTransformerHandler(new StreamSource("foo3.xsl"));
    
      // Create an XMLReader.
	    XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(tHandler1);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHandler1);

      tHandler1.setResult(new SAXResult(tHandler2));
      tHandler2.setResult(new SAXResult(tHandler3));

      // transformer3 outputs SAX events to the serializer.
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);
      serializer.setOutputStream(System.out);
      tHandler3.setResult(new SAXResult(serializer.asContentHandler()));

	    // Parse the XML input document. The input ContentHandler and output ContentHandler
      // work in separate threads to optimize performance.   
      reader.parse("foo.xml");
    }
  }
}
