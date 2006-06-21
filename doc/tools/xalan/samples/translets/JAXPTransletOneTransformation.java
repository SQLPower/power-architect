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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;


/**
 * Using the TrAX/JAXP 1.1 interface to compile and run a translet. The translet
 * extends the abstract Transformer class and is used to perform a single
 * transformation. If you want to use the translet to perform multiple 
 * transformations, see JAXPTransletMultipleTransformations.java.
 * 
 * @author Donald Leslie
 */
public class JAXPTransletOneTransformation
{
  public static void main(String argv[])
          throws TransformerException, TransformerConfigurationException, IOException, SAXException,
                 ParserConfigurationException, FileNotFoundException
  { 
    // Set the TransformerFactory system property to generate and use a translet.
    // Note: To make this sample more flexible, load properties from a properties file.    
    // The setting for the Xalan Transformer is "org.apache.xalan.processor.TransformerFactoryImpl"
    String key = "javax.xml.transform.TransformerFactory";
    String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    Properties props = System.getProperties();
    props.put(key, value);
    System.setProperties(props);    

    String xslInURI = "todo.xsl";
    String xmlInURI = "todo.xml";
    String htmlOutURI = "todo.html";
    try
    {
      // Instantiate the TransformerFactory, and use it along with a SteamSource
      // XSL stylesheet to create a Transformer.
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer(new StreamSource(xslInURI));
      // Perform the transformation from a StreamSource to a StreamResult;
      transformer.transform(new StreamSource(xmlInURI),
                            new StreamResult(new FileOutputStream(htmlOutURI)));  
      System.out.println("Produced todo.html");  
    }
    catch (Exception e) 
    {
     System.out.println(e.toString());
     e.printStackTrace();
    }      
  }
}
