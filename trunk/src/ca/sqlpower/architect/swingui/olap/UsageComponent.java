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

package ca.sqlpower.architect.swingui.olap;

import java.awt.Color;
import java.awt.event.MouseEvent;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.event.SelectionEvent;

/**
 * A component that visually indicates a usage of one olap pane by another. For example,
 * a dimension usage or a cube usage.
 */
public class UsageComponent extends PlayPenComponent {

    private final OLAPObject model;
    private final OLAPPane<?, ?> pane1;
    private final OLAPPane<?, ?> pane2;

    public UsageComponent(PlayPenContentPane parent, OLAPObject model, OLAPPane<?, ?> pane1, OLAPPane<?, ?> pane2) {
        super(parent);
        this.model = model;
        this.pane1 = pane1;
        this.pane2 = pane2;
        setOpaque(false);
        setForegroundColor(Color.BLACK);
        updateUI();
    }
    
    /**
     * Creates and installs the UI delegate. This generic UsageComponent creates
     * a UsageComponentUI, which should be sufficient for most purposes. If your
     * subclass needs a different UI, override this method and install a different
     * one.
     * <p>
     * Nasty warning: If you override this method, be aware that it's going to get
     * called before your constructor has been invoked. How weird is that?
     */
    protected void updateUI() {
        UsageComponentUI ui = new UsageComponentUI();
        ui.installUI(this);
        setUI(ui);
    }

    @Override
    public OLAPObject getModel() {
        return model;
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public void handleMouseEvent(MouseEvent evt) {
        PlayPen pp = getPlayPen();
        if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            if (!isSelected()) {
                pp.selectNone();
                setSelected(true, SelectionEvent.SINGLE_SELECT);
            }
        } else if (evt.getID() == MouseEvent.MOUSE_MOVED || evt.getID() == MouseEvent.MOUSE_DRAGGED) {
            setSelected(getUI().intersects(pp.getRubberBand()), SelectionEvent.SINGLE_SELECT);
        }
    }

    @Override
    public UsageComponentUI getUI() {
        return (UsageComponentUI) super.getUI();
    }
    
    /**
     * Returns one of the panes that this usage component connects together.
     */
    public OLAPPane<?, ?> getPane1() {
        return pane1;
    }
    
    /**
     * Returns one of the panes that this usage component connects together.
     */
    public OLAPPane<?, ?> getPane2() {
        return pane2;
    }

}
