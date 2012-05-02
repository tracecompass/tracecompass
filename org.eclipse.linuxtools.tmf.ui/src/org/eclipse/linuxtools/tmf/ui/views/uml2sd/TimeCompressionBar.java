/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * 
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ExecutionOccurrence;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Metrics;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SDTimeEvent;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.ColorImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.TimeEventComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * The time compression bar implementation.
 * </p>
 * 
 * @version 1.0
 * @author sveyrier
 * 
 */
public class TimeCompressionBar extends ScrollView implements DisposeListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The listener list
     */
    protected ArrayList<ITimeCompressionListener> listenerList = null;
    /**
     * The current frame displayed.
     */
    protected Frame frame = null;
    /**
     * List of time events.
     */
    protected List<SDTimeEvent> nodeList = null;
    /**
     * The minimum time delta.
     */
    protected ITmfTimestamp min = new TmfTimestamp();
    /**
     * The maximum time delta.
     */
    protected ITmfTimestamp max = new TmfTimestamp();
    /**
     * The current zoom value.
     */
    protected float zoomValue = 1;
    /**
     * The tooltip to display.
     */
    protected DrawableToolTip tooltip = null;
    /**
     *  Array of colors for displaying wight of time deltas. 
     */
    protected ColorImpl[] col;
    /**
     * The accessible object reference.
     */
    protected Accessible accessible = null;
    /**
     * The focused widget reference.
     */
    protected int focusedWidget = -1;
    /**
     * The sequence diagram view reference. 
     */
    protected SDView view = null;
    /**
     * The current lifeline.
     */
    protected Lifeline ll = null;
    /**
     * The current start event value.
     */
    protected int ls = 0;
    /**
     * The current number of events.
     */
    protected int ln = 0;
    /**
     * The Current color of range to display. 
     */
    protected IColor lc = null;
    /**
     *  The next graph node y coordinate. 
     */
    protected int nextNodeY = 0;
    /**
     *  The previous graph node y coordinate. 
     */
    protected int prevNodeY = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     * 
     * @param parent The parent composite
     * @param s The style bits
     */
    public TimeCompressionBar(Composite parent, int s) {
        super(parent, s | SWT.NO_BACKGROUND, false);
        setVScrollBarMode(ScrollView.ALWAYS_OFF);
        setHScrollBarMode(ScrollView.ALWAYS_OFF);
        listenerList = new ArrayList<ITimeCompressionListener>();
        col = new ColorImpl[10];
        col[0] = new ColorImpl(Display.getDefault(), 255, 229, 229);
        col[1] = new ColorImpl(Display.getDefault(), 255, 204, 204);
        col[2] = new ColorImpl(Display.getDefault(), 255, 178, 178);
        col[3] = new ColorImpl(Display.getDefault(), 255, 153, 153);
        col[4] = new ColorImpl(Display.getDefault(), 255, 127, 127);
        col[5] = new ColorImpl(Display.getDefault(), 255, 102, 102);
        col[6] = new ColorImpl(Display.getDefault(), 255, 76, 76);
        col[7] = new ColorImpl(Display.getDefault(), 255, 51, 51);
        col[8] = new ColorImpl(Display.getDefault(), 255, 25, 25);
        col[9] = new ColorImpl(Display.getDefault(), 255, 0, 0);
        super.addDisposeListener(this);

        accessible = getViewControl().getAccessible();

        accessible.addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                if (e.childID == ACC.CHILDID_SELF) {
                    // e.result = "Sequence Diagram";
                }
                // Case toolTip
                else if (e.childID == 0) {
                    if (tooltip != null) {
                        e.result = tooltip.getAccessibleText();
                    }
                } else if (e.childID == 1) {
                    createFakeTooltip();
                    e.result = tooltip.getAccessibleText();
                }
            }
        });

        accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getFocus(org.eclipse.swt.accessibility.AccessibleControlEvent)
             */
            @Override
            public void getFocus(AccessibleControlEvent e) {
                if (focusedWidget == -1) {
                    e.childID = ACC.CHILDID_SELF;
                }
                else {
                    e.childID = focusedWidget;
                }
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getRole(org.eclipse.swt.accessibility.AccessibleControlEvent)
             */
            @Override
            public void getRole(AccessibleControlEvent e) {
                switch (e.childID) {
                case ACC.CHILDID_SELF:
                    e.detail = ACC.ROLE_CLIENT_AREA;
                    break;
                case 0:
                    e.detail = ACC.ROLE_TOOLTIP;
                    break;
                case 1:
                    e.detail = ACC.ROLE_LABEL;
                    break;
                }
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getState(org.eclipse.swt.accessibility.AccessibleControlEvent)
             */
            @Override
            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_FOCUSABLE;
                if (e.childID == ACC.CHILDID_SELF) {
                    e.detail |= ACC.STATE_FOCUSED;
                } else {
                    e.detail |= ACC.STATE_SELECTABLE;
                    if (e.childID == focusedWidget) {
                        e.detail |= ACC.STATE_FOCUSED | ACC.STATE_SELECTED | ACC.STATE_CHECKED;
                    }
                }
            }
        });

        getViewControl().addTraverseListener(new TraverseListener() {

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
             */
            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                    e.doit = true;
                }
            }

        });

        addTraverseListener(new TraverseListener() {

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
             */
            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                    e.doit = true;
                }
            }

        });

        getViewControl().addFocusListener(new FocusListener() {

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusGained(FocusEvent e) {
                redraw();
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusLost(FocusEvent e) {
                redraw();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Sets the focus widget
     * 
     * @param newFocusShape widget reference to set
     */
    void setFocus(int newFocusShape) {
        focusedWidget = newFocusShape;
        if (focusedWidget == -1) {
            getViewControl().getAccessible().setFocus(ACC.CHILDID_SELF);
        } else {
            getViewControl().getAccessible().setFocus(focusedWidget);
        }
    }

    /**
     * Sets the current frame.
     * 
     * @param theFrame The frame to set
     */
    public void setFrame(Frame theFrame) {
        frame = theFrame;
        min = frame.getMinTime();
        max = frame.getMaxTime();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#drawContents(org.eclipse.swt.graphics.GC, int, int, int, int)
     */
    @Override
    protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {
        if (frame == null) {
            return;
        }
        nodeList = new ArrayList<SDTimeEvent>();
        int messageArraysStep = 1;

        if ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * zoomValue < Metrics.MESSAGE_SIGNIFICANT_VSPACING + 1) {
            messageArraysStep = Math.round(Metrics.MESSAGE_SIGNIFICANT_VSPACING + 1 / ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * zoomValue));
        }

        int firstVisible = frame.getFirstVisibleSyncMessage();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < frame.syncMessageCount(); i = i + messageArraysStep) {
            SyncMessage m = frame.getSyncMessage(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getEventOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                if (m.getY() * zoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = frame.getFirstVisibleSyncMessageReturn();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < frame.syncMessageReturnCount(); i = i + messageArraysStep) {
            SyncMessage m = frame.getSyncMessageReturn(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getEventOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                if (m.getY() * zoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = frame.getFirstVisibleAsyncMessage();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < frame.asyncMessageCount(); i = i + messageArraysStep) {
            AsyncMessage m = frame.getAsyncMessage(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getStartOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                t = new SDTimeEvent(m.getEndTime(), m.getEndOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                if (m.getY() * zoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = frame.getFirstVisibleAsyncMessageReturn();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < frame.asyncMessageReturnCount(); i = i + messageArraysStep) {
            AsyncMessageReturn m = frame.getAsyncMessageReturn(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getStartOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                t = new SDTimeEvent(m.getEndTime(), m.getEndOccurrence(), (ITimeRange) m);
                nodeList.add(t);
                if (m.getY() * zoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        List<SDTimeEvent> executionOccurrencesWithTime = frame.getExecutionOccurrencesWithTime();
        if (executionOccurrencesWithTime != null) {
            nodeList.addAll(executionOccurrencesWithTime);
        }

        SDTimeEvent[] temp = nodeList.toArray(new SDTimeEvent[0]);
        Arrays.sort(temp, new TimeEventComparator());
        nodeList = Arrays.asList(temp);

        Image dbuffer = null;
        GC gcim = null;
        try {
            dbuffer = new Image(getDisplay(), getClientArea().width, getClientArea().height);
        } catch (Exception e) {
            TmfUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,TmfUiPlugin.PLUGIN_ID, "Error creating image", e)); //$NON-NLS-1$
        }
        gcim = new GC(dbuffer);
        for (int i = 0; i < nodeList.size() - 1; i++) {
            SDTimeEvent m1 = (SDTimeEvent) nodeList.get(i);
            SDTimeEvent m2 = (SDTimeEvent) nodeList.get(i + 1);

            if (SDViewPref.getInstance().excludeExternalTime()) {
                if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                        continue;
                    }
                }
            }

            min = frame.getMinTime();
            max = frame.getMaxTime();
            ITmfTimestamp minMaxdelta = max.getDelta(min);
            double gr = (minMaxdelta.getValue()) / (double) 10;

            ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime()).getDelta(min);
            long absDelta = Math.abs(delta.getValue());
            
            ColorImpl color;
            if (gr != 0) {
                int colIndex = Math.round((float) (absDelta / gr));
                if (colIndex < col.length && colIndex > 0) {
                    color = col[colIndex - 1];
                } else if (colIndex <= 0) {
                    color = col[0];
                } else {
                    color = col[col.length - 1];
                }
            } else {
                color = col[0];
            }

            if (color.getColor() instanceof Color) {
                gcim.setBackground((Color) color.getColor());
            }
            int y1 = ((GraphNode) m1.getGraphNode()).getY();
            int y2 = ((GraphNode) m2.getGraphNode()).getY();
            if (m1.getGraphNode() instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) m1.getGraphNode();
                if (as.getEndTime() == m1.getTime()) {
                    y1 += as.getHeight();
                }
            }
            if (m2.getGraphNode() instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) m2.getGraphNode();
                if (as.getEndTime() == m2.getTime()) {
                    y2 += as.getHeight();
                }
            }
            if (m1.getGraphNode() instanceof ExecutionOccurrence) {

                ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                if (m1.getEvent() == eo.getEndOccurrence()) {
                    y1 += eo.getHeight();
                }

                if (m2.getGraphNode() instanceof ExecutionOccurrence) {

                    ExecutionOccurrence eo2 = (ExecutionOccurrence) m2.getGraphNode();
                    if (m2.getEvent() == eo2.getEndOccurrence()) {
                        y2 += eo2.getHeight();
                    }

                }
            }
            gcim.fillRectangle(contentsToViewX(0), contentsToViewY(Math.round(y1 * zoomValue)), 10, Math.round((y2 - y1) * zoomValue) + 1);
            if (messageArraysStep == 1) {
                Color backupColor = gcim.getForeground();
                gcim.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                gcim.drawRectangle(contentsToViewX(0), contentsToViewY(Math.round(y1 * zoomValue)), 9, Math.round((y2 - y1) * zoomValue));
                gcim.setForeground(backupColor);
            }
        }
        if (getViewControl().isFocusControl() || isFocusControl()) {
            gcim.drawFocus(contentsToViewX(0), contentsToViewY(Math.round(prevNodeY * zoomValue)), contentsToViewX(10), Math.round((nextNodeY - prevNodeY) * zoomValue));
        }
        try {
            gc.drawImage(dbuffer, 0, 0, getClientArea().width, getClientArea().height, 0, 0, getClientArea().width, getClientArea().height);
        } catch (Exception e) {
            TmfUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,TmfUiPlugin.PLUGIN_ID, "Error drawing image", e)); //$NON-NLS-1$
        }
        gcim.dispose();
        if (dbuffer != null) {
            dbuffer.dispose();
        }
        gc.dispose();
    }

    /**
     * Checks for focus of children.
     * 
     * @param children Control to check
     * @return true if child is on focus else false
     */
    protected boolean checkFocusOnChilds(Control childs) {
        if (childs instanceof Composite) {
            Control[] child = ((Composite) childs).getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    return true;
                } else {
                    checkFocusOnChilds(child[i]);
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#isFocusControl()
     */
    @Override
    public boolean isFocusControl() {
        Control[] child = getChildren();
        for (int i = 0; i < child.length; i++) {
            if (child[i].isFocusControl()) {
                return true;
            } else {
                checkFocusOnChilds(child[i]);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#contentsMouseMoveEvent(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    protected void contentsMouseMoveEvent(MouseEvent event) {
        if (tooltip != null) {
            tooltip.hideToolTip();
        }
        super.contentsMouseMoveEvent(event);
        if (!isFocusControl() || getViewControl().isFocusControl()) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    break;
                }
            }
        }
        setFocus(-1);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#contentsMouseHover(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    protected void contentsMouseHover(MouseEvent e) {
        if (tooltip == null) {
            tooltip = new DrawableToolTip(this);
        }
        if (frame != null) {
            setFocus(0);
            for (int i = 0; i < nodeList.size() - 1; i++) {
                SDTimeEvent m1 = (SDTimeEvent) nodeList.get(i);
                SDTimeEvent m2 = (SDTimeEvent) nodeList.get(i + 1);

                if (SDViewPref.getInstance().excludeExternalTime()) {
                    if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                        if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                            continue;
                        }
                    }
                }

                int y1 = ((GraphNode) m1.getGraphNode()).getY();
                int y2 = ((GraphNode) m2.getGraphNode()).getY();

                if (m1.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m1.getGraphNode();
                    if (as.getEndTime() == m1.getTime()) {
                        y1 += as.getHeight();
                    }
                }
                if (m2.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m2.getGraphNode();
                    if (as.getEndTime() == m2.getTime()) {
                        y2 += as.getHeight();
                    }
                }
                if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                    ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                    if (m1.getEvent() == eo.getEndOccurrence()) {
                        y1 += eo.getHeight();
                    }

                    if (m2.getGraphNode() instanceof ExecutionOccurrence) {

                        ExecutionOccurrence eo2 = (ExecutionOccurrence) m2.getGraphNode();
                        if (m2.getEvent() == eo2.getEndOccurrence()) {
                            y2 += eo2.getHeight();
                        }
                    }
                }
                int m1Y = Math.round(y1 * zoomValue);
                int m2Y = Math.round(y2 * zoomValue);
                if ((m1Y < e.y) && (m2Y >= e.y)) {
                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
                    tooltip.showToolTip(delta, min, max);
                }
            }
        }
        setFocus(0);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#contentsMouseExit(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    protected void contentsMouseExit(MouseEvent e) {
        if (tooltip != null)
            tooltip.hideToolTip();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#contentsMouseUpEvent(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    protected void contentsMouseUpEvent(MouseEvent event) {
        selectTimeDelta(event.y, 0);
        setFocus();
        super.contentsMouseUpEvent(event);
    }

    /**
     * Force the time compression bar to highlight the event occurrences between the two given messages. The event
     * occurrences are highlighted on the first message's end lifeline
     * 
     * @param mes1 the first message
     * @param mes2 the second message
     */
    public void highlightRegion(BaseMessage mes1, BaseMessage mes2) {
        if (frame == null) {
            return;
        }
        if (!(mes1 instanceof ITimeRange)) {
            return;
        }
        if (!(mes2 instanceof ITimeRange)) {
            return;
        }
        ITimeRange t1 = (ITimeRange) mes1;
        ITimeRange t2 = (ITimeRange) mes2;

        ITmfTimestamp time1 = t1.getStartTime();
        ITmfTimestamp time2 = t2.getStartTime();
        int event1 = mes1.getEventOccurrence();
        int event2 = mes2.getEventOccurrence();

        if (mes1 instanceof AsyncMessage) {
            AsyncMessage as = (AsyncMessage) mes2;
            time1 = as.getEndTime();
            event1 = as.getEndOccurrence();
        }
        if (mes2 instanceof AsyncMessage) {
            AsyncMessage as = (AsyncMessage) mes2;
            if (as.getEndOccurrence() > as.getStartOccurrence()) {
                time1 = as.getEndTime();
                event1 = as.getEndOccurrence();
            } else {
                time1 = as.getStartTime();
                event1 = as.getStartOccurrence();
            }
        }

        if (event1 > event2) {
            BaseMessage tempMes = mes2;
            mes2 = mes1;
            mes1 = tempMes;

            t1 = (ITimeRange) mes1;
            t2 = (ITimeRange) mes2;

            time1 = t1.getStartTime();
            time2 = t2.getStartTime();
            event1 = mes1.getEventOccurrence();
            event2 = mes2.getEventOccurrence();

            if (mes1 instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) mes2;
                time1 = as.getEndTime();
                event1 = as.getEndOccurrence();
            }
            if (mes2 instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) mes2;
                if (as.getEndOccurrence() > as.getStartOccurrence()) {
                    time1 = as.getEndTime();
                    event1 = as.getEndOccurrence();
                } else {
                    time1 = as.getStartTime();
                    event1 = as.getStartOccurrence();
                }
            }
        }

        ITmfTimestamp minMaxdelta = max.getDelta(min);
        double gr = (minMaxdelta.getValue()) / (double) 10;

        ITmfTimestamp delta = time2.getDelta(time1).getDelta(min);
        long absDelta = Math.abs(delta.getValue());

        int colIndex = 0;
        if (gr != 0) {
            colIndex = Math.round((float) (absDelta / gr));
            if (colIndex >= col.length) {
                colIndex = col.length - 1;
            } else if (colIndex < 0) {
                colIndex = 0;
            }
        } else {
            colIndex = 0;
        }
        for (int j = 0; j < listenerList.size(); j++) {
            ITimeCompressionListener list = (ITimeCompressionListener) listenerList.get(j);
            if (mes1.getEndLifeline() != null) {
                list.deltaSelected(mes1.getEndLifeline(), event1, event2 - event1, col[colIndex]);
            } else if (mes2.getStartLifeline() != null) {
                list.deltaSelected(mes2.getStartLifeline(), event1, event2 - event1, col[colIndex]);
            } else {
                list.deltaSelected(mes1.getStartLifeline(), event1, event2 - event1, col[colIndex]);
            }
        }
    }

    /**
     * Force the time compression bar to highlight the event occurrences between the two given messages. The event
     * occurrences are highlighted on the first message's end lifeline
     * 
     * @param mes1 the first message
     * @param mes2 the second message
     */
    public void highlightRegionSync(final BaseMessage mes1, final BaseMessage mes2) {
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                highlightRegion(mes1, mes2);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#scrollBy(int, int)
     */
    @Override
    public void scrollBy(int x, int y) {
    }

    /**
     * Sets the zoom value.
     * 
     * @param value The zoom value to set.
     */
    public void setZoom(float value) {
        zoomValue = value;
        redraw();
    }

    /**
     * Adds a listener to the time compression listener list to be notified about selected deltas.
     *  
     * @param listener The listener to add
     */
    public void addTimeCompressionListener(ITimeCompressionListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    /**
     * Removes a time compression listener.
     * 
     * @param listener The listener to remove.
     */
    public void removeSelectionChangedListener(ITimeCompressionListener listener) {
        listenerList.remove(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (tooltip != null) {
            tooltip.dispose();
        }
        super.removeDisposeListener(this);
        for (int i = 0; i < col.length; i++) {
            col[i].dispose();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#keyPressedEvent(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    protected void keyPressedEvent(KeyEvent _e) {
        if (tooltip != null) {
            tooltip.hideToolTip();
        }
        if (!isFocusControl() || getViewControl().isFocusControl()) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    // getViewControl().setFocus();
                    break;
                }
            }
        }
        setFocus(-1);

        boolean top = false;
        if (nextNodeY == 0) {
            top = true;
        }
        if ((frame != null) && (nextNodeY == 0)) {
            for (int i = 0; i < nodeList.size() - 1 && i < 1; i++) {
                SDTimeEvent m1 = (SDTimeEvent) nodeList.get(i);
                SDTimeEvent m2 = (SDTimeEvent) nodeList.get(i + 1);
                if (SDViewPref.getInstance().excludeExternalTime()) {
                    if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                        if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                            continue;
                        }
                    }
                }

                int y1 = ((GraphNode) m1.getGraphNode()).getY();
                int y2 = ((GraphNode) m2.getGraphNode()).getY();
                if (m1.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m1.getGraphNode();
                    if (as.getEndTime() == m1.getTime()) {
                        y1 += as.getHeight();
                    }
                }
                if (m2.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m2.getGraphNode();
                    if (as.getEndTime() == m2.getTime()) {
                        y2 += as.getHeight();
                    }
                }
                if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                    ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                    if (m1.getEvent() == eo.getEndOccurrence()) {
                        y1 += eo.getHeight();
                    }

                    if (m2.getGraphNode() instanceof ExecutionOccurrence) {

                        ExecutionOccurrence eo2 = (ExecutionOccurrence) m2.getGraphNode();
                        if (m2.getEvent() == eo2.getEndOccurrence()) {
                            y2 += eo2.getHeight();
                        }
                    }
                }
                prevNodeY = Math.round(y1 * zoomValue);
                nextNodeY = Math.round(y2 * zoomValue);
            }
        }

        if (ll != null) {
            for (int j = 0; j < listenerList.size(); j++) {
                ITimeCompressionListener list = (ITimeCompressionListener) listenerList.get(j);
                list.deltaSelected(ll, ls, ln, lc);
            }
        }

        if (_e.keyCode == SWT.ARROW_DOWN) {
            if (!top) {
                selectTimeDelta(nextNodeY + 1, 1);
            } else {
                selectTimeDelta(prevNodeY + 1, 1);
            }
            setFocus(1);
        } else if (_e.keyCode == SWT.ARROW_UP) {
            selectTimeDelta(prevNodeY - 1, 2);
            setFocus(1);
        } else if (_e.keyCode == SWT.ARROW_RIGHT) {
            selectTimeDelta(prevNodeY, 1);
            setFocus(1);
        }
        super.keyPressedEvent(_e);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#keyReleasedEvent(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    protected void keyReleasedEvent(KeyEvent _e) {
        super.keyReleasedEvent(_e);
    }

    /**
     * Selects the time delta for given delta y coordinate and direction.
     * 
     * @param dy The delta in y coordinate. 
     * @param direction 0 no direction, 1 = down, 2 = up
     */
    protected void selectTimeDelta(int dy, int direction) {
        SDTimeEvent lastM1 = null;
        SDTimeEvent lastM2 = null;
        int lastY1 = 0;
        int lastY2 = 0;
        boolean done = false;
        if (frame != null) {
            for (int i = 0; i < nodeList.size() - 1; i++) {
                SDTimeEvent m1 = (SDTimeEvent) nodeList.get(i);
                SDTimeEvent m2 = (SDTimeEvent) nodeList.get(i + 1);
                if (SDViewPref.getInstance().excludeExternalTime()) {
                    if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                        if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                            continue;
                        }
                    }
                }

                int y1 = ((GraphNode) m1.getGraphNode()).getY();
                int y2 = ((GraphNode) m2.getGraphNode()).getY();
                if (m1.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m1.getGraphNode();
                    if (as.getEndTime() == m1.getTime()) {
                        y1 += as.getHeight();
                    }
                }
                if (m2.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m2.getGraphNode();
                    if (as.getEndTime() == m2.getTime()) {
                        y2 += as.getHeight();
                    }
                }
                if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                    ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                    if (m1.getEvent() == eo.getEndOccurrence()) {
                        y1 += eo.getHeight();
                    }

                    if (m2.getGraphNode() instanceof ExecutionOccurrence) {
                        ExecutionOccurrence eo2 = (ExecutionOccurrence) m2.getGraphNode();
                        if (m2.getEvent() == eo2.getEndOccurrence()) {
                            y2 += eo2.getHeight();
                        }
                    }
                }
                int m1Y = Math.round(y1 * zoomValue);
                int m2Y = Math.round(y2 * zoomValue);

                if ((m1Y < dy) && (m2Y > dy) || (!done && m2Y > dy && direction == 1 && lastM1 != null) || (!done && m1Y > dy && direction == 2 && lastM1 != null)) {
                    if (m1Y > dy && direction == 2) {
                        m1 = lastM1;
                        m2 = lastM2;
                        m1Y = lastY1;
                        m2Y = lastY2;
                    }
                    done = true;
                    prevNodeY = m1Y;
                    nextNodeY = m2Y;
                    ITmfTimestamp minMaxdelta = max.getDelta(min);
                    double gr = (minMaxdelta.getValue()) / (double) 10;

                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime()).getDelta(min);
                    long absDelta = Math.abs(delta.getValue());

                    int colIndex = 0;
                    if (gr != 0) {
                        colIndex = Math.round((float) (absDelta / gr));
                        if (colIndex >= col.length) {
                            colIndex = col.length - 1;
                        } else if (colIndex < 0) {
                            colIndex = 0;
                        }
                    } else {
                        colIndex = 0;
                    }
                    if (m1.getGraphNode() instanceof BaseMessage) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        if (mes1.getEndLifeline() != null) {
                            ll = mes1.getEndLifeline();
                            ls = m1.getEvent();
                            ln = m2.getEvent() - m1.getEvent();
                            lc = col[colIndex];
                        } else if (m2.getGraphNode() instanceof BaseMessage && ((BaseMessage) m2.getGraphNode()).getStartLifeline() != null) {
                            ll = ((BaseMessage) m2.getGraphNode()).getStartLifeline();
                            ls = m1.getEvent();
                            ln = m2.getEvent() - m1.getEvent();
                            lc = col[colIndex];
                        } else {
                            ll = mes1.getStartLifeline();
                            ls = m1.getEvent();
                            ln = m2.getEvent() - m1.getEvent();
                            lc = col[colIndex];
                        }
                    } else if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                        if (m2.getGraphNode() instanceof ExecutionOccurrence) {
                            ExecutionOccurrence eo = (ExecutionOccurrence) m2.getGraphNode();
                            ll = eo.getLifeline();
                            ls = m1.getEvent();
                            ln = m2.getEvent() - m1.getEvent();
                            lc = col[colIndex];
                        } else {
                            ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                            ll = eo.getLifeline();
                            ls = m1.getEvent();
                            ln = m2.getEvent() - m1.getEvent();
                            lc = col[colIndex];
                        }
                    }
                    for (int j = 0; j < listenerList.size(); j++) {
                        ITimeCompressionListener list = (ITimeCompressionListener) listenerList.get(j);
                        list.deltaSelected(ll, ls, ln, lc);
                    }
                    break;
                } else {
                    lastM1 = m1;
                    lastM2 = m2;
                    lastY1 = m1Y;
                    lastY2 = m2Y;
                }
            }
        }
    }

    /**
     * Creates a fake tool tip. 
     */
    protected void createFakeTooltip() {
        if (tooltip == null) {
            tooltip = new DrawableToolTip(this);
        }

        if (frame != null) {
            setFocus(0);
            for (int i = 0; i < nodeList.size() - 1; i++) {
                SDTimeEvent m1 = (SDTimeEvent) nodeList.get(i);
                SDTimeEvent m2 = (SDTimeEvent) nodeList.get(i + 1);

                if (SDViewPref.getInstance().excludeExternalTime()) {
                    if ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage)) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                        if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                            continue;
                        }
                    }
                }

                int y1 = ((GraphNode) m1.getGraphNode()).getY();
                int y2 = ((GraphNode) m2.getGraphNode()).getY();

                if (m1.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m1.getGraphNode();
                    if (as.getEndTime() == m1.getTime()) {
                        y1 += as.getHeight();
                    }
                }
                if (m2.getGraphNode() instanceof AsyncMessage) {
                    AsyncMessage as = (AsyncMessage) m2.getGraphNode();
                    if (as.getEndTime() == m2.getTime()) {
                        y2 += as.getHeight();
                    }
                }
                if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                    ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                    if (m1.getEvent() == eo.getEndOccurrence()) {
                        y1 += eo.getHeight();
                    }

                    if (m2.getGraphNode() instanceof ExecutionOccurrence) {

                        ExecutionOccurrence eo2 = (ExecutionOccurrence) m2.getGraphNode();
                        if (m2.getEvent() == eo2.getEndOccurrence()) {
                            y2 += eo2.getHeight();
                        }
                    }
                }
                int m1Y = Math.round(y1 * zoomValue);
                int m2Y = Math.round(y2 * zoomValue);
                if ((m1Y < prevNodeY + 1) && (m2Y >= prevNodeY + 1)) {
                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
                    tooltip.showToolTip(delta, min, max);
                    tooltip.hideToolTip();
                }
            }
        }
    }
}
