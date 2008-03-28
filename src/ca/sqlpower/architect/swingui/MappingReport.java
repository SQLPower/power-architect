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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.etl.ETLUtils;

public class MappingReport {
    private int sourceTargetGap = 100;
    private int unrelatedSourcesGap = 10;
    private int arrowheadLength = 10;
    private double arrowheadAngle = Math.PI / 6.0;
    Map<SQLTable, Collection<SQLTable>> mappings;
    Map<SQLTable, TablePane> panes = new HashMap<SQLTable, TablePane>();
    int maxSourceWidth = 0;
    int maxTargetWidth = 0;

    public MappingReport(ArchitectSwingSession session, Collection<SQLTable> targetTables) throws ArchitectException {
        PlayPen pp = new PlayPen(session);
        mappings = ETLUtils.findTableLevelMappings(targetTables);
        for (SQLTable sourceTable : mappings.keySet()) {
            if (sourceTable == null) continue;
            TablePane stp = new TablePane(sourceTable, pp);
            stp.setFullyQualifiedNameInHeader(true);
            panes.put(sourceTable, stp);
            maxSourceWidth = Math.max(maxSourceWidth, stp.getPreferredSize().width);
        }

        for (SQLTable targetTable : targetTables) {
            TablePane ttp = new TablePane(targetTable, pp);
            panes.put(targetTable, ttp);
            maxTargetWidth = Math.max(maxTargetWidth, ttp.getPreferredSize().width);
        }
        
        // This mapping report is not intended to be interactive, so we can
        // destroy the temporary playpen now.
        pp.destroy();
    }

    public Dimension drawHighLevelReport(Graphics2D g, Dimension pageSize) throws ArchitectException {

        int y = 0;
        for (Map.Entry<SQLTable, Collection<SQLTable>> entry : mappings.entrySet()) {
            SQLTable st = entry.getKey();
            Collection<SQLTable> targets = entry.getValue();
            if (pageSize != null) {
                int clusterSize = drawSourceTargetCluster(null, panes, maxSourceWidth, maxTargetWidth, y, st, targets);
                if ((y % pageSize.height)+(clusterSize%pageSize.height) >= pageSize.height){
                    y += pageSize.height - (y % pageSize.height);
                }
            }
            y = drawSourceTargetCluster(g, panes, maxSourceWidth, maxTargetWidth, y, st, targets);
        }
        return new Dimension(maxSourceWidth + sourceTargetGap + maxTargetWidth, y );
    }

    private int drawSourceTargetCluster(Graphics2D g, Map<SQLTable, TablePane> panes, int maxSourceWidth, int maxTargetWidth,  int sy,  SQLTable st, Collection<SQLTable> targets) {
        int sx = 0;
        int tx = maxSourceWidth + sourceTargetGap;
        int ty = sy;

        for (SQLTable targetTable : targets) {

            TablePane ttp = panes.get(targetTable);
            Dimension tpsize = ttp.getPreferredSize();
            ttp.setBounds(
                    tx + maxTargetWidth/2 - tpsize.width/2, ty,
                    tpsize.width, tpsize.height);
            if (g != null && g.hitClip(ttp.getX(),ttp.getY(),ttp.getWidth(),ttp.getHeight())) {
                g.translate(ttp.getX(), ttp.getY());
                ttp.paint(g);
                g.translate(-ttp.getX(), -ttp.getY());
            }
            ty += ttp.getHeight()+unrelatedSourcesGap;
        }
        int targetsHeight = ty - sy;
        TablePane stp = panes.get(st);
        if (stp != null) {
            Dimension stpsize = stp.getPreferredSize();
            stp.setBounds(
                    sx + maxSourceWidth/2 - stpsize.width/2, Math.max(sy, sy + targetsHeight/2 - stpsize.height/2),
                    stpsize.width, stpsize.height);
            if (g != null ) {
                if (g.hitClip(stp.getX(),stp.getY(),stp.getWidth(),stp.getHeight())) {
                    g.translate(stp.getX(), stp.getY());
                    stp.paint(g);
                    g.translate(-stp.getX(), -stp.getY());
                }
                for (SQLTable targetTable : targets) {
                    drawArrow(g,
                            stp.getBounds(),
                            panes.get(targetTable).getBounds());
                }
            }
            sy += unrelatedSourcesGap;
            sy += stp.getHeight();
        }
        else {
            sy += unrelatedSourcesGap;
        }
        sy = Math.max(ty, sy);
        return sy;
    }

    public Dimension getRequiredSize() throws ArchitectException {
        return drawHighLevelReport(null,null);
    }

    private void drawArrow(Graphics2D g, Rectangle from, Rectangle to) {
        GeneralPath arrow = new GeneralPath();
        Point2D.Float p1 = new Point2D.Float(from.width + from.x, from.height/2 + from.y);
        Point2D.Float p2 = new Point2D.Float(to.x, to.y + to.height/2);

        float length = (float) Math.sqrt(Math.pow(p2.x-p1.x, 2) + Math.pow(p2.y-p1.y, 2));

        // now do everything with respect to p1 and length

        Point2D.Float a = new Point2D.Float(p1.x + length - arrowheadLength , (float) (p1.y + arrowheadLength * Math.tan(arrowheadAngle)));
        arrow.moveTo(p1.x, p1.y);
        arrow.lineTo(p1.x + length, p1.y);
        arrow.moveTo(a.x, a.y);
        arrow.lineTo(p1.x + length, p1.y);
        arrow.lineTo(a.x, -(a.y - p1.y) + p1.y);

        arrow.transform(AffineTransform.getRotateInstance(Math.atan((p2.y-p1.y)/(p2.x-p1.x)), p1.x, p1.y));
        Stroke backupStroke = g.getStroke();
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(arrow);
        g.setStroke(backupStroke);
    }
}
