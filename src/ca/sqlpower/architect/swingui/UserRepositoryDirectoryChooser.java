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

import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import ca.sqlpower.architect.etl.kettle.KettleRepositoryDirectoryChooser;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class implements the KettleRepositoryDirectoryChooser with a user interface
 */
public class UserRepositoryDirectoryChooser implements KettleRepositoryDirectoryChooser {

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
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(repo.getDirectoryTree());
        populateTree(root);
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
     */
    private void populateTree(DefaultMutableTreeNode root) {
        for (int i = 0; i < ((RepositoryDirectory)root.getUserObject()).getNrSubdirectories(); i++) {
            DefaultMutableTreeNode child = new 
                DefaultMutableTreeNode(((RepositoryDirectory)root.getUserObject()).getSubdirectory(i));
            populateTree(child);
            root.add(child);
        }
    }

}
