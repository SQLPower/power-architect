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
package servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import javax.xml.transform.OutputKeys;

import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.StylesheetRoot;
// SAX2 Imports
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Locator;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xalan.processor.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.XMLFilterImpl;

/*****************************************************************************************************
 *
 * ApplyXSLT supplies the basic
 * functions for transforming XML data using XSL stylesheets.
 *
 * @author Spencer Shepard (sshepard@us.ibm.com)
 * @author R. Adam King (rak@us.ibm.com)
 * @author Tom Rowe (trowe@us.ibm.com)
 * @author Don Leslie (donald_leslie@lotus.com)
 *
 *****************************************************************************************************/

public class ApplyXSLT extends HttpServlet
{

  /**
   * Operational parameters for this class.
   * <p>Request-time values override init-time values which override class defaults.</p>
   * @see #init
   * @serial
   */
  protected ApplyXSLTProperties ourDefaultParameters = null;

  /**
   * String representing the end of line characters for the System.
   */
  public final static String EOL = System.getProperty("line.separator");

  /**
   * String representing the file separator characters for the System.
   */
  public final static String FS = System.getProperty("file.separator");

   /**
   * String representing the current directory for properties files. See init().
   */
  public final static String ROOT = System.getProperty("server.root");
  public static String CURRENTDIR;

  /**
   * Initialize operational parameters from the configuration.
   * @param config Configuration
   * @exception ServletException Never thrown
   */
  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    // If the server.root property --see above-- is null, use current working directory
    // as default location for media.properties.
    if (ROOT != null){
      CURRENTDIR= getServletContext().getRealPath("/WEB-INF/classes/servlet/") + FS;
	  System.out.println ( CURRENTDIR);}
    else
      CURRENTDIR = System.getProperty("user.dir")+ FS;
    
	setDefaultParameters(config);
	
