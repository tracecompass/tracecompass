/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event implementation specific to the FlameGraph View (it represents a
 * function in a certain depth)
 *
 * @author Sonia Farrah
 *
 */
public class FlamegraphEvent extends TimeEvent {

    private final Object fSymbol;
    private final int fNbCalls;
    private final long fSelfTime;

    /**
     * Constructor
     *
     * @param source
     *            The Entry
     * @param beginTime
     *            The event's begin time
     * @param totalTime
     *            The event's total time
     * @param value
     *            The event's value
     * @param symbol
     *            The event's address or name
     * @param nbCalls
     *            The event's number of calls
     * @param selfTime
     *            The event's self time
     */
    public FlamegraphEvent(ITimeGraphEntry source, long beginTime, long totalTime, int value, Object symbol, int nbCalls, long selfTime) {
        super(source, beginTime, totalTime, value);
        fSymbol = symbol;
        fNbCalls = nbCalls;
        fSelfTime = selfTime;
    }

    /**
     * The event's name or address
     *
     * @return The event's name or address
     */
    public Object getSymbol() {
        return fSymbol;
    }

    /**
     * The event's number of calls
     *
     * @return The event's number of a calls
     */
    public int getNbCalls() {
        return fNbCalls;
    }

    /**
     * The self time of an event
     *
     * @return The self time
     */
    public long getSelfTime() {
        return fSelfTime;
    }
}
