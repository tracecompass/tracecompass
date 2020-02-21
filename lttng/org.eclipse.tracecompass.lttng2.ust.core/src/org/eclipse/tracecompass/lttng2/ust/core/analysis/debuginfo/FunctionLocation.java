/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Compound object representing a binary location inside a function/symbol
 * inside a binary.
 *
 * It consists of the function/symbol name, and offset within this function. The
 * offset may or may not be available.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class FunctionLocation {

    private final String fFunctionName;
    private final @Nullable Long fOffset;

    /**
     * Constructor
     *
     * @param functionName
     *            Name of the function
     * @param offsetInFunction
     *            Offset *within this function*. May be null to mean unknown.
     */
    public FunctionLocation(String functionName, @Nullable Long offsetInFunction) {
        fFunctionName = functionName;
        fOffset = offsetInFunction;
    }

    /**
     * Get the function name.
     *
     * @return The function name
     * @since 2.1
     */
    public String getFunctionName() {
        return fFunctionName;
    }

    /**
     * Get the offset *within this function* represented by this location.
     *
     * @return The offset of this location, or 'null' if unavailable
     * @since 2.1
     */
    public @Nullable Long getOffsetInFunction() {
        return fOffset;
    }

    @Override
    public String toString() {
        Long offset = fOffset;

        if (offset == null) {
            return fFunctionName;
        }
        return (fFunctionName + "+0x" + Long.toHexString(offset.longValue())); //$NON-NLS-1$

    }
}
