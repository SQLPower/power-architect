/*
 * Created on Jun 13, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.qfa;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.DBTree;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUIProject;


public class ExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(ExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        QFAFactory qfaFactory = new ArchitectExceptionReportFactory();
        ExceptionReport r = qfaFactory.createExceptionReport(e);
        ArchitectFrame af = ArchitectFrame.getMainInstance();
        StringBuffer remarks = new StringBuffer();
        if (af != null) {
            SwingUIProject p = af.getProject();
            if (p != null) {
                PlayPen pp = p.getPlayPen();
                if (pp != null) {
                    r.setNumObjectsInPlayPen(pp.getPPComponentCount());
                } else {
                    remarks.append("[playpen was null]");
                }
                DBTree dbt = p.getSourceDatabases();
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
                remarks.append("[architect frame's project was null]");
            }
        } else {
            remarks.append("[architect frame's main instance was null]");
        }

        r.setRemarks(remarks.toString());
        r.postReport();
    }

}
