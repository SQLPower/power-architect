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

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.UsageComponent;

/**
 * Creates a dimension usage after the user clicks a DimensionPane and a
 * CubePane.
 */
public class CreateDimensionUsageAction extends CreateUsageAction<DimensionPane, CubePane> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CreateDimensionUsageAction.class);

    public CreateDimensionUsageAction(ArchitectSwingSession session, PlayPen pp) {
        super(session, pp, DimensionPane.class, CubePane.class, "Dimension Usage");
    }

    @Override
    protected void createUsage(DimensionPane dp, CubePane cp) {
        if (OLAPUtil.isNameUnique(cp.getModel(), DimensionUsage.class, dp.getModel().getName())) {
            DimensionUsage du = new DimensionUsage();
            du.setName(dp.getModel().getName());
            du.setSource(dp.getModel().getName());
            cp.getModel().addChild(du);
            UsageComponent uc = new UsageComponent(playpen.getContentPane(), du, dp, cp);
            playpen.getContentPane().add(uc, playpen.getContentPane().getComponentCount());
        } else {
            String errorMsg = "Cube Dimension \"" + dp.getModel().getName() + "\" already exists in \"" +
                    cp.getModel().getName() + "\".\nDimension Usage was not created.";
            JOptionPane.showMessageDialog(playpen, errorMsg, "Duplicate Cube Dimension", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
