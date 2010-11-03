/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.enterprise;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.json.JSONException;

import ca.sqlpower.architect.enterprise.ArchitectClientSideSession;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContextImpl;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl;
import ca.sqlpower.enterprise.AbstractNetworkConflictResolver;
import ca.sqlpower.enterprise.client.ProjectLocation;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.enterprise.client.ServerProjectsManagerPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectServerProjectsManagerPanel extends ServerProjectsManagerPanel {
    
    private final ArchitectSwingSession session;

    private final ArchitectSwingSessionContext context;

    public ArchitectServerProjectsManagerPanel(
            ArchitectSwingSession session,
            ArchitectSwingSessionContext context,
            Action closeAction) {
        super(context.getServerManager(), session.getArchitectFrame(), session, 
                closeAction, session.getProjectLoader().getFile(), 
                ArchitectClientSideSession.getCookieStore());
        this.session = session;
        this.context = context;
    }

    public ArchitectServerProjectsManagerPanel(
            SPServerInfo serverInfo, 
            ArchitectSwingSession session,
            ArchitectSwingSessionContext context,
            Action closeAction) {
        super(serverInfo, session.getArchitectFrame(), session, 
                closeAction, session.getProjectLoader().getFile(),
                ArchitectClientSideSession.getCookieStore());
        this.session = session;
        this.context = context;
    }

    private final Action openAction = new AbstractAction("Open") {
        public void actionPerformed(ActionEvent e) {
            List<ProjectLocation> selectedProjects = getSelectedProjects();
            if (!selectedProjects.isEmpty()) {

                for (ProjectLocation location : selectedProjects) {
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
                getCloseAction().actionPerformed(e);
            }
        }
    }; 

    private final Action openSecurityManagerPanelAction = new AbstractAction("Security") {
        public void actionPerformed(ActionEvent e) {
            
            final JDialog d = SPSUtils.makeOwnedDialog(getDialogOwner(), "Security Manager");
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
            d.setLocationRelativeTo(getDialogOwner());
            d.setVisible(true);
        }
    };

    @Override
    protected List<ProjectLocation> getProjectLocations() throws 
        IOException, URISyntaxException, JSONException {
        
        ((ArchitectSwingSessionContextImpl) session.getContext()).
            createSecuritySession(getSelectedServerInfo());
        
        return ArchitectClientSideSession.getWorkspaceNames(getSelectedServerInfo(),
                session);
    }

    @Override
    protected Action getOpenAction() {
        return openAction;
    }

    @Override
    protected Action getSecurityAction() {
        return openSecurityManagerPanelAction;
    }
}
