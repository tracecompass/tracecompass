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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.TimeEventComparator;

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
public class Frame extends BasicFrame {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The lifeline that is current highlighted.
     */
    private Lifeline fHighlightLifeline = null;
    /**
     * The value of the start event.
     */
    private int fStartEvent = 0;
    /**
     * The number of events in the frame.
     */
    private int fNbEvent = 0;
    /**
     * The color for highlighting.
     */
    private IColor fHighlightColor = null;
    /**
     * The list of time events of the corresponding execution occurrences.
     */
    private List<SDTimeEvent> fExecutionOccurrencesWithTime;
    /**
     * The Array of lifeline categories.
     */
    private LifelineCategories[] fLifelineCategories = null;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Returns a list of all lifelines known by this frame. Known lifelines are the only one which can be displayed on
     * screen.
     *
     * @return the lifelines list
     */
    protected List<GraphNode> getLifelines() {
        if (!hasChildren()) {
            return null;
        }
        return getNodeMap().get(Lifeline.LIFELINE_TAG);
    }

    /**
     * Returns the number of lifelines stored in the frame
     *
     * @return the number of lifelines
     */
    public int lifeLinesCount() {
        List<GraphNode> lifelines = getLifelines();
        if (lifelines != null) {
            return lifelines.size();
        }
        return 0;
    }

    /**
     * Returns the lifeline at the given index in the lifelines array
     *
     * @param index the position in the lifeline array
     * @return the lifeline or <code>null</code>
     */
    public Lifeline getLifeline(int index) {
        if ((getLifelines() != null) && (index >= 0) && (index < lifeLinesCount())) {
            return (Lifeline) getLifelines().get(index);
        }
        return null;
    }

    /**
     * Returns a list of syncMessages known by this frame. Known syncMessages are the only on which can be displayed on
     * screen
     *
     * @return the syncMessages list
     */
    protected List<GraphNode> getSyncMessages() {
        if (!hasChildren()) {
            return null;
        }
        return getNodeMap().get(SyncMessage.SYNC_MESS_TAG);
    }

    /**
     * Returns the number of syncMessages stored in the frame
     *
     * @return the number of syncMessage
     */
    public int syncMessageCount() {
        if (getSyncMessages() != null) {
            return getSyncMessages().size();
        }
        return 0;
    }

    /**
     * Returns the syncMessage at the given index in the syncMessages array
     *
     * @param index the position in the syncMessages array
     * @return the syncMessage or <code>null</code>
     */
    public SyncMessage getSyncMessage(int index) {
        if ((getSyncMessages() != null) && (index >= 0) && (index < getSyncMessages().size())) {
            return (SyncMessage) getSyncMessages().get(index);
        }
        return null;
    }

    /**
     * Returns a list of asyncMessages known by this frame. Known asyncMessages are the only on which can be displayed
     * on screen
     *
     * @return the asyncMessages list or <code>null</code>
     */
    protected List<GraphNode> getAsyncMessages() {
        if (!hasChildren()) {
            return null;
        }
        return getNodeMap().get(AsyncMessage.ASYNC_MESS_TAG);
    }

    /**
     * Returns the number of asyncMessage stored in the frame
     *
     * @return the number of asyncMessage
     */
    public int asyncMessageCount() {
        if (getAsyncMessages() != null) {
            return getAsyncMessages().size();
        }
        return 0;
    }

    /**
     * Returns the asyncMessage at the given index in the asyncMessage array
     *
     * @param index the position in the asyncMessage array
     * @return the asyncMessage or <code>null</code>
     */
    public AsyncMessage getAsyncMessage(int index) {
        if ((getAsyncMessages() != null) && (index >= 0) && (index < getAsyncMessages().size())) {
            return (AsyncMessage) getAsyncMessages().get(index);
        }
        return null;
    }

    /**
     * Returns a list of syncMessages return known by this frame. Known syncMessages return are the only on which can be
     * displayed on screen
     *
     * @return the syncMessages return list or <code>null</code>
     */
    protected List<GraphNode> getSyncMessagesReturn() {
        if (!hasChildren()) {
            return null;
        }
        return getNodeMap().get(SyncMessageReturn.SYNC_MESS_RET_TAG);
    }

