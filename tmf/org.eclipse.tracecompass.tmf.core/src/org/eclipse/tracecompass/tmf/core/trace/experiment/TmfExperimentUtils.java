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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.experiment;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This utility class contains some utility methods to retrieve specific traces
 * or analysis in an experiment.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public final class TmfExperimentUtils {

    private TmfExperimentUtils() {

    }

    // ------------------------------------------------------------------------
    // Utility methods for analysis modules
    // ------------------------------------------------------------------------

    private static Iterable<ITmfTrace> getTracesFromHost(TmfExperiment experiment, String hostId) {
        Collection<ITmfTrace> hostTraces = new HashSet<>();
        for (ITmfTrace trace : experiment.getTraces()) {
            if (trace.getHostId().equals(hostId)) {
                hostTraces.add(trace);
            }
        }
        return hostTraces;
    }

    /**
     * Get a specific analysis module from a specific host of an experiment. It
     * will return the first module with the given ID from the first trace of
     * the host that has such a module.
     *
     * @param experiment
     *            The experiment the host belongs to
     * @param hostId
     *            The ID of the host for which we want the specified analysis
     * @param analysisId
     *            The ID of the requested analysis
     * @return The requested analysis module or {@code null} if no module found
     */
    public static @Nullable IAnalysisModule getAnalysisModuleForHost(TmfExperiment experiment, String hostId, String analysisId) {
        for (ITmfTrace trace : getTracesFromHost(experiment, hostId)) {
            IAnalysisModule module = trace.getAnalysisModule(analysisId);
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    /**
     * Get an analysis module of a specific class from a specific host of an
     * experiment. It will return the first module of the given class from the
     * first trace of the host that has such a module.
     *
     * @param experiment
     *            The experiment the host belongs to
     * @param hostId
     *            The ID of the host for which we want the specified analysis
     * @param moduleClass
     *            The class of the analysis module to return
     * @return The first analysis module of the given class or {@code null} if
     *         no module found
     */
    public static @Nullable <T extends IAnalysisModule> T getAnalysisModuleOfClassForHost(TmfExperiment experiment, String hostId, Class<T> moduleClass) {
        for (ITmfTrace trace : getTracesFromHost(experiment, hostId)) {
            for (T module : TmfTraceUtils.getAnalysisModulesOfClass(trace, moduleClass)) {
                return module;
            }
        }
        return null;
    }

}
