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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.TableOrder;

import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.table.FreqValueCountTableModel;
import ca.sqlpower.architect.swingui.table.FreqValueTable;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.swingui.TimedDocumentListener;
import ca.sqlpower.swingui.table.TableModelSortDecorator;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Creates and handles a specific display panel
 * 
 */
public class ProfileGraphPanel {
    
    private JLabel rowCountDisplay;

    private JLabel title;

    private JLabel nullableLabel;

    private JLabel minValue;

    private JLabel maxValue;

    private JLabel avgValue;

    private JLabel nullCountLabel;

    private JLabel nullPercentLabel;

    private JLabel minLengthLabel;

    private JLabel uniqueCountLabel;

    private JLabel uniquePercentLabel;

    private JLabel maxLengthLabel;

    private FreqValueTable freqValueTable;

    private JScrollPane freqValueSp;

    /**
     * A panel that contains both the {@link #validResultsPanel} and
     * {@link #invalidResultsPanel}, only one of which is visible at a time.
     */
    private final JPanel displayArea;

    /**
     * The panel with the various column stats fields, the top N list, and the
     * top N chart.
     */
    private final JPanel validResultsPanel;

    /**
     * The panel with the exception message and any hints on how to rectify the
     * problem.
     */
    private final JPanel invalidResultsPanel;

    /**
     * A label that fills up the whole invalid results panel.
     */
    private JLabel invalidResultsLabel;

    private int rowCount;

    private ChartPanel chartPanel;

    private static final Logger logger = Logger.getLogger(ProfileGraphPanel.class);

    private JTextArea notesField;
    
    private TimedDocumentListener notesFieldListener;
    
    private SPListener notesListener;
    
    private final ProfilePanel profilePanel;

    private ColumnProfileResult columnProfileResult;

    public ProfileGraphPanel(ProfilePanel panel, int rowCount) {
        this.profilePanel = panel;
        this.rowCount = rowCount;

        FormLayout displayLayout = new FormLayout("4dlu, default, 4dlu, 100dlu, 4dlu, fill:default:grow, 4dlu", // columns
                "4dlu, default, 6dlu"); // rows
        CellConstraints cc = new CellConstraints();

        validResultsPanel = ProfileGraphPanel.logger.isDebugEnabled() ? new FormDebugPanel(displayLayout) : new JPanel(
                displayLayout);
        validResultsPanel.setBorder(BorderFactory.createEtchedBorder());

        Font bodyFont = validResultsPanel.getFont();
        Font titleFont = bodyFont.deriveFont(Font.BOLD, bodyFont.getSize() * 1.25F);

        title = new JLabel("Column Name");
        title.setFont(titleFont);

        PanelBuilder pb = new PanelBuilder(displayLayout, validResultsPanel);
        pb.add(title, cc.xyw(2, 2, 5));

        int row = 4;
        rowCountDisplay = makeInfoRow(pb, "RowCount", row);
        row += 2;
        nullableLabel = makeInfoRow(pb, "Nullable", row);
        row += 2;
        nullCountLabel = makeInfoRow(pb, "Null Count", row);
        row += 2;
        nullPercentLabel = makeInfoRow(pb, "% Null Records", row);
        row += 2;
        minLengthLabel = makeInfoRow(pb, "Minimum Length", row);
        row += 2;
        maxLengthLabel = makeInfoRow(pb, "Maximum Length", row);
        row += 2;
        uniqueCountLabel = makeInfoRow(pb, "Unique Values", row);
        row += 2;
        uniquePercentLabel = makeInfoRow(pb, "% Unique", row);
        row += 2;
        minValue = makeInfoRow(pb, "Minimum Value", row);
        row += 2;
        maxValue = makeInfoRow(pb, "Maximum Value", row);
        row += 2;
        avgValue = makeInfoRow(pb, "Average Value", row);
        row += 2;

        freqValueTable = new FreqValueTable(null);
        freqValueSp = new JScrollPane(freqValueTable);

        pb.appendRow("fill:10dlu:grow");
        pb.appendRow("fill:default:grow");
        pb.add(freqValueSp, cc.xyw(2, row + 1, 3));

        // Now add something to represent the chart
        JFreeChart createPieChart = ChartFactory.createPieChart("", new DefaultPieDataset(new DefaultKeyedValues()),
                false, false, false);
        chartPanel = new ChartPanel(createPieChart);
        chartPanel.setPreferredSize(new Dimension(300, 300));

        if (panel.getProfileManager().getWorkspaceContainer() instanceof ArchitectSession &&
                ((ArchitectSession) panel.getProfileManager().getWorkspaceContainer()).isEnterpriseSession()) {
            pb.add(new JLabel("Column Profile Notes"), cc.xy(6, 2));
            notesField = new JTextArea();
            notesField.setLineWrap(true);
            notesField.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesField);
            notesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            pb.add(notesScroll, cc.xywh(6, 4, 1, row - 4));

            pb.appendRow("fill:4dlu:grow");
            pb.appendRow("4dlu");

            pb.add(chartPanel, cc.xy(6, row + 1));
        } else {
            pb.appendRow("fill:4dlu:grow");
            pb.appendRow("4dlu");
            pb.add(chartPanel, cc.xywh(6, 4, 1, row - 2));
        }
        
