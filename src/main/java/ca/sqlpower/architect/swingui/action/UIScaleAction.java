/*
 * Copyright (c) 2026, SQL Power Group Inc.
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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectFrame;

/**
 * Adjusts the global UI scale (all fonts and open windows) through the
 * {@link ArchitectFrame}. A positive {@code step} enlarges the UI, a negative
 * step shrinks it and a zero step resets it to 100%.
 */
public class UIScaleAction extends AbstractArchitectAction {

    public static final double STEP = 0.08;

    private final double step;

    public UIScaleAction(ArchitectFrame frame, double step,
            String name, String description, String icon) {
        super(frame, name, description, icon);
        this.step = step;
    }

    public void actionPerformed(ActionEvent e) {
        if (step == 0.0) {
            frame.resetUIScale();
        } else {
            frame.adjustUIScale(Math.pow(1.0 + STEP, step));
        }
    }
}