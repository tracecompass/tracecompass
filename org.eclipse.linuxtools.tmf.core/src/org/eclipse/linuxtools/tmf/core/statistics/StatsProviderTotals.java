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
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

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
        super(trace, ITmfEvent.class ,"TMF Statistics, event totals"); //$NON-NLS-1$
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

        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        }
    }
}
