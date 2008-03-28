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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import junit.framework.TestCase;

public class TestASUtilsMenu extends TestCase {
	
	protected JMenu fileMenu;

	public static void main(String[] args) {
		for (int i = 48; i <= 51; i++) 
			new TestASUtilsMenu().createMenuTest(i);
	}
	
	/**
	 * @throws HeadlessException
	 */
	private JFrame createMenuTest(int max) throws HeadlessException {
		final JFrame jf = new JFrame();
		jf.setSize(400, 600);
		final JMenuBar jb = new JMenuBar();
		jf.setJMenuBar(jb);
		fileMenu = new JMenu("File");
		for (int i = 0; i <= max; i++) {
			if ( i==10 || i == 9 ) {
				final JMenuItem jmi = new JMenuItem(Integer.toString(i));
				fileMenu.add(jmi);
				jmi.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						System.out.println("jf.bounds="+jf.getBounds());
						System.out.println("jmi:"+jmi.getBounds());
					}
					
				});
			}
			else {
				fileMenu.add(new JMenuItem(Integer.toString(i)));
			}
		}

		jb.add(fileMenu);
		ASUtils.breakLongMenu(jf, fileMenu);
		jf.setVisible(true);
		return jf;
	}
}
