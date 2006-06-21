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
import java.io.OutputStreamWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

  /**
   * Use command-line input as a stylesheet parameter.
   */

public class UseStylesheetParam
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
    if(args.length != 1)
    {
      System.err.println("Please pass one string to this program");
      return;
    }
  	// Get the parameter value from the command line.
    String paramValue = args[0];
	
   	TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(new StreamSource("foo.xsl"));

	// Set the parameter. I can't get non-null namespaces to work!!
    transformer.setParameter("param1",	/* parameter name */
               							 paramValue /* parameter value */ );
    
    transformer.transform(new StreamSource("foo.xml"), new StreamResult(new OutputStreamWriter(System.out)));
  }   
}
