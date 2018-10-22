/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * A column in the
 * {@link org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable}. In
 * addition to ones provided by default, trace types can extend this class to
 * create additional columns specific to their events.
 *
 * Those additional columns can then be passed to the constructor
 * {@link org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, java.util.Collection)}
 *
 * @author Alexandre Montplaisir
 * @noextend This class should not be extended directly. You should instead
 *           implement an {@link ITmfEventAspect}.
 */
@NonNullByDefault
public class TmfEventTableColumn {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final ITmfEventAspect<?> fAspect;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param aspect
     *            The {@link ITmfEventAspect} to be used to populate this
     *            column.
     */
    public TmfEventTableColumn(ITmfEventAspect<?> aspect) {
        fAspect = aspect;
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
        return fAspect.getName();
    }

    /**
     * Get the tooltip text for the column header
     *
     * @return The header's tooltip
     */
    public @Nullable String getHeaderTooltip() {
        return fAspect.getHelpText();
    }

    /**
     * Get the string that should be displayed in this column's cell for a given
     * trace event. Basically, this defines "what to print in this column for
     * this event".
     *
     * @param event
     *            The trace event whose element we want to display
     * @return The string to display in the column for this event
     */
    public String getItemString(ITmfEvent event) {
        return NonNullUtils.nullToEmptyString(fAspect.resolve(event));
    }

    /**
     * Get the event aspect assigned to this column
     *
     * @return The event aspect
     */
    public ITmfEventAspect<?> getEventAspect() {
        return fAspect;
    }

    // ------------------------------------------------------------------------
    // hashCode/equals (so that equivalent columns can be merged together)
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fAspect.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventTableColumn)) {
            return false;
        }
        TmfEventTableColumn other = (TmfEventTableColumn) obj;
        /* Aspects can also define how they can be "equal" to one another */
        return (fAspect.equals(other.fAspect));
    }
}
