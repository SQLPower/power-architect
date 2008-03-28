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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
     * The component that contains the question. The text will be replaced every
     * time {@link #promptUser(Object[])} gets called, based on the format
     * arguments provided.
     */
    private final JTextArea questionField;
    
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
        this.owner = owner;
        applyToAll = new JCheckBox("Apply to all");
        
        confirmDialog = new JDialog(owner);
        confirmDialog.setTitle("Overwrite");
        
        // this is just filled with the message pattern template to help with sizing
        questionField = new JTextArea(questionMessage);
        questionField.setEditable(false);
        questionField.setBackground(null);
        
        questionFormat = new MessageFormat(questionMessage);
        
        JPanel confirmPanel = new JPanel();
        FormLayout formLayout = new FormLayout("10dlu, 2dlu, pref:grow, 2dlu, 10dlu"
                                                , "");
        DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, confirmPanel);
        builder.setDefaultDialogBorder();
        
        builder.nextColumn(2);
        builder.append(questionField);
        builder.nextLine();
        
        builder.appendParagraphGapRow();
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
        builder.nextLine();
        
        builder.append("");
        builder.append(applyToAll);

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
                questionField.setText(questionFormat.format(formatArgs));
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
