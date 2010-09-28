/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
