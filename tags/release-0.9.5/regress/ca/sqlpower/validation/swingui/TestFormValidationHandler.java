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
package ca.sqlpower.validation.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import ca.sqlpower.validation.RegExValidator;

import junit.framework.TestCase;

public class TestFormValidationHandler extends TestCase {

    private FormValidationHandler handler;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        handler = new FormValidationHandler(new StatusComponent());
    }

    protected class Counter {
        int count = 0;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
    public void testEventSupport() throws BadLocationException {
        Integer count = new Integer(0);
        RegExValidator val = new RegExValidator("\\d+");
        final JTextField textField = new JTextField();
        final Counter counter = new Counter();

        final PropertyChangeListener listener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                counter.setCount(counter.getCount()+1);
            }};
        handler.addPropertyChangeListener(listener);
        handler.addValidateObject(textField,val);

        // TextField firs 2 events for setText: clearing then inserting,
        // when the file needs clear or insert new string
        assertEquals("There should be no events", 0, counter.getCount());

        // no need for clear
        textField.setText("10");
        assertEquals("event counter should be 1", 1, counter.getCount());

        // clear and insert
        textField.setText("20");
        assertEquals("event counter should be 3", 3, counter.getCount());

        // just clear
        textField.setText("");
        assertEquals("event counter should be 4", 4, counter.getCount());

        // just insert
        textField.setText("a");
        assertEquals("event counter should be 5", 5, counter.getCount());

        handler.removePropertyChangeListener(listener);
        textField.getDocument().insertString(textField.getDocument().getLength(),"aa",null);
        assertEquals("event counter should be remain 5", 5, counter.getCount());
    }
}
