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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileManagerImpl;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.event.ProfileResultEvent;
import ca.sqlpower.architect.profile.event.ProfileResultListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A component that displays the status and either rowcount or progressbar
 * for the given table profile. Typical use is as one Row in the
 * ca.sqlpower.architect.swingui.ProfileManagerView, but has
 * no dependencies thereon.
 */
public class ProfileRowComponent extends JPanel implements Selectable {

    private static final Logger logger = Logger.getLogger(ProfileRowComponent.class);

    private enum ComponentType {
        ICON,
        TABLE_NAME,
        TABLE_INFO,
        PROGRESS_BAR, 
        RELOAD, 
        CANCEL,
        DELETE;
    }

    /** The icon for all the rows (shared) */
    private static ImageIcon tableIcon =
        SPSUtils.createIcon("Table", "Table Result", ArchitectSwingSessionContext.ICON_SIZE);
    
    /** The Stop Sign icon for all the rows (shared) */
    private static ImageIcon stopIcon =
        SPSUtils.createIcon("stop", "Stop Profile", ArchitectSwingSessionContext.ICON_SIZE);
    
    /** The reload icon for all the rows (shared) */
    private static ImageIcon refreshIcon =
        SPSUtils.createIcon("arrow_refresh", "Re-Profile", ArchitectSwingSessionContext.ICON_SIZE);

    /** shared delete icon */
    private static ImageIcon deleteIcon =
        SPSUtils.createIcon("delete", "Delete Profile", ArchitectSwingSessionContext.ICON_SIZE);
    
    /**
     * The profile result that this component visualizes. This should be
     * considered the "model" of this component.
     */
    private final TableProfileResult result;

    /**
     * The profile manager that owns the result we're visualizing.
     */
    private final ProfileManager pm;

    /**
     * The label component for this profile result row.  Its contents
     * are originally set to bogus but plausible numbers just to have
     * a feel of how big the status label should be when creating the
     * dialog.
     */
    private final JLabel statusLabel =
        new JLabel(String.format(TableProfileResult.TOSTRING_FORMAT, 500, "Mar 9, 2007", 15000));

    private final JButton reProfileButton, cancelButton, deleteButton;

    /**
     * Performs the custom layout for this row.  The layout
     * changes drastically when the profile results switch states
     * between populating and populated or canceled.
     */
    private static class RowComponentLayout implements LayoutManager2 {
        private int xGap;
        private int yGap;
        private Component icon;
        private Component tableName;
        private Component tableInfo;
        private Component progressBar;
        private Component reload;
        private Component cancel;
        private Component delete;


        public RowComponentLayout(int xGap, int yGap) {
            logger.debug("RowComponentLayout constructed");
            this.xGap = xGap;
            this.yGap = yGap;
        }

        public void addLayoutComponent(Component comp, Object constraints) {
            ComponentType type = (ComponentType) constraints;

            switch (type) {
            case ICON:
                icon = comp;
                break;
            case TABLE_NAME:
                tableName = comp;
                break;
            case TABLE_INFO:
                tableInfo = comp;
                break;
            case PROGRESS_BAR:
                progressBar = comp;
                break;
            case RELOAD:
                reload = comp;
                break;
            case CANCEL:
                cancel = comp;
                break;
            case DELETE:
                delete = comp;
                break;
            default:
                throw new IllegalStateException("Unknown constraint given to RowCompnentLayout");
            }
        }

        public float getLayoutAlignmentX(Container target) {
            return 0;
        }

        public float getLayoutAlignmentY(Container target) {
            return 0;
        }

        public void invalidateLayout(Container target) {
        }

        public Dimension maximumLayoutSize(Container target) {
            return preferredLayoutSize(target);
        }

        public void addLayoutComponent(String name, Component comp) {
            addLayoutComponent(comp, name);
        }

