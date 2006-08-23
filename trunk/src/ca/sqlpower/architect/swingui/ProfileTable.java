package ca.sqlpower.architect.swingui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import ca.sqlpower.architect.profile.ProfileColumn;
import ca.sqlpower.architect.swingui.table.DateRendererFactory;
import ca.sqlpower.architect.swingui.table.DecimalRendererFactory;
import ca.sqlpower.architect.swingui.table.PercentRendererFactory;
import ca.sqlpower.architect.swingui.table.SQLObjectRendererFactory;
import ca.sqlpower.architect.swingui.table.ValueRendererFactory;

/**
 * Override JTable methods that control cell formatting, because we want
 * to always use particular format subclasses (from c.s.a.swingui.table)
 * to format particular columns.
 *
 */
public class ProfileTable extends JTable {

    public ProfileTable(TableModel model) {
        super(model);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        ProfileColumn pc = ProfileColumn.values()[column];
        switch(pc) {
        case DATABASE: case SCHEMA: case CATALOG: case TABLE: case COLUMN:
            return new SQLObjectRendererFactory();
        case RUNDATE:
            return new DateRendererFactory();
        case PERCENT_UNIQUE:
        case PERCENT_NULL:
            return new PercentRendererFactory();
        case AVERAGE_LENGTH:
            return new DecimalRendererFactory();
        case MIN_VALUE:
        case MAX_VALUE:
        case AVERAGE_VALUE:
            return new ValueRendererFactory();
        default:
            return super.getCellRenderer(row, column);

        }
    }
}
