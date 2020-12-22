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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.profile.event.ProfileChangeEvent;
import ca.sqlpower.architect.profile.event.ProfileChangeListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.swingui.Search;
import ca.sqlpower.swingui.SearchTextField;

/**
 * The controlling view for the Profile Manager. Vaguely patterned on e.g., the
 * Safari Download box; has some controls at the top and bottom,
 * and a row of components that displays, for each table profile result,
 * the name of the table AND either:
 * <ul><li>the date, rows, elapsed time, and a Re-Profile button, 
 * and a "delete this profile" button
 * <li>a progressbar and a Stop button. </ul>
 * <p>
 */
public class ProfileManagerView extends JPanel implements ProfileChangeListener, Search {
    
    private static Logger logger = Logger.getLogger(ProfileManagerView.class);
    
    /**
     * The number of rows we'd like the list to show by default
     */
    private final static int VISIBLE_ROWS = 8;

    /**
     * The profile manager this view is attached to.
     */
	private final ProfileManager pm;

    private final ResultListPanel resultListPanel;

    private final JScrollPane scrollPane;

    private final JLabel statusText;

    private final SearchTextField searchText;
    
    private final PageListener pageListener;
    
    /**
     * This is the sort order to show the profile results in. It will change
     * when you click on the radio buttons to change how the results will
     * be sorted in the list.
     */
    private Comparator<ProfileRowComponent> comparator;
    
    /**
     * The list of all valid ProfileRowComponents; note that this is NOT
     * necessarily the same as the list that is showing (see doSearch() for why not).
     */
    List<ProfileRowComponent> list = new ArrayList<ProfileRowComponent>();
    
    /**
     * The list of row components we will be showing in the results panel.
     */
    final List<ProfileRowComponent> showingRows = new ArrayList<ProfileRowComponent>();
    
    /**
     * This is the last pattern searched on from a change to the search text field.
     * This will be null until the first search is executed.
     */
    private Pattern lastSearchPattern;
    
    /**
     * This is the last value given to doSearch() for the matchExactly property.
     */
    private boolean lastMatchExactValue;
    
    /**
     * True if there is a selected row in showingRows.
     */
    private boolean hasProfileSelected = false;

    private class ResultListPanel extends JPanel implements Scrollable, SelectionListener {
        
        /**
         * This string maps an action for pressing up on the keyboard to select
         * the profile above the selected profile.
         */
        private static final String SELECT_ABOVE_ACTION = "selectAboveAction";
        
        /**
         * This string maps an action for pressing down on the keyboard to select
         * the profile below the selected profile.
         */
        private static final String SELECT_BELOW_ACTION = "selectBelowAction";
        
        /**
         * This string maps an action for pressing delete on the keyboard remove
         * the profile from the profile manager.
         */
        private static final String DELETE_ACTION = "deleteAction";

        /**
         * This string maps an action for pressing enter on the keyboard to
         * display the selected profiles.
         */
        private static final String SHOW_PROFILE_ACTION = "showProfileAction";
        
        private ProfileRowComponent lastSelectedRow;
        private boolean ignoreSelectionEvents = false;
        
        /**
         * This scroll pane contains the ResultListPanel to be displayed. This
         * can be null if the ResultListPanel is not in a JScrollPane.
         */
        private final JScrollPane parentScrollPane;
        
