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

/** Base class for a series of ResultSet printers.
 * @version $Id$
 */
public abstract class ResultsDecorator {
	PrintWriter out;
	Verbosity verbosity;

	ResultsDecorator(PrintWriter wr, Verbosity v) {
		this.out = wr;
		this.verbosity = v;
	}
	
	/** Print the name of this Decorator's output format */
	public abstract String getName();
	
	/** Print the contents of a ResultSet */
	public abstract int write(ResultSet rs) throws IOException, SQLException;
	
	public void printRowCount(int n) throws IOException {
		out.println("Row Count = " + n);
	}
	public void println(String line) throws IOException {
		out.println(line);
	}
	public void println() throws IOException {
		out.println();
	}
	public void print(String lineSeg) throws IOException {
		out.print(lineSeg);
	}

	public void flush() {
		out.flush();
	}

	public void setWriter(PrintWriter out) {
		this.out = out;
	}
}
