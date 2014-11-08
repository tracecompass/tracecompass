/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.viewers.events;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event table columns for LTTng 2.x kernel traces
 */
public class LttngEventTableColumns implements ITmfEventTableColumns {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    private static final @NonNull String CHANNEL_HEADER = Messages.EventsTable_channelColumn;

    @SuppressWarnings("null")
    private static final @NonNull Collection<TmfEventTableColumn> LTTNG_COLUMNS =
            ImmutableList.<TmfEventTableColumn> of(
                    new TmfEventTableColumn(ITmfEventAspect.BaseAspects.TIMESTAMP),
                    new TmfEventTableColumn(new LttngChannelAspect()),
                    new TmfEventTableColumn(ITmfEventAspect.BaseAspects.EVENT_TYPE),
                    new TmfEventTableColumn(ITmfEventAspect.BaseAspects.CONTENTS));

    private static class LttngChannelAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return CHANNEL_HEADER;
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof CtfTmfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((CtfTmfEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    @Override
    public Collection<? extends TmfEventTableColumn> getEventTableColumns() {
        return LTTNG_COLUMNS;
    }
}
