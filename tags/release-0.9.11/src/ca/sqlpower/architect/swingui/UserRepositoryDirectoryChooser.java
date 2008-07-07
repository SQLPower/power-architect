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

import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class implements the KettleRepositoryDirectoryChooser with a user interface
 */
public class UserRepositoryDirectoryChooser implements KettleRepositoryDirectoryChooser {

    private static final Logger logger = Logger.getLogger(UserRepositoryDirectoryChooser.class);
    
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
        chooserDialog.setTitle(Messages.getString("UserRepositoryDirectoryChooser.selectRepositoryDialogTitle")); //$NON-NLS-1$
        FormLayout layout = new FormLayout("10dlu, 2dlu, fill:pref:grow, 12dlu", "pref, fill:pref:grow, pref"); //$NON-NLS-1$ //$NON-NLS-2$
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.nextColumn(2);
        builder.append(Messages.getString("UserRepositoryDirectoryChooser.chooseDirectory")); //$NON-NLS-1$
        builder.nextLine();
        DefaultMutableTreeNode root = populateTree(repo.getDirectoryTree());
        final JTree tree = new JTree(root);
        builder.append(""); //$NON-NLS-1$
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(200, 350));
        builder.append(scrollPane);
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        buttonBar.addGlue();
        JButton okButton = new JButton(Messages.getString("UserRepositoryDirectoryChooser.okOption")); //$NON-NLS-1$
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
        JButton cancelButton = new JButton(Messages.getString("UserRepositoryDirectoryChooser.cancelOption")); //$NON-NLS-1$
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                directory = null;
                chooserDialog.dispose();
            }
        });
        buttonBar.addGridded(cancelButton);
        builder.nextLine();
        builder.append(""); //$NON-NLS-1$
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
                ASUtils.showExceptionDialogNoReport(parent, "While queing the dialog's pack, setVisible and setLocation, we were interrupted", e); //$NON-NLS-1$
            } catch (InvocationTargetException e) {
                ASUtils.showExceptionDialogNoReport(parent, "While queing the dialog's pack, setVisible and setLocation, an InvocationTargetException was thrown", e); //$NON-NLS-1$
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
            logger.debug("Populating the tree: on iteration " + i + " of " + numIterations); //$NON-NLS-1$ //$NON-NLS-2$
            RepositoryDirectory childDir = rootDir.getSubdirectory(i);
            DefaultMutableTreeNode child = populateTree(childDir);
            root.add(child);
        }
        return root;
    }

}
