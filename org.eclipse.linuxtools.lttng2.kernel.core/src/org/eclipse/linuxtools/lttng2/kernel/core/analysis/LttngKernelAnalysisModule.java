/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.LttngStrings;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

import com.google.common.collect.ImmutableSet;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class LttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * The file name of the History Tree
     */
    public static final @NonNull String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /** The ID of this analysis module */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.kernel.analysis"; //$NON-NLS-1$

    /*
     * TODO: Decide which events should be mandatory for the analysis, once the
     * appropriate error messages and session setup are in place.
     */
    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of();

    private static final ImmutableSet<String> OPTIONAL_EVENTS = ImmutableSet.of(
            LttngStrings.EXIT_SYSCALL,
            LttngStrings.IRQ_HANDLER_ENTRY,
            LttngStrings.IRQ_HANDLER_EXIT,
            LttngStrings.SOFTIRQ_ENTRY,
            LttngStrings.SOFTIRQ_EXIT,
            LttngStrings.SOFTIRQ_RAISE,
            LttngStrings.SCHED_PROCESS_FORK,
            LttngStrings.SCHED_PROCESS_EXIT,
            LttngStrings.SCHED_PROCESS_FREE,
            LttngStrings.SCHED_SWITCH,
            LttngStrings.STATEDUMP_PROCESS_STATE,
            LttngStrings.SCHED_WAKEUP,
            LttngStrings.SCHED_WAKEUP_NEW,

            /* FIXME Add the prefix for syscalls */
            LttngStrings.SYSCALL_PREFIX
            );

    /** The requirements as an immutable set */
    private static final ImmutableSet<TmfAnalysisRequirement> REQUIREMENTS;

    static {
        /* initialize the requirement: domain and events */
        TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);
        domainReq.addValue(SessionConfigStrings.CONFIG_DOMAIN_TYPE_KERNEL, ValuePriorityLevel.MANDATORY);

        TmfAnalysisRequirement eventReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_EVENT, REQUIRED_EVENTS, ValuePriorityLevel.MANDATORY);
        eventReq.addValues(OPTIONAL_EVENTS, ValuePriorityLevel.OPTIONAL);

        REQUIREMENTS = ImmutableSet.of(domainReq, eventReq);
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new LttngKernelStateProvider(getTrace());
    }

    @Override
    @NonNull
    protected String getSsFileName() {
        return HISTORY_TREE_FILE_NAME;
    }

    @Override
    protected String getFullHelpText() {
        return Messages.LttngKernelAnalysisModule_Help;
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return REQUIREMENTS;
    }
}
