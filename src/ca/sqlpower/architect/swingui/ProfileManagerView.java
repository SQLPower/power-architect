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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileChangeListener;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;

/**
 * The controlling view for the Profile Manager. Vaguely patterned on e.g., the
 * Safari Download box; has some controls at the top and bottom,
 * and a row of components that displays, for each table profile result,
 * the name of the table AND either:
 * <ul><li>the date, rows, elapsed time, and a Re-Profile button, 
 * and a "delete this profile" button
 * <li>a progressbar and a Stop button.
 * <p>
 * TODO make sorting work! (maintain separate list??)
 */
public class ProfileManagerView extends JPanel implements ProfileChangeListener  {
    
    private static Logger logger = Logger.getLogger(ProfileManagerView.class);

	ProfileManager pm;
	final static int NICE_ROWS = 8;

    final ResultListPanel resultListPanel;

    final JScrollPane scrollPane;

    final JLabel statusText;

    final JTextField searchText;
    
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
    List<ProfileRowComponent> showingRows = new ArrayList<ProfileRowComponent>();

    private class ResultListPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            // TODO Auto-generated method stub
            return 0;
        }

        public boolean getScrollableTracksViewportHeight() {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 15; // FIXME should be height of one ProfileRowComponent
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

    public ProfileManagerView(final ProfileManager pm) {
        super();
        this.pm = pm;

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);

        topPanel.add(new JLabel("Search"));
        searchText = new JTextField(10);
        searchText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                // nothing
            }

            public void keyReleased(KeyEvent e) {
                doSearch(searchText.getText());
            }

            public void keyTyped(KeyEvent e) {
                // nothing
            }
        });
        topPanel.add(searchText);
        ImageIcon clearSearchIcon = ASUtils.createJLFIcon("general/Delete", "Clear Search", ArchitectFrame.getMainInstance().getSprefs()
                    .getInt(SwingUserSettings.ICON_SIZE, 16));

        JButton clearSearchButton = new JButton(clearSearchIcon);
        clearSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchText.setText("");
                doSearch("");
            }
        });
        topPanel.add(clearSearchButton);
        
        comparator = new TableProfileNameComparator();
        JLabel orderByLabel = new JLabel("Order by");
        topPanel.add(orderByLabel);
        final JRadioButton nameRadioButton = new JRadioButton("Name");
        topPanel.add(nameRadioButton);
        final JRadioButton dateRadioButton = new JRadioButton("Date");
        topPanel.add(dateRadioButton);
        ActionListener radioListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(nameRadioButton.isSelected()) {
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
        resultListPanel = new ResultListPanel();
        resultListPanel.setBackground(Color.WHITE);
        resultListPanel.setLayout(new GridLayout(0, 1));
        // populate this panel with MyRowComponents
        for (TableProfileResult result : pm.getTableResults()) {
            ProfileRowComponent myRowComponent = new ProfileRowComponent(result, pm);
            list.add(myRowComponent);
            resultListPanel.add(myRowComponent);
            showingRows.add(myRowComponent);
        }

        scrollPane = new JScrollPane(resultListPanel);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);
 
        Action viewAllAction = new AbstractAction("View All") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("ProfileManagerView.inner.actionPerformed(): VIEW ALL"); 
                ProfileResultsViewer profileResultsViewer = 
                    ArchitectFrame.getMainInstance().getProject().getProfileResultsViewer();
                for (ProfileRowComponent rowComp : list) {
                    TableProfileResult result = rowComp.getResult();
                    System.out.println("ProfileManagerView.inner.actionPerformed(): add " + result);
                    profileResultsViewer.addTableProfileResult(result);
                }
                profileResultsViewer.getDialog().setVisible(true);
            }           
        };
        bottomPanel.add(new JButton(viewAllAction));
        
        Action deleteAllAction = new AbstractAction("Delete All") {
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(ArchitectFrame.getMainInstance(),
                        "Are you sure you want to delete all your profile data?\n" +
                        "(this cannot be undone)");
                if (confirm == 0) { // 0 == the first Option, which is Yes
                    resultListPanel.removeAll();
                    list.clear();
                    showingRows.clear();
                    pm.clear();
                    resultListPanel.revalidate();
                    System.out.println("All gone!");
                }
            }
        };
        bottomPanel.add(new JButton(deleteAllAction));
        statusText = new JLabel();
        updateStatus();
        bottomPanel.add(statusText);

        JButton closeButton = new JButton("Close");
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
        statusText.setText(String.format("Showing %d of %d Profiles", 
                                            numberShowing, 
                                            totalNumber));
    }
    
    private void updateResultListPanel() {
        
        resultListPanel.removeAll();
        for (ProfileRowComponent r : showingRows) {
            resultListPanel.add(r);
        }
        resultListPanel.revalidate();
    }
    
    /**
     * Search the list for profiles matching the given string.
     * XXX match on date fields too??
     */
    protected void doSearch(final String text) {
        showingRows.clear();
        if (text == null || text.length() == 0) {
            for (ProfileRowComponent r : list) {
                showingRows.add(r);
            }
        } else {
            String searchText = text.toLowerCase();
            for (ProfileRowComponent r : list) {
                if (r.getResult().getProfiledObject().getName().toLowerCase().contains(searchText)) {
                    showingRows.add(r);
                }
            }
        }
        Collections.sort(showingRows, comparator);
        updateResultListPanel();
        updateStatus();
    }
    
    private void setComparator(Comparator comparator) {
        this.comparator = comparator;
        doSearch(searchText.getText());
    }

    @Override
    public Dimension getPreferredSize() {
        if (list.size() == 0)
            return super.getPreferredSize();
        ProfileRowComponent x = list.get(0);
        Dimension d = x.getPreferredSize();
        d.height *= NICE_ROWS;
        return d;
    }

    /** Part of the ProfileChangeListener interface; called
     * to tell us when a ProfileResult has been added; we need
     * to create a corresponding ProfileRowComponent and add it to the view.
     */
    public void profileAdded(ProfileChangeEvent e) {
        System.out.println("ProfileManagerView.profileAdded(): table profile added");
        TableProfileResult profileResult = (TableProfileResult) e.getProfileResult();
        ProfileRowComponent myRowComponent = new ProfileRowComponent(profileResult, pm);
        list.add(myRowComponent);
        doSearch(searchText.getText());
    }

    /** Part of the ProfileChangeListener interface; called
     * to tell us when a ProfileResult has been removed; we need
     * to find and remove the corresponding ProfileRowComponent.
     */
    public void profileRemoved(ProfileChangeEvent e) {
        TableProfileResult profileResult = (TableProfileResult) e.getProfileResult();
        System.out.println("ProfileManagerView.profileAdded(): " + profileResult + ": profile deleted");
        for (ProfileRowComponent view : list) {
            if (view.getResult().equals(profileResult)) {
                list.remove(view);
                break;
            }
        }
        doSearch(searchText.getText());
    }

    public void profileListChanged(ProfileChangeEvent e) {
        logger.debug("ProfileChanged method not yet implemented.");
    }
    
    
}
