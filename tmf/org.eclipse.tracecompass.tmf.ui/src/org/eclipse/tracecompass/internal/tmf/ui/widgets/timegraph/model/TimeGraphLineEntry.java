/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * An entry for use in the time graph views
 *
 * @author Matthew Khouzam
 */
public class TimeGraphLineEntry extends TimeGraphEntry {

    /**
     * Constructor
     *
     * @param name
     *            the name of the entry
     * @param startTime
     *            the start time
     * @param endTime
     *            the end time
     */
    public TimeGraphLineEntry(String name, long startTime, long endTime) {
        super(name, startTime, endTime);
    }

    /**
     * Constructor
     *
     * @param model
     *            model
     *
     * TODO: change to interface
     */
    public TimeGraphLineEntry(@NonNull TimeGraphEntryModel model) {
        super(model);
    }

    @Override
    public void addEvent(ITimeEvent event) {
        if (isValidEvent(event)) {
            super.addEvent(event);
        }
    }

    @Override
    public void addZoomedEvent(ITimeEvent event) {
        if (isValidEvent(event)) {
            super.addZoomedEvent(event);
        }
    }

    @Override
    public void setEventList(List<ITimeEvent> eventList) {
        sanitizeList(eventList, super::setEventList);
    }

    @Override
    public void updateZoomedEvent(ITimeEvent event) {
        if (isValidEvent(event)) {
            super.updateZoomedEvent(event);
        }
    }

    @Override
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        sanitizeList(eventList, super::setZoomedEventList);
    }

    @Override
    public DisplayStyle getStyle() {
        return DisplayStyle.LINE;
    }

    private static boolean isValidEvent(ITimeEvent event) {
        return (event instanceof TimeLineEvent);
    }

    private static void sanitizeList(List<ITimeEvent> sourceList, Consumer<List<ITimeEvent>> listConsumer) {
        if (sourceList != null) {
            // Sets a filtered list
            List<ITimeEvent> events = new ArrayList<>();
            for (ITimeEvent event : sourceList) {
                if (isValidEvent(event)) {
                    events.add(event);
                }
            }
            listConsumer.accept(events);
        } else {
            listConsumer.accept(null);
        }
    }

}