        invalidResultsPanel = new JPanel(new BorderLayout());
        invalidResultsLabel = new JLabel("No error message yet");
        invalidResultsPanel.add(invalidResultsLabel);

        displayArea = new JPanel(new GridLayout(1, 1));
        displayArea.setPreferredSize(validResultsPanel.getPreferredSize());
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public JPanel getDisplayArea() {
        return displayArea;
    }

    /**
     * Switches the graph to show the value distribution for the given column.
     * 
     * @param cr
     *            The profile result to display. Must not be null.
     */
    public void displayProfile(final ColumnProfileResult cr) {
        this.columnProfileResult = cr;
        displayArea.removeAll();
        if (cr.getException() != null) {
            displayInvalidProfile(cr);
            displayArea.add(invalidResultsPanel);
        } else {
            displayValidProfile(cr);
            displayArea.add(validResultsPanel);
        }
        
        if (notesField != null) {
            if (notesFieldListener != null) {
                notesField.getDocument().removeDocumentListener(notesFieldListener);
                notesFieldListener.cancel();
            }
            columnProfileResult.removeSPListener(notesListener);
            notesField.setText(cr.getNotes());
            notesListener = new AbstractSPListener() {
                @Override
                public void propertyChanged(PropertyChangeEvent evt) {
                    if ("notes".equals(evt.getPropertyName())) {
                        if (!evt.getNewValue().equals(notesField.getText())) {
                            notesField.setText((String) evt.getNewValue());
                        }
                    }
                }
            };
            cr.addSPListener(notesListener);
            notesFieldListener = new TimedDocumentListener(cr.getProfiledObject().getName(), 2500) {
                @Override
                public void textChanged() {
                    final String notesText = notesField.getText();
                    profilePanel.getProfileManager().getRunnableDispatcher().runInForeground(new Runnable() {
                        @Override
                        public void run() {
                            cr.setNotes(notesText);
                        }
                    });
                }
            };
            notesField.getDocument().addDocumentListener(notesFieldListener);
        }
        
        displayArea.revalidate();
        displayArea.repaint();
    }

    /**
     * Subroutine of {@link #displayProfile(ColumnProfileResult)}.
     */
    private void displayInvalidProfile(ColumnProfileResult cr) {
        SQLColumn profiledColumn = cr.getProfiledObject();

        String databaseType = null;
        if (profiledColumn.getParent() != null) {
            SQLDatabase parentDB = profiledColumn.getParent().getParentDatabase();
            if (parentDB != null) {
                JDBCDataSource ds = parentDB.getDataSource();
                if (ds != null) {
                    JDBCDataSourceType parentType = ds.getParentType();
                    databaseType = parentType.getName();
                }
            }
        }

        String advice = "";
        if (databaseType != null) {
            advice = "You may be able to rectify this situation by visiting the JDBC Drivers " +
                    "configuration panel for " + databaseType + " and modifying the set of profile " +
                    "functions to attempt for type " + profiledColumn.getTypeName() + ".";
        }

        invalidResultsLabel.setText("<html>" + " <table width=100% height=100%>" + "  <tr>" + "   <td>" +
                "    <h2>Profiler was unable to create a profile for column <i>" + profiledColumn.getName() +
                "</i></h2>" + "    <p>" + cr.getException() + "    <p><p>" + advice + "   </td>" + "  </tr>" +
                " </table>" + "</html>");

        notesField.setText(cr.getNotes());
    }

