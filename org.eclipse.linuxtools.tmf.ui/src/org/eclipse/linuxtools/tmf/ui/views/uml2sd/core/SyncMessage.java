/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.Comparator;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SortSyncMessageComparator;

/**
 * A SyncMessage is a synchronous message which appear at the same event occurrence on both lifeline ends (sender and
 * receiver).<br>
 * A Sync message is usually drawn horizontally.<br>
 * <br>
 * <br>
 * Usage example:
 * 
 * <pre>
 * Frame frame;
 * Lifeline lifeLine1;
 * Lifeline lifeLine2;
 * 
 * SyncMessage message = new SyncMessage();
 * // Create a new event occurrence on each lifeline
 * lifeline1.getNewOccurrenceIndex();
 * lifeline2.getNewOccurrenceIndex();
 * // Set the message sender and receiver
 * message.setStartLifeline(lifeLine1);
 * message.setEndLifline(lifeline2);
 * message.setName(&quot;Message label&quot;);
 * // add the message to the frame
 * frame.addMessage(message);
 * </pre>
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @version 1.0
 * @author sveyrier
 * 
 */
public class SyncMessage extends BaseMessage implements ITimeRange {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The graphNode ID
     */
    public static final String SYNC_MESS_TAG = "SyncMessage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The associated message return
     */
    protected SyncMessageReturn messageReturn;
    /**
     * The time when the message occurs
     */
    protected ITmfTimestamp eventTime = new TmfTimestamp();
    /**
     * Flag whether the message has time information available or not
     */
    protected boolean hasTime = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public SyncMessage() {
        prefId = ISDPreferences.PREF_SYNC_MESS;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Ensure both lifelines have the same event occurrence (the greater found on each lifeline)
     */
    protected void syncLifelinesEventOccurrence() {
        if ((getStartLifeline() != null) && (getEndLifeline() != null)) {
            int newIndex = 0;
            if (getStartLifeline().getEventOccurrence() > getEndLifeline().getEventOccurrence()) {
                newIndex = getStartLifeline().getEventOccurrence();
            } else {
                newIndex = getEndLifeline().getEventOccurrence();
            }
            getStartLifeline().setCurrentEventOccurrence(newIndex);
            getEndLifeline().setCurrentEventOccurrence(newIndex);
            setEventOccurrence(getStartLifeline().getEventOccurrence());
        }
    }

    /**
     * Set the lifeLine from which the message has been sent.<br>
     * A new event occurrence will be created on this lifeLine.<br>
     * SyncMessage must occur at the same event occurrence on both lifeline, this method is responsible to synchronize the
     * event occurrence on each lifeline (the greater value will be used).<br>
     * This synchronization is only done if the end lifeline has already been set.
     * 
     * @param lifeline the message sender
     */
    public void autoSetStartLifeline(Lifeline lifeline) {
        lifeline.getNewEventOccurrence();
        setStartLifeline(lifeline);
    }

    /**
     * Set the lifeLine which has receiver the message.<br>
     * A new EventOccurence will be create on this lifeLine.<br>
     * SyncMessage must occur at the same event occurrence on both lifeline, this method is responsible to synchronize the
     * event occurrence on each lifeline (the greater value will be used).<br>
     * This synchronization is only done if the start lifeline has already been set.
     * 
     * @param lifeline the message receiver
     */
    public void autoSetEndLifeline(Lifeline lifeline) {
        lifeline.getNewEventOccurrence();
        setEndLifeline(lifeline);
    }

    /**
     * Set the lifeLine which has receiver the message.<br>
     * SyncMessage must occur at the same event occurrence on both lifeline, this method is responsible to synchronize the
     * event occurrence on each lifeline (the greater value will be used).<br>
     * This synchronization is only done if the start lifeline has already been set.
     * 
     * @param lifeline the message receiver
     */
    @Override
    public void setStartLifeline(Lifeline lifeline) {
        super.setStartLifeline(lifeline);
        if ((getEndLifeline() == null)) {
            setEventOccurrence(getStartLifeline().getEventOccurrence());
        } else {
            syncLifelinesEventOccurrence();
        }
    }

    /**
     * Set the lifeLine which has receiver the message.<br>
     * SyncMessage must occur at the same event occurrence on both lifelines, this method is responsible to synchronize the
     * event occurrence on each lifeline (the greater value will be used).<br>
     * This synchronization is only done if the start lifeline has already been set.
     * 
     * @param lifeline the message receiver
     */
    @Override
    public void setEndLifeline(Lifeline lifeline) {
        super.setEndLifeline(lifeline);
        if ((getStartLifeline() == null)) {
            setEventOccurrence(getEndLifeline().getEventOccurrence());
        } else {
            syncLifelinesEventOccurrence();
        }
    }

    /**
     * Set the event occurrence when this message occurs.<br>
     * 
     * @param occurrence the event occurrence to assign to this message.<br>
     * @see Lifeline Lifeline for more event occurence details
     */
    @Override
    protected void setEventOccurrence(int occurrence) {
        startEventOccurrence = occurrence;
        endEventOccurrence = occurrence;
    }

    /**
     * Set the message return associated with this message.
     * 
     * @param message the message return to associate
     */
    protected void setMessageReturn(SyncMessageReturn message) {
        messageReturn = message;
    }

    /**
     * Returns the syncMessageReturn associated to this syncMessage
     * 
     * @return the message return
     */
    public SyncMessageReturn getMessageReturn() {
        return messageReturn;
    }

    /**
     * Set the time when the message occurs
     * 
     * @param time the time when the message occurs
     */
    public void setTime(ITmfTimestamp time) {
        eventTime = time.clone();
        hasTime = true;
        if (getStartLifeline() != null && getStartLifeline().getFrame() != null) {
            getStartLifeline().getFrame().setHasTimeInfo(true);
        } else if (getEndLifeline() != null && getEndLifeline().getFrame() != null) {
            getEndLifeline().getFrame().setHasTimeInfo(true);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return eventTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#getStartTime()
     */ 
    @Override
    public ITmfTimestamp getStartTime() {
        return eventTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#hasTimeInfo()
     */ 
    @Override
    public boolean hasTimeInfo() {
        return hasTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    public void draw(IGC context) {
        if (!isVisible())
            return;
        // Draw it selected?
        if (!isSelected()) {
            context.setBackground(Frame.getUserPref().getBackGroundColor(prefId));
            context.setForeground(Frame.getUserPref().getForeGroundColor(prefId));
        }
        super.draw(context);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage#isVisible(int, int, int, int)
     */
    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        if (getY() > y + height +
                // take into account the message name drawn above the arrow
                Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth()) {
            return false;
        }

        // UML2 lost/found message visibility special case
        // Others visibility cases are perform in the ***common*** case
        if ((endLifeline == null && startLifeline != null) || (endLifeline != null && startLifeline == null)) {
            if (x + width > getX() + getWidth() && x < getX() + getWidth()) {
                return true;
            }
        }
        // ***Common*** syncMessages visibility
        return super.isVisible(x, y, width, height);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getComparator()
     */
    @Override
    public Comparator<GraphNode> getComparator() {
        return new SortSyncMessageComparator();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return SYNC_MESS_TAG;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#positiveDistanceToPoint(int, int)
     */
    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        if (getY() > y) {
            return true;
        }
        return false;
    }
}
