/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    @Override
    public String toString() {
        Long offset = fOffset;

        if (offset == null) {
            return fFunctionName;
        }
        return (fFunctionName + "+0x" + Long.toHexString(offset.longValue())); //$NON-NLS-1$

    }
}
