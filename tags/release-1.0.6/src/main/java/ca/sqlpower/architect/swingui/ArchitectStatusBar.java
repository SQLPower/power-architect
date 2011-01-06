/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectStatusInformation;
import ca.sqlpower.util.MonitorableImpl;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class contains a status bar that appears at the bottom of the
 * ArchitectFrame. The status bar is handy for updating the user on changes that
 * are helpful for the user to know but is not so important that they need to be
 * given a pop-up.
 */
public class ArchitectStatusBar implements ArchitectStatusInformation {
    
    private static final Logger logger = Logger.getLogger(ArchitectStatusBar.class);
    
    private static final ImageIcon PROGRESS_BAR_ICON = new ImageIcon(ArchitectStatusBar.class.getResource("/icons/progressBar.png"));

    /**
     * This progress bar updates its UI immediately by painting directly to its
     * graphics object on changes to its settings to keep the user informed of
     * the current progress of the operation even if the foreground thread that
     * normally does the painting is blocked waiting for server operations.
     * <p>
     * Methods in this class are synchronized as the progress bar can be updated
     * from multiple threads.
     */
    private class ArchitectStatusProgressBar extends MonitorableImpl {
        
        /**
         * This is the starting x position on the progress bar where this
         * progress bar specifically can start to paint.
         */
        private int x = 0;
        
        /**
         * This is the width of the progress bar this specific progress bar
         * can update on.
         */
        private int width = 0;
        
        @Override
        public synchronized void setJobSize(Integer jobSize) {
            super.setJobSize(jobSize);
            paint();
        }
        
        @Override
        public synchronized void setMessage(String message) {
            super.setMessage(message);
            paint();
        }
        
        @Override
        public synchronized void setProgress(int progress) {
            super.setProgress(progress);
            paint();
            if (progress >= getJobSize()) {
                removeProgressBar(this);
            }
        }
        
        @Override
        public synchronized void incrementProgress() {
            super.incrementProgress();
            paint();
            if (getProgress() >= getJobSize()) {
                removeProgressBar(this);
            }
        }
        
        @Override
        public synchronized void setCancelled(boolean cancelled) {
            super.setCancelled(cancelled);
            paint();
            removeProgressBar(this);
        }
        
        @Override
        public synchronized void setFinished(boolean finished) {
            super.setFinished(finished);
            paint();
            removeProgressBar(this);
        }
        
        private synchronized void paint() {
            Graphics g = progressBarPanel.getGraphics();
            progressBarPanel.setDoubleBuffered(true);
            
            if (!isCancelled() && !isFinished() && 
                    getJobSize() != null && getProgress() < getJobSize()) {
                BufferedImage buffer = new BufferedImage(getWidth(), progressBarPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics bufferG = buffer.getGraphics();
                bufferG = progressBarPanel.getGraphics();

                bufferG.setColor(progressBarPanel.getBackground());
                bufferG.fillRect(getX(), 0, getWidth(), progressBarPanel.getHeight());
                bufferG.drawImage(PROGRESS_BAR_ICON.getImage(), getX(), 0, 
                        (int) (getWidth() * getProgress() / getJobSize()), 
                        progressBarPanel.getHeight(), null);
                
                bufferG.setColor(Color.BLACK);
                if (getMessage() != null) {
                    
                    Font font = g.getFont();
                    int fontSize = font.getSize();
                    FontMetrics fm = g.getFontMetrics();
                    while (fm.getHeight() > progressBarPanel.getHeight() && fontSize > 0) {
                        font = font.deriveFont((float) (fontSize - 1));
                        g.setFont(font);
                        fm = g.getFontMetrics();
                        fontSize--;
                    }
                    
                    int fontX = (int) ((getWidth() / 2) - (fm.getStringBounds(getMessage(), g).getWidth() / 2)) + getX();
                    int fontY = (progressBarPanel.getHeight() / 2) + (fm.getHeight() / 2);
                    bufferG.drawString(getMessage(), fontX, fontY);
                }
                g.drawImage(buffer, getWidth(), progressBarPanel.getHeight(), null);
            } else {
                g.setColor(progressBarPanel.getBackground());
                g.fillRect(getX(), 0, getWidth(), progressBarPanel.getHeight());
            }
        }

        public synchronized void setX(int x) {
            this.x = x;
            paint();
        }

        public synchronized int getX() {
            return x;
        }

        public synchronized void setWidth(int width) {
            this.width = width;
            paint();
        }

        public synchronized int getWidth() {
            return width;
        }
        
    }

    /**
     * The actual panel that appears at the bottom of the ArchitectFrame.
     * Interesting things like status text and progress bars can be entered
     * here.
     */
    private final JPanel statusBar = new JPanel();

    /**
     * Different {@link ArchitectStatusBar} classes will be given this progress
     * bar and a section of it that they can repaint for a progress bar. This
     * allows the progress to be given to the user even if the EDT thread is
     * busy. If we tried to add in other panels and work with them the panel
     * would not be added until after the current work of the EDT was completed
     * which may be too late.
     */
    private final JPanel progressBarPanel = new JPanel();
    
    /**
     * The existing progress bars in this status bar.
     */
    @GuardedBy("this")
    private final List<ArchitectStatusProgressBar> progressBars = 
        new ArrayList<ArchitectStatusProgressBar>();
    
    public ArchitectStatusBar() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"), statusBar);
        builder.append(progressBarPanel);
    }
    
    public JPanel getStatusBar() {
        return statusBar;
    }

    @Override
    public MonitorableImpl createProgressMonitor() {
        ArchitectStatusProgressBar newBar = new ArchitectStatusProgressBar();
        synchronized(this) {
            progressBars.add(newBar);
        }
        resizeProgressBars();
        return newBar;
    }
    
    /**
     * Call to clear a progress bar off of the status bar.
     */
    private void removeProgressBar(ArchitectStatusProgressBar bar) {
        synchronized(this) {
            progressBars.remove(bar);
        }
        resizeProgressBars();
    }
    
    private void resizeProgressBars() {
        Graphics g = progressBarPanel.getGraphics();
        g.setColor(progressBarPanel.getBackground());
        g.fillRect(0, 0, progressBarPanel.getWidth(), progressBarPanel.getHeight());
        synchronized(this) {
            if (progressBars.isEmpty())  return;
            int newWidth = progressBarPanel.getWidth() / progressBars.size();
            int x = 0;
            for (ArchitectStatusProgressBar bar : progressBars) {
                bar.setX(x);
                bar.setWidth(newWidth);
                x += newWidth;
            }
        }
    }
    
}
