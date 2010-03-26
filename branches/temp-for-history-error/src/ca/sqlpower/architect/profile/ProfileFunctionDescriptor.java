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
package ca.sqlpower.architect.profile;

public class ProfileFunctionDescriptor {
    
    /**
     * This will create a ProfileFunctionDescriptor from a string. The string should
     * be a comma separated string of the values needed for a ProfileFunctionDescriptor.
     */
    public static ProfileFunctionDescriptor parseDescriptorString(String descString) {
        String[] dataTypeParts = descString.split(",");
        int dataTypeValue = Integer.parseInt(dataTypeParts[2].trim());
        
        ProfileFunctionDescriptor pfd = new ProfileFunctionDescriptor(dataTypeParts[1].trim(), 
                dataTypeValue, dataTypeParts[3].trim().startsWith("t"), dataTypeParts[4].trim().startsWith("t"),
                dataTypeParts[5].trim().startsWith("t"), dataTypeParts[6].trim().startsWith("t"),
                dataTypeParts[7].trim().startsWith("t"), dataTypeParts[8].trim().startsWith("t"),
                dataTypeParts[9].trim().startsWith("t"), dataTypeParts[10].trim().startsWith("t"));
        
        pfd.setArchitectSpecificName(dataTypeParts[0].trim());
        
        return pfd;
    }
    
    /**
     * This generates a comma separated string of the values of a {@link ProfileFunctionDescriptor}.
     * This string can be used to store a {@link ProfileFunctionDescriptor} in a file to be loaded
     * back later.
     */
    public static String createDescriptorString(ProfileFunctionDescriptor pfd) {
        return pfd.getArchitectSpecificName() + "," + pfd.getDataTypeName() + "," + pfd.getDataTypeCode() + "," + Boolean.toString(pfd.isCountDist()) +
                "," + Boolean.toString(pfd.isMaxValue()) + "," + Boolean.toString(pfd.isMinValue()) + "," + Boolean.toString(pfd.isAvgValue()) +
                "," + Boolean.toString(pfd.isMaxLength()) + "," + Boolean.toString(pfd.isMinLength()) + "," + Boolean.toString(pfd.isAvgLength()) +
                "," + Boolean.toString(pfd.isSumDecode());
    }

    /**
     * This is the architect specific name the data type is being mapped to.
     */
    private String architectSpecificName;
    private String dataTypeName;
    private int dataTypeCode;
    private boolean countDist;
    private boolean maxValue;
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
        this.maxValue = maxValus;
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


    public boolean isMaxValue() {
        return maxValue;
    }


    public void setMaxValue(boolean maxValue) {
        this.maxValue = maxValue;
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

    public void setArchitectSpecificName(String architectSpecificName) {
        this.architectSpecificName = architectSpecificName;
    }

    public String getArchitectSpecificName() {
        return architectSpecificName;
    }


}
