/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatistics.Attributes;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The state provider for traces statistics. It should work with any type of
 * trace TMF can handle.
 *
 * The resulting attribute tree will look like this:
 *
 * <root>
 *   \-- event_types
 *        |-- <event name 1>
 *        |-- <event name 2>
 *        |-- <event name 3>
 *       ...
 *
 * And each <event name>'s value will be an integer, representing how many times
 * this particular event type has been seen in the trace so far.
 *
 * @author Alexandre Montplaisir
 * @version 1.0
 */
class StatsStateProvider extends AbstractStateChangeInput {

    /* Commonly-used attributes */
    private int typeAttribute = -1;

    /**
     * Constructor
    *
     * @param trace
     *            The trace for which we build this state system
     */
    public StatsStateProvider(ITmfTrace trace) {
        super(trace, ITmfEvent.class ,"TMF Statistics"); //$NON-NLS-1$
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        super.assignTargetStateSystem(ssb);

        /* Setup common locations */
        typeAttribute = ss.getQuarkAbsoluteAndAdd(Attributes.EVENT_TYPES);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        int quark;

        /* Since this can be used for any trace types, normalize all the
         * timestamp values to nanoseconds. */
        final long ts = event.getTimestamp().normalize(0, -9).getValue();

        final String eventName = event.getType().getName();

        try {

            /* Number of events of each type, globally */
            quark = ss.getQuarkRelativeAndAdd(typeAttribute, eventName);
            ss.incrementAttribute(ts, quark);

//            /* Number of events per CPU */
//            quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATISTICS, Attributes.EVENT_TYPES, eventName);
//            ss.incrementAttribute(ts, quark);
//
//            /* Number of events per process */
//            quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATISTICS, Attributes.EVENT_TYPES, eventName);
//            ss.incrementAttribute(ts, quark);

        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        }
    }
}