    /**
     * Returns the number of syncMessageReturn stored in the frame
     *
     * @return the number of syncMessageReturn
     */
    public int syncMessageReturnCount() {
        if (getSyncMessagesReturn() != null) {
            return getSyncMessagesReturn().size();
        }
        return 0;
    }

    /**
     * Returns the syncMessageReturn at the given index in the syncMessageReturn array
     *
     * @param index the position in the syncMessageReturn array
     * @return the syncMessageReturn or <code>null</code>
     */
    public SyncMessageReturn getSyncMessageReturn(int index) {
        if ((getSyncMessagesReturn() != null) && (index >= 0) && (index < getSyncMessagesReturn().size())) {
            return (SyncMessageReturn) getSyncMessagesReturn().get(index);
        }
        return null;
    }

    /**
     * Returns a list of asyncMessageRetun known by this frame. Known asyncMessageRetun are the only on which can be
     * displayed on screen
     *
     * @return the asyncMessageRetun list or <code>null</code>
     */
    protected List<GraphNode> getAsyncMessagesReturn() {
        if (!hasChildren()) {
            return null;
        }
        return getNodeMap().get(AsyncMessageReturn.ASYNC_MESS_RET_TAG);
    }

    /**
     * Returns the number of asyncMessageReturn stored in the frame
     *
     * @return the number of asyncMessageReturn
     */
    public int asyncMessageReturnCount() {
        if (getAsyncMessagesReturn() != null) {
            return getAsyncMessagesReturn().size();
        }
        return 0;
    }

    /**
     * Returns the asyncMessageReturn at the given index in the asyncMessageReturn array
     *
     * @param index the position in the asyncMessageReturn array
     * @return the asyncMessageReturn or <code>null</code>
     */
    public AsyncMessageReturn getAsyncMessageReturn(int index) {
        if ((getAsyncMessagesReturn() != null) && (index >= 0) && (index < getAsyncMessagesReturn().size())) {
            return (AsyncMessageReturn) getAsyncMessagesReturn().get(index);
        }
        return null;
    }

    /**
     * Adds a lifeline to the frame lifelines list. The lifeline X drawing order depends on the lifeline addition order
     * into the frame lifelines list.
     *
     * @param lifeline the lifeline to add
     */
    public void addLifeLine(Lifeline lifeline) {
        setComputeMinMax(true);
        if (lifeline == null) {
            return;
        }
        // set the lifeline parent frame
        lifeline.setFrame(this);
        // Increate the frame lifeline counter
        // and set the lifeline drawing order
        lifeline.setIndex(getNewHorizontalIndex());
        if (lifeline.hasTimeInfo()) {
            setHasTimeInfo(true);
        }
        // add the lifeline to the lifelines list
        addNode(lifeline);
    }

    /**
     * Returns the first visible lifeline drawn in the view
     *
     * @return the first visible lifeline index
     */
    public int getFirstVisibleLifeline() {
        if (!hasChildren()) {
            return 0;
        } else if (getIndexes().get(Lifeline.LIFELINE_TAG) != null) {
            return getIndexes().get(Lifeline.LIFELINE_TAG).intValue();
        }
        return 0;
    }

    /**
     * Returns the first visible synchronous message drawn in the view
     *
     * @return the first visible synchronous message index
     */
    public int getFirstVisibleSyncMessage() {
        if (!hasChildren()) {
            return 0;
        } else if (getIndexes().get(SyncMessage.SYNC_MESS_TAG) != null) {
            return getIndexes().get(SyncMessage.SYNC_MESS_TAG).intValue();
        }
        return 0;
    }

    /**
     * Returns the first visible synchronous message return drawn in the view
     *
     * @return the first visible synchronous message return index
     */
    public int getFirstVisibleSyncMessageReturn() {
        if (!hasChildren()) {
            return 0;
        } else if (getIndexes().get(SyncMessageReturn.SYNC_MESS_RET_TAG) != null) {
            return getIndexes().get(SyncMessageReturn.SYNC_MESS_RET_TAG).intValue();
        }
        return 0;
    }

    /**
     * Returns the first visible synchronous message drawn in the view
     *
     * @return the first visible synchronous message index
     */
    public int getFirstVisibleAsyncMessage() {
        if (!hasChildren()) {
            return 0;
        } else if (getIndexes().get(AsyncMessage.ASYNC_MESS_TAG) != null) {
            return getIndexes().get(AsyncMessage.ASYNC_MESS_TAG).intValue();
        }
        return 0;
    }

