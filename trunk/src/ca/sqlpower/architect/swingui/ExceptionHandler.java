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

package ca.sqlpower.architect.swingui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.UserSettings;
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
        try {
            logger.error("Uncaught exception", e);
            handleAndReport(e);
        } catch (Throwable doubleTrouble) {
            // if handling the exception results in a new exception, we don't
            // want that fact to go unnoticed!  At this point, we can't trust
            // log4j or our own exception dialog, so we're left with System.err
            doubleTrouble.printStackTrace();
        }
    }
    
    private void handleAndReport(Throwable e) {
        ASUtils.showExceptionDialogNoReport("An unexpected exception has occured: ", e);
        UserSettings settings = context.getUserSettings().getQfaUserSettings();
        if (!settings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true)) return;
        ExceptionReport report = new ExceptionReport(e, DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION.toString(), "Architect");
        
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
