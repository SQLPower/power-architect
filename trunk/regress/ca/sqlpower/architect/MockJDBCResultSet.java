package regress.ca.sqlpower.architect;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MockJDBCResultSet implements ResultSet {

	private MockJDBCStatement statement;
	private int columnCount;
	private String[] columnNames;
	private List<Object[]> rows;
	private int currentRow;
	private boolean wasNull;
	
	MockJDBCResultSet(MockJDBCStatement statement, int columnCount) {
		this.statement = statement;
		this.columnCount = columnCount;
		this.columnNames = new String[columnCount];
		this.rows = new ArrayList<Object[]>();
	}

	/**
	 * Gets the value in the current row at columnIndex.  Checks that currentRow and columnIndex
	 * are valid, and throws SQLException if they are not.  Also sets wasNull as a side effect.
	 * @param columnIndex The column index to get (between 1 and columnCount; 0 is not valid).
	 * @return The value in the given cell, with no type conversions.
	 * @throws SQLException If the current row or given column index are out of range.
	 */
	private Object getRowCol(int columnIndex) throws SQLException {
		if (currentRow < 1 || currentRow > rows.size()) {
			throw new SQLException("Not on a valid row (current="+currentRow+", rows="+rows.size()+")");
		}
		if (columnIndex < 1 || columnIndex > columnCount) {
			throw new SQLException("Column index "+columnIndex+" out of range (columnCount="+columnCount+")");
		}
		Object val = rows.get(currentRow-1)[columnIndex-1];
		wasNull = val == null;
		return val;
	}

	/**
	 * Adds a new row at the end of this result set, and makes it the current row.
	 */
	void addRow() {
		rows.add(new Object[columnCount]);
		last();
	}
	
	/**
	 * Stores a lower-case version of name for the columnIndexth column.
	 * 
	 * @param columnIndex The index of the column to (re)name.  This starts with 1, not 0.
	 * @param name The name the give the column.
	 */
	void setColumnName(int columnIndex, String name) {
		columnNames[columnIndex-1] = name.toLowerCase();
	}
	
	
	// ============ java.sql.ResultSet implementation is below this line ===========
	
	public boolean next() throws SQLException {
		return relative(1);
	}

	public void close() throws SQLException {
		// do nothing
	}

	public boolean wasNull() throws SQLException {
		return wasNull;
	}

	public String getString(int columnIndex) throws SQLException {
		Object val = getRowCol(columnIndex);
		if (val == null) {
			return null;
		} else {
			return val.toString();
		}
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public byte getByte(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public short getShort(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getInt(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public long getLong(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public float getFloat(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public double getDouble(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Date getDate(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Time getTime(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getString(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean getBoolean(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public byte getByte(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public short getShort(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean isBeforeFirst() {
		return currentRow == 0;
	}

	public boolean isAfterLast() {
		return currentRow == rows.size() + 1;
	}

	public boolean isFirst() {
		return currentRow == 1;
	}

	public boolean isLast() {
		return currentRow == rows.size();
	}

	public void beforeFirst() {
		if (rows.size() > 0) {
			absolute(0);
		}
	}

	public void afterLast() {
		if (rows.size() > 0) {
			absolute(rows.size() + 1);
		}
	}

	public boolean first() {
		return absolute(1);
	}

	public boolean last() {
		return absolute(-1);
	}

	public int getRow() {
		return currentRow;
	}

	public boolean absolute(int row) {
		if (row == 0) {
			// before first row
			currentRow = 0;
			return false;
		} else if (row < 0) {
			// absolute position from end of result set
			int newRow = rows.size() + row + 1;
			if (newRow < 1) {
				currentRow = 0;
				return false;
			} else {
				currentRow = newRow;
				return true;
			}
		} else if (row > rows.size()) {
			// after last row
			currentRow = rows.size() + 1;
			return false;
		} else {
			// absolute position from beginning
			currentRow = row;
			return true;
		}
	}

	public boolean relative(int nrows) {
		currentRow += nrows;
		if (currentRow < 1) {
			currentRow = 0;
			return false;
		} else if (currentRow > rows.size()) {
			currentRow = rows.size() + 1;
			return false;
		} else {
			return true;
		}
	}

	public boolean previous() {
		return relative(-1);
	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");

	}

	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getType() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getConcurrency() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateNull(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateObject(int columnIndex, Object x, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		rows.get(currentRow-1)[columnIndex-1] = x;
	}

	public void updateNull(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateShort(String columnName, short x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateInt(String columnName, int x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateLong(String columnName, long x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBigDecimal(String columnName, BigDecimal x)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateString(String columnName, String x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateTimestamp(String columnName, Timestamp x)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateAsciiStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBinaryStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateCharacterStream(String columnName, Reader reader,
			int length) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateObject(String columnName, Object x, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Statement getStatement() throws SQLException {
		return statement;
	}

	public Object getObject(int arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Ref getRef(int i) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Blob getBlob(int i) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Clob getClob(int i) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Array getArray(int i) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Object getObject(String arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Ref getRef(String colName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Blob getBlob(String colName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Clob getClob(String colName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Array getArray(String colName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public URL getURL(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public URL getURL(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getInt(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public long getLong(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public float getFloat(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public double getDouble(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public byte[] getBytes(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Date getDate(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Time getTime(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public String getCursorName() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Object getObject(int columnIndex) throws SQLException {
		return getRowCol(columnIndex);
	}

	public Object getObject(String columnName) throws SQLException {
		return getObject(findColumn(columnName));
	}

	public int findColumn(String columnName) throws SQLException {
		return Arrays.asList(columnNames).indexOf(columnName.toLowerCase());
	}

}
