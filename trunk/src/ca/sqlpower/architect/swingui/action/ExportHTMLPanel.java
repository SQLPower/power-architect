/*
 * Copyright (c) 2009, SQL Power Group Inc.
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
package ca.sqlpower.architect.swingui.action;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.XsltTransformation;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel to select the XSLT stylesheet and the output file to be generated.
 * <p>
 * The panel has a start and close button so that it is self-contained.
 * showDialog() will display this panel in a non-modal JDialog.
 * <p>
 * The last 15 selected XSLT transformations will be remembered and made
 * available through a dropdown box.
 */
public class ExportHTMLPanel {

	private static final Logger logger = Logger.getLogger(ExportHTMLPanel.class);
	
	private JRadioButton builtin;
	private JRadioButton external;
	private JComboBox xsltFile;
	private JButton selectXslt;
	private JButton selectOutput;
	private JButton startButton;
	private JButton closeButton;
	private JTextField outputFile;

	private JLabel statusBar;
	
	private final ArchitectSwingSession session;

	private JDialog dialog;

	private static final String ENCODING = "UTF-8";
	private PipedOutputStream xmlOutputStream;
	private FileOutputStream result;

    private final JPanel panel;

	private static final String PREF_KEY_BUILTIN = "htmlgen.builtin";
	private static final String PREF_KEY_LAST_XSLT = "htmlgen.lastxslt";
	private static final String PREF_KEY_XSLT_HISTORY = "htmlgen.xslt.recent";
	private static final String PREF_KEY_OUTPUT = "htmlgen.lastoutput";
	private static final int MAX_HISTORY_ENTRIES = 15;
	
	public ExportHTMLPanel(ArchitectSwingSession architect) {

		session = architect;
		FormLayout layout = new FormLayout("10dlu, 3dlu, pref:grow, 3dlu, pref");
		DefaultFormBuilder builder;
		if (logger.isDebugEnabled()) {
		    builder = new DefaultFormBuilder(layout, new FormDebugPanel());
		} else {
		    builder = new DefaultFormBuilder(layout);
		}

		ButtonGroup group = new ButtonGroup();
		builtin = new JRadioButton(Messages.getString("XSLTSelectionPanel.labelBuiltIn"));
		external = new JRadioButton(Messages.getString("XSLTSelectionPanel.labelExternal"));
		group.add(builtin);
		group.add(external);

		// place Radio buttons
		builder.append(builtin, 5);
		
		builder.appendUnrelatedComponentsGapRow();
		builder.nextLine();
        builder.nextLine();
		
		builder.append(external, 5);

		// Selection of XSLT file
		xsltFile = new JComboBox();
		xsltFile.setRenderer(new ComboTooltipRenderer());
		xsltFile.setEditable(true);

		builder.append("");
		builder.append(xsltFile);
		
		selectXslt = new JButton("...");
		builder.append(selectXslt);

		builder.appendUnrelatedComponentsGapRow();
        builder.nextLine();
        builder.nextLine();
		
		// Output selection
		builder.append(new JLabel(Messages.getString("XSLTSelectionPanel.labelOutput")), 5);
		builder.nextLine();
		
		outputFile = new JTextField(30);
		builder.append("", outputFile);

		selectOutput = new JButton("...");
		builder.append(selectOutput);

		builder.appendUnrelatedComponentsGapRow();
        builder.nextLine();
        builder.nextLine();
		
		// "Statusbar"
		statusBar = new JLabel(" ");
		builder.append(statusBar, 5);

		builder.appendUnrelatedComponentsGapRow();
        builder.nextLine();

		selectXslt.addActionListener(componentStateHandler);
		selectOutput.addActionListener(componentStateHandler);
		builtin.addActionListener(componentStateHandler);
		external.addActionListener(componentStateHandler);
		builtin.setSelected(true);

		startButton = new JDefaultButton(Messages.getString("XSLTSelectionPanel.startOption"));
		startButton.addActionListener(componentStateHandler);
		
		closeButton = new JButton(Messages.getString("XSLTSelectionPanel.closeOption"));
		closeButton.addActionListener(componentStateHandler);

		builder.nextLine();
		JPanel bp = ButtonBarFactory.buildRightAlignedBar(startButton, closeButton);
		builder.append(bp, 5);

		builder.setDefaultDialogBorder();
		panel = builder.getPanel();
		
		restoreSettings();
	}

