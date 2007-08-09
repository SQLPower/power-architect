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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPDataSourcePanel;
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
                        logger.warn("Interrupted in sleep");
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
        SPDataSource currentTarget = session.getPlayPen().getDatabase().getDataSource();
        newTargetDB.addItem(currentTarget);
        for (SPDataSource dbcs : session.getUserSettings().getConnections()) {
            if(!dbcs.equals(currentTarget)) {
                newTargetDB.addItem(dbcs);
            }
        }
        newTargetDB.setSelectedIndex(0);
        targetDB.setModel(newTargetDB.getModel());
        targetDB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SPDataSource projectDS = session.getPlayPen().getDatabase().getDataSource();
                SPDataSource comboBoxDS = (SPDataSource)((JComboBox)e.getSource()).getSelectedItem();
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
        
        JDialog d = showDbcsDialog(parentWindow, session, session.getPlayPen().db.getDataSource(), null);
        
        d.addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent e){
                    session.getPlayPen().getDatabase().getDataSource().setName("(Target Database)");
                    ASUtils.setupTargetDBComboBox(session, targetDB);
                }
            });
    }
    
    /**
     * Pops up a dialog box that lets the user inspect and change the given db's
     * connection spec.
     * 
     * @param parentWindow
     *            The window that owns the dialog
     * @param session
     *            the current session
     * @param dataSource
     *            the data source to edit (null not allowed)
     * @param onAccept
     *            this runnable will be invoked if the user OKs the dialog and
     *            validation succeeds. If you don't need to do anything in this
     *            situation, just pass in null for this parameter.
     */
    public static JDialog showDbcsDialog(
            Window parentWindow,
            final ArchitectSwingSession session,
            SPDataSource dataSource,
            final Runnable onAccept) {
        
        final DataEntryPanel dbcsPanel = createDataSourceOptionsPanel(dataSource);
        
        Action okAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (dbcsPanel.applyChanges()) {
                    if (onAccept != null) {
                        onAccept.run();
                    }
                }
            }
        };
    
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dbcsPanel.discardChanges();
            }
        };
    
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                dbcsPanel, parentWindow,
                "Database Connection: " + dataSource.getDisplayName(),
                DataEntryPanelBuilder.OK_BUTTON_LABEL,
                okAction, cancelAction);
    
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
    }

    /**
     * Creates a tabbed panel for editing various aspects of the given data source.
     * Currently, the tabs are for General Options and Kettle Options.
     * 
     * @param ds The data source to edit
     */
    public static DataEntryPanel createDataSourceOptionsPanel(SPDataSource ds) {
        final SPDataSourcePanel generalPanel = new SPDataSourcePanel(ds);
        final KettleDataSourceOptionsPanel kettlePanel = new KettleDataSourceOptionsPanel(ds);

        TabbedDataEntryPanel p = new TabbedDataEntryPanel();
        p.addTab("General", generalPanel);
        p.addTab("Kettle", kettlePanel);
        
        // update kettle fields if/when user picks new driver
        generalPanel.getDataSourceTypeBox().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                SPDataSourceType parentType = (SPDataSourceType) cb.getSelectedItem();
                kettlePanel.parentTypeChanged(parentType);
            }
        });

        return p;
    }

    public static String lineToString(Line2D.Double l) {
		return "[("+l.x1+","+l.y1+") - ("+l.x2+","+l.y2+")]";
	}

	/**
	 *
	 *
	 */
	public static List <Point2D.Double> getIntersectPoints(Shape s1, Shape s2) {
		List <Point2D.Double>list   = new ArrayList();
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
						"Unsupported my PathIterator type "+mySegType+
						". Current myLine is "+lineToString(myLine));
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
							"Unsupported other PathIterator type "+otherSegType+
							". Current otherLine is "+lineToString(otherLine));
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
	 * Update a potentially-long JMenu with the nth-last items replaced by sub-menus.
	 * If the menu seems to fit the current frame, it is unchanged.
	 * @param frame The parent Frame or JFrame, used to compute insets and to listen for resizes
	 * 	(neither of these is implemented at present).
	 * @param input The JMenu.
	 */
	public static void breakLongMenu(final Window frame, final JMenu input) {

		if ( input.getItemCount() <= 0 )
			return;

		final int windowHeight = frame.getSize().height;
		final int totalRows = input.getItemCount();
		final int preferredHeight = input.getItem(0).getPreferredSize().height;
		final int FUDGE = 3; // XXX find a better way to compute this...

		int rowsPerSubMenu = (windowHeight/ preferredHeight) - FUDGE;
		if ( rowsPerSubMenu < 3 )
			rowsPerSubMenu = 3;
		if (totalRows <= rowsPerSubMenu) {
			return;
		}

		JMenu parentMenu = input;
		JMenu subMenu = new JMenu("More...");
		parentMenu.add(subMenu);

		while (input.getItemCount() > rowsPerSubMenu + 1) {
			final JMenuItem item = input.getItem(rowsPerSubMenu);
			subMenu.add(item);	// Note that this removes it from the original menu!

			if (subMenu.getItemCount() >= rowsPerSubMenu &&
				input.getItemCount() > rowsPerSubMenu + 1 ) {
				parentMenu = subMenu;
				subMenu = new JMenu("More...");
				parentMenu.add(subMenu);
			}
		}


		/** TODO: Resizing the main window does not change the height of the menu.
		 * This is left as an exercise for the reader:
		 * frame.addComponentListener(new ComponentAdapter() {
		 * @Override
		 * public void componentResized(ComponentEvent e) {
		 * JMenu oldMenu = fileMenu;
		 * // Loop over oldMenu, if JMenu, replace with its elements, recursively...!
		 * ASUtils.breakLongMenu(fileMenu);
		 * }
		 * });
		 */
	}

    /**
     * Returns an icon that is suitable for use as a frame icon image
     * in the Architect.
     */
    public static Image getFrameIconImage() {
        return SPSUtils.createIcon("Architect", "Architect Logo", ArchitectSwingSessionContext.ICON_SIZE).getImage();
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
            } catch (ArchitectException e) {
                JOptionPane.showMessageDialog(null, "Could not launch the Power*Architect.\n"
                        + "Stacktrace is available on the Java Console");
                e.printStackTrace();
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
        SPSUtils.showExceptionDialogNoReport(message, t);
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
            ExceptionReport report = new ExceptionReport(t, ExceptionHandler.DEFAULT_REPORT_URL, ArchitectVersion.APP_VERSION, "Architect");
            
            if (session != null &&
                    session.getProject() != null &&
                    session.getPlayPen() != null &&
                    session.getSourceDatabases() != null) {
                PlayPen pp = session.getPlayPen();
                report.addAdditionalInfo("Number of objects in the play pen", "" + pp.getTablePanes().size() + pp.getRelationships().size());
                report.addAdditionalInfo("Number of source connections", "" + session.getSourceDatabases().getDatabaseList().size()); 
                logger.debug(report.toString());
                report.send();
            }
        } catch (Throwable seriousProblem) {
            logger.error("Couldn't generate and send exception report!  Note that this is not the primary problem; it's a side effect of trying to report the real problem.", seriousProblem);
            JOptionPane.showMessageDialog(null, "Error reporting failed: "+seriousProblem.getMessage()+"\nAdditional information is available in the application log.");
        } finally {
            JFrame owner = null;
            if (session != null) {
                owner = session.getArchitectFrame();
            } else {
                logger.error("got a null session in showExceptionDialog()");
            }
            SPSUtils.showExceptionDialogNoReport(owner, message, t);
        }
    }
}