    /**
     * Subroutine of {@link #displayProfile(ColumnProfileResult)}.
     */
    private void displayValidProfile(final ColumnProfileResult cr) {
        TableProfileResult tr = (TableProfileResult) cr.getParent();
        rowCount = tr.getRowCount();
        rowCountDisplay.setText(Integer.toString(rowCount));

        StringBuffer sb = new StringBuffer();
        SQLColumn c = cr.getProfiledObject();
        sb.append(c);
        if (c.isPrimaryKey()) {
            sb.append(' ').append("[PK]");
        }
        setTitle(sb.toString());
        nullableLabel.setText(Boolean.toString(c.isDefinitelyNullable()));

        chartPanel.setChart(createTopNChart(cr, rowCount));
        nullCountLabel.setText(Integer.toString(cr.getNullCount()));
        int nullsInRecords = cr.getNullCount();
        double ratio = rowCount > 0 ? nullsInRecords * 100D / rowCount : 0;
        nullPercentLabel.setText(format(ratio));
        uniqueCountLabel.setText(Integer.toString(cr.getDistinctValueCount()));
        double uniqueRatio = rowCount > 0 ? cr.getDistinctValueCount() * 100D / rowCount : 0;
        uniquePercentLabel.setText(format(uniqueRatio));
        minLengthLabel.setText(Integer.toString(cr.getMinLength()));
        maxLengthLabel.setText(Integer.toString(cr.getMaxLength()));

        minValue.setText(cr.getMinValue() == null ? "" : cr.getMinValue().toString());
        maxValue.setText(cr.getMaxValue() == null ? "" : cr.getMaxValue().toString());
        Object o = cr.getAvgValue();
        if (o == null) {
            avgValue.setText("");
        } else if (o instanceof BigDecimal) {
            double d = ((BigDecimal) o).doubleValue();
            avgValue.setText(format(d));
        } else {
            ProfilePanel.logger.debug("Got avgValue of type: " + o.getClass().getName());
            avgValue.setText(cr.getAvgValue().toString());
        }

        if (notesField != null) {
            
        }

        FreqValueCountTableModel freqValueCountTableModel = new FreqValueCountTableModel(cr);
        TableModelSortDecorator sortModel = new TableModelSortDecorator(freqValueCountTableModel);
        sortModel.setColumnComparator(freqValueCountTableModel.getColumnClass(2), 
                new ColumnValueCount.ColumnValueComparator());
        freqValueTable.setModel(sortModel);
        sortModel.setTableHeader(freqValueTable.getTableHeader());
        freqValueTable.initColumnSizes();
    }

    public static JFreeChart createTopNChart(ColumnProfileResult cr, int rowCount) {
        JFreeChart chart;
        List<ColumnValueCount> valueCounts = cr.getValueCount();
        SQLColumn col = cr.getProfiledObject();
        DefaultCategoryDataset catDataset = new DefaultCategoryDataset();

        long otherDataCount = rowCount;
        for (ColumnValueCount vc : valueCounts) {
            catDataset
                    .addValue(vc.getCount(), col.getName(), vc.getValue() == null ? "null" : vc.getValue().toString());
            otherDataCount -= vc.getCount();
        }
        int numberOfTopValues = catDataset.getColumnCount();
        if (otherDataCount > 0) {
            catDataset.addValue(otherDataCount, col.getName(), "Other Values");
        }

        String chartTitle;
        if (numberOfTopValues == 10) {
            chartTitle = "Top " + numberOfTopValues + " most common values";
        } else {
            chartTitle = "All " + numberOfTopValues + " values";
        }

        chart = ChartFactory.createPieChart(chartTitle, new CategoryToPieDataset(catDataset, TableOrder.BY_ROW, 0),
                false, true, false);
        if (chart.getPlot() instanceof PiePlot) {
            ((PiePlot) chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0} [{1}]"));
        }
        return chart;
    }

    private String format(double d) {
        return String.format("%6.2f", d);
    }

    private JLabel makeInfoRow(PanelBuilder pb, String title, int row) {
        CellConstraints cc = new CellConstraints();
        pb.appendRow("default");
        pb.appendRow("2dlu");
        pb.add(new JLabel(title), cc.xy(2, row));
        JLabel label = new JLabel("--");
        pb.add(label, cc.xy(4, row));
        return label;
    }

    public void close() {
        if (notesFieldListener != null) {
            notesFieldListener.cancel();
        }
    }
    
}