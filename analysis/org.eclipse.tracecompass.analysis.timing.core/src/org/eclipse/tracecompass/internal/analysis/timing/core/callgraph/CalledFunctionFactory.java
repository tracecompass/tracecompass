/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Factory to create {@link ICalledFunction}s.
 *
 * @author Matthew Khouzam
 */
public final class CalledFunctionFactory {

    private static final String SEPARATOR = ": "; //$NON-NLS-1$
    private static final String ERROR_MSG = "Cannot create a called function of type : "; //$NON-NLS-1$

    private CalledFunctionFactory() {
        // do nothing
    }

    /**
     * Factory Method for a state value mapped called function
     *
     * @param start
     *            the start time
     * @param end
     *            the end time
     * @param depth
     *            the depth
     * @param stateValue
     *            the symbol
     * @param processId
     *            The process ID of the traced application
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified properties
     */
    public static AbstractCalledFunction create(long start, long end, int depth, Object stateValue, int processId, @Nullable ICalledFunction parent) {
        if (stateValue instanceof Integer) {
            return create(start, end, depth, (int) stateValue, processId, parent);
        } else if (stateValue instanceof Long) {
            return create(start, end, depth, (long) stateValue, processId, parent);
        } else if (stateValue instanceof String) {
            return create(start, end, depth, (String) stateValue, processId, parent);
        }
        throw new IllegalArgumentException(ERROR_MSG + stateValue.getClass() + SEPARATOR + stateValue.toString());
    }

    /**
     * Factory method to create a called function with a symbol that is a long
     * integer
     *
     * @param start
     *            the start time
     * @param end
     *            the end time
     * @param depth
     *            the depth
     * @param value
     *            the symbol
     * @param processId
     *            The process ID of the traced application
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified propertiess
     */
    private static CalledFunction create(long start, long end, int depth, long value, int processId, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + '[' + start + ',' + end + ']');
        }
        return new CalledFunction(start, end, value, depth, processId, parent);
    }

    /**
     * Factory method to create a called function with a symbol that is a
     * {@link String}
     *
     * @param start
     *            the start time
     * @param end
     *            the end time
     * @param depth
     *            the depth
     * @param value
     *            the symbol
     * @param processId
     *            The process ID of the traced application
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified properties
     */
    public static CalledStringFunction create(long start, long end, int depth, String value, int processId, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + '[' + start + ',' + end + ']');
        }
        return new CalledStringFunction(start, end, value, depth, processId, parent);
    }
}
