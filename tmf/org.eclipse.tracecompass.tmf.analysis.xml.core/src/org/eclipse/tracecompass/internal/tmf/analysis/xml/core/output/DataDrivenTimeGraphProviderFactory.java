/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IDataDrivenRuntimeObject;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * A factory for data driven time graphs. It describes the time graph but does
 * not apply it yet to a trace.
 *
 * @author Geneviève Bastien
 * @author Loic Prieur-Drevon
 */
public class DataDrivenTimeGraphProviderFactory implements IDataDrivenRuntimeObject {

    private final List<DataDrivenPresentationState> fValues;
    private final List<DataDrivenTimeGraphEntry> fEntries;
    private final Set<String> fAnalysisIds;

    /**
     * Constructor
     *
     * @param entries
     *            The entries for this time graph
     * @param analysisIds
     *            The IDs of the analyses to build
     * @param values
     *            The values to use to display the labels and colors of this
     *            time graph
     */
    public DataDrivenTimeGraphProviderFactory(List<DataDrivenTimeGraphEntry> entries, Set<String> analysisIds, List<DataDrivenPresentationState> values) {
        fValues = values;
        fEntries = entries;
        fAnalysisIds = analysisIds;
    }

    /**
     * Create a data provider for a trace
     *
     * @param trace
     *            The trace for which to create a provider
     * @return A time graph data provider, or <code>null</code> if the data
     *         provider cannot be built for this trace because it does not have
     *         the proper analyses.
     */
    public @Nullable ITimeGraphDataProvider<TimeGraphEntryModel> create(ITmfTrace trace) {

        Set<@NonNull ITmfAnalysisModuleWithStateSystems> stateSystemModules = new HashSet<>();
        List<ITmfStateSystem> sss = new ArrayList<>();
        if (fAnalysisIds.isEmpty()) {
            /*
             * No analysis specified, take all state system analysis modules
             */
            Iterables.addAll(stateSystemModules, TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class));
        } else {
            for (String moduleId : fAnalysisIds) {
                // Get the module for the current trace only. The caller will
                // take care of
                // generating composite providers with experiments
                IAnalysisModule module = trace.getAnalysisModule(moduleId);
                if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                    stateSystemModules.add((ITmfAnalysisModuleWithStateSystems) module);
                }
            }
        }

        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            if (module.schedule().isOK() && module.waitForInitialization()) {
                module.getStateSystems().forEach(sss::add);
            }
        }
        return (sss.isEmpty() ? null : new DataDrivenTimeGraphDataProvider(trace, sss, fEntries, fValues));
    }

}
