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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * What it does: this sample creates multiple threads 
 * and runs them. Each thread will be assigned a particular
 * stylesheet. Each thread will run multiple transformations on
 * various xml files using its own transformer.
 * 
 * Note: the flavors used by the transformations can be
 * configured below by changing SOURCE_FLAVOR and
 * RESULT_FLAVOR. XSLTC can also be used by changing
 * USE_XSLTC.
 * 
 * Description of files included with the sample:
 * 
 * foo0.xsl and foo1.xsl: foo0.xsl is the stylesheet used 
 * for transformations by thread #0, foo1.xsl is the stylesheet
 * used by thread #1.
 * 
 * foo0.xml and foo1.xml: foo0.xml and foo1.xml are the XML
 * files used for the first and second transformations done
 * by each thread.
 * 
 * Output will go to *.out files in the TransformThread directory.
 * 
 * @author <a href="mailto:richcao@ca.ibm.com">Richard Cao</a>
 */
public class TransformThread implements Runnable
{    
  // Flavors
  public final static int STREAM = 0;
  public final static int SAX = 1;
  public final static int DOM = 2;
  public final static String[] flavorNames =
    new String[] { "Stream", "SAX", "DOM" };

  // Configurable options
  private static int SOURCE_FLAVOR = STREAM;
    // private static int SOURCE_FLAVOR = SAX;
    // private static int SOURCE_FLAVOR = DOM;
    
  private static int RESULT_FLAVOR = STREAM;
    // private static int RESULT_FLAVOR = SAX;
    // private static int RESULT_FLAVOR = DOM;
  
  private static boolean USE_XSLTC = false;
    // private static boolean useXSLTC = true;


  // Threads
  private final static int NUM_THREADS = 2;
  private static TransformThread INSTANCES[] = null;
  protected Thread m_thread = null;
  
  // Number of transformations per thread
  private final static int NUM_TRANSFORMATIONS = 2;

  // Files names and extensions
  private final static String XML_IN_BASE = "foo";
  private final static String XML_EXT = ".xml";
  private final static String XSL_IN_BASE = "foo";
  private final static String XSL_EXT = ".xsl";
  private final static String FILE_OUT_BASE = "foo_";
  private final static String FILE_OUT_EXT = ".out";

  // Thread identifier
  private int m_thrdNum = -1;

  private InputStream[] m_inStream = null;

  private Source[] m_inSource = null;
  private Result[] m_outResult = null;

  // One Transformer per thread since Transformers
  // are _NOT_ thread-safe
  private Transformer m_transformer = null;

  /** Constructs the TransformThread object
   * @param thrdNum a unique identifier for this object
   */
  public TransformThread(int thrdNum)
  {
    m_thrdNum = thrdNum;

    m_inStream = new InputStream[NUM_TRANSFORMATIONS];
    m_inSource = new Source[NUM_TRANSFORMATIONS];
    m_outResult = new Result[NUM_TRANSFORMATIONS];

    try
    {
      initSource();
      initResult();

      // ensure xslSourceURI is a valid URI
      final String xslSourceFileName = XSL_IN_BASE + m_thrdNum + XSL_EXT;
      final String xslSourceURI = (new File(xslSourceFileName)).toURL().toString();
      StreamSource xslSource = new StreamSource(xslSourceFileName);
      xslSource.setSystemId(xslSourceURI);
      
      // Initialize the tranformer
      m_transformer =
        TransformerFactory.newInstance().newTransformer(xslSource);
      m_thread = new Thread(this);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /** Initialize the results (m_outResult) according
   * to RESULT_FLAVOR
   */
  private void initResult()
  {
    try 
    {
      for (int i = 0; i < NUM_TRANSFORMATIONS; i++)
      {
        switch (RESULT_FLAVOR) 
        {
          case STREAM :
              OutputStream outStream =
                new FileOutputStream(FILE_OUT_BASE + "thread_" 
                  + m_thrdNum + "_transformation_" + i + FILE_OUT_EXT);
                
              m_outResult[i] = new StreamResult(outStream);
            break;

          case SAX :
            DefaultHandler defaultHandler = new DefaultHandler();
            m_outResult[i] = new SAXResult(defaultHandler);
            break;

          case DOM :
            m_outResult[i] = new DOMResult();
            break;
        }  
      }
    }
    catch (Exception e)
    { 
      e.printStackTrace();
      System.exit(1);
    }
  }

  /** Initialize the sources (m_inSource) according
   * to SOURCE_FLAVOR
   */
  private void initSource()
  {
    try 
    {
      for (int i = 0; i < NUM_TRANSFORMATIONS; i++)
      {
        // Ensure we get a valid URI
        final String sourceXMLURI = (new File(XML_IN_BASE + i + XML_EXT)).toURL().toString();
        
        // Open for input
        m_inStream[i] = new FileInputStream(XML_IN_BASE + i + XML_EXT);
        
        switch (SOURCE_FLAVOR)
        {
          case STREAM :
            m_inSource[i] = new StreamSource(m_inStream[i]);
            break;
  
          case SAX :
            m_inSource[i] = new SAXSource(new InputSource(m_inStream[i]));
            break;
  
          case DOM :
            try
            {
              DocumentBuilderFactory dfactory =
                DocumentBuilderFactory.newInstance();
  
              // Must always setNamespaceAware when 
              // building xsl stylesheets
              dfactory.setNamespaceAware(true);
              m_inSource[i] =
                new DOMSource(dfactory.newDocumentBuilder().parse(m_inStream[i]));
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
            break;
        }
  
        if (m_inSource[i] != null)
        {
          // If we don't do this, the transformer 
          // won't know how to resolve relative URLs 
          // in the stylesheet.
          m_inSource[i].setSystemId(sourceXMLURI);
        }
      }
    }
    catch (Exception e)
    { 
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Perform multiple transformations with the same
      // transformer
      for (int i = 0; i < NUM_TRANSFORMATIONS; i++)
      {
        m_transformer.transform(m_inSource[i], m_outResult[i]);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /** Creates thread instances
   */
  private static void initThreads()
  {
    INSTANCES = new TransformThread[NUM_THREADS];

    for (int count = 0; count < NUM_THREADS; count++)
    {
      INSTANCES[count] = new TransformThread(count);
    }
  }
  
  /** Sets the appropriate system properties if XSLTC is
   * to be used (according to USE_XSLTC)
   */
  private static void initSystemProperties()
  {
    if (USE_XSLTC) 
    {
      // Set the TransformerFactory system property if XSLTC is required
      // Note: To make this sample more flexible, load properties from a properties file.
      // The setting for the Xalan Transformer is "org.apache.xalan.processor.TransformerFactoryImpl"
      String key = "javax.xml.transform.TransformerFactory";
      String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
      Properties props = System.getProperties();
      props.put(key, value); 
      System.setProperties(props);
    }
  }

  /**
   * Usage:
   * java TransformThread
   */
  public static void main(String argv[])
  {
    try
    { 
      initSystemProperties();
      initThreads();

      for (int count = 0; count < NUM_THREADS; count++)
      {
        INSTANCES[count].m_thread.start();
      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
