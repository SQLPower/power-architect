package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;

public class SaveDocument extends JFileChooser {


	public SaveDocument ( Component owner, 
				AbstractDocument doc, FileExtensionFilter fef ) {
		super();
		setFileFilter(fef);			
		int returnVal = showSaveDialog(owner);
		
		while (true) {
			if (returnVal == JFileChooser.CANCEL_OPTION)
				break;
			else if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				File file = getSelectedFile();				
				String fileName = file.getPath();
				String fileExt = ASUtils.FileExtensionFilter.getExtension(file);
				if ( fileExt.length() == 0 ) {
					file = new File(fileName + "." +
								fef.getFilterExtension(new Integer(0)));
				}
				if ( file.exists() && 
						( JOptionPane.showOptionDialog(owner, 
							"Are your sure you want to overwrite this file?",
							"Confirm Overwrite", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,null,null,null) == JOptionPane.NO_OPTION ) )
					{
						returnVal = showSaveDialog(owner);
					}
				else {
					writeDocument(doc,file);
					break;
				}
			}
		}
	}
	
	protected void writeDocument (AbstractDocument doc, File file) {
		try {						
			StringReader sr = new StringReader(doc.getText(0,doc.getLength()));
			BufferedReader br = new BufferedReader(sr);
			PrintWriter out = new PrintWriter(file);
			String s;
			while ( (s = br.readLine()) != null ) {
				out.println(s);
			}
			out.close();
		} catch (IOException e1) {
			ASUtils.showExceptionDialog("Save file Error!", e1);
		} catch (BadLocationException e1) {
			ASUtils.showExceptionDialog("Open file Error!", e1);
		} 
	}
}
