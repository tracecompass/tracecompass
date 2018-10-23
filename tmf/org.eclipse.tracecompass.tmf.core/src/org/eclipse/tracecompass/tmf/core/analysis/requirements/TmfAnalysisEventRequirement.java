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
public class TmfAnalysisEventRequirement extends TmfAbstractAnalysisRequirement {

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

        if (!(trace instanceof ITmfTraceWithPreDefinedEvents)) {
            return true;
        }

        Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(((ITmfTraceWithPreDefinedEvents) trace).getContainedEventTypes());
        Set<String> values = getValues();

        switch (getPriorityLevel()) {
        case ALL_OR_NOTHING:
            traceEvents.retainAll(values);
            return (traceEvents.isEmpty() || traceEvents.size() == values.size());
        case AT_LEAST_ONE:
            traceEvents.retainAll(values);
            return !traceEvents.isEmpty();
        case MANDATORY:
            return traceEvents.containsAll(values);
        case OPTIONAL:
            return true;
        default:
            throw new IllegalStateException("Unknown value level: " + getPriorityLevel()); //$NON-NLS-1$
        }
    }

}
