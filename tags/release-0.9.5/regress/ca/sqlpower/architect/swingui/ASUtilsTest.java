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
package ca.sqlpower.architect.swingui;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

public class ASUtilsTest extends TestCase {

    /** Trivial test, just make sure our class name appears */
    public void testStackTrace() {
        String answer = getPrintOut(new Exception("Boo!"));
        assertTrue(answer.indexOf(this.getClass().getName()) >= 0);
    }

    final String FAKE_CLASS_NAME = "my.test";

    /** Make sure we get the correct number of elements truncated */
    public void testStackTraceArithmetic() {
        final int STACK_DEPTH = 16;
        assertTrue(STACK_DEPTH >= 1+ASUtils.MAX_JRE_ELEMENTS);
        Throwable t = fakeUpException(STACK_DEPTH);

        String answer = getPrintOut(t);
        assertTrue(answer.indexOf(FAKE_CLASS_NAME) >= 0);
        int expectedLength = STACK_DEPTH - ASUtils.MAX_JRE_ELEMENTS;
        System.out.println("ANSER=" + answer);
        System.out.println("EXPECT=" + expectedLength);
        assertTrue(answer.indexOf(expectedLength + " more") >= 0);
    }


    /** Border case : make sure the "more" does not appear if exactly 10 */
    public void testStackTraceBoundary() {
        Throwable t = fakeUpException(10);
        String answer = getPrintOut(t);
        assertFalse(answer.indexOf("more...") >= 0);
    }


    // Private Methods...

    private String getPrintOut(Throwable t) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pout = new PrintWriter(sWriter);
        ASUtils.printStackTrace(t, pout);
        pout.close();
        return sWriter.toString();
    }

    private Throwable fakeUpException(int howMany) {
        StackTraceElement data[] = new StackTraceElement[howMany];
        data[0] = new StackTraceElement(FAKE_CLASS_NAME, "foo", "mytest.java", 123);
        for (int i = 1; i < howMany; i++) {
            data[i] = new StackTraceElement("java.lang.Fake", "method" + i, "yourtest.java", i);
        }
        Throwable t = new RuntimeException();
        t.setStackTrace(data);
        return t;
    }

    public void testClassNameStuff() {
        assertEquals("String", ASUtils.niceClassName(""));
        assertEquals("Object", ASUtils.niceClassName(new Object()));
    }
}
