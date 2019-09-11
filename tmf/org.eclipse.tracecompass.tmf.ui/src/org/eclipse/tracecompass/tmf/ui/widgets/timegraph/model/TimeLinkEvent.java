/*******************************************************************************
 * Copyright (c) 2013, 2019 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;

/**
 * TimeEvent implementation for events that do not involve only one entry, they
 * have a source entry and destination entry
 */
public class TimeLinkEvent extends TimeEvent implements ILinkEvent {

    /** TimeGraphEntry matching the destination this time event */
    private ITimeGraphEntry fDestEntry;

    /**
     * Standard constructor
     *
     * @param src
     *            The source entry of this event
     * @param dst
     *            The destination entry of this event
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of the event
     */
    public TimeLinkEvent(ITimeGraphEntry src, ITimeGraphEntry dst, long time, long duration) {
        super(src, time, duration);
        fDestEntry = dst;
    }

    /**
     * Constructor
     *
     * @param src
     *            The source entry of this event
     * @param dst
     *            The destination entry of this event
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     * @param value
     *            The status assigned to the event
     */
    public TimeLinkEvent(ITimeGraphEntry src, ITimeGraphEntry dst, long time, long duration,
            int value) {
        super(src, time, duration, value);
        fDestEntry = dst;
    }

    /**
     * Constructor
     *
     * @param arrowModel
     *            {@link ITimeGraphArrow} that represents this time link event
     * @param src
     *            The source entry of this event
     * @param dst
     *            The destination entry of this event
     * @since 5.1
     */
    public TimeLinkEvent(ITimeGraphArrow arrowModel, ITimeGraphEntry src, ITimeGraphEntry dst) {
        super(src, new TimeGraphState(arrowModel.getStartTime(), arrowModel.getDuration(), null, arrowModel.getStyle()));
        fDestEntry = dst;
    }

    @Override
    public ITimeGraphEntry getDestinationEntry() {
        return fDestEntry;
    }

}