        public ResultListPanel(JScrollPane parent) {
            this.parentScrollPane = parent;
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), SELECT_ABOVE_ACTION);
            getActionMap().put(SELECT_ABOVE_ACTION, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    ProfileRowComponent focusedRow = null;
                    for (ProfileRowComponent prc : showingRows) {
                        if (prc.hasFocus()) {
                            focusedRow = prc;
                            break;
                        }
                    }
                    if (focusedRow == null) {
                        return;
                    }
                    if (showingRows.indexOf(focusedRow) != 0) {
                        ProfileRowComponent abovePRC = showingRows.get(showingRows.indexOf(focusedRow) - 1);
                        abovePRC.setSelected(true, SelectionEvent.SINGLE_SELECT);
                        if (parentScrollPane != null) {
                            Rectangle viewRect = parentScrollPane.getViewport().getViewRect();
                            parentScrollPane.getViewport().scrollRectToVisible(new Rectangle((int) (abovePRC.getX() - viewRect.getX()),
                                                                                        (int) (abovePRC.getY() - viewRect.getY()),
                                                                                        (int) (abovePRC.getWidth()),
                                                                                        (int) (abovePRC.getHeight())));
                        }
                    }
                }
            });
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), SELECT_BELOW_ACTION);
            getActionMap().put(SELECT_BELOW_ACTION, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    ProfileRowComponent focusedRow = null;
                    for (ProfileRowComponent prc : showingRows) {
                        if (prc.hasFocus()) {
                            focusedRow = prc;
                            break;
                        }
                    }
                    if (focusedRow == null) {
                        return;
                    }
                    if (showingRows.indexOf(focusedRow) != showingRows.size() - 1) {
                        ProfileRowComponent belowPRC = showingRows.get(showingRows.indexOf(focusedRow) + 1);
                        belowPRC.setSelected(true, SelectionEvent.SINGLE_SELECT);
                        if (parentScrollPane != null) {
                            Rectangle viewRect = parentScrollPane.getViewport().getViewRect();
                            parentScrollPane.getViewport().scrollRectToVisible(new Rectangle((int) (belowPRC.getX() - viewRect.getX()),
                                                                                        (int) (belowPRC.getY() - viewRect.getY()),
                                                                                        (int) (belowPRC.getWidth()),
                                                                                        (int) (belowPRC.getHeight())));
                        }
                    }
                }
                
            });
            
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ACTION);
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DELETE_ACTION);
            getActionMap().put(DELETE_ACTION, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (int i = showingRows.size() - 1; i >= 0; i--) {
                        ProfileRowComponent rowComp = showingRows.get(i);
                        if (rowComp.isSelected()) {
                            pm.removeProfile(rowComp.getResult());
                        }
                    }
                    updateResultListPanel();
                } 
            });
            
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), SHOW_PROFILE_ACTION);
            getActionMap().put(SHOW_PROFILE_ACTION, viewSelectedAction);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return d;
        }
        
        public Dimension getPreferredScrollableViewportSize() {
            if (list.size() == 0)
                return super.getPreferredSize();
            Dimension d = super.getPreferredSize();
            d.height = list.get(0).getPreferredSize().height * VISIBLE_ROWS;
            d.width = Math.max(resultListPanel.getPreferredSize().width, d.width);
            return d;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return resultListPanel.getPreferredScrollableViewportSize().height;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 15; // FIXME should be height of one ProfileRowComponent
        }
        
        public void itemDeselected(SelectionEvent e) {
            if (ignoreSelectionEvents){
                return;
            }
            ignoreSelectionEvents = true;
            
            hasProfileSelected = false;
            for (ProfileRowComponent row : showingRows) {
                if (row.isSelected()) {
                    hasProfileSelected = true;
                    break;
                }
            }
            updateSelection();
            
            ignoreSelectionEvents = false;
        }

        public void itemSelected(SelectionEvent e) {
            if (ignoreSelectionEvents){
                return;
            }
            ignoreSelectionEvents = true;
            hasProfileSelected = true;
            ProfileRowComponent selectedRow = (ProfileRowComponent) e.getSource();
            if (e.getMultiselectType() == SelectionEvent.SINGLE_SELECT) {
                lastSelectedRow = selectedRow;
                for (ProfileRowComponent row : list) {
                    if (row != selectedRow) {
                        row.setSelected(false,SelectionEvent.SINGLE_SELECT);
                    }
                }
            } else if (e.getMultiselectType() == SelectionEvent.CTRL_MULTISELECT) {
                lastSelectedRow = selectedRow;
            } else if (e.getMultiselectType() == SelectionEvent.SHIFT_MULTISELECT) {
                int lastSelectedRowIndex = showingRows.indexOf(lastSelectedRow);
                int selectedRowIndex = showingRows.indexOf(selectedRow);
                int start = Math.min(lastSelectedRowIndex, selectedRowIndex);
                int end = Math.max(lastSelectedRowIndex, selectedRowIndex);
                for (int i = 0; i < showingRows.size(); i++) {
                    if (i < start || i > end) {
                        showingRows.get(i).setSelected(false, SelectionEvent.SINGLE_SELECT);
                    } else {
                        showingRows.get(i).setSelected(true, SelectionEvent.SINGLE_SELECT);
                    }
                }
                showingRows.get(selectedRowIndex).setSelected(true, SelectionEvent.SINGLE_SELECT);
            }
            updateSelection();
            ignoreSelectionEvents = false;
        }
    }
        
    private class TableProfileNameComparator implements Comparator<ProfileRowComponent> {

        public int compare(ProfileRowComponent o1, ProfileRowComponent o2) {
            TableProfileResult tpr1 = o1.getResult();
            TableProfileResult tpr2 = o2.getResult();
            
            int result;
            result = tpr1.getProfiledObject().getName().compareTo(tpr2.getProfiledObject().getName());
            if (result != 0) return result;
            
            if (tpr1.getCreateStartTime() < tpr2.getCreateStartTime()) return -1;
            if (tpr1.getCreateStartTime() > tpr2.getCreateStartTime()) return 1;
            return 0;
        }
        
    }

    private class TableProfileDateComparator implements Comparator<ProfileRowComponent> {

        public int compare(ProfileRowComponent o1, ProfileRowComponent o2) {
            TableProfileResult tpr1 = o1.getResult();
            TableProfileResult tpr2 = o2.getResult();
            
            if (tpr1.getCreateStartTime() < tpr2.getCreateStartTime()) return -1;
            if (tpr1.getCreateStartTime() > tpr2.getCreateStartTime()) return 1;

            int result;
            result = tpr1.getProfiledObject().getName().compareTo(tpr2.getProfiledObject().getName());
            return result;
        }
        
    }
    
    
    // A listener to detect page up/down request
    private class PageListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            if (scrollPane != null && resultListPanel != null) {
                if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    JScrollBar sb = scrollPane.getVerticalScrollBar();
                    sb.setValue(sb.getValue() + resultListPanel.getPreferredScrollableViewportSize().height);
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    JScrollBar sb = scrollPane.getVerticalScrollBar();
                    sb.setValue(sb.getValue() - resultListPanel.getPreferredScrollableViewportSize().height);
                }
            }
        }
        public void keyTyped(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
    }
    
    /**
     * This action will open a viewer for all of the currently selected profiles.
     */
    private final Action viewSelectedAction = new AbstractAction(Messages.getString("ProfileManagerView.viewSelectedActionName")) { //$NON-NLS-1$

        public void actionPerformed(ActionEvent e) {
            ProfileResultsViewer profileResultsViewer = 
                new ProfileResultsViewer(pm);
            profileResultsViewer.clearScanList();
            for (ProfileRowComponent rowComp : showingRows) {
                if (rowComp.isSelected()) {
                    TableProfileResult result = rowComp.getResult();
                    profileResultsViewer.addTableProfileResultToScan(result);
                    profileResultsViewer.addTableProfileResult(result);
                }
            }
            profileResultsViewer.getDialog().setVisible(true);           
        }            
    };

    private Action viewAllAction = new AbstractAction(Messages.getString("ProfileManagerView.viewAllActionName")) { //$NON-NLS-1$
        public void actionPerformed(ActionEvent e) {
            ProfileResultsViewer profileResultsViewer = 
                new ProfileResultsViewer(pm);
            profileResultsViewer.clearScanList();
            for (ProfileRowComponent rowComp : showingRows) {
                TableProfileResult result = rowComp.getResult();
                profileResultsViewer.addTableProfileResultToScan(result);
                profileResultsViewer.addTableProfileResult(result);
            }
            profileResultsViewer.getDialog().setVisible(true);
        }           
    };
    
    private Action deleteAllAction = new AbstractAction(Messages.getString("ProfileManagerView.deleteAllActionName")) { //$NON-NLS-1$
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(scrollPane,
                    Messages.getString("ProfileManagerView.confirmDeleteProfileData"), //$NON-NLS-1$
                    Messages.getString("ProfileManagerView.deleteAllButton") , JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
            if (confirm == 0) { // 0 == the first Option, which is Yes
                resultListPanel.removeAll();
                list.clear();
                showingRows.clear();
                pm.clear();
                resultListPanel.revalidate();
            }
        }
    };
    
    public ProfileManagerView(final ProfileManager pm) {
        super();
        this.pm = pm;
        pageListener = new PageListener();
        addKeyListener(pageListener);
        
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);
        
        searchText = new SearchTextField(this, 10);
        searchText.getTextField().addKeyListener(pageListener);
        topPanel.add(searchText.getPanel());

        JButton clearSearchButton = new JButton(Messages.getString("ProfileManagerView.clearSearch")); //$NON-NLS-1$
        clearSearchButton.addKeyListener(pageListener);
        clearSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchText.clear();
            }
        });
        topPanel.add(clearSearchButton);
        
        comparator = new TableProfileNameComparator();
        
        JLabel orderByLabel = new JLabel(Messages.getString("ProfileManagerView.orderBy")); //$NON-NLS-1$
        topPanel.add(orderByLabel);
        final JRadioButton nameRadioButton = new JRadioButton(Messages.getString("ProfileManagerView.nameOption")); //$NON-NLS-1$
        topPanel.add(nameRadioButton);
        nameRadioButton.addKeyListener(pageListener);
        final JRadioButton dateRadioButton = new JRadioButton(Messages.getString("ProfileManagerView.dateOption")); //$NON-NLS-1$
        topPanel.add(dateRadioButton);
        dateRadioButton.addKeyListener(pageListener);
        ActionListener radioListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nameRadioButton.isSelected()) {
                    setComparator(new TableProfileNameComparator());
                } else {
                    setComparator(new TableProfileDateComparator());
                }
            }            
        };
        nameRadioButton.addActionListener(radioListener);
        dateRadioButton.addActionListener(radioListener);
        ButtonGroup group = new ButtonGroup();
        group.add(nameRadioButton);
        group.add(dateRadioButton);
        nameRadioButton.setSelected(true);
        scrollPane = new JScrollPane();
        resultListPanel = new ResultListPanel(scrollPane);
        scrollPane.setViewportView(resultListPanel);
        resultListPanel.setFocusable(true);
        resultListPanel.addKeyListener(pageListener);
        resultListPanel.setBackground(UIManager.getColor("List.background")); //$NON-NLS-1$
        resultListPanel.setLayout(new GridLayout(0, 1));

        // populate this panel with MyRowComponents
        logger.debug("Populating profile manager view from profile manager " + System.identityHashCode(pm)); //$NON-NLS-1$
        for (TableProfileResult result : pm.getResults()) {
            ProfileRowComponent myRowComponent = new ProfileRowComponent(result, pm);
            myRowComponent.addProfileChangeListener(this);
            list.add(myRowComponent);
            myRowComponent.addSelectionListener(resultListPanel);
            resultListPanel.add(myRowComponent);
            showingRows.add(myRowComponent);
        }

        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getViewport().setFocusable(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);
 
        bottomPanel.add(new JButton(viewAllAction));
        
        bottomPanel.add(new JButton(viewSelectedAction));
        
        statusText = new JLabel();
        updateStatus();
        bottomPanel.add(statusText);
        bottomPanel.add(new JButton(deleteAllAction));

        JButton closeButton = new JButton(Messages.getString("ProfileManagerView.closeButton")); //$NON-NLS-1$
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Component c = ProfileManagerView.this.getParent();
                while  (c != null && !(c instanceof Window)) {
                    c = c.getParent();
                }
                if (c != null) {
                    c.setVisible(false);
                }
            }
        });
        bottomPanel.add(closeButton);
    }

    private void updateStatus() {
        int totalNumber = list.size();
        int numberShowing = showingRows.size();
        viewAllAction.setEnabled(numberShowing != 0);
        deleteAllAction.setEnabled(totalNumber != 0);
        updateSelection();
        statusText.setText(String.format(Messages.getString("ProfileManagerView.profileDisplayStatus"),  //$NON-NLS-1$
                                            numberShowing, 
                                            totalNumber));
    }
    
    /**
     * Called when the resultListPanel selection change.
     */
    private void updateSelection() {
        viewSelectedAction.setEnabled(hasProfileSelected);
    }
    
    private void updateResultListPanel() {
        resultListPanel.removeAll();
        List<TableProfileResult> tableProfileResults = new ArrayList<TableProfileResult>();
        hasProfileSelected = false;
        for (ProfileRowComponent r : showingRows) {
            resultListPanel.add(r);
            tableProfileResults.add(r.getResult());
            if( !hasProfileSelected && r.isSelected() )
                hasProfileSelected = true;
        }
        pm.setProcessingOrder(tableProfileResults);
        resultListPanel.revalidate();
        updateSelection();
        logger.debug("Showing rows has contents " + showingRows);
    }
    
    /**
     * Search the list for profiles matching the given string.
     * XXX match on date fields too??
     */
    public void doSearch(Pattern p, boolean matchExactly) {
        lastSearchPattern = p;
        lastMatchExactValue = matchExactly;
        showingRows.clear();
        if (p == null || p.pattern() == null || p.pattern().length() == 0) {
            for (ProfileRowComponent r : list) {
                showingRows.add(r);
            }
        } else {
            for (ProfileRowComponent r : list) {
                if (matchExactly && p.matcher(r.getResult().getProfiledObject().getName()).matches()) {
                    showingRows.add(r);
                } else if (!matchExactly && p.matcher(r.getResult().getProfiledObject().getName()).find()) {
                    showingRows.add(r);
                }
            }
        }
        Collections.sort(showingRows, comparator);
        updateResultListPanel();
        updateStatus();
    }
    
    private void setComparator(Comparator<ProfileRowComponent> comparator) {
        this.comparator = comparator;
        doSearch(lastSearchPattern, lastMatchExactValue);
    }

    
    /** Part of the ProfileChangeListener interface; called
     * to tell us when a ProfileResult has been added; we need
     * to create a corresponding ProfileRowComponent and add it to the view.
     */
    public void profilesAdded(ProfileChangeEvent e) {
        logger.debug("ProfileManagerView.profileAdded(): table profile added"); //$NON-NLS-1$
        List<ProfileResult> profileResult = new ArrayList<ProfileResult>(e.getProfileResults());
        List<TableProfileResult> tpr = new ArrayList<TableProfileResult>();
        for (ProfileResult pr : profileResult) {
            ProfileRowComponent myRowComponent;
            if ( pr instanceof TableProfileResult){
                myRowComponent = new ProfileRowComponent((TableProfileResult)pr, pm);
                myRowComponent.addSelectionListener(resultListPanel);
                myRowComponent.addProfileChangeListener(this);
                list.add(myRowComponent);
                tpr.add((TableProfileResult) pr);
                pm.setProcessingOrder(tpr);
            } else {
                logger.debug("Cannot create a component based on the profile result " + pr); //$NON-NLS-1$
            }
        }
        doSearch(lastSearchPattern, lastMatchExactValue);
    }

    /** Part of the ProfileChangeListener interface; called
     * to tell us when a ProfileResult has been removed; we need
     * to find and remove the corresponding ProfileRowComponent.
     */
    public void profilesRemoved(ProfileChangeEvent e) {
        List<ProfileResult> profileResults = e.getProfileResults();
        logger.debug("ProfileManagerView.profileRemoved(): " + profileResults + ": profiles deleted"); //$NON-NLS-1$ //$NON-NLS-2$
        for (ProfileResult profileResult: profileResults) {
            for (ProfileRowComponent view : list) {
                if (view.getResult().equals(profileResult)) {
                    list.remove(view);
                    view.removeSelectionListener(resultListPanel);
                    break;
                }
            }
        }
        doSearch(lastSearchPattern, lastMatchExactValue);
    }
    
    public void profileListChanged(ProfileChangeEvent e) {
        Runnable runner = new Runnable() {
            public void run() {
                sort();
            }
        };
        try {
            SwingUtilities.invokeLater(runner);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    

    /**
     * Sorts the list either alphabetically or chronologically.
     */
    private void sort() {
        Collections.sort(showingRows, comparator);
        updateStatus();
        updateResultListPanel();
    }
}
