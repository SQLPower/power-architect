package ca.sqlpower.architect.swingui;

import java.util.*;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

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

	public static final FileFilter SQL_FILE_FILTER =
		new FileExtensionFilter("SQL Script Files", new String[] {"ddl", "sql"});

	public static final FileFilter INI_FILE_FILTER =
		new FileExtensionFilter(".INI Files", new String[] {"ini"});

	public static final FileFilter EXE_FILE_FILTER =
		new FileExtensionFilter(".EXE Files", new String[] {"exe"});

	public static final FileFilter JAR_ZIP_FILE_FILTER =
		new FileExtensionFilter("JAR Files", new String[] {"jar", "zip"});

	public static final FileFilter LOG_FILE_FILTER =
		new FileExtensionFilter("Log Files", new String[] {"log"});

	public static class FileExtensionFilter extends FileFilter {

		protected HashSet extensions;
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
			this.extensions = new HashSet(Arrays.asList(extensions));
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
		System.out.println("Loading resource "+realPath);
		java.net.URL imgURL = ASUtils.class.getResource(realPath);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.out.println("Couldn't find file: " + realPath);
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
		System.out.println("Loading resource "+realPath);
		java.net.URL imgURL = ASUtils.class.getResource(realPath);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.out.println("Couldn't find file: " + realPath);
			return null;
		}
	}
	
	private Thread focusDebuggerThread = null;
	private boolean focusDebuggerStopping = true;
	private Runnable showFocusOwnerTask = new Runnable() {
			public void run() {
				for (;;) {
					if (focusDebuggerStopping) break;
					System.out.println(java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
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

	/**
	 * Creates and packs (but does not show) a dialog window with the given main content pane, title,
	 * and ok and cancel buttons.
	 * 
	 * @param contentPanel The panel which forms the main body of the new dialog.
	 * @param dialogParent The parent of the new dialog (must be either Frame or Dialog; plain Window
	 * is not allowed).
	 * @param dialogTitle The title of the new dialog.
	 * @param okButton The button to use for the dialog's OK button.
	 * @param cancelButton The button to use for the dialog's Cancel button.
	 * @throws IllegalArgumentException if the dialogParent is not a Frame or Dialog (or subclass thereof).
	 */
	public static JDialog createOkCancelDialog(
			JComponent contentPanel,
			Window dialogParent,
			String dialogTitle,
			JButton okButton,
			JButton cancelButton) {
		JDialog d;
		if (dialogParent instanceof Frame) {
			d = new JDialog((Frame) dialogParent, dialogTitle);
		} else if (dialogParent instanceof Dialog) {
			d = new JDialog((Dialog) dialogParent, dialogTitle);
		} else {
			throw new IllegalArgumentException("The dialogParent you gave me is not a " +
					"Frame or Dialog (it is a "+dialogParent.getClass().getName()+")");
		}
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(contentPanel, BorderLayout.CENTER);
		cp.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), BorderLayout.SOUTH);
		cp.setBorder(Borders.DIALOG_BORDER);
		d.setContentPane(cp);
		d.pack();
		return d;
	}
}
