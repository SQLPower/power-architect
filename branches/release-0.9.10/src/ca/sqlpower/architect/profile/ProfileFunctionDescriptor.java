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


}
