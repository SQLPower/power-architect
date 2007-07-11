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
package ca.sqlpower.architect.swingui.action;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class HelpAction extends AbstractArchitectAction {
    
    ArchitectSwingSession session;
    
    public HelpAction(ArchitectSwingSession session) {
        super(session, "Help", "Help", "help");
        this.session = session;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String helpHS = "jhelpset.hs";
            ClassLoader cl = getClass().getClassLoader();
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            HelpSet hs = new HelpSet(null, hsURL);
            HelpBroker hb = hs.createHelpBroker();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 
            // Default HelpBroker size is too small, make bigger unless on anciente "VGA" resolution
            if (d.width >= 1024 && d.height >= 800) {
                hb.setSize(new Dimension(1024, 700));
            } else {
                hb.setSize(new Dimension(640, 480));
            }
            CSH.DisplayHelpFromSource helpDisplay = new CSH.DisplayHelpFromSource(hb);
            helpDisplay.actionPerformed(e);

        } catch (Exception ev) {
            setEnabled(false);
            ASUtils.showExceptionDialog(session,
                    "Could not load Help File\n" +
                    "Help function disabled",
                    ev);
        }         
    }
}