	/**
	 * Displays this selection panel to the user.
	 * The dialog is non-modal
	 *
	 */
	public void showDialog() {

		if (dialog == null) {
			ArchitectFrame frame = session.getArchitectFrame();

			dialog = new JDialog(frame, Messages.getString("XSLTSelectionPanel.dialogTitle"));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			SPSUtils.makeJDialogCancellable(dialog, new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					closeDialog();
				}
			});
			dialog.setContentPane(panel);
			dialog.getRootPane().setDefaultButton(startButton);
			dialog.pack();
			dialog.setLocationRelativeTo(frame);
		}
        dialog.setVisible(true);
	}

	/**
	 * Return the filename the user selected. If the user
	 * chose to use the built-in XSLT, this returns null
	 *
	 * @return the filename selected by the user, or null if the internal
	 *  XSLT should be used.
	 */
	public File getXsltFile() {
		if (builtin.isSelected()) {
			return null;
		}
		Object o = xsltFile.getSelectedItem();
		if (o instanceof File) {
			return (File)o;
		} else if (o instanceof String) {
			// might happen if the user entered the filename manually
			return new File((String)o);
		}
		return null;
	}

	/**
	 * Returns the filename of the (user-selected) output file.
	 *
	 */
	public String getOutputFilename() {
		return outputFile.getText();
	}

	private final ActionListener componentStateHandler = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	        if (e.getSource() == selectXslt) {
	            selectXslt();
	        } else if (e.getSource() == selectOutput) {
	            selectOutput();
	        } else if (e.getSource() == startButton) {
	            transformFile();
	        } else if (e.getSource() == closeButton) {
	            closeDialog();
	        } else if (e.getSource() == xsltFile) {
	            updateDropDownToolTip();
	        } else if (e.getSource() == builtin) {
	            xsltFile.setEnabled(external.isSelected());
	        } else if (e.getSource() == external) {
	            xsltFile.setEnabled(external.isSelected());
	        }
	    }
	};
	
	private void updateDropDownToolTip() {
		File f = this.getXsltFile();
		if (f != null) {
			xsltFile.setToolTipText(getFullName(f));
		}
	}

	private void setXsltFile(File xslt)	{
		ComboBoxFile cf = new ComboBoxFile(xslt);
		external.setSelected(true);
		xsltFile.setEnabled(true);
		xsltFile.addItem(cf);
		xsltFile.setSelectedItem(cf);
		xsltFile.setToolTipText(getFullName(xslt));
	}

	private void syncDropDown() {
		// if the user pasted the filename into the editable part
		// of the dropdown, this will not be part of the actual dropdown items
		// so I'm adding that here "manually"
		File current = getXsltFile();

		if (current == null) return;
		boolean found = false;

		int numEntries = xsltFile.getItemCount() ;
		for (int index = 0; index < numEntries; index++) {
			if (xsltFile.getItemAt(index).equals(current)) {
				found = true;
				break;
			}
		}

		if (!found) {
			xsltFile.addItem(new ComboBoxFile(current));
		}
	}

	private void saveSettings() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.putBoolean(PREF_KEY_BUILTIN, builtin.isSelected());
		prefs.put(PREF_KEY_OUTPUT, outputFile.getText());

		// Add any pasted filename to the dropdown's model, so that it
		// stored correctly in the user preferences
		syncDropDown();
		
		File f = getXsltFile();
		if (f != null) {
			prefs.put(PREF_KEY_LAST_XSLT, getFullName(f));
		} else {
			prefs.remove(PREF_KEY_LAST_XSLT);
		}

		int numEntries = (xsltFile.getItemCount() > MAX_HISTORY_ENTRIES ? MAX_HISTORY_ENTRIES : xsltFile.getItemCount());
		
		for (int i=0; i < numEntries; i++) {
			Object o = xsltFile.getItemAt(i);
			String key = PREF_KEY_XSLT_HISTORY + "." + i;
			if (o instanceof File) {
				prefs.put(key, getFullName((File)o));
			} else if (o instanceof String) {
				prefs.put(key, (String)o);
			} else {
				prefs.remove(key);
			}
		}
	}

	private void restoreSettings() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		final boolean useBuiltin = prefs.getBoolean(PREF_KEY_BUILTIN, true);
		builtin.setSelected(useBuiltin);
		external.setSelected(!useBuiltin);
		xsltFile.setEnabled(!useBuiltin);

		// I'm actively setting the focus, because by default the focus is
		// set to the "Internal" radio button. I think that initial focus is
		// a bit confusing when the "external" one is selected.

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (useBuiltin) {
					builtin.requestFocusInWindow();
				} else {
					external.requestFocusInWindow();
				}
			}
		});

		// Restore the history
		for (int i=0; i < 15; i++) {
			String fname = prefs.get(PREF_KEY_XSLT_HISTORY + "." + i, null);
			if (fname == null) break;
			ComboBoxFile f = new ComboBoxFile(fname);
			xsltFile.addItem(f);
		}

		// The last used XSLT
		String file = prefs.get(PREF_KEY_LAST_XSLT, null);
		if (file != null) {
			ComboBoxFile f = new ComboBoxFile(file);
			xsltFile.setSelectedItem(f);
		}
		outputFile.setText(prefs.get(PREF_KEY_OUTPUT, ""));
	}

	public static String getFullName(File fo) {
		if (fo == null) return null;
		try {
			// The canonical path is a bit more "user-friendly" especially
			// on Windows. But as it can throw an IOException (why?) I need
			// to wrap it here
			return fo.getCanonicalPath();
		} catch (IOException io) {
			return fo.getAbsolutePath();
		}
	}


	private void selectXslt() {
		JFileChooser chooser = new JFileChooser(session.getProject().getFile());
		chooser.addChoosableFileFilter(SPSUtils.XSLT_FILE_FILTER);
		chooser.setDialogTitle(Messages.getString("XSLTSelectionPanel.selectXsltTitle"));
		int response = chooser.showOpenDialog(dialog);
		if (response != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		dialog.requestFocus();
		setXsltFile(file);
	}

	private void closeDialog() {
		if (dialog != null) {
			saveSettings();
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	private void selectOutput() {
		JFileChooser chooser = new JFileChooser(session.getProject().getFile());
		chooser.addChoosableFileFilter(SPSUtils.HTML_FILE_FILTER);
		chooser.setDialogTitle(Messages.getString("XSLTSelectionPanel.saveAsTitle"));
		
		int response = chooser.showSaveDialog(session.getArchitectFrame());

		if (response != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		if (!file.getPath().endsWith(".html")) { //$NON-NLS-1$
			file = new File(file.getPath() + ".html"); //$NON-NLS-1$
		}

		try {
			outputFile.setText(file.getCanonicalPath());
		} catch (IOException io) {
			outputFile.setText(file.getAbsolutePath());
		}
		dialog.requestFocus();
	}

	/**
	 * Transforms the current playpen according to the selection that the user made.
	 * <br/>
	 * An xml OutputStream(using a {@link PipedOutputStream}) is generated, based on the
	 * current playPen content and is read by a {@link PipedInputStream} which is used as the xml source. 
	 * <br/>
	 * The stylesheet and the xml source are passed as parameters to the
	 * {@link XsltTransformation} methods to generate an HTML report off the content
	 * to a location specified by the user.
	*/
	protected void transformFile() {

		File file = new File(outputFile.getText());
		
		if (file.exists()) {
			int response = JOptionPane.showConfirmDialog(session.getArchitectFrame(),
			  Messages.getString("XSLTSelectionPanel.fileAlreadyExists", file.getPath()), //$NON-NLS-1$
					Messages.getString("XSLTSelectionPanel.fileAlreadyExistsDialogTitle"),
					JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
			if (response == JOptionPane.NO_OPTION) {
				return;
			}
		}

		statusBar.setText(Messages.getString("XSLTSelectionPanel.msgGenerating"));
		Thread t = new Thread(new Runnable() {
			public void run() {
				_transformFile();
			}
		});
		t.setName("HTML Generation Thread");
		t.setDaemon(true);
		t.start();
	}
	
	protected void _transformFile() {
		PipedInputStream xmlInputStream = new PipedInputStream();
		try {
			xmlOutputStream = new PipedOutputStream(xmlInputStream);
			new Thread(
					new Runnable() {

						public void run() {
							try {
								session.getProject().save(xmlOutputStream, ENCODING);
							} catch (IOException e2) {
								SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "You got an error", e2);
							} catch (SQLObjectException e2) {
								SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "You got an error", e2);
							}
						}
					}).start();
			xmlOutputStream.flush();

		} catch (IOException e2) {
			SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "You got an error", e2);
		}

		File file = new File(getOutputFilename());
		try {
			result = new FileOutputStream(file);
		} catch (FileNotFoundException e2) {
			SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "You got an error", e2);
		}

		XsltTransformation xsltTransform = new XsltTransformation();

		InputStream xsltInput = null;
		try {
			File xslt = getXsltFile();
			if (xslt == null) {
				xsltTransform.transform("/xsltStylesheets/architect2html.xslt", xmlInputStream, result);
			} else {
				xsltInput = new FileInputStream(xslt);
				xsltTransform.transform(xsltInput, xmlInputStream, result);
			}

			EventQueue.invokeLater(new Runnable() {

				public void run() {
					statusBar.setText(Messages.getString("XSLTSelectionPanel.msgStartingBrowser"));
				}
			});

			//Opens up the html file in the default browser
			BrowserUtil.launch(file.toURI().toString());
		} catch (Exception e1) {
			SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "You got an error", e1);
		} finally {
			closeQuietly(result);
			closeQuietly(xmlInputStream);
			closeQuietly(xmlOutputStream);
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusBar.setText("");
			}
		});

	}

	private void closeQuietly(Closeable stream) {
		try {
			stream.close();
		} catch (IOException io) {
			logger.error("Error closing file", io);
		}

	}

	/**
	 * A class used for the items in the combobox.
	 * The only difference to the File class is, that the toString()
	 * method returns only the file name, not the full path.
	 * Otherwise the dropdown would be too wide.
	 *
	 * A better solution would be a dropdown that displays only the filename
	 * for the selected file, but shows the full names when it is opened.
	 * But unfortunately Swing cannot handle a dropdown where the popup is wider
	 * than the actual component.
	 */
	private class ComboBoxFile
		extends File {

		public ComboBoxFile(File f) {
			super(f.getAbsolutePath());
		}

		public ComboBoxFile(String pathname) {
			super(pathname);
		}

		public String toString() {
			return getName();
		}
	}

	private class ComboTooltipRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof File) {
				comp.setToolTipText(ExportHTMLPanel.getFullName((File)value));
			} else {
				comp.setToolTipText(null);
			}
			return comp;
		}
	}
}


