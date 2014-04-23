/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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

package org.eclipse.linuxtools.lttng2.ust.core.analysis.memory;

import org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage.MemoryUsageStateProvider;
import org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage.UstMemoryStrings;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * UST trace
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            UstMemoryStrings.MALLOC,
            UstMemoryStrings.FREE,
            UstMemoryStrings.CALLOC,
            UstMemoryStrings.REALLOC,
            UstMemoryStrings.MEMALIGN,
            UstMemoryStrings.POSIX_MEMALIGN
            );

    /** The requirements as an immutable set */
    private static final ImmutableSet<TmfAnalysisRequirement> REQUIREMENTS;

    static {
        /* Initialize the requirements for the analysis: domain and events */
        TmfAnalysisRequirement eventsReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_EVENT, REQUIRED_EVENTS, ValuePriorityLevel.MANDATORY);
        /*
         * In order to have these events, the libc wrapper with probes should be
         * loaded
         */
        eventsReq.addInformation(Messages.UstMemoryAnalysisModule_EventsLoadingInformation);
        eventsReq.addInformation(Messages.UstMemoryAnalysisModule_EventsLoadingExampleInformation);

        /* The domain type of the analysis */
        TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);
        domainReq.addValue(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST, ValuePriorityLevel.MANDATORY);

        REQUIREMENTS = ImmutableSet.of(domainReq, eventsReq);
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new MemoryUsageStateProvider(getTrace());
    }

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            throw new IllegalStateException("UstMemoryAnalysisModule: trace should be of type LttngUstTrace"); //$NON-NLS-1$
        }
        super.setTrace(trace);
    }

    @Override
    protected LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return REQUIREMENTS;
    }
}
