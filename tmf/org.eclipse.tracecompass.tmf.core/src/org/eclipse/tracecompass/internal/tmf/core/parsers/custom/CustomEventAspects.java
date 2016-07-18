/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;

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
     * Build a set of event aspects for a given trace definition
     *
     * @param definition
     *            The {@link CustomTraceDefinition} of the trace for which you
     *            want the aspects
     * @return The set of event aspects for the given trace
     */
    public static @NonNull Iterable<ITmfEventAspect<?>> generateAspects(CustomTraceDefinition definition) {
        ImmutableList.Builder<ITmfEventAspect<?>> builder = new ImmutableList.Builder<>();
        for (OutputColumn output : definition.outputs) {
            if (output.tag.equals(Tag.TIMESTAMP) &&
                    (definition.timeStampOutputFormat == null || definition.timeStampOutputFormat.isEmpty())) {
                builder.add(TmfBaseAspects.getTimestampAspect());
            } else if (output.tag.equals(Tag.EVENT_TYPE)) {
                builder.add(TmfBaseAspects.getEventTypeAspect());
            } else {
                builder.add(new TmfContentFieldAspect(output.name, output.name));
            }
        }
        return builder.build();
    }
}
