/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Renamed from TmfEventFieldAspect and support subfield array
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Event aspect representing a single field of an event's content.
 *
 * @author Alexandre Montplaisir
 */
public class TmfContentFieldAspect implements ITmfEventAspect {

    private final String fAspectName;
    private final String[] fFieldPath;

    /**
     * Constructor
     *
     * @param aspectName
     *            The name of the aspect. Should be localized.
     * @param fieldPath
     *            The field name or absolute field path array to look for in the
     *            event content. Should *not* be localized!
     */
    public TmfContentFieldAspect(String aspectName, String... fieldPath) {
        fAspectName = aspectName;
        fFieldPath = checkNotNull(Arrays.copyOf(fieldPath, fieldPath.length));
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
    public @Nullable Object resolve(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fFieldPath);
        if (field == null) {
            return null;
        }
        return field.getValue();
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
        result = prime * result + Arrays.hashCode(fFieldPath);
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
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        TmfContentFieldAspect other = (TmfContentFieldAspect) obj;
        if (!fAspectName.equals(other.fAspectName)) {
            return false;
        }
        if (!Arrays.equals(fFieldPath, other.fFieldPath)) {
            return false;
        }
        return true;
    }
}
