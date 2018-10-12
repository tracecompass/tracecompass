/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Guilliano Molaire - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStateProvider;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * UST trace
 *
 * @author Geneviève Bastien
 */
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    /** The analysis's requirements. Only set after the trace is set. */
    private @Nullable Set<TmfAbstractAnalysisRequirement> fAnalysisRequirements;

    @Override
    public ITmfStateProvider createStateProvider() {
        return new UstMemoryStateProvider(checkNotNull(getTrace()));
    }

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        Set<TmfAbstractAnalysisRequirement> requirements = fAnalysisRequirements;
        if (requirements == null) {
            LttngUstTrace trace = getTrace();
            ILttngUstEventLayout layout;
            if (trace == null) {
                layout = ILttngUstEventLayout.DEFAULT_LAYOUT;
            } else {
                layout = trace.getEventLayout();
            }

            @NonNull Set<@NonNull String> requiredEvents = ImmutableSet.of(
                    layout.eventLibcMalloc(),
                    layout.eventLibcFree(),
                    layout.eventLibcCalloc(),
                    layout.eventLibcRealloc(),
                    layout.eventLibcMemalign(),
                    layout.eventLibcPosixMemalign()
                  );

            /* Initialize the requirements for the analysis: domain and events */
            TmfAbstractAnalysisRequirement eventsReq = new TmfAnalysisEventRequirement(requiredEvents, PriorityLevel.MANDATORY);
            /*
             * In order to have these events, the libc wrapper with probes should be
             * loaded
             */
            eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingInformation));
            eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingExampleInformation));

            /* The domain type of the analysis */
            // FIXME: The domain requirement should have a way to be verified. It is useless otherwise
            // TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(nullToEmptyString(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN));
            // domainReq.addValue(nullToEmptyString(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST), ValuePriorityLevel.MANDATORY);

            requirements = ImmutableSet.of(eventsReq);
            fAnalysisRequirements = requirements;
        }
        return requirements;
    }
}
