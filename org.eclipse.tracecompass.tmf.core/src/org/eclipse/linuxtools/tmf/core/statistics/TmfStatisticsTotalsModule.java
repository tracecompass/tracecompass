/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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

import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The analysis module building the "totals" statistics state system.
 *
 * It is not in the extension point (and as such, not registered in the
 * TmfAnalysisManager), as it is being handled by the TmfStatisticsModule.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class TmfStatisticsTotalsModule extends TmfStateSystemAnalysisModule {

    /**
     * The ID of this analysis module (which is also the ID of the state system)
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.statistics.totals"; //$NON-NLS-1$

    private static final String NAME = "TMF Statistics, event totals"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public TmfStatisticsTotalsModule() {
        super();
        setId(ID);
        setName(NAME);
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new StatsProviderTotals(getTrace());
    }

    @Override
    protected String getSsFileName() {
        return "statistics-totals.ht"; //$NON-NLS-1$
    }


    /**
     * The state provider for traces statistics that use TmfStateStatistics. It
     * should work with any trace type for which we can use the state system.
     *
     * Only one attribute will be stored, containing the total of events seen so
     * far. The resulting attribute tree will look like this:
     *
     * <pre>
     * (root)
     *   \-- total
     * </pre>
     *
     * @author Alexandre Montplaisir
     * @version 1.0
     */
    class StatsProviderTotals extends AbstractTmfStateProvider {

        /**
         * Version number of this input handler. Please bump this if you modify the
         * contents of the generated state history in some way.
         */
        private static final int VERSION = 2;

        /**
         * Constructor
        *
         * @param trace
         *            The trace for which we build this state system
         */
        public StatsProviderTotals(ITmfTrace trace) {
            super(trace, ITmfEvent.class , NAME);
        }

        @Override
        public int getVersion() {
            return VERSION;
        }

        @Override
        public StatsProviderTotals getNewInstance() {
            return new StatsProviderTotals(this.getTrace());
        }

        @Override
        protected void eventHandle(ITmfEvent event) {
            /* Do not count lost events in the total */
            if (event instanceof ITmfLostEvent) {
                return;
            }

            /* Since this can be used for any trace types, normalize all the
             * timestamp values to nanoseconds. */
            final long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

            try {
                /* Total number of events */
                int quark = ss.getQuarkAbsoluteAndAdd(Attributes.TOTAL);
                ss.incrementAttribute(ts, quark);

            } catch (StateValueTypeException | TimeRangeException | AttributeNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
