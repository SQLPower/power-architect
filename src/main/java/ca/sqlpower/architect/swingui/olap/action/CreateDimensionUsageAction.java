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

import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Dimension;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.DimensionUsageEditPanel;
import ca.sqlpower.architect.swingui.olap.OSUtils;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * Creates a dimension usage after the user clicks a DimensionPane and a
 * CubePane.
 */
public class CreateDimensionUsageAction extends CreateUsageAction<DimensionPane, CubePane> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CreateDimensionUsageAction.class);

    public CreateDimensionUsageAction(ArchitectSwingSession session, PlayPen pp) {
        super(session, pp, DimensionPane.class, CubePane.class, "Dimension Usage", OSUtils.DIMENSION_USAGE_ADD_ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
    }

    @Override
    protected void createUsage(DimensionPane dp, CubePane cp) {
        final Dimension dimension = dp.getModel();
        final Cube cube = cp.getModel();
        if (OLAPUtil.isNameUnique(cp.getModel(), DimensionUsage.class, dimension.getName())) {
            getSession().getWorkspace().begin("Create dimension usage"); 
            final DimensionUsage du = new DimensionUsage();
            try {
                du.setName(dimension.getName());
                du.setSource(dimension.getName());
                cube.addChild(du);
                UsageComponent uc = new UsageComponent(getPlaypen().getContentPane(), du, dp, cp);
                getPlaypen().getContentPane().addChild(uc, getPlaypen().getContentPane().getChildren().size());
                getSession().getWorkspace().commit();
            } catch (Throwable e) {
                getSession().getWorkspace().rollback("Error occurred: " + e.toString());
                throw new RuntimeException(e);
            }
            
            try {
                final DataEntryPanel mep = new DimensionUsageEditPanel(du);
                Callable<Boolean> okCall = new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return mep.applyChanges();
                    }
                };
                Callable<Boolean> cancelCall = new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        du.getParent().removeChild(du);
                        return true;
                    }
                };
                JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                        mep,
                        SwingUtilities.getWindowAncestor(getPlaypen()),
                        "Dimension Usage Properties",
                        DataEntryPanelBuilder.OK_BUTTON_LABEL,
                        okCall,
                        cancelCall);
                d.setLocationRelativeTo(getPlaypen());
                d.setVisible(true);
            } catch (SQLObjectException e) {
                throw new SQLObjectRuntimeException(e);
            }
        } else {
            String errorMsg = "Cube Dimension \"" + dimension.getName() + "\" already exists in \"" +
                    cp.getModel().getName() + "\".\nDimension Usage was not created.";
            JOptionPane.showMessageDialog(getPlaypen(), errorMsg, "Duplicate Cube Dimension", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
