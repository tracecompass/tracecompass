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
 *   Patrick Tasse - Support subfield array
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Event aspect representing a single field of an event.
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventFieldAspect implements ITmfEventAspect {

    private static final char SLASH = '/';
    private static final char BACKSLASH = '\\';

    private final String fAspectName;
    private final String fFieldName;
    private final String[] fFieldArray;

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
        if (!fieldName.isEmpty() && fieldName.charAt(0) == SLASH) {
            fFieldArray = getFieldArray(fieldName);
        } else {
            fFieldArray = new String[] { fieldName };
        }

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
    public @Nullable String resolve(ITmfEvent event) {
        ITmfEventField field;
        field = event.getContent().getField(fFieldArray);
        if (field == null) {
            return null;
        }
        return field.getFormattedValue();
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
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
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

    private static String[] getFieldArray(String field) {

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        // We start at 1 since the first character is a slash that we want to
        // ignore.
        for (int i = 1; i < field.length(); i++) {
            char charAt = field.charAt(i);
            if (charAt == SLASH) {
                // char is slash. Cut here.
                list.add(sb.toString());
                sb = new StringBuilder();
            } else if ((charAt == BACKSLASH) && (i < field.length() - 1) && (field.charAt(i + 1) == SLASH)) {
                // Uninterpreted slash. Add it.
                sb.append(SLASH);
                i++;
            } else {
                // Any other character. Add.
                sb.append(charAt);
            }
        }

        // Last block. Add it to list.
        list.add(sb.toString());

        // Transform to array
        String[] array = new String[list.size()];
        list.toArray(array);

        return array;
    }
}
