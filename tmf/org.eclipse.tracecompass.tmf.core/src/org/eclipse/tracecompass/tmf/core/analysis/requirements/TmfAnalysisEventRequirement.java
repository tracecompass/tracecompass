/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;

/**
 * An analysis requirement for event names
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class TmfAnalysisEventRequirement extends TmfAnalysisRequirement {

    /**
     * Constructor for an optional requirement
     *
     * TODO: The TYPE_EVENT should be removed and this class used instead
     *
     * @param eventNames
     *            The list of event names the trace must have
     */
    public TmfAnalysisEventRequirement(Iterable<String> eventNames) {
        super(TmfAnalysisRequirement.TYPE_EVENT, eventNames, ValuePriorityLevel.OPTIONAL);
    }

    /**
     * Constructor. Instantiate a requirement object with a list of values of
     * the same level
     *
     * @param eventNames
     *            The list of event names the trace must have
     * @param level
     *            A level associated with all the values
     */
    public TmfAnalysisEventRequirement(Iterable<String> eventNames, ValuePriorityLevel level) {
        super(TmfAnalysisRequirement.TYPE_EVENT, eventNames, level);
    }

    @Override
    public boolean isFulfilled(@NonNull ITmfTrace trace) {

        if (trace instanceof ITmfTraceWithPreDefinedEvents) {
            Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(((ITmfTraceWithPreDefinedEvents) trace).getContainedEventTypes());
            Set<String> mandatoryValues = getValues(ValuePriorityLevel.MANDATORY);
            return traceEvents.containsAll(mandatoryValues);
        }

        return true;
    }

}
