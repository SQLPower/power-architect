package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
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

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileChangeListener;
import ca.sqlpower.architect.profile.ProfileManagerInterface;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;

/**
 * The main view for the Profile Manager. Vaguely patterned on e.g., the
 * Safari Download box; has some controls at the top and bottom,
 * and a row of components that displays, for each table profile result,
 * the name of the table AND either:
 * <ul><li>the date, rows, elapsed time, and a Re-Profile button, or,
 * <li>a progressbar;
 * </ul>Always followed by an X button, which either deletes the profile
 * if it's finished, or stops the profiling if it's active.
 * <p>
 * This code is incomplete, and still needs the following:
 * - TODO make sorting work! (maintain separate list??)
 * - TODO listeners for the progress bars, etc.!
 * ...
 */
public class ProfileManagerView extends JPanel implements ProfileChangeListener  {

    private static Logger logger = Logger.getLogger(ProfileManagerView.class);

	ProfileManagerInterface pm;
	final static int NICE_ROWS = 6;

    final JPanel resultListPanel;

    final JLabel statusText;

    List<ProfileRowComponent> list = new ArrayList<ProfileRowComponent>();

    public ProfileManagerView(ProfileManagerInterface pm) {
        super();
        this.pm = pm;

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);

        topPanel.add(new JLabel("Search"));
        final JTextField searchText = new JTextField(10);
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
        JLabel orderByLabel = new JLabel("Order by");
        topPanel.add(orderByLabel);
        orderByLabel.setEnabled(false);
        JRadioButton nameRadioButton = new JRadioButton("Name");
        nameRadioButton.setEnabled(false);
        topPanel.add(nameRadioButton);
        JRadioButton dateRadioButton = new JRadioButton("Date");
        dateRadioButton.setEnabled(false);
        topPanel.add(dateRadioButton);
        ButtonGroup group = new ButtonGroup();
        group.add(nameRadioButton);
        group.add(dateRadioButton);
        nameRadioButton.setSelected(true);

        resultListPanel = new JPanel();
        BoxLayout listLayout = new BoxLayout(resultListPanel, BoxLayout.PAGE_AXIS);
        resultListPanel.setLayout(listLayout);
        // populate this panel with MyRowComponents
        for (TableProfileResult result : pm.getTableResults()) {
            ProfileRowComponent myRowComponent = new ProfileRowComponent(result);
            list.add(myRowComponent);
            resultListPanel.add(myRowComponent);
        }

        JScrollPane scrollPane = new JScrollPane(resultListPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        Action deleteAllAction = new AbstractAction("Delete All") {
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(ArchitectFrame.getMainInstance(),
                        "Are you sure you want to delete all your profile data?\n" +
                        "(this cannot be undone)");
                if (confirm == 0) { // 0 == the first Option, which is Yes
                    for (ProfileRowComponent r : list) {
                        // r.setVisible(false);
                        System.out.println("XXX REMOVE FROM PM"); // XXX
                        resultListPanel.remove(r);
                    }
                    resultListPanel.invalidate();
                    list.clear();
                    System.out.println("All gone!");
                }
            }
        };
        bottomPanel.add(new JButton(deleteAllAction));
        statusText = new JLabel(getStatus());
        bottomPanel.add(statusText);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        bottomPanel.add(closeButton);
    }

    private String getStatus() {
        // XXX compute this dynamically when list management is done
        int numberShowing = list.size();
        return String.format("Showing %d of %d Profiles", numberShowing, list.size());
    }
    /**
     * Search the list for profiles matching the given string.
     * XXX match on date fields too??
     */
    protected void doSearch(final String text) {
        if (text == null || text.length() == 0) {
            for (ProfileRowComponent r : list) {
                r.setVisible(true);
            }
        } else {
            String searchText = text.toLowerCase();
            for (ProfileRowComponent r : list) {
                r.setVisible(r.getResult().getProfiledObject().getName().toLowerCase().contains(searchText));
            }
        }
        resultListPanel.invalidate();
        statusText.setText(getStatus());
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

    public void profileAdded(ProfileChangeEvent e) {
        ProfileResult profileResult = e.getProfileResult();
        System.out.println("Need to write code to add " + profileResult);
    }

    public void profileRemoved(ProfileChangeEvent e) {
        ProfileResult profileResult = e.getProfileResult();
        System.out.println("Need to write code to remove " + profileResult);

    }

    public void profileListChanged(ProfileChangeEvent e) {
        // TODO Auto-generated method stub

    }
}
