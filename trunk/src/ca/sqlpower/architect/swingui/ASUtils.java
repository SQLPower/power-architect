package ca.sqlpower.architect.swingui;

import java.util.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * ASUtils is a container class for static utility methods used
 * throughout the Swing user interface.  "ASUtils" is short for
 * "ArchitectSwingUtils" which is too long to use frequently.
 */
public class ASUtils {
	private static final Logger logger = Logger.getLogger(ASUtils.class);

	/**
	 * Short-form convenience method for
	 * <code>new ArchitectSwingUtils.LabelValueBean(label,value)</code>.
	 */
	public static LabelValueBean lvb(String label, Object value) {
		return new LabelValueBean(label, value);
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

	public static FileFilter architectFileFilter =
		new FileExtensionFilter("Architect Project Files", new String[] {"architect"});

	public static FileFilter sqlFileFilter =
		new FileExtensionFilter("SQL Script Files", new String[] {"ddl", "sql"});

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
	 * Returns an ImageIcon, or null if the path was invalid.  Copied
	 * from the Swing Tutorial.
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
	
}