    /**
     * Returns the first visible synchronous message return drawn in the view
     *
     * @return the first visible synchronous message return index
     */
    public int getFirstVisibleAsyncMessageReturn() {
        if (!hasChildren()) {
            return 0;
        } else if (getIndexes().get(AsyncMessageReturn.ASYNC_MESS_RET_TAG) != null) {
            return getIndexes().get(AsyncMessageReturn.ASYNC_MESS_RET_TAG).intValue();
        }
        return 0;
    }

    /**
     * Returns the list of execution occurrences.
     *
     * @return the list of execution occurrences
     */
    public List<SDTimeEvent> getExecutionOccurrencesWithTime() {
        return fExecutionOccurrencesWithTime;
    }

    /**
     * Inserts a lifeline after a given lifeline.
     *
     * @param toInsert A lifeline to insert
     * @param after A lifelife the toInsert-lifeline will be inserted after.
     */
    public void insertLifelineAfter(Lifeline toInsert, Lifeline after) {
        if ((toInsert == null)) {
            return;
        }
        if (toInsert == after) {
            return;
        }
        int insertPoint = 0;
        if (after != null) {
            insertPoint = after.getIndex();
        }
        int removePoint = toInsert.getIndex() - 1;
        if (removePoint >= insertPoint) {
            getLifelines().remove(removePoint);
        }
        getLifelines().add(insertPoint, toInsert);
        if (removePoint < insertPoint) {
            getLifelines().remove(removePoint);
        }

        if (removePoint >= insertPoint) {
            toInsert.setIndex(insertPoint + 1);
        } else {
            toInsert.setIndex(insertPoint - 1);
        }

        insertPoint++;
        if (removePoint >= insertPoint) {
            for (int i = insertPoint; i < getLifelines().size(); i++) {
                getLifeline(i).setIndex(i + 1);
            }
        } else {
            for (int i = 0; i < insertPoint && i < getLifelines().size(); i++) {
                getLifeline(i).setIndex(i + 1);
            }
        }
    }

    /**
     * Inserts a lifeline before a given lifeline.
     *
     * @param toInsert
     *            A lifeline to insert
     * @param before
     *            A lifeline the toInsert-lifeline will be inserted before.
     */
    public void insertLifelineBefore(Lifeline toInsert, Lifeline before) {
        if ((toInsert == null)) {
            return;
        }
        if (toInsert == before) {
            return;
        }
        int insertPoint = 0;
        if (before != null) {
            insertPoint = before.getIndex() - 1;
        }
        int removePoint = toInsert.getIndex() - 1;
        if (removePoint >= insertPoint) {
            getLifelines().remove(removePoint);
        }
        getLifelines().add(insertPoint, toInsert);
        if (removePoint < insertPoint) {
            getLifelines().remove(removePoint);
        }

        if (removePoint >= insertPoint) {
            toInsert.setIndex(insertPoint + 1);
        } else {
            toInsert.setIndex(insertPoint - 1);
        }

        insertPoint++;
        if (removePoint >= insertPoint) {
            for (int i = insertPoint; i < getLifelines().size(); i++) {
                getLifeline(i).setIndex(i + 1);
            }
        } else {
            for (int i = 0; i < insertPoint && i < getLifelines().size(); i++) {
                getLifeline(i).setIndex(i + 1);
            }
        }
    }

