/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.TablePane;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.SPSUtils;

public class InvadersAction extends AbstractArchitectAction {

    public InvadersAction(ArchitectFrame frame) {
        super(frame, "Architect Invaders", "Defend your data model against interplanetary attack");
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            Game game = new Game(getSession());
            game.start();
        } catch (SQLObjectException ex) {
            SPSUtils.showExceptionDialogNoReport(getSession().getArchitectFrame(), "FAIL", null, ex);
        }
    }
    
    private static class Game implements ActionListener, MouseListener {
        
        private final ArchitectSwingSession session;
        private final PlayPen playpen;
        private final Timer timer = new Timer(100, this);
        private final Random random = new Random();
        
        // geometry
        private final Dimension playfieldSize = new Dimension(640, 600);
        
        // bad guy stuff
        private final List<TablePane> baddies = new ArrayList<TablePane>();
        private final List<Bullet> poops = new ArrayList<Bullet>();
        private enum BaddyState { LEFT, RIGHT }
        private BaddyState baddyState = BaddyState.RIGHT;
        private static final int LINE_SPACING = 70;
        private static final int LINEFEED_SIZE = 50;
        private String legs = "/|\\";
        private static final int POOP_VELOCITY = 3;
        private TablePane mothership = null;
        private int lastMothershipAppearance = 500;
        
        // good guy stuff
        private final List<TablePane> bases = new ArrayList<TablePane>();
        private boolean fireNextTurn = false;
        private Bullet bullet;
        private TablePane player;
        private static final int BULLET_VELOCITY = -19;
        
        // Score keeping
        private int score = 0;
        private static final int BADDY_ELIMINATION_SCORE = 100;
        private static final int MOTHERSHIP_ELIMINATION_SCORE = 10000;
        private static final int LIFE_BONUS_SCORE = 1000;
        
        // Special message
        private int messageTTL = 0;
        private TablePane message;
        
        Game(ArchitectSwingSession session) throws SQLObjectException {
            this.session = session;
            this.playpen = session.getPlayPen();

            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 4; y++) {
                    TablePane baddy = makeTable(
                            x * (playfieldSize.width / 10),
                            (y + 1) * LINE_SPACING,
                            "Baddy", legs);
                    baddies.add(baddy);
                }
            }

            for (int x = 0; x < 3; x++) {
                TablePane base = makeTable(
                        (int) (10 + x * (playfieldSize.width / 3.0)),
                        playfieldSize.height - 150,
                        "Base", "Fortification----------", "Fortification-----", "Fortification");
                bases.add(base);
            }
            
            player = makeTable(
                    playfieldSize.width / 2,
                    playfieldSize.height - 50,
                    "0", "Life 1", "Life 2", "Life 3");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                
                // If there is a special message, we pause while it's visible
                if (messageTTL > 0) {
                    messageTTL--;
                    return;
                } else if (message != null) {
                    message.getModel().getParent().removeChild(message.getModel());
                }
                
                // detect hits from poops (on bases and player)
                for (Iterator<Bullet> poopIt = poops.iterator(); poopIt.hasNext(); ) {
                    Bullet poop = poopIt.next();
                    Point poopPosition = poop.getPosition();
                    
                    for (Iterator<TablePane> baseIt = bases.iterator(); baseIt.hasNext(); ) {
                        TablePane base = baseIt.next();
                        
                        if (base.contains(poopPosition)) {
                            poopIt.remove();
                            poop.destroy();
                            
                            if (processBaseHit(base)) {
                                baseIt.remove();
                            }
                        }
                    }
                    
                    if (player.contains(poopPosition)) {
                        poopIt.remove();
                        poop.destroy();
                        
                        endOfTurn();
                        return;
                    }
                }
                
                
                // move baddies and detect hits on them
                if (legs == "/|\\") {
                    legs = "|||";
                } else {
                    legs = "/|\\";
                }

                BaddyState nextBaddyState = null;
                for (Iterator<TablePane> baddyIt = baddies.iterator(); baddyIt.hasNext(); ) {
                    TablePane baddy = baddyIt.next();
                    
                    // hit detection -- bullet
                    if (bullet != null && baddy.contains(bullet.getPosition())) {
                        score += BADDY_ELIMINATION_SCORE;
                        baddyIt.remove();
                        baddy.getModel().getParent().removeChild(baddy.getModel());
                        
                        bullet.destroy();
                        bullet = null;
                        
                        continue;
                    }
                    
                    Rectangle bb = baddy.getBounds();
                    
                    // hit detection -- bases
                    for (Iterator<TablePane> baseIt = bases.iterator(); baseIt.hasNext(); ) {
                        TablePane base = baseIt.next();

                        if (bb.intersects(base.getBounds())) {
                            baddyIt.remove();
                            baddy.getModel().getParent().removeChild(baddy.getModel());

                            if (processBaseHit(base)) {
                                baseIt.remove();
                            }
                        }
                    }
                    
                    // hit detection -- player
                    if (baddy.getBounds().intersects(player.getBounds())) {
                        endOfTurn();
                        return;
                    }
                    
                    // movement
                    baddy.getModel().getColumn(0).setName(legs);
                    if (baddyState == BaddyState.LEFT) {
                        baddy.setLocation(baddy.getX() - 1, baddy.getY());
                        if (baddy.getX() <= 0) {
                            nextBaddyState = BaddyState.RIGHT;
                        }
                    } else if (baddyState == BaddyState.RIGHT) {
                        baddy.setLocation(baddy.getX() + 1, baddy.getY());
                        if (baddy.getX() >= playfieldSize.width) {
                            nextBaddyState = BaddyState.LEFT;
                        }
                    }
                    
                    if (poops.size() <= 3 && random.nextInt(100) == 0) {
                        Bullet poop = new Bullet(
                                baddy.getX() + (baddy.getWidth() / 2),
                                baddy.getY() + baddy.getHeight(),
                                POOP_VELOCITY);
                        poops.add(poop);
                    }
                }

                if (nextBaddyState != null) {
                    for (TablePane baddy : baddies) {
                        baddy.setLocation(baddy.getX(), baddy.getY() + LINEFEED_SIZE);
                    }
                    baddyState = nextBaddyState;
                }
                
                // mothership hit detection, movement, and creation
                if (mothership != null) {
                    mothership.setLocation(mothership.getX() - 8, mothership.getY());
                    
                    boolean hit = false;
                    if (bullet != null && mothership.contains(bullet.getPosition())) {
                        score += MOTHERSHIP_ELIMINATION_SCORE;
                        hit = true;
                    }
                    
                    if (hit || mothership.getX() < 0) {
                        mothership.getModel().getParent().removeChild(mothership.getModel());
                        mothership = null;
                    }
                } else if (lastMothershipAppearance < score && score % 1000 == 0) {
                    mothership = makeTable(playfieldSize.width, 20, "Mothership");
                }

                // process input (move player, fire bullet)
                Point position = playpen.getMousePosition();
                if (position != null) {
                    player.setLocation(position.x, player.getY());
                }
                if (fireNextTurn) {
                    if (bullet == null) {
                        bullet = new Bullet(
                                player.getX() + player.getWidth() / 2,
                                player.getY(),
                                BULLET_VELOCITY);
                    }
                    fireNextTurn = false;
                }

                // move bullets
                if (bullet != null) {
                    bullet.move();
                    if (bullet.getPosition().y < 0) {
                        bullet.destroy();
                        bullet = null;
                    }
                }
                
                for (Iterator<Bullet> it = poops.iterator(); it.hasNext(); ) {
                    Bullet poop = it.next();
                    poop.move();
                    if (poop.getPosition().y > playfieldSize.height) {
                        poop.destroy();
                        it.remove();
                    }
                }
                
                if (mothership == null && baddies.isEmpty()) {
                    winGame();
                    return;
                }
                
                // update score display
                player.getModel().setName(String.valueOf(score));
                
            } catch (SQLObjectException ex) {
                stop();
                throw new SQLObjectRuntimeException(ex);
            } catch (ObjectDependentException ex) {
                stop();
                throw new RuntimeException(ex);
            }
        }
        
        /**
         * Reduces the size of the given base. Returns true if the base should
         * be removed from the bases list, or false if it should remain in play.
         * 
         * @return
         * @throws SQLObjectException 
         */
        private boolean processBaseHit(TablePane base) throws ObjectDependentException, SQLObjectException {
            SQLTable baseTable = base.getModel();
            if (baseTable.getColumns().size() > 0) {
                baseTable.removeColumn(0);
                return false;
            } else {
                baseTable.getParent().removeChild(baseTable);
                return true;
            }

        }
        
        private void endOfTurn() throws SQLObjectException {
            SQLTable playerTable = player.getModel();
            if (playerTable.getColumns().size() <= 1) {
                // end of game
                stop();
                
                if (!playerTable.getColumns().isEmpty()) {
                    playerTable.removeColumn(0);
                }
                
                message = makeTable(
                        playfieldSize.width / 4, playfieldSize.height / 4,
                        "Game Over", "Your Score: " + score);
                message.setMinimumSize(
                        new Dimension(
                                playfieldSize.width / 2,
                                playfieldSize.height / 2));
                message.revalidate();
            } else {
                // end of this turn
                playerTable.removeColumn(playerTable.getColumns().size() - 1);
                messageTTL = 1000 / timer.getDelay();
                
                for (Bullet poop : poops) {
                    poop.destroy();
                }
                poops.clear();
            }
        }

        private void winGame() throws SQLObjectException {
            stop();
            SQLTable playerTable = player.getModel();

            int lifeBonus =  playerTable.getColumns().size() * LIFE_BONUS_SCORE;
            score += lifeBonus;

            message = makeTable(
                    playfieldSize.width / 4, playfieldSize.height / 4,
                    "Game Over",
                    "Bonus for remaining lives: " + lifeBonus,
                    "Your Score: " + score);
            message.setMinimumSize(
                    new Dimension(
                            playfieldSize.width / 2,
                            playfieldSize.height / 2));
            message.revalidate();
        }

        public void start() {
            playpen.addMouseListener(this);
            timer.start();
        }
        
        public void stop() {
            playpen.removeMouseListener(this);
            timer.stop();
        }

        /**
         * Creates a SQLTable and its TablePane. Both are added to the session's
         * project.
         * 
         * @param x
         *            The X position at which to add the new table pane
         * @param y
         *            The Y position at which to add the new table pane
         * @param name
         *            The table's name
         * @param columnNames
         *            The names of zero or more columns to add to the new table
         * @return The new TablePane that was created
         */
        private TablePane makeTable(int x, int y, String name, String ... columnNames)
        throws SQLObjectException {
            
            SQLTable t = new SQLTable();
            t.initFolders(true);
            t.setName(name);
            for (String cn : columnNames) {
                t.addColumn(new SQLColumn(t, cn, Types.BIT, 0, 0));
            }
            
            TablePane tp = new TablePane(t, playpen.getContentPane());
            tp.setMinimumSize(new Dimension(1, 0));
            session.getTargetDatabase().addChild(tp.getModel());
            playpen.addTablePane(tp, new Point(x, y));
            
            return tp;
        }
        
        private class Bullet {
            private final int velocity;
            private final Relationship r;
            private final SQLTable top;
            private final SQLTable bot;
            private final TablePane tpTop;
            private final TablePane tpBot;
            
            Bullet(int startx, int starty, int velocity) throws SQLObjectException {
                this.velocity = velocity;
                top = new SQLTable();
                top.initFolders(true);
                top.setName("");
                tpTop = new TablePane(top, playpen.getContentPane());
                tpTop.setMinimumSize(new Dimension(20, 0));
                session.getTargetDatabase().addChild(tpTop.getModel());
                playpen.addTablePane(tpTop, new Point(startx, starty));
                tpTop.revalidate();
                
                bot = new SQLTable();
                bot.initFolders(true);
                bot.setName("");
                tpBot = new TablePane(bot, playpen.getContentPane());
                tpBot.setMinimumSize(new Dimension(20, 0));
                session.getTargetDatabase().addChild(tpBot.getModel());
                playpen.addTablePane(tpBot, new Point(tpTop.getX(), tpTop.getY() + tpTop.getHeight() + 30));

                SQLRelationship sr = SQLRelationship.createRelationship(top, bot, true);
                r = new Relationship(sr, playpen.getContentPane());
                playpen.addRelationship(r);
            }
            
            void destroy() {
                SQLDatabase db = session.getTargetDatabase();
                try {
                    db.removeChild(top);
                    db.removeChild(bot);
                } catch (ObjectDependentException e) {
                    throw new RuntimeException(e);
                }
            }
            
            void move() {
                tpTop.setLocation(tpTop.getX(), tpTop.getY() + velocity);
                tpBot.setLocation(tpBot.getX(), tpBot.getY() + velocity);
            }
            
            /** Returns the "live" point of this bullet (where hit detection should take place). */
            Point getPosition() {
                if (velocity < 0) {
                    return new Point(tpTop.getX(), tpTop.getY() + tpTop.getHeight());
                } else {
                    return new Point(tpBot.getX(), tpBot.getY());
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
            fireNextTurn = true;
        }

        public void mouseEntered(MouseEvent e) {
            // don't care
        }

        public void mouseExited(MouseEvent e) {
            // don't care
        }

        public void mousePressed(MouseEvent e) {
            // don't care
        }

        public void mouseReleased(MouseEvent e) {
            // don't care
        }

    }
    
}
