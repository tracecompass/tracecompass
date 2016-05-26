/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tassé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * Event type for custom text parsers.
 *
 * @author Patrick Tassé
 */
public abstract class CustomEventType extends TmfEventType {

    private static final @NonNull String EMPTY = ""; //$NON-NLS-1$
    private @NonNull String fEventName;

    /**
     * Constructor
     *
     * @param definition
     *            Trace definition
     * @deprecated Use {@link #CustomEventType(String, ITmfEventField)} instead.
     */
    @Deprecated
    public CustomEventType(CustomTraceDefinition definition) {
        super(EMPTY, getRootField(definition));
        fEventName = checkNotNull(definition.definitionName);
    }

    /**
     * Constructor
     *
     * @param eventName
     *            the event name
     * @param root
     *            the root field
     * @since 2.1
     */
    public CustomEventType(@NonNull String eventName, ITmfEventField root) {
        super(EMPTY, root);
        fEventName = eventName;
    }

    @Override
    public @NonNull String getName() {
        return fEventName;
    }

    /**
     * Set the event name.
     *
     * @param eventName
     *            the event name
     * @since 2.1
     */
    public void setName(@NonNull String eventName) {
        fEventName = eventName;
    }

    static ITmfEventField getRootField(CustomTraceDefinition definition) {
        ITmfEventField[] fields = new ITmfEventField[definition.outputs.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TmfEventField(definition.outputs.get(i).name, null, null);
        }
        ITmfEventField rootField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fields);
        return rootField;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += fEventName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && (obj.getClass().equals(getClass()))) {
            return fEventName.equals(((CustomEventType) obj).fEventName);
        }
        return false;
    }

    @Override
    public String toString() {
        return fEventName;
    }
}
