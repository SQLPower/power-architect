/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.transformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;

/**
 * This class transforms xml content from the InputStream passed, into the
 * format specified by the xslt stylesheet and sends the results to an
 * OutputStream.
 */
public class XsltTransformation
  implements ReportTransformer, URIResolver {

	private File baseDir;
	private File projectDir;
	
	private Map<String, Object> parameters = new HashMap<String, Object>();

	public XsltTransformation() {
	}

	/**
	 * Performs an XSLT transformation using a built-in stylesheet, sending the results
	 * to the OutputStream result.
	 *
	 * @param builtInXsltName the name of the built-in XSLT (part of the classpath)
	 * @param xml The XML that should be transformed
	 * @param result the output stream where the result of the transformation should be written to
	 */
	public void transform(String builtInXsltName, File result, ArchitectSwingSession session)
	  throws Exception {

		InputStream xsltStylesheet = getClass().getResourceAsStream(builtInXsltName);
		transform(xsltStylesheet, result, session);
	}

	/**
	 * Performs an external XSLT transformation, sending the results
	 * to the OutputStream result.
	 *
	 * @param the XSLT that should be run
	 * @param xml The XML that should be transformed
	 * @param result the output stream where the result of the transformation should be written to
	 */
	public void transform(File xsltStylesheet, File output, ArchitectSwingSession session)
	  throws Exception {

		InputStream xslt = new FileInputStream(xsltStylesheet);
		baseDir = xsltStylesheet.getParentFile();
		transform(xslt, output, session);
	}

	/**
	 * Performs an external XSLT transformation, sending the results
	 * to the OutputStream result.
	 *
	 * @param the XSLT that should be run
	 * @param xml The XML that should be transformed
	 * @param result the output stream where the result of the transformation should be written to
	 */
	public void transform(InputStream xsltStylesheet, File output, ArchitectSwingSession session)
	  throws Exception {
		ByteArrayOutputStream sessionAsStream = new ByteArrayOutputStream();
		session.getSaveBehaviour().saveToStream(session, sessionAsStream);

		Source xmlSource = new StreamSource(new ByteArrayInputStream(sessionAsStream.toByteArray()));
		Source xsltSource = new StreamSource(xsltStylesheet);
		FileOutputStream result = new FileOutputStream(output);

		TransformerFactory transFact =
				TransformerFactory.newInstance();

		transFact.setURIResolver(this);
		Transformer trans = transFact.newTransformer(xsltSource);
		
		for (Entry<String, Object> entry : parameters.entrySet()) {
		    trans.setParameter(entry.getKey(), entry.getValue());
		}

		trans.transform(xmlSource, new StreamResult(result));
		result.flush();
		result.close();
		xsltStylesheet.close();
	}

	/**
	 * Resolve embedded XSLTs (e.g: imported through <xsl:import/>
	 * <br/>
	 * The referenced file is first searched in the same directory as the
	 * main XSLT. If that is not defined (because the a built-in stylesheet is
	 * used) the referenced resource is loaded from the classloader.
	 * <br/>
	 * If the resource is still not found, the directory where the project file
	 * is stored is searched for the resource.
	 * <br/>
	 * If the resource is still not found, null is returned
	 * 
	 * @param href An href attribute, which may be relative or absolute.
	 * @param base The base URI against which the first argument will be made absolute if the absolute URI is required.
	 * @return A Source object, or null if the href cannot be resolved, and the processor should try to resolve the URI itself.
	 * 
	 * @throws TransformerException
	 */
	public Source resolve(String href, String base)
			throws TransformerException {

		File referenced = null;
		if (base == null) {
			referenced = new File(href);
		} else {
			referenced = new File(base, href);
		}

		try {
			if (referenced.exists()) {
				return new StreamSource(new FileInputStream(referenced));
			}
			File toUse = null;

			if (baseDir != null) {
				toUse = new File(baseDir, href);
			} else {
				// If baseDir == null, a built-in template was used
				// to the referenced stylesheet is most probably part
				// of the classpath as well
				InputStream in = getClass().getResourceAsStream(href);
				if (in != null) {
					return new StreamSource(in);
				}
			}

			if (toUse.exists()) {
				return new StreamSource(new FileInputStream(toUse));
			}

			if (projectDir != null) {
				toUse = new File(projectDir, href);
			}

			if (toUse.exists()) {
				return new StreamSource(new FileInputStream(toUse));
			}

			return null;

		} catch (FileNotFoundException e) {
			return null;
		}
	}

    @Override
    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }
}
