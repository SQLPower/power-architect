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

import java.io.File;

/**
 *
 * @author Thomas Kellerer
 */
public class TransformerFactory {
	
	/**
	 * Creates an instance of ReportTransformer based on the extension of
	 * the given template.
	 * <br/>
	 * Currently XSLT and Velocity are supported.
	 * <br/>
	 * The correct transformer is identified by looking at the extension of
	 * the file. The following rules apply
	 * <ul>
	 *   <li>xslt, xsl will create an XsltTransformation</li>
	 *   <li>vm will create a VelocitTransformation</li>
	 * </ul>
	 * 
	 * @param template the template that should be run
	 * @return
	 */
	public static ReportTransformer getTransformer(File template) 
		throws UnknowTemplateTypeException {

		// If no template is defined, the built-in XSLT is used
		if (template == null) return new XsltTransformation();
		
		String fullname = template.getAbsolutePath();
		int pos = fullname.lastIndexOf('.');
		if (pos == -1) throw new UnknowTemplateTypeException();

		String ext = fullname.substring(pos + 1).toLowerCase();
		if (ext.equals("xslt") || ext.equals("xsl")) {
			return new XsltTransformation();
		} else if (ext.equals("vm")) {
			return new VelocityTransformation();
		}
		throw new UnknowTemplateTypeException();
	}
}
