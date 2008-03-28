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
package ca.sqlpower;

import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import ca.sqlpower.architect.SQLObject;

public class ArchitectTestCase extends TestCase {
	
	public ArchitectTestCase() {
		super();
	}
	
	public ArchitectTestCase(String name) {
		super(name);
	}

	/**
	 * Compare two maps fairly carefully, and fail() if they differ. Apologies in advance to JUnit purist about the name.
	 * @param expected The Map with the expected values
	 * @param actual The Map with the actual values
	 * @throws AssertionFailedError
	 */
	public static void assertMapsEqual(Map<String,Object> expected, Map<String,Object> actual) throws AssertionFailedError {
		StringBuffer errors = new StringBuffer();
		for (Map.Entry<String,Object> expectedEntry : expected.entrySet()) {
			Object actualValue = actual.get(expectedEntry.getKey());
			Object expectedValue = expectedEntry.getValue();
			if (expectedValue == null) {
				// skip this check (we don't save null-valued properties)
			} else if (actualValue == null) {
				errors.append("Expected entry '"+expectedEntry.getKey()+
						"' missing in actual value map (expected value: '"
						+expectedValue+"')\n");
			} else if (expectedValue instanceof SQLObject) {
				SQLObject eso = (SQLObject) expectedValue;
				SQLObject aso = (SQLObject) actualValue;
				boolean same = eso.getName() == null ?
						aso.getName() == null :
						eso.getName().equals(aso.getName());
				if (!same) {
					errors.append("Value of '"+expectedEntry.getKey()+
							"' differs (expected SQLObject named: '"+expectedValue+
							"'; actual name: '"+actualValue+"')\n");
				}
			} else if ( ! actualValue.equals(expectedValue)) {
				errors.append("Value of '"+expectedEntry.getKey()+
						"' differs (expected: '"+expectedValue+
						"'; actual: '"+actualValue+"')\n");
			}
		}
		assertFalse(errors.toString(), errors.length() > 0);
	}
}
