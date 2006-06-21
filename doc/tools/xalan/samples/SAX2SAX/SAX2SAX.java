
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

/**
 *  Replicate the SimpleTransform sample, explicitly using the SAX model to handle the
 *  stylesheet, the XML input, and the transformation.
 */

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class SAX2SAX
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{

    // Instantiate a TransformerFactory.
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    // Determine whether the TransformerFactory supports The use of SAXSource 
    // and SAXResult
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    { 
      // Cast the TransformerFactory.
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
      // Create a ContentHandler to handle parsing of the stylesheet.
      TemplatesHandler templatesHandler = saxTFactory.newTemplatesHandler();

      // Create an XMLReader and set its ContentHandler.
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(templatesHandler);
    
      // Parse the stylesheet.                       
      reader.parse("birds.xsl");

      //Get the Templates object from the ContentHandler.
      Templates templates = templatesHandler.getTemplates();
      // Create a ContentHandler to handle parsing of the XML source.  
      TransformerHandler handler 
        = saxTFactory.newTransformerHandler(templates);
      // Reset the XMLReader's ContentHandler.
      reader.setContentHandler(handler);  

      // Set the ContentHandler to also function as a LexicalHandler, which
      // includes "lexical" events (e.g., comments and CDATA). 
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      
   	  FileOutputStream fos = new FileOutputStream("birds.out");
      
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");      
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);
      serializer.setOutputStream(fos);
   
      
      // Set the result handling to be a serialization to the file output stream.
      Result result = new SAXResult(serializer.asContentHandler());
      handler.setResult(result);
      
      // Parse the XML input document.
      reader.parse("birds.xml");
      
    	System.out.println("************* The result is in birds.out *************");	
    }	
    else
      System.out.println("The TransformerFactory does not support SAX input and SAX output");
  }
}
