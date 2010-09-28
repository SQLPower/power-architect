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
package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectDataSourceTypeEditor;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.JDefaultButton;
import ca.sqlpower.architect.swingui.PreferencesPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;

public class PreferencesAction extends AbstractArchitectAction {
	private static final Logger logger = Logger.getLogger(EditTableAction.class);

	public PreferencesAction(ArchitectSwingSession session) {
        super(session, "User Preferences...", "User Preferences");
	}

	public void actionPerformed(ActionEvent evt) {
		showPreferencesDialog();
	}

	public void showPreferencesDialog() {
		logger.debug("showPreferencesDialog");
		
		// XXX Can't easily use ArchitectPanelBuilder since this
		// contains a JTabbedPane which is not an ArchitectPanel.
		final JDialog d = new JDialog(frame, "User Preferences");
		
		JPanel cp = new JPanel(new BorderLayout(12,12));
		JTabbedPane tp = new JTabbedPane();
		cp.add(tp, BorderLayout.CENTER);
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

		final PreferencesPanel prefPanel = new PreferencesPanel(session);
		tp.add("General", prefPanel);

        final ArchitectDataSourceTypeEditor dsTypeEditor =
            new ArchitectDataSourceTypeEditor(session.getUserSettings().getPlDotIni());
 		tp.add("JDBC Drivers", dsTypeEditor.getPanel());

	
		JDefaultButton okButton = new JDefaultButton(ArchitectPanelBuilder.OK_BUTTON_LABEL);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.applyChanges();
                    dsTypeEditor.applyChanges();
					d.setVisible(false);
				}
			});
	
		Action cancelAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					prefPanel.discardChanges();
                    dsTypeEditor.discardChanges();
					d.setVisible(false);
				}
		};
		cancelAction.putValue(Action.NAME, ArchitectPanelBuilder.CANCEL_BUTTON_LABEL);
		JButton cancelButton = new JButton(cancelAction);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);

		ASUtils.makeJDialogCancellable(d, cancelAction);
		d.getRootPane().setDefaultButton(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(frame);
		d.setVisible(true);		
	}
}
