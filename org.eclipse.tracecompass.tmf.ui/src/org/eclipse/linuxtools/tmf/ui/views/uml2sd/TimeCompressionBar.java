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

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
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
 */
public class TimeCompressionBar extends ScrollView implements DisposeListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int BASE_RED_VALUE = 255;
    private static final int BASE_GREEN_BLUE_VALUE = 225;
    private static final int COLOR_STEP = 25;
    private static final int NUMBER_STEPS = 10;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The listener list
     */
    private List<ITimeCompressionListener> fListenerList = null;
    /**
     * The current frame displayed.
     */
    private Frame fFrame = null;
    /**
     * List of time events.
     */
    private List<SDTimeEvent> fNodeList = null;
    /**
     * The minimum time delta.
     */
    private ITmfTimestamp fMinTime = new TmfTimestamp();
    /**
     * The maximum time delta.
     */
    private ITmfTimestamp fMaxTime = new TmfTimestamp();
    /**
     * The current zoom value.
     */
    private float fZoomValue = 1;
    /**
     * The tooltip to display.
     */
    private DrawableToolTip fTooltip = null;
    /**
     *  Array of colors for displaying wight of time deltas.
     */
    private ColorImpl[] fColors;
    /**
     * The accessible object reference.
     */
    private Accessible fAccessible = null;
    /**
     * The focused widget reference.
     */
    private int fFocusedWidget = -1;
    /**
     * The current lifeline.
     */
    private Lifeline fLifeline = null;
    /**
     * The current start event value.
     */
    private int fLifelineStart = 0;
    /**
     * The current number of events.
     */
    private int fLifelineNumEvents = 0;
    /**
     * The Current color of range to display.
     */
    private IColor fLifelineColor = null;
    /**
     *  The next graph node y coordinate.
     */
    private int fNextNodeY = 0;
    /**
     *  The previous graph node y coordinate.
     */
    private int fPrevNodeY = 0;

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
        fListenerList = new ArrayList<>();
        fColors = new ColorImpl[NUMBER_STEPS];
        int greenBlue = BASE_GREEN_BLUE_VALUE;
        final int step = COLOR_STEP;
        for (int i = 0; i < fColors.length; i++) {
            fColors[i] = new ColorImpl(Display.getDefault(), BASE_RED_VALUE, greenBlue, greenBlue);
            greenBlue -= step;
        }
        super.addDisposeListener(this);

        fAccessible = getViewControl().getAccessible();

        fAccessible.addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                // Case toolTip
                if (e.childID == 0) {
                    if (fTooltip != null) {
                        e.result = fTooltip.getAccessibleText();
                    }
                } else if (e.childID == 1) {
                    createFakeTooltip();
                    e.result = fTooltip.getAccessibleText();
                }
            }
        });

        fAccessible.addAccessibleControlListener(new AccessibleControlAdapter() {
            @Override
            public void getFocus(AccessibleControlEvent e) {
                if (fFocusedWidget == -1) {
                    e.childID = ACC.CHILDID_SELF;
                }
                else {
                    e.childID = fFocusedWidget;
                }
            }

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
                default:
                   break;
                }
            }

            @Override
            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_FOCUSABLE;
                if (e.childID == ACC.CHILDID_SELF) {
                    e.detail |= ACC.STATE_FOCUSED;
                } else {
                    e.detail |= ACC.STATE_SELECTABLE;
                    if (e.childID == fFocusedWidget) {
                        e.detail |= ACC.STATE_FOCUSED | ACC.STATE_SELECTED | ACC.STATE_CHECKED;
                    }
                }
            }
        });

        getViewControl().addTraverseListener(new LocalTraverseListener());

        addTraverseListener(new LocalTraverseListener());

        getViewControl().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                redraw();
            }

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
        fFocusedWidget = newFocusShape;
        if (fFocusedWidget == -1) {
            getViewControl().getAccessible().setFocus(ACC.CHILDID_SELF);
        } else {
            getViewControl().getAccessible().setFocus(fFocusedWidget);
        }
    }

    /**
     * Sets the current frame.
     *
     * @param theFrame The frame to set
     */
    public void setFrame(Frame theFrame) {
        fFrame = theFrame;
        fMinTime = fFrame.getMinTime();
        fMaxTime = fFrame.getMaxTime();
    }

    @Override
    protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {
        if (fFrame == null) {
            return;
        }
        fNodeList = new ArrayList<>();
        int messageArraysStep = 1;

        if ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * fZoomValue < Metrics.MESSAGE_SIGNIFICANT_VSPACING + 1) {
            messageArraysStep = Math.round(Metrics.MESSAGE_SIGNIFICANT_VSPACING + 1 / ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * fZoomValue));
        }

        int firstVisible = fFrame.getFirstVisibleSyncMessage();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < fFrame.syncMessageCount(); i = i + messageArraysStep) {
            SyncMessage m = fFrame.getSyncMessage(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getEventOccurrence(), m);
                fNodeList.add(t);
                if (m.getY() * fZoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = fFrame.getFirstVisibleSyncMessageReturn();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < fFrame.syncMessageReturnCount(); i = i + messageArraysStep) {
            SyncMessage m = fFrame.getSyncMessageReturn(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getEventOccurrence(), m);
                fNodeList.add(t);
                if (m.getY() * fZoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = fFrame.getFirstVisibleAsyncMessage();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < fFrame.asyncMessageCount(); i = i + messageArraysStep) {
            AsyncMessage m = fFrame.getAsyncMessage(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getStartOccurrence(), m);
                fNodeList.add(t);
                t = new SDTimeEvent(m.getEndTime(), m.getEndOccurrence(), m);
                fNodeList.add(t);
                if (m.getY() * fZoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        firstVisible = fFrame.getFirstVisibleAsyncMessageReturn();
        if (firstVisible > 0) {
            firstVisible = firstVisible - 1;
        }
        for (int i = firstVisible; i < fFrame.asyncMessageReturnCount(); i = i + messageArraysStep) {
            AsyncMessageReturn m = fFrame.getAsyncMessageReturn(i);
            if (m.hasTimeInfo()) {
                SDTimeEvent t = new SDTimeEvent(m.getStartTime(), m.getStartOccurrence(), m);
                fNodeList.add(t);
                t = new SDTimeEvent(m.getEndTime(), m.getEndOccurrence(), m);
                fNodeList.add(t);
                if (m.getY() * fZoomValue > getContentsY() + getVisibleHeight()) {
                    break;
                }
            }
        }

        List<SDTimeEvent> executionOccurrencesWithTime = fFrame.getExecutionOccurrencesWithTime();
        if (executionOccurrencesWithTime != null) {
            fNodeList.addAll(executionOccurrencesWithTime);
        }

        SDTimeEvent[] temp = fNodeList.toArray(new SDTimeEvent[fNodeList.size()]);
        Arrays.sort(temp, new TimeEventComparator());
        fNodeList = Arrays.asList(temp);

        Image dbuffer = new Image(getDisplay(), getClientArea().width, getClientArea().height);
        GC gcim = new GC(dbuffer);

        for (int i = 0; i < fNodeList.size() - 1; i++) {
            SDTimeEvent m1 = fNodeList.get(i);
            SDTimeEvent m2 = fNodeList.get(i + 1);

            if ((SDViewPref.getInstance().excludeExternalTime()) && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                    continue;
                }
            }

            fMinTime = fFrame.getMinTime();
            fMaxTime = fFrame.getMaxTime();
            ITmfTimestamp minMaxdelta = fMaxTime.getDelta(fMinTime);
            double gr = (minMaxdelta.getValue()) / (double) NUMBER_STEPS;

            ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime()).getDelta(fMinTime);
            long absDelta = Math.abs(delta.getValue());

            ColorImpl color;
            if (gr != 0) {
                int colIndex = Math.round((float) (absDelta / gr));
                if (colIndex < fColors.length && colIndex > 0) {
                    color = fColors[colIndex - 1];
                } else if (colIndex <= 0) {
                    color = fColors[0];
                } else {
                    color = fColors[fColors.length - 1];
                }
            } else {
                color = fColors[0];
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
            gcim.fillRectangle(contentsToViewX(0), contentsToViewY(Math.round(y1 * fZoomValue)), 10, Math.round((y2 - y1) * fZoomValue) + 1);
            if (messageArraysStep == 1) {
                Color backupColor = gcim.getForeground();
                gcim.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                gcim.drawRectangle(contentsToViewX(0), contentsToViewY(Math.round(y1 * fZoomValue)), 9, Math.round((y2 - y1) * fZoomValue));
                gcim.setForeground(backupColor);
            }
        }
        if (getViewControl().isFocusControl() || isFocusControl()) {
            gcim.drawFocus(contentsToViewX(0), contentsToViewY(Math.round(fPrevNodeY * fZoomValue)), contentsToViewX(10), Math.round((fNextNodeY - fPrevNodeY) * fZoomValue));
        }
        try {
            gc.drawImage(dbuffer, 0, 0, getClientArea().width, getClientArea().height, 0, 0, getClientArea().width, getClientArea().height);
        } catch (Exception e) {
            Activator.getDefault().logError("Error drawing image", e); //$NON-NLS-1$
        }
        gcim.dispose();
        dbuffer.dispose();
        gc.dispose();
    }

    /**
     * Checks for focus of children.
     *
     * @param children
     *            Control to check
     * @return true if child is on focus else false
     */
    protected boolean checkFocusOnChilds(Control children) {
        if (children instanceof Composite) {
            Control[] child = ((Composite) children).getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    return true;
                }
                checkFocusOnChilds(child[i]);
            }
        }
        return false;
    }

    @Override
    public boolean isFocusControl() {
        Control[] child = getChildren();
        for (int i = 0; i < child.length; i++) {
            if (child[i].isFocusControl()) {
                return true;
            }
            checkFocusOnChilds(child[i]);
        }
        return false;
    }

    @Override
    protected void contentsMouseMoveEvent(MouseEvent event) {
        if (fTooltip != null) {
            fTooltip.hideToolTip();
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

    @Override
    protected void contentsMouseHover(MouseEvent e) {
        if (fTooltip == null) {
            fTooltip = new DrawableToolTip(this);
        }
        if (fFrame != null) {
            setFocus(0);
            for (int i = 0; i < fNodeList.size() - 1; i++) {
                SDTimeEvent m1 = fNodeList.get(i);
                SDTimeEvent m2 = fNodeList.get(i + 1);

                if ((SDViewPref.getInstance().excludeExternalTime()) && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                        continue;
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
                int m1Y = Math.round(y1 * fZoomValue);
                int m2Y = Math.round(y2 * fZoomValue);
                if ((m1Y < e.y) && (m2Y >= e.y)) {
                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
                    fTooltip.showToolTip(delta, fMinTime, fMaxTime);
                }
            }
        }
        setFocus(0);
    }

    @Override
    protected void contentsMouseExit(MouseEvent e) {
        if (fTooltip != null) {
            fTooltip.hideToolTip();
        }
    }

    @Override
    protected void contentsMouseUpEvent(MouseEvent event) {
        selectTimeDelta(event.y, 0);
        setFocus();
        super.contentsMouseUpEvent(event);
    }

    /**
     * Force the time compression bar to highlight the event occurrences between
     * the two given messages. The event occurrences are highlighted on the
     * first message's end lifeline
     *
     * @param mes1
     *            the first message
     * @param mes2
     *            the second message
     */
    public void highlightRegion(BaseMessage mes1, BaseMessage mes2) {
        BaseMessage localMes1 = mes1;
        BaseMessage localMes2 = mes2;

        if (fFrame == null) {
            return;
        }
        if (!(localMes1 instanceof ITimeRange)) {
            return;
        }
        if (!(localMes2 instanceof ITimeRange)) {
            return;
        }
        ITimeRange t1 = (ITimeRange) localMes1;
        ITimeRange t2 = (ITimeRange) localMes2;

        ITmfTimestamp time1 = t1.getStartTime();
        ITmfTimestamp time2 = t2.getStartTime();
        int event1 = localMes1.getEventOccurrence();
        int event2 = localMes2.getEventOccurrence();

        if (localMes1 instanceof AsyncMessage) {
            AsyncMessage as = (AsyncMessage) localMes1;
            time1 = as.getEndTime();
            event1 = as.getEndOccurrence();
        }
        if (localMes2 instanceof AsyncMessage) {
            AsyncMessage as = (AsyncMessage) localMes2;
            if (as.getEndOccurrence() > as.getStartOccurrence()) {
                time1 = as.getEndTime();
                event1 = as.getEndOccurrence();
            } else {
                time1 = as.getStartTime();
                event1 = as.getStartOccurrence();
            }
        }

        if (event1 > event2) {
            BaseMessage tempMes = localMes2;
            localMes2 = localMes1;
            localMes1 = tempMes;

            t1 = (ITimeRange) localMes1;
            t2 = (ITimeRange) localMes2;

            time1 = t1.getStartTime();
            time2 = t2.getStartTime();
            event1 = localMes1.getEventOccurrence();
            event2 = localMes2.getEventOccurrence();

            if (localMes1 instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) localMes1;
                time1 = as.getEndTime();
                event1 = as.getEndOccurrence();
            }
            if (localMes2 instanceof AsyncMessage) {
                AsyncMessage as = (AsyncMessage) localMes2;
                if (as.getEndOccurrence() > as.getStartOccurrence()) {
                    time1 = as.getEndTime();
                    event1 = as.getEndOccurrence();
                } else {
                    time1 = as.getStartTime();
                    event1 = as.getStartOccurrence();
                }
            }
        }

        ITmfTimestamp minMaxdelta = fMaxTime.getDelta(fMinTime);
        double gr = (minMaxdelta.getValue()) / (double) NUMBER_STEPS;

        ITmfTimestamp delta = time2.getDelta(time1).getDelta(fMinTime);
        long absDelta = Math.abs(delta.getValue());

        int colIndex = 0;
        if (gr != 0) {
            colIndex = Math.round((float) (absDelta / gr));
            if (colIndex >= fColors.length) {
                colIndex = fColors.length - 1;
            } else if (colIndex < 0) {
                colIndex = 0;
            }
        } else {
            colIndex = 0;
        }
        for (int j = 0; j < fListenerList.size(); j++) {
            ITimeCompressionListener list = fListenerList.get(j);
            if (localMes1.getEndLifeline() != null) {
                list.deltaSelected(localMes1.getEndLifeline(), event1, event2 - event1, fColors[colIndex]);
            } else if (localMes2.getStartLifeline() != null) {
                list.deltaSelected(localMes2.getStartLifeline(), event1, event2 - event1, fColors[colIndex]);
            } else {
                list.deltaSelected(localMes1.getStartLifeline(), event1, event2 - event1, fColors[colIndex]);
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

    @Override
    public void scrollBy(int x, int y) {
    }

    /**
     * Sets the zoom value.
     *
     * @param value The zoom value to set.
     */
    public void setZoom(float value) {
        fZoomValue = value;
        redraw();
    }

    /**
     * Adds a listener to the time compression listener list to be notified about selected deltas.
     *
     * @param listener The listener to add
     */
    public void addTimeCompressionListener(ITimeCompressionListener listener) {
        if (!fListenerList.contains(listener)) {
            fListenerList.add(listener);
        }
    }

    /**
     * Removes a time compression listener.
     *
     * @param listener The listener to remove.
     */
    public void removeSelectionChangedListener(ITimeCompressionListener listener) {
        fListenerList.remove(listener);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (fTooltip != null) {
            fTooltip.dispose();
        }
        super.removeDisposeListener(this);
        for (int i = 0; i < fColors.length; i++) {
            fColors[i].dispose();
        }
    }

    @Override
    protected void keyPressedEvent(KeyEvent event) {
        if (fTooltip != null) {
            fTooltip.hideToolTip();
        }
        if (!isFocusControl() || getViewControl().isFocusControl()) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    break;
                }
            }
        }
        setFocus(-1);

        boolean top = false;
        if (fNextNodeY == 0) {
            top = true;
        }
        if ((fFrame != null) && (fNextNodeY == 0)) {
            for (int i = 0; i < fNodeList.size() - 1 && i < 1; i++) {
                SDTimeEvent m1 = fNodeList.get(i);
                SDTimeEvent m2 = fNodeList.get(i + 1);
                if ((SDViewPref.getInstance().excludeExternalTime()) && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                        continue;
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
                fPrevNodeY = Math.round(y1 * fZoomValue);
                fNextNodeY = Math.round(y2 * fZoomValue);
            }
        }

        if (fLifeline != null) {
            for (int j = 0; j < fListenerList.size(); j++) {
                ITimeCompressionListener list = fListenerList.get(j);
                list.deltaSelected(fLifeline, fLifelineStart, fLifelineNumEvents, fLifelineColor);
            }
        }

        if (event.keyCode == SWT.ARROW_DOWN) {
            if (!top) {
                selectTimeDelta(fNextNodeY + 1, 1);
            } else {
                selectTimeDelta(fPrevNodeY + 1, 1);
            }
            setFocus(1);
        } else if (event.keyCode == SWT.ARROW_UP) {
            selectTimeDelta(fPrevNodeY - 1, 2);
            setFocus(1);
        } else if (event.keyCode == SWT.ARROW_RIGHT) {
            selectTimeDelta(fPrevNodeY, 1);
            setFocus(1);
        }
        super.keyPressedEvent(event);
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
        if (fFrame != null) {
            for (int i = 0; i < fNodeList.size() - 1; i++) {
                SDTimeEvent m1 = fNodeList.get(i);
                SDTimeEvent m2 = fNodeList.get(i + 1);
                if ((SDViewPref.getInstance().excludeExternalTime()) && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                        continue;
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
                int m1Y = Math.round(y1 * fZoomValue);
                int m2Y = Math.round(y2 * fZoomValue);

                if ((m1Y < dy) && (m2Y > dy) || (!done && m2Y > dy && direction == 1 && lastM1 != null) || (!done && m1Y > dy && direction == 2 && lastM1 != null)) {
                    if (m1Y > dy && direction == 2) {
                        m1 = lastM1;
                        m2 = lastM2;
                        m1Y = lastY1;
                        m2Y = lastY2;
                    }
                    done = true;
                    fPrevNodeY = m1Y;
                    fNextNodeY = m2Y;
                    ITmfTimestamp minMaxdelta = fMaxTime.getDelta(fMinTime);
                    double gr = (minMaxdelta.getValue()) / (double) NUMBER_STEPS;

                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime()).getDelta(fMinTime);
                    long absDelta = Math.abs(delta.getValue());

                    int colIndex = 0;
                    if (gr != 0) {
                        colIndex = Math.round((float) (absDelta / gr));
                        if (colIndex >= fColors.length) {
                            colIndex = fColors.length - 1;
                        } else if (colIndex < 0) {
                            colIndex = 0;
                        }
                    } else {
                        colIndex = 0;
                    }
                    if (m1.getGraphNode() instanceof BaseMessage) {
                        BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                        if (mes1.getEndLifeline() != null) {
                            fLifeline = mes1.getEndLifeline();
                            fLifelineStart = m1.getEvent();
                            fLifelineNumEvents = m2.getEvent() - m1.getEvent();
                            fLifelineColor = fColors[colIndex];
                        } else if (m2.getGraphNode() instanceof BaseMessage && ((BaseMessage) m2.getGraphNode()).getStartLifeline() != null) {
                            fLifeline = ((BaseMessage) m2.getGraphNode()).getStartLifeline();
                            fLifelineStart = m1.getEvent();
                            fLifelineNumEvents = m2.getEvent() - m1.getEvent();
                            fLifelineColor = fColors[colIndex];
                        } else {
                            fLifeline = mes1.getStartLifeline();
                            fLifelineStart = m1.getEvent();
                            fLifelineNumEvents = m2.getEvent() - m1.getEvent();
                            fLifelineColor = fColors[colIndex];
                        }
                    } else if (m1.getGraphNode() instanceof ExecutionOccurrence) {
                        if (m2.getGraphNode() instanceof ExecutionOccurrence) {
                            ExecutionOccurrence eo = (ExecutionOccurrence) m2.getGraphNode();
                            fLifeline = eo.getLifeline();
                            fLifelineStart = m1.getEvent();
                            fLifelineNumEvents = m2.getEvent() - m1.getEvent();
                            fLifelineColor = fColors[colIndex];
                        } else {
                            ExecutionOccurrence eo = (ExecutionOccurrence) m1.getGraphNode();
                            fLifeline = eo.getLifeline();
                            fLifelineStart = m1.getEvent();
                            fLifelineNumEvents = m2.getEvent() - m1.getEvent();
                            fLifelineColor = fColors[colIndex];
                        }
                    }
                    for (int j = 0; j < fListenerList.size(); j++) {
                        ITimeCompressionListener list = fListenerList.get(j);
                        list.deltaSelected(fLifeline, fLifelineStart, fLifelineNumEvents, fLifelineColor);
                    }
                    break;
                }
                lastM1 = m1;
                lastM2 = m2;
                lastY1 = m1Y;
                lastY2 = m2Y;
            }
        }
    }

    /**
     * Creates a fake tool tip.
     */
    protected void createFakeTooltip() {
        if (fTooltip == null) {
            fTooltip = new DrawableToolTip(this);
        }

        if (fFrame != null) {
            setFocus(0);
            for (int i = 0; i < fNodeList.size() - 1; i++) {
                SDTimeEvent m1 = fNodeList.get(i);
                SDTimeEvent m2 = fNodeList.get(i + 1);

                if ((SDViewPref.getInstance().excludeExternalTime()) && ((m1.getGraphNode() instanceof BaseMessage) && (m2.getGraphNode() instanceof BaseMessage))) {
                    BaseMessage mes1 = (BaseMessage) m1.getGraphNode();
                    BaseMessage mes2 = (BaseMessage) m2.getGraphNode();
                    if ((mes2.getStartLifeline() == null) || (mes1.getEndLifeline() == null)) {
                        continue;
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
                int m1Y = Math.round(y1 * fZoomValue);
                int m2Y = Math.round(y2 * fZoomValue);
                if ((m1Y < fPrevNodeY + 1) && (m2Y >= fPrevNodeY + 1)) {
                    ITmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
                    fTooltip.showToolTip(delta, fMinTime, fMaxTime);
                    fTooltip.hideToolTip();
                }
            }
        }
    }

    /**
     * Traverse Listener implementation.
     */
    protected static class LocalTraverseListener implements TraverseListener {
        @Override
        public void keyTraversed(TraverseEvent e) {
            if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                e.doit = true;
            }
        }
    }
}
