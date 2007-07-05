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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

/**
 * A demonstration of a dialog with a StatusComponent but not Validator;
 * validation is faked by toggling a boolean.
 */
public class StatusComponentDemo {

    static ValidateResult status = ValidateResult.createValidateResult(Status.OK, "");
    static Status innerStatus = Status.OK;

    public static void main(String[] args) {
        final JFrame jx = new JFrame("Test");
        jx.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final StatusComponent statusComponent = new StatusComponent();
        statusComponent.setText("Unknown error");
        statusComponent.setResult(status);
        JPanel p = new JPanel(new BorderLayout());
        jx.add(p);
        p.add(statusComponent, BorderLayout.CENTER);
        JButton toggleButton = new JButton("Toggle");
        p.add(toggleButton, BorderLayout.SOUTH);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Just cycle through all the values over and over
                switch(statusComponent.getResult().getStatus()) {
                case OK:
                    status = ValidateResult.createValidateResult(
                            Status.WARN, "Warning");
                    break;
                case WARN:
                    status = ValidateResult.createValidateResult(
                            Status.FAIL, "Failure");
                    break;
                case FAIL:
                    status = ValidateResult.createValidateResult(
                            Status.OK, "Swell");
                    break;
                }
                statusComponent.setResult(status);
            }
        });
        jx.pack();
        jx.setLocation(400, 400);
        jx.setVisible(true);
    }
}
