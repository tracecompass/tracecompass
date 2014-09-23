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

package org.eclipse.linuxtools.internal.gdbtrace.ui.views.events;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event table column definition for GDB traces.
 *
 * @author Alexandre Montplaisir
 */
public class GdbEventTableColumns implements ITmfEventTableColumns {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    static final @NonNull Collection<TmfEventTableColumn> GDB_COLUMNS = ImmutableList.of(
            new GdbTraceFrameColumn(),
            new GdbTracepointColumn(),
            new GdbFileColumn()
            );

    private static class GdbTraceFrameColumn extends TmfEventTableFieldColumn {
        public GdbTraceFrameColumn() {
            super(GdbTraceEventContent.TRACE_FRAME);
        }
    }

    private static class GdbTracepointColumn extends TmfEventTableFieldColumn {
        public GdbTracepointColumn() {
            super(GdbTraceEventContent.TRACEPOINT);
        }
    }

    private static class GdbFileColumn extends TmfEventTableColumn {

        public GdbFileColumn() {
            super("File"); //$NON-NLS-1$
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
    // ITmfEventTableColumns
    // ------------------------------------------------------------------------

    @Override
    public Collection<? extends TmfEventTableColumn> getEventTableColumns() {
        return GDB_COLUMNS;
    }
}
