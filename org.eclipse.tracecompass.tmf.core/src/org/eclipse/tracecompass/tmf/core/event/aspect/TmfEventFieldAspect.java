/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Event aspect representing a single field of an event.
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventFieldAspect implements ITmfEventAspect {

    private final String fAspectName;
    private final String fFieldName;

    /**
     * Constructor
     *
     * @param aspectName
     *            The name of the aspect. Should be localized.
     * @param fieldName
     *            The name of the field to look for in the trace. Should *not*
     *            be localized!
     */
    public TmfEventFieldAspect(String aspectName, String fieldName) {
        fAspectName = aspectName;
        fFieldName = fieldName;
    }

    @Override
    public String getName() {
        return fAspectName;
    }

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public String resolve(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fFieldName);
        if (field == null) {
            return EMPTY_STRING;
        }
        String fieldValue = field.getFormattedValue();
        return (fieldValue == null ? EMPTY_STRING : fieldValue);
    }

    @Override
    public @Nullable String getFilterId() {
        return null;
    }

    // ------------------------------------------------------------------------
    // hashCode/equals
    // Typically we want identical field aspects to be merged together.
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fAspectName.hashCode();
        result = prime * result + fFieldName.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TmfEventFieldAspect)) {
            return false;
        }
        TmfEventFieldAspect other = (TmfEventFieldAspect) obj;
        if (!fAspectName.equals(other.fAspectName)) {
            return false;
        }
        if (!fFieldName.equals(other.fFieldName)) {
            return false;
        }
        return true;
    }
}
