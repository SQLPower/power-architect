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

package ca.sqlpower.architect.swingui;

import java.awt.print.PageFormat;

/**
 * A simple class for storing print settings.
 */
public class PrintSettings {
    
    private String printerName;
    
    private double zoom;
    
    private int numCopies;
    
    private boolean pageNumbersPrinted;

    /**
     * This is the orientation of the page. It's value is from
     * {@link PageFormat}.
     */
    private int orientation;
    
    private double paperWidth;
    
    private double paperHeight;
    
    private double leftBorder;
    
    private double rightBorder;
    
    private double topBorder;
    
    private double bottomBorder;
    
    public PrintSettings() {
        //generic defaults
        zoom = 1.0;
        numCopies = 1;
        pageNumbersPrinted = true;
        printerName = null;
        orientation = PageFormat.PORTRAIT;
        paperWidth = 8.5 * 72;
        paperHeight = 11.0 * 72;
        leftBorder = 50;
        rightBorder = 50;
        topBorder = 50;
        bottomBorder = 50;
    }

    public double getPaperWidth() {
        return paperWidth;
    }

    public void setPaperWidth(double paperWidth) {
        this.paperWidth = paperWidth;
    }

    public double getPaperHeight() {
        return paperHeight;
    }

    public void setPaperHeight(double paperHeight) {
        this.paperHeight = paperHeight;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public int getNumCopies() {
        return numCopies;
    }

    public void setNumCopies(int numCopies) {
        this.numCopies = numCopies;
    }

    public boolean isPageNumbersPrinted() {
        return pageNumbersPrinted;
    }

    public void setPageNumbersPrinted(boolean pageNumbersPrinted) {
        this.pageNumbersPrinted = pageNumbersPrinted;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

    public double getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(double leftBorder) {
        this.leftBorder = leftBorder;
    }

    public double getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(double rightBorder) {
        this.rightBorder = rightBorder;
    }

    public double getTopBorder() {
        return topBorder;
    }

    public void setTopBorder(double topBorder) {
        this.topBorder = topBorder;
    }

    public double getBottomBorder() {
        return bottomBorder;
    }

    public void setBottomBorder(double bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

}
