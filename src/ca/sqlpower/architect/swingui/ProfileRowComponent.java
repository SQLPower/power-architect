package ca.sqlpower.architect.swingui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.profile.TableProfileResult;

/**
 * A component that displays the status and either rowcount or progressbar
 * for the given table profile
 * TODO JGoodies Forms for most of the layout
 */
public class ProfileRowComponent extends JPanel {

    /** The icon for all the rows (shared) */
    private static ImageIcon tableIcon;
    /** The Stop Sign icon for all the rows (shared) */
    private static ImageIcon stopIcon;
    /** The reload icon for all the rows (shared) */
    private static ImageIcon refreshIcon;
    /** shared delete icon */
    private static ImageIcon deleteIcon;

    static {
        tableIcon = ASUtils.createJLFIcon("general/Save", "DB Table", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));

        refreshIcon = ASUtils.createJLFIcon("general/Refresh", "Re-Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));

        stopIcon = ASUtils.createJLFIcon("general/Stop", "Stop Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));

        deleteIcon = ASUtils.createJLFIcon("general/Delete", "Delete Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));
    }

    final TableProfileResult result;

    JButton reProfileButton, stopButton, reloadButton, deleteButton;

    public ProfileRowComponent(TableProfileResult result) {
        super();
        add(new JLabel(tableIcon));
        this.result = result;
        this.reProfileButton = new JButton(refreshIcon);
        this.stopButton = new JButton(stopIcon);
        this.deleteButton = new JButton(deleteIcon);
        add(new JLabel(result.getProfiledObject().getName()));
        add(reProfileButton);
        add(stopButton);
        add(deleteButton);
    }

    public TableProfileResult getResult() {
        return result;
    }
}
