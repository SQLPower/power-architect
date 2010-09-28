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
import java.io.File;
import java.io.IOException;
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
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.transformation.ReportTransformer;
import ca.sqlpower.architect.transformation.TransformerFactory;
import ca.sqlpower.architect.transformation.UnknowTemplateTypeException;
import ca.sqlpower.architect.transformation.XsltTransformation;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel to select a transformation template (XSLT or Velocity) and the output file to be generated.
 * <p>
 * The panel has a start and close button so that it is self-contained.
 * showDialog() will display this panel in a non-modal JDialog.
 * <p>
 * The last 15 selected XSLT transformations will be remembered and made
 * available through a dropdown box.
 */
public class ExportHTMLPanel {

	private static final Logger logger = Logger.getLogger(ExportHTMLPanel.class);
	
	private static String builtinTransform = "/xsltStylesheets/architect2html.xslt";
	private static BuiltinOptionPanelFactory builtinOptions;

	private JRadioButton builtin;
	private JRadioButton external;
	private JComboBox templateFile;
	private JButton selectTemplate;
	private JButton selectOutput;
	private JButton startButton;
	private JButton closeButton;
	private JTextField outputFile;

	private JLabel statusBar;

	private final ArchitectSwingSession session;

	private JDialog dialog;

    private final JPanel panel;
    
    private final BuiltinOptionPanel builtinOptionPanel;

	private static final String PREF_KEY_BUILTIN = "htmlgen.builtin";
	private static final String PREF_KEY_LAST_XSLT = "htmlgen.lastxslt";
	private static final String PREF_KEY_XSLT_HISTORY = "htmlgen.xslt.recent";
	private static final String PREF_KEY_OUTPUT = "htmlgen.lastoutput";
	private static final int MAX_HISTORY_ENTRIES = 15;


	public static void setBuiltinTransform(String builtinTransform) {
	    ExportHTMLPanel.builtinTransform = builtinTransform;
	}
	
	public static void setBuiltinOptionPanelFactory(BuiltinOptionPanelFactory builtinOptions) {
        ExportHTMLPanel.builtinOptions = builtinOptions;
	}
	
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
		builder.appendRelatedComponentsGapColumn();
		if (builtinOptions != null) {
		    builtinOptionPanel = builtinOptions.createPanel(session);
		    builder.append("");
            builder.append(builtinOptionPanel, 4);
		} else {
		    builtinOptionPanel = null;
		}

		builder.appendUnrelatedComponentsGapRow();
		builder.nextLine();
        builder.nextLine();

		builder.append(external, 5);

		// Selection of XSLT file
		templateFile = new JComboBox();
		templateFile.setRenderer(new ComboTooltipRenderer());
		templateFile.setEditable(true);

		builder.append("");
		builder.append(templateFile);

		selectTemplate = new JButton("...");
		builder.append(selectTemplate);

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

