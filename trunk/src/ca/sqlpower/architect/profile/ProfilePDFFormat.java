package ca.sqlpower.architect.profile;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.profile.ColumnProfileResult.ColumnValueCount;

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


public class ProfilePDFFormat {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);
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
    
    public ProfilePDFFormat() {
        super();
        totalColumn = headings.length;
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
    public void createPdf(OutputStream out,
                                 java.util.List<SQLTable> tables,
                                 ProfileManager pm)
                throws DocumentException, IOException, SQLException, 
                    ArchitectException, InstantiationException, IllegalAccessException {
        
        final int minRowsTogether = 1;  // counts smaller than this are considered orphan/widow
        final int mtop = 50;  // margin at top of page (in points)
        final int mbot = 50;  // margin at bottom of page (page numbers are below this)
        final int pbot = 20;  // padding between bottom margin and bottom of body text
        final int mlft = 50;  // margin at left side of page
        final int mrgt = 50;  // margin at right side of page
        final Rectangle pagesize = PageSize.LETTER.rotate();
        final Document document = new Document(pagesize, mlft, mrgt, mtop, mbot);
        final PdfWriter writer = PdfWriter.getInstance(document, out);

        final float fsize = 6f; // the font size to use in the table body
        final BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
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

        float[] widths = new float[totalColumn];  // widths of widest cells per row in pdf table
        LinkedList<PdfPTable> profiles = new LinkedList<PdfPTable>(); // 1 table per profile result
        for (SQLTable t : tables) {
            PdfPTable table = makeNextTable(pm,t, bf, fsize, widths);
            profiles.add(table);
        }

        // add the PdfPTables to the document; try to avoid orphan and widow rows
        pos = writer.getVerticalPosition(true) - fsize;
        logger.debug("Starting at pos="+pos);
        int x=1;
        for (PdfPTable table : profiles) {
            

            table.setTotalWidth(pagesize.width() - mrgt - mlft);
            table.setWidths(widths);
            int startrow = table.getHeaderRows();
            int endrow = startrow; // current page will contain header+startrow..endrow
System.out.println("\n\npage size="+document.getPageSize());
System.out.println("table:"+(x++)+ "  total="+table.size()+"(rows)   page="+writer.getPageNumber()+"   Starting at pos="+pos+"  header height="+calcHeaderHeight(table)+"  row="+table.getHeaderRows());
            
            if (endrow == table.size()) {
                pos = table.writeSelectedRows(0, table.getHeaderRows(), mlft, pos, cb);
                continue;
            }
            
            while (endrow < table.size()) {

                // figure out how many body rows fit nicely on the page
                float endpos = pos - calcHeaderHeight(table);
System.out.println("pos="+pos+"  endpos="+endpos);                
                while (endpos > (mbot + pbot) && endrow < table.size() ) {
                    endpos -= table.getRowHeight(endrow);
System.out.println("endrow="+ endrow+ "  endpos="+endpos+"  height="+table.getRowHeight(endrow) );                    
                    endrow++;
                }

                // adjust for orphan rows. Might create widows or make 
                // endrow < startrow, which is handled later by deferring the table
                if (endrow < table.size() && endrow + minRowsTogether >= table.size()) {
                    if (endrow + 1 == table.size()) {
System.out.println("\ntry to squeeze!!!\n");
System.out.println("table size="+table.size()+"   endrow="+endrow+ "   endpos="+endpos );
System.out.println("last row heigth="+table.getRowHeight(endrow+1)+"    this row heigth="+table.getRowHeight(endrow));

                        // short by 1 row.. just squeeze it in
                        endrow = table.size();
                    } else {
                        // more than 1 row remains: shorten this page so orphans aren't lonely
                        endrow = table.size() - minRowsTogether;
                    }
                }

                if (endrow == table.size() || endrow - startrow >= minRowsTogether) {
                    // this is the end of the table, or we have enough rows to bother printing
                    pos = table.writeSelectedRows(0, table.getHeaderRows(), mlft, pos, cb);
                    pos = table.writeSelectedRows(startrow, endrow, mlft, pos, cb);
                    startrow = endrow;
                } else {
                    // not the end of the table and not enough rows to print out
                    endrow = startrow;
//                    throw new IllegalStateException("Page is not large engouh to display "+minRowsTogether+" row(s)");
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
    protected PdfPTable makeNextTable(ProfileManager pm, SQLTable sqlTable,
                                             BaseFont bf, float fsize, float[] widths) 
            throws DocumentException, IOException, ArchitectException,
                    SQLException, InstantiationException, IllegalAccessException {

        logger.debug("Making next table");

        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100f);

        Font f = new Font(bf, fsize);
        float titleFSize = fsize * 1.5f;
        float colHeadingFSize = fsize;
        addHeaderRow(pm, sqlTable, table, bf, titleFSize, colHeadingFSize, widths);
        
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
    private void addHeaderRow(ProfileManager pm, SQLTable sqlTable, 
                                PdfPTable table, BaseFont bf, float titleFSize,
                                float colHeadingFSize, float[] widths) 
        throws DocumentException, IOException, ArchitectException {
        
        
        int ncols = headings.length;

        Font titleFont = new Font(bf, titleFSize, Font.BOLD);
        Font colHeadingFont = new Font(bf, colHeadingFSize);
        
        TableProfileResult tProfile = (TableProfileResult) pm.getResult(sqlTable);
        PdfPTable infoTable = new PdfPTable(2);
        StringBuffer heading = new StringBuffer();
        if ( tProfile.isError() ) {
            heading.append("Table: ").append(sqlTable.getName());
            heading.append(" Profiling Error");
            if ( tProfile.getEx() != null )
                heading.append(":\n").append(tProfile.getEx());
        }
        else {
            PdfPCell infoCell;
            
            heading.append("Table: ").append(sqlTable.getName());
            infoCell = new PdfPCell(new Phrase("Row Count:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);
            
            infoCell = new PdfPCell(new Phrase(String.valueOf(tProfile.getRowCount()), colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);
            
            infoCell = new PdfPCell(new Phrase("Create Date:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            infoCell = new PdfPCell(new Phrase(df.format(tProfile.getCreateDate()), colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);
            
            infoCell = new PdfPCell(new Phrase("Elapsed:",colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(infoCell);
            
            infoCell = new PdfPCell(new Phrase(tProfile.getTimeToCreate()+"ms", colHeadingFont));
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(infoCell);
        }
        
        PdfPCell hcell = new PdfPCell(new Phrase(heading.toString(), titleFont));
        hcell.setColspan(ncols - 2);
        hcell.setBorder(Rectangle.NO_BORDER);
        hcell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(hcell);
        
        hcell = new PdfPCell(infoTable);
        hcell.setColspan(2);
        hcell.setBorder(Rectangle.NO_BORDER);
        table.addCell(hcell);
        
        if ( sqlTable.getColumns().size() > 0 ) {

            for (int colNo = 0; colNo < ncols; colNo++) {
                String contents = headings[colNo];
                
                // ensure column width is at least enough for widest word in heading
                StringTokenizer st = new StringTokenizer(contents);
                while (st.hasMoreTokens()) {
                    widths[colNo] = Math.max(widths[colNo],
                            bf.getWidthPoint(st.nextToken(), colHeadingFSize));
                }
                
                Phrase colTitle = new Phrase(contents, colHeadingFont);
                PdfPCell cell = new PdfPCell(colTitle);
                cell.setBorder(Rectangle.BOTTOM | Rectangle.TOP);
                cell.setBorderWidth(2);
                cell.setBackgroundColor(new Color(200, 200, 200));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
    
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

    protected void addBodyRow(ProfileManager pm, SQLColumn col, PdfPTable table,
                                     BaseFont bf, Font f, float fsize, float[] widths) 
        throws DocumentException, IOException, ArchitectException,
                SQLException, InstantiationException, IllegalAccessException {

        TableProfileResult tProfile = (TableProfileResult) pm.getResult(col.getParentTable());
        ColumnProfileResult cProfile = (ColumnProfileResult) pm.getResult(col);
        
        DDLGenerator gddl = DDLUtils.createDDLGenerator(
                col.getParentTable().getParentDatabase().getDataSource());

        int rowCount = -1;
        if ( tProfile != null && tProfile.isError() != true ) {
            rowCount = tProfile.getRowCount();
        }
        java.util.List<ColumnValueCount> topTen = null;
        
        boolean errorColumnProfiling = false;
        Exception columnException = null;
        if ( cProfile != null && cProfile.isError() != true ) {
            topTen = cProfile.getValueCount();
        }
        else {
            errorColumnProfiling = true;
            if ( cProfile != null && cProfile.getEx() != null )
                columnException = cProfile.getEx();
        }
        
        DecimalFormat pctFormat = new DecimalFormat("0%");
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setMaximumFractionDigits(col.getScale());
        df.setMinimumFractionDigits(col.getScale());
        
        DecimalFormat adf = new DecimalFormat("#,##0.00");
        adf.setMaximumFractionDigits(Math.max(2,col.getScale()));
        adf.setMinimumFractionDigits(Math.max(2,col.getScale()));
        
        for (int colNo = 0; colNo < totalColumn; colNo++) {
            
            String contents;
            int alignment;
            
            if ( headings[colNo].equalsIgnoreCase("table name") ) {
                if ( tProfile == null || tProfile.isError() ) {
                    contents = col.getParentTable().getName() + "\nProfiling Error:\n";
                    if ( tProfile != null && tProfile.getEx() != null )
                        contents += tProfile.getEx();
                }
                else {
                    contents = col.getParentTable().getName();
                }
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("row count") ) {
                contents = String.valueOf(rowCount);
                alignment = Element.ALIGN_RIGHT;
            } else if ( headings[colNo].equalsIgnoreCase("column name") ) {
                contents = col.getName();
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("data type") ) {
                contents = gddl.columnType(col);
                alignment = Element.ALIGN_LEFT;
            } else if ( headings[colNo].equalsIgnoreCase("null count") ) {
                if ( errorColumnProfiling ) {
                    contents = "Column Profiling Error:\n";
                    if ( columnException != null )
                        contents += columnException; 
                    alignment = Element.ALIGN_LEFT;
                }
                else {
                    if ( col.isDefinitelyNullable() ) {
                        contents = String.valueOf(cProfile.getNullCount());
                    }
                    else {
                        contents = "!NULL";
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
                            contents = pctFormat.format(cProfile.getNullCount() / (double) rowCount);
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
                    contents = String.valueOf(cProfile.getDistinctValueCount());
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
                        contents = pctFormat.format(cProfile.getDistinctValueCount() / (double) rowCount);
                        alignment = Element.ALIGN_RIGHT;
                    }
                }
                else {
                    continue;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Min Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(cProfile.getMinLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Max Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(cProfile.getMaxLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("avg Length") ) {
                if ( !errorColumnProfiling ) {
                    contents = String.valueOf(cProfile.getAvgLength());
                    alignment = Element.ALIGN_RIGHT;
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Min value") ) {
                if ( !errorColumnProfiling ) {
                    if (cProfile.getMinValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (cProfile.getMinValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = df.format((Number)cProfile.getMinValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(cProfile.getMinValue());
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("Max value") ) {
                if ( !errorColumnProfiling ) {
                    if (cProfile.getMaxValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (cProfile.getMaxValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = df.format((Number)cProfile.getMaxValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(cProfile.getMaxValue());
                    }
                }
                else {
                    contents = "";
                    alignment = Element.ALIGN_LEFT;
                }
            } else if ( headings[colNo].equalsIgnoreCase("avg value") ) {
                if ( !errorColumnProfiling ) {
                    if (cProfile.getAvgValue() == null) {
                        alignment = Element.ALIGN_CENTER;
                        contents = "";
                    } else if (cProfile.getAvgValue() instanceof Number) {
                        alignment = Element.ALIGN_RIGHT;
                        contents = adf.format((Number)cProfile.getAvgValue());
                    } else {
                        alignment = Element.ALIGN_LEFT;
                        contents = String.valueOf(cProfile.getAvgValue());
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
                if (contentLine.length() > maxCharsInTopN) {
                    contentLine = contentLine.substring(0, maxCharsInTopN) + "...";
                }
                widths[colNo] = Math.max(widths[colNo],
                                      bf.getWidthPoint((String) contentLine, fsize));
                truncContents.append(contentLine).append("\n");
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

}
