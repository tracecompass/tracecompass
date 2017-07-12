/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Messages;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Numerical aspect, useful for graphs. Not public since it is a trivial
 * implementation of {@link ITmfCounterAspect}
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class AbstractCounterAspect implements ITmfCounterAspect {
    private final String fFieldName;
    private final String fLabel;

    /**
     * Counter aspect
     *
     * @param fieldName
     *            The name of the counter field in an event
     * @param label
     *            The label to display in "help"
     */
    public AbstractCounterAspect(String fieldName, String label) {
        fFieldName = fieldName;
        fLabel = label;
    }

    @Override
    public @NonNull String getName() {
        return fLabel;
    }

    @Override
    public @NonNull String getHelpText() {
        return Messages.CounterAspect_HelpPrefix + ' ' + getName();
    }

    @Override
    public @Nullable Long resolve(@NonNull ITmfEvent event) {
        return event.getContent().getFieldValue(Long.class, fFieldName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + fFieldName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fFieldName, fLabel);
    }

    /**
     * @inheritDoc
     *
     * This is a conservative equals. It only works on very identical aspects.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractCounterAspect other = (AbstractCounterAspect) obj;
        return Objects.equals(fFieldName, other.fFieldName) && Objects.equals(fLabel, other.fLabel);
    }

}