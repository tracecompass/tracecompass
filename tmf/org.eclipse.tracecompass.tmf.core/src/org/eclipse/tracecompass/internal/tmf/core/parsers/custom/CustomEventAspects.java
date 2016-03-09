/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Update for TmfEventTableColumn
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.parsers.custom;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event aspects for Custom {Text|XML} traces.
 *
 * Since this definition will be different for every single custom trace, we
 * cannot define specific {@link ITmfEventAspect} in advance.
 *
 * Instead, one has to call {@link #generateAspects(CustomTraceDefinition)}
 * with the CustomTraceDefinition of the the particular trace to display.
 *
 * @author Alexandre Montplaisir
 */
public class CustomEventAspects {

    /**
     * Aspects for custom events, which use an integer ID to represent each
     * field.
     */
    private static final class CustomEventFieldAspect implements ITmfEventAspect {

        private final @NonNull String fName;
        private final int fIndex;

        /**
         * Constructor
         *
         * @param name
         *            The name of this aspect
         * @param idx
         *            The index of this field in the event's content to display
         */
        public CustomEventFieldAspect(@NonNull String name, int idx) {
            fName = name;
            fIndex = idx;
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (event instanceof CustomEvent) {
                return NonNullUtils.nullToEmptyString(((CustomEvent) event).getEventString(fIndex));
            }
            return EMPTY_STRING;
        }
    }

    /**
     * Build a set of event aspects for a given trace definition
     *
     * @param definition
     *            The {@link CustomTraceDefinition} of the trace for which you
     *            want the aspects
     * @return The set of event aspects for the given trace
     */
    public static @NonNull Iterable<ITmfEventAspect> generateAspects(CustomTraceDefinition definition) {
        ImmutableList.Builder<ITmfEventAspect> builder = new ImmutableList.Builder<>();
        List<OutputColumn> outputs = definition.outputs;
        for (int i = 0; i < outputs.size(); i++) {
            String name = outputs.get(i).name;
            builder.add(new CustomEventFieldAspect(name, i));
        }
        return builder.build();
    }
}
