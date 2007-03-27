package ca.sqlpower.architect.swingui;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DataSourceCollection;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ArchitectDataSourceTypeEditor implements ArchitectPanel {
    
    private static final Logger logger = Logger.getLogger(ArchitectDataSourceTypeEditor.class);
    
    /**
     * The panel that this editor's GUI lives in.
     */
    private final JPanel panel;
    
    private final DataSourceCollection dataSourceCollection;
    
    /**
     * The list of data source types.
     */
    private final JList dsTypeList;
    
    /**
     * The panel that edits the currently-selected data source type.
     */
    private final ArchitectDataSourceTypePanel dsTypePanel;
    
    /**
     * The panel that maintains the currently-selected data source type's
     * classpath.
     */
    private final JDBCDriverPanel jdbcPanel;
    
    public ArchitectDataSourceTypeEditor(DataSourceCollection dataSourceCollection) {
        this.dataSourceCollection = dataSourceCollection;
        
        dsTypeList = new JList(dataSourceCollection.getDataSourceTypes().toArray());
        dsTypeList.setCellRenderer(new ArchitectDataSourceTypeListCellRenderer());
        
        dsTypePanel = new ArchitectDataSourceTypePanel();
        
        jdbcPanel = new JDBCDriverPanel();
        
        dsTypeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                try {
                    ArchitectDataSourceType dst =
                        (ArchitectDataSourceType) dsTypeList.getSelectedValue();
                    switchToDsType(dst);
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialogNoReport(
                            "Can't edit this database type due to an unexpected error", ex);
                }
            }
        });
        panel = createPanel();
    }
    
    /**
     * Creates the panel layout. Requires that the GUI components have already
     * been created. Does not fill in any values into the components. See
     * {@link #switchToDsType()} for that.
     */
    private JPanel createPanel() {
        FormLayout layout = new FormLayout("60dlu, 6dlu, pref:grow", "pref, 6dlu, pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.setDefaultDialogBorder();
        
        fb.add(new JScrollPane(dsTypeList), "1, 1, 1, 3");
        fb.add(dsTypePanel.getPanel(),      "3, 1");
        fb.add(jdbcPanel,                   "3, 3");
        
        return fb.getPanel();
    }
    
    /**
     * Returns this editor's GUI.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Copies all the data source types and their properties back to the
     * DataSourceCollection we're editing.
     */
    public boolean applyChanges() {
        logger.debug("Applying changes to all data source types");
        applyCurrentChanges();
        ListModel lm = dsTypeList.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            ArchitectDataSourceType dst = (ArchitectDataSourceType) lm.getElementAt(i);
            dataSourceCollection.mergeDataSourceType(dst);
        }
        return true;
    }

    /**
     * This method is a no-op implementation, since all we have to do to discard the
     * changes is not copy them back to the model.
     */
    public void discardChanges() {
        // nothing to do
    }
    
    /**
     * Causes this editor to set up all its GUI components to edit the given data source type.
     * Null is an acceptable value, and means to make no DS Type the current type.
     */
    public void switchToDsType(ArchitectDataSourceType dst) throws ArchitectException {
        applyCurrentChanges();
        dsTypeList.setSelectedValue(dst, true);
        dsTypePanel.editDsType(dst);
        jdbcPanel.editDsType(dst);
    }
    
    private void applyCurrentChanges() {
        dsTypePanel.applyChanges();
        jdbcPanel.applyChanges();
    }
}
