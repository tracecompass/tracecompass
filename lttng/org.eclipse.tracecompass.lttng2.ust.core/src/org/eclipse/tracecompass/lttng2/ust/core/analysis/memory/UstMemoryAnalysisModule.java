/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
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

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStateProvider;
import org.eclipse.tracecompass.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
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
    private @Nullable Set<TmfAnalysisRequirement> fAnalysisRequirements;

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new UstMemoryStateProvider(checkNotNull(getTrace()));
    }

    /**
     * @since 1.0
     */
    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        /*
         * setTrace() calls getAnalysisRequirements, so we need the field set
         * for the check to work.
         */
        fAnalysisRequirements = requirementsForTrace((LttngUstTrace) trace);
        boolean traceIsSet = super.setTrace(trace);
        if (!traceIsSet) {
            /* Unset the requirements, the trace was not good after all. */
            fAnalysisRequirements = null;
        }
        return traceIsSet;
    }

    @Override
    protected LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    private static Set<TmfAnalysisRequirement> requirementsForTrace(LttngUstTrace trace) {
        /*
         * Compute the list of required events, whose exact names can change
         * depending on the tracer's version.
         */
        ILttngUstEventLayout layout = trace.getEventLayout();
        Set<String> requiredEvents = ImmutableSet.of(
                layout.eventLibcMalloc(),
                layout.eventLibcFree(),
                layout.eventLibcCalloc(),
                layout.eventLibcRealloc(),
                layout.eventLibcMemalign(),
                layout.eventLibcPosixMemalign()
              );

        /* Initialize the requirements for the analysis: domain and events */
        TmfAnalysisRequirement eventsReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_EVENT, requiredEvents, ValuePriorityLevel.MANDATORY);
        /*
         * In order to have these events, the libc wrapper with probes should be
         * loaded
         */
        eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingInformation));
        eventsReq.addInformation(nullToEmptyString(Messages.UstMemoryAnalysisModule_EventsLoadingExampleInformation));

        /* The domain type of the analysis */
        TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);
        domainReq.addValue(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST, ValuePriorityLevel.MANDATORY);

        return ImmutableSet.of(domainReq, eventsReq);
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        Set<TmfAnalysisRequirement> reqs = fAnalysisRequirements;
        if (reqs == null) {
            throw new IllegalStateException("Cannot get the analysis requirements without an assigned trace."); //$NON-NLS-1$
        }
        return reqs;
    }
}
