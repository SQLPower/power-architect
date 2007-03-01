package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.profile.ProfileManagerInterface;
import ca.sqlpower.architect.profile.TableProfileResult;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A component that displays the status and either rowcount or progressbar
 * for the given table profile
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

    final JButton reProfileButton, stopButton, deleteButton;
    private ProfileManagerInterface pm;

    public ProfileRowComponent(final TableProfileResult result, final ProfileManagerInterface pm) {
        super();
        this.result = result;
        this.pm = pm;
        setBorder(BorderFactory.createEtchedBorder());
        FormLayout layout = new FormLayout(
           "l:p, 2dlu, l:max(50dlu;p), 2dlu, max(50dlu;p), 2dlu, p, 2dlu, p",
           "p, p");
        PanelBuilder builder = new PanelBuilder(layout, this);
        CellConstraints cc = new CellConstraints();

        final int ICON_COL = 1, TEXT_COL=3, PROGRESS_COL = 5, RELOAD_COL = 7, KILL_COL = 9;
        builder.add(new JLabel(tableIcon), cc.xywh(ICON_COL, 1, 1, 2));
        this.reProfileButton = new JButton(refreshIcon);
        reProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("REFRESH");
            }
        });
        this.stopButton = new JButton(stopIcon);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("STOP");
            }
        });
        this.deleteButton = new JButton(deleteIcon);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("ProfileRowComponent DELETE");
                try {
                    pm.remove(result);
                } catch (ArchitectException e1) {
                    JOptionPane.showMessageDialog(null,
                        "Error trying to delete: " + e1);
                }
            }
        });
        builder.add(new JLabel(result.getProfiledObject().getName()), cc.xy(TEXT_COL, 1));
        builder.add(reProfileButton, cc.xywh(RELOAD_COL, 1, 1, 2));
        builder.add(new JProgressBar(), cc.xywh(PROGRESS_COL, 1, 3, 2));
        builder.add(new JLabel("Today 24 rows 1.4 sec"), cc.xy(TEXT_COL, 2));
        if (result.isFinished()) {
            builder.add(deleteButton, cc.xywh(KILL_COL, 1, 1, 2));
        } else {
            builder.add(stopButton, cc.xywh(KILL_COL, 1, 1, 2));
        }
    }

    public TableProfileResult getResult() {
        return result;
    }
}
