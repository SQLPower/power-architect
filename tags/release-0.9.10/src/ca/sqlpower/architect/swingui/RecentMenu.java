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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Maintain a "Recent Items" menu component. The caller must override a "template method"
 * named loadFile, and all open operations must be channeled through the RecentMenu's
 * openFile method, which calls loadFile and then updates the Recent Items.
 * <p>
 * Recent items are persisted across runs of the program, using java.util.prefs.Prefences under a 
 * UserNode with the class passed into the constructor, which may be a Class descriptor (from within a static main
 * method using MyClassName.class) or simply passing main program (its getClass method will be called).
 * <p>
 * Will most commonly be used to maintain a list of recent files.
 * Typical usage might be as follows:
 * <pre>
 * 		// Assume this code is in MyMainClass constructor
 * 		JMenuItem open = new JMenuItem("Open");
 * 		fileMenu.add(open);
 * 		final RecentMenu recent;
 *		recent = new RecentMenu(this.getClass()) {
 *			@Override
 *			public void loadFile(String fileName) throws IOException {
 *				myModel.openFile(fileName);
 *			}
 *			
 *		};
 *		open.addActionListener(new ActionListener() {		
 *			public void actionPerformed(ActionEvent evt) {
 *				try {
 *					// maybe get "someFileName" from a JFileChooser...
 *					recent.openFile(someFileName);
 *				} catch (IOException e) {
 *					myErrorPopup("Could not open file" + fileName, e);
 *				}				
 *			}
 *			
 *		});
 *		fileMenu.add(recent);
 *		JMenuItem clearItem = new JMenuItem("Clear Recent Files");
 *		clearItem.addActionListener(new ActionListener() {
 *			public void actionPerformed(ActionEvent e) {
 *				recentFilesMenu.clear();
 *			}			
 *		});
 * </pre>
 * @author Ian Darwin
 */
public abstract class RecentMenu extends JMenu {
	
    private static final Logger logger = Logger.getLogger(RecentMenu.class);
    
	public final static int DEFAULT_MAX_RECENT_FILES = 5;
	private final int maxRecentFiles;
	private static final String PREFS_KEY = "recentFile";
	
	/** The List of recent files */
	private List<String> recentFileNames = new ArrayList<String>();
	
	final Preferences prefs;
	
	/** Construct a RecentMenu with a given class and the number of items to hold
	 * The class is used to for UserPrefsNode to determine where to save the list.
	 * @param mainClass
	 * @param max
	 */
	public RecentMenu(Class <?> mainClass, int max) {
		super("Recent Items");		

		prefs = getUserPrefsNode(mainClass);
		prefs.addPreferenceChangeListener(recentListener);
		maxRecentFiles = max;
		logger.debug("Called from Recent menu constructor");
		loadRecentMenu();
	}

	/**
	 * @param mainClass
	 */
	public static Preferences getUserPrefsNode(Class <?> mainClass) {
		return Preferences.userNodeForPackage(mainClass);
	}
	
	/** Construct a RecentMenu with a given class.
	 * The class is used to for UserPrefsNode to determine where to save the list.
	 * @param mainClass
	 */
	public RecentMenu(Class <?> mainClass) {
		this(mainClass, DEFAULT_MAX_RECENT_FILES);
	}

	/**
	 * Call back to the main program to load the file, and and update
	 * the recent files list.
	 */
	public void openFile(String fileName) throws IOException {
		loadFile(fileName);
		putRecentFileName(fileName);
	}

	/**
	 * This is a template method that the caller must provide to
	 * actually open a file.
	 * @param fileName
	 */
	public abstract void loadFile(String fileName) throws IOException;

	/**
	 * ActionListener that is used by all the Menuitems in the Recent Menu;
	 * just opens the file named by the MenuItem's text.
	 */
	private ActionListener recentOpener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JMenuItem mi = (JMenuItem) e.getSource();
			try {
				openFile(mi.getText());
			} catch (IOException e1) {
			    ASUtils.showExceptionDialogNoReport("Couldn't open file.", e1);
			}
		}
	};
	
	/**
     * PreferenceChangeListener that is used by all the sessions;
     * updates the recent file lists of each session if one is changed.
     */
    private PreferenceChangeListener recentListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
           logger.debug("Called from pref change");
           
           //Invoke later is used because the preference change events
           //are called on a separate thread (at least in Windows)
           SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   loadRecentMenu();
               }
           });
        }
    };
	
	/**
	 * Add the given filename to the top of the recent list in Prefs and in menu.
	 * It is generally <b>not</b> necessary for user code to call this method!
	 */
	public void putRecentFileName(String f) {
        
	    recentFileNames.clear();
	    for (int i = 0; i < maxRecentFiles; i++) {
            String file = prefs.get(PREFS_KEY + i, null);
            if (file == null) {    // Stop on first missing
                break;
            }
            if (file.equals("Clear Recent Items")) {
                break;
            }
            if (new File(file).exists()) {
                if (recentFileNames.contains(file)) {
                    recentFileNames.remove(file);
                }
                recentFileNames.add(file);
            }
        }
	    
	    // Move filename to front: Remove if present, add at front.
	    if (recentFileNames.contains(f)) {
            recentFileNames.remove(f);
        }
	    
		// Trim from back end if too long
		while (recentFileNames.size() > maxRecentFiles - 1) {
			recentFileNames.remove(recentFileNames.size()-1);
		}
	
		recentFileNames.add(0, f);
				
		// Now save from List into Prefs
		for (int i = 0; i < recentFileNames.size(); i++) {
		    String t = recentFileNames.get(i);
		    logger.debug("put " + t);
			prefs.put(PREFS_KEY + i, t);
		}
		
		logger.debug("Called from putFileName");
		// Finally, load menu again.
		loadRecentMenu();
	}

	/**
	 * Load or re-load the recentFileMenu
	 */
	public void loadRecentMenu() {
	    recentFileNames.clear();
		setEnabled(false);
		removeAll();

		// Copy from Prefs into Menu
		JMenuItem mi;
		for (int i = 0; i < maxRecentFiles; i++) {
			String f = prefs.get(PREFS_KEY + i, null);
			if (f == null) {	// Stop on first missing
				break;
			}
			
			//stops the loop if it passes the end
			if (f.equals("Clear Recent Items")) {
			    break;
			}
			
			// Drop from list if file has been deleted.
			if (new File(f).exists()) {
				// If at least one item, enable menu
				setEnabled(true);
				// And add to Menu
				this.add(mi = new JMenuItem(f));
				mi.addActionListener(recentOpener);
				
				//add to the list for to be used by
				//getMostRecentFile()
				recentFileNames.add(f);
			}
		}
        
        //Add in the clear item to the menu
        this.addSeparator();
        JMenuItem clearItem = new JMenuItem("Clear Recent Files");
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        recentFileNames.add("Clear Recent Items");
        this.add(clearItem);
	}
	
	/**
	 * Clear all saved Recent Items from Preferences, from memory, and from the Menu.
	 * There is NO UNDO for this so call with care.
	 */
	public void clear() {
		for (int i = 0; i < maxRecentFiles; i++) {
			prefs.remove(PREFS_KEY + i);
		}
		loadRecentMenu();
	}

    /**
     * Returns the most recent path in this recent menu as a File object.
     * If there are no recent files in this menu, returns null.
     */
    public File getMostRecentFile() {
        if (recentFileNames.size() > 0) {
            String mostRecentPath = recentFileNames.get(0);
            return new File(mostRecentPath);
        } else {
            return null;
        }
    }
}