        public void layoutContainer(Container parent) {
            logger.debug("layoutContainer called");
            JComponent p = (JComponent) parent;
            Insets inset = p.getBorder().getBorderInsets(p);
            final int height = parent.getHeight() + inset.top;
            final int width = parent.getWidth() + inset.left;
            final int stretchyPreferredWidth = width -
                inset.left -
                inset.right -
                3 * xGap -
                icon.getPreferredSize().width -
                // reload and delete buttons are same size
                2 * reload.getPreferredSize().width;

            if (icon != null) {
                Dimension preferredSize = icon.getPreferredSize();
                int x = inset.left;
                int y = height /2 - preferredSize.height/2; 
                icon.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (tableName != null) {
                Dimension preferredSize = new Dimension(stretchyPreferredWidth,
                        tableName.getPreferredSize().height);
                int x = inset.left + icon.getPreferredSize().width + xGap;
                int y = height /2 - yGap/2 - preferredSize.height;
                tableName.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (tableInfo != null) {
                Dimension preferredSize = new Dimension(stretchyPreferredWidth,
                        tableInfo.getPreferredSize().height);
                int x = inset.left + icon.getPreferredSize().width + xGap;
                int y = height/2 + yGap/2;
                tableInfo.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (progressBar != null) {
                Dimension preferredSize = new Dimension(stretchyPreferredWidth,
                        progressBar.getPreferredSize().height);
                int x = inset.left + icon.getPreferredSize().width + xGap;
                int y = height/2 + yGap/2;
                progressBar.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (delete != null) {
                Dimension preferredSize = delete.getPreferredSize();
                int x = width - inset.right - preferredSize.width;
                int y = height/2 - preferredSize.height/2;
                delete.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (cancel != null) {
                Dimension preferredSize = cancel.getPreferredSize();
                int x = width - inset.right - preferredSize.width;
                int y = height/2 - preferredSize.height/2;
                cancel.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
            if (reload != null) {
                Dimension preferredSize = reload.getPreferredSize();
                int x = width - inset.right - cancel.getPreferredSize().width - xGap - preferredSize.width;
                int y = height/2 - preferredSize.height/2;
                reload.setBounds(x, y, preferredSize.width, preferredSize.height);
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(icon.getMinimumSize().width +  
                    3 * xGap + 
                    Math.max(progressBar.getMinimumSize().width, 
                            tableName.getMinimumSize().width) +
                            // reload and delete buttons same size
                            2 * reload.getMinimumSize().width, 
                            tableName.getMinimumSize().height +
                            yGap + progressBar.getMinimumSize().height);
        }

        public Dimension preferredLayoutSize(Container parent) {
            JComponent p = (JComponent) parent;
            Insets inset = p.getBorder().getBorderInsets(p);
            int maxOfBarAndInfo = Math.max(progressBar.getPreferredSize().width,
                    tableInfo.getPreferredSize().width);
            return new Dimension(icon.getPreferredSize().width +
                    3 * xGap +
                    Math.max(maxOfBarAndInfo, tableName.getPreferredSize().width) +
                    // reload and delete buttons same size
                    2 * reload.getPreferredSize().width +
                    inset.left + inset.right,
                    tableName.getPreferredSize().height +
                    yGap + progressBar.getPreferredSize().height +
                    inset.top + inset.bottom);
        }

        public void removeLayoutComponent(Component comp) {
        }

    }

    private class ProfileRowMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getButton() == MouseEvent.BUTTON1) {
                if (evt.getClickCount() == 2) {
                    if (getResult().getException() != null) {
                        ASUtils.showExceptionDialogNoReport(
                                ProfileRowComponent.this,
                                "Profiling failed for table " + getResult().getProfiledObject(),
                                getResult().getException());
                    } else if (getResult().getProgressMonitor().isFinished() && 
                                    !getResult().getProgressMonitor().isCancelled() &&
                                    !(obj instanceof JButton)) {
                        ProfileResultsViewer profileResultsViewer = 
                            new ProfileResultsViewer((ProfileManagerImpl) pm);
                        profileResultsViewer.clearScanList();
                        profileResultsViewer.addTableProfileResultToScan(result);
                        profileResultsViewer.addTableProfileResult(result);
                        profileResultsViewer.getDialog().setVisible(true);
                    }
                } else if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0){
                    setSelected(!selected, SelectionEvent.CTRL_MULTISELECT);
                } else if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0){
                    setSelected(true, SelectionEvent.SHIFT_MULTISELECT);
                }  else {
                    setSelected(true, SelectionEvent.SINGLE_SELECT);
                }
            }
        }
    }
    
    /**
     * Listens for changes in the profile's state (started, cancelled, finished)
     * and makes the appropriate UI updates on this component.
     */
    private final ProfileResultListener profileResultListener = new ProfileResultListener() {
        public void profileCancelled(ProfileResultEvent event) {
            statusLabel.setVisible(true);
            statusLabel.setText("Cancelled");
            cancelButton.setVisible(false);
            deleteButton.setVisible(true);
            progressBar.setVisible(false);
            reProfileButton.setVisible(true);
        }

        public void profileFinished(ProfileResultEvent event) {
            reProfileButton.setVisible(true);
            cancelButton.setVisible(false);
            deleteButton.setVisible(true);
            statusLabel.setVisible(true);
            if (result.getException() == null) {
                if (result.getCreateStartTime() >= 0) {
                    statusLabel.setText(result.toString());
                    statusLabel.setForeground(null);
                } else {
                    statusLabel.setText("Waiting to be profiled...");
                    statusLabel.setForeground(null);
                }
            } else {
                statusLabel.setText("Failed: " + result.getException().getMessage());
                statusLabel.setForeground(Color.RED);
            }
            progressBar.setVisible(false);
        }

        public void profileStarted(ProfileResultEvent event) {
            reProfileButton.setVisible(false);
            statusLabel.setVisible(false);
        }
    };

    /**
     * Creates a profile row component that visualizes the given profile
     * result and provides a user interface for stopping, restarting,
     * deleting, and viewing the profile result.
     * 
     * @param result The profile result object to visualize.  Note that
     * the result will not normally be populated by the time it is given
     * to this component, but it should have already been added to the
     * profile manager.
     * @param pm The profile manager that the result belongs to.  It
     * would be better if the result object exposed a parent pointer back
     * to its manager, so this constructor could just take the result arg.
     */
    public ProfileRowComponent(final TableProfileResult result, final ProfileManager pm) {
        super(new RowComponentLayout(5, 5));
        this.result = result;
        this.pm = pm;
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setBackground(Color.WHITE);

        add(new JLabel(tableIcon), ComponentType.ICON);
        
        this.reProfileButton = new JButton(refreshIcon);
        reProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton.setVisible(true);
                deleteButton.setVisible(false);
                reProfileButton.setVisible(false);
                ProgressWatcher.watchProgress(progressBar, result.getProgressMonitor());
                add(progressBar, ComponentType.PROGRESS_BAR);
                pm.scheduleProfile(result);
                logger.debug("REFRESH");
            }
        });
        
