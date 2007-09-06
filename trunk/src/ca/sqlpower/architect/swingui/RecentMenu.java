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
import javax.swing.JOptionPane;

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
				JOptionPane.showMessageDialog(RecentMenu.this, "Could not open file " + e1);
			}
		}
	};
	
	/**
     * PreferenceChangeListener that is used by all the sessions;
     * updates the recent file lists of each session if one is changed.
     */
    private PreferenceChangeListener recentListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            loadRecentMenu();
        }
    };
	
	/**
	 * Add the given filename to the top of the recent list in Prefs and in menu.
	 * It is generally <b>not</b> necessary for user code to call this method!
	 */
	public void putRecentFileName(String f) {
		// Trim from back end if too long
		while (recentFileNames.size() > maxRecentFiles - 1) {
			recentFileNames.remove(recentFileNames.size()-1);
		}
		// Move filename to front: Remove if present, add at front.
		if (recentFileNames.contains(f)) {
			recentFileNames.remove(f);
		}
		recentFileNames.add(0, f);

		// Now save from List into Prefs
		for (int i = 0; i < recentFileNames.size(); i++) {
			String t = recentFileNames.get(i);
			prefs.put(PREFS_KEY + i, t);
		}

		// Finally, load menu again.
		loadRecentMenu();
	}

	/**
	 * Lodd or re-load the recentFileMenu
	 */
	public void loadRecentMenu() {
		setEnabled(false);
		// Clear out both all menu items and List in memory
		for (int i = getMenuComponentCount() - 1; i >= 0; i--) {
			remove(0);
		}
		recentFileNames.clear();

		// Copy from Prefs into Menu
		JMenuItem mi;
		for (int i = 0; i < maxRecentFiles; i++) {
			String f = prefs.get(PREFS_KEY + i, null);
			if (f == null) {	// Stop on first missing
				break;
			}
			// Drop from list if file has been deleted.
			if (new File(f).exists()) {
				// Add to List in memory
				recentFileNames.add(f);
				// If at least one item, enable menu
				setEnabled(true);
				// And add to Menu
				this.add(mi = new JMenuItem(f));
				mi.addActionListener(recentOpener);
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
