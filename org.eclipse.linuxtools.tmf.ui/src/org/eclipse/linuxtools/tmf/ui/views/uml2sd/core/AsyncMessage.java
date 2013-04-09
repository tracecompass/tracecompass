/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.Comparator;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SortAsyncForBackward;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SortAsyncMessageComparator;

/**
 * A AsyncMessage is a asynchronous message which appear at two different event occurrences on each lifeline ends (sender
 * and receiver).<br>
 * <br>
 * <br>
 * Usage example:
 *
 * <pre>
 * Frame frame;
 * Lifeline lifeLine1;
 * Lifeline lifeLine2;
 *
 * AsyncMessage message = new AsyncMessage();
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
 * @see Lifeline Lifeline for more event occurence details
 * @version 1.0
 * @author sveyrier
 * @since 2.0
 */
public class AsyncMessage extends BaseMessage implements ITimeRange {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
    * The grahNode ID constant
    */
    public static final String ASYNC_MESS_TAG = "AsyncMessage"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Flag whether message has time information or not.
     */
    protected boolean fHasTime = false;
    /**
     * The time when the message begin
     */
    protected ITmfTimestamp fEndTime = new TmfTimestamp();
    /**
     * The time when the message end
     */
    protected ITmfTimestamp fStartTime = new TmfTimestamp();
    /**
     * The associated message.
     */
    protected AsyncMessageReturn fMessageReturn = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public AsyncMessage() {
        fPrefId = ISDPreferences.PREF_ASYNC_MESS;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public int getX() {
        int x = super.getX(true);
        int activationWidth = Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
        if ((fStartLifeline != null) && (fEndLifeline != null) && (fStartLifeline.getX() > fEndLifeline.getX())) {
            activationWidth = -activationWidth;
        }

        if (isMessageStartInActivation(fStartEventOccurrence)) {
            x = x + activationWidth;
        }
        return x;
    }

    @Override
    public int getY() {
        if ((fStartLifeline != null) && (fEndLifeline != null)) {
            return fEndLifeline.getY() + fEndLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fStartEventOccurrence;
        }
        return super.getY();
    }

    @Override
    public int getWidth() {
        int width = super.getWidth(true);
        int activationWidth = Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
        if ((fStartLifeline != null) && (fEndLifeline != null) && (fStartLifeline.getX() > fEndLifeline.getX())) {
            activationWidth = -activationWidth;
        }

        if (isMessageStartInActivation(fStartEventOccurrence)) {
            width = width - activationWidth;
        }

        if (isMessageEndInActivation(fEndEventOccurrence)) {
            width = width - activationWidth;
        }

        return width;
    }

    @Override
    public int getHeight() {
        if ((fStartLifeline != null) && (fEndLifeline != null)) {
            return (fEndLifeline.getY() + fEndLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fEndEventOccurrence) - getY();
        }
        return super.getHeight();
    }

    /**
     * Set the message return associated with this message.
     *
     * @param message the message return to associate
     */
    protected void setMessageReturn(AsyncMessageReturn message) {
        fMessageReturn = message;
    }

    /**
     * Set the event occurrence attached to this message for its end lifeline
     *
     * @param occurrence the event occurrence to set
     */
    public void setEndOccurrence(int occurrence) {
        fEndEventOccurrence = occurrence;
        if (getStartLifeline() == null) {
            fStartEventOccurrence = occurrence;
        }
        informFrame(getEndLifeline(), occurrence);
    }

    /**
     * Informs the given lifeline about the maximum occurrence if applicable.
     *
     * @param lifeLine
     *            Concerned lifeline
     * @param occurrence
     *            Occurrence number
     */
    protected void informFrame(Lifeline lifeLine, int occurrence) {
        if ((lifeLine != null) && (lifeLine.getFrame() != null) && (lifeLine.getFrame().getMaxEventOccurrence() < occurrence)) {
            lifeLine.getFrame().setMaxEventOccurrence(occurrence);
        }
    }

    /**
     * Set the event occurrence attached to this message for its start lifeline
     *
     * @param occurrence the event occurrence to set
     */
    public void setStartOccurrence(int occurrence) {
        fStartEventOccurrence = occurrence;
        if (getEndLifeline() == null) {
            fEndEventOccurrence = fStartEventOccurrence;
        }
        informFrame(getStartLifeline(), occurrence);
    }

    /**
     * Set the lifeLine which has sent the message.<br>
     * A new EventOccurence will be create on this lifeLine.<br>
     *
     * @param lifeline the message sender
     */
    public void autoSetStartLifeline(Lifeline lifeline) {
        lifeline.getNewEventOccurrence();
        setStartLifeline(lifeline);
    }

    /**
     * Set the lifeLine which has received the message.<br>
     * A new EventOccurence will be create on this lifeLine.<br>
     *
     * @param lifeline the message receiver
     */
    public void autoSetEndLifeline(Lifeline lifeline) {
        lifeline.getNewEventOccurrence();
        setEndLifeline(lifeline);
    }

    @Override
    public void setStartLifeline(Lifeline lifeline) {
        super.setStartLifeline(lifeline);
        setStartOccurrence(getStartLifeline().getEventOccurrence());
        if (getEndLifeline() == null) {
            fEndEventOccurrence = fStartEventOccurrence;
        }
    }

    @Override
    public void setEndLifeline(Lifeline lifeline) {
        super.setEndLifeline(lifeline);
        setEventOccurrence(getEndLifeline().getEventOccurrence());
    }

    /**
     * Returns true if the point C is on the segment defined with the point A and B
     *
     * @param xA point A x coordinate
     * @param yA point A y coordinate
     * @param xB point B x coordinate
     * @param yB point B y coordinate
     * @param xC point C x coordinate
     * @param yC point C y coordinate
     * @return Return true if the point C is on the segment defined with the point A and B, else otherwise
     */
    protected boolean isNearSegment(int xA, int yA, int xB, int yB, int xC, int yC) {
        if ((xA > xB) && (xC > xA)) {
            return false;
        }
        if ((xA < xB) && (xC > xB)) {
            return false;
        }
        if ((xA < xB) && (xC < xA)) {
            return false;
        }
        if ((xA > xB) && (xC < xB)) {
            return false;
        }
        double distAB = Math.sqrt((xB - xA) * (xB - xA) + (yB - yA) * (yB - yA));
        double scalar = ((xB - xA) * (xC - xA) + (yB - yA) * (yC - yA)) / distAB;
        double distAC = Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA));
        double distToSegment = Math.sqrt(Math.abs(distAC * distAC - scalar * scalar));
        if (distToSegment <= Metrics.MESSAGE_SELECTION_TOLERANCE) {
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(int x, int y) {
        // Is it a self message?
        if (fStartLifeline == fEndLifeline) {
            return super.contains(x, y);
        }
        if (isNearSegment(getX(), getY(), getX() + getWidth(), getY() + getHeight(), x, y)) {
            return true;
        }
        int messageMaxWidth = Metrics.swimmingLaneWidth() - Metrics.EXECUTION_OCCURRENCE_WIDTH;
        int nameWidth = getName().length() * Metrics.getAverageCharWidth();
        if (getName().length() * Metrics.getAverageCharWidth() > messageMaxWidth) {
            if (GraphNode.contains(getX(), getY() - Metrics.MESSAGES_NAME_SPACING - Metrics.getMessageFontHeigth(), messageMaxWidth, Metrics.getMessageFontHeigth(), x, y)) {
                return true;
            }
        } else {
            if (GraphNode.contains(getX() + (messageMaxWidth - nameWidth) / 2, getY() + getHeight() / 2 - Metrics.MESSAGES_NAME_SPACING - Metrics.getMessageFontHeigth(), nameWidth, Metrics.getMessageFontHeigth(), x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draws the asynchronous message using giving graphical context.
     *
     * @param context A graphical context to draw in.
     */
    protected void drawAsyncMessage(IGC context) {
        if (fStartLifeline != null && fEndLifeline != null && fStartLifeline == fEndLifeline && (fStartEventOccurrence != fEndEventOccurrence)) {
            int x = getX();
            int y = getY();
            int height = getHeight();
            int tempx = 0;
            boolean startInActivation = isMessageStartInActivation(fStartEventOccurrence);
            boolean endInActivation = isMessageEndInActivation(fEndEventOccurrence);

            if (endInActivation && !startInActivation) {
                tempx = Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
            }
            if (startInActivation && !endInActivation) {
                tempx = -Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
            }

            int tempy = Metrics.INTERNAL_MESSAGE_WIDTH / 2;
            if (getHeight() <= Metrics.INTERNAL_MESSAGE_WIDTH) {
                tempy = getHeight() / 2;
            }

            context.drawLine(x, y, x + Metrics.INTERNAL_MESSAGE_WIDTH / 2, y);
            context.drawLine(x + Metrics.INTERNAL_MESSAGE_WIDTH, y + tempy, x + Metrics.INTERNAL_MESSAGE_WIDTH, y + height - tempy);
            context.drawLine(x + tempx, y + height, x + Metrics.INTERNAL_MESSAGE_WIDTH / 2, y + height);

            Double xt = Double.valueOf(Math.cos(0.75) * 7);
            Double yt = Double.valueOf(Math.sin(0.75) * 7);

            context.drawLine(x + xt.intValue() + tempx, y + height + yt.intValue(), x + tempx, y + height);
            context.drawArc(x, y, Metrics.INTERNAL_MESSAGE_WIDTH, 2 * tempy, 0, 90);
            context.drawArc(x, y + height, Metrics.INTERNAL_MESSAGE_WIDTH, -2 * tempy, 0, -90);
            context.drawLine(x + xt.intValue() + tempx, y + height - yt.intValue(), x + tempx, y + height);

            context.drawTextTruncated(getName(), x + Metrics.INTERNAL_MESSAGE_WIDTH + Metrics.INTERNAL_MESSAGE_V_MARGIN, y, Metrics.swimmingLaneWidth() - Metrics.EXECUTION_OCCURRENCE_WIDTH + -Metrics.INTERNAL_MESSAGE_WIDTH,
                    +Metrics.MESSAGES_NAME_SPACING - Metrics.getMessageFontHeigth(), !isSelected());
        } else {
            super.draw(context);
        }
    }

    @Override
    public void draw(IGC context) {
        if (!isVisible()) {
            return;
        }

        ISDPreferences pref = SDViewPref.getInstance();

        // Draw it selected?
        if (isSelected() && (fStartLifeline != null && fEndLifeline != null && fStartLifeline == fEndLifeline && (fStartEventOccurrence != fEndEventOccurrence))) {
            /*
             * Draw it twice First time, bigger inverting selection colors Second time, regular drawing using selection
             * colors This create the highlight effect
             */
            context.setForeground(pref.getBackGroundColorSelection());
            context.setLineWidth(Metrics.SELECTION_LINE_WIDTH);
            drawAsyncMessage(context);
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
            // Second drawing is done after the else
        } else {
            context.setBackground(pref.getBackGroundColor(fPrefId));
            context.setForeground(pref.getForeGroundColor(fPrefId));
        }
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(true);
        }
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        drawAsyncMessage(context);
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(false);
        }
    }

    /**
     * Set the time when the message end
     *
     * @param time the time when the message end
     * @since 2.0
     */
    public void setEndTime(ITmfTimestamp time) {
        fEndTime = time;
        fHasTime = true;
        if (getStartLifeline() != null && getStartLifeline().getFrame() != null) {
            getStartLifeline().getFrame().setHasTimeInfo(true);
        } else if (getEndLifeline() != null && getEndLifeline().getFrame() != null) {
            getEndLifeline().getFrame().setHasTimeInfo(true);
        }
    }

    /**
     * Set the time when the message start
     *
     * @param time the time when the message start
     * @since 2.0
     */
    public void setStartTime(ITmfTimestamp time) {
        fStartTime = time;
        fHasTime = true;
        if (getStartLifeline() != null && getStartLifeline().getFrame() != null) {
            getStartLifeline().getFrame().setHasTimeInfo(true);
        } else if (getEndLifeline() != null && getEndLifeline().getFrame() != null) {
            getEndLifeline().getFrame().setHasTimeInfo(true);
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }

    @Override
    public boolean hasTimeInfo() {
        return fHasTime;
    }

    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        int toDrawY = getY();
        int toDrawHeight = getHeight();
        if ((toDrawY > y + height + Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth()) && (toDrawY + toDrawHeight > y + height + Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth())) {
            return false;
        }
        if (toDrawY < y && (toDrawY + toDrawHeight < y)) {
            return false;
        }
        return super.isVisible(x, y, width, height);
    }

    @Override
    public Comparator<GraphNode> getComparator() {
        return new SortAsyncMessageComparator();
    }

    @Override
    public String getArrayId() {
        return ASYNC_MESS_TAG;
    }

    @Override
    public Comparator<GraphNode> getBackComparator() {
        return new SortAsyncForBackward();
    }

    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        int mY = getY();
        int mH = getHeight();
        if ((mY > y) || (mY + mH > y)) {
            return true;
        }
        return false;
    }
}
