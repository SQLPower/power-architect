/* Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 2004-2006.
 * $Id$
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ca.sqlpower.architect.sqlrunner;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.WebRowSet;

/**
 * This guy's primary raison d'etre is to generate an XML output file
 * for use in JUnit testing of the ResultsDecoratorSQL!
 * @version $Id$
 */
public class ResultsDecoratorXML extends ResultsDecorator {
	private static final String SUN_WEBROWSET_IMPL_CLASS = "com.sun.rowset.WebRowSetImpl";
	WebRowSet results;

	public ResultsDecoratorXML(PrintWriter out, Verbosity v) {
		super(out, v);

		try {
			// The class name is uncommitted so subject to change.
			Class c = Class.forName(SUN_WEBROWSET_IMPL_CLASS);
			results = (WebRowSet)c.newInstance();

		} catch (Exception ex){
			throw new IllegalArgumentException(
			"can't load " + SUN_WEBROWSET_IMPL_CLASS + ", check CLASSPATH");
		}
	}

	@Override
	public int write(ResultSet rs) throws SQLException {
		results.writeXml(rs, out);
		return results.getRow();
	}

	@Override
	public void printRowCount(int rowCount) throws IOException {
		System.err.println("RowCount: " + rowCount);

	}

	/* (non-Javadoc)
	 * @see ResultsDecorator#getName()
	 */
	@Override
	public String getName() {
		return "XML";
	}
}
