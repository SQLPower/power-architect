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

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider.PropertyType;
import ca.sqlpower.swingui.ChangeListeningDataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelChangeUtil;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * A DataEntryPanel implementation that is capable of modifying the properties
 * of one or more columns. The user interface is slightly different in multi-column
 * edit mode.
 */
public class ColumnEditPanel extends ChangeListeningDataEntryPanel implements ActionListener, SPListener {
    
    private static final Logger logger = Logger.getLogger(ColumnEditPanel.class);

    private static final Font TITLE_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 10f);

    /**
     * A simple enum that gives a nicer name to true and false for combo boxes.
     * <p>
     * Used in testing
     */
    static enum YesNoEnum {
        YES("yes", true),
        NO("no", false);
        
        private final String displayName;
        private final boolean value;

        private YesNoEnum(String displayName, boolean value) {
            this.displayName = displayName;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        public boolean getValue() {
            return value;
        }
        
        public static YesNoEnum valueOf(Boolean bool) {
            if (bool == null) {
                return null;
            } else if (bool) {
                return YES;
            } else {
                return NO;
            }

        }
    }

    /**
     * The column we're editing.
     */
    private final List<SQLColumn> columns;
    
    private final JPanel panel;

    /**
     * Mapping of data entry components to the checkboxes that say whether
     * or not the value should be applied.
     */
    private final Map<JComponent, JCheckBox> componentEnabledMap = new HashMap<JComponent, JCheckBox>();

    /**
     * Mapping of data entry components specific to data types to the check
     * boxes that say if the value in the column editor window should override
     * the value from the underlying type or if the underlying type should be
     * used instead.
     */
    private final Map<JComponent, JCheckBox> typeOverrideMap = new HashMap<JComponent, JCheckBox>();
    
    /**
     * Label that shows where the column was reverse engineered from, or
     * where its data comes from when building an ETL mapping.
     */
    private final JLabel sourceLabel;

    private final JTextField colLogicalName;
    
    private final JTextField colPhysicalName;

    private final JComboBox colType;

    private final JSpinner colScale;

    private final JSpinner colPrec;

    private final JComboBox colNullable;

    private final JTextArea colRemarks;

    private final JTextField colDefaultValue;

    private final JCheckBox colInPK;

    private final JComboBox colAutoInc;

    /**
     * Text field for the name of the sequence that will generate this column's
     * default values. In multi-edit mode, this component will be null. 
     */
    private final JTextField colAutoIncSequenceName;

    /**
     * The prefix string that comes before the current column name in the
     * sequence name. This is set via the {@link #discoverSequenceNamePattern()}
     * method, which should be called automatically whenever the user changes
     * the sequence name.
     */
    private String seqNamePrefix;

    /**
     * The suffix string that comes after the current column name in the
     * sequence name. This is set via the {@link #discoverSequenceNamePattern()}
     * method, which should be called automatically whenever the user changes
     * the sequence name.
     */
    private String seqNameSuffix;

    private final ArchitectSession session;

    public ColumnEditPanel(SQLColumn col, ArchitectSwingSession session) throws SQLObjectException {
        this(Collections.singleton(col), session);
    }
    
    public ColumnEditPanel(Collection<SQLColumn> cols, ArchitectSwingSession session) throws SQLObjectException {
        logger.debug("ColumnEditPanel called"); //$NON-NLS-1$

        if (session == null) {
            throw new NullPointerException("Null session is not allowed"); //$NON-NLS-1$
        }
        this.session = session;
        
        if (cols == null || cols.isEmpty()) {
            throw new NullPointerException("Null or empty collection of columns is not allowed"); //$NON-NLS-1$
        }
        columns = new ArrayList<SQLColumn>(cols);              
        
//        if (columns.get(0).getParent() != null) {
//            columns.get(0).getParent().getPrimaryKeyIndex().addSPListener(this);
//            for (SQLColumn col : columns) {
//                col.addSPListener(this);
//            }
//        }
        
        FormLayout layout = new FormLayout(
                "pref, pref, pref:grow, 4dlu, pref, pref:grow",
                "");
        layout.setColumnGroups(new int[][] { { 3, 6 } } );
        panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        
        JCheckBox cb;
        int row = 1;
        int width = 5;
        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.source")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        panel.add(sourceLabel = new JLabel(), cc.xyw(2, row++, width));

        layout.appendRow(RowSpec.decode("5dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.logicalName")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row));
        }
        panel.add(colLogicalName = new JTextField(), cc.xyw(2, row++, width));
        componentEnabledMap.put(colLogicalName, cb);
        colLogicalName.getDocument().addDocumentListener(new DocumentCheckboxEnabler(cb));
        colLogicalName.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                colLogicalName.requestFocusInWindow();
            }
        });
        colLogicalName.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("focus Gained : " + e);
                }
                colLogicalName.selectAll();
            }
        });

        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.physicalName")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row));
        }
        panel.add(colPhysicalName = new JTextField(), cc.xyw(2, row++, width));
        componentEnabledMap.put(colPhysicalName, cb);
        colPhysicalName.getDocument().addDocumentListener(new DocumentCheckboxEnabler(cb));
        colPhysicalName.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                colPhysicalName.requestFocusInWindow();
            }
        });
        colPhysicalName.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("focus Gained : " + e);
                }
                colPhysicalName.selectAll();
            }
        });

        layout.appendRow(RowSpec.decode("5dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row));
        }
        panel.add(colInPK = new JCheckBox(Messages.getString("ColumnEditPanel.inPrimaryKey")), cc.xyw(2, row++, width)); //$NON-NLS-1$        
        componentEnabledMap.put(colInPK, cb);
        colInPK.addActionListener(this);
        colInPK.addActionListener(checkboxEnabler);
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.type")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row));
        }
        panel.add(colType = new JComboBox(session.getSQLTypes().toArray()), cc.xyw(2, row++, width));
        componentEnabledMap.put(colType, cb);
        colType.setSelectedItem(null);
        colType.addActionListener(this);

        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.precision")), cc.xy(3, row)); //$NON-NLS-1$
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.scale")), cc.xy(6, row++)); //$NON-NLS-1$

        layout.appendRow(RowSpec.decode("p"));
        panel.add(colPrec = createPrecisionEditor(), cc.xy(3, row));
        colPrec.addChangeListener(checkboxEnabler);
        SPSUtils.makeJSpinnerSelectAllTextOnFocus(colPrec);
        final JCheckBox colPrecCB = new JCheckBox();
        panel.add(colPrecCB, cc.xy(2, row));
        typeOverrideMap.put(colPrec, colPrecCB);
        colPrecCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (colPrecCB.isSelected()) {
                    colPrec.setEnabled(true);
                } else {
                    colPrec.setEnabled(false);
                    if (colType.getSelectedItem() != null) {
                        colPrec.setValue(((UserDefinedSQLType) colType.getSelectedItem()).getPrecision(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
                    }
                }
            }
        });
        colPrec.setEnabled(false);
        
        final JCheckBox colScaleCB = new JCheckBox();
        panel.add(colScaleCB, cc.xy(5, row));
        panel.add(colScale = createScaleEditor(), cc.xy(6, row++));
        typeOverrideMap.put(colScale, colScaleCB);
        colScale.addChangeListener(checkboxEnabler);
        SPSUtils.makeJSpinnerSelectAllTextOnFocus(colScale);
        colScaleCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (colScaleCB.isSelected()) {
                    colScale.setEnabled(true);
                } else {
                    colScale.setEnabled(false);
                    if (colType.getSelectedItem() != null) {
                        colScale.setValue(((UserDefinedSQLType) colType.getSelectedItem()).getScale(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
                    }
                }
            }
        });
        colScale.setEnabled(false);
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.allowsNulls")), cc.xyw(3, row++, width - 1)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("p"));
        final JCheckBox colNullCB = new JCheckBox();
        panel.add(colNullCB, cc.xy(2, row));
        panel.add(colNullable = new JComboBox(YesNoEnum.values()), cc.xy(3, row++)); //$NON-NLS-1$
        typeOverrideMap.put(colNullable, colNullCB);
        colNullable.addActionListener(this);
        colNullable.addActionListener(checkboxEnabler);
        colNullCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (colNullCB.isSelected()) {
                    colNullable.setEnabled(true);
                } else {
                    colNullable.setEnabled(false);
                    if (colType.getSelectedItem() != null) {
                        colNullable.setSelectedItem(YesNoEnum.valueOf(
                                ((UserDefinedSQLType) colType.getSelectedItem()).getNullability() == DatabaseMetaData.columnNullable));
                    }
                }
                updateComponents();
            }
        });
        colNullable.setEnabled(false);

        layout.appendRow(RowSpec.decode("3dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.autoIncrement")), cc.xyw(3, row++, width - 1)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("p"));
        final JCheckBox colAutoIncCB = new JCheckBox();
        panel.add(colAutoIncCB, cc.xy(2, row));
        panel.add(colAutoInc = new JComboBox(YesNoEnum.values()), cc.xy(3, row++)); //$NON-NLS-1$
        typeOverrideMap.put(colAutoInc, colAutoIncCB);
        colAutoInc.addActionListener(this);
        colAutoInc.addActionListener(checkboxEnabler);
        colAutoIncCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (colAutoIncCB.isSelected()) {
                    colAutoInc.setEnabled(true);
                } else {
                    colAutoInc.setEnabled(false);
                    if (colType.getSelectedItem() != null) {
                        colAutoInc.setSelectedItem(YesNoEnum.valueOf(
                                ((UserDefinedSQLType) colType.getSelectedItem()).getAutoIncrement()));
                    }
                }
            }
        });
        colAutoInc.setEnabled(false);
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.defaultValue")), cc.xyw(3, row++, width - 1)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        final JCheckBox colDefaultCB = new JCheckBox();
        panel.add(colDefaultCB, cc.xy(2, row));
        panel.add(colDefaultValue = new JTextField(), cc.xyw(3, row++, width - 1));
        colDefaultValue.setEnabled(false);
            
        typeOverrideMap.put(colDefaultValue, colDefaultCB);
        colDefaultValue.addActionListener(this);
        colDefaultCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (colDefaultCB.isSelected()) {
                    colDefaultValue.setEnabled(true);
                } else {
                    colDefaultValue.setEnabled(false);
                    if (colType.getSelectedItem() != null) {
                        colDefaultValue.setText(((UserDefinedSQLType) colType.getSelectedItem()).getDefaultValue(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
                    }
                }
                updateComponents();
            }
        });

        layout.appendRow(RowSpec.decode("6dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.sequenceName")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("p"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row));
        }
        panel.add(colAutoIncSequenceName = new JTextField(), cc.xyw(2, row++, width));
        componentEnabledMap.put(colAutoIncSequenceName, cb);
        colAutoIncSequenceName.getDocument().addDocumentListener(new DocumentCheckboxEnabler(cb));
        
        DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                syncSequenceName();
            }

            public void insertUpdate(DocumentEvent e) {
                syncSequenceName();
            }

            public void removeUpdate(DocumentEvent e) {
                syncSequenceName();
            }
        };
        // Listener to update the sequence name when the column name changes
        colPhysicalName.getDocument().addDocumentListener(listener);
        colLogicalName.getDocument().addDocumentListener(listener);

        // Listener to rediscover the sequence naming convention, and reset the
        // sequence name to its original (according to the column's own sequence
        // name) naming convention when the user clears the sequence name field
        colAutoIncSequenceName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (columns.size() == 1 && colAutoIncSequenceName.getText().trim().equals("")) { //$NON-NLS-1$
                    // Changing sequence name doesn't make sense in multi-edit
                    // because sequence names have to be unique
                    SQLColumn column = columns.iterator().next();
                    colAutoIncSequenceName.setText(column.getAutoIncrementSequenceName());
                    if (column.getPhysicalName() != null && !column.getPhysicalName().trim().equals("")) {
                        discoverSequenceNamePattern(column.getPhysicalName());
                    } else {
                        discoverSequenceNamePattern(column.getName());
                    }
                    syncSequenceName();
                } else {
                    if (colPhysicalName.getText() != null && !colPhysicalName.getText().trim().equals("")) {
                        discoverSequenceNamePattern(colPhysicalName.getText());
                    } else {
                        discoverSequenceNamePattern(colLogicalName.getText());
                    }
                }
            }
        });

        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        panel.add(makeTitle(Messages.getString("ColumnEditPanel.remarks")), cc.xyw(2, row++, width)); //$NON-NLS-1$
        layout.appendRow(RowSpec.decode("pref:grow"));
        cb = new JCheckBox();
        if (cols.size() > 1) {
            panel.add(cb, cc.xy(1, row, "center, top"));
        }
        panel.add(new JScrollPane(colRemarks = new JTextArea()), cc.xyw(2, row++, width, "fill, fill"));
        componentEnabledMap.put(colRemarks, cb);
        colRemarks.getDocument().addDocumentListener(new DocumentCheckboxEnabler(cb));
        colRemarks.setRows(5);
        colRemarks.setLineWrap(true);
        colRemarks.setWrapStyleWord(true);

        // start with all components enabled; if there are multiple columns
        // to edit, these checkboxes will be turned off selectively for the
        // mismatching values
        for (JCheckBox checkbox : componentEnabledMap.values()) {
            checkbox.setSelected(true);
        }
        
        //The type covers multiple fields and needs a different check to see if
        //it should start enabled. All type info must match across the objects
        //for the checkbox to start selected
        if (cols.size() > 1) {
            Iterator<SQLColumn> colIterator = cols.iterator();
            SQLColumn firstCol = colIterator.next();
            while (colIterator.hasNext()) {
                SQLColumn nextCol = colIterator.next();
                if (!firstCol.getTypeName().equals(nextCol.getTypeName()) ||
                        firstCol.getPrecision() != nextCol.getPrecision() ||
                        firstCol.getScale() != nextCol.getScale() ||
                        firstCol.getNullable() != nextCol.getNullable() ||
                        firstCol.isAutoIncrement() != nextCol.isAutoIncrement() ||
                        !firstCol.getDefaultValue().equals(nextCol.getDefaultValue())) {
                    componentEnabledMap.get(colType).setSelected(false);
                    break;
                }
            }
        }
        
        for (SQLColumn col : cols) {
            logger.debug("Updating component state for column " + col);
            updateComponents(col);
        }

