/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.enterprise;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl;
import ca.sqlpower.architect.swingui.action.Messages;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ServerProjectsManagerPanel {

    private final Component dialogOwner;
    private final ArchitectSessionContext context;
    private final ArchitectSwingSession session;
    
    private final JPanel panel;
    private final Action closeAction;
    private final JList projects;
    private final JList servers;
    
    private final Action refreshAction = new AbstractAction("Refresh") {
        public void actionPerformed(ActionEvent e) {
            refreshInfoList();
        }
    };
    
    private final Action newAction = new AbstractAction("New...") {
        public void actionPerformed(ActionEvent e) {
         
            if (getSelectedServerInfo() != null) {
               
                String name = JOptionPane.showInputDialog(dialogOwner, "Please specify the name of your project", "", JOptionPane.QUESTION_MESSAGE);
                
                if (name != null) {
                    DefaultListModel model = (DefaultListModel) projects.getModel();
                    for (int i = 0; i < model.size(); i++) {
                        if (((ProjectLocation) model.getElementAt(i)).getName().trim().equalsIgnoreCase(name.trim())) {
                            JOptionPane.showMessageDialog(dialogOwner, "A project called \"" + name + "\" already exists. Please use a different name.", "Duplicate project name", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    try {
                        ArchitectFrame frame = session.getArchitectFrame();
                        
                        JLabel messageLabel = new JLabel("Creating New Project");
                        JProgressBar progressBar = new JProgressBar();
                        progressBar.setIndeterminate(true);
                        
                        final JDialog dialog = new JDialog(frame, "Creating New Project");
                        
                        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref"));
                        builder.setDefaultDialogBorder();
                        builder.append(messageLabel, 3);
                        builder.nextLine();
                        builder.append(progressBar, 3);
                        dialog.add(builder.getPanel());
                        
                        dialog.setSize(new Dimension(300,90));
                        dialog.setLocation(frame.getX() + (frame.getWidth() - dialog.getWidth())/2, 
                                           frame.getY() + (frame.getHeight() - dialog.getHeight())/2);
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                        
                        ArchitectClientSideSession.createNewServerSession(getSelectedServerInfo(), name, session);
 
                        dialog.dispose();
                        
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to create new project", ex);
                    }

                    refreshInfoList();
                }
            }
        }
    };
    
    private final Action openAction = new AbstractAction("Open") {
        public void actionPerformed(ActionEvent e) {
            
            if (getSelectedServerInfo() != null) {
                
                int [] indices = projects.getSelectedIndices();
                
                if (indices.length >= 1) {
                    
                    final Object [] objs = new Object[indices.length];
                    for (int i = 0; i < indices.length; i++) {
                        objs[i] = projects.getModel().getElementAt(indices[i]);
                    }
                    
                    for (Object obj : objs) {
                        if (obj instanceof ProjectLocation) {
                            ProjectLocation location = (ProjectLocation) obj;
                            try {
                                
                                ArchitectSwingSession newSession = ((ArchitectSwingSessionContextImpl) context).createServerSession(location, false);
                                ArchitectFrame frame = session.getArchitectFrame();
                                frame.addSession(newSession);
                                frame.setCurrentSession(newSession);
                                
                                JLabel messageLabel = new JLabel("Opening");
                                JProgressBar progressBar = new JProgressBar();
                                progressBar.setIndeterminate(true);
                                
                                final JDialog dialog = new JDialog(frame, "Opening");
                                DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 5dlu, pref"));
                                builder.setDefaultDialogBorder();
                                builder.append(messageLabel, 3);
                                builder.nextLine();
                                builder.append(progressBar, 3);
                                dialog.add(builder.getPanel());
                                
                                dialog.pack();
                                dialog.setLocation(frame.getX() + (frame.getWidth() - dialog.getWidth())/2, 
                                                   frame.getY() + (frame.getHeight() - dialog.getHeight())/2);
                                dialog.setAlwaysOnTop(true);
                                dialog.setVisible(true);
                                
                                ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) newSession).getDelegateSession())
                                .getUpdater().addListener(new AbstractNetworkConflictResolver.UpdateListener() {
                                    public boolean updatePerformed(AbstractNetworkConflictResolver resolver) {
                                        dialog.dispose();
                                        return true;
                                    }

                                    public boolean updateException(AbstractNetworkConflictResolver resolver, Throwable t) {
                                        return false;
                                    }

                                    public void preUpdatePerformed(AbstractNetworkConflictResolver resolver) {
                                        //do nothing
                                    }

                                    public void workspaceDeleted() {
                                        refreshInfoList();
                                    }
                                });
                                
                                ((ArchitectClientSideSession) ((ArchitectSwingSessionImpl) newSession).getDelegateSession()).startUpdaterThread();
                            
                            } catch (Exception ex) {
                                throw new RuntimeException("Unable to open project", ex);
                            }
                        } 
                    }
                    
                    closeAction.actionPerformed(e);
                }
            }
        }
    }; 
   
    private final Action uploadAction = new AbstractAction("Upload") {
        
        public void actionPerformed(ActionEvent e) {
            final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(dialogOwner), "Upload Project", ModalityType.DOCUMENT_MODAL);
            
            JPanel panel = new JPanel();
            FormLayout layout = new FormLayout("4dlu, pref, 4dlu, max(150dlu;pref), 4dlu, pref, 4dlu", "pref, pref, pref");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout, panel);
            CellConstraints cc = new CellConstraints();
            
            final JTextField nameField = new JTextField();
            builder.add(new JLabel("Name"), cc.xy(2, 1));
            builder.add(nameField, cc.xyw(4, 1, 3));
            
            final JTextField fileField = new JTextField();
            JButton fileButton = new JButton("...");
            fileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser(session.getProjectLoader().getFile());
                    chooser.addChoosableFileFilter(SPSUtils.ARCHITECT_FILE_FILTER);
                    chooser.setDialogTitle(Messages.getString("Choose Project to Upload"));

                    int response = chooser.showOpenDialog(dialog);

                    if (response != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    fileField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });
            builder.add(new JLabel("File"), cc.xy(2, 2));
            builder.add(fileField, cc.xy(4, 2));
            builder.add(fileButton, cc.xy(6, 2));
            
            final JButton okButton = new JButton("Upload");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    File f = new File(fileField.getText());
                    if (!f.canRead()) {
                        JOptionPane.showMessageDialog(dialogOwner, "File cannot be read", "Invalid File", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        ArchitectClientSideSession.uploadProject(getSelectedServerInfo(), nameField.getText(), f, session);
                        dialog.dispose();
                        refreshInfoList();
                    } catch (Exception ex) {
                        SPSUtils.showExceptionDialogNoReport(dialog, "Unable to upload project", ex);
                    }
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            
            okButton.setEnabled(false);
            DocumentListener documentListener = new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    textChanged(e);
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    textChanged(e);
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    textChanged(e);
                }
                private void textChanged(DocumentEvent e) {
                    okButton.setEnabled(!fileField.getText().isEmpty() && !nameField.getText().isEmpty());
                }
            };
            fileField.getDocument().addDocumentListener(documentListener);
            nameField.getDocument().addDocumentListener(documentListener);
            
            JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
            builder.add(buttonBar, cc.xyw(2, 3, 5));
            dialog.add(panel);
            SPSUtils.makeJDialogCancellable(dialog, null);
            dialog.pack();
            dialog.setLocationRelativeTo(dialogOwner);
            dialog.setVisible(true);
        }
    };
    
    private final Action deleteAction = new AbstractAction("Delete") {
        public void actionPerformed(ActionEvent e) {
         
            if (getSelectedServerInfo() != null) {
                
                int [] indices = projects.getSelectedIndices();
                
                if (indices.length >= 1) {
                    
                    final Object [] objs = new Object[indices.length];
                    for (int i = 0; i < indices.length; i++) {
                        objs[i] = projects.getModel().getElementAt(indices[i]);
                    }
                    
                    String promptMessage;
                    if (indices.length == 1) {
                        promptMessage = "Are you sure you want to delete the selected project?" +
                                        "\nThis action cannot be undone.";
                    } else {
                        promptMessage = "Are you sure you want to delete these " + indices.length + " selected projects?" +
                                        "\nThis action cannot be undone.";
                    }
                    
                    if (JOptionPane.showConfirmDialog(dialogOwner, promptMessage, "Confirm Delete Projects", 
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        for (Object obj : objs) {
                            if (obj instanceof ProjectLocation) {
                                ProjectLocation location = (ProjectLocation) obj;
                                try {
                                    ArchitectClientSideSession.deleteServerWorkspace(location, session);
                                } catch (Exception ex) {
                                    throw new RuntimeException("Unable to delete project", ex);
                                }
                            } 
                        }
                        
                        refreshInfoList();
                    }
                } 
            }      
        }
    };
    
    private final Action openSecurityManagerPanelAction = new AbstractAction("Security") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(dialogOwner, "Security Manager");
            Action closeAction = new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                }
            };

            SecurityPanel spm = new SecurityPanel(getSelectedServerInfo(), closeAction, d, session);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(spm.getPanel());
            
            SPSUtils.makeJDialogCancellable(d, null);
            d.pack();
            d.setLocationRelativeTo(dialogOwner);
            d.setVisible(true);
        }
    };
       
    private boolean connected = false;
    private SPServerInfo serverInfo = null;

    public ServerProjectsManagerPanel(
            SPServerInfo serverInfo,
            ArchitectSwingSession session,
            ArchitectSessionContext context, 
            Component dialogOwner, 
            Action closeAction) 
    {
        this.serverInfo = serverInfo;
        this.dialogOwner = dialogOwner;
        this.session = session;
        this.context = context;
        this.closeAction = closeAction;
        
        ArchitectClientSideSession.getCookieStore().clear();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref", 
                "pref, pref, pref"));
        
        servers = null;
        
        projects = new JList(new DefaultListModel());
        projects.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                refreshPanel();
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openAction.actionPerformed(null);
                }
            }
        });
        
        JScrollPane projectsPane = new JScrollPane(projects);
        projectsPane.setPreferredSize(new Dimension(250, 300));
        
        JButton securityButton = new JButton(openSecurityManagerPanelAction);
        
        refreshInfoList();
        CellConstraints cc = new CellConstraints();    
        builder.add(new JLabel(serverInfo.getName() + "'s projects:"), cc.xyw(1, 1, 2));
        builder.nextLine();
        builder.add(projectsPane, cc.xywh(1, 2, 1, 2));
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(securityButton);
        buttonBarBuilder.append(new JButton(newAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(deleteAction));
        buttonBarBuilder.append(new JButton(closeAction));
        builder.add(buttonBarBuilder.getPanel(), cc.xy(3, 2));
        builder.setDefaultDialogBorder();
        panel = builder.getPanel();
    }
    
    public ServerProjectsManagerPanel(
            ArchitectSwingSession session,
            ArchitectSessionContext context, 
            Component dialogOwner, 
            Action closeAction) 
    {
        this.session = session;
        this.dialogOwner = dialogOwner;
        this.context = context;
        this.closeAction = closeAction;
        
        ArchitectClientSideSession.getCookieStore().clear();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref:grow, 5dlu, pref", 
                "pref, pref, pref"));
        
        servers = new JList(new DefaultListModel());
        servers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    refreshInfoList();
                }
            }
        });
        
        DefaultListModel serversModel = (DefaultListModel) servers.getModel();
        serversModel.removeAllElements();
        if (context.getServerManager().getServers(false).size() > 0) {
            for (SPServerInfo serverInfo : context.getServerManager().getServers(false)) {
                serversModel.addElement(serverInfo);
            }
        } else {
            serversModel.addElement("No Servers");
            servers.setEnabled(false);
        }
        
        projects = new JList(new DefaultListModel());
        projects.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                refreshPanel();
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openAction.actionPerformed(null);
                }
            }
        });
        
        JScrollPane projectsPane = new JScrollPane(projects);
        projectsPane.setPreferredSize(new Dimension(250, 300));
        
        JScrollPane serverPane = new JScrollPane(servers);
        serverPane.setPreferredSize(new Dimension(250, 300));
        
        JButton securityButton = new JButton(openSecurityManagerPanelAction);
        
        refreshInfoList();
        CellConstraints cc = new CellConstraints();    
        builder.add(new JLabel("Servers:"), cc.xyw(1, 1, 2));
        builder.add(new JLabel("Projects:"), cc.xyw(3, 1, 2));
        builder.nextLine();
        builder.add(serverPane, cc.xywh(1, 2, 1, 2));
        builder.add(projectsPane, cc.xywh(3, 2, 1, 2));
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(securityButton);
        buttonBarBuilder.append(new JButton(newAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(uploadAction));
        buttonBarBuilder.append(new JButton(deleteAction));
        buttonBarBuilder.append(new JButton(closeAction));
        builder.add(buttonBarBuilder.getPanel(), cc.xy(5, 2));
        builder.setDefaultDialogBorder();
        panel = builder.getPanel();
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    private void refreshPanel() {
        // Update the status of buttons and lists .
        if (connected) {
            
            openSecurityManagerPanelAction.setEnabled(true);
            newAction.setEnabled(true);
            uploadAction.setEnabled(true);
            
            if (projects.isSelectionEmpty()) {
                openAction.setEnabled(false);
                deleteAction.setEnabled(false);
            } else {
                openAction.setEnabled(true);
                deleteAction.setEnabled(true);
            }
            
            projects.setEnabled(true);
        } else {
            openSecurityManagerPanelAction.setEnabled(false);
            newAction.setEnabled(false);
            openAction.setEnabled(false);
            uploadAction.setEnabled(false);
            deleteAction.setEnabled(false);
            projects.setEnabled(false);
        }
    }
    
    private void refreshInfoList() {
        DefaultListModel model = (DefaultListModel) projects.getModel();
        model.removeAllElements();
        
        SPServerInfo serviceInfo = getSelectedServerInfo();
        if (serviceInfo != null) {
            try {
                
                ((ArchitectSwingSessionContextImpl) session.getContext()).createSecuritySession(serviceInfo);
                
                // Sorts the project locations alphabetically
                List<ProjectLocation> projects = ArchitectClientSideSession.getWorkspaceNames(serviceInfo,
                        session);
                Collections.sort(projects, new Comparator<ProjectLocation>() {
                    public int compare(ProjectLocation proj1, ProjectLocation proj2) {
                        return proj1.getName().compareToIgnoreCase(proj2.getName());
                    }
                });
                
                model.clear();
                for (ProjectLocation pl : projects) {
                    model.addElement(pl);
                }
                
                connected = true;
            } catch (Exception ex) {
                model.removeAllElements();
                model.addElement("Unable to get projects from server");
                connected = false;
                session.createUserPrompter("Server Unavailable", 
                        UserPromptType.MESSAGE, 
                        UserPromptOptions.OK, 
                        UserPromptResponse.OK, 
                        "OK", "OK").promptUser("");
            }
            
            refreshPanel();
        } else {
            model.addElement("No Server Selected");
            connected = false;
            refreshPanel();
        }    
    }
    
    private SPServerInfo getSelectedServerInfo() {
        if (serverInfo != null) return serverInfo;
        
        int index = servers.getSelectedIndex();
        Object obj;
        
        if (index >= 0) {
            obj = servers.getModel().getElementAt(index);
       
            if (obj instanceof SPServerInfo) {
                return (SPServerInfo) obj;
            } 
        }
        
        return null;
    }
}
