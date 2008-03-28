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
