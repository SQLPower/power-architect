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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;

import ca.sqlpower.architect.etl.kettle.CreateKettleJob;
import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class implements the KettleRepositoryDirectoryChooser with a user interface
 */
public class UserRepositoryDirectoryChooser implements KettleRepositoryDirectoryChooser {

    private static final Logger logger = Logger.getLogger(CreateKettleJob.class);
    
    /**
     * This is the parent frame to know which frame to make this dialog on top of.
     */
    private JFrame parent;
    
    /**
     * The repository directory that has been or will be selected
     */
    private RepositoryDirectory directory = null;
    
    public UserRepositoryDirectoryChooser(JFrame parent) {
        this.parent = parent;
    }

    /**
     * This method allows the selection of the directory. Null is returned if the user cancelled.
     */
    public RepositoryDirectory selectDirectory(Repository repo) {
        final JDialog chooserDialog = new JDialog(parent);
        chooserDialog.setTitle("Select Repository Directory");
        FormLayout layout = new FormLayout("10dlu, 2dlu, fill:pref:grow, 12dlu", "pref, fill:pref:grow, pref");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.nextColumn(2);
        builder.append("Choose the directory to save in");
        builder.nextLine();
        DefaultMutableTreeNode root = populateTree(repo.getDirectoryTree());
        final JTree tree = new JTree(root);
        builder.append("");
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(200, 350));
        builder.append(scrollPane);
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        buttonBar.addGlue();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() == null) {
                    return;
                }
                directory = (RepositoryDirectory)((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
                chooserDialog.dispose();
            }
        });
        buttonBar.addGridded(okButton);
        buttonBar.addRelatedGap();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                directory = null;
                chooserDialog.dispose();
            }
        });
        buttonBar.addGridded(cancelButton);
        builder.nextLine();
        builder.append("");
        builder.add(buttonBar.getPanel());
        
        chooserDialog.add(builder.getPanel());

        chooserDialog.setModal(true);
        Runnable promptUser = new Runnable() {
            public void run() {
                chooserDialog.pack();
                chooserDialog.setLocationRelativeTo(parent);
                chooserDialog.setVisible(true);
            }
        };

        if (SwingUtilities.isEventDispatchThread ()) {
            promptUser.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(promptUser);
            } catch (InterruptedException e) {
                ASUtils.showExceptionDialog("While queing the dialog's pack, setVisible and setLocation, we were interrupted", e);
            } catch (InvocationTargetException e) {
                ASUtils.showExceptionDialog("While queing the dialog's pack, setVisible and setLocation, an InvocationTargetException was thrown", e);
            }
        }  
        return directory;
    }

    /**
     * This is a recursive helper method to create the tree to show the user for directory selection
     * 
     * @return The root tree node of the tree made from the repository directory
     */
    private DefaultMutableTreeNode populateTree(RepositoryDirectory rootDir) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDir);
        int numIterations = rootDir.getNrSubdirectories();
        for (int i = 0; i < numIterations; i++) {
            logger.debug("Populating the tree: on iteration " + i + " of " + numIterations);
            RepositoryDirectory childDir = rootDir.getSubdirectory(i);
            DefaultMutableTreeNode child = populateTree(childDir);
            root.add(child);
        }
        return root;
    }

}
