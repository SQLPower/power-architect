package ca.sqlpower.architect.swingui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
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
	final boolean dropShadowEnabled = true;

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

			//  We don't want to paint inside the insets or borders.
			Insets insets = c.getInsets();
			g.translate(insets.left, insets.top);
			int width = c.getWidth() - insets.left - insets.right;
			int height = c.getHeight() - insets.top - insets.bottom;
			
			// print table name
			Font font = c.getFont();
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			int ascent = metrics.getAscent();
			int y = 0;
			g2.drawString(tablePane.getModel().getTableName(), 0, y += ascent);

			// draw box
			if (fontHeight < 0) {
				throw new IllegalStateException("FontHeight is negative");
			}
			g2.draw3DRect(0, fontHeight+gap, width-boxLineThickness,
						  height-(fontHeight+gap+boxLineThickness), dropShadowEnabled);
			y += gap + boxLineThickness + tp.getMargin().top;

			// print primary key
			
			// draw line
			
			// print rest of columns
			Iterator colNameIt = tablePane.getModel().getColumns().iterator();
			while (colNameIt.hasNext()) {
				g2.drawString(((SQLColumn) colNameIt.next()).getShortDisplayName(),
							  boxLineThickness+tp.getMargin().left,
							  y += fontHeight);
			}

			
			g.translate(-insets.left, -insets.top);
		} catch (ArchitectException e) {
			logger.warn("BasicTablePaneUI.paint failed", e);
		}
	}

	public void computeSize(TablePane c) {
		int height = 0;
		int width = 0;
		try {
			Insets insets = c.getInsets();
			SQLTable table = c.getModel();
			int cols = table.getColumns().size();
			Font font = c.getFont();
			if (font == null) {
				logger.error("Null font in TablePane "+c);
				return;
			}
			FontMetrics metrics = c.getFontMetrics(font);
			int fontHeight = metrics.getHeight();
			height = insets.top + fontHeight + gap + c.getMargin().top + cols*fontHeight + boxLineThickness*2 + c.getMargin().bottom + insets.bottom;
			width = c.getMinimumSize().width;

			Iterator columnIt = table.getColumns().iterator();
			while (columnIt.hasNext()) {
				width = Math.max(width, metrics.stringWidth(columnIt.next().toString()));
			}
			width += insets.left + c.getMargin().left + boxLineThickness*2 + c.getMargin().right + insets.right;
		} catch (ArchitectException e) {
			logger.warn("BasicTablePaneUI.computeSize failed due to", e);
			width = 100;
			height = 100;
		}

		c.setPreferredSize(new Dimension(width, height));
		c.setSize(width, height); // XXX: maybe this should go elsewhere (not sure where)
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
		}

		return TablePane.COLUMN_INDEX_NONE;
	}

	/**
	 * Recomputes the component's size if the given property change
	 * makes this necessary (any visible changes will make
	 * recompuation necessary).
	 */
	public void propertyChange(PropertyChangeEvent e) {
		logger.debug("BasicTablePaneUI notices change of "+e.getPropertyName()
					 +" from "+e.getOldValue()+" to "+e.getNewValue()+" on "+e.getSource());
		if (e.getPropertyName().equals("UI")) return;
		if (e.getPropertyName().equals("preferredSize")) return;
		if (e.getPropertyName().equals("model.tableName")) {
			tablePane.setName(tablePane.getModel().getTableName());
			return;
		}
		computeSize((TablePane) e.getSource());
	}
}
