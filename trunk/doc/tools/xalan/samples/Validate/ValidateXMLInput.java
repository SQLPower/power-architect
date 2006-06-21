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
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validate the XML input by using SAXParserFactory to turn on namespace awareness and 
 * validation, and a SAX XMLReader to parse the input and report problems to an error 
 * handler.
 * 
 * This sample uses birds.xml with an internal DOCTYPE declaration. As shipped, birds.xml
 * contains an element that violates the declared document type.
 */
public class ValidateXMLInput
{
  
  public static void main(String[] args) 
    throws Exception
  {
    ValidateXMLInput v = new ValidateXMLInput();
    v.validate();
  }

  void validate()
    throws Exception
   {
     // Since we're going to use a SAX feature, the transformer must support 
    // input in the form of a SAXSource.
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if(tfactory.getFeature(SAXSource.FEATURE))
    {
      // Standard way of creating an XMLReader in JAXP 1.1.
      SAXParserFactory pfactory= SAXParserFactory.newInstance();
      pfactory.setNamespaceAware(true); // Very important!
      // Turn on validation.
      pfactory.setValidating(true);
      // Get an XMLReader.
      XMLReader reader = pfactory.newSAXParser().getXMLReader();
  
      // Instantiate an error handler (see the Handler inner class below) that will report any
      // errors or warnings that occur as the XMLReader is parsing the XML input.
      Handler handler = new Handler();
      reader.setErrorHandler(handler);
  
      // Standard way of creating a transformer from a URL.
      Transformer t = tfactory.newTransformer(
        new StreamSource("birds.xsl"));
      
      // Specify a SAXSource that takes both an XMLReader and a URL.
      SAXSource source = new SAXSource(reader,
        new InputSource("birds.xml"));
      
      // Transform to a file.
      try
      {
        t.transform(source, new StreamResult("birds.out"));
      }
      catch (TransformerException te)
      {
        // The TransformerException wraps someting other than a SAXParseException
        // warning or error, either of which should be "caught" by the Handler.
        System.out.println("Not a SAXParseException warning or error: " + te.getMessage());
      }
                                  
      System.out.println("=====Done=====");
    }
    else
      System.out.println("tfactory does not support SAX features!");
  }

  // Catch any errors or warnings from the XMLReader.
  class Handler extends DefaultHandler
  {
    public void warning (SAXParseException spe)
	     throws SAXException
    {
      System.out.println("SAXParseException warning: " + spe.getMessage());
    }    

    public void error (SAXParseException spe)
    	throws SAXException
    {
      System.out.println("SAXParseException error: " + spe.getMessage());
    }     
  }
}
