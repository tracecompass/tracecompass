/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventFieldRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * Analysis requirement implementation for LTTng Call Stack Analysis.
 *
 * @author Bernd Hufmann
 *
 */
@NonNullByDefault
public class LttngUstCallStackAnalysisRequirement extends TmfAnalysisRequirement {

    private List<TmfAnalysisEventFieldRequirement> fEntryRequirements = new ArrayList<>();
    private List<TmfAnalysisEventFieldRequirement> fExitRequirements = new ArrayList<>();
    /**
     * Constructor
     *
     * @param layout
     *            The event layout (non-null)
     */
    public LttngUstCallStackAnalysisRequirement(ILttngUstEventLayout layout) {
        super(Collections.EMPTY_SET, PriorityLevel.MANDATORY);

        Set<@NonNull String> requiredEventsFields = ImmutableSet.of(
                layout.contextProcname(),
                layout.contextVtid());
        TmfAnalysisEventFieldRequirement requirement = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFuncEntry(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);
        fEntryRequirements.add(requirement);

        requirement = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFastFuncEntry(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);
        fEntryRequirements.add(requirement);

        requirement = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFuncExit(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);
        fExitRequirements.add(requirement);

        requirement = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFastFuncExit(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);
        fExitRequirements.add(requirement);

        // Add mandatory values (event names and context names)
        addInformation(nullToEmptyString(Messages.LttnUstCallStackAnalysisModule_EventsLoadingInformation));
    }

    @Override
    public boolean test(ITmfTrace trace) {
        boolean fullfilled = fEntryRequirements.stream().anyMatch(requirement -> {
            return requirement.test(trace);
        });

        if (fullfilled) {
            fullfilled = fExitRequirements.stream().anyMatch(requirement -> {
                return requirement.test(trace);
            });
        }
        return fullfilled;
    }
}
