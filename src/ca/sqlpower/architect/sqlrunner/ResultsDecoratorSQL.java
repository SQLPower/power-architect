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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.io.*;
import java.sql.*;

/**
 * Print an SQL ResultSet in SQL-import format.
 * TODO: check all escaped characters needed! Test on PGSQL and DB2 at least...
 * @version $Id$
 */
public class ResultsDecoratorSQL extends ResultsDecorator {
	
	public ResultsDecoratorSQL(PrintWriter out, Verbosity v) {
		super(out, v);
	}
	
	@Override
	public int write(ResultSet rs) throws IOException, SQLException {
		ResultSetMetaData md = rs.getMetaData();
		// This assumes you're not using a Join!!
		String tableName = md.getTableName(1);
		if (tableName == null) {
			tableName = "XXXTABLENAMEXXX";
			System.err.println("Warning: at least one tablename null");
		}
		int colCount = md.getColumnCount();
		StringBuffer sb = new StringBuffer("insert into ").append(tableName).append("(");
		for (int i = 1; i <= colCount; i++) {
			sb.append(md.getColumnName(i));
			if (i != colCount) {
				sb.append(", ");
			}
		}
		sb.append(") values (");
		String insertCommand = sb.toString();
		
		int rowCount = 0;
		while (rs.next()) {
			++rowCount;
			println(insertCommand);		
			for (int i = 1; i <= colCount; i++) {
				String tmp = rs.getString(i);
				if (rs.wasNull()) {
					print("null");
				} else {
					int type = md.getColumnType(i);
					// Don't quote numeric types; quote all others for now.
					switch (type) {
						case Types.BIGINT:
						case Types.DECIMAL:
						case Types.DOUBLE:
						case Types.FLOAT:
						case Types.INTEGER:
						default:
							print(tmp);
							break;
						case Types.CHAR:
						case Types.CLOB:
						case Types.VARCHAR:
						case Types.LONGVARCHAR:
							tmp = tmp.replaceAll("'", "''");
							print("'" + tmp + "'");
							break;
					}
				}
				if (i != colCount) {
					print( ", ");
				}
			}
			println(");");
		}
		return rowCount;
	}

	@Override
	public void printRowCount(int rowCount) throws IOException {
		println("-- RowCount: " + rowCount);
		
	}
	/* (non-Javadoc)
	 * @see ResultsDecorator#getName()
	 */
	@Override
	public String getName() {
		return "SQL";
	}
}