    setMediaProps(config.getInitParameter("mediaURL"));	
  }
  
 /**
   * Sets the default parameters for the servlet from the configuration.
   * Also sets required system properties until we figure out why servlet 
   * sometimess fails to read properties from properties files.
   * @param config Configuration
   */
  protected void setDefaultParameters(ServletConfig config)
  {
    ourDefaultParameters = new DefaultApplyXSLTProperties(config);
  }
  
  /**
   *	Loads the media properties file specified by the given string.
   * @param mediaURLstring Location of the media properties file.  Can be either a full URL or a path relative
   * to the System's server.root /servlets directory.  If this parameter is null,
   * server.root/servlets/media.properties will be used.
   * @see ApplyXSL#CURRENTDIR
   */
  protected void setMediaProps(String mediaURLstring)
  {
    if (mediaURLstring != null)
    {
      URL url = null;
      try
      {
        url = new URL(mediaURLstring);
      }
      catch (MalformedURLException mue1)
      {
        try
        {
          url = new URL("file", "", CURRENTDIR + mediaURLstring);
        }
        catch (MalformedURLException mue2)
        {
          writeLog("Unable to find the media properties file based on parameter 'mediaURL' = "
                   + mediaURLstring, HttpServletResponse.SC_ACCEPTED, mue2);
          url = null;
        }
      }
      if (url != null)
      {
        try
        {
          ourMediaProps = new OrderedProps(url.openStream());
        }
        catch (IOException ioe1)
        {
          writeLog("Exception occurred while opening media properties file: " + mediaURLstring +
                   ".  Media table may be invalid.", HttpServletResponse.SC_ACCEPTED, ioe1);
        }
      }
    }
    else
    {
      String defaultProp = CURRENTDIR + "media.properties";
      try
      {
        ourMediaProps = new OrderedProps(new FileInputStream(defaultProp));
      }
      catch (IOException ioe2)
      {
        writeLog("Default media properties file " + defaultProp + " not found.",
                 HttpServletResponse.SC_ACCEPTED, ioe2);
      }
    }
  }

  public String getMedia(HttpServletRequest request)
  {
    return ourMediaProps.getValue(request.getHeader(HEADER_NAME));
  }
  
  // doPost removed for security reasons due to the possibility of sending
  // unsecure XML and XSL XSLTInputSources through the request input stream

  /**
   * HTTP Get method passed on to process().
   * @param request The request
   * @param response The response
   * @see #process
   * @exception ServletException Never thrown
   * @exception IOException Never thrown
   */
  public void doGet (HttpServletRequest request,
                     HttpServletResponse response)
    throws ServletException, IOException
  {
    try
    {	
      TransformerFactory tFactory = TransformerFactory.newInstance();
      process(tFactory, request, response);
    }
    catch (Exception e)
    {
    }
  }
  
  /**
   * Coordinates applying an XSL stylesheet to XML data using operational parameters.
   * <p>If successfully applied, the result tree will be streamed to the response object
   * and the content type set according to the XSL stylesheet's &lt;xsl:output> element(s).</p>
   * <p>If there is a problem in parsing the XML/XSL or if there is a problem in applying
   * the XSL to the XML, an exception will be streamed to the response object.  The detail
   * of the information returned in the response object will depend on whether we're
   * running in debug mode or not.</p>
   * @param processor implementation of TRaX processor
   * @param request  May contain information relevant to creating XML and XSL XSLTInputSource's
   * @param response Where to write the transformation result
   * @see #getDocument
   * @see #getStylesheet
   * @see #getContentType
   * @see #displayException
   * @see #setStylesheetParams
   * @exception ServletException Never thrown
   * @exception IOException Never thrown
   */
  
  public void process(TransformerFactory tFactory, 
					  HttpServletRequest request,
                      HttpServletResponse response)
    throws ServletException, IOException, SAXException
  {
    boolean debug = ourDefaultParameters.isDebug(request);

    long time = 0;
    if (debug)
      time = System.currentTimeMillis();

    // Listener to be used for all reporting
    ApplyXSLTListener listener = new ApplyXSLTListener();
	listener.out.println("debug is " + debug);

    StreamSource xmlSource = null;
	StreamSource xslSource = null;
    try
    {
      if ((xmlSource = getDocument(request, listener)) == null)
        throw new ApplyXSLTException("getDocument() returned null",
                                     new NullPointerException(),
                                     response.SC_NOT_FOUND);
    }
    catch (ApplyXSLTException axe)
    {
      axe.appendMessage(EOL + "getDocument() resulted in ApplyXSLTException" + EOL
                        + listener.getMessage());
      if (debug) writeLog(axe);
      displayException(response, axe, debug);
      xmlSource = null;
    }
    // creating XSL Stylesheet
    if (xmlSource != null)
	{
      try
      {
	    if ((xslSource = getStylesheet(tFactory, request, xmlSource, listener)) == null)
		{
          throw new ApplyXSLTException("getStylesheet() returned null",
                                       new NullPointerException(),
                                       response.SC_NOT_FOUND);
        }
        // For time being, must "reset" xmlSource after extracting stylesheet PI
		xmlSource = getDocument(request, listener); 
      }
      catch (ApplyXSLTException axe)
      {
        axe.appendMessage(EOL + "getStylesheet() resulted in ApplyXSLTException" + EOL
                          + listener.getMessage());
        if (debug) writeLog(axe);
        displayException(response, axe, debug);
        xslSource = null;
      }
    // perform Transformation
	  
    if ((xmlSource != null) && (xslSource != null))
    {
	  try
	  {
        listener.out.println("Performing transformation...");
		
        Templates templates = tFactory.newTemplates(xslSource);
        Transformer transformer = templates.newTransformer();
        {
          try
          {
            String contentType = null;
			      contentType = getContentType(templates);
            if (contentType != null);
              response.setContentType(contentType);

			      if (transformer instanceof TransformerImpl)
			      {
			        TransformerImpl transformerImpl = (TransformerImpl)transformer;
              transformerImpl.setQuietConflictWarnings(ourDefaultParameters.isNoCW(request));
			      }
			
			      setStylesheetParams(transformer, request);			
	          transformer.transform(xmlSource, new StreamResult(response.getOutputStream()));
			
			      if (debug)              
              writeLog(listener.getMessage(), response.SC_OK);
          }
          catch (Exception exc)
          {
            ApplyXSLTException axe = new ApplyXSLTException
				                     ("Exception occurred during Transformation:"
                                          + EOL + listener.getMessage() + EOL
                                          + exc.getMessage(), 
									              exc,
                                response.SC_INTERNAL_SERVER_ERROR);
            if (debug) writeLog(axe);
            displayException(response, axe, debug);
          }
          finally
          {
            // transformer.reset();
          } // end of try ... catch ... finally
		}
	  }
      catch (/*org.xml.sax.SAX*/Exception saxExc)
      {
        ApplyXSLTException axe = new ApplyXSLTException
			                     ("Exception occurred during ctor/Transformation:"
                                             + EOL + listener.getMessage() + EOL
                                             + saxExc.getMessage(), 
			                					  saxExc,
                                  response.SC_INTERNAL_SERVER_ERROR);
        if (debug) writeLog(axe);
        displayException(response, axe, debug);
      } // end of new try ... catch
    } // end of if((stylesheetRoot != null) ...
    if (debug)
    {
      time = System.currentTimeMillis() - time;
      writeLog("  No Conflict Warnings = " + ourDefaultParameters.isNoCW(request) +
               "  Transformation time: " + time + " ms", response.SC_OK);
    }
  }
  }  

  /**
   * Returns an XML XSLTInputSource DOM.  Attempts will be make to create the DOM from the following
   * sources:
   * <ol>
   * <li>A relative URL specified in the HTTP request's path information. This capability is intended
   * for use by <b>servlet engines that map</b> some or all XML data to be processed at the server.</li>
   * <li>A URL specified in the HTTP request's <code>URL=</code> parameter.  This capability
   * is intended for <b>clients wishing to selectively process</b> XML data at the server.  For
   * security reasons, this URL will be forced to the local IP host.</li>
   * <li>The HTTP request's XML input stream. This capability is intended for use by chained servlets.</li>
   * </ol>
   * @param request May contain or point to the XML XSLTInputSource
   * @param listener To record detailed parsing messages for possible return to requestor
   * @return XML XSLTInputSource DOM, or null if the XSLTInputSource could not be parsed
   * @exception ApplyXSLTException Thrown if exception occurs while handling request
   */
  protected StreamSource getDocument(HttpServletRequest request,
                                     ApplyXSLTListener listener)
    throws ApplyXSLTException
  {
    try
    {
      String xmlURL = null;
      // document from PathInfo
      if ((xmlURL = request.getPathInfo()) != null)
      {
        listener.out.println("Parsing XML Document from PathInfo: " + xmlURL);
        return new StreamSource(new URL("http", ((DefaultApplyXSLTProperties)
                                         ourDefaultParameters).getLocalHost(),
                                         request.getServerPort(),
                                         xmlURL.replace('\\', '/')).openStream());                          
      }
      // document from Request parameter
      if ((xmlURL = ourDefaultParameters.getXMLurl(request)) != null)
      {
        listener.out.println("Parsing XML Document from request parameter: " + xmlURL);
        return new StreamSource(new URL(xmlURL).openStream());
      }
      // document from chain
      String contentType = request.getContentType();
      if ((contentType != null) && contentType.startsWith("text/xml"))
      {
        listener.out.println("Parsing XML Document from request chain");
        return new StreamSource(request.getInputStream());
      }
    }
    catch (IOException ioe)
    {
      throw new ApplyXSLTException(ioe, HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception e)
    {
      throw new ApplyXSLTException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    return null;
  }

  /**
   * Returns a Templates (StylesheetRoot) object.  Attempts will be make to create the Stylesheet 
   * from the followingsources:
   * <ol>
   * <li>A URL specified in the HTTP request's <code>xslURL=</code> parameter.  This capability
   * is intended for clients wishing to selectively override the server algorithm for applying XSL
   * stylesheets.  For security reasons, this URL will be forced to the local IP host.</li>
   * <li>XML association.  XML documents may contain references to one or more stylesheets using
   * <a HREF="http://www.w3.org/TR/1999/PR-xml-stylesheet-19990114">this</a> W3C proposed recommendation.
   * If the XML document does contain such references, a best match will be chosen based on the browser
   * type making the request and the default association.  This capability enables relationships to be
   * defined between client capabilities and stylesheets capable of acting on these capabilities.</li>
   * <li>A configured default stylesheet URL</li>
   * </ol>
   * @param request May contain or point to the XSL XSLTInputSource
   * @param xmlSource  May point to the XSL XSLTInputSource
   * @param listener To record detailed parsing messages for possible return to requestor
   * @return XSL XSLTInputSource, or null if the request could not be parsed
   * @see #makeDocument
   * @see #getMedia
   * @see #STYLESHEET_ATTRIBUTE
   * @see #getXSLURLfromDoc
   * @see #toAcceptLanguageConnection
   * @exception ApplyXSLTException Thrown if exception occurs while handling request
   */
  protected StreamSource getStylesheet(TransformerFactory tFactory,
				   		  			   HttpServletRequest request,
                                       StreamSource xmlSource,
                                       ApplyXSLTListener listener)
    throws ApplyXSLTException
  {
    try
    {
      //stylesheet URL from request
      String xslURL = ((DefaultApplyXSLTProperties) ourDefaultParameters).getXSLRequestURL(request);

      if (xslURL != null)
        listener.out.println("Parsing XSL Stylesheet Document from request parameter: "
                             + xslURL);
      else
      {
        // find stylesheet from XML Document, Media tag preference
        if (xmlSource != null){
          listener.out.println("calling getXSLURLfromDoc and getMedia " + getMedia(request) );
          xslURL = getXSLURLfromDoc(xmlSource, STYLESHEET_ATTRIBUTE, getMedia(request), tFactory);
        }
        if (xslURL != null)
          listener.out.println("Parsing XSL Stylesheet Document from XML Document tag: " + xslURL);
        else
          // Configuration Default
          if ((xslURL = ourDefaultParameters.getXSLurl(null)) != null)
            listener.out.println("Parsing XSL Stylesheet Document from configuration: " + xslURL);
      }
      return new StreamSource(xslURL);
    }
    catch (IOException ioe)
    {
      throw new ApplyXSLTException(ioe, HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception e)
    {
      throw new ApplyXSLTException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns the response content type specified by the media-type and encoding attributes of
   * the &lt;xsl:output> element(s) of the stylesheet.
   * @param xslSourceRoot XSL Stylesheet to be examined for &lt;xsl:output> elements.
   * @return The response content type (MIME type and charset) of the stylesheet output
   * @see #process
   */
  public String getContentType(Templates templates)
  {
    Properties oprops = templates.getOutputProperties();
    String encoding = oprops.getProperty(OutputKeys.ENCODING);  
          String media = oprops.getProperty(OutputKeys.MEDIA_TYPE);
          if (media != null)
          {
      if (encoding != null)
        return media + "; charset=" + encoding;
      return media;
          }
          else
          {
            String method = oprops.getProperty(OutputKeys.METHOD);
            if (method.equals("html"))
                    return "text/html";
            else if (method.equals("text"))
                    return "text/plain";
            else 
                    return "text/xml";
          }
  }  
  

  /**
   * Defines and sets select top-level XSL stylesheet variables from the HTTP request, which
   * can be evaluated using &lt;xsl:param-variable&gt;.  The following variables will be
   * automatically set:
   * <dl>
   * <dt><i>ParameterName</i></dt>
   * <dd>Each non-reserved request parameter returned from request.getParameterNames().  If a
   *     parameter contains more than a single value, only the first value is available.</dd>
   * <dt>servlet-RemoteAddr</dt>
   * <dd>Contains String output from request.getRemoteAddr(), which is the IP address
   *     of the client machine.</dd>
   * <dt>servlet-RemoteHost</dt>
   * <dd>Contains String output from request.getRemoteHost(), which is the host name
   *     of the client machine.</dd>
   * <dt>servlet-RemoteUser</dt>
   * <dd>Contains String output from request.getRemoteUser(), which was the user name
   *     accepted by the server to grant access to this servlet.</dd>
   * <dt>servlet-Request</dt>
   * <dd>Contains the request object.</dd>
   * </dl>
   * @param xslprocessor Where to register parameters to be set
   * @param request Provides access to all meaningful parameters to set
   * @see #process
   */
  public void setStylesheetParams(Transformer transformer, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String) paramNames.nextElement();
      try
      {
        String[] paramVals = request.getParameterValues(paramName);
        if (paramVals != null)
            transformer.setParameter(paramName, new XString(paramVals[0]));
                                            
      }
      catch (Exception e)
      {
      }
    }
    try
    {
      transformer.setParameter("servlet-RemoteAddr", new XString(request.getRemoteAddr()));
                                      
    }
    catch (Exception e)
    {
    }
    try
    {
      transformer.setParameter("servlet-RemoteHost", new XString(request.getRemoteHost()));
                                      
    }
    catch (Exception e)
    {
    }
    try
    {
      transformer.setParameter("servlet-RemoteUser", new XString(request.getRemoteUser()));
                                      
    }
    catch (Exception e)
    {
    }
  }


  /**
   * Writes the following information to the servlet log:
   * <ol>
   * <li>HTTP status code</li>
   * <li>Message</li>
   * <li>Stack trace</li>
   * </ol>
   * @param axe Contains valid HTTP status code, message, and stack trace (optional)
   */
  protected void writeLog(ApplyXSLTException axe)
  {
    writeLog(axe.getMessage(), axe.getStatusCode(), axe.getException());
  }

  /**
   * Writes the following information to the servlet log:
   * <ol>
   * <li>HTTP status code</li>
   * <li>Message</li>
   * <li>Stack trace</li>
   * </ol>
   * @param msg Message to be logged
   * @param statusCode Valid status code from javax.servlet.http.HttpServletResponse
   * @param t Used to generate stack trace (may be =null to suppress stack trace)
   */
  protected void writeLog(String msg, int statusCode, Throwable t)
  {
    if (t == null)
      writeLog(msg, statusCode);
    else
    {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(bytes, true);
      System.out.println("Exception is " + t.getClass().getName());
      t.printStackTrace(writer);
      log("HTTP Status Code: " + statusCode + " - " + msg + EOL + bytes.toString());
    }
  }

  /**
   * Writes the following information to the servlet log:
   * <ol>
   * <li>HTTP status code</li>
   * <li>Message</li>
   * </ol>
   * @param msg Message to be logged
   * @param statusCode Valid status code from javax.servlet.http.HttpServletResponse
   */
  protected void writeLog(String msg, int statusCode)
  {
    log("HTTP Status Code: " + statusCode + " - " + msg);
  }

  /**
   * Invokes response.sendError setting an HTTP status code and optionally an error message
   * as an HTML page.
   * <p>If running in debug mode, also try to return a stack trace of the exception and
   * and xml/xsl processor messages.</p>
   * @param response Where to stream the exception to
   * @param xse The wrapper which contains the exception and its HTTP status code
   * @param debug Indicates whether to include stack trace, etc.
   */
  protected void displayException(HttpServletResponse response, ApplyXSLTException xse, boolean debug)
  {
    String mesg = xse.getMessage();
    if (mesg == null)
      mesg = "";
    else mesg = "<B>" + mesg + "</B>";
    StringTokenizer tokens = new StringTokenizer(mesg, EOL);
    StringBuffer strBuf = new StringBuffer();
    while (tokens.hasMoreTokens())
      strBuf.append(tokens.nextToken() + EOL + "<BR>");
    mesg = strBuf.toString();
    if (debug)
    {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(bytes, true);
      xse.getException().printStackTrace(writer);
      mesg += " <PRE> " + bytes.toString() + " </PRE> ";
    }
    response.setContentType("text/html");
    try
    {
      response.sendError(xse.getStatusCode(), mesg);
    }
    catch (IOException ioe)
    {
      System.err.println("IOException is occurring when sendError is called");
    }
  }

  /**
   * Mapping of HTTP request's user-Agent values to stylesheet media= values.
   * <p>This mapping is defined by a file pointed to by the operational parameter "mediaURL" which can
   *  either contain a full URL or a path relative to the System's server.root /servlets directory.</p>
   * @see #setMediaProps
   * @see #getMedia
   * @serial
   */
  protected OrderedProps ourMediaProps = null;

  /**
   * Returns a connection which respects the Accept-Language header of the HTTP request.  This
   * is useful when XSL files are internationalized for use with Web servers which respect this
   * header.
   * <p>For example, Apache 1.3.6 may be configured for multiviews.  Under this configuration,
   * requests for http://myhost/index.html would return http://myhost/index.html.fr to French browsers
   * and http://myhost/index.html.en to English browsers.</p>
   * @param url Location to connect to
   * @param request Could contain an Accept-Language header
   * @return An Accept-Language-enabled URL connection
   * @see #getStylesheet
   */
  protected URLConnection toAcceptLanguageConnection(URL url, HttpServletRequest request)
    throws Exception
  {
    URLConnection tempConnection = url.openConnection();
    tempConnection.setRequestProperty("Accept-Language", request.getHeader("Accept-Language"));
    return tempConnection;
  }


  /**
   * Returns the XSL stylesheet URL associated with the specified XML document.  If multiple XSL
   * stylesheets are associated with the XML document, preference will be given to the stylesheet
   * which contains an attribute name/value pair that corresponds to the specified attributeName
   * and attributeValue.
   * @param xmlSource XML XSLTInputSource to be searched for associated XSL stylesheets
   * @param attributeName  Attribute name to provide preferential matching
   * @param attributeValue Attribute value to provide preferential matching
   * @return The preferred XSL stylesheet URL, or null if no XSL stylesheet association is found
   * @see #getStylesheet
   */
  public static String getXSLURLfromDoc(StreamSource xmlSource,
                                        String attributeName,
                                        String attributeValue,
                                        TransformerFactory tFactory)
  {
    String tempURL = null, returnURL = null;
    try
    {
	  DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      Node sourceTree = docBuilder.parse(xmlSource.getInputStream());
      for(Node child=sourceTree.getFirstChild(); null != child; child=child.getNextSibling())
      {
        if(Node.PROCESSING_INSTRUCTION_NODE == child.getNodeType())
        {
          ProcessingInstruction pi = (ProcessingInstruction)child;
          if(pi.getNodeName().equals("xml-stylesheet"))
          {
            PIA pia = new PIA(pi);
            if("text/xsl".equals(pia.getAttribute("type")))
            {
              tempURL = pia.getAttribute("href");
              String attribute = pia.getAttribute(attributeName);
              if ((attribute != null) && (attribute.indexOf(attributeValue) > -1))
                return tempURL;
              if (!"yes".equals(pia.getAttribute("alternate")))
                returnURL = tempURL;
            }
          }
        }
      }
    }
    catch(Exception saxExc)
    {
    }
    return returnURL;
  }  

 /**
   * The attribute name in the <?xml-stylesheet> tag used in stylesheet selection.
   */
  protected static final String STYLESHEET_ATTRIBUTE = "media";

  /**
   *	The HTTP Header used for matching the Stylesheet attribute via the
   * media properties file selected.
   */
  protected static final String HEADER_NAME = "user-Agent";
}

/**
 *  Stores the keys and values from a file (similar to a properties file) and
 *  can return the first value which has a key contained in its string.
 *  File can have comment lines starting with '#" and for each line the entries are
 *  separated by tabs and '=' char.
 */
class OrderedProps
{

  /**
   * Stores the Key and Values as an array of Strings
   */
  private Vector attVec = new Vector(15);

  /**
   * Constructor.
   * @param inputStream Stream containing the properties file.
   * @exception IOException Thrown if unable to read from stream
   */
  OrderedProps(InputStream inputStream)
    throws IOException
  {
    BufferedReader input  = new BufferedReader(new InputStreamReader(inputStream));
    String currentLine, Key = null;
    StringTokenizer currentTokens;
    while ((currentLine = input.readLine()) != null)
    {
      currentTokens = new StringTokenizer(currentLine, "=\t\r\n");
      if (currentTokens.hasMoreTokens()) Key = currentTokens.nextToken().trim();
      if ((Key != null) && !Key.startsWith("#") && currentTokens.hasMoreTokens())
      {
        String temp[] = new String[2];
        temp[0] = Key; temp[1] = currentTokens.nextToken().trim();
        attVec.addElement(temp);
      }
    }
  }

  /**
   * Iterates through the Key list and returns the first value for whose
   * key the given string contains.  Returns "unknown" if no key is contained
   * in the string.
   * @param s String being searched for a key.
   * @return Value for key found in string, otherwise "unknown"
   */
  String getValue(String s)
  {
    int i, j = attVec.size();
    for (i = 0; i < j; i++)
    {
      String temp[] = (String[]) attVec.elementAt(i);
      if (s.indexOf(temp[0]) > -1)
        return temp[1];
    }
    return "unknown";
  }
}

/**
 * Parses a processing instruction's (PI) attributes for easy retrieval.
 */
class PIA
{

  private Hashtable piAttributes = null;

  /**
   * Constructor.
   * @param pi The processing instruction whose attributes are to be parsed
   */
  PIA(ProcessingInstruction pi)
  {
    piAttributes = new Hashtable();
    StringTokenizer tokenizer = new StringTokenizer(pi.getNodeValue(), "=\"");
    while(tokenizer.hasMoreTokens())
    {
      piAttributes.put(tokenizer.nextToken().trim(), tokenizer.nextToken().trim());
    }
  }

  /**
   * Returns value of specified attribute.
   *  @param name Attribute name
   *  @return Attribute value, or null if the attribute name does not exist
   */
  String getAttribute(String name)
  {
    return (String) piAttributes.get(name);
  }  
}
