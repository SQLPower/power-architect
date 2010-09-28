/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.architect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

public class LogWriterTest extends TestCase {
	public static final String MESSAGE = "0123456789abcdefghijklmnopqrstuvwxyz";
	private LogWriter theWriter;
	private File theFile;
	
	public void setUp() throws Exception {
		theFile = File.createTempFile("test", null);
		theFile.deleteOnExit();
		theWriter = new LogWriter(theFile.getAbsolutePath());
	}
	
	public void testInfo() throws IOException {
		theWriter.info(MESSAGE);
		theWriter.close();
		BufferedReader is = new BufferedReader(new FileReader(theFile));
		String line = is.readLine();
		assertEquals("(1)" + ' ' + MESSAGE, line);
		is.close();
	}
}
