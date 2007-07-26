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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;

/** A JFileChooser that includes code to save the Document.
 */
public class SaveDocument extends JFileChooser {

	/**
	 * This Constructor pops up the chooser and saves the Document.
	 * @param owner
	 * @param doc
	 * @param fef
	 */
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
				String fileExt = SPSUtils.FileExtensionFilter.getExtension(file);
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
			ASUtils.showExceptionDialogNoReport("Save file Error!", e1);
		} catch (BadLocationException e1) {
			ASUtils.showExceptionDialogNoReport("Open file Error!", e1);
		}
	}
}
