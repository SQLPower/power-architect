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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.ArchitectSessionContext;
import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.enterprise.ProjectLocation;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.SPServerInfoManager;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ServerProjectsManagerPanel {

    private final Component dialogOwner;
    private final JPanel panel;
    private final SPServerInfoManager serverInfoManager;
    private final ArchitectSessionContext context;
    private JList projects;
    private JList servers;
    
    private Action refreshAction = new AbstractAction("Refresh...") {
        public void actionPerformed(ActionEvent e) {
            refreshInfoList();
        }
    };
    
    private Action newAction = new AbstractAction("New...") {
        public void actionPerformed(ActionEvent e) {
         
            if (getSelectedServerInfo() != null) {
               
                String name = JOptionPane.showInputDialog(dialogOwner, "Please specify the name of your project", "", JOptionPane.QUESTION_MESSAGE);
                
                if (name != null) {
                    try {
                        ArchitectClientSideSession.createNewServerSession(getSelectedServerInfo());
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to create new project", ex);
                    }
                    
                    refreshInfoList();
                }
            }
        }
    };
    
    private Action openAction = new AbstractAction("Open...") {
        public void actionPerformed(ActionEvent e) {
            
            if (getSelectedServerInfo() != null) {
                
                int [] indecies = projects.getSelectedIndices();
                
                if (indecies.length >= 1) {
                    
                    final Object [] objs = new Object[indecies.length];
                    for (int i = 0; i < indecies.length; i++) {
                        objs[i] = projects.getModel().getElementAt(indecies[i]);
                    }
                    
                    for (Object obj : objs) {
                        if (obj instanceof ProjectLocation) {
                            ProjectLocation location = (ProjectLocation) obj;
                            try {
                                ((ArchitectSwingSessionContextImpl) context).createServerSession(location, true);
                            } catch (Exception ex) {
                                throw new RuntimeException("Unable to open project", ex);
                            }
                        } 
                    }
                        
                    refreshInfoList();      
                }
            }
        }
    }; 
   
    
    private Action deleteAction = new AbstractAction("Delete...") {
        public void actionPerformed(ActionEvent e) {
         
            if (getSelectedServerInfo() != null) {
                
                int [] indecies = projects.getSelectedIndices();
                
                if (indecies.length >= 1) {
                    
                    final Object [] objs = new Object[indecies.length];
                    for (int i = 0; i < indecies.length; i++) {
                        objs[i] = projects.getModel().getElementAt(indecies[i]);
                    }
                    
                    String promptMessage;
                    if (indecies.length == 1) {
                        promptMessage = "Are you sure you want to delete the selected project?" +
                                        "\nThis action cannot be undone.";
                    } else {
                        promptMessage = "Are you sure you want to delete these " + indecies.length + " selected projects?" +
                                        "\nThis action cannot be undone.";
                    }
                    
                    if (JOptionPane.showConfirmDialog(dialogOwner, promptMessage, "Confirm Delete Projects", 
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        for (Object obj : objs) {
                            if (obj instanceof ProjectLocation) {
                                ProjectLocation location = (ProjectLocation) obj;
                                try {
                                    ArchitectClientSideSession.deleteServerWorkspace(location);
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
    
    public ServerProjectsManagerPanel(ArchitectSessionContext context, SPServerInfoManager serverInfoManager, Component dialogOwner, Action closeAction) {
        this.dialogOwner = dialogOwner;
        this.serverInfoManager = serverInfoManager;
        this.context = context;
        
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
                "pref:grow, 5dlu, pref:grow, 5dlu, pref", 
                "pref, pref, pref"));
        
        servers = new JList(new DefaultListModel());
        servers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    refreshInfoList();
                }
            }
        });
        
        DefaultListModel serversModel = (DefaultListModel) servers.getModel();
        serversModel.removeAllElements();
        if (serverInfoManager.getServers(false).size() > 0) {
            for (SPServerInfo serverInfo : serverInfoManager.getServers(false)) {
                serversModel.addElement(serverInfo);
            }
        } else {
            serversModel.addElement("No Servers");
        }

        projects = new JList(new DefaultListModel());
        projects.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedProject();
                }
            }
        });
        
        JScrollPane projectsPane = new JScrollPane(projects);
        projectsPane.setPreferredSize(new Dimension(250, 300));
        
        JScrollPane serverPane = new JScrollPane(servers);
        serverPane.setPreferredSize(new Dimension(250, 300));
        
        refreshInfoList();
        CellConstraints cc = new CellConstraints();    
        builder.add(new JLabel("Servers:"), cc.xyw(1, 1, 2));
        builder.add(new JLabel("Projects:"), cc.xyw(3, 1, 2));
        builder.nextLine();
        builder.add(serverPane, cc.xywh(1, 2, 1, 2));
        builder.add(projectsPane, cc.xywh(3, 2, 1, 2));
        
        DefaultFormBuilder buttonBarBuilder = new DefaultFormBuilder(new FormLayout("pref"));      
        buttonBarBuilder.append(new JButton(refreshAction));
        buttonBarBuilder.append(new JButton(newAction));
        buttonBarBuilder.append(new JButton(openAction));
        buttonBarBuilder.append(new JButton(deleteAction));
        buttonBarBuilder.append(new JButton(closeAction));
        builder.add(buttonBarBuilder.getPanel(), cc.xy(5, 2));
        builder.setDefaultDialogBorder();
        panel = builder.getPanel();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    private void refreshInfoList() {
        DefaultListModel model = (DefaultListModel) projects.getModel();
        model.removeAllElements();
        
        SPServerInfo serviceInfo = getSelectedServerInfo();
        if (serviceInfo != null) {
            try {
                for (ProjectLocation pl : ArchitectClientSideSession.getWorkspaceNames(serviceInfo)) {
                    model.addElement(pl);
                } 
            } catch (Exception ex) {
                model.removeAllElements();
                model.addElement("There has been a problem retrieving projects from the selected server");
            }
        } else {
            model.addElement("No Server Selected");
        }    
    }
    
    private SPServerInfo getSelectedServerInfo() {
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
    
    private void openSelectedProject() {
        
    }
}