        this.cancelButton = new JButton(stopIcon);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                result.getProgressMonitor().setCancelled(true);
                logger.debug("STOP");
            }
        });
        
        this.deleteButton = new JButton(deleteIcon);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.debug("ProfileRowComponent: DELETE object: " + result);
                pm.removeProfile(result);
            }
        });
        add(deleteButton, ComponentType.DELETE);
        deleteButton.setVisible(false);
        
        add(new JLabel(result.getProfiledObject().getName()), ComponentType.TABLE_NAME);
        
        add(reProfileButton, ComponentType.RELOAD);
        reProfileButton.setVisible(false);
        
        ProgressWatcher pw = new ProgressWatcher(progressBar, result.getProgressMonitor());
        pw.setHideLabelWhenFinished(true);
        pw.setHideProgressBarWhenFinished(true);
        pw.start();
        
        result.addProfileResultListener(profileResultListener);
        
        add(progressBar, ComponentType.PROGRESS_BAR);
        statusLabel.setVisible(false);
        add(statusLabel, ComponentType.TABLE_INFO);      
        add(cancelButton, ComponentType.CANCEL);  
        this.addMouseListener(new ProfileRowMouseListener());
        
        profileResultListener.profileFinished(null);
    }

    public TableProfileResult getResult() {
        return result;
    }
    
    private boolean selected = false;
    private List<SelectionListener> listeners = new ArrayList<SelectionListener>();

    private final JProgressBar progressBar = new JProgressBar();
    
    public void setSelected(boolean v,int selectionType) {
        selected = v;
        setBackground(selected ? UIManager.getColor("List.selectionBackground"): UIManager.getColor("List.background"));
        fireSelectionEvent(new SelectionEvent(ProfileRowComponent.this, selected ? SelectionEvent.SELECTION_EVENT : SelectionEvent.DESELECTION_EVENT,selectionType));
    }

    public boolean isSelected() {
        return selected;
    }

    public void addSelectionListener(SelectionListener l) {
        listeners.add(l);
    }

    public void removeSelectionListener(SelectionListener l) {
        listeners.remove(l);
    }
    
    public void fireSelectionEvent(SelectionEvent e) {
        for (SelectionListener listener : listeners) {
            if (e.getType() == SelectionEvent.SELECTION_EVENT) {
                listener.itemSelected(e);
            } else {
                listener.itemDeselected(e);
            }
        }
    }
}
