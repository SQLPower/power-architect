package ca.sqlpower.architect.profile;

public class ProfileFunctionDescriptor {

    private String dataTypeName;
    private int dataTypeCode;
    private boolean countDist;
    private boolean maxValus;
    private boolean minValue;
    private boolean avgValue;
    private boolean maxLength;
    private boolean minLength;
    private boolean sumDecode;
    private boolean avgLength;
    
    
    public ProfileFunctionDescriptor(String dataTypeName,
                                int dataTypeCode,
                                boolean countDist,
                                boolean maxValus,
                                boolean minValue,
                                boolean avgValue,
                                boolean maxLength,
                                boolean minLength,
                                boolean avgLength,
                                boolean sumDecode) {
        this.dataTypeName = dataTypeName;
        this.dataTypeCode = dataTypeCode;
        this.countDist = countDist;
        this.maxValus = maxValus;
        this.minValue = minValue;
        this.avgValue = avgValue;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.avgLength = avgLength;
        this.sumDecode = sumDecode;
    }


    public boolean isAvgLength() {
        return avgLength;
    }


    public void setAvgLength(boolean avgLength) {
        this.avgLength = avgLength;
    }


    public boolean isAvgValue() {
        return avgValue;
    }


    public void setAvgValue(boolean avgValue) {
        this.avgValue = avgValue;
    }


    public boolean isCountDist() {
        return countDist;
    }


    public void setCountDist(boolean countDist) {
        this.countDist = countDist;
    }


    public int getDataTypeCode() {
        return dataTypeCode;
    }


    public void setDataTypeCode(int dataTypeCode) {
        this.dataTypeCode = dataTypeCode;
    }


    public String getDataTypeName() {
        return dataTypeName;
    }


    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }


    public boolean isMaxLength() {
        return maxLength;
    }


    public void setMaxLength(boolean maxLength) {
        this.maxLength = maxLength;
    }


    public boolean isMaxValus() {
        return maxValus;
    }


    public void setMaxValus(boolean maxValus) {
        this.maxValus = maxValus;
    }


    public boolean isMinLength() {
        return minLength;
    }


    public void setMinLength(boolean minLength) {
        this.minLength = minLength;
    }


    public boolean isMinValue() {
        return minValue;
    }


    public void setMinValue(boolean minValue) {
        this.minValue = minValue;
    }


    public boolean isSumDecode() {
        return sumDecode;
    }


    public void setSumDecode(boolean sumDecode) {
        this.sumDecode = sumDecode;
    }


}
