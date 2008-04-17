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
package ca.sqlpower.architect.profile.output;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ColumnValueCount;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class ProfilePDFFormat implements ProfileFormat {
    
    /**
     * The approximate border length of a pdfBorder. This is hard coded here
     * because the table appears to have no way of getting this information.
     * This value can be changed if things do not come out looking right
     */
    private static final int PIXELS_PER_BORDER = 6;

    private static final Logger logger = Logger.getLogger(ProfilePDFFormat.class);
    private int totalColumn;


    private final String[] headings = new String[] {
            "Column Name",
            "Data Type",
            "Null Count",
            "% null",
            "Unique Count",
            "% unique",
            "Min Length",
            "Max Length",
            "Avg Length",
            "Min Value",
            "Max Value",
            "Avg Value",
            "Top N Values",
            "Count"
    };
    private int maxCharsInTopN = 50;
    
    private List<String> columnsToTruncate = new ArrayList<String>();
    
    /**
     * The length to truncate the cells by.
     */
    private double truncateLength = -1;

    public ProfilePDFFormat() {
        super();
        totalColumn = headings.length;
        columnsToTruncate.add("Min Value");
        columnsToTruncate.add("Max Value");
        columnsToTruncate.add("Top N Values");
    }

    /**
     * This is the maximum number of characters that can appear in a "top n" value.
     * Anything in excess of this number of characters will be truncated and replaced
     * by an ellipsis.
     */

    /**
     * Outputs a PDF file report of the data in drs to the given
     * output stream.
     * @throws ArchitectException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void format(OutputStream out, List<ProfileResult> profileResults)
                throws DocumentException, IOException, SQLException,
                    ArchitectException, InstantiationException,
                    IllegalAccessException, ClassNotFoundException {

        final int minRowsTogether = 1;  // counts smaller than this are considered orphan/widow
        final int mtop = 50;  // margin at top of page (in points)
        final int mbot = 50;  // margin at bottom of page (page numbers are below this)
//        final int pbot = 20;  // padding between bottom margin and bottom of body text
        final int mlft = 50;  // margin at left side of page
        final int mrgt = 50;  // margin at right side of page
        final Rectangle pagesize = PageSize.LETTER.rotate();
        final Document document = new Document(pagesize, mlft, mrgt, mtop, mbot);
        final PdfWriter writer = PdfWriter.getInstance(document, out);

        final float fsize = 6f; // the font size to use in the table body
        final BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        document.addTitle("Table Profiling Report");
        document.addSubject("Tables: " + profileResults);
        document.addAuthor(System.getProperty("user.name"));
        document.addCreator("Power*Architect version "+ArchitectVersion.APP_VERSION);

        document.open();

        // vertical position where next element should start
        //   (bottom is 0; top is pagesize.height())
        float pos = pagesize.height() - mtop;

        final PdfContentByte cb = writer.getDirectContent();
        final PdfTemplate nptemplate = cb.createTemplate(50, 50);
        writer.setPageEvent(new PdfPageEventHelper() {
                // prints the "page N of <template>" footer
                public void onEndPage(PdfWriter writer, Document document) {
                    int pageN = writer.getPageNumber();
                    String text = "Page " + pageN + " of ";
                    float len = bf.getWidthPoint(text, fsize-2);
                    cb.beginText();
                    cb.setFontAndSize(bf, fsize-2);
                    cb.setTextMatrix(pagesize.width()/2 - len/2, mbot/2);
                    cb.showText(text);
                    cb.endText();
                    cb.addTemplate(nptemplate, pagesize.width()/2 - len/2 + len, mbot/2);
                }

                public void onCloseDocument(PdfWriter writer, Document document) {
                    nptemplate.beginText();
                    nptemplate.setFontAndSize(bf, fsize-2);
                    nptemplate.showText(String.valueOf(writer.getPageNumber() - 1));
                    nptemplate.endText();
                }
            });

        document.add(new Paragraph("Power*Architect Profiling Report"));
        document.add(new Paragraph("Generated "+new java.util.Date()
                                   +" by "+System.getProperty("user.name")));

        float[] widths = new float[totalColumn];  // widths of widest cells per row in pdf table
        LinkedList<ProfileTableStructure> profiles = new LinkedList<ProfileTableStructure>(); // 1 table per profile result

        Font f = new Font(bf, fsize);

        // This ddl generator is set to the appropriate ddl generator for the source database
        // every time we encounter a table profile result in the list.
        DDLGenerator ddlg = null;
        
        PdfPTable pdfTable = null;
        for (ProfileResult result : profileResults ) {
            if ( result instanceof TableProfileResult ) {
                TableProfileResult tableResult = (TableProfileResult) result;
                pdfTable = new PdfPTable(widths.length);
                pdfTable.setWidthPercentage(100f);
                ProfileTableStructure oneProfile = makeNextTable(
                        tableResult, pdfTable, bf, fsize, widths);
                profiles.add(oneProfile);
                ddlg = tableResult.getDDLGenerator();
            } else if ( result instanceof ColumnProfileResult ) {
                final ColumnProfileResult columnResult = (ColumnProfileResult) result;
                TableProfileResult tResult = columnResult.getParentResult();
                addBodyRow(tResult,columnResult, ddlg, pdfTable, bf, f, fsize, widths);
            }
        }
        
        double allowedTableSize = pagesize.width() - mrgt - mlft;
        double totalWidths = 0;
        for (int i = 0; i < headings.length; i++) {
            if (!columnsToTruncate.contains(headings[i])) {
                widths[i] += PIXELS_PER_BORDER;
                totalWidths += widths[i];
            }
        }
        truncateLength = (allowedTableSize - totalWidths - (PIXELS_PER_BORDER * (columnsToTruncate.size()))) / columnsToTruncate.size();
        logger.debug("Truncate length is " + truncateLength);
        widths = new float[totalColumn]; 
        
        profiles = new LinkedList<ProfileTableStructure>(); // 1 table per profile result
        for (ProfileResult result : profileResults ) {
            if ( result instanceof TableProfileResult ) {
                TableProfileResult tableResult = (TableProfileResult) result;
                pdfTable = new PdfPTable(widths.length);
                pdfTable.setWidthPercentage(100f);
                ProfileTableStructure oneProfile = makeNextTable(
                        tableResult, pdfTable, bf, fsize, widths);
                profiles.add(oneProfile);
                ddlg = tableResult.getDDLGenerator();
            } else if ( result instanceof ColumnProfileResult ) {
                final ColumnProfileResult columnResult = (ColumnProfileResult) result;
                TableProfileResult tResult = columnResult.getParentResult();
                addBodyRow(tResult,columnResult, ddlg, pdfTable, bf, f, fsize, widths);
            }
        }
        
        for (int i = 0; i < headings.length; i++) {
            widths[i] += PIXELS_PER_BORDER;
        }

        // add the PdfPTables to the document; try to avoid orphan and widow rows
        pos = writer.getVerticalPosition(true) - fsize;
        logger.debug("Starting at pos="+pos);
        boolean newPageInd = true;

        for ( ProfileTableStructure profile : profiles) {

            pdfTable = profile.getMainTable();
            pdfTable.setTotalWidth(pagesize.width() - mrgt - mlft);
            pdfTable.setWidths(widths);
            resetHeaderWidths(profile,widths);

            int startrow = pdfTable.getHeaderRows();
            int endrow = startrow; // current page will contain header+startrow..endrow

            /* no other rows in the table, just the header, and the header may
             * contain error message
             */
            if (endrow == pdfTable.size()) {
                pos = pdfTable.writeSelectedRows(0, pdfTable.getHeaderRows(), mlft, pos, cb);
                continue;
            }

            while (endrow < pdfTable.size()) {

                // figure out how many body rows fit nicely on the page
                float endpos = pos - calcHeaderHeight(pdfTable);

                // y position of page number# = (mbot/2+fsize)
                while ( (endpos-pdfTable.getRowHeight(endrow)) >= (mbot/2+fsize+2) &&
                        endrow < pdfTable.size() ) {
                    endpos -= pdfTable.getRowHeight(endrow);
                    endrow++;
                }


                // adjust for orphan rows. Might create widows or make
                // endrow < startrow, which is handled later by deferring the table
                if (endrow < pdfTable.size() && endrow + minRowsTogether >= pdfTable.size()) {

                    // page # maybe fall into table area, but usually that's column of
                    // min value, usually that's enough space for both, or we should
                    // disable page # on this page
                    if (endrow + 1 == pdfTable.size() &&
                        endpos - pdfTable.getRowHeight(endrow) > 10 ) {

                        // short by 1 row.. just squeeze it in
                        endrow = pdfTable.size();
                    } else {
                        // more than 1 row remains: shorten this page so orphans aren't lonely
                        endrow = pdfTable.size() - minRowsTogether;
                    }
                }

                if (endrow == pdfTable.size() || endrow - startrow >= minRowsTogether) {
                    // this is the end of the table, or we have enough rows to bother printing
                    pos = pdfTable.writeSelectedRows(0, pdfTable.getHeaderRows(), mlft, pos, cb);
                    pos = pdfTable.writeSelectedRows(startrow, endrow, mlft, pos, cb);
                    startrow = endrow;
                    newPageInd = false;
                } else {
                    // not the end of the table and not enough rows to print out
                    if ( newPageInd )
                        throw new IllegalStateException("PDF Page is not large engouh to display "+minRowsTogether+" row(s)");
                    endrow = startrow;
                }

                // new page if necessary (that is, when we aren't finished the table yet)
                if (endrow != pdfTable.size()) {
                    document.newPage();
                    pos = pagesize.height() - mtop;
                    newPageInd = true;
                }
            }
        }
        document.close();
    }

    /**
     * Calculates the total height of all header rows in the given table.
     */
    private float calcHeaderHeight(PdfPTable table) {
        int hrows = table.getHeaderRows();
        float height = 0f;
        for (int i = 0; i < hrows; i++) {
            height += table.getRowHeight(i);
        }
        return height;
    }

    /**
     * Creates a PdfPTable of the data in profile from its current cursor
     * position up to the end of the current break.  The table
     * includes a header, body rows, and a subtotal row.
     *
     * @param tProfile the profile result for the SQLTable in question
     * @param bf The BaseFont (typeface) to use for all table text
     * @param fsize The size (in points) of the table body text
     * @param widths The maximum width of each visible column in the
     * PDF so far, in points.  THIS ARRAY WILL BE MODIFIED to reflect
     * the new maxmimum column widths if any new cells are added that
     * are wider than the sizes in the array.  If you are making a
     * series of tables that should all have the same column widths,
     * re-use the same array for each call to makeNextTable, then set
     * each table's column widths using the final resulting widths
     * array.  If you want each table to have its own optimal widths,
     * use a new array for each invocation.
     * @throws ArchitectException
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected ProfileTableStructure makeNextTable(TableProfileResult result,
                                                PdfPTable table,
                                                BaseFont bf,
                                                float fsize,
                                                float[] widths)
            throws DocumentException, IOException, ArchitectException,
                    SQLException, InstantiationException, IllegalAccessException {

        logger.debug("Making next table");
        float titleFSize = fsize * 1.5f;
        float colHeadingFSize = fsize;
        ProfileTableStructure profile = new ProfileTableStructure(table);
        addHeaderRow( result, profile, bf, titleFSize, colHeadingFSize, widths );
        logger.debug("Finished body rows");
        table.setWidths(widths);
        return profile;
    }

    /**
     * @param widths The maximum width of each column's contents in
     * points.  THIS ARRAY WILL BE MODIFIED to the width of the widest
     * single word in the heading if it is wider than the existing
     * width value for that column.  Words are split using the default
     * settings for java.util.StringTokenizer.
     * @param headerTopNColumns reference to the null count/% inner table in the header
     * @param headerValueColumns reference to the unique count/% inner table in the header
     * @param headerLengthColumns reference to the length min/max/avg inner table in the header
     * @param headerUniqueColumns reference to the value min/max/avg inner table in the header
     * @param headerNullColumns reference to the top N Value/count inner table in the header
     * we will resert widths of these inner table after we have all rows
     */
    private void addHeaderRow(TableProfileResult result,
                            ProfileTableStructure profile,
                            BaseFont bf,
                            float titleFSize,
                            float colHeadingFSize,
                            float[] widths )
        throws DocumentException, IOException, ArchitectException {


        int ncols = headings.length;

        Font titleFont = new Font(bf, titleFSize, Font.BOLD);
        Font colHeadingFont = new Font(bf, colHeadingFSize);
        PdfPTable table = profile.getMainTable();
        SQLTable sqlTable = result.getProfiledObject();

//        TableProfileResult tProfile = (TableProfileResult) pm.getResult(sqlTable);
        PdfPTable infoTable = new PdfPTable(2);
        StringBuffer heading = new StringBuffer();
        heading.append("Connection: ").append(sqlTable.getParentDatabase().getName()).append("\n");
        heading.append("Table: ").append(ArchitectUtils.toQualifiedName(sqlTable, SQLDatabase.class));
        if ( result.getException() != null ) {
            heading.append(" Profiling Error");
            if ( result.getException() != null )
                heading.append(":\n").append(result.getException());
        }
        else {
            PdfPCell infoCell;

            infoCell = new PdfPCell(new Phrase("Row Count:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);

            infoCell = new PdfPCell(new Phrase(String.valueOf(result.getRowCount()), colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);

            infoCell = new PdfPCell(new Phrase("Create Date:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            infoCell = new PdfPCell(new Phrase(df.format(new Date(result.getCreateStartTime())), colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);

            infoCell = new PdfPCell(new Phrase("Elapsed:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);

            infoCell = new PdfPCell(new Phrase(result.getTimeToCreate()+"ms", colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);
        }

        PdfPCell hcell = new PdfPCell(new Phrase(heading.toString(), titleFont));
        hcell.setColspan(ncols - 3);
        hcell.setBorder(Rectangle.NO_BORDER);
        hcell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(hcell);

        hcell = new PdfPCell(infoTable);
        hcell.setColspan(3);
        hcell.setBorder(Rectangle.NO_BORDER);
        table.addCell(hcell);

        if ( sqlTable.getColumns().size() > 0 ) {

            int colNo = 0;
            // column name
            Phrase colTitle = new Phrase("Column Name", colHeadingFont);
            PdfPCell cell = new PdfPCell(colTitle);
            cell.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
            cell.setBorderWidth(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            // ensure column width is at least enough for widest word in heading
            widths[colNo] = Math.max(widths[colNo],
                        bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;


            // date type
            colTitle = new Phrase("Data Type", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
            cell.setBorderWidth(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            // null count and %
            colTitle = new Phrase("NULL", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setColspan(2);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableNullColumn().addCell(cell);

            colTitle = new Phrase("#", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableNullColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("%", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableNullColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            cell = new PdfPCell(profile.getInnerTableNullColumn());
            cell.setColspan(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setBorderWidth(2);
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            table.addCell(cell);

            // unique count and %
            colTitle = new Phrase("Unique", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(2);
            profile.getInnerTableUniqueColumn().addCell(cell);

            colTitle = new Phrase("#", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableUniqueColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("%", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableUniqueColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            cell = new PdfPCell(profile.getInnerTableUniqueColumn());
            cell.setColspan(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            cell.setBorderWidth(2);
            table.addCell(cell);

            // length max/min/avg
            colTitle = new Phrase("Length", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(3);
            profile.getInnerTableLengthColumn().addCell(cell);

            colTitle = new Phrase("Min", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableLengthColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("Max", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableLengthColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("Avg", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableLengthColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            cell = new PdfPCell(profile.getInnerTableLengthColumn());
            cell.setColspan(3);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setBorderWidth(2);
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            table.addCell(cell);

            // value max/min/avg
            colTitle = new Phrase("Value", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(3);
            profile.getInnerTableValueColumn().addCell(cell);

            colTitle = new Phrase("Min", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableValueColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("Max", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableValueColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("Avg", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableValueColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            cell = new PdfPCell(profile.getInnerTableValueColumn());
            cell.setColspan(3);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setBorderWidth(2);
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            table.addCell(cell);

            // top n
            colTitle = new Phrase("Top N", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(2);
            profile.getInnerTableTopNColumn().addCell(cell);

            colTitle = new Phrase("Values", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableTopNColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            colTitle = new Phrase("#", colHeadingFont);
            cell = new PdfPCell(colTitle);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            profile.getInnerTableTopNColumn().addCell(cell);
            widths[colNo] = Math.max(widths[colNo],
                    bf.getWidthPoint(colTitle.content(), colHeadingFSize));
            colNo++;

            cell = new PdfPCell(profile.getInnerTableTopNColumn());
            cell.setColspan(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setBorderWidth(2);
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            table.addCell(cell);

        }
        else {
            hcell = new PdfPCell(new Phrase("No Column Found in the table", titleFont));
            hcell.setColspan(ncols);
            hcell.setBorder(Rectangle.BOTTOM);
            hcell.setVerticalAlignment(Element.ALIGN_LEFT);
            table.addCell(hcell);
        }
        table.setHeaderRows(2);
    }

    /**
     * @param ProfileTableStructure the structure of main and 5 inner tables
     * @param widths The maximum width of each column's contents in
     * points.
     * @param totalWidthInPoint = (page width - margins)
     * resert table header row column widths, after we have generate table rows
     * @throws DocumentException
     */
    private void resetHeaderWidths( ProfileTableStructure profile, float[] widths )
                        throws DocumentException {

        resetTableHeaderWidths(profile.getInnerTableNullColumn(),widths,2,3);
        resetTableHeaderWidths(profile.getInnerTableUniqueColumn(),widths,4,5);
        resetTableHeaderWidths(profile.getInnerTableLengthColumn(),widths,6,8);
        resetTableHeaderWidths(profile.getInnerTableValueColumn(),widths,9,11);
        resetTableHeaderWidths(profile.getInnerTableTopNColumn(),widths,12,13);
    }

    private void resetTableHeaderWidths( PdfPTable table, float[] widths,
                 int start, int end ) throws DocumentException {

        float[] headerWidths = new float[end-start+1];
        for ( int i=start; i<=end; i++ ) {
            headerWidths[i-start] = widths[i];
        }

        if ( table != null ) {
            table.setWidths(headerWidths);
        }
    }

    private void addBodyRow( TableProfileResult tProfile,
                                ColumnProfileResult result,
                                DDLGenerator ddlg,
                                PdfPTable table,
                                BaseFont bf,
                                Font f,
                                float fsize,
                                float[] widths)
        throws DocumentException, IOException, ArchitectException, SQLException {

        SQLColumn col = result.getProfiledObject();
        

        int rowCount = -1;
        if ( tProfile != null && tProfile.getException() == null ) {
            rowCount = tProfile.getRowCount();
        }
        java.util.List<ColumnValueCount> topTen = null;

        boolean errorColumnProfiling = false;
        Exception columnException = null;
        if ( result != null && result.getException() == null ) {
            topTen = result.getValueCount();
        }
        else {
            errorColumnProfiling = true;
            if ( result != null && result.getException() != null )
                columnException = result.getException();
        }

        DecimalFormat pctFormat = new DecimalFormat("0%");

        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setMaximumFractionDigits(col.getScale());
        df.setMinimumFractionDigits(col.getScale());

        DecimalFormat adf = new DecimalFormat("#,##0.00");
        adf.setMaximumFractionDigits(Math.max(2,col.getScale()));
        adf.setMinimumFractionDigits(Math.max(2,col.getScale()));

        DecimalFormat aldf = new DecimalFormat("#,##0.0");
        aldf.setMaximumFractionDigits(1);
        aldf.setMinimumFractionDigits(0);

        for (int colNo = 0; colNo < totalColumn; colNo++) {

            String contents;
            int alignment;

            if ( headings[colNo].equalsIgnoreCase("table name") ) {
                String fqTableName = ArchitectUtils.toQualifiedName(col.getParentTable(), SQLDatabase.class);
                if ( tProfile == null || tProfile.getException() != null) {
                    contents = fqTableName + "\nProfiling Error:\n";
                    if ( tProfile != null && tProfile.getException() != null )
                        contents += tProfile.getException();
                }
                else {
                    contents = fqTableName;
                }
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("row count") ) {
                contents = String.valueOf(rowCount);
                alignment = Element.ALIGN_RIGHT;
            } else if ( headings[colNo].equalsIgnoreCase("column name") ) {
                contents = col.getName();
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("data type") ) {
                contents = ddlg.columnType(col);
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("null count") ) {

                if ( errorColumnProfiling ) {
                    if ( result == null ) {
                        contents = "Column Profiling Not Found\n";
                    }
                    else {
                        contents = "Column Profiling Error:\n";
                        if ( columnException != null )
                            contents += columnException;
                    }
                    alignment = Element.ALIGN_LEFT;
                }
                else {
                    if ( col.isDefinitelyNullable() ) {
                        contents = String.valueOf(result.getNullCount());
                    }
                    else {
                        contents = "---";
                    }
                    alignment = Element.ALIGN_RIGHT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("% null") ) {
                if ( errorColumnProfiling ) {
                    continue;
                }
                else {
                    if ( col.isDefinitelyNullable() ) {
                        if (rowCount <= 0) {
                            contents = "N/A";
                            alignment = Element.ALIGN_CENTER;
                        } else {
                            contents = pctFormat.format(result.getNullCount() / (double) rowCount);
                            alignment = Element.ALIGN_RIGHT;
                        }
                    }
                    else {
                        contents = "---";
                        alignment = Element.ALIGN_CENTER;
                    }
                }
            } else if ( headings[colNo].equalsIgnoreCase("Unique Count") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(result.getDistinctValueCount());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    continue;
                }
            } else if ( headings[colNo].equalsIgnoreCase("% unique") ) {
                if ( !errorColumnProfiling ) {
                    if (rowCount == 0) {
                        contents = "N/A";
                        alignment = Element.ALIGN_CENTER;
                    } else {
                        contents = pctFormat.format(result.getDistinctValueCount() / (double) rowCount);
                        alignment = Element.ALIGN_RIGHT;
                    }
                }
                else {
                    continue;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Min Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(result.getMinLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Max Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(result.getMaxLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("avg Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = aldf.format(result.getAvgLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Min value") ) {
                if ( !errorColumnProfiling ) {
                    if (result.getMinValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (result.getMinValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = df.format((Number)result.getMinValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(result.getMinValue());
                    }
                    alignment = Element.ALIGN_LEFT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Max value") ) {
                if ( !errorColumnProfiling ) {
                    if (result.getMaxValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (result.getMaxValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = df.format((Number)result.getMaxValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(result.getMaxValue());
                    }
                    alignment = Element.ALIGN_LEFT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("avg value") ) {
                if ( !errorColumnProfiling ) {
                    if (result.getAvgValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (result.getAvgValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = adf.format((Number)result.getAvgValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(result.getAvgValue());
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Top N Values") ) {
                if ( !errorColumnProfiling && topTen != null ) {
                    StringBuffer sb = new StringBuffer();
                    for ( ColumnValueCount cvc : topTen ) {
                        sb.append(cvc.getValue()).append("\n");
                    }
                    contents = sb.toString();
                    alignment = Element.ALIGN_LEFT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Count") ) {
                if ( !errorColumnProfiling ) {
                    StringBuffer sb = new StringBuffer();
                    for ( ColumnValueCount cvc : topTen ) {
                        sb.append(cvc.getCount()).append("\n");
                    }
                    contents = sb.toString();
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else {
                throw new IllegalStateException("I don't know about column "+colNo);
            }

            StringBuffer truncContents = new StringBuffer(contents.length());

            // update column width to reflect the widest cell
            for (String contentLine : contents.split("\n")) {
                String newLine;
                if (truncateLength >= 0) {
                    if (bf.getWidthPoint(contentLine, fsize) < truncateLength) {
                        newLine = contentLine + "\n";
                    } else {
                        double currentLength = bf.getWidthPoint("...", fsize);
                        int stringPosition = 0;
                        for (; stringPosition < contentLine.length(); stringPosition++) {
                            if (currentLength > truncateLength) {
                                break;
                            }
                            currentLength = bf.getWidthPoint(contentLine.substring(0, stringPosition) + "...", fsize); 
                            stringPosition++;
                        }
                        newLine = contentLine.substring(0, stringPosition - 1) + "...\n";
                    }
                } else {
                    newLine = contentLine + "\n";
                }
                truncContents.append(newLine);
                widths[colNo] = Math.max(widths[colNo],
                                      bf.getWidthPoint(newLine, fsize));
                logger.debug("width is now " + widths[colNo] + " for column " + colNo);
            }


            PdfPCell cell;
            if ( headings[colNo].equalsIgnoreCase("Top N Values") ||
                 headings[colNo].equalsIgnoreCase("Count") ) {
                cell = new PdfPCell(new Paragraph(truncContents.toString(), f));
                cell.setNoWrap(true);
            } else if ( headings[colNo].equalsIgnoreCase("null count") &&
                        errorColumnProfiling ) {
                cell = new PdfPCell(new Paragraph(truncContents.toString(), f));
                cell.setColspan(4);
            }
            else {
                Phrase phr = new Phrase(truncContents.toString(), f);
                cell = new PdfPCell(phr);

            }
//            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(alignment);
            table.addCell(cell);
        }
    }

    private class ProfileTableStructure {

        private PdfPTable innerTableNullColumn = null;
        private PdfPTable innerTableUniqueColumn = null;
        private PdfPTable innerTableLengthColumn = null;
        private PdfPTable innerTableValueColumn = null;
        private PdfPTable innerTableTopNColumn = null;
        private PdfPTable mainTable = null;

        public ProfileTableStructure(PdfPTable mainTable) {
            this.mainTable = mainTable;
            innerTableNullColumn = new PdfPTable(2);
            innerTableUniqueColumn = new PdfPTable(2);
            innerTableLengthColumn = new PdfPTable(3);
            innerTableValueColumn = new PdfPTable(3);
            innerTableTopNColumn = new PdfPTable(2);
        }

        public PdfPTable getInnerTableLengthColumn() {
            return innerTableLengthColumn;
        }
        public void setInnerTableLengthColumn(PdfPTable innerTableLengthColumn) {
            this.innerTableLengthColumn = innerTableLengthColumn;
        }
        public PdfPTable getInnerTableNullColumn() {
            return innerTableNullColumn;
        }
        public void setInnerTableNullColumn(PdfPTable innerTableNullColumn) {
            this.innerTableNullColumn = innerTableNullColumn;
        }
        public PdfPTable getInnerTableTopNColumn() {
            return innerTableTopNColumn;
        }
        public void setInnerTableTopNColumn(PdfPTable innerTableTopNColumn) {
            this.innerTableTopNColumn = innerTableTopNColumn;
        }
        public PdfPTable getInnerTableUniqueColumn() {
            return innerTableUniqueColumn;
        }
        public void setInnerTableUniqueColumn(PdfPTable innerTableUniqueColumn) {
            this.innerTableUniqueColumn = innerTableUniqueColumn;
        }
        public PdfPTable getInnerTableValueColumn() {
            return innerTableValueColumn;
        }
        public void setInnerTableValueColumn(PdfPTable innerTableValueColumn) {
            this.innerTableValueColumn = innerTableValueColumn;
        }
        public PdfPTable getMainTable() {
            return mainTable;
        }
        public void setMainTable(PdfPTable mainTable) {
            this.mainTable = mainTable;
        }

    }
}
