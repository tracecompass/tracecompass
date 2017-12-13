/*****************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Vitaly A. Provodin, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;

/**
 * Handler for the tool tips in the generic time graph view.
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphTooltipHandler extends TmfAbstractToolTipHandler {

    private static final String MIN_STRING = "< 0.01%"; //$NON-NLS-1$

    private static final double MIN_RATIO = 0.0001;

    private static final String MAX_STRING = "> 1000%"; //$NON-NLS-1$

    private static final int MAX_RATIO = 10;

    private static final int HOVER_MAX_DIST = 10;

    private ITimeDataProvider fTimeDataProvider;
    private ITimeGraphPresentationProvider fTimeGraphProvider = null;

    /**
     * Standard constructor
     *
     * @param graphProv
     *            The presentation provider
     * @param timeProv
     *            The time provider
     */
    public TimeGraphTooltipHandler(ITimeGraphPresentationProvider graphProv,
            ITimeDataProvider timeProv) {

        this.fTimeGraphProvider = graphProv;
        this.fTimeDataProvider = timeProv;
    }

    /**
     * Set the time data provider
     *
     * @param timeDataProvider
     *            The time data provider
     */
    public void setTimeProvider(ITimeDataProvider timeDataProvider) {
        fTimeDataProvider = timeDataProvider;
    }

    @Override
    protected void fill(Control control, MouseEvent event, Point pt) {
        TimeGraphControl timeGraphControl = (TimeGraphControl) control;
        if ((event.stateMask & SWT.MODIFIER_MASK) != SWT.SHIFT) {
            ILinkEvent linkEvent = timeGraphControl.getArrow(pt);
            if (linkEvent != null) {
                fillValues(linkEvent);
            }
        }
        if (getTipComposite().getChildren().length == 0) {
            ITimeGraphEntry entry = timeGraphControl.getEntry(pt);
            fillValues(pt, timeGraphControl, entry);
        }
    }

    private void fillValues(Point pt, TimeGraphControl timeGraphControl, ITimeGraphEntry entry) {
        if (entry == null) {
            return;
        }
        if (entry.hasTimeEvents()) {
            long currPixelTime = timeGraphControl.getTimeAtX(pt.x);
            long nextPixelTime = timeGraphControl.getTimeAtX(pt.x + 1);
            if (nextPixelTime == currPixelTime) {
                nextPixelTime++;
            }
            ITimeEvent currEvent = Utils.findEvent(entry, currPixelTime, 0);
            ITimeEvent nextEvent = Utils.findEvent(entry, currPixelTime, 1);

            /*
             * if there is no current event at the start of the current
             * pixel range, or if the current event starts before the
             * current pixel range, use the next event as long as it
             * starts within the current pixel range
             */
            if ((currEvent == null || currEvent.getTime() < currPixelTime) &&
                    (nextEvent != null && nextEvent.getTime() < nextPixelTime)) {
                currEvent = nextEvent;
                currPixelTime = nextEvent.getTime();
            }

            /*
             * if there is still no current event, use the closest
             * between the next and previous event, as long as they are
             * within a distance threshold
             */
            if (currEvent == null || currEvent instanceof NullTimeEvent) {
                int nextDelta = Integer.MAX_VALUE;
                int prevDelta = Integer.MAX_VALUE;
                long nextTime = 0;
                long prevTime = 0;
                if (nextEvent != null && !(nextEvent instanceof NullTimeEvent)) {
                    nextTime = nextEvent.getTime();
                    nextDelta = Math.abs(timeGraphControl.getXForTime(nextTime) - pt.x);
                }
                ITimeEvent prevEvent = Utils.findEvent(entry, currPixelTime, -1);
                if (prevEvent != null && !(prevEvent instanceof NullTimeEvent)) {
                    prevTime = prevEvent.getTime() + prevEvent.getDuration() - 1;
                    prevDelta = Math.abs(pt.x - timeGraphControl.getXForTime(prevTime));
                }
                if (nextDelta < HOVER_MAX_DIST && nextDelta <= prevDelta) {
                    currEvent = nextEvent;
                    currPixelTime = nextTime;
                } else if (prevDelta < HOVER_MAX_DIST) {
                    currEvent = prevEvent;
                    currPixelTime = prevTime;
                }
            }

            // state name
            String stateTypeName = fTimeGraphProvider.getStateTypeName(entry);
            String entryName = entry.getName();
            if (stateTypeName == null) {
                stateTypeName = fTimeGraphProvider.getStateTypeName();
            }

            if (!entryName.isEmpty()) {
                addItem(stateTypeName, entry.getName());
            }

            if (currEvent == null || currEvent instanceof NullTimeEvent) {
                return;
            }

            // state
            String state = fTimeGraphProvider.getEventName(currEvent);
            if (state != null) {
                addItem(Messages.TmfTimeTipHandler_TRACE_STATE, state);
            }

            // This block receives a list of <String, String> values to
            // be added to the tip table
            Map<String, String> eventAddOns = fTimeGraphProvider.getEventHoverToolTipInfo(currEvent, currPixelTime);
            if (eventAddOns != null) {
                for (Entry<String, String> eventAddOn : eventAddOns.entrySet()) {
                    addItem(eventAddOn.getKey(), eventAddOn.getValue());
                }
            }
            if (fTimeGraphProvider.displayTimesInTooltip()) {
                long eventStartTime = -1;
                long eventDuration = -1;
                long eventEndTime = -1;

                eventStartTime = currEvent.getTime();
                eventDuration = currEvent.getDuration();
                if (eventDuration < 0 && nextEvent != null) {
                    eventEndTime = nextEvent.getTime();
                    eventDuration = eventEndTime - eventStartTime;
                } else {
                    eventEndTime = eventStartTime + eventDuration;
                }

                Resolution res = Resolution.NANOSEC;
                TimeFormat tf = fTimeDataProvider.getTimeFormat2();
                String startTime = "?"; //$NON-NLS-1$
                String duration = "?"; //$NON-NLS-1$
                String endTime = "?"; //$NON-NLS-1$
                if (fTimeDataProvider instanceof ITimeDataProviderConverter) {
                    ITimeDataProviderConverter tdp = (ITimeDataProviderConverter) fTimeDataProvider;
                    if (eventStartTime > -1) {
                        eventStartTime = tdp.convertTime(eventStartTime);
                        startTime = FormatTimeUtils.formatTime(eventStartTime, tf, res);
                    }
                    if (eventEndTime > -1) {
                        eventEndTime = tdp.convertTime(eventEndTime);
                        endTime = FormatTimeUtils.formatTime(eventEndTime, tf, res);
                    }
                    if (eventDuration > -1) {
                        duration = FormatTimeUtils.formatDelta(eventEndTime - eventStartTime, tf, res);
                    }
                } else {
                    if (eventStartTime > -1) {
                        startTime = FormatTimeUtils.formatTime(eventStartTime, tf, res);
                    }
                    if (eventEndTime > -1) {
                        endTime = FormatTimeUtils.formatTime(eventEndTime, tf, res);
                    }
                    if (eventDuration > -1) {
                        duration = FormatTimeUtils.formatDelta(eventDuration, tf, res);
                    }
                }
                if (tf == TimeFormat.CALENDAR) {
                    addItem(Messages.TmfTimeTipHandler_TRACE_DATE,
                            eventStartTime > -1 ? FormatTimeUtils.formatDate(eventStartTime) : "?"); //$NON-NLS-1$
                }
                if (eventDuration > 0) {
                    addItem(Messages.TmfTimeTipHandler_TRACE_START_TIME, startTime);
                    addItem(Messages.TmfTimeTipHandler_TRACE_STOP_TIME, endTime);
                } else {
                    addItem(Messages.TmfTimeTipHandler_TRACE_EVENT_TIME, startTime);
                }

                if (eventDuration > 0) {
                    addItem(Messages.TmfTimeTipHandler_DURATION, duration);
                    long begin = fTimeDataProvider.getSelectionBegin();
                    long end = fTimeDataProvider.getSelectionEnd();
                    final long delta = Math.abs(end - begin);
                    final double durationRatio = (double) eventDuration / (double) delta;
                    if (delta > 0) {
                        String percentage;
                        if (durationRatio > MAX_RATIO) {
                            percentage = MAX_STRING;
                        } else if (durationRatio < MIN_RATIO) {
                            percentage = MIN_STRING;
                        } else {
                            percentage = String.format("%,.2f%%", durationRatio * 100.0); //$NON-NLS-1$
                        }

                        addItem(Messages.TmfTimeTipHandler_PERCENT_OF_SELECTION, percentage);
                    }
                }
            }
        }
    }

    private void fillValues(ILinkEvent linkEvent) {
        addItem(Messages.TmfTimeTipHandler_LINK_SOURCE, linkEvent.getEntry().getName());
        addItem(Messages.TmfTimeTipHandler_LINK_TARGET, linkEvent.getDestinationEntry().getName());

        // This block receives a list of <String, String> values to be
        // added to the tip table
        Map<String, String> eventAddOns = fTimeGraphProvider.getEventHoverToolTipInfo(linkEvent);
        if (eventAddOns != null) {
            for (Entry<String, String> eventAddOn : eventAddOns.entrySet()) {
                addItem(eventAddOn.getKey(), eventAddOn.getValue());
            }
        }
        if (fTimeGraphProvider.displayTimesInTooltip()) {
            long sourceTime = linkEvent.getTime();
            long duration = linkEvent.getDuration();
            long targetTime = sourceTime + duration;
            if (fTimeDataProvider instanceof ITimeDataProviderConverter) {
                ITimeDataProviderConverter tdp = (ITimeDataProviderConverter) fTimeDataProvider;
                sourceTime = tdp.convertTime(sourceTime);
                targetTime = tdp.convertTime(targetTime);
                duration = targetTime - sourceTime;
            }
            Resolution res = Resolution.NANOSEC;
            TimeFormat tf = fTimeDataProvider.getTimeFormat2();
            if (tf == TimeFormat.CALENDAR) {
                addItem(Messages.TmfTimeTipHandler_TRACE_DATE, FormatTimeUtils.formatDate(sourceTime));
            }
            if (duration > 0) {
                addItem(Messages.TmfTimeTipHandler_LINK_SOURCE_TIME, FormatTimeUtils.formatTime(sourceTime, tf, res));
                addItem(Messages.TmfTimeTipHandler_LINK_TARGET_TIME, FormatTimeUtils.formatTime(targetTime, tf, res));
                addItem(Messages.TmfTimeTipHandler_DURATION, FormatTimeUtils.formatDelta(duration, tf, res));
            } else {
                addItem(Messages.TmfTimeTipHandler_LINK_TIME, FormatTimeUtils.formatTime(sourceTime, tf, res));
            }
        }
    }
}
