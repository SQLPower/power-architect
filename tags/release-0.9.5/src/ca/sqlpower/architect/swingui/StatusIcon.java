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
package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.Icon;

public abstract class StatusIcon implements Icon {

    private final static int DIAMETER = 15;

    public int getIconHeight() {
        return DIAMETER;
    }

    public int getIconWidth() {
        return DIAMETER;
    }
    private static ImageObserver dummyObserver = new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    };

    /** An error icon */
    private static final Icon FAIL_ICON = new StatusIcon() {
        final Image myImage = ASUtils.createIcon("stat_err_", "Failure", ArchitectSwingSessionContext.ICON_SIZE).getImage();
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
        }
    };

    /** A Warning Icon */
    private static final Icon WARN_ICON = new StatusIcon() {
        Image myImage = ASUtils.createIcon("stat_warn_", "Failure", ArchitectSwingSessionContext.ICON_SIZE).getImage();
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(myImage, x, y, dummyObserver);
        }
    };

    /** A blank icon of the right size, just to avoid resize flashing */
    private static final Icon NULL_ICON = new StatusIcon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // no painting required for null icon
        }
    };

    public static Icon getFailIcon() {
        return FAIL_ICON;
    }

    public static Icon getNullIcon() {
        return NULL_ICON;
    }

    public static Icon getWarnIcon() {
        return WARN_ICON;
    }

}
