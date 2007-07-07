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
package ca.sqlpower.validation;

import junit.framework.TestCase;

/**
 * JUnit tests for RegexValidatator
 */
public class RegExValidatorTest extends TestCase {

    /**
     * If this pattern works, any regex pattern should work
     * assuming that java.util.Regex.* have also been tested.
     */
    public void testValidateDigits() {
        RegExValidator val = new RegExValidator("\\d+");
        // These work
        assertEquals(Status.OK, val.validate("0").getStatus());
        assertEquals(Status.OK, val.validate("42").getStatus());

        // These should not
        assertFalse(Status.OK == val.validate("").getStatus());
        assertFalse(Status.OK == val.validate("123 112").getStatus());
        assertFalse(Status.OK == val.validate("123 ").getStatus());
        assertFalse(Status.OK == val.validate(" 123").getStatus());
        assertFalse(Status.OK == val.validate("abcde").getStatus());
        assertFalse(Status.OK == val.validate("abc123").getStatus()); // uses match, not find
    }

    public void testCaseSensitive() {
        RegExValidator val = new RegExValidator("[a-z]*", "must be all lowercase", true);
        assertEquals(Status.OK, val.validate("abc").getStatus());
        assertFalse(Status.OK == val.validate("ABC").getStatus());
    }

    public void testCaseInsensitive() {
        RegExValidator val = new RegExValidator("[a-z]*", "must be all lowercase", false);
        assertEquals(Status.OK, val.validate("abc").getStatus());
        assertEquals(Status.OK, val.validate("ABC").getStatus());
    }
}
