package ca.sqlpower.architect.profile;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.profile.ColumnProfileResult.ColumnValueCount;
import ca.sqlpower.sql.ColumnNotDisplayableException;
import ca.sqlpower.sql.FieldTypes;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
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

public class ProfilePDFFormat {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);
    
    public ProfilePDFFormat() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Outputs a PDF file report of the data in drs to the given
     * output stream.
     * @throws ArchitectException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static void createPdf(OutputStream out,
                                 java.util.List<SQLTable> tables,
                                 ProfileManager pm)
                throws DocumentException, IOException, SQLException, 
                    ArchitectException, InstantiationException, IllegalAccessException {
        
        final int minRowsTogether = 5;  // counts smaller than this are considered orphan/widow
        final int mtop = 50;  // margin at top of page (in points)
        final int mbot = 50;  // margin at bottom of page (page numbers are below this)
        final int pbot = 20;  // padding between bottom margin and bottom of body text
        final int mlft = 50;  // margin at left side of page
        final int mrgt = 50;  // margin at right side of page
        final Rectangle pagesize = PageSize.LETTER.rotate();
        final Document document = new Document(pagesize, mlft, mrgt, mtop, mbot);
        final PdfWriter writer = PdfWriter.getInstance(document, out);

        final float fsize = 12f; // the font size to use in the table body
        final BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        document.addTitle("Table Profiling Report");
        document.addSubject("Tables: " + tables);
        document.addAuthor(System.getProperty("user.name"));
        document.addCreator("Power*Architect version "+ArchitectUtils.APP_VERSION);
    
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

        int vcols = 18;
        float[] widths = new float[vcols];  // widths of widest cells per row in pdf table
        LinkedList<PdfPTable> profiles = new LinkedList<PdfPTable>(); // 1 table per profile result
        for (SQLTable t : tables) {
            ProfileResult tpr = pm.getResult(t);
            PdfPTable table = makeNextTable(pm,t, bf, fsize, widths);
            profiles.add(table);
        }

        // add the PdfPTables to the document; try to avoid orphan and widow rows
        pos = writer.getVerticalPosition(true) - fsize;
        logger.debug("Starting at pos="+pos);
        for (PdfPTable table : profiles) {
            table.setTotalWidth(pagesize.width() - mrgt - mlft);
            table.setWidths(widths);
            int startrow = table.getHeaderRows();
            int endrow = startrow; // current page will contain header+startrow..endrow
            while (endrow < table.size()) {

                // figure out how many body rows fit nicely on the page
                float endpos = pos - calcHeaderHeight(table);
                while (endpos > (mbot + pbot) && endrow < table.size() ) {
                    endpos -= table.getRowHeight(endrow);
                    endrow++;
                }

                // adjust for orphan rows. Might create widows or make 
                // endrow < startrow, which is handled later by deferring the table
                if (endrow < table.size() && endrow + minRowsTogether >= table.size()) {
                    if (endrow + 1 == table.size()) {
                        // short by 1 row.. just squeeze it in
                        endrow = table.size();
                    } else {
                        // more than 1 row remains: shorten this page so orphans aren't lonely
                        endrow = table.size() - minRowsTogether;
                    }
                }

                if (endrow == table.size() || endrow - startrow > minRowsTogether) {
                    // this is the end of the table, or we have enough rows to bother printing
                    pos = table.writeSelectedRows(0, table.getHeaderRows(), mlft, pos, cb);
                    pos = table.writeSelectedRows(startrow, endrow, mlft, pos, cb);
                    startrow = endrow;
                } else {
                    // not the end of the table and not enough rows to print out
                    endrow = startrow;
                }

                // new page if necessary (that is, when we aren't finished the table yet)
                if (endrow != table.size()) {
                    document.newPage();
                    pos = pagesize.height() - mtop;
                }
            }
        }

        document.close();
    }

    /**
     * Calculates the total height of all header rows in the given table.
     */
    private static float calcHeaderHeight(PdfPTable table) {
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
    protected static PdfPTable makeNextTable(ProfileManager pm, SQLTable sqlTable,
                                             BaseFont bf, float fsize, float[] widths) 
            throws DocumentException, IOException, ArchitectException,
                    SQLException, InstantiationException, IllegalAccessException {

        logger.debug("Making next table");

        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100f);

        Font f = new Font(bf, fsize);

        TableProfileResult tProfile = (TableProfileResult) pm.getResult(sqlTable);
        addHeaderRow(table, bf, f, fsize, widths);
        
        // body rows
        for (SQLColumn col : sqlTable.getColumns()) {
            addBodyRow(pm, col, table, bf, f, fsize, widths);
        }

        logger.debug("Finished body rows");

        table.setWidths(widths);

        return table;
    }

    /**
     * @param widths The maximum width of each column's contents in
     * points.  THIS ARRAY WILL BE MODIFIED to the width of the widest
     * single word in the heading if it is wider than the existing
     * width value for that column.  Words are split using the default
     * settings for java.util.StringTokenizer.
     */
    private static void addHeaderRow(PdfPTable table,
                                       BaseFont bf, Font f, float fsize, float[] widths) 
        throws DocumentException, IOException, ArchitectException {
        
        final String[] headings = new String[] {
                "Table Name",
                "Record Count",
                "Column Name",
                "Data Type",
                "Nullable",
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
        int ncols = headings.length;

        for (int colNo = 0; colNo < ncols; colNo++) {
            String contents = headings[colNo];
            
            // ensure column width is at least enough for widest word in heading
            StringTokenizer st = new StringTokenizer(contents);
            while (st.hasMoreTokens()) {
                widths[colNo] = Math.max(widths[colNo],
                        bf.getWidthPoint(st.nextToken(), fsize));
            }
            
            Phrase colTitle = new Phrase(contents, f);
            PdfPCell cell = new PdfPCell(colTitle);
            cell.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
            cell.setBorderWidth(2);
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        
        table.setHeaderRows(1);
    }

    protected static void addBodyRow(ProfileManager pm, SQLColumn col, PdfPTable table,
                                     BaseFont bf, Font f, float fsize, float[] widths) 
        throws DocumentException, IOException, ArchitectException,
                SQLException, InstantiationException, IllegalAccessException {

        TableProfileResult tProfile = (TableProfileResult) pm.getResult(col.getParentTable());
        ColumnProfileResult cProfile = (ColumnProfileResult) pm.getResult(col);

        
        DDLGenerator gddl = DDLUtils.createDDLGenerator(
                col.getParentTable().getParentDatabase().getDataSource());

        DecimalFormat pctFormat = new DecimalFormat("0%");
        
        int rowCount = -1;
        if ( tProfile != null && tProfile.isError() != true ) {
            rowCount = tProfile.getRowCount();
        }
        java.util.List<ColumnValueCount> topTen = null;
        if ( cProfile != null && cProfile.isError() != true ) {
            topTen = cProfile.getValueCount();
        }
        
        final int ncols = 17;
        for (int colNo = 0; colNo < ncols; colNo++) {
            
            String contents;
            int alignment;
            if (colNo == 0) {
                if ( tProfile == null || tProfile.isError() ) {
                    contents = col.getParentTable().getName() + "\nProfiling Error:\n";
                    if ( tProfile != null && tProfile.getEx() != null )
                        contents += tProfile.getEx();
                }
                else {
                    contents = col.getParentTable().getName();
                }
                alignment = Element.ALIGN_LEFT;
            } else if (colNo == 1) {
                contents = String.valueOf(rowCount);
                alignment = Element.ALIGN_RIGHT;
            } else if (colNo == 2) {
                contents = col.getName();
                alignment = Element.ALIGN_LEFT;
            } else if (colNo == 3) {
                contents = gddl.columnType(col);
                alignment = Element.ALIGN_LEFT;
            } else if (colNo == 4) {
                contents = col.isDefinitelyNullable() ? "Y" : "N";
                alignment = Element.ALIGN_CENTER;
            } else if (colNo == 5) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getNullCount());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "Column Profiling Error";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 6) {
                if ( cProfile != null && !cProfile.isError() ) {
                    if (rowCount == 0) {
                        contents = "N/A";
                        alignment = Element.ALIGN_CENTER;
                    } else {
                        contents = pctFormat.format(cProfile.getNullCount() / (double) rowCount);
                        alignment = Element.ALIGN_RIGHT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 7) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getDistinctValueCount());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 8) {
                if ( cProfile != null && !cProfile.isError() ) {
                    if (rowCount == 0) {
                        contents = "N/A";
                        alignment = Element.ALIGN_CENTER;
                    } else {
                        contents = pctFormat.format(cProfile.getDistinctValueCount() / (double) rowCount);
                        alignment = Element.ALIGN_RIGHT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 9) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getMinLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 10) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getMaxLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 11) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getAvgLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 12) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getMinValue());
                    if (cProfile.getMinValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                    } else if (cProfile.getMinValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                    } else {
                        alignment = Element.ALIGN_LEFT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 13) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getMaxValue());
                    if (cProfile.getMaxValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                    } else if (cProfile.getMaxValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                    } else {
                        alignment = Element.ALIGN_LEFT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 14) {
                if ( cProfile != null && !cProfile.isError() ) {
                    contents = String.valueOf(cProfile.getAvgValue());
                    if (cProfile.getAvgValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                    } else if (cProfile.getAvgValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                    } else {
                        alignment = Element.ALIGN_LEFT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 15) {
                if ( cProfile != null && !cProfile.isError() && topTen != null ) {
                    Object value = null;
                    StringBuffer sb = new StringBuffer();
                    for ( ColumnValueCount cvc : topTen ) {
                        value = cvc.getValue();
                        sb.append(value).append("\n");
                    }
                    contents = sb.toString();
                    if (value == null) {
                        alignment = Element.ALIGN_CENTER;
                    } else if (value instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                    } else {
                        alignment = Element.ALIGN_LEFT;
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if (colNo == 16) {
                if ( cProfile != null && !cProfile.isError() ) {
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
            
            
            // update column width to reflect the widest cell
            widths[colNo] = Math.max(widths[colNo],
                                      bf.getWidthPoint((String) contents, fsize));
            
            Phrase phr = new Phrase((String) contents, f);
            PdfPCell cell = new PdfPCell(phr);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(alignment);
            table.addCell(cell);
        }
    }

}
