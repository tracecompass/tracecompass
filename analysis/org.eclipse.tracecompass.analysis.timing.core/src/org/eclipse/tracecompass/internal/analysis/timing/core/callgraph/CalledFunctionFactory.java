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
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * Factory to create {@link ICalledFunction}s.
 *
 * @author Matthew Khouzam
 */
public class CalledFunctionFactory {

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
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified propertiess
     */
    public static AbstractCalledFunction create(long start, long end, int depth, ITmfStateValue stateValue, @Nullable ICalledFunction parent) {
        switch (stateValue.getType()) {
        case CUSTOM:
            throw new IllegalArgumentException(ERROR_MSG + stateValue.getType() + SEPARATOR + stateValue.toString());
        case DOUBLE:
            throw new IllegalArgumentException(ERROR_MSG + stateValue.getType() + SEPARATOR + stateValue.toString());
        case INTEGER:
            return create(start, end, depth, stateValue.unboxInt(), parent);
        case LONG:
            return create(start, end, depth, stateValue.unboxLong(), parent);
        case NULL:
            throw new IllegalArgumentException(ERROR_MSG + stateValue.getType() + SEPARATOR + stateValue.toString());
        case STRING:
            return create(start, end, depth, stateValue.unboxStr(), parent);
        default:
            throw new IllegalArgumentException(ERROR_MSG + stateValue.getType() + SEPARATOR + stateValue.toString());
        }
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
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified propertiess
     */
    private static CalledFunction create(long start, long end, int depth, long value, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + '[' + start + ',' + end + ']');
        }
        return new CalledFunction(start, end, value, depth, parent);
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
     * @param parent
     *            the parent node
     * @return an ICalledFunction with the specified properties
     */
    public static CalledStringFunction create(long start, long end, int depth, String value, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + '[' + start + ',' + end + ']');
        }
        return new CalledStringFunction(start, end, value, depth, parent);
    }
}
