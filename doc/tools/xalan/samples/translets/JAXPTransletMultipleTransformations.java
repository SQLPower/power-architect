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

import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * Using the TrAX/JAXP 1.1 interface to compile a translet and use it 
 * to perform multiple transformations. The translet implements 
 * the Templates interface. If you want to use the translet to perform a 
 * single transformation, see JAXPTransletOneTransformation.java.
 * 
 * 
 * @author Donald Leslie
 */
public class JAXPTransletMultipleTransformations 
{
 static void doTransform(Templates translet, String xmlInURI, String htmlOutURI)
        throws TransformerException, FileNotFoundException     
  {
    // For each transformation, instantiate a new Transformer, and perform
    // the transformation from a StreamSource to a StreamResult;
    Transformer transformer = translet.newTransformer();
    transformer.transform( new StreamSource(xmlInURI),
                           new StreamResult(new FileOutputStream(htmlOutURI)));
  }

  public static void main(String argv[])        
  { 
    // Set the TransformerFactory system property to generate and use translets.
    // Note: To make this sample more flexible, load properties from a properties file.
    // The setting for the Xalan Transformer is "org.apache.xalan.processor.TransformerFactoryImpl"
    String key = "javax.xml.transform.TransformerFactory";
    String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    Properties props = System.getProperties();
    props.put(key, value);
    
    System.setProperties(props);

    String xslInURI = "todo.xsl";
    
    try
    {
      // Instantiate the TransformerFactory, and use it along with a SteamSource
      // XSL stylesheet to create a translet as a Templates object.
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Templates translet = tFactory.newTemplates(new StreamSource(xslInURI));
    
      // Perform each transformation
      doTransform(translet, "todo.xml", "todo.html");
      System.out.println("Produced todo.html");
    
      doTransform(translet, "todotoo.xml", "todotoo.html");
      System.out.println("Produced todotoo.html");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }    
  } 
}
