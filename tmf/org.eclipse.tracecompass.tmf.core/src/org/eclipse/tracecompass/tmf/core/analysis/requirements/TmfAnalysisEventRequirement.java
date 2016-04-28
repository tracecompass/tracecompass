/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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
    public TmfAnalysisEventRequirement(Collection<String> eventNames) {
        super(eventNames, PriorityLevel.OPTIONAL);
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
    public TmfAnalysisEventRequirement(Collection<String> eventNames, PriorityLevel level) {
        super(eventNames, level);
    }

    @Override
    public boolean test(ITmfTrace trace) {

        // TODO: implement for all levels
        if (trace instanceof ITmfTraceWithPreDefinedEvents) {
            Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(((ITmfTraceWithPreDefinedEvents) trace).getContainedEventTypes());
            Set<String> mandatoryValues = getPriorityLevel().equals(PriorityLevel.MANDATORY) ? getValues() : Collections.EMPTY_SET;
            return traceEvents.containsAll(mandatoryValues);
        }

        return true;
    }

}
