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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * The Frame class is the base sequence diagram graph nodes container.<br>
 * For instance, only one frame can be drawn in the View.<br>
 * Lifelines, Messages and Stop which are supposed to represent a Sequence diagram are drawn in a Frame.<br>
 * Only the graph node added to their representing list will be drawn.
 *
 * The lifelines are appended along the X axsis when added in a frame.<br>
 * The syncMessages are ordered along the Y axsis depending on the event occurrence they are attached to.<br>
 *
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @author sveyrier
 * @version 1.0
 */
public class BasicFrame extends GraphNode {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Contains the max elapsed time between two consecutive messages in the whole frame
     */
    private ITmfTimestamp fMaxTime = new TmfTimestamp(0);
    /**
     * Contains the min elapsed time between two consecutive messages in the whole frame
     */
    private ITmfTimestamp fMinTime = new TmfTimestamp(0);
    /**
     * Indicate if the min and max elapsed time between two consecutive messages in the whole frame need to be computed
     */
    private boolean fComputeMinMax = true;
    /**
     * Store the preference set by the user regarding the external time. This flag is used determine if the min and max
     * need to be recomputed in case this preference is changed.
     */
    private boolean fLastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();
    /**
     * The greater event occurrence created on graph nodes drawn in this Frame This directly impact the Frame height
     */
    private int fVerticalIndex = 0;
    /**
     * The index along the x axis where the next lifeline will is drawn This directly impact the Frame width
     */
    private int fHorizontalIndex = 0;
    /**
     * The time information flag.
     */
    private boolean fHasTimeInfo = false;
    /**
     * The current Frame visible area - x coordinates
     */
    private int fVisibleAreaX;
    /**
     * The current Frame visible area - y coordinates
     */
    private int fVisibleAreaY;
    /**
     * The current Frame visible area - width
     */
    private int fVisibleAreaWidth;
    /**
     * The current Frame visible area - height
     */
    private int fVisibleAreaHeight;
    /**
     * The event occurrence spacing (-1 for none)
     */
    private int fForceEventOccurrenceSpacing = -1;
    /**
     * Flag to indicate customized minumum and maximum.
     */
    private boolean fCustomMinMax = false;
    /**
     * The minimum time between messages of the sequence diagram frame.
     */
    private ITmfTimestamp fMinSDTime = new TmfTimestamp();
    /**
     * The maximum time between messages of the sequence diagram frame.
     */
    private ITmfTimestamp fMaxSDTime = new TmfTimestamp();
    /**
     * Flag to indicate that initial minimum has to be computed.
     */
    private boolean fInitSDMin = true;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates an empty frame.
     */
    public BasicFrame() {
        Metrics.setForcedEventSpacing(fForceEventOccurrenceSpacing);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     *
     * Returns the greater event occurence known by the Frame
     *
     * @return the greater event occurrence
     */
    protected int getMaxEventOccurrence() {
        return fVerticalIndex;
    }

    /**
     * Set the greater event occurrence created in GraphNodes included in the frame
     *
     * @param eventOccurrence the new greater event occurrence
     */
    protected void setMaxEventOccurrence(int eventOccurrence) {
        fVerticalIndex = eventOccurrence;
    }

    /**
     * This method increase the lifeline place holder The return value is usually assign to a lifeline. This can be used
     * to set the lifelines drawing order. Also, calling this method two times and assigning only the last given index
     * to a lifeline will increase this lifeline draw spacing (2 times the default spacing) from the last added
     * lifeline.
     *
     * @return a new lifeline index
     */
    protected int getNewHorizontalIndex() {
        return ++fHorizontalIndex;
    }

    /**
     * Returns the current horizontal index
     *
     * @return the current horizontal index
     * @see Frame#getNewHorizontalIndex() for horizontal index description
     */
    protected int getHorizontalIndex() {
        return fHorizontalIndex;
    }

    @Override
    public void addNode(GraphNode nodeToAdd) {
        setComputeMinMax(true);
        super.addNode(nodeToAdd);
    }

    @Override
    public int getX() {
        return Metrics.FRAME_H_MARGIN;
    }

    @Override
    public int getY() {
        return Metrics.FRAME_V_MARGIN;
    }

    @Override
    public int getWidth() {
        if (fHorizontalIndex == 0) {
            return 3 * Metrics.swimmingLaneWidth() + Metrics.LIFELINE_H_MAGIN * 2 - Metrics.FRAME_H_MARGIN - Metrics.LIFELINE_SPACING / 2;
        }
        return fHorizontalIndex * Metrics.swimmingLaneWidth() + Metrics.LIFELINE_H_MAGIN * 2 + 1 - Metrics.LIFELINE_SPACING;
    }

    @Override
    public int getHeight() {
        // The Frame height depends on the maximum number of messages added to a lifeline
        if (fVerticalIndex == 0) {
            return 5 * (Metrics.getMessagesSpacing() + Metrics.getMessageFontHeigth()) + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getFrameFontHeigth() + Metrics.LIFELINE_VT_MAGIN + Metrics.LIFELINE_VB_MAGIN
                    + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getLifelineFontHeigth() * 2;
        }
        if (fForceEventOccurrenceSpacing >= 0) {
            Metrics.setForcedEventSpacing(fForceEventOccurrenceSpacing);
        }
        return fVerticalIndex * (Metrics.getMessagesSpacing() + Metrics.getMessageFontHeigth()) + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getFrameFontHeigth() + Metrics.LIFELINE_VT_MAGIN + Metrics.LIFELINE_VB_MAGIN
                + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getLifelineFontHeigth() * 2;
    }

    /**
     * @return true if mininum and maximum time needs to be calculated else false
     * @since 2.0
     */
    protected boolean isComputeMinMax() {
        return fComputeMinMax;
    }

    /**
     * @return true if mininum and maximum time needs to be calculated else false
     * @since 2.0
     */
    protected boolean isCustomMinMax() {
        return fCustomMinMax;
    }

    /**
     * gets the initialization flag for SD minimum.
     *
     * @return the initialization flag for SD minimum
     * @since 2.0
     */
    protected boolean getInitSDMin() {
        return fInitSDMin;
    }

    /**
     * Returns the graph node which contains the point given in parameter for the given graph node list and starting the
     * iteration at the given index<br>
     * WARNING: Only graph nodes with smaller coordinates than the current visible area can be returned.<br>
     *
     * @param x the x coordinate of the point to test
     * @param y the y coordinate of the point to test
     * @param list the list to search in
     * @param fromIndex list browsing starting point
     * @return the graph node containing the point given in parameter, null otherwise
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getNodeFromListAt(int, int, java.util.List, int)
     */
    @Override
    protected GraphNode getNodeFromListAt(int x, int y, List<GraphNode> list, int fromIndex) {
        if (list == null) {
            return null;
        }
        for (int i = fromIndex; i < list.size(); i++) {
            GraphNode node = list.get(i);
            // only lifeline list is x ordered
            // Stop browsing the list if the node is outside the visible area
            // all others nodes will be not visible
            if ((node instanceof Lifeline) && (node.getX() > fVisibleAreaX + fVisibleAreaWidth)) {
                break;
            }
            if (node.getHeight() < 0) {
                if (node.getY() + node.getHeight() > fVisibleAreaY + fVisibleAreaHeight) {
                    break;
                }
            } else {
                if (node.getY() > fVisibleAreaY + fVisibleAreaHeight) {
                    break;
                }
            }
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Draw the Frame rectangle
     *
     * @param context the context to draw to
     */
    protected void drawFrame(IGC context) {

        ISDPreferences pref = SDViewPref.getInstance();

        context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_FRAME));

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Draw the frame main rectangle
        context.fillRectangle(x, y, w, h);
        context.drawRectangle(x, y, w, h);

        context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME_NAME));
        context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_FRAME_NAME));
        context.setFont(pref.getFont(ISDPreferences.PREF_FRAME_NAME));

        int nameWidth = context.textExtent(getName()) + 2 * Metrics.FRAME_NAME_V_MARGIN;
        int nameHeight = Metrics.getFrameFontHeigth() + +Metrics.FRAME_NAME_H_MARGIN * 2;

        // Draw the frame name area
        if (nameWidth > w) {
            nameWidth = w;
        }

        int[] points = { x, y, x + nameWidth, y, x + nameWidth, y - 11 + nameHeight, x - 11 + nameWidth, y + nameHeight, x, y + nameHeight, x, y + nameHeight };
        context.fillPolygon(points);
        context.drawPolygon(points);
        context.drawLine(x, y, x, y + nameHeight);

        context.setForeground(pref.getFontColor(ISDPreferences.PREF_FRAME_NAME));
        context.drawTextTruncatedCentred(getName(), x, y, nameWidth - 11, nameHeight, false);

        context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_FRAME));
    }

    @Override
    public void draw(IGC context) {
        draw(context, true);
    }

    /**
     * Draws the Frame on the given context.<br>
     * This method start width GraphNodes ordering if needed.<br>
     * After, depending on the visible area, only visible GraphNodes are drawn.<br>
     *
     * @param context the context to draw to
     * @param drawFrame indicate if the frame rectangle need to be redrawn
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(IGC)
     */
    protected void draw(IGC context, boolean drawFrame) {
        fVisibleAreaHeight = context.getVisibleHeight();
        fVisibleAreaWidth = context.getVisibleWidth();
        fVisibleAreaX = context.getContentsX();
        fVisibleAreaY = context.getContentsY();

        if (fForceEventOccurrenceSpacing >= 0) {
            Metrics.setForcedEventSpacing(fForceEventOccurrenceSpacing);
        } else {
            Metrics.setForcedEventSpacing(-1);
        }

        super.drawChildenNodes(context);
    }

    /**
     * Sets the event occurrence spacing (-1 for none)
     *
     * @param space A spacing to set.
     */
    public void forceEventOccurrenceSpacing(int space) {
        fForceEventOccurrenceSpacing = space;
    }

    /**
     * Return the X coordinates of the frame visible area
     *
     * @return the X coordinates of the frame visible area
     */
    public int getVisibleAreaX() {
        return fVisibleAreaX;
    }

    /**
     * Return the frame visible area width
     *
     * @return the frame visible area width
     */
    public int getVisibleAreaWidth() {
        return fVisibleAreaWidth;
    }

    /**
     * Return the frame visible area height
     *
     * @return the frame visible area height
     */
    public int getVisibleAreaHeight() {
        return fVisibleAreaHeight;
    }

    /**
     * Return the X coordinates of the frame visible area
     *
     * @return the X coordinates of the frame visible area
     */
    public int getVisibleAreaY() {
        return fVisibleAreaY;
    }

    /**
     * Return the minimum time stored in the frame taking all GraphNodes into account
     *
     * @return the minimum GraphNode time
     * @since 2.0
     */
    public ITmfTimestamp getMinTime() {
        if (fLastExternalTimePref != SDViewPref.getInstance().excludeExternalTime()) {
            fLastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();
            setComputeMinMax(true);
        }
        if ((fComputeMinMax) && (!fCustomMinMax)) {
            computeMinMax();
            setComputeMinMax(false);
        }
        return fMinTime;
    }

    /**
     * Set the minimum timestamp of the frame.
     *
     * @param min
     *            The minimum timestamp
     * @since 2.0
     */
    public void setMin(ITmfTimestamp min) {
        fMinTime = min;
        fCustomMinMax = true;
    }

    /**
     * Set the maximum timestamp of the frame.
     *
     * @param max
     *            The maximum timestamp
     * @since 2.0
     */
    public void setMax(ITmfTimestamp max) {
        fMaxTime = max;
        fCustomMinMax = true;
    }

    /**
     * Reset min/max timestamp values to the default ones.
     */
    public void resetCustomMinMax() {
        fCustomMinMax = false;
        setComputeMinMax(true);
    }

    /**
     * Return the maximum time stored in the frame taking all GraphNodes into account
     *
     * @return the maximum GraphNode time
     * @since 2.0
     */
    public ITmfTimestamp getMaxTime() {
        if (fLastExternalTimePref != SDViewPref.getInstance().excludeExternalTime()) {
            fLastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();
            setComputeMinMax(true);
        }
        if (fComputeMinMax) {
            computeMinMax();
            setComputeMinMax(false);
        }
        return fMaxTime;
    }

    /**
     * Computes the minimum and maximum time between consecutive messages within the frame.
     */
    protected void computeMaxMinTime() {
        if (!fInitSDMin) {
            return;
        }

        List<SDTimeEvent> timeArray = buildTimeArray();

        if ((timeArray == null) || timeArray.isEmpty()) {
            return;
        }
        for (int i = 0; i < timeArray.size(); i++) {
            SDTimeEvent m = timeArray.get(i);

            if (m.getTime().compareTo(fMaxSDTime, true) > 0) {
                fMaxSDTime = m.getTime();
            }

            if ((m.getTime().compareTo(fMinSDTime, true) < 0) || fInitSDMin) {
                fMinSDTime = m.getTime();
                fInitSDMin = false;
            }
        }
    }

    /**
     * Returns the minimum time between consecutive messages.
     *
     * @return the minimum time between consecutive messages
     * @since 2.0
     */
    public ITmfTimestamp getSDMinTime() {
        computeMaxMinTime();
        return fMinSDTime;
    }

    /**
     * Returns the maximum time between consecutive messages.
     *
     * @return the maximum time between consecutive messages
     * @since 2.0
     */
    public ITmfTimestamp getSDMaxTime() {
        computeMaxMinTime();
        return fMaxSDTime;
    }

    /**
     * Browse all the GraphNode to compute the min and max times store in the Frame
     */
    protected void computeMinMax() {
        List<SDTimeEvent> timeArray = buildTimeArray();

        if ((timeArray == null) || timeArray.isEmpty()) {
            return;
        }
        for (int i = 0; i < timeArray.size() - 1; i++) {
            SDTimeEvent m1 = timeArray.get(i);
            SDTimeEvent m2 = timeArray.get(i + 1);

            updateMinMax(m1, m2);
        }
    }

    /**
     * Updates the minimum and maximum time between consecutive message within the frame based on the given values.
     *
     * @param m1 A first SD time event.
     * @param m2 A second SD time event.
     */
    protected void updateMinMax(SDTimeEvent m1, SDTimeEvent m2) {
        ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
        if (fComputeMinMax) {
            fMinTime = delta;
            if (fMinTime.compareTo(TmfTimestamp.ZERO, false) < 0) {
                fMinTime = new TmfTimestamp(0, m1.getTime().getScale(), m1.getTime().getPrecision());
            }
            fMaxTime = fMinTime;
            setComputeMinMax(false);
        }

        if ((delta.compareTo(fMinTime, true) < 0) && (delta.compareTo(TmfTimestamp.ZERO, false) > 0)) {
            fMinTime = delta;
        }

        if ((delta.compareTo(fMaxTime, true) > 0) && (delta.compareTo(TmfTimestamp.ZERO, false) > 0)) {
            fMaxTime = delta;
        }
    }

    /**
     * Builds the time array based on the list of graph nodes.
     *
     * @return the time array else empty list.
     */
    protected List<SDTimeEvent> buildTimeArray() {
        if (!hasChildren()) {
            return new ArrayList<>();
        }

        Iterator<String> it = getForwardSortMap().keySet().iterator();
        List<SDTimeEvent> timeArray = new ArrayList<>();
        while (it.hasNext()) {
            String nodeType = it.next();
            List<GraphNode> list = getNodeMap().get(nodeType);
            for (int i = 0; i < list.size(); i++) {
                Object timedNode = list.get(i);
                if ((timedNode instanceof ITimeRange) && ((ITimeRange) timedNode).hasTimeInfo()) {
                    int event = list.get(i).getStartOccurrence();
                    ITmfTimestamp time = ((ITimeRange) list.get(i)).getStartTime();
                    SDTimeEvent f = new SDTimeEvent(time, event, (ITimeRange) list.get(i));
                    timeArray.add(f);
                    if (event != list.get(i).getEndOccurrence()) {
                        event = (list.get(i)).getEndOccurrence();
                        time = ((ITimeRange) list.get(i)).getEndTime();
                        f = new SDTimeEvent(time, event, (ITimeRange) list.get(i));
                        timeArray.add(f);
                    }
                }
            }
        }
        return timeArray;
    }

    @Override
    public String getArrayId() {
        return null;
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    /**
     * @return true if frame has time info else false
     * @since 2.0
     */
    public boolean hasTimeInfo() {
        return fHasTimeInfo;
    }

    /**
     * Sets the flag whether the frame has time info or not
     * @since 2.0
     * @param hasTimeInfo
     *          true if frame has time info else false
     */
    public void setHasTimeInfo(boolean hasTimeInfo) {
        fHasTimeInfo = hasTimeInfo;
    }

    /**
     * Sets the flag for minimum and maximum computation.
     * @param computeMinMax
     *          true if mininum and maximum time needs to be calculated else false
     * @since 2.0
     */
    public void setComputeMinMax(boolean computeMinMax) {
        fComputeMinMax = computeMinMax;
    }

    /**
     * Sets the initialization flag for SD minimum.
     *
     * @param initSDMin
     *          the flag to set
     * @since 2.0
     */
    public void setInitSDMin(boolean initSDMin) {
        fInitSDMin = initSDMin;
    }
}
