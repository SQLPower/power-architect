/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.TableOrder;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.ProfilePanel.ChartTypes;
import ca.sqlpower.architect.swingui.table.FreqValueCountTableModel;
import ca.sqlpower.architect.swingui.table.FreqValueTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 *  Creates and handles a specific display panel
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

    ChartTypes chartType = ChartTypes.PIE;
    JPanel displayArea;
    private int rowCount;
    private ChartPanel chartPanel;

    private static final Logger logger = Logger.getLogger(ProfileGraphPanel.class);


    public ProfileGraphPanel(ProfilePanel panel, int rowCount) {
        this.rowCount = rowCount;

        FormLayout displayLayout = new FormLayout(
                "4dlu, default, 4dlu, 100dlu, 4dlu, fill:default:grow, 4dlu", // columns
                "4dlu, default, 6dlu"); // rows
        CellConstraints cc = new CellConstraints();

        displayArea = ProfileGraphPanel.logger.isDebugEnabled() ? new FormDebugPanel(displayLayout) : new JPanel(displayLayout);
        displayArea.setBorder(BorderFactory.createEtchedBorder());

        Font bodyFont = displayArea.getFont();
        Font titleFont = bodyFont.deriveFont(Font.BOLD, bodyFont.getSize() * 1.25F);

        title = new JLabel("Column Name");
        title.setFont(titleFont);

        PanelBuilder pb = new PanelBuilder(displayLayout, displayArea);
        pb.add(title, cc.xyw(2, 2, 5));

        int row = 4;
        rowCountDisplay = makeInfoRow(pb, "RowCount", row); row += 2;
        nullableLabel = makeInfoRow(pb, "Nullable", row); row += 2;
        nullCountLabel = makeInfoRow(pb, "Null Count", row); row += 2;
        nullPercentLabel = makeInfoRow(pb, "% Null Records", row); row += 2;
        minLengthLabel = makeInfoRow(pb, "Minimum Length", row); row += 2;
        maxLengthLabel = makeInfoRow(pb, "Maximum Length", row); row += 2;
        uniqueCountLabel = makeInfoRow(pb,"Unique Values",row); row+=2;
        uniquePercentLabel = makeInfoRow(pb,"% Unique", row); row +=2;
        minValue = makeInfoRow(pb, "Minimum Value", row); row += 2;
        maxValue = makeInfoRow(pb, "Maximum Value", row); row += 2;
        avgValue = makeInfoRow(pb, "Average Value", row); row += 2;


        freqValueTable = new FreqValueTable(null);
        freqValueSp = new JScrollPane(freqValueTable);

        pb.appendRow("fill:10dlu:grow");
        pb.appendRow("fill:default:grow");
        pb.add(freqValueSp, cc.xyw(2,row+1,3));




        pb.appendRow("fill:4dlu:grow");
        pb.appendRow("4dlu");


        // Now add something to represent the chart
        JFreeChart createPieChart = ChartFactory.createPieChart("",new DefaultPieDataset(new DefaultKeyedValues()),false,false,false);
        chartPanel = new ChartPanel(createPieChart);

        pb.add(chartPanel, cc.xywh(6, 4, 1, row-2));


    }

    public void setTitle(String title) {
        this.title.setText(title);
    }


    public JPanel getDisplayArea() {
        return displayArea;
    }

    public void setDisplayArea(JPanel displayArea) {
        this.displayArea = displayArea;
    }

    public void displayProfile(ColumnProfileResult cr) {
        TableProfileResult tr = (TableProfileResult) cr.getParentResult();
        
        if (tr instanceof TableProfileResult) {
            rowCount = tr.getRowCount();
        } else {
            rowCount = 0;
        }
        rowCountDisplay.setText(Integer.toString(rowCount));

        StringBuffer sb = new StringBuffer();
        SQLColumn c = cr.getProfiledObject();
        sb.append(c);
        if (c.isPrimaryKey()) {
            sb.append(' ').append("[PK]");
        }
        setTitle(sb.toString());
        nullableLabel.setText(Boolean.toString(c.isDefinitelyNullable()));
        if (cr == null) {
            ProfilePanel.logger.error("displayProfile called but unable to get ColumnProfileResult for column: "+c);

            // XXX the following code should instead replace chartPanel with a JLabel that contains the error message
            //     (and also not create a dummy profile result)
//            cr = new ColumnProfileResult(c, null, null);
//            cr.setCreateStartTime(0);
//            chartPanel.setChart(ChartFactory.createPieChart("", new DefaultPieDataset(), false, false, false));
            chartPanel.setChart(null);  // if this works, great!
        } else {
            chartPanel.setChart(createTopNChart(cr));
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
                double d = ((BigDecimal)o).doubleValue();
                avgValue.setText(format(d));
            } else {
                ProfilePanel.logger.debug("Got avgValue of type: " + o.getClass().getName());
                avgValue.setText(cr.getAvgValue().toString());
            }


            FreqValueCountTableModel freqValueCountTableModel = new FreqValueCountTableModel(cr);
            TableModelSortDecorator sortModel = new TableModelSortDecorator(freqValueCountTableModel);
            freqValueTable.setModel(sortModel);
            sortModel.setTableHeader(freqValueTable.getTableHeader());
            freqValueTable.initColumnSizes();
        }
    }

    private JFreeChart createTopNChart(ColumnProfileResult cr){
        JFreeChart chart;
        List<ColumnValueCount> valueCounts = cr.getValueCount();
        SQLColumn col = cr.getProfiledObject();
        DefaultCategoryDataset catDataset = new DefaultCategoryDataset();

        
        long otherDataCount = rowCount;
        for (ColumnValueCount vc: valueCounts){
            catDataset.addValue(vc.getCount(),col.getName(),vc.getValue()==null ? "null" : vc.getValue().toString());
            otherDataCount -= vc.getCount();
        }
        int numberOfTopValues = catDataset.getColumnCount();
        if (otherDataCount > 0){
            catDataset.addValue(otherDataCount,col.getName(),"Other Values");
        }

        if ( chartType == ChartTypes.BAR ){
            String chartTitle;
            if (numberOfTopValues == 10 ) {
                chartTitle = "Top "+numberOfTopValues+" most common values";
            } else {
                chartTitle = "All "+numberOfTopValues+" values";
            }

            chart = ChartFactory.createBarChart(
                    chartTitle,
                    "","",catDataset,PlotOrientation.VERTICAL,
                    false,true,false);

            final CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
        } else if (chartType == ChartTypes.PIE) {

            String chartTitle;
            if (numberOfTopValues == 10 ) {
                chartTitle = "Top "+numberOfTopValues+" most common values";
            } else {
                chartTitle = "All "+numberOfTopValues+" values";
            }

            chart = ChartFactory.createPieChart(
                    chartTitle,
                    new CategoryToPieDataset(catDataset,TableOrder.BY_ROW,0),
                    false,true,false);
            if (chart.getPlot() instanceof PiePlot) {
                ((PiePlot)chart.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0} [{1}]"));
            }
        } else {

            throw new IllegalStateException("chart type "+chartType+" not recognized");
        }
        return chart;
    }

    public ChartTypes getChartType() {
        return chartType;
    }

    public void setChartType(ChartTypes chartType) {
        this.chartType = chartType;
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
}