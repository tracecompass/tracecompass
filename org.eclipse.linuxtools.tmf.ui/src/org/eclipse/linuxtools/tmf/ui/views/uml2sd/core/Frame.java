/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Frame.java,v 1.3 2008/01/24 02:28:49 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
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

    protected Lifeline highlightLifeline = null;
    protected int startEvent = 0;
    protected int nbEvent = 0;
    protected IColor highlightColor = null;

    protected List<SDTimeEvent> executionOccurrencesWithTime;

    protected LifelineCategories[] lifelineCategories = null;

    /**
     * Returns a list of all lifelines known by this frame. Known lifelines are the only one which can be displayed on
     * screen.
     * 
     * @return the lifelines list
     */
    protected List<GraphNode> getLifelines() {
        if (!hasChilden)
            return null;
        else
            return (List<GraphNode>) nodes.get(Lifeline.LIFELINE_TAG);
    }

    /**
     * Returns the number of lifelines stored in the frame
     * 
     * @return the number of lifelines
     */
    public int lifeLinesCount() {
        List<GraphNode> lifelines = getLifelines();
        if (lifelines != null)
            return lifelines.size();
        else
            return 0;
    }

    /**
     * Returns the lifeline at the given index in the lifelines array
     * 
     * @param index the position in the lifeline array
     * @return the lifeline
     */
    public Lifeline getLifeline(int index) {
        if ((getLifelines() != null) && (index >= 0) && (index < lifeLinesCount()))
            return (Lifeline) getLifelines().get(index);
        return null;
    }

    /**
     * Returns a list of syncMessages known by this frame. Known syncMessages are the only on which can be displayed on
     * screen
     * 
     * @return the syncMessages list
     */
    protected List<GraphNode> getSyncMessages() {
        if (!hasChilden)
            return null;
        else
            return (List<GraphNode>) nodes.get(SyncMessage.SYNC_MESS_TAG);
    }

    /**
     * Returns the number of syncMessages stored in the frame
     * 
     * @return the number of syncMessage
     */
    public int syncMessageCount() {
        if (getSyncMessages() != null)
            return getSyncMessages().size();
        else
            return 0;
    }

    /**
     * Returns the syncMessage at the given index in the syncMessages array
     * 
     * @param index the position in the syncMessages array
     * @return the syncMessage
     */
    public SyncMessage getSyncMessage(int index) {
        if ((getSyncMessages() != null) && (index >= 0) && (index < getSyncMessages().size()))
            return (SyncMessage) getSyncMessages().get(index);
        return null;
    }

    /**
     * Returns a list of asyncMessages known by this frame. Known asyncMessages are the only on which can be displayed
     * on screen
     * 
     * @return the asyncMessages list
     */
    protected List<GraphNode> getAsyncMessages() {
        if (!hasChilden)
            return null;
        else
            return (List<GraphNode>) nodes.get(AsyncMessage.ASYNC_MESS_TAG);
    }

    /**
     * Returns the number of asyncMessage stored in the frame
     * 
     * @return the number of asyncMessage
     */
    public int asyncMessageCount() {
        if (getAsyncMessages() != null)
            return getAsyncMessages().size();
        else
            return 0;
    }

    /**
     * Returns the asyncMessage at the given index in the asyncMessage array
     * 
     * @param index the position in the asyncMessage array
     * @return the asyncMessage
     */
    public AsyncMessage getAsyncMessage(int index) {
        if ((getAsyncMessages() != null) && (index >= 0) && (index < getAsyncMessages().size()))
            return (AsyncMessage) getAsyncMessages().get(index);
        return null;
    }

    /**
     * Returns a list of syncMessages return known by this frame. Known syncMessages return are the only on which can be
     * displayed on screen
     * 
     * @return the syncMessages return list
     */
    protected List<GraphNode> getSyncMessagesReturn() {
        if (!hasChilden)
            return null;
        else
            return (List<GraphNode>) nodes.get(SyncMessageReturn.SYNC_MESS_RET_TAG);
    }

    /**
     * Returns the number of syncMessageReturn stored in the frame
     * 
     * @return the number of syncMessageReturn
     */
    public int syncMessageReturnCount() {
        if (getSyncMessagesReturn() != null)
            return getSyncMessagesReturn().size();
        else
            return 0;
    }

    /**
     * Returns the syncMessageReturn at the given index in the syncMessageReturn array
     * 
     * @param index the position in the syncMessageReturn array
     * @return the syncMessageReturn
     */
    public SyncMessageReturn getSyncMessageReturn(int index) {
        if ((getSyncMessagesReturn() != null) && (index >= 0) && (index < getSyncMessagesReturn().size()))
            return (SyncMessageReturn) getSyncMessagesReturn().get(index);
        return null;
    }

    /**
     * Returns a list of asyncMessageRetun known by this frame. Known asyncMessageRetun are the only on which can be
     * displayed on screen
     * 
     * @return the asyncMessageRetun list
     */
    protected List<GraphNode> getAsyncMessagesReturn() {
        if (!hasChilden)
            return null;
        else
            return (List<GraphNode>) nodes.get(AsyncMessageReturn.ASYNC_MESS_RET_TAG);
    }

    /**
     * Returns the number of asyncMessageReturn stored in the frame
     * 
     * @return the number of asyncMessageReturn
     */
    public int asyncMessageReturnCount() {
        if (getAsyncMessagesReturn() != null)
            return getAsyncMessagesReturn().size();
        else
            return 0;
    }

    /**
     * Returns the asyncMessageReturn at the given index in the asyncMessageReturn array
     * 
     * @param index the position in the asyncMessageReturn array
     * @return the asyncMessageReturn
     */
    public AsyncMessageReturn getAsyncMessageReturn(int index) {
        if ((getAsyncMessagesReturn() != null) && (index >= 0) && (index < getAsyncMessagesReturn().size()))
            return (AsyncMessageReturn) getAsyncMessagesReturn().get(index);
        return null;
    }

    /**
     * Adds a lifeline to the frame lifelines list. The lifeline X drawing order depends on the lifeline addition order
     * into the frame lifelines list.
     * 
     * @param the lifeline to add
     */
    public void addLifeLine(Lifeline lifeLine) {
        computeMinMax = true;
        if (lifeLine == null)
            return;
        // set the lifeline parent frame
        lifeLine.setFrame(this);
        // Increate the frame lifeline counter
        // and set the lifeline drawing order
        lifeLine.setIndex(getNewHorizontalIndex());
        if (lifeLine.hasTimeInfo()) {
            timeInfo = true;
        }
        // add the lifeline to the lifelines list
        addNode(lifeLine);
    }

    /**
     * Returns the first visible lifeline drawn in the view
     * 
     * @return the first visible lifeline index
     */
    public int getFirstVisibleLifeline() {
        if (!hasChilden)
            return 0;
        else if (indexes.get(Lifeline.LIFELINE_TAG) != null)
            return ((Integer) indexes.get(Lifeline.LIFELINE_TAG)).intValue();
        else
            return 0;
    }

    /**
     * Returns the first visible synchronous message drawn in the view
     * 
     * @return the first visible synchronous message index
     */
    public int getFirstVisibleSyncMessage() {
        if (!hasChilden)
            return 0;
        else if (indexes.get(SyncMessage.SYNC_MESS_TAG) != null)
            return ((Integer) indexes.get(SyncMessage.SYNC_MESS_TAG)).intValue();
        else
            return 0;
    }

    /**
     * Returns the first visible synchronous message return drawn in the view
     * 
     * @return the first visible synchronous message return index
     */
    public int getFirstVisibleSyncMessageReturn() {
        if (!hasChilden)
            return 0;
        else if (indexes.get(SyncMessageReturn.SYNC_MESS_RET_TAG) != null)
            return ((Integer) indexes.get(SyncMessageReturn.SYNC_MESS_RET_TAG)).intValue();
        else
            return 0;
    }

    /**
     * Returns the first visible synchronous message drawn in the view
     * 
     * @return the first visible synchronous message index
     */
    public int getFirstVisibleAsyncMessage() {
        if (!hasChilden)
            return 0;
        else if (indexes.get(AsyncMessage.ASYNC_MESS_TAG) != null)
            return ((Integer) indexes.get(AsyncMessage.ASYNC_MESS_TAG)).intValue();
        else
            return 0;
    }

    /**
     * Returns the first visible synchronous message return drawn in the view
     * 
     * @return the first visible synchronous message return index
     */
    public int getFirstVisibleAsyncMessageReturn() {
        if (!hasChilden)
            return 0;
        else if (indexes.get(AsyncMessageReturn.ASYNC_MESS_RET_TAG) != null)
            return ((Integer) indexes.get(AsyncMessageReturn.ASYNC_MESS_RET_TAG)).intValue();
        else
            return 0;
    }

    public List<SDTimeEvent> getExecutionOccurrencesWithTime() {
        return executionOccurrencesWithTime;
    }

    public void insertLifelineAfter(Lifeline toInsert, Lifeline after) {
        if ((toInsert == null))
            return;
        if (toInsert == after)
            return;
        int insertPoint = 0;
        if (after != null)
            insertPoint = after.getIndex();
        int removePoint = toInsert.getIndex() - 1;
        if (removePoint >= insertPoint)
            getLifelines().remove(removePoint);
        getLifelines().add(insertPoint, toInsert);
        if (removePoint < insertPoint)
            getLifelines().remove(removePoint);

        if (removePoint >= insertPoint)
            toInsert.setIndex(insertPoint + 1);
        else
            toInsert.setIndex(insertPoint - 1);

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

    public void insertLifelineBefore(Lifeline toInsert, Lifeline before) {
        if ((toInsert == null))
            return;
        if (toInsert == before)
            return;
        int insertPoint = 0;
        if (before != null)
            insertPoint = before.getIndex() - 1;
        int removePoint = toInsert.getIndex() - 1;
        if (removePoint >= insertPoint)
            getLifelines().remove(removePoint);
        getLifelines().add(insertPoint, toInsert);
        if (removePoint < insertPoint)
            getLifelines().remove(removePoint);

        if (removePoint >= insertPoint)
            toInsert.setIndex(insertPoint + 1);
        else
            toInsert.setIndex(insertPoint - 1);

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

    public Lifeline getCloserLifeline(int x) {
        int index = (x - Metrics.FRAME_H_MARGIN + Metrics.LIFELINE_H_MAGIN) / Metrics.swimmingLaneWidth() - 1;
        if (index < 0)
            index = 0;
        if (index >= getLifelines().size())
            index = getLifelines().size() - 1;
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
        if (dist1 <= dist2 && dist1 <= dist3)
            return node1;
        else if (dist2 <= dist1 && dist2 <= dist3)
            return node2;
        else
            return node3;
    }

    public void reorder(ArrayList<?> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Lifeline[]) {
                Lifeline temp[] = (Lifeline[]) list.get(i);
                if (temp.length == 2) {
                    if (temp[1] == null) {
                        insertLifelineAfter(temp[0], getLifeline(lifeLinesCount() - 1));
                    } else
                        insertLifelineBefore(temp[0], temp[1]);
                }
            }
        }
    }

    public void resetTimeCompression() {
        highlightLifeline = null;
        this.startEvent = 0;
        this.nbEvent = 0;
        highlightColor = null;
    }

    @Override
    protected void computeMinMax() {
        List<SDTimeEvent> timeArray = buildTimeArray();
        if (timeArray == null)
            return;
        for (int i = 0; i < timeArray.size() - 1; i++) {
            SDTimeEvent m1 = (SDTimeEvent) timeArray.get(i);
            SDTimeEvent m2 = (SDTimeEvent) timeArray.get(i + 1);
            if (SDViewPref.getInstance().excludeExternalTime())
                if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.startLifeline == null) || (mes1.endLifeline == null))
                        continue;
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
     */
    public boolean findDateBounds(TmfTimestamp dateToFind, ITimeRange bounds[]) {
        if (hasTimeInfo()) {
            List<SDTimeEvent> timeArray = buildTimeArray();
            bounds[0] = null;
            bounds[1] = null;
            for (int i = 0; i < timeArray.size(); i++) {
                SDTimeEvent m = (SDTimeEvent) timeArray.get(i);
                if (m.getTime().compareTo(dateToFind, true) > 0) {
                    bounds[1] = m.getGraphNode();
                    if (i > 0) {
                        bounds[0] = ((SDTimeEvent) timeArray.get(i - 1)).getGraphNode();
                        return true;
                    }
                    return false;
                }
            }
            bounds[0] = ((SDTimeEvent) timeArray.get(timeArray.size() - 1)).getGraphNode();
        }
        return false;
    }

    protected void setHasTimeInfo(boolean value) {
        timeInfo = value;
    }

    /**
     * @return true if frame has time info else false
     */
    public boolean hasTimeInfo() {
        return timeInfo;
    }

    /**
     * @param lifeline
     * @param startEvent
     * @param nbEvent
     * @param color
     */
    public void highlightTimeCompression(Lifeline lifeline, int startEvent, int nbEvent, IColor color) {
        highlightLifeline = lifeline;
        this.startEvent = startEvent;
        this.nbEvent = nbEvent;
        highlightColor = color;
    }

    /**
     * Set the lifeline categories which will be use during the lifelines creation
     * 
     * @see Lifeline#setCategory(int)
     * @param categories the lifeline categories array
     */
    public void setLifelineCategories(LifelineCategories[] categories) {
        lifelineCategories = Arrays.copyOf(categories, categories.length);
    }

    /**
     * Returns the lifeline categories array set for the this frame
     * 
     * @return the lifeline categories array or null if not set
     */
    public LifelineCategories[] getLifelineCategories() {
        return  Arrays.copyOf(lifelineCategories, lifelineCategories.length); 
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
     * @param the message to add
     */
    public void addMessage(BaseMessage message) {
        addNode(message);
    }

    @Override
    public void draw(IGC context) {
        drawFrame(context);
        if (!hasChilden)
            return;
        
        if (highlightLifeline != null) {
            IColor backupColor = context.getBackground();
            context.setBackground(Frame.getUserPref().getTimeCompressionSelectionColor());
            int gy = highlightLifeline.getY() + highlightLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * startEvent;
            context.fillRectangle(Metrics.FRAME_H_MARGIN + 1, gy, highlightLifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.FRAME_H_MARGIN, (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * nbEvent);
            context.setBackground(backupColor);
        }
        super.draw(context, false);
        int lifelineArryStep = 1;
        if (Metrics.swimmingLaneWidth() * context.getZoom() < Metrics.LIFELINE_SIGNIFICANT_HSPACING)
            lifelineArryStep = Math.round(Metrics.LIFELINE_SIGNIFICANT_HSPACING / (Metrics.swimmingLaneWidth() * context.getZoom()));
        if (indexes.size() == 0)
            return;
        int lifeLineDrawIndex = ((Integer) indexes.get(Lifeline.LIFELINE_TAG)).intValue();
        for (int i = lifeLineDrawIndex; i < ((List<GraphNode>) nodes.get(Lifeline.LIFELINE_TAG)).size(); i = i + lifelineArryStep) {
            Lifeline toDraw = (Lifeline) ((List<GraphNode>) nodes.get(Lifeline.LIFELINE_TAG)).get(i);
            if (toDraw.getX() - Metrics.LIFELINE_SPACING / 2 > context.getContentsX() + context.getVisibleWidth())
                break;
            toDraw.drawName(context);

            if (highlightLifeline != null) {
                if (toDraw == highlightLifeline)
                    toDraw.highlightExecOccurrenceRegion(context, startEvent, nbEvent, highlightColor);
                else if ((toDraw.getIndex() < highlightLifeline.getIndex()) || ((toDraw.getIndex() < highlightLifeline.getIndex()))) {

                    int acIndex = toDraw.getExecOccurrenceDrawIndex();
                    // acIndex = first visible execution occurrence
                    // for drawing speed reason with only search on the visible subset
                    if (toDraw.getExecutions() != null)
                        for (int index = acIndex; index < toDraw.getExecutions().size(); index++) {
                            BasicExecutionOccurrence exec = (BasicExecutionOccurrence) toDraw.getExecutions().get(index);
                            int tempEvent = startEvent;
                            for (int j = 0; j < nbEvent; j++) {
                                if (((tempEvent >= exec.startEventOccurrence) && (tempEvent <= exec.endEventOccurrence) && (tempEvent + 1 >= exec.startEventOccurrence) && (tempEvent + 1 <= exec.endEventOccurrence))) {
                                    toDraw.highlightExecOccurrenceRegion(context, tempEvent, 1, Frame.getUserPref().getTimeCompressionSelectionColor());
                                }
                                tempEvent = tempEvent + 1;
                            }
                            // if we are outside the visible area we stop right now
                            // This works because execution occurrences are ordered along the Y axis
                            if (exec.getY() > getY())
                                break;
                        }
                }
            }
        }
    }

    @Override
    protected List<SDTimeEvent> buildTimeArray() {
        if (!hasChilden)
            return null;
        try {
            List<SDTimeEvent> timeArray = super.buildTimeArray();
            executionOccurrencesWithTime = null;
            if (getLifelines() != null)
                for (int i = 0; i < ((List<GraphNode>) nodes.get(Lifeline.LIFELINE_TAG)).size(); i++) {
                    Lifeline l = (Lifeline) ((List<GraphNode>) nodes.get(Lifeline.LIFELINE_TAG)).get(i);
                    if (l.hasTimeInfo() && l.getExecutions() != null) {
                        for (Iterator<GraphNode> j = l.getExecutions().iterator(); j.hasNext();) {
                            GraphNode o = j.next();
                            if (o instanceof ExecutionOccurrence) {
                                ExecutionOccurrence eo = (ExecutionOccurrence) o;
                                if (eo.hasTimeInfo()) {
                                    int event = eo.getStartOccurrence();
                                    ITmfTimestamp time = eo.getStartTime();
                                    SDTimeEvent f = new SDTimeEvent(time, event, eo);
                                    timeArray.add(f);
                                    if (executionOccurrencesWithTime == null) {
                                        executionOccurrencesWithTime = new ArrayList<SDTimeEvent>();
                                    }
                                    executionOccurrencesWithTime.add(f);
                                    event = eo.getEndOccurrence();
                                    time = eo.getEndTime();
                                    f = new SDTimeEvent(time, event, eo);
                                    timeArray.add(f);
                                    executionOccurrencesWithTime.add(f);
                                }
                            }
                        }
                    }
                }

            if (executionOccurrencesWithTime != null) {
                SDTimeEvent[] temp = executionOccurrencesWithTime.toArray(new SDTimeEvent[0]);
                Arrays.sort(temp, new TimeEventComparator());
                executionOccurrencesWithTime = Arrays.asList(temp);
            }
            SDTimeEvent[] temp = timeArray.toArray(new SDTimeEvent[0]);
            Arrays.sort(temp, new TimeEventComparator());
            timeArray = Arrays.asList(temp);
            return timeArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    protected GraphNode getCloserLeavingMessage(Lifeline lifeline, BaseMessage message, List<GraphNode> list, boolean smallerEvent) {
        if (list == null)
            return null;
        if (smallerEvent == false) {
            int event = 0;
            if (message != null)
                event = message.getEventOccurrence();
            for (int i = 0; i < list.size(); i++) {
                GraphNode node = (GraphNode) list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() > event) && (syncNode.getStartLifeline() == lifeline) && !syncNode.isSameAs(message))
                        return node;
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() > event) && (asyncNode.getStartLifeline() == lifeline) && !asyncNode.isSameAs(message))
                        return node;
                }
            }
        } else {
            int event = getMaxEventOccurrence();
            if (message != null)
                if (message instanceof AsyncMessage) {
                    event = ((AsyncMessage) message).getStartOccurrence();
                } else
                    event = message.getEventOccurrence();
            for (int i = list.size() - 1; i >= 0; i--) {
                GraphNode node = (GraphNode) list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() < event) && (syncNode.getStartLifeline() == lifeline) && !syncNode.isSameAs(message))
                        return node;
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() < event) && (asyncNode.getStartLifeline() == lifeline) && !asyncNode.isSameAs(message))
                        return node;
                }
            }
        }
        return null;
    }

    protected GraphNode getCloserEnteringMessage(Lifeline lifeline, BaseMessage message, List<GraphNode> list, boolean smallerEvent) {
        if (list == null)
            return null;
        if (smallerEvent == false) {
            int event = 0;
            if (message != null)
                event = message.getEventOccurrence();
            for (int i = 0; i < list.size(); i++) {
                GraphNode node = (GraphNode) list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() > event) && (syncNode.getEndLifeline() == lifeline) && !syncNode.isSameAs(message))
                        return node;
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() > event) && (asyncNode.getEndLifeline() == lifeline) && !asyncNode.isSameAs(message))
                        return node;
                }
            }
        } else {
            int event = getMaxEventOccurrence();
            if (message != null)
                if (message instanceof AsyncMessage) {
                    event = ((AsyncMessage) message).getStartOccurrence();
                } else
                    event = message.getEventOccurrence();
            for (int i = list.size() - 1; i >= 0; i--) {
                GraphNode node = (GraphNode) list.get(i);
                if (node instanceof SyncMessage) {
                    SyncMessage syncNode = (SyncMessage) node;
                    if ((syncNode.getEventOccurrence() < event) && (syncNode.getEndLifeline() == lifeline) && !syncNode.isSameAs(message))
                        return node;
                } else if (node instanceof AsyncMessage) {
                    AsyncMessage asyncNode = (AsyncMessage) node;
                    if ((asyncNode.getStartOccurrence() < event) && (asyncNode.getEndLifeline() == lifeline) && !asyncNode.isSameAs(message))
                        return node;
                }
            }
        }
        return null;
    }

    protected int distanceFromEvent(GraphNode node, int event) {
        int distance = 0;
        if (node instanceof SyncMessage)
            distance = ((SyncMessage) node).getEventOccurrence() - event;
        else if (node instanceof AsyncMessage) {
            int start = ((AsyncMessage) node).getStartOccurrence();
            int end = ((AsyncMessage) node).getEndOccurrence();
            if ((start - event) < (end - event))
                distance = start - event;
            else
                distance = end - event;
        }
        return Math.abs(distance);
    }

    protected GraphNode getCloserToEvent(GraphNode node1, GraphNode node2, int event) {
        if ((node1 != null) && (node2 != null)) {
            if (distanceFromEvent(node1, event) < distanceFromEvent(node2, event))
                return node1;
            else
                return node2;
        } else if (node1 != null)
            return node1;
        else if (node2 != null)
            return node2;
        else
            return null;
    }

    public GraphNode getCalledMessage(BaseMessage StartMessage) {
        int event = 0;
        GraphNode result = null;
        Lifeline lifeline = null;
        if (StartMessage != null) {
            event = ((BaseMessage) StartMessage).getEventOccurrence();
            lifeline = ((BaseMessage) StartMessage).getEndLifeline();
            if (lifeline == null)
                lifeline = ((BaseMessage) StartMessage).getStartLifeline();
        }
        if (lifeline == null)
            return null;
        GraphNode message = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessages(), false);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessagesReturn(), false);
        result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessages(), false);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessagesReturn(), false);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    public GraphNode getCallerMessage(BaseMessage StartMessage) {
        int event = getMaxEventOccurrence();
        GraphNode result = null;
        Lifeline lifeline = null;
        if (StartMessage != null) {
            event = ((BaseMessage) StartMessage).getEventOccurrence();
            lifeline = ((BaseMessage) StartMessage).getStartLifeline();
            if (lifeline == null)
                lifeline = ((BaseMessage) StartMessage).getEndLifeline();
        }
        if (lifeline == null)
            return null;
        GraphNode message = getCloserEnteringMessage(lifeline, StartMessage, getSyncMessages(), true);
        GraphNode messageReturn = getCloserEnteringMessage(lifeline, StartMessage, getSyncMessagesReturn(), true);
        result = getCloserToEvent(message, messageReturn, event);
        message = getCloserEnteringMessage(lifeline, StartMessage, getAsyncMessages(), true);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserEnteringMessage(lifeline, StartMessage, getAsyncMessagesReturn(), true);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    public GraphNode getNextLifelineMessage(Lifeline lifeline, BaseMessage StartMessage) {
        int event = 0;
        if (StartMessage != null)
            event = ((BaseMessage) StartMessage).getEventOccurrence();
        if (lifeline == null)
            return null;
        GraphNode message = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessages(), false);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessagesReturn(), false);
        GraphNode result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessages(), false);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessagesReturn(), false);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }

    public BasicExecutionOccurrence getFirstExecution(Lifeline lifeline) {
        if (lifeline == null)
            return null;
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null)
            return null;
        if (list.size() == 0)
            return null;
        BasicExecutionOccurrence result = (BasicExecutionOccurrence) list.get(0);
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() < result.getEndOccurrence()))
                result = e;
        }
        return result;
    }

    public BasicExecutionOccurrence getPrevExecOccurrence(BasicExecutionOccurrence exec) {
        if (exec == null)
            return null;
        Lifeline lifeline = exec.getLifeline();
        if (lifeline == null)
            return null;
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null)
            return null;
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() < exec.startEventOccurrence) && (result == null))
                result = e;
            if ((e.getStartOccurrence() < exec.startEventOccurrence) && (e.getStartOccurrence() >= result.getEndOccurrence()))
                result = e;
        }
        return result;
    }

    public BasicExecutionOccurrence getNextExecOccurrence(BasicExecutionOccurrence exec) {
        if (exec == null)
            return null;
        Lifeline lifeline = exec.getLifeline();
        if (lifeline == null)
            return null;
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null)
            return null;
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if ((e.getStartOccurrence() > exec.startEventOccurrence) && (result == null))
                result = e;
            if ((e.getStartOccurrence() > exec.startEventOccurrence) && (e.getStartOccurrence() <= result.getEndOccurrence()))
                result = e;
        }
        return result;
    }

    public BasicExecutionOccurrence getLastExecOccurrence(Lifeline lifeline) {
        if (lifeline == null)
            return null;
        List<GraphNode> list = lifeline.getExecutions();
        if (list == null)
            return null;
        BasicExecutionOccurrence result = null;
        for (int i = 0; i < list.size(); i++) {
            BasicExecutionOccurrence e = (BasicExecutionOccurrence) list.get(i);
            if (result == null)
                result = e;
            if (e.getStartOccurrence() > result.getEndOccurrence())
                result = e;
        }
        return result;
    }

    public GraphNode getPrevLifelineMessage(Lifeline lifeline, BaseMessage StartMessage) {
        int event = getMaxEventOccurrence();
        if (StartMessage != null)
            if (StartMessage instanceof AsyncMessage) {
                event = ((AsyncMessage) StartMessage).getStartOccurrence();
            } else
                event = StartMessage.getEventOccurrence();
        if (lifeline == null)
            return null;
        GraphNode message = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessages(), true);
        GraphNode messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getSyncMessagesReturn(), true);
        GraphNode result = getCloserToEvent(message, messageReturn, event);
        message = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessages(), true);
        result = getCloserToEvent(result, message, event);
        messageReturn = getCloserLeavingMessage(lifeline, StartMessage, getAsyncMessagesReturn(), true);
        result = getCloserToEvent(result, messageReturn, event);
        return result;
    }
}