    /**
     * Gets the closer life line to the given x-coordinate.
     *
     * @param x A x coordinate
     * @return the closer lifeline
     */
    public Lifeline getCloserLifeline(int x) {
        int index = (x - Metrics.FRAME_H_MARGIN + Metrics.LIFELINE_H_MAGIN) / Metrics.swimmingLaneWidth() - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= getLifelines().size()) {
            index = getLifelines().size() - 1;
        }
        Lifeline node1, node2, node3;
        int dist1, dist2, dist3;
        node1 = node2 = node3 = getLifeline(index);
        dist1 = dist2 = dist3 = Math.abs(node1.getX() + node1.getWidth() / 2 - x);
        if (index > 0) {
            node2 = getLifeline(index - 1);
            dist2 = Math.abs(node2.getX() + node2.getWidth() / 2 - x);
        }
        if (index < getLifelines().size() - 1) {
            node3 = getLifeline(index + 1);
            dist3 = Math.abs(node3.getX() + node3.getWidth() / 2 - x);
        }
        if (dist1 <= dist2 && dist1 <= dist3) {
            return node1;
        } else if (dist2 <= dist1 && dist2 <= dist3) {
            return node2;
        }
        return node3;
    }

    /**
     * Re-orders the given list of lifelines.
     *
     * @param list A list of lifelines to reorder.
     */
    public void reorder(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Lifeline[]) {
                Lifeline temp[] = (Lifeline[]) list.get(i);
                if (temp.length == 2) {
                    if (temp[1] == null) {
                        insertLifelineAfter(temp[0], getLifeline(lifeLinesCount() - 1));
                    } else {
                        insertLifelineBefore(temp[0], temp[1]);
                    }
                }
            }
        }
    }

    /**
     * Resets the time compression information.
     */
    public void resetTimeCompression() {
        fHighlightLifeline = null;
        this.fStartEvent = 0;
        this.fNbEvent = 0;
        fHighlightColor = null;
    }

    @Override
    protected void computeMinMax() {
        List<SDTimeEvent> timeArray = buildTimeArray();
        if ((timeArray == null) || timeArray.isEmpty()) {
            return;
        }
        for (int i = 0; i < timeArray.size() - 1; i++) {
            SDTimeEvent m1 = timeArray.get(i);
            SDTimeEvent m2 = timeArray.get(i + 1);
            if (SDViewPref.getInstance().excludeExternalTime() && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                    continue;
                }
            }

            updateMinMax(m1, m2);
        }
    }

    /**
     * Find the two graph nodes that are closest to this date, one just earlier, second just later. If date is before
     * any graph node, bounds[0] is null and bounds[1] is the earliest. If date is after any graph node, bounds[1] is
     * null and bounds[0] is the latest.
     *
     * @param dateToFind date to be found
     * @param bounds a two items array that will receive bounds if found
     * @return true if both bounds not null
     * @since 2.0
     */
    public boolean findDateBounds(ITmfTimestamp dateToFind, ITimeRange bounds[]) {
        if (hasTimeInfo()) {
            List<SDTimeEvent> timeArray = buildTimeArray();

            if ((timeArray == null) || timeArray.isEmpty()) {
                return false;
            }

            bounds[0] = null;
            bounds[1] = null;
            for (int i = 0; i < timeArray.size(); i++) {
                SDTimeEvent m = timeArray.get(i);
                if (m.getTime().compareTo(dateToFind, true) > 0) {
                    bounds[1] = m.getGraphNode();
                    if (i > 0) {
                        bounds[0] = timeArray.get(i - 1).getGraphNode();
                        return true;
                    }
                    return false;
                }
            }
            bounds[0] = timeArray.get(timeArray.size() - 1).getGraphNode();
        }
        return false;
    }

    /**
     * Highlights the time compression.
     *
     * @param lifeline A lifeline to highlight
     * @param startEvent A start event number
     * @param nbEvent A number of events
     * @param color A color for highlighting
     */
    public void highlightTimeCompression(Lifeline lifeline, int startEvent, int nbEvent, IColor color) {
        fHighlightLifeline = lifeline;
        this.fStartEvent = startEvent;
        this.fNbEvent = nbEvent;
        fHighlightColor = color;
    }

    /**
     * Set the lifeline categories which will be use during the lifelines creation
     *
     * @see Lifeline#setCategory(int)
     * @param categories the lifeline categories array
     */
    public void setLifelineCategories(LifelineCategories[] categories) {
        fLifelineCategories = Arrays.copyOf(categories, categories.length);
    }

    /**
     * Returns the lifeline categories array set for the this frame
     *
     * @return the lifeline categories array or null if not set
     */
    public LifelineCategories[] getLifelineCategories() {
        return  Arrays.copyOf(fLifelineCategories, fLifelineCategories.length);
    }

    /**
     * Adds a message to the Frame message list. Four kinds of syncMessages can be added:<br>
     * - synchronous syncMessages<br>
     * - synchronous syncMessages return<br>
     * - asynchronous syncMessages<br>
     * - asynchronous syncMessages return<br>
     * For drawing performance reason, it is recommended to add synchronous syncMessages in the same order they should
     * appear along the Y axis in the Frame.
     *
     * @param message the message to add
     */
    public void addMessage(BaseMessage message) {
        addNode(message);
    }

    @Override
    public void draw(IGC context) {
        drawFrame(context);
        if (!hasChildren()) {
            return;
        }

        if (fHighlightLifeline != null) {
            IColor backupColor = context.getBackground();
            context.setBackground(SDViewPref.getInstance().getTimeCompressionSelectionColor());
            int gy = fHighlightLifeline.getY() + fHighlightLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fStartEvent;
            context.fillRectangle(Metrics.FRAME_H_MARGIN + 1, gy, fHighlightLifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.FRAME_H_MARGIN, (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fNbEvent);
            context.setBackground(backupColor);
        }
        super.draw(context, false);
        int lifelineArryStep = 1;
        if (Metrics.swimmingLaneWidth() * context.getZoom() < Metrics.LIFELINE_SIGNIFICANT_HSPACING) {
            lifelineArryStep = Math.round(Metrics.LIFELINE_SIGNIFICANT_HSPACING / (Metrics.swimmingLaneWidth() * context.getZoom()));
        }
        if (getIndexes().size() == 0) {
            return;
        }
        int lifeLineDrawIndex = getIndexes().get(Lifeline.LIFELINE_TAG).intValue();
        for (int i = lifeLineDrawIndex; i < getNodeMap().get(Lifeline.LIFELINE_TAG).size(); i = i + lifelineArryStep) {
            Lifeline toDraw = (Lifeline) getNodeMap().get(Lifeline.LIFELINE_TAG).get(i);
            if (toDraw.getX() - Metrics.LIFELINE_SPACING / 2 > context.getContentsX() + context.getVisibleWidth()) {
                break;
            }
            toDraw.drawName(context);

            if (fHighlightLifeline != null) {
                if (toDraw == fHighlightLifeline) {
                    toDraw.highlightExecOccurrenceRegion(context, fStartEvent, fNbEvent, fHighlightColor);
                } else if ((toDraw.getIndex() < fHighlightLifeline.getIndex()) || ((toDraw.getIndex() < fHighlightLifeline.getIndex()))) {

                    int acIndex = toDraw.getExecOccurrenceDrawIndex();
                    // acIndex = first visible execution occurrence
                    // for drawing speed reason with only search on the visible subset
                    if (toDraw.getExecutions() != null) {
                        for (int index = acIndex; index < toDraw.getExecutions().size(); index++) {
                            BasicExecutionOccurrence exec = (BasicExecutionOccurrence) toDraw.getExecutions().get(index);
                            int tempEvent = fStartEvent;
                            for (int j = 0; j < fNbEvent; j++) {
                                if (((tempEvent >= exec.getStartOccurrence()) && (tempEvent <= exec.getEndOccurrence()) && (tempEvent + 1 >= exec.getStartOccurrence()) && (tempEvent + 1 <= exec.getEndOccurrence()))) {
                                    toDraw.highlightExecOccurrenceRegion(context, tempEvent, 1, SDViewPref.getInstance().getTimeCompressionSelectionColor());
                                }
                                tempEvent = tempEvent + 1;
                            }
                            // if we are outside the visible area we stop right now
                            // This works because execution occurrences are ordered along the Y axis
                            if (exec.getY() > getY()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected List<SDTimeEvent> buildTimeArray() {

        if (!hasChildren()) {
            return new ArrayList<SDTimeEvent>();
        }

        List<SDTimeEvent> timeArray = super.buildTimeArray();
        fExecutionOccurrencesWithTime = null;
        if (getLifelines() != null) {
            for (int i = 0; i < getNodeMap().get(Lifeline.LIFELINE_TAG).size(); i++) {
                Lifeline lifeline = (Lifeline) getNodeMap().get(Lifeline.LIFELINE_TAG).get(i);
                if (lifeline.hasTimeInfo() && lifeline.getExecutions() != null) {
                    for (Iterator<GraphNode> j = lifeline.getExecutions().iterator(); j.hasNext();) {
                        GraphNode o = j.next();
                        if (o instanceof ExecutionOccurrence) {
                            ExecutionOccurrence eo = (ExecutionOccurrence) o;
                            if (eo.hasTimeInfo()) {
                                int event = eo.getStartOccurrence();
                                ITmfTimestamp time = eo.getStartTime();
                                SDTimeEvent f = new SDTimeEvent(time, event, eo);
                                timeArray.add(f);
                                if (fExecutionOccurrencesWithTime == null) {
                                    fExecutionOccurrencesWithTime = new ArrayList<SDTimeEvent>();
                                }
                                fExecutionOccurrencesWithTime.add(f);
                                event = eo.getEndOccurrence();
                                time = eo.getEndTime();
                                f = new SDTimeEvent(time, event, eo);
                                timeArray.add(f);
                                fExecutionOccurrencesWithTime.add(f);
                            }
                        }
                    }
                }
            }
        }

        if (fExecutionOccurrencesWithTime != null) {
            SDTimeEvent[] temp = fExecutionOccurrencesWithTime.toArray(new SDTimeEvent[fExecutionOccurrencesWithTime.size()]);
            Arrays.sort(temp, new TimeEventComparator());
            fExecutionOccurrencesWithTime = Arrays.asList(temp);
        }
        SDTimeEvent[] temp = timeArray.toArray(new SDTimeEvent[timeArray.size()]);
        Arrays.sort(temp, new TimeEventComparator());
        timeArray = Arrays.asList(temp);
        return timeArray;
    }

    /**
     * Get the closer leaving message.
     *
     * @param lifeline A lifeline reference
     * @param message A message reference
     * @param list A list of graph nodes
     * @param smallerEvent A smaller event flag
     * @return the closer leaving message.
     */
    protected GraphNode getCloserLeavingMessage(Lifeline lifeline, BaseMessage message, List<GraphNode> list, boolean smallerEvent) {
        if (list == null) {
            return null;
        }

        if (!smallerEvent) {
            int event = 0;
            if (message != null) {
                event = message.getEventOccurrence();
            }
            for (int i = 0; i < list.size(); i++) {
                GraphNode node = list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() > event) && (syncNode.getStartLifeline() == lifeline) && !syncNode.isSameAs(message)) {
                        return node;
                    }
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() > event) && (asyncNode.getStartLifeline() == lifeline) && !asyncNode.isSameAs(message)) {
                        return node;
                    }
                }
            }
        } else {
            int event = getMaxEventOccurrence();
            if (message != null) {
                if (message instanceof AsyncMessage) {
                    event = ((AsyncMessage) message).getStartOccurrence();
                } else {
                    event = message.getEventOccurrence();
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                GraphNode node = list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() < event) && (syncNode.getStartLifeline() == lifeline) && !syncNode.isSameAs(message)) {
                        return node;
                    }
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() < event) && (asyncNode.getStartLifeline() == lifeline) && !asyncNode.isSameAs(message)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Get the closer entering message.
     *
     * @param lifeline A lifeline reference
     * @param message A message reference
     * @param list A list of graph nodes
     * @param smallerEvent A smaller event flag
     * @return the closer entering message.
     */
    protected GraphNode getCloserEnteringMessage(Lifeline lifeline, BaseMessage message, List<GraphNode> list, boolean smallerEvent) {
        if (list == null) {
            return null;
        }
        if (!smallerEvent) {
            int event = 0;
            if (message != null) {
                event = message.getEventOccurrence();
            }
            for (int i = 0; i < list.size(); i++) {
                GraphNode node = list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() > event) && (syncNode.getEndLifeline() == lifeline) && !syncNode.isSameAs(message)) {
                        return node;
                    }
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() > event) && (asyncNode.getEndLifeline() == lifeline) && !asyncNode.isSameAs(message)) {
                        return node;
                    }
                }
            }
        } else {
            int event = getMaxEventOccurrence();
            if (message != null) {
                if (message instanceof AsyncMessage) {
                    event = ((AsyncMessage) message).getStartOccurrence();
                } else {
                    event = message.getEventOccurrence();
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                GraphNode node = list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() < event) && (syncNode.getEndLifeline() == lifeline) && !syncNode.isSameAs(message)) {
                        return node;
                    }
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() < event) && (asyncNode.getEndLifeline() == lifeline) && !asyncNode.isSameAs(message)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get distance of given event from given graph node.
     *
     * @param node A graph node reference.
     * @param event A event number to check.
     * @return distance of event from graph node.
     */
    protected int distanceFromEvent(GraphNode node, int event) {
        int distance = 0;
        if (node instanceof SyncMessage) {
            distance = ((SyncMessage) node).getEventOccurrence() - event;
        } else if (node instanceof AsyncMessage) {
            int start = ((AsyncMessage) node).getStartOccurrence();
            int end = ((AsyncMessage) node).getEndOccurrence();
            if ((start - event) < (end - event)) {
                distance = start - event;
            } else {
                distance = end - event;
            }
        }
        return Math.abs(distance);
    }

    /**
     * Get node from 2 given nodes that is close to event.
     *
     * @param node1 A first graph node
     * @param node2 A second graph node
     * @param event A event to check.
     * @return graph node that is closer or <code>null</code>
     */
    protected GraphNode getCloserToEvent(GraphNode node1, GraphNode node2, int event) {
        if ((node1 != null) && (node2 != null)) {
            if (distanceFromEvent(node1, event) < distanceFromEvent(node2, event)) {
                return node1;
            }
            return node2;
        } else if (node1 != null) {
            return node1;
        } else if (node2 != null) {
            return node2;
        }
        return null;
    }

    /**
     * Get called message based on given start message.
     *
     * @param startMessage A start message to check.
     * @return called message (graph node) or <code>null</code>
     */
    public GraphNode getCalledMessage(BaseMessage startMessage) {
        int event = 0;
        GraphNode result = null;
        Lifeline lifeline = null;
        if (startMessage != null) {
            event = startMessage.getEventOccurrence();
            lifeline = startMessage.getEndLifeline();
            if (lifeline == null) {
                lifeline = startMessage.getStartLifeline();
            }
        }
        if (lifeline == null) {
            return null;
        }
        GraphNode message = getCloserLeavingMessage(lifeline, startMessage, getSyncMessages(), false);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, startMessage, getSyncMessagesReturn(), false);
        result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessages(), false);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessagesReturn(), false);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    /**
     * Get caller message based on given start message.
     *
     * @param startMessage A start message to check.
     * @return called message (graph node) or <code>null</code>
     */
    public GraphNode getCallerMessage(BaseMessage startMessage) {
        int event = getMaxEventOccurrence();
        GraphNode result = null;
        Lifeline lifeline = null;
        if (startMessage != null) {
            event = startMessage.getEventOccurrence();
            lifeline = startMessage.getStartLifeline();
            if (lifeline == null) {
                lifeline = startMessage.getEndLifeline();
            }
        }
        if (lifeline == null) {
            return null;
        }
        GraphNode message = getCloserEnteringMessage(lifeline, startMessage, getSyncMessages(), true);
        GraphNode messageReturn = getCloserEnteringMessage(lifeline, startMessage, getSyncMessagesReturn(), true);
        result = getCloserToEvent(message, messageReturn, event);
        message = getCloserEnteringMessage(lifeline, startMessage, getAsyncMessages(), true);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserEnteringMessage(lifeline, startMessage, getAsyncMessagesReturn(), true);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    /**
     * Get next lifeline based on given message.
     *
     * @param lifeline A lifeline reference
     * @param startMessage A start message to check
     * @return next lifeline or <code>null</code>
     */
    public GraphNode getNextLifelineMessage(Lifeline lifeline, BaseMessage startMessage) {
        int event = 0;
        if (startMessage != null) {
            event = startMessage.getEventOccurrence();
        }
        if (lifeline == null) {
            return null;
        }
        GraphNode message = getCloserLeavingMessage(lifeline, startMessage, getSyncMessages(), false);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, startMessage, getSyncMessagesReturn(), false);
        GraphNode result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessages(), false);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessagesReturn(), false);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    /**
     * Get previous lifeline based on given message.
     *
     * @param lifeline A lifeline reference
     * @param startMessage A start message to check.
     * @return previous lifeline or <code>null</code>
     */
    public GraphNode getPrevLifelineMessage(Lifeline lifeline, BaseMessage startMessage) {
        int event = getMaxEventOccurrence();
        if (startMessage != null) {
            if (startMessage instanceof AsyncMessage) {
                event = ((AsyncMessage) startMessage).getStartOccurrence();
            } else {
                event = startMessage.getEventOccurrence();
            }
        }
        if (lifeline == null) {
            return null;
        }
        GraphNode message = getCloserLeavingMessage(lifeline, startMessage, getSyncMessages(), true);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, startMessage, getSyncMessagesReturn(), true);
        GraphNode result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessages(), true);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, startMessage, getAsyncMessagesReturn(), true);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    /**
     * Get the first execution occurrence.
     *
     * @param lifeline A lifeline reference
     * @return the first execution occurrence of lifeline or <code>null</code>.
     */
    public BasicExecutionOccurrence getFirstExecution(Lifeline lifeline) {
        if (lifeline == null) {
            return null;
        }
        List<GraphNode> list = lifeline.getExecutions();

        if ((list == null) || (list.isEmpty())) {
            return null;
        }

        BasicExecutionOccurrence result = (BasicExecutionOccurrence) list.get(0);
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() < result.getEndOccurrence())) {
                result = e;
            }
        }
        return result;
    }

    /**
     * Get the previous execution occurrence relative to a given execution occurrence.
     *
     * @param exec A execution occurrence reference.
     * @return the previous execution occurrence of lifeline or <code>null</code>.
     */
    public BasicExecutionOccurrence getPrevExecOccurrence(BasicExecutionOccurrence exec) {
        if (exec == null) {
            return null;
        }
        Lifeline lifeline = exec.getLifeline();
        if (lifeline == null) {
            return null;
        }
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null) {
            return null;
        }
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() < exec.getStartOccurrence()) && (result == null)) {
                result = e;
            }
            if ((e.getStartOccurrence() < exec.getStartOccurrence()) && (result != null) && (e.getStartOccurrence() >= result.getEndOccurrence())) {
                result = e;
            }
        }
        return result;
    }

    /**
     * Get the next execution occurrence relative to a given execution occurrence.
     *
     * @param exec A execution occurrence reference.
     * @return the next execution occurrence of lifeline or <code>null</code>.
     */
    public BasicExecutionOccurrence getNextExecOccurrence(BasicExecutionOccurrence exec) {
        if (exec == null) {
            return null;
        }
        Lifeline lifeline = exec.getLifeline();
        if (lifeline == null) {
            return null;
        }
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null) {
            return null;
        }
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() > exec.getStartOccurrence()) && (result == null)) {
                result = e;
            }
            if ((e.getStartOccurrence() > exec.getStartOccurrence()) && (result != null) && (e.getStartOccurrence() <= result.getEndOccurrence())) {
                result = e;
            }
        }
        return result;
    }

    /**
     * Get the last execution occurrence.
     *
     * @param lifeline A lifeline reference.
     * @return the last execution occurrence of lifeline or <code>null</code>.
     */
    public BasicExecutionOccurrence getLastExecOccurrence(Lifeline lifeline) {
        if (lifeline == null) {
            return null;
        }
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null) {
            return null;
        }
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if (result == null) {
                result = e;
            }
            if (e.getStartOccurrence() > result.getEndOccurrence()) {
                result = e;
            }
        }
        return result;
    }

    /**
     * @return highlighted life line if set else null.
     * @since 2.0
     */
    protected Lifeline getHighlightLifeline() {
        return fHighlightLifeline;
    }

    /**
     * @return the start event value.
     * @since 2.0
     */
    protected int getStartEvent() {
        return fStartEvent;
    }

    /**
     * Returns the number of events
     *
     * @return the number of events
     * @since 2.0
     */
    protected int getNumberOfEvents() {
        return fNbEvent;
    }

    /**
     * Returns the highlight color.
     * @return the highlight color
     * @since 2.0
     */
    protected IColor getHighlightColor() {
        return fHighlightColor;
    }

    /**
     * Set the highlighted life line.
     * @param lifeline
     *          The highlighted life line if set else null
     * @since 2.0
     */
    protected void setHighlightLifeline(Lifeline lifeline) {
        fHighlightLifeline = lifeline;
    }

    /**
     * Sets the start event value
     * @param startEvent
     *           the start event value.
     * @since 2.0
     */
    protected void setStartEvent(int startEvent) {
        fStartEvent = startEvent;
    }

    /**
     * Sets the number of events
     *
     * @param nbEvents
     *          The number of events
     * @since 2.0
     */
    protected void setNumberOfEvents(int nbEvents) {
        fNbEvent = nbEvents;
    }

    /**
     * Sets the highlight color.
     * @param color
     *          the highlight color
     * @since 2.0
     */
    protected void setHighlightColor(IColor color) {
        fHighlightColor = color;
    }

    /**
     * sets the list of execution occurrences.
     *
     * @param occurences
     *          the list of execution occurrences
     * @since 2.0
     */
    protected void setExecutionOccurrencesWithTime(List<SDTimeEvent> occurences) {
        fExecutionOccurrencesWithTime = occurences;
    }
}