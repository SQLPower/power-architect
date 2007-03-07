package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.event.TaskTerminationEvent;
import ca.sqlpower.architect.swingui.event.TaskTerminationListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A component that displays the status and either rowcount or progressbar
 * for the given table profile. Typical use is as one Row in the
 * ca.sqlpower.architect.swingui.ProfileManagerView, but has
 * no dependencies thereon.
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
    final JLabel statusLabel = new JLabel("");
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
    private ProfileManager pm;

    private class ProfileRowMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getClickCount() == 2) {
                if (getResult().isFinished() && !(obj instanceof JButton)) {
                    Collection<TableProfileResult> results = new ArrayList<TableProfileResult>();
                    results.add(result);
                    ProfileResultsViewer profileResultsViewer = ArchitectFrame.getMainInstance().getProject().getProfileResultsViewer();
                    // new ProfileResultsViewer((TableProfileManager) pm);
                    profileResultsViewer.addTableProfileResult(result);
                    profileResultsViewer.getDialog().setVisible(true);
                }
            }
        }
    }   
    
    public ProfileRowComponent(final TableProfileResult result, final ProfileManager pm) {
        super();
        this.result = result;
        this.pm = pm;
        setBorder(BorderFactory.createEtchedBorder());
        FormLayout layout = new FormLayout(
           "l:p, 2dlu, l:max(50dlu;p), 2dlu, max(50dlu;p), 2dlu, p, 2dlu, p",
           "p:none, p:none");
        final PanelBuilder builder = new PanelBuilder(layout, this);
        final CellConstraints cc = new CellConstraints();

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
                System.out.println("ProfileRowComponent: DELETE object: " + result);
                pm.removeProfile(result);
            }
        });
        builder.add(new JLabel(result.getProfiledObject().getName()), cc.xy(TEXT_COL, 1));
        builder.add(reProfileButton, cc.xywh(RELOAD_COL, 1, 1, 2));
   
        JProgressBar progressBar = new JProgressBar();
        ProgressWatcher watcher = new ProgressWatcher(progressBar, result);
        builder.add(progressBar, cc.xywh(PROGRESS_COL, 1, 1, 2));
        watcher.addTaskTerminationListener(new TaskTerminationListener() {

            public void taskFinished(TaskTerminationEvent e) {
                // TODO show or enable Refresh button
                // TODO maybe hide progressBar
                // replace stop button with delete button 
                ProfileRowComponent.this.remove(stopButton);
                builder.add(deleteButton, cc.xywh(KILL_COL, 1, 1, 2));                
                statusLabel.setText(result.toString());
            }           
        });
        
        
        builder.add(statusLabel, cc.xy(TEXT_COL, 2));      
        builder.add(stopButton, cc.xywh(KILL_COL, 1, 1, 2));  
        this.addMouseListener(new ProfileRowMouseListener());
    }

    public TableProfileResult getResult() {
        return result;
    }

}
