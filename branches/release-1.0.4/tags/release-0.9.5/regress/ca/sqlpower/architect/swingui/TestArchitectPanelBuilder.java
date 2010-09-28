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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * Test the ASUtils.createArchitectPanelDialog() method.
 */
public class TestArchitectPanelBuilder {
	
	/** For testing the ArchitectPanelBuilder with the default Actions
	 */
	static class TestPanel extends JPanel implements ArchitectPanel {
		TestPanel() {
			setLayout(new BorderLayout());
			add(new JLabel("This is just a test"), BorderLayout.CENTER);
		}

		public boolean applyChanges() {
			System.out.println("You applied your changes");
			return false;
		}

		public void discardChanges() {
			System.out.println("You cancelled your changes");
		}

		public JComponent getPanel() {
			return this;
		}
	}

	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Test Main Program");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JDialog dlgWithDefaultActions = 
			ArchitectPanelBuilder.createArchitectPanelDialog(
					new TestPanel(), frame, "Test", "OK Dudes");
		
		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("OK action actionPerformed called");
			}
		};
		
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("cancel action actionPerformed called");
			}
		};
		
		final JDialog dlg2 = 
			ArchitectPanelBuilder.createArchitectPanelDialog(
					new TestPanel(), frame,
					"Test with actions passed in",
					"OK Dudes",
					okAction,
					cancelAction);
		
		frame.add(
			new JLabel("This is the test program's main window",
			JLabel.CENTER),
			BorderLayout.NORTH);	
		
		JButton test1Button = new JButton();
		test1Button.setText("Test Default Actions");
		
		JButton test2Button = new JButton();
		test2Button.setText("Test Caller-Provided Actions");
		
		test1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlgWithDefaultActions.setVisible(true);
			}
		});
		
		test2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg2.setVisible(true);
			}
		});
		
		
		JPanel cp = new JPanel(new BorderLayout());
		
		cp.add(ButtonBarFactory.buildOKCancelBar(
				test1Button,
				test2Button),
				BorderLayout.SOUTH);
		cp.setBorder(Borders.DIALOG_BORDER);
		
		frame.add(cp, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}
}
