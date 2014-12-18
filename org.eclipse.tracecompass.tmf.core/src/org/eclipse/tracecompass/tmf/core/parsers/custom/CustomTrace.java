/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

/**
 * Base class for custom traces.
 *
 * @author Patrick Tasse
 */
public abstract class CustomTrace extends TmfTrace {

    private static final char SEPARATOR = ':';

    private final String fTraceTypeId;

    /**
     * Basic constructor.
     *
     * @param definition
     *            Custom trace definition
     */
    public CustomTrace(CustomTraceDefinition definition) {
        fTraceTypeId = buildTraceTypeId(getClass(), definition.categoryName, definition.definitionName);
    }

    /**
     * Build the trace type id for a custom trace
     *
     * @param traceClass
     *            the trace class
     * @param category
     *            the category
     * @param definitionName
     *            the definition name
     * @return the trace type id
     */
    public static @NonNull String buildTraceTypeId(Class<? extends ITmfTrace> traceClass, String category, String definitionName) {
        return traceClass.getCanonicalName() + SEPARATOR + category + SEPARATOR + definitionName;
    }

    @Override
    public String getTraceTypeId() {
        return fTraceTypeId;
    }
}
