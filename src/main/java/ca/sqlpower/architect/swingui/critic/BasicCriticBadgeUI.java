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

package ca.sqlpower.architect.swingui.critic;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentUI;

/**
 * Simple critic badge UI.
 */
public class BasicCriticBadgeUI implements PlayPenComponentUI {
    
    private final CriticBadge model;

    public BasicCriticBadgeUI(CriticBadge model) {
        this.model = model;
    }

    public boolean contains(Point p) {
        Point location = model.getLocation();
        return (location.x < p.x && p.x < location.x + getPreferredSize().width && 
                location.y < p.y && p.y < location.y + getPreferredSize().height);
    }

    public Dimension getPreferredSize() {
        //All icons used by this UI must be of the same size or this will fail.
        return new Dimension(CriticSwingUtil.ERROR_ICON.getIconWidth(), 
                CriticSwingUtil.ERROR_ICON.getIconHeight());
    }

    public void installUI(PlayPenComponent c) {
        // TODO add listener to badge that is notified when there are changes
        //to the criticisms being displayed.

    }

    public void paint(Graphics2D g2) {
        ImageIcon icon = CriticSwingUtil.WARNING_ICON;
        for (Criticism criticism : model.getCriticisms()) {
            if (criticism.getCritic().getSeverity().equals(Severity.ERROR)) {
                icon = CriticSwingUtil.ERROR_ICON;
                break;
            }
        }
        g2.drawImage(icon.getImage(), 0, 0, null);
    }

    public void revalidate() {
        Point badgePoint = model.getUIOfSubject().getPointForModelObject(model.getSubject());
        int xLoc = badgePoint.x - getPreferredSize().width;
        int yLoc = badgePoint.y;
        if (xLoc != model.getLocation().x || yLoc != model.getLocation().y) {
            model.setLocation(new Point(xLoc, yLoc));
        }
    }

    public void uninstallUI(PlayPenComponent c) {
        // TODO remove listeners added in installUI
    }

    public Point getPointForModelObject(Object modelObject) {
        return model.getLocation();
    }
}
