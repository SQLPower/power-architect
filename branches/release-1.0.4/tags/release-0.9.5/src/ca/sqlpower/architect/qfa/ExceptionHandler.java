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
/*
 * Created on Jun 13, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.qfa;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;


public class ExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(ExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        QFAFactory qfaFactory = new ArchitectExceptionReportFactory();
        ArchitectSwingSessionContext context = ASUtils.getContext();
        ExceptionReport r = qfaFactory.createExceptionReport(e);
        StringBuffer remarks = new StringBuffer();
        
        Collection<ArchitectSwingSession> sessions = context.getSessions();
        for (ArchitectSwingSession session: sessions) {
            if (session != null) {
                PlayPen pp = session.getPlayPen();
                if (pp != null) {
                    r.setNumObjectsInPlayPen(pp.getPPComponentCount());
                } else {
                    remarks.append("[playpen was null]");
                }
                DBTree dbt = session.getSourceDatabases();
                if (dbt != null) {
                    TreeModel dbtm = dbt.getModel();
                    if (dbtm != null) {
                        r.setNumSourceConnections(dbtm.getChildCount(dbtm.getRoot()));
                    } else {
                        remarks.append("[dbtree's model was null]");
                    }
                } else {
                    remarks.append("[dbtree was null]");
                }
            } else {
                remarks.append("[architect session instance was null]");
            }
        }            

        r.setRemarks(remarks.toString());
        r.postReport(context);
    }
}
