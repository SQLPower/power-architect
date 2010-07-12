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
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;

/**
 * Creates a cube usage after the user clicks a CubePane and a
 * VirtualCubePane.
 */
public class CreateCubeUsageAction extends CreateUsageAction<CubePane, VirtualCubePane> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CreateCubeUsageAction.class);

    public CreateCubeUsageAction(ArchitectSwingSession session, PlayPen pp) {
        super(session, pp, CubePane.class, VirtualCubePane.class, "Cube Usage", OSUtils.CUBE_USAGE_ADD_ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('x'));
    }

    @Override
    protected void createUsage(CubePane cp, VirtualCubePane vcp) {
        if (OLAPUtil.isNameUnique(vcp.getModel(), CubeUsage.class, cp.getModel().getName())) {            
            try {
                getSession().getWorkspace().begin("Creating cube usage");
                CubeUsage cu = new CubeUsage();
                cu.setCubeName(cp.getModel().getName());
                vcp.getModel().getCubeUsage().addCubeUsage(cu);
                UsageComponent uc = new UsageComponent(getPlaypen().getContentPane(), cu, cp, vcp);
                getPlaypen().getContentPane().addChild(uc, getPlaypen().getContentPane().getChildren().size());
                getSession().getWorkspace().commit();
            } catch (Throwable e) {
                getSession().getWorkspace().rollback("Error occurred: " + e.toString());
                throw new RuntimeException(e);
            }
        } else {
            String errorMsg = "Cube Usage \"" + cp.getModel().getName() + "\" alreadys exists in \"" +
                    vcp.getModel().getName() + "\"\nCube Usage was not created.";
            JOptionPane.showMessageDialog(getPlaypen(), errorMsg, "Duplicate Cube Usage", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
