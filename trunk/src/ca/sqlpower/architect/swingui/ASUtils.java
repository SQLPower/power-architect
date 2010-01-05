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

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.object.SPObjectUtils;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTable.TransferStyles;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.JDBCDataSourcePanel;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.ExceptionReport;

/**
 * ASUtils is a container class for static utility methods used
 * throughout the Swing user interface.  "ASUtils" is short for
 * "ArchitectSwingUtils" which is too long to use frequently.
 */
public class ASUtils {
	private static final Logger logger = Logger.getLogger(ASUtils.class);
    
    private static ArchitectSwingSessionContext context;

	private ASUtils() {
        // this constructor never gets called
    }

	/**
	 * Returns the length of the shortest line from p1 to p2.
	 */
	public static double distance(Point p1, Point p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	private Thread focusDebuggerThread = null;
	private boolean focusDebuggerStopping = true;
	private Runnable showFocusOwnerTask = new Runnable() {
			public void run() {
				for (;;) {
					if (focusDebuggerStopping) break;
					logger.debug(java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
                        logger.warn("Interrupted in sleep"); //$NON-NLS-1$
					}
				}
				focusDebuggerThread = null;
			}
		};

	/**
	 * Signals for the focus debugger thread(s) to terminate itself
	 * (themselves).
	 */
	public void stopFocusDebugger() {
		focusDebuggerStopping = true;
	}

	/**
	 * Creates and starts a thread that prints the keyboard focus
	 * owner to System.out once per second.  There is no check to stop
	 * multiple such threads from running in parallel, but a single
	 * call to stopFocusDebugger() should termainate all such threads
	 * within 1 second.
	 */
	public void startFocusDebugger() {
		focusDebuggerThread = new Thread(showFocusOwnerTask);
		focusDebuggerStopping = false;
		focusDebuggerThread.start();
	}

    /**
     * This method sets up the combo box passed as targetDB to contain all of the 
     * source connections. The combo box will also contain the additional connection
     * of "(Target Database)" if it is customized or it will make sure that only one
     * copy of the target's connection is in the list
     */
	public static void setupTargetDBComboBox(final ArchitectSwingSession session, final JComboBox targetDB) {
        JComboBox newTargetDB = new JComboBox();
        JDBCDataSource currentTarget = session.getTargetDatabase().getDataSource();
        newTargetDB.addItem(currentTarget);
        for (JDBCDataSource dbcs : session.getContext().getConnections()) {
            if(!dbcs.equals(currentTarget)) {
                newTargetDB.addItem(dbcs);
            }
        }
        newTargetDB.setSelectedIndex(0);
        targetDB.setModel(newTargetDB.getModel());
        targetDB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDBCDataSource projectDS = session.getTargetDatabase().getDataSource();
                JDBCDataSource comboBoxDS = (JDBCDataSource)((JComboBox)e.getSource()).getSelectedItem();
                if(!projectDS.equals(comboBoxDS)) {
                    projectDS.copyFrom(comboBoxDS);
                }
                setupTargetDBComboBox(session, targetDB);
            }
        });
    }

    /**
     * Pops up a dialog box that lets the user inspect and change the
     * target db's connection spec.  Create from scratch every time
     * just in case the user changed the Target Database from the DBTree.
     */
    public static void showTargetDbcsDialog(
            Window parentWindow,
            final ArchitectSwingSession session,
            final JComboBox targetDB) {
        
        JDialog d = showDbcsDialog(parentWindow, session.getTargetDatabase().getDataSource(), null, false);
        
        d.addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent e){
                    session.getTargetDatabase().getDataSource().setName(Messages.getString("ASUtils.targetDatabase")); //$NON-NLS-1$
                    ASUtils.setupTargetDBComboBox(session, targetDB);
                }
            });
    }
    
    /**
     * Works like the 4-argument version of showDbcsDialog where the last argument
     * (enforceUniqueName) is true.
     * 
     * @param parentWindow
     *            The window that owns the dialog
     * @param dataSource
     *            the data source to edit (null not allowed)
     * @param onAccept
     *            this runnable will be invoked if the user OKs the dialog and
     *            validation succeeds. If you don't need to do anything in this
     *            situation, just pass in null for this parameter.
     */
    public static JDialog showDbcsDialog(
            Window parentWindow,
            JDBCDataSource dataSource,
            final Runnable onAccept) {
        return showDbcsDialog(parentWindow, dataSource, onAccept, true);
    }

    /**
     * Pops up a dialog box that lets the user inspect and change the given db's
     * connection spec.
     * 
     * @param parentWindow
     *            The window that owns the dialog
     * @param dataSource
     *            the data source to edit (null not allowed)
     * @param onAccept
     *            this runnable will be invoked if the user OKs the dialog and
     *            validation succeeds. If you don't need to do anything in this
     *            situation, just pass in null for this parameter.
     * @param enforceUniqueName
     *            controls whether or not the dialog will apply its changes when
     *            the new name for the data source is the same as some other
     *            name in the session context's list of data sources. You almost always
     *            want this to be true.
     */
    public static JDialog showDbcsDialog(
            Window parentWindow,
            JDBCDataSource dataSource,
            final Runnable onAccept,
            boolean enforceUniqueName) {
    
        final DataEntryPanel dbcsPanel = createDataSourceOptionsPanel(dataSource, enforceUniqueName);
        
        Callable<Boolean> okCall = new Callable<Boolean>() {
            public Boolean call() {
                if (dbcsPanel.applyChanges()) {
                    if (onAccept != null) {
                        onAccept.run();
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        };
    
        Callable<Boolean> cancelCall = new Callable<Boolean>() {
            public Boolean call() {
                dbcsPanel.discardChanges();
                return Boolean.TRUE;
            }
        };
    
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                dbcsPanel, parentWindow,
                Messages.getString("ASUtils.databaseConnectionDialogTitle", dataSource.getDisplayName()), //$NON-NLS-1$
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okCall, cancelCall);
    
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
    }

    /**
     * Creates a tabbed panel for editing various aspects of the given data
     * source. Currently, the tabs are for General Options and Kettle Options.
     * 
     * @param ds
     *            The data source to edit
     * @param enforceUniqueName
     *            controls whether or not the dialog will apply its changes when
     *            the new name for the data source is the same as some other
     *            name in the session context's list of data sources. You almost
     *            always want this to be true.
     */
    public static DataEntryPanel createDataSourceOptionsPanel(JDBCDataSource ds, boolean enforceUniqueName) {
        final JDBCDataSourcePanel generalPanel = new JDBCDataSourcePanel(ds);
        generalPanel.setEnforcingUniqueName(enforceUniqueName);
        final KettleDataSourceOptionsPanel kettlePanel = new KettleDataSourceOptionsPanel(ds);

        TabbedDataEntryPanel p = new TabbedDataEntryPanel();
        p.addTab(Messages.getString("ASUtils.datasourceOptionsGeneralTab"), generalPanel); //$NON-NLS-1$
        p.addTab(Messages.getString("ASUtils.datasourceOptionsKettleTab"), kettlePanel); //$NON-NLS-1$
        
        // update kettle fields if/when user picks new driver
        generalPanel.getDataSourceTypeBox().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                JDBCDataSourceType parentType = (JDBCDataSourceType) cb.getSelectedItem();
                kettlePanel.parentTypeChanged(parentType);
            }
        });

        return p;
    }

    public static String lineToString(Line2D.Double l) {
		return "[("+l.x1+","+l.y1+") - ("+l.x2+","+l.y2+")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
     * Returns a list of all the points where line segments of the shapes s1 and
     * s2 intersect.
     */
	public static List<Point2D.Double> getIntersectPoints(Shape s1, Shape s2) {
		List<Point2D.Double> list = new ArrayList<Point2D.Double>();
		PathIterator myPI = s1.getPathIterator(null);
		Line2D.Double myLine = new Line2D.Double();
		float[] myCoords = new float[6];
		while (!myPI.isDone()) {
			int mySegType = myPI.currentSegment(myCoords);
			if (mySegType == PathIterator.SEG_LINETO) {
				myLine.x1 = myLine.x2;
				myLine.y1 = myLine.y2;

				myLine.x2 = myCoords[0];
				myLine.y2 = myCoords[1];
			} else if (mySegType == PathIterator.SEG_MOVETO ||
					mySegType == PathIterator.SEG_CLOSE ) {
				myLine.x1 = myCoords[0];
				myLine.y1 = myCoords[1];

				myLine.x2 = myCoords[0];
				myLine.y2 = myCoords[1];
			} else {
				throw new IllegalStateException(
						"Unsupported my PathIterator type "+mySegType+ //$NON-NLS-1$
						". Current myLine is "+lineToString(myLine)); //$NON-NLS-1$
			}
			myPI.next();

			// if this line has no length, no need to check for intersection
			if (myLine.x1 == myLine.x2 && myLine.y1 == myLine.y2) continue;

			PathIterator otherPI = s2.getPathIterator(null);
			Line2D.Double otherLine = new Line2D.Double();
			float[] otherCoords = new float[6];
			while (!otherPI.isDone()) {
				int otherSegType = otherPI.currentSegment(otherCoords);
				if (otherSegType == PathIterator.SEG_LINETO) {
					otherLine.x1 = otherLine.x2;
					otherLine.y1 = otherLine.y2;

					otherLine.x2 = otherCoords[0];
					otherLine.y2 = otherCoords[1];
				} else if (otherSegType == PathIterator.SEG_MOVETO ||
						otherSegType == PathIterator.SEG_CLOSE ) {
					otherLine.x1 = otherCoords[0];
					otherLine.y1 = otherCoords[1];

					otherLine.x2 = otherCoords[0];
					otherLine.y2 = otherCoords[1];
				} else {
					throw new IllegalStateException(
							"Unsupported other PathIterator type "+otherSegType+ //$NON-NLS-1$
							". Current otherLine is "+lineToString(otherLine)); //$NON-NLS-1$
				}
				otherPI.next();

				// if this line has no length, no need to check for intersection
				if (otherLine.x1 == otherLine.x2 && otherLine.y1 == otherLine.y2) continue;

				Point2D.Double point = new Point2D.Double();
				if ( ASUtils.getLineLineIntersection(myLine,otherLine,point) ) {
					list.add(point);
				}
			}
		}
		return list;
	}
	/**
	 * calculate the intersection point of 2 lines,
	 * copy from http://persistent.info/archives/2004/03/08/java_lineline_intersections
	 * @param l1
	 * @param l2
	 * @param intersection
	 * @return
	 */
	public static boolean getLineLineIntersection(Line2D.Double l1, Line2D.Double l2,
			Point2D.Double intersection) {

		if (!l1.intersectsLine(l2))
			return false;

		double x1 = l1.getX1(), y1 = l1.getY1(), x2 = l1.getX2(), y2 = l1.getY2();
		double x3 = l2.getX1(), y3 = l2.getY1(), x4 = l2.getX2(), y4 = l2.getY2();

		intersection.x = det(det(x1, y1, x2, y2), x1 - x2, det(x3, y3, x4, y4), x3 - x4)
				/ det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		intersection.y = det(det(x1, y1, x2, y2), y1 - y2, det(x3, y3, x4, y4), 	y3 - y4)
				/ det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

		return true;
	}


	static double det(double a, double b, double c, double d) {
		return a * d - b * c;
	}

    /**
     * Returns an icon that is suitable for use as a frame icon image
     * in the Architect.
     */
    public static Image getFrameIconImage() {
        return SPSUtils.createIcon("Architect", "Architect Logo", ArchitectSwingSessionContext.ICON_SIZE).getImage(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the single instance of the swing session context for this app.
     * The reason we need a singleton is because the error reporting mechanism
     * needs to be able to pick up the pieces, even for an uncaught exception.
     * <p>
     * Under all other circumstances, you should be extremely hesitant to treat
     * the session context as a singleton.  We don't really want it to be a
     * singleton. It's more of a dirty little secret that we've let you in on here.
     * If you need the session context for the current session, always always use
     * Session.getContext(), which will give you the context in a much better way.
     * 
     * @deprecated Use {@link ArchitectSwingSession#getContext()} to get your session
     * context.  This method should only be called when launching the app (in main()),
     * and when picking up the pieces while handling an uncaught exception.
     */
    public static ArchitectSwingSessionContext getContext() {
        if (context == null) {
            try {
                context = new ArchitectSwingSessionContextImpl();
                context.setExitAfterAllSessionsClosed(true);
            } catch (SQLObjectException e) {
                showExceptionDialogNoReport(Messages.getString("ASUtils.couldNotLaunchPowerArchitect"), e); //$NON-NLS-1$
                System.exit(1);
            }
        }        
        return context;
    }

    /**
     * 
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog will
     * not have a parent component so it will be displayed on top of 
     * everything.
     * 
     * @deprecated This method will create a dialog, but because it
     * has no parent component, it will stay over everything including
     * ArchitectFrames from other sessions.
     * 
     * @param message A user visible string that should explain the problem
     * @param t The exception that warranted a dialog
     */
    public static void showExceptionDialogNoReport(String message, Throwable t) {
        JFrame f = new JFrame();
        f.setIconImage(getFrameIconImage());
        SPSUtils.showExceptionDialogNoReport(f, message, t);
    }
    
    /**
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog will
     * use the provided component as its parent.
     * 
     * @param parent The parent component that will own the dialog
     * @param message A user-visible message that describes what went wrong
     * @param t The exception that warranted a dialog
     */
    public static void showExceptionDialogNoReport(Component parent, String message, Throwable t) {
        SPSUtils.showExceptionDialogNoReport(parent, message, t);
    }
    
    /**
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog will
     * use the architect frame in the provided session as its parent.
     * <p>
     * Also attempts to post an anonymous description of the error to
     * a central reporting server.
     * 
     * @param session The session that the exception occurred in
     * @param message A user visible string that should describe the problem
     * @param t The exception that warranted a dialog
     */
    public static void showExceptionDialog(ArchitectSwingSession session, String message, Throwable t) {
        try {
            UserSettings settings = context.getUserSettings().getQfaUserSettings();
            if (!settings.getBoolean(QFAUserSettings.EXCEPTION_REPORTING,true)) return;
            ExceptionReport report = new ExceptionReport(t, ExceptionHandler.DEFAULT_REPORT_URL, ArchitectVersion.APP_FULL_VERSION.toString(), "Architect");
            
            if (session != null &&
                    session.getProject() != null &&
                    session.getPlayPen() != null &&
                    session.getSourceDatabases() != null) {
                PlayPen pp = session.getPlayPen();
                report.addAdditionalInfo("Number of objects in the play pen", "" + pp.getPPComponentCount()); //$NON-NLS-1$ //$NON-NLS-2$
                report.addAdditionalInfo("Number of source connections", "" + session.getSourceDatabases().getDatabaseList().size());  //$NON-NLS-1$ //$NON-NLS-2$
                logger.debug(report.toString());
                report.send();
            }
        } catch (Throwable seriousProblem) {
            logger.error("Couldn't generate and send exception report!  Note that this is not the primary problem; it's a side effect of trying to report the real problem.", seriousProblem); //$NON-NLS-1$
            JOptionPane.showMessageDialog(null, "Error reporting failed: "+seriousProblem.getMessage()+"\nAdditional information is available in the application log."); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            JFrame owner = null;
            if (session != null) {
                owner = session.getArchitectFrame();
            } else {
                logger.error("got a null session in showExceptionDialog()"); //$NON-NLS-1$
            }
            SPSUtils.showExceptionDialogNoReport(owner, message, t);
        }
    }

    /**
     * Given a SQLObject as a source that is being duplicated, this method will
     * create a properties object to define how the source can be modified or a
     * duplicate can be made from it to place in or on a target.
     */
    public static DuplicateProperties createDuplicateProperties(ArchitectSwingSession currentSession, SQLObject source) {
        ArchitectSwingSession containingSession = null;
        final List<SQLObject> ancestorList = SQLObjectUtils.ancestorList(source);
        for (ArchitectSession s : currentSession.getContext().getSessions()) {
            ArchitectSwingSession session = (ArchitectSwingSession) s;
            SQLObjectRoot root = session.getRootObject();
            if (ancestorList.contains(root)) {
                containingSession = session;
                break;
            }
        }
        if (containingSession == null) { //The SQLObject source comes from outside this context.
            return new DuplicateProperties(
                    false, //This is technically possible but ridiculous. The Architects could be loaded from different pl.ini files.
                    SQLTable.TransferStyles.COPY,
                    false, 
                    false,
                    true
                    );
        }
        
        if (containingSession != currentSession) { //Duplicating a SQLObject across sessions in the same context.
            return new DuplicateProperties(
                    true, //Have to add the source db to the project if it's missing.
                    SQLTable.TransferStyles.COPY,
                    false, 
                    true,
                    true
                    );
        }
        
        if (!ancestorList.contains(currentSession.getTargetDatabase())) { //Duplicating a SQLObject from a source to the target database.
            return new DuplicateProperties(
                    true,
                    SQLTable.TransferStyles.REVERSE_ENGINEER, 
                    false,
                    true, //This flag should not matter as doing any duplicate from a source to the target should be a reverse engineer.
                    false
                    );
        } else { //Duplicating a SQLObject from the target database to the same target database.
            return new DuplicateProperties(
                    false,
                    SQLTable.TransferStyles.COPY,
                    true,
                    true,
                    true
                    );
        }
    }

    /**
     * This method will take a given column that has been added to a session and
     * update its source column to be the same source as a column in another
     * session. The DuplicateProperties will decide if the new column should
     * have its source updated. Data sources required to update the source
     * column will be added to the new session as needed.
     */
    public static void correctSourceColumn(SQLColumn source, DuplicateProperties duplicateProperties, SQLColumn column, DBTree dbTree)
            throws SQLObjectException {
        if (!duplicateProperties.isPreserveColumnSource()) {
            return;
        }
        SQLColumn sourceColumn;
        if (duplicateProperties.getDefaultTransferStyle() == TransferStyles.REVERSE_ENGINEER) {
            sourceColumn = source;
        } else if (duplicateProperties.getDefaultTransferStyle() == TransferStyles.COPY) {
            sourceColumn = source.getSourceColumn();
        } else {
            sourceColumn = null;
        }
        correctSourceColumn(column, sourceColumn, dbTree);
    }

    /**
     * This method will take a given column that has been added to a session and update its source
     * column to be the same source as a column in another session. Data sources required to update
     * the source column will be added to the new session as needed.
     * <p>
     * This is a helper method for the two public correctSourceColumn methods.
     */
    private static void correctSourceColumn(SQLColumn column, SQLColumn sourceColumn, DBTree dbTree) throws SQLObjectException {
        logger.debug("New column " + column.getName() + " has source " + column.getSourceColumn());
        if (sourceColumn != null && !SQLObjectUtils.isInSameSession(column, sourceColumn)) {
            
            //The source database of the source column ETL lineage from the source column from the import/copy (if you can understand that) 
            SQLDatabase sourceSourceDatabase = SPObjectUtils.getAncestor(sourceColumn, SQLDatabase.class);
            
            //The source data source of the target of this import/copy if it exists (less confusing than above thankfully)
            SPDataSource targetSourceSPDataSource = dbTree.getDuplicateDbcs(sourceSourceDatabase.getDataSource());
            if (targetSourceSPDataSource == null) {
                throw new IllegalStateException("Cannot find target data source " + sourceSourceDatabase + " in the target session but was available in the source.");
            }
            if (!dbTree.dbcsAlreadyExists(targetSourceSPDataSource)) {
                dbTree.addSourceConnection(sourceSourceDatabase.getDataSource());
            }
            SQLDatabase targetSourceDatabase = dbTree.getDatabase(targetSourceSPDataSource);
            
            //set the source column of the column we are creating in the target of the import/copy to the column equivalent in the targetSourceDatabase defined above.
            List<SQLObject> ancestors = SQLObjectUtils.ancestorList(sourceColumn);
            System.out.println(ancestors);
            SQLObject child = targetSourceDatabase;
            for (int i = 2; i < ancestors.size(); i++) {
                SQLObject ancestor = ancestors.get(i);
                System.out.println("Child " + child + " ancestor " + ancestor);
                child = child.getChildByName(ancestor.getName());
            }
            column.setSourceColumn((SQLColumn) child);
            
        }
    }
}
