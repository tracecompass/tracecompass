/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Added lost events attribute
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfLostEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The analysis module building the "event types" statistics state system.
 *
 * It is not in the extension point (and as such, not registered in the
 * TmfAnalysisManager), as it is being handled by the TmfStatisticsModule.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStatisticsEventTypesModule extends TmfStateSystemAnalysisModule {

    /**
     * The ID of this analysis module (which is also the ID of the state system)
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.statistics.types"; //$NON-NLS-1$

    private static final @NonNull String NAME = "TMF Statistics, events per type"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public TmfStatisticsEventTypesModule() {
        super();
        setId(ID);
        setName(NAME);
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new StatsProviderEventTypes(checkNotNull(getTrace()));
    }

    @Override
    protected String getSsFileName() {
        return "statistics-types.ht"; //$NON-NLS-1$
    }


    /**
     * The state provider for traces statistics that use TmfStateStatistics. It
     * should work with any trace type for which we can use the state system.
     *
     * It will store number of events seen, per event types. The resulting
     * attribute tree will look like this:
     *
     * <pre>
     * (root)
     *   |-- event_types
     *   |    |-- (event name 1)
     *   |    |-- (event name 2)
     *   |    |-- (event name 3)
     *   |   ...
     *   \-- lost_events
     * </pre>
     *
     * Each (event name)'s value will be an integer, representing how many times
     * this particular event type has been seen in the trace so far.
     *
     * The value of the lost_events attribute will be a long, representing the
     * latest end time of any current or previous lost event time range, in
     * nanoseconds. If the value at a specific time 't' is greater than 't',
     * then there is at least one lost event time range that overlaps time 't'.
     *
     * @author Alexandre Montplaisir
     * @version 1.0
     */
    class StatsProviderEventTypes extends AbstractTmfStateProvider {

        /**
         * Version number of this input handler. Please bump this if you modify the
         * contents of the generated state history in some way.
         */
        private static final int VERSION = 3;

        /**
         * Constructor
         *
         * @param trace
         *            The trace for which we build this state system
         */
        public StatsProviderEventTypes(@NonNull ITmfTrace trace) {
            super(trace ,"TMF Statistics, events per type"); //$NON-NLS-1$
        }

        @Override
        public int getVersion() {
            return VERSION;
        }

        @Override
        public StatsProviderEventTypes getNewInstance() {
            return new StatsProviderEventTypes(this.getTrace());
        }

        @Override
        protected void eventHandle(ITmfEvent event) {
            ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
            int quark;

            /* Since this can be used for any trace types, normalize all the
             * timestamp values to nanoseconds. */
            final long ts = event.getTimestamp().toNanos();

            final String eventName = event.getName();

            try {
                /* Special handling for lost events */
                if (event instanceof ITmfLostEvent) {
                    ITmfLostEvent le = (ITmfLostEvent) event;
                    quark = ss.getQuarkAbsoluteAndAdd(Attributes.EVENT_TYPES, eventName);

                    int curVal = ss.queryOngoingState(quark).unboxInt();
                    if (curVal == -1) {
                        curVal = 0;
                    }

                    ss.modifyAttribute(ts, (int) (curVal + le.getNbLostEvents()), quark);

                    long lostEventsStartTime = le.getTimeRange().getStartTime().toNanos();
                    long lostEventsEndTime = le.getTimeRange().getEndTime().toNanos();
                    int lostEventsQuark = ss.getQuarkAbsoluteAndAdd(Attributes.LOST_EVENTS);
                    ITmfStateValue currentLostEventsEndTime = ss.queryOngoingState(lostEventsQuark);
                    if (currentLostEventsEndTime.isNull() || currentLostEventsEndTime.unboxLong() < lostEventsStartTime) {
                        ss.modifyAttribute(lostEventsStartTime, lostEventsEndTime, lostEventsQuark);
                    } else if (currentLostEventsEndTime.unboxLong() < lostEventsEndTime) {
                        ss.updateOngoingState(TmfStateValue.newValueLong(lostEventsEndTime), lostEventsQuark);
                    }
                    return;
                }

                /* Number of events of each type, globally */
                quark = ss.getQuarkAbsoluteAndAdd(Attributes.EVENT_TYPES, eventName);
                StateSystemBuilderUtils.incrementAttributeInt(ss, ts, quark, 1);

//                /* Number of events per CPU */
//                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATISTICS, Attributes.EVENT_TYPES, eventName);
//                ss.incrementAttribute(ts, quark);
    //
//                /* Number of events per process */
//                quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATISTICS, Attributes.EVENT_TYPES, eventName);
//                ss.incrementAttribute(ts, quark);

            } catch (AttributeNotFoundException e) {
                Activator.logError("Get attribute not found exception ", e);  //$NON-NLS-1$
            }
        }
    }
}
