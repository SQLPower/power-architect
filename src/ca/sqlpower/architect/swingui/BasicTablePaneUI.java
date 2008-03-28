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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class BasicTablePaneUI extends TablePaneUI implements PropertyChangeListener, java.io.Serializable {
	private static Logger logger = Logger.getLogger(BasicTablePaneUI.class);

	/**
	 * The TablePane component that this UI delegate works for.
	 */
	private TablePane tablePane;

	/**
	 * Thickness (in Java2D units) of the surrounding box.
	 */
	final int boxLineThickness = 1;
	
	/**
	 * Amount of space left between the surrounding box and the text it contains.
	 */
	final int gap = 1;
	
	/**
	 * The amount of extra (vertical) space between the PK columns and the non-PK columns. 
	 */
	final int pkGap = 10;
	
	/**
	 * Colour of the text background for selected tables and columns.
	 */
	protected Color selectedColor = new Color(204, 204, 255);
	
	/**
	 * Colour of the title background for non-selected tables.
	 */
	protected Color unselectedColor = new Color(240, 240, 240);

	/**
	 * Doesn't return a preferredSize with width less than this.
	 */
	protected int minimumWidth = 100;

	public static PlayPenComponentUI createUI(PlayPenComponent c) {
        return new BasicTablePaneUI();
    }

    public void installUI(PlayPenComponent c) {
		tablePane = (TablePane) c;
		tablePane.addPropertyChangeListener(this);
    }

    public void uninstallUI(PlayPenComponent c) {
		tablePane = (TablePane) c;
		tablePane.removePropertyChangeListener(this);
    }

    public void paint(Graphics2D g) {
    		paint(g,tablePane);
    }
    
    public void paint(Graphics g, PlayPenComponent c) {
		TablePane tp = (TablePane) c;
		try {
			Graphics2D g2 = (Graphics2D) g;
	
			
			if (logger.isDebugEnabled()) {
				Rectangle clip = g2.getClipBounds();
				if (clip != null) {
					g2.setColor(Color.red);
					clip.width--;
					clip.height--;
					g2.draw(clip);
					g2.setColor(tp.getForeground());
					logger.debug("Clipping region: "+g2.getClip());
				} else {
					logger.debug("Null clipping region");
				}
			}

			//  We don't want to paint inside the insets or borders.
			Insets insets = c.getInsets();
			
			//builds a little buffer to reduce the clipping problem
			//this only seams to work at a non-zoomed level. This could 
			//use a little work (better fix)
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, c.getWidth(), c.getHeight());
			
			g.translate(insets.left, insets.top);

	        int width = c.getWidth() - insets.left - insets.right;
			int height = c.getHeight() - insets.top - insets.bottom;

			Font font = c.getFont();
			if (font == null) {
			    // This happens when the table exists but has no visible ancestor.
			    // Don't ask me why it's being asked to paint under those circumstances!
				//logger.error("paint(): Null font in TablePane "+c);
				return;
			}
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			int ascent = metrics.getAscent();
			int maxDescent = metrics.getMaxDescent();
			int y = 0;
			
			g2.setColor(c.getBackground());
			g2.fillRect(0, 0, width, height);
			// no need to reset to foreground: next operation always changes the colour
			
			// highlight title if table is selected
			if (tp.selected == true) {
				g2.setColor(selectedColor);
			} else {
				g2.setColor(unselectedColor);
			}
			g2.fillRect(0, 0, c.getWidth(), fontHeight);
			g2.setColor(c.getForeground());

			// print table name
			g2.drawString(getTitleString(tablePane), 0, y += ascent);

			// draw box around columns
			if (fontHeight < 0) {
				throw new IllegalStateException("FontHeight is negative");
			}
			g2.drawRect(0, fontHeight+gap,
						width-boxLineThickness, height-(fontHeight+gap+boxLineThickness));
			y += gap + boxLineThickness + tp.getMargin().top;

			// print columns
			Iterator colNameIt = tablePane.getModel().getColumns().iterator();
			int i = 0;
			int hwidth = width-tp.getMargin().right-tp.getMargin().left-boxLineThickness*2;
			boolean stillNeedPKLine = true;
			Color currentColor = null;
			while (colNameIt.hasNext()) {
				SQLColumn col = (SQLColumn) colNameIt.next();
				if (col.getPrimaryKeySeq() == null && stillNeedPKLine) {
					stillNeedPKLine = false;
					y += pkGap;
					currentColor = null;
					g2.setColor(tp.getForeground());
					g2.drawLine(0, y+maxDescent-(pkGap/2), width-1, y+maxDescent-(pkGap/2));
				}
				if (tp.isColumnSelected(i)) {
					if (logger.isDebugEnabled()) logger.debug("Column "+i+" is selected");
					g2.setColor(selectedColor);
					g2.fillRect(boxLineThickness+tp.getMargin().left, y-ascent+fontHeight,
								hwidth, fontHeight);
					g2.setColor(tp.getForeground());
				}
				if (tp.getColumnHighlight(i) != currentColor) {
				    currentColor = tp.getColumnHighlight(i);
				    g2.setColor(currentColor == null ? tp.getForeground() : currentColor);
				}
				g2.drawString(col.getShortDisplayName(),
							  boxLineThickness+tp.getMargin().left,
							  y += fontHeight);
				i++;
			}

			if (currentColor != null) {
			    g2.setColor(tp.getForeground());
			}
			
			if (stillNeedPKLine) {
			    stillNeedPKLine = false;
			    y += pkGap;
			    g2.drawLine(0, y+maxDescent-(pkGap/2), width-1, y+maxDescent-(pkGap/2));
			}
			
			// paint insertion point
			int ip = tablePane.getInsertionPoint();
			if (logger.isDebugEnabled()) {
			    g2.drawString(String.valueOf(ip), width-20, ascent);
			}
			if (ip != TablePane.COLUMN_INDEX_NONE) {
			    y = gap + boxLineThickness + tp.getMargin().top + fontHeight;
			    if (ip == TablePane.COLUMN_INDEX_END_OF_PK) {
			        y += fontHeight * tablePane.getModel().getPkSize();
			    } else if (ip == TablePane.COLUMN_INDEX_START_OF_NON_PK) {
			        y += fontHeight * tablePane.getModel().getPkSize() + pkGap;
			    } else if (ip < tablePane.getModel().getPkSize()) {
			        if (ip == TablePane.COLUMN_INDEX_TITLE) ip = 0;
			        y += ip * fontHeight;
			    } else {
				    y += ip * fontHeight + pkGap;
				}
				g2.drawLine(5, y, width - 6, y);
				g2.drawLine(2, y-3, 5, y);
				g2.drawLine(2, y+3, 5, y);
				g2.drawLine(width - 3, y-3, width - 6, y);
				g2.drawLine(width - 3, y+3, width - 6, y);
			}

			g.translate(-insets.left, -insets.top);

		} catch (ArchitectException e) {
			logger.warn("BasicTablePaneUI.paint failed", e);
		}
	}

	public Dimension getPreferredSize() {
		return getPreferredSize(tablePane);
	}
	
	public Dimension getPreferredSize(PlayPenComponent ppc) {
		TablePane c = (TablePane) ppc;
		SQLTable table = c.getModel();
		if (table == null) return null;

		int height = 0;
		int width = 0;
		try {
			Insets insets = c.getInsets();
			java.util.List columnList = table.getColumns();
			int cols = columnList.size();
			Font font = c.getFont();
			if (font == null) {
				logger.error("getPreferredSize(): TablePane is missing font.");
				return null;
			}
			FontRenderContext frc = c.getFontRenderContext();
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			height = insets.top + fontHeight + gap + c.getMargin().top + pkGap + cols*fontHeight + boxLineThickness*2 + c.getMargin().bottom + insets.bottom;
			width = minimumWidth;
			logger.debug("starting width is: " + width);
			List<String> itemsToCheck = new ArrayList<String>();
			for (SQLColumn col : table.getColumns()) {
				if (col == null) {
					logger.error("Found null column in table '"+table.getName()+"'");
					throw new NullPointerException("Found null column in table '"+table.getName()+"'");
				}
				itemsToCheck.add(col.toString());
			}
			itemsToCheck.add(getTitleString(c));   // this works as long as the title uses the same font as the columns
			for(String item : itemsToCheck) {
				if (item == null) item = "(null!?)";
				if (frc == null) {
				    width = Math.max(width, metrics.stringWidth(item));
				} else {
				    width = Math.max(width, (int) font.getStringBounds(item, frc).getWidth());
				}
				logger.debug("new width is: " + width);
			}
			width += insets.left + c.getMargin().left + boxLineThickness*2 + c.getMargin().right + insets.right;
		} catch (ArchitectException e) {
			logger.warn("BasicTablePaneUI.getPreferredSize failed due to", e);
			width = 100;
			height = 100;
		}

		return new Dimension(width, height);
	}

	/**
	 * This method is specified by TablePane.pointToColumnIndex().
	 * This implementation depends on the implementation of paint().
	 */
    @Override
	public int pointToColumnIndex(Point p) throws ArchitectException {
		Font font = tablePane.getFont();
		FontMetrics metrics = tablePane.getFontMetrics(font);
		int fontHeight = metrics.getHeight();

		int numPkCols = tablePane.getModel().getPkSize();
		int numCols = tablePane.getModel().getColumns().size();
		int firstColStart = fontHeight + gap + boxLineThickness + tablePane.getMargin().top;
		int pkLine = firstColStart + fontHeight*numPkCols;

		if (logger.isDebugEnabled()) logger.debug("p.y = "+p.y);
		
		int returnVal;
		
		if (p.y < 0) {
		    logger.debug("y<0");
		    returnVal = TablePane.COLUMN_INDEX_NONE;
		} else if (p.y <= fontHeight) {
		    logger.debug("y<=fontHeight = "+fontHeight);
		    returnVal = TablePane.COLUMN_INDEX_TITLE;
		} else if (numPkCols > 0 && p.y <= firstColStart + fontHeight*numPkCols - 1) {
		    logger.debug("y<=firstColStart + fontHeight*numPkCols - 1= "+(firstColStart + fontHeight*numPkCols));
		    returnVal = (p.y - firstColStart) / fontHeight;
		} else if (p.y <= pkLine + pkGap/2) {
		    logger.debug("y<=pkLine + pkGap/2 = "+(pkLine + pkGap/2));
		    returnVal = TablePane.COLUMN_INDEX_END_OF_PK;
		} else if (p.y <= firstColStart + fontHeight*numPkCols + pkGap) {
		    logger.debug("y<=firstColStart + fontHeight*numPkCols + pkGap = "+(firstColStart + fontHeight*numPkCols + pkGap));
		    returnVal = TablePane.COLUMN_INDEX_START_OF_NON_PK;
		} else if (p.y < firstColStart + pkGap + fontHeight*numCols) {
		    logger.debug("y<=firstColStart + pkGap + fontHeight*numCols = " + (firstColStart + pkGap + fontHeight*numCols));
		    returnVal = (p.y - firstColStart - pkGap) / fontHeight;
		} else {
		    returnVal = TablePane.COLUMN_INDEX_NONE;
		}
		logger.debug("pointToColumnIndex return value is " + returnVal);
		return returnVal;
	}

    @Override
    public int columnIndexToCentreY(int colidx) throws ArchitectException {
        Font font = tablePane.getFont();
        FontMetrics metrics = tablePane.getFontMetrics(font);
        int fontHeight = metrics.getHeight();
        if (colidx == TablePane.COLUMN_INDEX_TITLE) {
            return tablePane.getMargin().top + (fontHeight / 2);
        } else if (colidx >= 0 && colidx < tablePane.getModel().getColumns().size()) {
            int firstColY = fontHeight + gap + boxLineThickness + tablePane.getMargin().top;
            int y = firstColY + (fontHeight * colidx) + (fontHeight / 2);
            if (colidx >= tablePane.getModel().getPkSize()) {
                y += pkGap;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Column " + colidx + " Y value is " + y);
                logger.debug("gap=" + gap + "; boxLineThickness=" + boxLineThickness + "; margin.top=" + tablePane.getMargin().top);
            }
            return y;
        } else {
            return -1;
        }
    }
    
	/**
	 * Tells the component to revalidate (also causes a repaint) if
	 * the given property change makes this necessary (any properties
	 * rendered visibly repainting necessary).
	 */
	public void propertyChange(PropertyChangeEvent e) {
		logger.debug("BasicTablePaneUI notices change of "+e.getPropertyName()
					 +" from "+e.getOldValue()+" to "+e.getNewValue()+" on "+e.getSource());
		if (e.getPropertyName().equals("UI")) return;
		else if (e.getPropertyName().equals("preferredSize")) return;
		else if (e.getPropertyName().equals("insertionPoint")) return;
		else if (e.getPropertyName().equals("model.tableName")) {
			// helps with debugging to keep component names identical with model -- it's not visual
			tablePane.setName(tablePane.getModel().getName());
			return;
		}
		tablePane.revalidate();
	}

	public boolean contains(Point p) {
		return tablePane.getBounds().contains(p);
	}

	public void revalidate() {
	}

    private String getTitleString(TablePane tp) {
        if (tp.isFullyQualifiedNameInHeader()) {
            SQLTable t = tp.getModel();
            String db = t.getParentDatabase() == null ? null : t.getParentDatabase().getName();
            String cat = t.getCatalogName().length() == 0 ? null : t.getCatalogName();
            String sch = t.getSchemaName().length() == 0 ? null : t.getSchemaName();
            StringBuffer fqn = new StringBuffer();
            fqn.append(db);
            if (cat != null) fqn.append('.').append(cat);
            if (sch != null) fqn.append('.').append(sch);
            fqn.append('.').append(tp.getModel().getName());
            return fqn.toString();
        } else {
            return tp.getModel().getName();
        }
    }

}
