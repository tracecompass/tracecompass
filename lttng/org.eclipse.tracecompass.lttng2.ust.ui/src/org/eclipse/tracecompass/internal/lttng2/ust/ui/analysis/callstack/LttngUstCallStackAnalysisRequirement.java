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

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventFieldRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfCompositeAnalysisRequirement;

import com.google.common.collect.ImmutableSet;

/**
 * Analysis requirement implementation for LTTng Call Stack Analysis.
 *
 * @author Bernd Hufmann
 *
 */
@NonNullByDefault
public class LttngUstCallStackAnalysisRequirement extends TmfCompositeAnalysisRequirement {

    /**
     * Constructor
     *
     * @param layout
     *            The event layout (non-null)
     */
    public LttngUstCallStackAnalysisRequirement(ILttngUstEventLayout layout) {
        super(getSubRequirements(layout), PriorityLevel.AT_LEAST_ONE);

        addInformation(nullToEmptyString(Messages.LttnUstCallStackAnalysisModule_EventsLoadingInformation));
    }

    private static Collection<TmfAbstractAnalysisRequirement> getSubRequirements(ILttngUstEventLayout layout) {
        Set<@NonNull String> requiredEventsFields = ImmutableSet.of(
                layout.contextProcname(),
                layout.contextVtid());

        // Requirement for the cyg_profile events
        TmfAnalysisEventFieldRequirement entryReq = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFuncEntry(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);

        TmfAbstractAnalysisRequirement exitReq = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFuncExit(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);

        TmfAbstractAnalysisRequirement cygProfile = new TmfCompositeAnalysisRequirement(ImmutableSet.of(entryReq, exitReq), PriorityLevel.MANDATORY);

        // Requirement for the cyg_profile_fast events
        entryReq = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFastFuncEntry(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);

        exitReq = new TmfAnalysisEventFieldRequirement(
                layout.eventCygProfileFastFuncExit(),
                requiredEventsFields,
                PriorityLevel.MANDATORY);
        TmfAbstractAnalysisRequirement cygProfileFast = new TmfCompositeAnalysisRequirement(ImmutableSet.of(entryReq, exitReq), PriorityLevel.MANDATORY);

        return ImmutableSet.of(cygProfile, cygProfileFast);
    }

}
