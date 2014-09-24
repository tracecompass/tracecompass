/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Update for TmfEventTableColumn
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event table column definition for Custom {Text|XML} traces.
 *
 * Since this definition will be different for every single custom trace, this
 * does not work the same as with {@link ITmfEventTableColumns}.
 *
 * Instead, one has to call {@link #generateColumns(CustomTraceDefinition)} with
 * the CustomTraceDefinition of the the particular trace to display. Then the
 * returned collection can be passed to the constructor
 * {@link TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, Collection)}
 * as usual.
 *
 * @author Alexandre Montplaisir
 */
public class CustomEventTableColumns {

    /**
     * Column for custom events, which uses an integer ID to represent each
     * column.
     */
    private static final class CustomEventTableColumn extends TmfEventTableColumn {

        private final int fIndex;

        /**
         * Constructor
         *
         * @param name
         *            The name (title) of this column
         * @param idx
         *            The index of this column, which should be the index of the
         *            field in the event's content to display.
         */
        public CustomEventTableColumn(@NonNull String name, int idx) {
            super(name);
            fIndex = idx;
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (event instanceof CustomEvent) {
                String ret = ((CustomEvent) event).getEventString(fIndex);
                return (ret == null ? EMPTY_STRING : ret);
            }
            return EMPTY_STRING;
        }

        @Override
        public String getFilterFieldId() {
            return getHeaderName();
        }
    }

    /**
     * Get the event table columns for a given trace definition
     *
     * @param definition The {@link CustomTraceDefinition} of the trace for which you want the columns
     * @return The set of columns for the given trace.
     */
    public static Collection<CustomEventTableColumn> generateColumns(CustomTraceDefinition definition) {
        ImmutableList.Builder<CustomEventTableColumn> builder = new ImmutableList.Builder<>();
        List<OutputColumn> outputs = definition.outputs;
        for (int i = 0; i < outputs.size(); i++) {
            String name = outputs.get(i).name;
            if (name != null) {
                builder.add(new CustomEventTableColumn(name, i));
            }
        }
        return builder.build();
    }
}
