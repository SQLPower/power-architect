/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
