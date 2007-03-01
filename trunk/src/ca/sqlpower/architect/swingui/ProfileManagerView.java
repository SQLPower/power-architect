package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.profile.ProfileManagerInterface;
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
 * - TODO JGoodies Forms for most of the layouts!!
 * - TODO make sorting work! (maintain separare list??)
 * - TODO listeners for the progress bars, for list change events, etc., etc.!
 * ...
 */
public class ProfileManagerView extends JPanel {

    private static Logger logger = Logger.getLogger(ProfileManagerView.class);

	ProfileManagerInterface pm;

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
        topPanel.add(new JLabel("Order by"));
        JRadioButton nameRadioButton = new JRadioButton("Name");
        topPanel.add(nameRadioButton);
        JRadioButton dateRadioButton = new JRadioButton("Date");
        topPanel.add(dateRadioButton);
        ButtonGroup group = new ButtonGroup();
        group.add(nameRadioButton);
        group.add(dateRadioButton);
        nameRadioButton.setSelected(true);

        final JPanel resultListPanel = new JPanel();
        BoxLayout listLayout = new BoxLayout(resultListPanel, BoxLayout.PAGE_AXIS);
        resultListPanel.setLayout(listLayout);
        // populate this panel with MyRowComponents
        for (TableProfileResult result : pm.getTableResults()) {
            ProfileRowComponent myRowComponent = new ProfileRowComponent(result);
            list.add(myRowComponent);
            resultListPanel.add(myRowComponent);
        }
        add(new JScrollPane(resultListPanel), BorderLayout.CENTER);

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
        JLabel statusText = new JLabel("2 profiles");
        bottomPanel.add(statusText);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        bottomPanel.add(closeButton);
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
            return;
        }
        String searchText = text.toLowerCase();
        for (ProfileRowComponent r : list) {
            r.setVisible(r.getResult().getProfiledObject().getName().toLowerCase().contains(searchText));
        }
    }

}
