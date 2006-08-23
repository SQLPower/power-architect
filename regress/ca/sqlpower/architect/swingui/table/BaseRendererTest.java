package ca.sqlpower.architect.swingui.table;

import javax.swing.JTable;

import junit.framework.TestCase;

public abstract class BaseRendererTest extends TestCase {
    JTable table = new JTable() {
        @Override
        public int convertColumnIndexToModel(int viewColumnIndex) {
            return viewColumnIndex;
        }
    };

}
