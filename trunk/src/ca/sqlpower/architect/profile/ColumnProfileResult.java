package ca.sqlpower.architect.profile;

import java.util.ArrayList;
import java.util.List;

public class ColumnProfileResult extends ProfileResult {

    private int distinctValueCount;
    private Object minValue;
    private Object maxValue;
    private Object avgValue;
    private int minLength;
    private int maxLength;
    private double avgLength;
    private int nullCount;
    private List<ColumnValueCount> topTen;
    
    public ColumnProfileResult(long createStartTime) {
        super(createStartTime);
        topTen = new ArrayList<ColumnValueCount>();
    }

    public double getAvgLength() {
        return avgLength;
    }

    public void setAvgLength(double avgLength) {
        this.avgLength = avgLength;
    }

    /**
     * @return The average value as a Number object, or null if there were
     * 0 values.
     */
    public Object getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Object avgValue) {
        this.avgValue = avgValue;
    }

    public int getDistinctValueCount() {
        return distinctValueCount;
    }

    public void setDistinctValueCount(int distinctValueCount) {
        this.distinctValueCount = distinctValueCount;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return The minimum value as a Number object, or null if there were
     * 0 values.
     */
    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    @Override
    public String toString() {
        return "[ColumnProfileResult:" + 
                "; distinctValues: "+distinctValueCount+
                "; minLength: "+minLength+
                "; maxLength: "+maxLength+
                "; avgLength: "+avgLength+
                "; minValue: "+getMinValue()+
                "; maxValue: "+getMaxValue()+
                "; avgValue: "+avgValue+
                "; nullCount: "+getNullCount()+ "]";
    }

    public int getNullCount() {
        return nullCount;
    }

    public void setNullCount(int nullCount) {
        this.nullCount = nullCount;
    }

    public void addValueCount(Object value, int count) {
        topTen.add(new ColumnValueCount(value,count));
        return;
    }
    public List<ColumnValueCount> getValueCount() {
        return topTen;
    }
    
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
}
