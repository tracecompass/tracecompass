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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.events;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;

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
                    TmfEventTableColumn.BaseColumns.TIMESTAMP,
                    new LttngChannelColumn(),
                    TmfEventTableColumn.BaseColumns.EVENT_TYPE,
                    TmfEventTableColumn.BaseColumns.CONTENTS);

    private static class LttngChannelColumn extends TmfEventTableColumn {

        public LttngChannelColumn() {
            super(CHANNEL_HEADER);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
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
