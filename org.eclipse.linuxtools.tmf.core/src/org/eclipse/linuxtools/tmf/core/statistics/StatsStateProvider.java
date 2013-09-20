/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Fix javadoc
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The state provider for traces statistics that use TmfStateStatistics. It
 * should work with any trace type for which we can use the state system.
 *
 * The resulting attribute tree will look like this:
 *<pre>
 * (root)
 *   |-- total
 *   \-- event_types
 *        |-- (event name 1)
 *        |-- (event name 2)
 *        |-- (event name 3)
 *       ...
 *</pre>
 * And each (event name)'s value will be an integer, representing how many times
 * this particular event type has been seen in the trace so far.
 *
 * @author Alexandre Montplaisir
 * @version 1.0
 */
class StatsStateProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this input handler. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

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
    public int getVersion() {
        return VERSION;
    }

    @Override
    public StatsStateProvider getNewInstance() {
        return new StatsStateProvider(this.getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        int quark;

        /* Since this can be used for any trace types, normalize all the
         * timestamp values to nanoseconds. */
        final long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        final String eventName = event.getType().getName();

        try {
            /* Special handling for lost events */
            if (event instanceof ITmfLostEvent) {
                ITmfLostEvent le = (ITmfLostEvent) event;
                quark = ss.getQuarkAbsoluteAndAdd(Attributes.EVENT_TYPES, eventName);

                int curVal = ss.queryOngoingState(quark).unboxInt();
                if (curVal == -1) { curVal = 0; }

                TmfStateValue value = TmfStateValue.newValueInt((int) (curVal + le.getNbLostEvents()));
                ss.modifyAttribute(ts, value, quark);
                return;
            }

            /* Total number of events */
            quark = ss.getQuarkAbsoluteAndAdd(Attributes.TOTAL);
            ss.incrementAttribute(ts, quark);

            /* Number of events of each type, globally */
            quark = ss.getQuarkAbsoluteAndAdd(Attributes.EVENT_TYPES, eventName);
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
