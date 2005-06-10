package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.font.FontRenderContext;

import javax.swing.*;
import javax.swing.plaf.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.*;

public class BasicTablePaneUI extends TablePaneUI implements PropertyChangeListener, java.io.Serializable {
	private static Logger logger = Logger.getLogger(BasicTablePaneUI.class);

	private TablePane tablePane;

	final int boxLineThickness = 1;
	final int gap = 1;
	protected Color selectedColor = new Color(204, 204, 255);
	protected Color unselectedColor = new Color(240, 240, 240);

	/**
	 * Doesn't return a preferredSize with width less than this.
	 */
	protected int minimumWidth = 100;

	public static ComponentUI createUI(JComponent c) {
        return new BasicTablePaneUI();
    }

    public void installUI(JComponent c) {
		tablePane = (TablePane) c;
		tablePane.addPropertyChangeListener(this);
    }

    public void uninstallUI(JComponent c) {
		tablePane = (TablePane) c;
		tablePane.removePropertyChangeListener(this);
    }

    public void paint(Graphics g, JComponent c) {
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
			g.translate(insets.left, insets.top);
			int width = c.getWidth() - insets.left - insets.right;
			int height = c.getHeight() - insets.top - insets.bottom;

			Font font = c.getFont();
			if (font == null) {
				logger.error("paint(): Null font in TablePane "+c);
				logger.error("paint(): TablePane's parent is "+c.getParent());
				if (c.getParent() != null) {
					logger.error("paint(): parent font is "+c.getParent().getFont());
				}
				return;
			}
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			int ascent = metrics.getAscent();
			int maxDescent = metrics.getMaxDescent();
			int y = 0;
			
			// hilight title if table is selected
			if (tp.selected == true) {
				g2.setColor(selectedColor);
			} else {
				g2.setColor(unselectedColor);
			}
			g2.fillRect(0, 0, c.getWidth(), fontHeight);
			g2.setColor(c.getForeground());

			// print table name
			g2.drawString(tablePane.getModel().getTableName(), 0, y += ascent);

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
			while (colNameIt.hasNext()) {
				if (tp.isColumnSelected(i)) {
					logger.debug("Column "+i+" is selected");
					g2.setColor(selectedColor);
					g2.fillRect(boxLineThickness+tp.getMargin().left, y-ascent+fontHeight,
								hwidth, fontHeight);
					g2.setColor(tp.getForeground());
				}
				SQLColumn col = (SQLColumn) colNameIt.next();
				if (col.getPrimaryKeySeq() == null && stillNeedPKLine) {
					stillNeedPKLine = false;
					g2.drawLine(0, y+maxDescent, width-1, y+maxDescent);
				}
				g2.drawString(col.getShortDisplayName(),
							  boxLineThickness+tp.getMargin().left,
							  y += fontHeight);
				i++;
			}

			// paint insertion point
			int ip = tablePane.getInsertionPoint();
			if (ip != TablePane.COLUMN_INDEX_NONE) {
				y = gap + boxLineThickness + tp.getMargin().top + ((ip+1) * fontHeight);
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

	public Dimension getPreferredSize(JComponent jc) {
		TablePane c = (TablePane) jc;
		SQLTable table = c.getModel();
		if (table == null) return null;

		int height = 0;
		int width = 0;
		try {
			Insets insets = c.getInsets();
			java.util.List columnList = table.getColumns();
			int cols = columnList.size();
			Font font = c.getFont();
			FontRenderContext frc = c.getCurrentFontRederContext();
			if (font == null || frc == null) {
				logger.error("getPreferredSize(): TablePane is missing font or fontRenderContext.");
				logger.error("getPreferredSize(): component="+c.getName()+"; font="+font+"; frc="+frc);
				return null;
			}
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			height = insets.top + fontHeight + gap + c.getMargin().top + cols*fontHeight + boxLineThickness*2 + c.getMargin().bottom + insets.bottom;
			width = minimumWidth;
			logger.debug("starting width is: " + width);
			Iterator columnIt = table.getColumns().iterator();
			while (columnIt.hasNext()) {
				String theColumn = columnIt.next().toString();
				width = Math.max(width, (int) font.getStringBounds(theColumn, frc).getWidth());
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
	public int pointToColumnIndex(Point p) throws ArchitectException {
		Insets insets = tablePane.getInsets();
		Font font = tablePane.getFont();
		FontMetrics metrics = tablePane.getFontMetrics(font);
		int fontHeight = metrics.getHeight();
		int ascent = metrics.getAscent();

		if (0 <= p.y && p.y <= fontHeight) {
			return TablePane.COLUMN_INDEX_TITLE;
		}

		int firstColStart = fontHeight + gap + boxLineThickness + tablePane.getMargin().top;
		int numCols = tablePane.getModel().getColumns().size();
		if (firstColStart <= p.y && p.y <= firstColStart + fontHeight*numCols) {
			return (p.y - firstColStart) / fontHeight;
		} else if (p.y > firstColStart + fontHeight*numCols) {
			return numCols;
		} else {
			return TablePane.COLUMN_INDEX_NONE;
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
			tablePane.setName(tablePane.getModel().getTableName());
			return;
		}
		tablePane.revalidate();
	}
}
