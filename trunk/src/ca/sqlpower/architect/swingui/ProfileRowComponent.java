package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.profile.TableProfileResult;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

    final JButton reProfileButton, stopButton, deleteButton;

    public ProfileRowComponent(TableProfileResult result) {
        super();
        setBorder(BorderFactory.createEtchedBorder());
        FormLayout layout = new FormLayout(
           "l:p, 2dlu, l:max(150dlu;p), 2dlu, p, 2dlu, p",
           "p, p");
        PanelBuilder builder = new PanelBuilder(layout, this);
        CellConstraints cc = new CellConstraints();

        final int ICON_COL = 1, MID_COL=3, RELOAD_COL = 5, KILL_COL = 7;
        builder.add(new JLabel(tableIcon), cc.xywh(ICON_COL, 1, 1, 2));
        this.result = result;
        this.reProfileButton = new JButton(refreshIcon);
        this.stopButton = new JButton(stopIcon);
        this.deleteButton = new JButton(deleteIcon);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("DELETE");
            }
        });
        builder.add(new JLabel(result.getProfiledObject().getName()), cc.xy(MID_COL, 1));
        builder.add(reProfileButton, cc.xywh(RELOAD_COL, 1, 1, 2));
        builder.add(deleteButton, cc.xywh(KILL_COL, 1, 1, 2));

        builder.add(new JLabel("Today 24 rows 1.4 sec"), cc.xy(MID_COL, 2));
    }

    public TableProfileResult getResult() {
        return result;
    }
}
