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
package ca.sqlpower.architect.swingui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;

/**
 * The ExceptionHandler catches uncaught exceptions and handles
 * them gracefully by showing an error message dialog and posting
 * a report to SQL Power.
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(ExceptionHandler.class);
    
    /**
     * The URL to post the error report to if the system property
     * that overrides it isn't defined.
     */
    public static final String DEFAULT_REPORT_URL = "http://bugs.sqlpower.ca/architect/postReport";
    
    /**
     * The session that this exception was caused in.
     */
    private ArchitectSwingSessionContext context;
    
    /**
     * The exception handler is used for handling uncaught exceptions.
     * 
     * @param context
     *            The context is used to get additional information about the
     *            state of the Architect when an uncaught exception is caught
     *            here.
     */
    public ExceptionHandler (ArchitectSwingSessionContext context) {
        super();
        this.context = context;
    }
    
    /**
     * The uncaughtException method displays an exception dialog to the user
     * and posts a report to SQL Power at the url specified in the system
     * properties (or the default if not specified).
     */
    public void uncaughtException(Thread t, Throwable e) {
        SPSUtils.showExceptionDialogNoReport("An unexpected exception has occured: ", e);
        UserSettings settings = context.getUserSettings().getQfaUserSettings();
        if (!settings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true)) return;
        ExceptionReport report = new ExceptionReport(e, DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION, "Architect");
        
        StringBuffer remarks = new StringBuffer();
        Collection<ArchitectSession> sessions = context.getSessions();
        for (ArchitectSession s: sessions) {
            ArchitectSwingSession session = (ArchitectSwingSession) s;
            if (session != null) {
                PlayPen pp = session.getPlayPen();
                if (pp != null) {
                    report.addAdditionalInfo("Number of objects in the play pen", "" + pp.getPPComponentCount());
                } else {
                    remarks.append("[playpen was null]");
                }
                DBTree dbt = session.getSourceDatabases();
                if (dbt != null) {
                    TreeModel dbtm = dbt.getModel();
                    if (dbtm != null) {
                        report.addAdditionalInfo("Number of source connections", "" + dbtm.getChildCount(dbtm.getRoot()));
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
        report.setRemarks(remarks.toString());
        
        report.send();
    }
    
}