		selectTemplate.addActionListener(componentStateHandler);
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
	public File getTemplateFile() {
		if (builtin.isSelected()) {
			return null;
		}
		Object o = templateFile.getSelectedItem();
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

	public File getOutputFile() {
		return new File(outputFile.getText());
	}

	private final ActionListener componentStateHandler = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	        if (e.getSource() == selectTemplate) {
	            selectTemplate();
	        } else if (e.getSource() == selectOutput) {
	            selectOutput();
	        } else if (e.getSource() == startButton) {
	            transformFile();
	        } else if (e.getSource() == closeButton) {
	            closeDialog();
	        } else if (e.getSource() == templateFile) {
	            updateDropDownToolTip();
	        } else if (e.getSource() == builtin) {
	            templateFile.setEnabled(external.isSelected());
	        } else if (e.getSource() == external) {
	            templateFile.setEnabled(external.isSelected());
	        }
	    }
	};

	private void updateDropDownToolTip() {
		File f = this.getTemplateFile();
		if (f != null) {
			templateFile.setToolTipText(getFullName(f));
		}
	}

	private void setTemplateFile(File template)	{
		ComboBoxFile cf = new ComboBoxFile(template);
		external.setSelected(true);
		templateFile.setEnabled(true);
		templateFile.addItem(cf);
		templateFile.setSelectedItem(cf);
		templateFile.setToolTipText(getFullName(template));
	}

	private void syncDropDown() {
		// if the user pasted the filename into the editable part
		// of the dropdown, this will not be part of the actual dropdown items
		// so I'm adding that here "manually"
		File current = getTemplateFile();

		if (current == null) return;
		boolean found = false;

		int numEntries = templateFile.getItemCount() ;
		for (int index = 0; index < numEntries; index++) {
			if (templateFile.getItemAt(index).equals(current)) {
				found = true;
				break;
			}
		}

		if (!found) {
			templateFile.addItem(new ComboBoxFile(current));
		}
	}

	private void saveSettings() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.putBoolean(PREF_KEY_BUILTIN, builtin.isSelected());
		prefs.put(PREF_KEY_OUTPUT, outputFile.getText());

		// Add any pasted filename to the dropdown's model, so that it
		// stored correctly in the user preferences
		syncDropDown();

		File f = getTemplateFile();
		if (f != null) {
			prefs.put(PREF_KEY_LAST_XSLT, getFullName(f));
		} else {
			prefs.remove(PREF_KEY_LAST_XSLT);
		}

		int numEntries = (templateFile.getItemCount() > MAX_HISTORY_ENTRIES ? MAX_HISTORY_ENTRIES : templateFile.getItemCount());

		for (int i=0; i < numEntries; i++) {
			Object o = templateFile.getItemAt(i);
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
		templateFile.setEnabled(!useBuiltin);

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
			templateFile.addItem(f);
		}

		// The last used XSLT
		String file = prefs.get(PREF_KEY_LAST_XSLT, null);
		if (file != null) {
			ComboBoxFile f = new ComboBoxFile(file);
			templateFile.setSelectedItem(f);
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

	private void selectTemplate() {
		JFileChooser chooser = new JFileChooser(session.getProjectLoader().getFile());
		File tmpl = getTemplateFile();
		boolean xslt = true;
		String fname = "";
		if (tmpl != null) {
			fname = tmpl.getAbsolutePath().toLowerCase();
			xslt = fname.endsWith("xslt") || fname.endsWith("xsl");
		}

		chooser.addChoosableFileFilter(SPSUtils.XSLT_FILE_FILTER);
		chooser.addChoosableFileFilter(SPSUtils.VELOCITY_FILE_FILTER);
		if (xslt) {
			chooser.setFileFilter(SPSUtils.XSLT_FILE_FILTER);
		} else if (fname.endsWith("vm")){
			chooser.setFileFilter(SPSUtils.VELOCITY_FILE_FILTER);
		} else {
			FileFilter[] filters = chooser.getChoosableFileFilters();
			chooser.setFileFilter(filters[0]);
		}
		chooser.setDialogTitle(Messages.getString("XSLTSelectionPanel.selectXsltTitle"));
		int response = chooser.showOpenDialog(dialog);
		if (response != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		dialog.requestFocus();
		setTemplateFile(file);
	}

	private void closeDialog() {
		if (dialog != null) {
			saveSettings();
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	private void selectOutput() {
		JFileChooser chooser = new JFileChooser(session.getProjectLoader().getFile());
		chooser.addChoosableFileFilter(SPSUtils.HTML_FILE_FILTER);
		chooser.setDialogTitle(Messages.getString("XSLTSelectionPanel.saveAsTitle"));

		int response = chooser.showSaveDialog(session.getArchitectFrame());

		if (response != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		// Add the .html extension only if the HTML file filter was selected
		if (chooser.getFileFilter() == SPSUtils.HTML_FILE_FILTER &&
			!file.getPath().endsWith(".html")) { //$NON-NLS-1$

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

		setStatusBarText(Messages.getString("XSLTSelectionPanel.msgGenerating"));

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
		File file = new File(getOutputFilename());

		final ReportTransformer transformer;
		try {
			transformer = TransformerFactory.getTransformer(getTemplateFile());
		} catch (UnknowTemplateTypeException e) {
			SPSUtils.showExceptionDialogNoReport(panel, "Error", e);
			setStatusBarText("");
			return;
		}

		if (builtinOptionPanel != null) {
		    builtinOptionPanel.applyChanges(transformer, getOutputFile(), session);
		}
		
		try {
			File xslt = getTemplateFile();
			if (xslt == null) {
			    transformer.transform(builtinTransform, getOutputFile(), session);
			} else {
				transformer.transform(xslt, getOutputFile(), session);
			}

			//Opens up the html file in the default browser
			String fname = getOutputFilename().toLowerCase();
			if (fname.endsWith("html") || fname.endsWith("htm")) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						statusBar.setText(Messages.getString("XSLTSelectionPanel.msgStartingBrowser"));
					}
				});
				BrowserUtil.launch(file.toURI().toString());
			}
		} catch (Exception e1) {
			SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(), "Transformation error", e1);
		}

		setStatusBarText("");
	}

	protected void setStatusBarText(final String text) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusBar.setText(text);
			}
		});
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
	private static class ComboBoxFile
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

	private static class ComboTooltipRenderer extends DefaultListCellRenderer {

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
	
	public static abstract class BuiltinOptionPanel extends JPanel {
	    public abstract void applyChanges(ReportTransformer transformer, File outputFile, ArchitectSession session);
	}
	
	public static interface BuiltinOptionPanelFactory {
	    public BuiltinOptionPanel createPanel(ArchitectSession session); 
	}
}


