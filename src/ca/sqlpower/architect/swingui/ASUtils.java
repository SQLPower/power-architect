package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.qfa.ExceptionReport;
import ca.sqlpower.architect.qfa.QFAFactory;
import ca.sqlpower.architect.swingui.action.DBCSOkAction;

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
	 * Short-form convenience method for
	 * <code>new ArchitectSwingUtils.LabelValueBean(label,value)</code>.
	 */
	public static LabelValueBean lvb(String label, Object value) {
		return new LabelValueBean(label, value);
	}

	/**
	 * Returns the length of the shortest line from p1 to p2.
	 */
	public static double distance(Point p1, Point p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * Useful for combo boxes where you want the user to see the label
	 * but the code needs the value (only useful when the value's
     * toString() method isn't).
	 */
	public static class LabelValueBean {
		String label;
		Object value;

		public LabelValueBean(String label, Object value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel()  {
			return this.label;
		}

		public void setLabel(String argLabel) {
			this.label = argLabel;
		}

		public Object getValue()  {
			return this.value;
		}

		public void setValue(Object argValue) {
			this.value = argValue;
		}

		/**
		 * Just returns the label.
		 */
		public String toString() {
			return label;
		}
	}

	public static final FileFilter ARCHITECT_FILE_FILTER =
		new FileExtensionFilter("Architect Project Files", new String[] {"arc", "architect"});

	public static final FileFilter TEXT_FILE_FILTER =
		new FileExtensionFilter("Text Files ", new String[] {"txt"});

	public static final FileFilter SQL_FILE_FILTER =
		new FileExtensionFilter("SQL Script Files", new String[] {"sql","ddl"});

	public static final FileFilter INI_FILE_FILTER =
		new FileExtensionFilter(".INI Files", new String[] {"ini"});

	public static final FileFilter EXE_FILE_FILTER =
		new FileExtensionFilter(".EXE Files", new String[] {"exe"});

	public static final FileFilter JAR_ZIP_FILE_FILTER =
		new FileExtensionFilter("Java JAR Files", new String[] {"jar", "zip"});

	public static final FileFilter LOG_FILE_FILTER =
		new FileExtensionFilter("Log Files", new String[] {"log"});

    public static final FileFilter XML_FILE_FILTER =
        new FileExtensionFilter("XML Files", new String[] {"xml"});

    public static final FileFilter PDF_FILE_FILTER =
        new FileExtensionFilter("Portable Document (PDF) Files", new String[] {"pdf"});

    public static final FileFilter CSV_FILE_FILTER =
        new FileExtensionFilter("Comma-Separated Value Files", new String[] {"csv"});

    public static final FileFilter HTML_FILE_FILTER =
        new FileExtensionFilter("HTML Files", new String[] {"html"});

    public static final FileFilter BATCH_FILE_FILTER =
        new FileExtensionFilter("Batch Scripts", new String[] {"bat"});

	public static class FileExtensionFilter extends FileFilter {

		protected LinkedHashSet<String> extensions;
		protected String name;

		/**
		 * Creates a new filter which only accepts directories and
		 * files whose names end with a dot "." followed by one of the
		 * given strings.
		 *
		 * @param name The name of this filter to show to the user
		 * @param extensions an array of lowercase filename extensions.
		 */
		public FileExtensionFilter(String name, String[] extensions) {
			this.name = name;
			this.extensions = new LinkedHashSet<String>(Arrays.asList(extensions));
		}

		public String toString() {
			StringBuffer s = new StringBuffer();
			s.append(name);
			s.append(":");
			s.append(extensions.toString());
			return s.toString();
		}
		public boolean accept(File f) {
			return f.isDirectory() || extensions.contains(getExtension(f));
		}

		public String getDescription() {
			return name;
		}

		/*
		 * Get the extension of a file.
		 */
		public static String getExtension(File f) {
			String ext = "";
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}

		/*
		 * Get the extension of a filter.
		 */
		public String getFilterExtension(Integer index) {
			List<String> l = new ArrayList<String>(extensions);
			int i;

			if ( index == null ||
					index.intValue() < 0 ||
					index.intValue() >= l.size() )
				i = 0;
			else
				i = index.intValue();

			if ( l.size() > 0 )
				return l.get(i);
			return null;


		}
	}

	/**
	 * Returns an ImageIcon with a graphic from the JLF Graphics
	 * Repository, or null if the path was invalid.  Copied from the
	 * Swing Tutorial.
	 *
	 * @param name The icon category and name from the JLF graphics
	 * repository, such as "general/Help".  See jlfgr_1.0.jar for details.
	 * @param size Either 16 or 24.
	 */
	public static ImageIcon createJLFIcon(String name,
										  String description,
										  int size) {
		String realPath = "/toolbarButtonGraphics/"+name+size+".gif";
		logger.debug("Loading resource "+realPath);
		java.net.URL imgURL = ASUtils.class.getResource(realPath);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			logger.debug("Couldn't find file: " + realPath);
			return null;
		}
	}

	/**
	 * Returns an ImageIcon with an image from the collection of
	 * icons in the classpath, or null if the path was invalid.  Copied from the Swing
	 * Tutorial.
	 *
	 * @param name The base of the filename from our graphics repository, such as
	 * "NewTable".  See the icons directory.
	 * @param size Either 16 or 24.
	 */
    public static ImageIcon createIcon(String name,
                                       String description,
                                       int size) {
        return createIcon(name+size, description);
    }

    /**
     * Returns an ImageIcon with an image from the collection of
     * icons in the classpath, or null if the path was invalid.  Copied from the Swing
     * Tutorial.
     *  
     * @param name The base of the filename from our graphics repository, such as
     * "NewTable".  See the icons directory.
     * @param description The description of the icon (maybe not used for anything).
     * @return
     */
    public static ImageIcon createIcon(String name,
                                       String description) {
        String realPath = "/icons/"+name+".png";
		logger.debug("Loading resource "+realPath);
		java.net.URL imgURL = ASUtils.class.getResource(realPath);
        if (imgURL == null) {
            realPath = realPath.replace(".png", ".gif");
            imgURL = ASUtils.class.getResource(realPath);
        }
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			logger.debug("Couldn't find file: " + realPath);
			return null;
		}
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
        final SwingUIProject project = session.getProject();
        ArchitectDataSource currentTarget = session.getPlayPen().getDatabase().getDataSource();
        newTargetDB.addItem(currentTarget);
        for (ArchitectDataSource dbcs : session.getUserSettings().getConnections()) {
            if(!dbcs.equals(currentTarget)) {
                newTargetDB.addItem(dbcs);
            }
        }
        newTargetDB.setSelectedIndex(0);
        targetDB.setModel(newTargetDB.getModel());
        targetDB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArchitectDataSource projectDS = session.getPlayPen().getDatabase().getDataSource();
                ArchitectDataSource comboBoxDS = (ArchitectDataSource)((JComboBox)e.getSource()).getSelectedItem();
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
    public static void showTargetDbcsDialog(Window parentWindow, final ArchitectSwingSession session, final JComboBox targetDB) {
        JDialog d = showDbcsDialog(parentWindow, session, session.getPlayPen().db.getDataSource());
        
        d.addWindowListener(new WindowAdapter(){
                public void windowClosed(WindowEvent e){
                    session.getPlayPen().getDatabase().getDataSource().setName("(Target Database)");
                    ASUtils.setupTargetDBComboBox(session, targetDB);
                }
            });
    }
    
    /**
     * Pops up a dialog box that lets the user inspect and change the given
     * db's connection spec.
     */
    public static JDialog showDbcsDialog(Window parentWindow, final ArchitectSwingSession session, ArchitectDataSource dataSource) {
        final DBCSPanel dbcsPanel = new DBCSPanel(
                session.getUserSettings().getPlDotIni());
    
        dbcsPanel.setDbcs(dataSource);
        DBCSOkAction okAction = new DBCSOkAction(dbcsPanel, session, false);
    
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dbcsPanel.discardChanges();
            }
        };
    
        JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
                dbcsPanel, parentWindow,
                "Target Database Connection", ArchitectPanelBuilder.OK_BUTTON_LABEL,
                okAction, cancelAction);
    
        okAction.setConnectionDialog(d);
        d.pack();
        d.setLocationRelativeTo(parentWindow);
    
        d.setVisible(true);
        return d;
    }

    /**
     * Arrange for an existing JDialog or JFrame to close nicely when the ESC
     * key is pressed. Called with an Action, which will become the cancelAction
     * of the dialog.
     * <p>
     * Note: we explicitly close the dialog from this code.
     *
     * @param w The Window which you want to make cancelable with the ESC key.  Must
     * be either a JFrame or a JDialog.
     * @param cancelAction The action to invoke on cancelation, or null for nothing
     * @param disposeOnCancel If true, the window will be disposed after invoking the provided
     * action when the ESC key is pressed.  Otherwise, the provided action will be invoked,
     * but the window won't be closed.  If you set this to false, and don't provide an action,
     * nothing interesting will happen when ESC is pressed in your dialog.
     */
    public static void makeJDialogCancellable(
    		final Window w,
    		final Action cancelAction,
            final boolean disposeOnCancel) {

        JComponent c;
        if (w instanceof JFrame) {
            c = (JComponent) ((JFrame) w).getRootPane();
        } else if (w instanceof JDialog) {
            c = (JComponent) ((JDialog) w).getRootPane();
        } else {
            throw new IllegalArgumentException(
                    "The window argument has to be either a JFrame or JDialog." +
                    "  You provided a " + (w == null ? null : w.getClass().getName()));
        }

    	InputMap inputMap = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    	ActionMap actionMap = c.getActionMap();

    	inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    	actionMap.put("cancel", new AbstractAction() {
    		public void actionPerformed(ActionEvent e) {
                if ( cancelAction != null ) {
                    cancelAction.actionPerformed(e);
                }
                if (disposeOnCancel){
                    w.dispose();
                }
    		}
    	});
    }

    /**
     * Works like {@link #makeJDialogCancellable(Window, Action, boolean)}
     * with disposeOnCancel set to true.
     *
     * @param w The Window to attach the ESC event handler to
     * @param cancelAction The action to perform.  null is allowed: no custom
     * action will be performed, but the dialog will still be disposed on ESC.
     */
    public static void makeJDialogCancellable(
            final Window w,
            final Action cancelAction){
        makeJDialogCancellable(w, cancelAction, true);
    }

    /**
	 * Displays a dialog box with the given message and exception,
	 * allowing the user to examine the stack trace.  The dialog will
     * not have a parent component.
     * 
     * @deprecated This method does not properly parent the dialog it
     * shows, and also cannot report statistics about the current
     * session to the Quality Feedback Agent.  Use the
     * {@link #showExceptionDialog(Component, String, Throwable)}
     * or {@link #showExceptionDialog(ArchitectSwingSession, String, Throwable)}.
	 */
	public static void showExceptionDialog(String message, Throwable throwable) {
		showExceptionDialog(null, message, throwable, new ArchitectExceptionReportFactory());
	}

    /**
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  Also attempts
     * to post an anonymous description of the error to a central reporting
     * server.
     * 
     * @param parent Frame or window to own the display
     * @param message Message text
     * @param throwable The cause of the problem
     */
	public static void showExceptionDialog(Component parent, String message, Throwable throwable) {
        showExceptionDialog(parent, message, throwable, new ArchitectExceptionReportFactory());
    }
    
    /**
     * Displays a dialog box with the given message and exception, allowing the
     * user to examine the stack trace. Also attempts to post an anonymous
     * description of the error to a central reporting server.
     * <p>
     * The given session's ArchitectFrame will be the error dialog's parent,
     * <b>which means when the user dismisses the dialog, the ArchitectFrame
     * will be given focus</b>. If you are showing an exception in the context
     * of another frame or dialog, use the
     * {@link #showExceptionDialog(ArchitectSwingSession, Component, String, Throwable)}
     * method instead.
     * 
     * @param session
     *            The session in which the error occurred.
     * @param message
     *            Message text
     * @param throwable
     *            The cause of the problem
     */
    public static void showExceptionDialog(ArchitectSwingSession session, String message, Throwable throwable) {
        showExceptionDialog(session, session.getArchitectFrame(), message, throwable);
    }
    
    /**
     * Displays a dialog box with the given message and exception, allowing the
     * user to examine the stack trace. Also attempts to post an anonymous
     * description of the error to a central reporting server.
     * 
     * @param session
     *            The session in which the error occurred.
     * @param parent
     *            Frame or window to own the display
     * @param message
     *            Message text
     * @param throwable
     *            The cause of the problem
     */
    public static void showExceptionDialog(ArchitectSwingSession session, Component parent, String message, Throwable throwable) {
        showExceptionDialog(session, parent, message, null, throwable, new ArchitectExceptionReportFactory());
    }
    
    /** Displays a dialog box with the given message and exception,
     * returning focus to the given component. Intended for use
     * on panels like the CompareDMPanel, so focus works better.
     * @param parent Frame or window to own the display
     * @param message Message text
     * @param throwable The cause of the problem
     * @param qfaFactory Exception Reporter
     */
    public static void showExceptionDialog(Component parent, String message, Throwable throwable, QFAFactory qfaFactory) {
        showExceptionDialog(parent, message, null, throwable, qfaFactory);
    }

    /**
     * Displays a modal dialog box with the given messages and exception stack trace.
     *
     * @param parent The component that should own the dialog.  Used for positioning
     * and proper iconification behaviour.
     * @param message Primary error message, displayed in the dialog in large red type.
     * If you provide null, a generic "Unexpected error" message will be used.
     * @param subMessage Secondary message, displayed in the default colour and normal
     * size type under the primary message.  If you make this null, the sub-message
     * will not be rendered.
     * @param throwable The cause of it all
     * @param qfaFactory The error report generator; may not be null.
     */
    public static void showExceptionDialog(Component parent, String message, String subMessage, Throwable throwable, QFAFactory qfaFactory) {
        showExceptionDialog(null, parent, message, subMessage, throwable, qfaFactory);
	}
    
    /**
     * This is the version of showExceptionDialog that all of the overloaded methods
     * delegate to.
     * <p>
     * Displays a modal dialog box with the given messages and exception stack trace.  This method won't
     * try to look for any singletons in the application.  It is the only safe version of show exception dialog 
     * during early startup.
     *
     * @param session The session in which the error occurred.  Used to gather statistics.
     *                  It is safe to pass in null, but that will disable statistic gathering.
     * @param parent The component that should own the dialog.  Used for positioning
     * and proper iconification behaviour.
     * @param message Primary error message, displayed in the dialog in large red type.
     * If you provide null, a generic "Unexpected error" message will be used.
     * @param subMessage Secondary message, displayed in the default colour and normal
     * size type under the primary message.  If you make this null, the sub-message
     * will not be rendered.
     * @param throwable The cause of it all
     * @param qfaFactory The error report generator; may not be null.
     */
    public static void showExceptionDialog(ArchitectSwingSession session, Component parent, String message, String subMessage, Throwable throwable, QFAFactory qfaFactory) {
        try {
            ExceptionReport er = qfaFactory.createExceptionReport(throwable);
            
            if (session != null &&
                    session.getProject() != null &&
                    session.getPlayPen() != null &&
                    session.getSourceDatabases() != null) {
                PlayPen pp = session.getPlayPen();
                er.setNumObjectsInPlayPen(pp.getTablePanes().size() + pp.getRelationships().size());
                er.setNumSourceConnections(session.getSourceDatabases().getDatabaseList().size());
                er.setUserActivityDescription("");
                logger.debug(er.toString());
                er.postReport(session.getContext());
            }
        } catch (Throwable seriousProblem) {
            logger.error("Couldn't generate and send exception report!  Note that this is not the primary problem; it's a side effect of trying to report the real problem.", seriousProblem);
            JOptionPane.showMessageDialog(null, "Error reporting failed: "+seriousProblem.getMessage()+"\nAdditional information is available in the application log.");
        } finally {
            displayExceptionDialog(parent,message,subMessage,throwable);
        }
    }
    

    /**
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace, but do NOT generate
     * a report back to SQLPower web site.  The dialog's
     * parent component will be the ArchitectFrame's main instance.
     * 
     * @deprecated This method will display a dialog box that is not properly
     * parented. Use {@link #showExceptionDialogNoReport(Component, String, Throwable)} instead.
     */
	public static void showExceptionDialogNoReport(String string, Throwable ex) {
        displayExceptionDialog(null, string, null, ex);
	}

    /**
     * Displays a dialog box with the given message and submessage and exception,
     * allowing the user to examine the stack trace, but do NOT generate
     * a report back to SQLPower web site.
     * @param dialog
     * @param string
     * @param string2
     * @param e1
     */
    public static void showExceptionDialogNoReport(Component parent, String message, String subMessage, Throwable throwable) {
        displayExceptionDialog(parent, message, subMessage, throwable);
    }

    /** Displays a dialog box with the given message and exception,
     * returning focus to the given component. Intended for use
     * on panels like the CompareDMPanel, so focus works better.
     * @param parent
     * @param message
     * @param throwable
     */
    public static void showExceptionDialogNoReport(Component parent,String string, Throwable ex) {
       displayExceptionDialog(parent, string, null, ex);
    }

    /**
     * XXX To get rid of this ugly static variable,
     * the Session should handle all errors, and have
     * all these methods require an Icon as an argument.
     */
    static ImageIcon masterIcon;

    private static void displayExceptionDialog(
            final Component parent,
            final String message,
            final String subMessage,
            final Throwable throwable) {
        JDialog dialog;
        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            dialog = new JDialog(frame, "Error Report");
            if (masterIcon != null) {
                // Ugly temporary workaround for the fact that MM uses
                // some Architect code, which we think is creating a
                // JFrame with the Architect icon on it...
                frame.setIconImage(masterIcon.getImage());
            }
        } else if (parent instanceof Dialog) {
            dialog = new JDialog((Dialog)parent, "Error Report");
        } else {
            logger.error(
                    String.format("dialog parent component %s is neither JFrame nor JDialog", parent));
            dialog = new JDialog((Frame)null, "Error report");
        }
        logger.debug("displayExceptionDialog: showing exception dialog for:", throwable);

        ((JComponent)dialog.getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 5));

        // Details information
        Throwable t = throwable;
        StringWriter stringWriter = new StringWriter();
        final PrintWriter traceWriter = new PrintWriter(stringWriter);
        do {
             printStackTrace(t, traceWriter);
            t = t.getCause();
            if (t != null) {
                traceWriter.println("Caused by:");
            }
        } while (t != null);
        traceWriter.close();

        JPanel top = new JPanel(new GridLayout(0, 1, 5, 5));

        StringBuilder labelText = new StringBuilder();
        labelText.append("<html><font color='red' size='+1'>");
        labelText.append(message == null ?
                "Unexpected error" :
                nlToBR(message));
        labelText.append("</font>");
        if (subMessage != null) {
            labelText.append("<p>");
            labelText.append(subMessage);
        }
        JLabel messageLabel = new JLabel(labelText.toString());
        messageLabel.setIcon(StatusIcon.getFailIcon());
        top.add(messageLabel);

        JLabel errClassLabel =
            new JLabel("<html><b>Exception type</b>: " + nlToBR(throwable.getClass().getName()));
        top.add(errClassLabel);
        String excDetailMessage = throwable.getMessage();
        if (excDetailMessage != null) {
            top.add(new JLabel("<html><b>Detail string</b>: " + nlToBR(excDetailMessage)));
        }

        final JButton detailsButton = new JButton("Show Details");
        final JPanel detailsButtonPanel = new JPanel();
        detailsButtonPanel.add(detailsButton);

        final JButton forumButton = new JButton(ArchitectFrame.forumAction);
        detailsButtonPanel.add(forumButton);
        top.add(detailsButtonPanel);

        dialog.add(top, BorderLayout.NORTH);
        final JScrollPane detailScroller =
            new JScrollPane(new JTextArea(stringWriter.toString()));

        final JPanel messageComponent = new JPanel(new BorderLayout());
        messageComponent.add(detailScroller, BorderLayout.CENTER);
        messageComponent.setPreferredSize(new Dimension(700, 400));

        final JComponent fakeMessageComponent = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(700, 0);
            }
        };

        final JDialog finalDialogReference = dialog;
        finalDialogReference.add(fakeMessageComponent, BorderLayout.CENTER);
        ActionListener detailsAction = new ActionListener() {
            boolean showDetails = true;
            public void actionPerformed(ActionEvent e) {
                if (showDetails) {
                    finalDialogReference.remove(fakeMessageComponent);
                    finalDialogReference.add(messageComponent, BorderLayout.CENTER);
                    detailsButton.setText("Hide Details");
                } else /* hide details */ {
                    finalDialogReference.remove(messageComponent);
                    finalDialogReference.add(fakeMessageComponent, BorderLayout.CENTER);
                    detailsButton.setText("Show Details");
                }
                finalDialogReference.pack();

                Rectangle dialogBounds = finalDialogReference.getBounds();
                Rectangle screenBounds = finalDialogReference.getGraphicsConfiguration().getBounds();
                if ( !screenBounds.contains(dialogBounds) ) {
                    int x = dialogBounds.x;
                    int y = dialogBounds.y;
                    if (screenBounds.x+screenBounds.width < dialogBounds.x + dialogBounds.width){
                        x = dialogBounds.x - (dialogBounds.x + dialogBounds.width - screenBounds.x - screenBounds.width);
                    }
                    if (screenBounds.y+screenBounds.height < dialogBounds.y + dialogBounds.height){
                        y = dialogBounds.y - (dialogBounds.y + dialogBounds.height - screenBounds.y - screenBounds.height);
                    }
                    if (screenBounds.x > x){
                        x = screenBounds.x;
                    }
                    if (screenBounds.y > y){
                        y = screenBounds.y;
                    }
                    finalDialogReference.setLocation(x,y);
                }
                showDetails = ! showDetails;
            }
        };
        detailsButton.addActionListener(detailsAction);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                finalDialogReference.dispose();
                finalDialogReference.setVisible(false);
            }
        });
        JPanel bottom = new JPanel();
        bottom.add(okButton);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        dialog.setVisible(true);
    }

    /**
     * Simple convenience routine to replace all \n's with <br>
     * @param s
     * @return
     */
    static String nlToBR(String s) {
        // Do NOT xml-ify the BR tag until Swing's HTML supports this.
        return s.replaceAll("\n", "<br>");
    }

    static final int MAX_JRE_ELEMENTS = 10;
    static final int THRESHOLD = 5;
	static void printStackTrace(Throwable throwable, PrintWriter traceWriter) {
        traceWriter.println(throwable);
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0, jreElements = 0; i < stackTrace.length; i++) {
            StackTraceElement e = stackTrace[i];
            traceWriter.print("\t");
            traceWriter.println(e);
            String clazzName = e.getClassName();
            if (clazzName.startsWith("java.") ||
                clazzName.startsWith("javax.") ||
                clazzName.startsWith("sun.") ||
                clazzName.startsWith("org.")) {
                final int remainingLength = stackTrace.length - i;
                if (++jreElements >= MAX_JRE_ELEMENTS &&
                        remainingLength > THRESHOLD) {
                    traceWriter.printf("\t... %d more...%n", remainingLength);
                    break;
                }
            }
        }
        traceWriter.flush();
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

    public static String niceClassName(Object o) {
        Class c = o.getClass();
        String name = c.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1)
            return name;
        return name.substring(lastDot + 1);
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
        return createIcon("Architect", "Architect Logo", ArchitectSwingSessionContext.ICON_SIZE).getImage();
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
     * Tries very hard to create a JDialog which is owned by the parent
     * Window of the given component.  However, if the component does not
     * have a Window ancestor, or the component has a Window ancestor that
     * is not a Frame or Dialog, this method instead creates an unparented
     * JDialog which is always-on-top.
     * <P>
     * This method was shamelessly stolen from the grodbots project,
     * http://grodbots.googlecode.com/svn/trunk/src/net/bluecow/robot/RobotUtils.java
     * 
     * @param owningComponent The component that should own this dialog.
     * @param title The title for the dialog.
     * @return A JDialog that is either owned by the Frame or Dialog ancestor of
     * owningComponent, or not owned but set to be alwaysOnTop.
     */
    public static JDialog makeOwnedDialog(Component owningComponent, String title) {
        Window owner = SwingUtilities.getWindowAncestor(owningComponent);
        if (owner instanceof Frame) {
            return new JDialog((Frame) owner, title);
        } else if (owner instanceof Dialog) {
            return new JDialog((Dialog) owner, title);
        } else {
            JDialog d = new JDialog();
            d.setTitle(title);
            d.setAlwaysOnTop(true);
            return d;
        }
    }
}