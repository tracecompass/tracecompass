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
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 * Extension of {@link TmfCallsite} specifically for the debug-info analysis,
 * which will not print the function name in the event table. This name will be
 * available by a separate aspect.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 * @deprecated No need for this anymore, use {@link TmfCallsite} directly.
 */
@Deprecated
public class SourceCallsite extends TmfCallsite {

    /**
     * Constructor
     *
     * @param fileName
     *            File name
     * @param functionName
     *            Function name
     * @param lineNumber
     *            Line number
     */
    public SourceCallsite(String fileName, @Nullable String functionName, long lineNumber) {
        super(fileName, lineNumber);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getFileName()).append(':');
        builder.append(getLineNo());
        return builder.toString();
    }

}
