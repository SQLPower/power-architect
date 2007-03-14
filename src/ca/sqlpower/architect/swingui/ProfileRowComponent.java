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
import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.event.TaskTerminationEvent;
import ca.sqlpower.architect.swingui.event.TaskTerminationListener;

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
    private static ImageIcon tableIcon;
    /** The Stop Sign icon for all the rows (shared) */
    private static ImageIcon stopIcon;
    /** The reload icon for all the rows (shared) */
    private static ImageIcon refreshIcon;
    /** shared delete icon */
    private static ImageIcon deleteIcon;
    
    /* Bogus numbers filled in just to have a feel of how big
     * the status label should be when creating the dialog */
    final JLabel statusLabel = new JLabel(String.format(TableProfileResult.TOSTRING_FORMAT, 500, "Mar 9, 2007", 15000));

    static {
        tableIcon = ASUtils.createIcon("Table", "Table Result", 16);

        refreshIcon = ASUtils.createJLFIcon("general/Refresh", "Re-Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));

        stopIcon = ASUtils.createJLFIcon("general/Stop", "Stop Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));

        deleteIcon = ASUtils.createJLFIcon("general/Delete", "Delete Profile", ArchitectFrame.getMainInstance().getSprefs()
                .getInt(SwingUserSettings.ICON_SIZE, 24));
    }

    final TableProfileResult result;

    final JButton reProfileButton, cancelButton, deleteButton;
    private ProfileManager pm;


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
            // TODO Auto-generated method stub

        }

    }

    private class ProfileRowMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            Object obj = evt.getSource();
            if (evt.getButton() == MouseEvent.BUTTON1) {
                if (evt.getClickCount() == 2) {
                    if (getResult().isFinished() && 
                                    !getResult().isCancelled() &&
                                    !(obj instanceof JButton)) {
                        ProfileResultsViewer profileResultsViewer = 
                            new ProfileResultsViewer((TableProfileManager) pm);
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
    
    private class ResultTaskTerminationListener implements TaskTerminationListener {

        public void taskFinished(TaskTerminationEvent e) {
            reProfileButton.setVisible(true);
            ProfileRowComponent.this.remove(cancelButton);
            ProfileRowComponent.this.add(deleteButton, ComponentType.DELETE);
            if (!result.isCancelled()) {
                statusLabel.setVisible(true);
                statusLabel.setText(result.toString());
            }
        }           
    }

    public ProfileRowComponent(final TableProfileResult result, final ProfileManager pm) {
        super(new RowComponentLayout(5, 5));
        this.result = result;
        this.pm = pm;
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        final JProgressBar progressBar = new JProgressBar();
        setBackground(Color.WHITE);

        add(new JLabel(tableIcon), ComponentType.ICON);
        this.reProfileButton = new JButton(refreshIcon);
        reProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (result.isCancelled()) {
                    result.setCancelled(false);
                    progressBar.setVisible(true);
                    ProgressWatcher watcher = new ProgressWatcher(progressBar, result);
                    add(progressBar, ComponentType.PROGRESS_BAR);
                    revalidate();
                    watcher.addTaskTerminationListener(new ResultTaskTerminationListener());
                    result.populate();
                }
                System.out.println("REFRESH");
            }
        });
        this.cancelButton = new JButton(stopIcon);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                result.setCancelled(true);
                statusLabel.setVisible(true);
                statusLabel.setText("Cancelled");
                ProfileRowComponent.this.remove(cancelButton);
                ProfileRowComponent.this.add(deleteButton, ComponentType.DELETE);
                progressBar.setVisible(false);
                reProfileButton.setVisible(true);
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
        add(new JLabel(result.getProfiledObject().getName()), ComponentType.TABLE_NAME);
        add(reProfileButton, ComponentType.RELOAD);
        reProfileButton.setVisible(false);
        
        ProgressWatcher watcher = new ProgressWatcher(progressBar, result);
        add(progressBar, ComponentType.PROGRESS_BAR);
        watcher.addTaskTerminationListener(new ResultTaskTerminationListener());
        statusLabel.setVisible(false);
        add(statusLabel, ComponentType.TABLE_INFO);      
        add(cancelButton, ComponentType.CANCEL);  
        this.addMouseListener(new ProfileRowMouseListener());
    }

    public TableProfileResult getResult() {
        return result;
    }
    
    boolean selected = false;
    List<SelectionListener> listeners = new ArrayList<SelectionListener>();
    
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
