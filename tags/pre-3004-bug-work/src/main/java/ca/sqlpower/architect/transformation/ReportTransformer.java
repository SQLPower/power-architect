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

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import java.io.File;

public interface ReportTransformer {

	/**
	 * Applies a built in template to the current project's file, sending the results
	 * to file identified by result
	 *
	 * @param builtInXsltName the name of the built-in XSLT (part of the classpath)
	 * @param xml The XML that should be transformed
	 * @param result the output stream where the result of the transformation should be written to
	 */
	void transform(String builtInTemplate, File result, ArchitectSwingSession session) throws Exception;

	/**
	 * Applies an external template to the current project's file, sending the results
	 * to file identified by result
	 *
	 * @param the XSLT that should be run
	 * @param xml The XML that should be transformed
	 * @param result the output stream where the result of the transformation should be written to
	 */
	void transform(File template, File output, ArchitectSwingSession session) throws Exception;

	/**
	 * Sets a parameter, to be used in the transformation step.
	 */
	void setParameter(String name, Object value);
	
}
