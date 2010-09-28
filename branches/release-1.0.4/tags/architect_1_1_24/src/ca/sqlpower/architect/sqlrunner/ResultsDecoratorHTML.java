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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/** Print ResultSet in HTML
 */
class ResultsDecoratorHTML extends ResultsDecorator {
	
	public ResultsDecoratorHTML(PrintWriter out, Verbosity v) {
		super(out, v);
	}
	
	public int write(ResultSet rs) throws IOException, SQLException {

		ResultSetMetaData md = rs.getMetaData();
		int colCount = md.getColumnCount();
		println("<table border=1>");
		print("<tr>");
		for (int i=1; i<=colCount; i++) {
			print("<th>");
			print(md.getColumnLabel(i));
		}
		println("</tr>");
		int rowCount = 0;
		while (rs.next()) {
			++rowCount;
			print("<tr>");
			for (int i=1; i<=colCount; i++) {
				print("<td>");
				print(rs.getString(i));
			}
			println("</tr>");
		}
		println("</table>");
		return rowCount;
	}

	/** Return a printable name for this decorator
	 * @see ResultsDecorator#getName()
	 */
	public String getName() {
		return "HTML";
	}
}
