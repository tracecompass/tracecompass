/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

/**
 * This represents a specialized query filter used by data some providers. In
 * addition to base query filters, it encapsulated the selected thread.
 *
 * TODO : For the moment, there is no support for multiple thread selection for
 * XY charts. Once it is supported, please make this class implements
 * {@link IMultipleSelectionQueryFilter}
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class SelectedThreadQueryFilter extends TimeQueryFilter {

    private final String fSelectedThread;

    /**
     * Constructor. Given a start value, end value and n entries, this constructor
     * will set x values property to an array of n entries uniformly distributed and
     * ordered ascendingly.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     * @param selectedThread
     *            A selected thread
     */
    public SelectedThreadQueryFilter(long start, long end, int n, String selectedThread) {
        super(start, end, n);
        fSelectedThread = selectedThread;
    }

    /**
     * Gets the selected thread
     *
     * @return selected thread
     */
    public String getSelectedThread() {
        return fSelectedThread;
    }
}
