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
