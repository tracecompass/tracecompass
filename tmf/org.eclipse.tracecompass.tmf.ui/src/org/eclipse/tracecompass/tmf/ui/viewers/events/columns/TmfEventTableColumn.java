/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.events.columns;

import java.util.ArrayList;
import java.util.List;

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

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private final List<ITmfEventAspect<?>> fAspects = new ArrayList<>();

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
        fAspects.add(aspect);
    }

    // ------------------------------------------------------------------------
    // adders
    // ------------------------------------------------------------------------
    /**
     * Add another Aspect with the same name
     *
     * @param duplicate
     *            the aspect with the same name
     * @since 5.0
     */
    public void addDuplicate(ITmfEventAspect<?> duplicate) {
        fAspects.add(duplicate);
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
        return fAspects.get(0).getName();
    }

    /**
     * Get the tooltip text for the column header
     *
     * @return The header's tooltip
     */
    public @Nullable String getHeaderTooltip() {
        return fAspects.get(0).getHelpText();
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
        for (ITmfEventAspect<?> aspect : fAspects) {
            String eventString = NonNullUtils.nullToEmptyString(aspect.resolve(event));
            if (!eventString.isEmpty()) {
                return eventString;
            }
        }
        return EMPTY_STRING;
    }

    /**
     * Get the event aspect assigned to this column
     *
     * @return The event aspect
     */
    public ITmfEventAspect<?> getEventAspect() {
        return fAspects.get(0);
    }

    // ------------------------------------------------------------------------
    // hashCode/equals (so that equivalent columns can be merged together)
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fAspects.get(0).hashCode();
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
        return (fAspects.get(0).equals(other.fAspects.get(0)));
    }
}
