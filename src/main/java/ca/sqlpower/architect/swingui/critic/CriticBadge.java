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

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;

/**
 * A badge that appears in the play pen beside the object that is being
 * criticized.
 */
public class CriticBadge extends PlayPenComponent {

    /**
     * The criticisms that this badge represents.
     */
    private final List<Criticism> criticisms;
    
    /**
     * The subject of the criticisms of this badge. All criticisms of this badge
     * must be about this object.
     */
    private final Object subject;
    
    /**
     * A play pen component that will tell us where to display the critic badge.
     */
    private final PlayPenComponent UIOfSubject;
    
    /**
     * Listener attached to the {@link #UIOfSubject} object to be notified when
     * it moves. This lets us correct our location to give chase.
     */
    private final SPListener UISubjectMoveListener = new AbstractSPListener() {
        
        @Override
        public void propertyChanged(PropertyChangeEvent evt) {
            revalidate();
        }
    };

    /**
     * Listens for changes to the UISubject. If the subject is removed this
     * badge will be removed as well.
     */
    private final SPListener UISubjectRemovedListener = new AbstractPoolingSPListener() {
        
        @Override
        public void childRemovedImpl(SPChildEvent e) {
            if (e.getChild().equals(UIOfSubject)) {
                getParent().removeCriticBadge(CriticBadge.this);
                cleanup();
            }
        }
        
        @Override
        protected void childAddedImpl(SPChildEvent e) {
            if (e.getChild().equals(UIOfSubject)) {
                getParent().addCriticBadge(CriticBadge.this);
                connect();
            }
        }
    };

    /**
     * Listens for a removal of the subject object. If the model is removed this
     * badge will be removed as well. This can only be done for SPObjects as we
     * know how to check if an SPObject is being removed from its parent.
     */
    private final SPListener SubjectRemovedListener = new AbstractPoolingSPListener() {
        
        public void childRemovedImpl(SPChildEvent e) {
            if (e.getChild().equals(subject)) {
                getParent().removeCriticBadge(CriticBadge.this);
                cleanup();
            }
        }
        
        @Override
        protected void childAddedImpl(SPChildEvent e) {
            if (e.getChild().equals(subject)) {
                getParent().addCriticBadge(CriticBadge.this);
                connect();
            }
        }
    };
    
    public CriticBadge(List<Criticism> criticisms, Object subject, PlayPenComponent UIOfSubject) {
        super("Criticism of " + subject.toString());
        this.criticisms = new ArrayList<Criticism>(criticisms);
        this.subject = subject;
        this.UIOfSubject = UIOfSubject;
        BasicCriticBadgeUI ui = new BasicCriticBadgeUI(this);
        ui.installUI(this);
        setUI(ui);
        updateToolTipText();
        connect();
    }
    
    @Override
    public void connect() {
        UIOfSubject.addSPListener(UISubjectMoveListener);
        if (UIOfSubject.getParent() != null) {
            UIOfSubject.getParent().addSPListener(UISubjectRemovedListener);
        }
        if (subject instanceof SPObject && ((SPObject) subject).getParent() != null) {
            ((SPObject) subject).getParent().addSPListener(SubjectRemovedListener);
        }
    }
    
    public CleanupExceptions cleanup() {
        UIOfSubject.removeSPListener(UISubjectMoveListener);
        if (UIOfSubject.getParent() != null) {
            UIOfSubject.getParent().removeSPListener(UISubjectRemovedListener);
        }
        if (subject instanceof SPObject && ((SPObject) subject).getParent() != null) {
            ((SPObject) subject).getParent().removeSPListener(SubjectRemovedListener);
        }
        return new CleanupExceptions();
    }
    
    @Override
    public List<Criticism> getModel() {
        return criticisms;
    }

    @Override
    public String getModelName() {
        return getSubject().toString();
    }
    
    private void updateToolTipText() {
        StringBuffer buffer = new StringBuffer();
        for (Criticism criticism : criticisms) {
            buffer.append(criticism.getDescription() + "\n");
        }
        setToolTipText(buffer.toString());
    }
    
    @Override
    public void handleMouseEvent(MouseEvent evt) {
        // TODO left click should highlight criticisms in the panel in the play pen
        // TODO right click should give a menu option to ignore a critic on the subject.
    }

    /**
     * Adds a criticism to this badge. The criticism must be on the subject of
     * this badge.
     */
    public void addCriticism(Criticism c) {
        if (!c.getSubject().equals(getSubject())) throw new IllegalArgumentException(
                "Subject of new critic " + c.getSubject() + " is not " + getSubject());
        criticisms.add(c);
        updateToolTipText();
    }
    
    public void removeCriticism(Criticism c) {
        criticisms.remove(c);
        if (criticisms.isEmpty()) {
            getParent().removeCriticBadge(CriticBadge.this);
            cleanup();
        }
    }
    
    public List<Criticism> getCriticisms() {
        return Collections.unmodifiableList(criticisms);
    }

    public PlayPenComponent getUIOfSubject() {
        return UIOfSubject;
    }

    public Object getSubject() {
        return subject;
    }

    @Override
    public void setParent(SPObject parent) {
        super.setParent(parent);
        revalidate();
    }
    
}
