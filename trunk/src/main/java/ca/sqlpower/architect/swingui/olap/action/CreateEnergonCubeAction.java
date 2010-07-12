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

package ca.sqlpower.architect.swingui.olap.action;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Timer;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.action.AbstractArchitectAction;
import ca.sqlpower.swingui.SPSUtils;

public class CreateEnergonCubeAction extends AbstractArchitectAction {

    private final class FrameAdvancer implements ActionListener {
        private final JLabel energon;


        int frame;

        private FrameAdvancer(JLabel energon) {
            this.energon = energon;
        }

        public void actionPerformed(ActionEvent e) {
            if (frame >= frames.length) {
                timer.stop();
                return;
            }
            energon.setIcon(new CompositeIcon(bg, frames[frame++]));
        }
    }

    private ImageIcon bg;
    private Icon[] frames;
    private Timer timer;

    public CreateEnergonCubeAction(ArchitectSwingSession session, PlayPen pp) {
        super(
            session, pp, "New Energon Cube...", "Create a new Energon Cube",
            new ImageIcon(CreateEnergonCubeAction.class.getResource("energonCubeAdd.png")));
    }

    public void actionPerformed(ActionEvent e) {
        if (bg == null) {
            bg = new ImageIcon(getClass().getResource("energonCube_background.jpg"));
        }
        if (frames == null) {
            frames = new Icon[15];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = new ImageIcon(getClass().getResource("energonCube_anim_"+(i+1)+".png"));
            }
        }        
        final JLabel energon = new JLabel(bg);
        timer = new Timer(200, new FrameAdvancer(energon));
        
        JDialog d = SPSUtils.makeOwnedDialog(getPlaypen(), "New Energon Cube");
        d.setContentPane(energon);
        d.setModal(true);
        d.pack();
        timer.start();
        d.setVisible(true);
    }
    
    private static class CompositeIcon implements Icon {

        private final Icon bg;
        private final Icon overlay;

        CompositeIcon(Icon bg, Icon overlay) {
            this.bg = bg;
            this.overlay = overlay;
        }
        
        public int getIconHeight() {
            return bg.getIconHeight();
        }

        public int getIconWidth() {
            return bg.getIconWidth();
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            bg.paintIcon(c, g, x, y);
            overlay.paintIcon(c, g, x, y);
        }
        
    }
}
