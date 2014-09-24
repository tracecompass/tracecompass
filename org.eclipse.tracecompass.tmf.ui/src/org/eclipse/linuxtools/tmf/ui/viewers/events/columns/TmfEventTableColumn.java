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
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns.TmfContentsColumn;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns.TmfReferenceColumn;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns.TmfSourceColumn;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns.TmfTimestampColumn;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns.TmfTypeColumn;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * A column in the
 * {@link org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable}. In
 * addition to ones provided by default, trace types can extend this class to
 * create additional columns specific to their events.
 *
 * Those additional columns can then be passed to the constructor
 * {@link org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, java.util.Collection)}
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
@NonNullByDefault
public abstract class TmfEventTableColumn {

    // ------------------------------------------------------------------------
    // Class attributes
    // ------------------------------------------------------------------------

    /**
     * The base set of columns, which can apply to any trace type.
     */
    public static interface BaseColumns {

        /** Column showing the event timestamp */
        TmfEventTableColumn TIMESTAMP = new TmfTimestampColumn();

        /** Column showing the event's source */
        TmfEventTableColumn SOURCE = new TmfSourceColumn();

        /** Column showing the event type */
        TmfEventTableColumn EVENT_TYPE = new TmfTypeColumn();

        /** Column showing the event reference */
        TmfEventTableColumn REFERENCE = new TmfReferenceColumn();

        /** Column showing the aggregated event contents (fields) */
        TmfEventTableColumn CONTENTS = new TmfContentsColumn();
    }

    /**
     * Static definition of an empty string. Return this instead of returning
     * 'null'!
     */
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final String fHeaderName;
    private final @Nullable String fHeaderTooltip;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor with no tooltip.
     *
     * @param headerName
     *            The name (title) of this column. Should ideally be short.
     */
    public TmfEventTableColumn(String headerName) {
        fHeaderName = headerName;
        fHeaderTooltip = null;
    }

    /**
     * Constructor with a tooltip.
     *
     * @param headerName
     *            The name (title) of this column. Should ideally be short.
     * @param headerTooltip
     *            The tooltip text for the column header. Use 'null' for no
     *            tooltip.
     */
    public TmfEventTableColumn(String headerName, @Nullable String headerTooltip) {
        fHeaderName = headerName;
        fHeaderTooltip = headerTooltip;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get this column's header name, a.k.a. title
     *
     * @return The column's title
     */
    public String getHeaderName() {
        return fHeaderName;
    }

    /**
     * Get the tooltip text for the column header
     *
     * @return The header's tooltip
     */
    public @Nullable String getHeaderTooltip() {
        return fHeaderTooltip;
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Get the string that should be displayed in this column's cell for a given
     * trace event. Basically, this defines "what to print in this column for
     * this event".
     * <p>
     * Note to implementers:
     * <p>
     * This method takes an {@link ITmfEvent}, because any type of event could
     * potentially be present in the table at the time. Do not assume that you
     * will only receive events of your trace type. You'd probably want to
     * return an empty string for event that don't match your expected class
     * type here.
     *
     * @param event
     *            The trace event whose element we want to display
     * @return The string to display in the column for this event
     */
    public abstract String getItemString(ITmfEvent event);

    /**
     * Return the FILTER_ID used by the filters to search this column.
     *
     * @return The filter ID for this column, or 'null' to not provide a filter
     *         ID (which will mean this column will probably not be
     *         searchable/filterable.)
     */
    public abstract @Nullable String getFilterFieldId();
}
