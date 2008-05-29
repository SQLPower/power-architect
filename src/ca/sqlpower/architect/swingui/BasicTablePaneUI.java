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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
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
	public static final int BOX_LINE_THICKNESS = 1;
	
	/**
	 * Amount of space left between the surrounding box and the text it contains.
	 */
	public static final int GAP = 1;
	
	/**
	 * The amount of extra (vertical) space between the PK columns and the non-PK columns. 
	 */
	public static final int PK_GAP = 10;
	
	/**
	 * The width and height of the arc for a rounded rectangle table. 
	 */
	private static final int ARC_LENGTH = 7;
	
	/**
	 * Dashed and normal strokes for different line styles on tables.
	 */
	private static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {15.0f, 4.0f}, 0.0f);
	private static final BasicStroke NORMAL_STROKE = new BasicStroke(1.0f);
	
	/**
	 * Colour of the text background for selected columns.
	 */
	protected Color selectedColor = new Color(204, 204, 255);

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
			Stroke oldStroke = g2.getStroke();
			
			if (tp.isDashed()) {
			    g2.setStroke(DASHED_STROKE);
			} else {
			    g2.setStroke(NORMAL_STROKE);
			}
			
			
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
			
			g2.setColor(c.getPlayPen().getBackground());
			g2.fillRect(0, 0, width, height);
			// no need to reset to foreground: next operation always changes the colour

			// highlight title if table is selected
			if (tp.selected == true) {
				g2.setColor(tp.getBackground().darker());
			} else {
				g2.setColor(tp.getBackground());
			}
			
			if (tp.isRounded()) {
			    g2.fillRoundRect(0, 0, c.getWidth(), fontHeight, ARC_LENGTH, ARC_LENGTH);
			} else {
			    g2.fillRect(0, 0, c.getWidth(), fontHeight);
			}
			
			g2.setColor(tp.getForeground());

			// print table name
			g2.drawString(getTitleString(tablePane), 0, y += ascent);
			
			g2.setColor(Color.BLACK);
			// draw box around columns
			if (fontHeight < 0) {
				throw new IllegalStateException("FontHeight is negative");
			}
			
			if (tp.isRounded()) {
			    g2.drawRoundRect(0, fontHeight+GAP, width-BOX_LINE_THICKNESS, 
			            height-(fontHeight+GAP+BOX_LINE_THICKNESS), ARC_LENGTH, ARC_LENGTH);
			} else {
			    g2.drawRect(0, fontHeight+GAP, width-BOX_LINE_THICKNESS, 
                        height-(fontHeight+GAP+BOX_LINE_THICKNESS));
			}
			y += GAP + BOX_LINE_THICKNESS + tp.getMargin().top;

			// print columns
			Iterator colNameIt = tablePane.getModel().getColumns().iterator();
			int i = 0;
			int hwidth = width-tp.getMargin().right-tp.getMargin().left-BOX_LINE_THICKNESS*2;
			boolean stillNeedPKLine = true;
			Color currentColor = null;
			while (colNameIt.hasNext()) {
				SQLColumn col = (SQLColumn) colNameIt.next();
				if (col.getPrimaryKeySeq() == null && stillNeedPKLine) {
					stillNeedPKLine = false;
					currentColor = null;
					y += PK_GAP;
					g2.setColor(Color.BLACK);
					g2.drawLine(0, y+maxDescent-(PK_GAP/2), width-1, y+maxDescent-(PK_GAP/2));
				}
				if (tp.isColumnSelected(i)) {
					if (logger.isDebugEnabled()) logger.debug("Column "+i+" is selected");
					g2.setColor(selectedColor);
					g2.fillRect(BOX_LINE_THICKNESS+tp.getMargin().left, y-ascent+fontHeight,
								hwidth, fontHeight);
					g2.setColor(Color.BLACK);
				}
				if (tp.getColumnHighlight(i) != currentColor) {
					currentColor = tp.getColumnHighlight(i);
					g2.setColor(currentColor == null ? Color.BLACK : currentColor);
				}
				g2.drawString(col.getShortDisplayName(),
							  BOX_LINE_THICKNESS+tp.getMargin().left,
							  y += fontHeight);
				i++;
			}
			
			if (currentColor != null) {
				g2.setColor(Color.BLACK);
			}

			if (stillNeedPKLine) {
			    stillNeedPKLine = false;
			    y += PK_GAP;
			    g2.drawLine(0, y+maxDescent-(PK_GAP/2), width-1, y+maxDescent-(PK_GAP/2));
			}
			
			// paint insertion point
			int ip = tablePane.getInsertionPoint();
			if (logger.isDebugEnabled()) {
			    g2.drawString(String.valueOf(ip), width-20, ascent);
			}

			g2.setStroke(oldStroke);
			
			if (ip != TablePane.COLUMN_INDEX_NONE) {
			    y = GAP + BOX_LINE_THICKNESS + tp.getMargin().top + fontHeight;
			    if (ip == TablePane.COLUMN_INDEX_END_OF_PK) {
			        y += fontHeight * tablePane.getModel().getPkSize();
			    } else if (ip == TablePane.COLUMN_INDEX_START_OF_NON_PK) {
			        y += fontHeight * tablePane.getModel().getPkSize() + PK_GAP;
			    } else if (ip < tablePane.getModel().getPkSize()) {
			        if (ip == TablePane.COLUMN_INDEX_TITLE) ip = 0;
			        y += ip * fontHeight;
			    } else {
				    y += ip * fontHeight + PK_GAP;
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
			height = insets.top + fontHeight + GAP + c.getMargin().top + PK_GAP + cols*fontHeight + BOX_LINE_THICKNESS*2 + c.getMargin().bottom + insets.bottom;
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
			width += insets.left + c.getMargin().left + BOX_LINE_THICKNESS*2 + c.getMargin().right + insets.right;
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
		int firstColStart = fontHeight + GAP + BOX_LINE_THICKNESS + tablePane.getMargin().top;
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
		} else if (p.y <= pkLine + PK_GAP/2) {
		    logger.debug("y<=pkLine + pkGap/2 = "+(pkLine + PK_GAP/2));
		    returnVal = TablePane.COLUMN_INDEX_END_OF_PK;
		} else if (p.y <= firstColStart + fontHeight*numPkCols + PK_GAP) {
		    logger.debug("y<=firstColStart + fontHeight*numPkCols + pkGap = "+(firstColStart + fontHeight*numPkCols + PK_GAP));
		    returnVal = TablePane.COLUMN_INDEX_START_OF_NON_PK;
		} else if (p.y < firstColStart + PK_GAP + fontHeight*numCols) {
		    logger.debug("y<=firstColStart + pkGap + fontHeight*numCols = " + (firstColStart + PK_GAP + fontHeight*numCols));
		    returnVal = (p.y - firstColStart - PK_GAP) / fontHeight;
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
            int firstColY = fontHeight + GAP + BOX_LINE_THICKNESS + tablePane.getMargin().top;
            int y = firstColY + (fontHeight * colidx) + (fontHeight / 2);
            if (colidx >= tablePane.getModel().getPkSize()) {
                y += PK_GAP;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Column " + colidx + " Y value is " + y);
                logger.debug("gap=" + GAP + "; boxLineThickness=" + BOX_LINE_THICKNESS + "; margin.top=" + tablePane.getMargin().top);
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
