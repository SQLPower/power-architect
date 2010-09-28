package ca.sqlpower.architect.profile;

public class ColumnValueCount {

    private Object value;
    private int count;

    public ColumnValueCount(Object value, int count) {
        this.value = value;
        this.count = count;
    }
    public int getCount() {
        return count;
    }
    public Object getValue() {
        return value;
    }


}
