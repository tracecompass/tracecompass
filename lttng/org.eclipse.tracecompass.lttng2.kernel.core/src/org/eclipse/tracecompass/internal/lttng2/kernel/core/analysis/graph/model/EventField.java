/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    /**
     * Get int field
     *
     * @param event
     *            the event
     * @param name
     *            the field name
     * @return the long value
     * @deprecated Use the {@link ITmfEventField#getFieldValue(Class, String...)} with class Integer.class instead
     */
    @Deprecated
    public static Integer getInt(ITmfEvent event, String name) {
        ITmfEventField field = NonNullUtils.checkNotNull(event.getContent().getField(name));
        Object value = field.getValue();
        if (value instanceof Long) {
            return NonNullUtils.checkNotNull(((Long) value).intValue());
        }
        return NonNullUtils.checkNotNull((Integer) value);
    }

    /**
     * Get long field
     *
     * @param event
     *            the event
     * @param name
     *            the field name
     * @return the long value
     * @deprecated Use the {@link ITmfEventField#getFieldValue(Class, String...)} with class Long.class instead
     */
    @Deprecated
    public static Long getLong(ITmfEvent event, String name) {
        ITmfEventField field = NonNullUtils.checkNotNull(event.getContent().getField(name));
        return NonNullUtils.checkNotNull((Long) field.getValue());
    }

    /**
     * Get string field
     *
     * @param event
     *            the event
     * @param name
     *            the field name
     * @return the string value
     * @deprecated Use the {@link ITmfEventField#getFieldValue(Class, String...)} with class String.class instead
     */
    @Deprecated
    public static String getString(ITmfEvent event, String name) {
        ITmfEventField field = NonNullUtils.checkNotNull(event.getContent().getField(name));
        return NonNullUtils.checkNotNull((String) field.getValue());
    }

    /**
     * Get float field
     *
     * @param event
     *            the event
     * @param name
     *            the field name
     * @return the float value
     * @deprecated Use the {@link ITmfEventField#getFieldValue(Class, String...)} with class Double.class instead
     */
    @Deprecated
    public static double getFloat(ITmfEvent event, String name) {
        ITmfEventField field = NonNullUtils.checkNotNull(event.getContent().getField(name));
        return NonNullUtils.checkNotNull((Double) field.getValue());
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
