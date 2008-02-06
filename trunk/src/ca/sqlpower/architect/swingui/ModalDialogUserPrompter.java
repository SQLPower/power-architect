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
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.UserPrompter;
import ca.sqlpower.architect.UserPrompterFactory;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ModalDialogUserPrompter implements UserPrompter {
    
    private static Logger logger = Logger.getLogger(ModalDialogUserPrompter.class);

    /**
     * The dialog that poses the question to the user.
     */
    private JDialog confirmDialog;
    
    /**
     * The component that owns the modal dialog. Also used as the owner
     * for error dialogs.
     */
    private JFrame owner;
    
    /**
     * The label that contains the question. The text of the label will be
     * replaced every time {@link #promptUser(Object[])} gets called, based
     * on the format arguments provided.
     */
    private final JLabel questionLabel;
    
    /**
     * The formatter responsible for doing formatting and parameter substitution
     * in the question text each time the dialog is displayed.
     */
    private final MessageFormat questionFormat;
    
    /**
     * The user's most recent response.
     */
    private UserPromptResponse response;
    
    /**
     * The check box that decides if the decision should be applied to all
     * future responses.
     */
    private final JCheckBox applyToAll;
   
    /**
     * Keeps track of whether or not the dialog has already been displayed
     * at least once.
     */
    private boolean firstPrompt = true;
    
    /**
     * Creates a new user prompter that uses a dialog to prompt the user.
     * Normally this constructor should be called via a {@link UserPrompterFactory}
     * such as the current ArchitectSession.
     */
    public ModalDialogUserPrompter(
            JFrame owner, String questionMessage, String okText,
            String notOkText, String cancelText) {
        /*
         *         String fileNameMessage = fileName;
         *         String filePathMessage = "at " + path;
         *         String questionMessage = "already exists. Do you wish to overwrite it?";
         */
        this.owner = owner;
        applyToAll = new JCheckBox("Apply to all");
        
        confirmDialog = new JDialog(owner);
        confirmDialog.setTitle("Overwrite");
        
        // this is just filled with the message pattern template to help with sizing
        questionLabel = new JLabel(questionMessage);
        
        questionFormat = new MessageFormat(questionMessage);
        
        JPanel confirmPanel = new JPanel();
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref:grow, 2dlu, 10dlu"
                                                , "");
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, confirmPanel);
        builder.setDefaultDialogBorder();
        
        builder.nextColumn(2);
        builder.append(questionLabel);
        builder.nextLine();
        
        builder.append("");
        builder.append(applyToAll);
        builder.nextLine();
        
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        JButton okButton = new JButton(okText);
        JButton notOkButton = new JButton(notOkText);
        JButton cancelButton = new JButton(cancelText);
        buttonBar.addGlue();
        buttonBar.addGridded(okButton);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(notOkButton);
        buttonBar.addRelatedGap();
        buttonBar.addGridded(cancelButton);
        buttonBar.addGlue();
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = UserPromptResponse.OK;
                confirmDialog.dispose();
            }
        });
        notOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = UserPromptResponse.NOT_OK;
                confirmDialog.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                response = UserPromptResponse.CANCEL;
                confirmDialog.dispose();
            }
        });
        builder.append("");
        builder.append(buttonBar.getPanel());
        confirmDialog.setModal(true);
        confirmDialog.add(builder.getPanel());
    }
    
    /**
     * Solicits a response from the user by presenting the modal dialog (unless
     * the user has previously selected "apply to all"). This method can be
     * called from any thread; if not called from the Swing EDT and the dialog
     * has to be shown, the current thread will be suspended until the dialog
     * has been shown and dismissed.
     */
    public UserPromptResponse promptUser(final Object ... formatArgs) {

        if (logger.isDebugEnabled()) {
            logger.debug("Prompting user. Format Args: " + Arrays.asList(formatArgs));
        }
        
        if (applyToAll.isSelected()) {
            return response;
        }
        
        // The default response, in case the user closes the dialog without
        // pressing one of the buttons
        response = UserPromptResponse.CANCEL;
        
        Runnable promptUser = new Runnable() {
            public void run() {
                questionLabel.setText(questionFormat.format(formatArgs));
                if (firstPrompt) {
                    confirmDialog.pack();
                    confirmDialog.setLocationRelativeTo(owner);
                    firstPrompt = false;
                }
                confirmDialog.setVisible(true);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
          promptUser.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(promptUser);
            } catch (InterruptedException e) {
                ASUtils.showExceptionDialogNoReport(owner,
                        "Failed to show prompting dialog", e);
            } catch (InvocationTargetException e) {
                ASUtils.showExceptionDialogNoReport(owner,
                        "Failed to show prompting dialog", e);
            }
        }
        
        return response;
    }

}