//         TODO only give focus to column name if it's enabled?
        colPhysicalName.requestFocus();
        colPhysicalName.selectAll();
        
        SQLPowerUtils.listenToHierarchy(session.getRootObject(), obsolesenceListener);
        SQLPowerUtils.listenToHierarchy(session.getRootObject(), this);
        panel.addAncestorListener(cleanupListener);
        colType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getItem() instanceof UserDefinedSQLType && e.getStateChange() == ItemEvent.SELECTED) {
                    UserDefinedSQLType sqlType = (UserDefinedSQLType) e.getItem();
                    updateSQLTypeComponents(sqlType, false);
                }
            }
        });
    }

    private Component makeTitle(String string) {
        JLabel label = new JLabel(string);
        label.setFont(TITLE_FONT);
        return label;
    }

    private JSpinner createScaleEditor() {
        return new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    }

    private JSpinner createPrecisionEditor() {
        return createScaleEditor(); // looks better if both spinners are same
                                    // size
    }

    /**
     * Updates all the UI components to reflect the given column's properties.
     * <p>
     * This is a constructor subroutine which is only called one time per
     * instance per column being edited. Once a ColumnEditPanel is constructed,
     * it is forever tied to the column or columns it was constructed with. In
     * the multi-column-edit case, the first call to this method works "as usual,"
     * meaning that all the fields get set to represent the values of that column.
     * Subsequent calls end up unchecking the "apply this value" checkboxes beside
     * each component whenever a difference is discovered between the component's
     * existing value and the value that would have been set for that subsequent
     * column.
     * 
     * @param col One of the columns to edit in this dialog.
     */
    private void updateComponents(SQLColumn col) throws SQLObjectException {
        SQLColumn sourceColumn = col.getSourceColumn();
        if (sourceColumn == null) {
            sourceLabel.setText(Messages.getString("ColumnEditPanel.noneSpecified")); //$NON-NLS-1$
        } else {
            
            sourceLabel.setText(
                    DDLUtils.toQualifiedName(
                            sourceColumn.getParent()) + "." + sourceColumn.getName());
        }
        
        updateComponent(colLogicalName, col.getName());
        updateComponent(colPhysicalName, col.getPhysicalName());
        updateComponent(colType, col.getUserDefinedSQLType().getUpstreamType());

        updateSQLTypeComponents(col.getUserDefinedSQLType(), true);
        
        updateComponent(colRemarks, col.getRemarks());
        
        boolean inPk;
        if (col.getParent() == null) {
            inPk = SQLColumn.isDefaultInPK(); // XXX looks fishy--how can a column be in the PK if it has no parent table?
            logger.debug("new constructed column");
        } else {
            inPk = col.isPrimaryKey();
            logger.debug("existing column");
        }
        updateComponent(colInPK, inPk);
        logger.debug("Selected" + colInPK.isSelected());
        
        
        updateComponent(colAutoIncSequenceName, col.getAutoIncrementSequenceName());

        updateComponents();
        if (col.getPhysicalName() != null && !col.getPhysicalName().trim().equals("")) {
            discoverSequenceNamePattern(col.getPhysicalName());
        } else {
            discoverSequenceNamePattern(col.getName());
        }
    }

    /** Subroutine of {@link #updateComponents(SQLColumn)}. */
    private void updateComponent(JTextComponent comp, String expectedValue) {
        boolean unvisited = comp.getText().equals("");
        if (componentEnabledMap.get(comp).isSelected() && (unvisited || comp.getText().equals(expectedValue))) {
            comp.setText(expectedValue);
        } else {
            comp.setText("");
            componentEnabledMap.get(comp).setSelected(false);
        }
    }
    
    /** Subroutine of {@link #updateComponents(SQLColumn)}. */
    private void updateComponent(JComboBox comp, Object expectedValue) {
        boolean unvisited = comp.getSelectedItem() == null;
        if (componentEnabledMap.get(comp).isSelected() &&
                (unvisited || comp.getSelectedItem().equals(expectedValue))) {
            comp.setSelectedItem(expectedValue);
        } else {
            comp.setSelectedItem(null);
            componentEnabledMap.get(comp).setSelected(false);
        }
    }

    /** Subroutine of {@link #updateComponents(SQLColumn)}. */
    private void updateComponent(JCheckBox comp, boolean expectedValue) {
        // Checking if a checkbox was visited is not possible just by examining its value,
        // so we check for (and store) a client property when we visit it
        final String multiEditVisitedProperty = "ColumnEditPanel.multiEditVisited";
        boolean unvisited = comp.getClientProperty(multiEditVisitedProperty) == null;
        if (componentEnabledMap.get(comp).isSelected() && (unvisited || comp.isSelected() == expectedValue)) {
            comp.setSelected(expectedValue);
        } else {
            comp.setSelected(false);
            componentEnabledMap.get(comp).setSelected(false);
        }
        comp.putClientProperty(multiEditVisitedProperty, Boolean.TRUE);
    }

    /**
     * Figures out what the sequence name prefix and suffix strings are, based
     * on the current contents of the sequence name and column name fields.
     */
    private void discoverSequenceNamePattern(String colName) {
        String seqName = colAutoIncSequenceName.getText();
        int prefixEnd = seqName.indexOf(colName);
        if (prefixEnd >= 0 && colName.length() > 0) {
            seqNamePrefix = seqName.substring(0, prefixEnd);
            seqNameSuffix = seqName.substring(prefixEnd + colName.length());
        } else {
            seqNamePrefix = null;
            seqNameSuffix = null;
        }
    }

    /**
     * Modifies the contents of the "auto-increment sequence name" field to
     * match the naming scheme as it is currently understood. This modification
     * is only performed if the naming scheme has been successfully determined
     * by the {@link #discoverSequenceNamePattern(String)} method. The new
     * sequence name is written directly to the {@link #colAutoIncSequenceName}
     * field.
     */
    private void syncSequenceName() {
        if (seqNamePrefix != null && seqNameSuffix != null) {
            String newName = seqNamePrefix;
            newName += (colPhysicalName.getText() == null || colPhysicalName.getText().trim().equals("")) ? 
                    colLogicalName.getText() : colPhysicalName.getText();
            newName += seqNameSuffix;
            colAutoIncSequenceName.setText(newName);
        }
    }

    /**
     * Implementation of ActionListener.
     */
    public void actionPerformed(ActionEvent e) {
        logger.debug("action event " + e); //$NON-NLS-1$
        updateComponents();
    }

    /**
     * Examines the components and makes sure they're in a consistent state
     * (they are legal with respect to the model).
     */
    private void updateComponents() {
        // allow nulls is free unless column is in PK
        if (colInPK.isSelected() || !typeOverrideMap.get(colNullable).isSelected()) {
            colNullable.setEnabled(false);
        } else {
            colNullable.setEnabled(true);
        }

        // primary key is free unless column allows nulls
        if (((YesNoEnum) colNullable.getSelectedItem()).getValue()) {
            colInPK.setEnabled(false);
        } else {
            colInPK.setEnabled(true);
        }

        if (colInPK.isSelected() && ((YesNoEnum) colNullable.getSelectedItem()).getValue()) {
            // this should not be physically possible
            colNullable.setSelectedItem(false);
            colNullable.setEnabled(false);
        }

        if (((YesNoEnum) colAutoInc.getSelectedItem()).getValue() || 
                !typeOverrideMap.get(colDefaultValue).isSelected()) {
            colDefaultValue.setText(""); //$NON-NLS-1$
            colDefaultValue.setEnabled(false);
        } else {
            colDefaultValue.setEnabled(true);
        }

        colAutoIncSequenceName.setEnabled(((YesNoEnum) colAutoInc.getSelectedItem()).getValue());
    }

    /**
     * Sets the properties of each column being edited to match those on screen. Only
     * components with their associated checkbox selected will be considered.
     * 
     * @return A list of error messages if the update was not successful.
     */
    private List<String> updateModel() {
        logger.debug("Updating model"); //$NON-NLS-1$
        List<String> errors = new ArrayList<String>();
        SQLObject compoundEditRoot = SQLObjectUtils.findCommonAncestor(columns);
        logger.debug("Compound edit root is " + compoundEditRoot);
        try {
            compoundEditRoot.begin(Messages.getString("ColumnEditPanel.compoundEditName")); //$NON-NLS-1$
            
            for (SQLColumn column : columns) {
                if (componentEnabledMap.get(colLogicalName).isSelected()) {
                    if (colLogicalName.getText().trim().length() == 0) {
                        errors.add(Messages.getString("ColumnEditPanel.columnNameRequired")); //$NON-NLS-1$
                    } else {
                        column.setName(colLogicalName.getText());
                    }
                }
                if (componentEnabledMap.get(colPhysicalName).isSelected()) {
                    column.setPhysicalName(colPhysicalName.getText());
                }                
                if (componentEnabledMap.get(colType).isSelected()) {
                    column.getUserDefinedSQLType().setUpstreamType((UserDefinedSQLType) colType.getSelectedItem());
                }
                
                if (componentEnabledMap.get(colType).isSelected()) {
                    if (typeOverrideMap.get(colScale).isSelected()) {
                        column.setScale(((Integer) colScale.getValue()).intValue());
                    } else {
                        column.getUserDefinedSQLType().setScale(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM, null);
                    }
                }
                
                if (componentEnabledMap.get(colType).isSelected()) {
                    if (typeOverrideMap.get(colPrec).isSelected()) {
                        column.setPrecision(((Integer) colPrec.getValue()).intValue());
                    } else {
                        column.getUserDefinedSQLType().setPrecision(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM, null);
                    }
                }
                
                if (componentEnabledMap.get(colType).isSelected()) {
                    if (typeOverrideMap.get(colNullable).isSelected()) {
                        column.setNullable(((YesNoEnum) colNullable.getSelectedItem()).getValue() ? DatabaseMetaData.columnNullable
                            : DatabaseMetaData.columnNoNulls);
                    } else {
                        column.getUserDefinedSQLType().setMyNullability(null);
                    }
                }
                
                if (componentEnabledMap.get(colRemarks).isSelected()) {
                    column.setRemarks(colRemarks.getText());
                }

                if (componentEnabledMap.get(colType).isSelected()) {
                    if (typeOverrideMap.get(colDefaultValue).isSelected()) {
                        // avoid setting default value to empty string
                        if (!(column.getDefaultValue() == null && colDefaultValue.getText().equals(""))) { //$NON-NLS-1$
                            column.setDefaultValue(colDefaultValue.getText());
                        }
                    } else {
                        column.getUserDefinedSQLType().setDefaultValue(
                                SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM, null);
                    }
                }
                
                // Autoincrement has to go before the primary key or
                // this column will never allow nulls
                if (componentEnabledMap.get(colType).isSelected()) {
                    if (typeOverrideMap.get(colAutoInc).isSelected()) {
                        column.setAutoIncrement(((YesNoEnum) colAutoInc.getSelectedItem()).getValue());
                    } else {
                        column.getUserDefinedSQLType().setMyAutoIncrement(null);
                    }
                }
                
                if (componentEnabledMap.get(colInPK).isSelected()) {
                    if (colInPK.isSelected() && !column.isPrimaryKey()) {
                        column.getParent().addToPK(column);
                    } else if (!colInPK.isSelected() && column.isPrimaryKey()) {
                        column.getParent().moveAfterPK(column);
                    }
                }
                
                if (componentEnabledMap.get(colAutoIncSequenceName).isSelected()) {
                    column.setAutoIncrementSequenceName(colAutoIncSequenceName.getText());
                }
            }
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        } finally {
            compoundEditRoot.commit();
        }
        return errors;
    }

    // ------------------ ARCHITECT PANEL INTERFACE ---------------------

    /**
     * Calls updateModel since the user may have clicked "ok" before hitting
     * enter on a text field.
     */
    public boolean applyChanges() {
        SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), this);
        List<String> errors = updateModel();
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(panel, errors.toString());
            return false;
        } else {
            return true;
        }
    }

    /**
     * Does nothing. The column's properties will not have been modified.
     */
    public void discardChanges() {
        SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), this);
    }

    /* docs inherit from interface */
    public JPanel getPanel() {
        return panel;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JComboBox getColAutoInc() {
        return colAutoInc;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JTextField getColDefaultValue() {
        return colDefaultValue;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JCheckBox getColInPK() {
        return colInPK;
    }
    /** Only for testing. Normal client code should not need to call this. */
    public JTextField getColLogicalName() {
        return colLogicalName;
    }
    
    /** Only for testing. Normal client code should not need to call this. */
    public JTextField getColPhysicalName() {
        return colPhysicalName;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JComboBox getColNullable() {
        return colNullable;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JSpinner getColPrec() {
        return colPrec;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JTextArea getColRemarks() {
        return colRemarks;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JSpinner getColScale() {
        return colScale;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JComboBox getColType() {
        return colType;
    }

    /** Only for testing. Normal client code should not need to call this. */
    public JLabel getSourceLabel() {
        return sourceLabel;
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }

    /**
     * The one instance of {@link CheckboxEnabler} that handles events from all
     * non-text components in this panel.
     */
    private final CheckboxEnabler checkboxEnabler = new CheckboxEnabler();
    
    /**
     * A simple listener that enables the checkbox associated with a component
     * whenever that component is manipulated by the user.
     */
    private class CheckboxEnabler implements ActionListener, ChangeListener {

        public void actionPerformed(ActionEvent e) { enable((JComponent) e.getSource()); }

        public void stateChanged(ChangeEvent e) { enable((JComponent) e.getSource()); }
        
        private void enable(JComponent c) {
            JCheckBox checkBox = componentEnabledMap.get(c);
            if (checkBox != null) {
                checkBox.setSelected(true);
            }
        }
    }
    
    /**
     * Simple listener that enables the checkbox associated with a single
     * text component whenever its document changes. Instances of this listener
     * can't be shared among components; you need one instance per component.
     */
    private class DocumentCheckboxEnabler implements DocumentListener {
        
        private final JCheckBox checkBox;

        public DocumentCheckboxEnabler(JCheckBox checkBox) {
            this.checkBox = checkBox;
        }
        
        public void changedUpdate(DocumentEvent e) { checkBox.setSelected(true); }
        public void insertUpdate(DocumentEvent e) { checkBox.setSelected(true); }
        public void removeUpdate(DocumentEvent e) { checkBox.setSelected(true); }
    }
    
    /**
     * Listens for SQLObject removals in the model that would make this
     * column editor obsolete (because it refers to properties of a 
     * column that is no longer in the model). When this editor is deemed
     * obsolete, it looks for its nearest Window ancestor and disposes it.
     */
    private final SPListener obsolesenceListener = new AbstractPoolingSPListener() {
        @Override
        public void childAddedImpl(SPChildEvent e) {
            logger.debug("SQLObject children got inserted: " + e); //$NON-NLS-1$
        }

        /**
         * Checks to see if any of the columns being edited was just removed from
         * the playpen. If yes, disposes the enclosing window.
         */
        @Override
        public void childRemovedImpl(SPChildEvent e) {
            logger.debug("SQLObject children got removed: " + e); //$NON-NLS-1$
            for (SQLColumn column : columns) {
                if (e.getChild().equals(column) || e.getChild().equals(column.getParent())) {
                    Window parentWindow = SwingUtilities.getWindowAncestor(panel);
                    if (parentWindow != null) {
                        parentWindow.dispose();
                    }
                }
            }
        }

    };
 
    /**
     * Watches for this component becoming invisible and then unregisters it as a
     * listener on all the objects it has been listening to.
     */
    private final AncestorListener cleanupListener = new AncestorListener() {

        public void ancestorAdded(AncestorEvent event) { /* don't care */ }

        public void ancestorMoved(AncestorEvent event) { /* don't care */ }

        public void ancestorRemoved(AncestorEvent event) {
            SQLPowerUtils.unlistenToHierarchy(session.getRootObject(), obsolesenceListener);            
        }
    };

    public void childAdded(SPChildEvent e) {
        try {
            if (e.getSource() == columns.get(0).getParent().getPrimaryKeyIndex()) {
                // The column that was added may not be the one at index 0, but
                // e.getChild() is a copied column, and it doesn't matter
                // so long as a change is flagged, which it will be.
                propertyChanged(new PropertyChangeEvent(columns.get(0), "inPK", false, true));
            }
        } catch (SQLObjectException ex) {            
            throw new RuntimeException(ex);
        }
    }

    public void childRemoved(SPChildEvent e) {
        try {
            if (e.getSource() == columns.get(0).getParent().getPrimaryKeyIndex()) {
                propertyChanged(new PropertyChangeEvent(columns.get(0), "inPK", true, false));
            }
        } catch (SQLObjectException ex) {            
            throw new RuntimeException(ex);
        }
    }

    public void propertyChanged(PropertyChangeEvent e) {
        if (columns.contains(e.getSource())) {
            String property = e.getPropertyName();
            if (property.equals("name")) {
                DataEntryPanelChangeUtil.incomingChange(colLogicalName, e);
            } else if (property.equals("physicalName")) {                
                DataEntryPanelChangeUtil.incomingChange(colPhysicalName, e);
            } else if (property.equals("type")) {
                DataEntryPanelChangeUtil.incomingChange(colType, e);
            } else if (property.equals("precision")) {
                DataEntryPanelChangeUtil.incomingChange(colPrec, e);
            } else if (property.equals("scale")) {                            
                DataEntryPanelChangeUtil.incomingChange(colScale, e);
            } else if (property.equals("inPK")) {
                DataEntryPanelChangeUtil.incomingChange(colInPK, e);
            } else if (property.equals("isNullable")) {                                        
                DataEntryPanelChangeUtil.incomingChange(colNullable, e);
            } else if (property.equals("autoIncrement")) {
                DataEntryPanelChangeUtil.incomingChange(colAutoInc, e);
            } else if (property.equals("autoIncrementSequenceName")) {
                DataEntryPanelChangeUtil.incomingChange(colAutoIncSequenceName, e);
            } else if (property.equals("remarks")) {
                DataEntryPanelChangeUtil.incomingChange(colRemarks, e);
            } else if (property.equals("defaultValue")) { 
                DataEntryPanelChangeUtil.incomingChange(colDefaultValue, e);
            } else return;
            setErrorText(DataEntryPanelChangeUtil.ERROR_MESSAGE);
        }       
    }

    public void transactionEnded(TransactionEvent e) {
        //no-op
    }

    public void transactionRollback(TransactionEvent e) {
        //no-op
    }

    public void transactionStarted(TransactionEvent e) {
        //no-op
    }

    /**
     * If a user chooses a new Type to base the column on, then the UI
     * components for other properties like precision, scale, default value,
     * nullability, and autoincrement need to change to match that of the new
     * type. But the SQLColumn object itself must not change at that point, so
     * that it is simple to cancel any changes if the user chooses to click the
     * 'Cancel' button.
     * 
     * @param sqlType
     *            The data type to update all of the type fields to.
     * @param overrideIfNotNull
     *            If true the override check boxes will be checked and the field
     *            enabled if the value in the type given is not null. If false
     *            the override checkboxes will never be checked to start and
     *            just use the defaults given by the data type.
     */
    private void updateSQLTypeComponents(UserDefinedSQLType sqlType, boolean overrideIfNotNull) {
        
        if (!componentEnabledMap.get(colType).isSelected()) {
            //not editing any of these fields, setting to defaults
            colScale.setValue(0);
            colPrec.setValue(0);
            colNullable.setSelectedItem(YesNoEnum.NO);
            colAutoInc.setSelectedItem(YesNoEnum.NO);
            colDefaultValue.setText("");
            return;
        }
        
        if (sqlType.getScaleType(SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM) == PropertyType.NOT_APPLICABLE) {
            typeOverrideMap.get(colScale).setSelected(false);
            typeOverrideMap.get(colScale).setEnabled(false);
        } else if (sqlType.getDefaultPhysicalProperties().getScale() == null || !overrideIfNotNull) {
            typeOverrideMap.get(colScale).setSelected(false);
            typeOverrideMap.get(colScale).setEnabled(true);
        } else {
            typeOverrideMap.get(colScale).setSelected(true);
            typeOverrideMap.get(colScale).setEnabled(true);
        }
        colScale.setValue(sqlType.getScale(SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
        
        
        if (sqlType.getPrecisionType(SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM) == PropertyType.NOT_APPLICABLE) {
            typeOverrideMap.get(colPrec).setSelected(false);
            typeOverrideMap.get(colPrec).setEnabled(false);
        } else if (sqlType.getDefaultPhysicalProperties().getPrecision() == null || !overrideIfNotNull) {
            typeOverrideMap.get(colPrec).setSelected(false);
            typeOverrideMap.get(colPrec).setEnabled(true);
        } else {
            typeOverrideMap.get(colPrec).setSelected(true);
            typeOverrideMap.get(colPrec).setEnabled(true);
        }
        colPrec.setValue(sqlType.getPrecision(SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
        
        if (sqlType.getMyNullability() == null || !overrideIfNotNull) {
            typeOverrideMap.get(colNullable).setSelected(false);
        } else {
            typeOverrideMap.get(colNullable).setSelected(true);
        }
        colNullable.setSelectedItem(YesNoEnum.valueOf(
                sqlType.getNullability() == DatabaseMetaData.columnNullable));
        
        if (sqlType.getDefaultPhysicalProperties().getDefaultValue() == null || !overrideIfNotNull) {
            typeOverrideMap.get(colDefaultValue).setSelected(false);
        } else {
            typeOverrideMap.get(colDefaultValue).setSelected(true);
        }
        colDefaultValue.setText(sqlType.getDefaultValue(SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM));
        
        if (sqlType.getMyAutoIncrement() == null || !overrideIfNotNull) {
            typeOverrideMap.get(colAutoInc).setSelected(false);
        } else {
            typeOverrideMap.get(colAutoInc).setSelected(true);
        }
        colAutoInc.setSelectedItem(YesNoEnum.valueOf(sqlType.getAutoIncrement()));
    }
    
    /**
     * For testing purposes only.
     */
    Map<JComponent, JCheckBox> getTypeOverrideMap() {
        return typeOverrideMap;
    }
}
