/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Type casting for getting field values
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class EventField {

    private EventField() {
        // Empty constructor
    }

    /**
     * Get string field with default value
     *
     * @param event
     *            the event
     * @param name
     *            the field name
     * @param def
     *            the default value to return if the field does not exists
     * @return the long value
     */
    public static String getOrDefault(ITmfEvent event, String name, String def) {
        ITmfEventField field = event.getContent().getField(name);
        if (field == null) {
            return def;
        }
        return NonNullUtils.checkNotNull((String) field.getValue());
    }
}
