package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.qfa.ExceptionReport;

/**
 * ASUtils is a container class for static utility methods used
 * throughout the Swing user interface.  "ASUtils" is short for
 * "ArchitectSwingUtils" which is too long to use frequently.
 */
public class ASUtils {
	private static final Logger logger = Logger.getLogger(ASUtils.class);

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
	 * but the code needs the value.
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
		new FileExtensionFilter("JAR Files", new String[] {"jar", "zip"});

	public static final FileFilter LOG_FILE_FILTER =
		new FileExtensionFilter("Log Files", new String[] {"log"});

    public static final FileFilter XML_FILE_FILTER =
        new FileExtensionFilter("XML Files", new String[] {"xml"});

    public static final FileFilter PDF_FILE_FILTER =
        new FileExtensionFilter("PDF Files", new String[] {"pdf"});
    
    public static final FileFilter HTML_FILE_FILTER =
        new FileExtensionFilter("HTML Files", new String[] {"html"});
    
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
	 * Returns an ImageIcon with an image from our own collection of
	 * icons, or null if the path was invalid.  Copied from the Swing
	 * Tutorial.
	 *
	 * @param name The name from our graphics repository, such as
	 * "NewTable".  See the icons directory.
	 * @param size Either 16 or 24.
	 */
	public static ImageIcon createIcon(String name,
									   String description,
									   int size) {
		String realPath = "/icons/"+name+size+".gif";
		logger.debug("Loading resource "+realPath);
		java.net.URL imgURL = ASUtils.class.getResource(realPath);
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

    /** Centre a Window, Frame, JFrame, Dialog, etc. */
    public static void centre(final Window w) {
        // After packing a Frame or Dialog, centre it on the screen.
        Dimension us = w.getSize(), 
            them = Toolkit.getDefaultToolkit().getScreenSize();
        int newX = (them.width - us.width) / 2;
        int newY = (them.height- us.height)/ 2;
        w.setLocation(newX, newY);
    }
    
	/**
	 * Displays a dialog box with the given message and exception,
	 * allowing the user to examine the stack trace.  The dialog's
	 * parent component will be the ArchitectFrame's main instance.
	 */
	public static void showExceptionDialog(String message, Throwable throwable) {
		showExceptionDialog(ArchitectFrame.getMainInstance(), message, throwable);
	}
	
	/** Displays a dialog box with the given message and exception,
	 * returning focus to the given component. Intended for use
	 * on panels like the CompareDMPanel, so focus works better.
	 * @param parent
	 * @param message
	 * @param throwable
	 */
	public static void showExceptionDialog(Component parent, String message, Throwable throwable) {
        ExceptionReport er = new ExceptionReport(throwable);
        er.setNumObjectsInPlayPen(ArchitectFrame.getMainInstance().playpen.getTablePanes().size()
                                  + ArchitectFrame.getMainInstance().playpen.getRelationships().size());
        er.setNumSourceConnections(ArchitectFrame.getMainInstance().dbTree.getDatabaseList().size());
        er.setUserActivityDescription("");
        logger.debug(er.toXML());
        er.postReportToSQLPower();
        
        displayExceptionDialog(parent,message,throwable);
	}

    /**
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog's
     * parent component will be the ArchitectFrame's main instance.
     */
	public static void showExceptionDialogNoReport(String string, Throwable ex) {
        displayExceptionDialog(ArchitectFrame.getMainInstance(), string, ex);  
	}
    /** Displays a dialog box with the given message and exception,
     * returning focus to the given component. Intended for use
     * on panels like the CompareDMPanel, so focus works better.
     * @param parent
     * @param message
     * @param throwable
     */
    public static void showExceptionDialogNoReport(Component parent,String string, Throwable ex) {
       displayExceptionDialog(parent, string, ex);  
    }
    
    private static void displayExceptionDialog(Component parent, String message, Throwable throwable) {
        StringWriter traceWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(traceWriter));
        JPanel messageComponent = new JPanel(new BorderLayout());
        messageComponent.add(new JLabel(message), BorderLayout.NORTH);
        messageComponent.add(new JScrollPane(new JTextArea(traceWriter.toString())), BorderLayout.CENTER);
        messageComponent.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(parent, 
                                      messageComponent,
                                      "Error Report",
                                      JOptionPane.ERROR_MESSAGE);
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

	
}
