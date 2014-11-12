/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Utility methods for ITmfTrace's.
 *
 * @author Alexandre Montplaisir
 */
public final class TmfTraceUtils {

    private TmfTraceUtils() {}

    /**
     * Get an analysis module belonging to this trace, with the specified ID and
     * class.
     *
     * @param trace
     *            The trace for which you want the modules
     * @param moduleClass
     *            Returned modules must extend this class
     * @param id
     *            The ID of the analysis module
     * @return The analysis module with specified class and ID, or null if no
     *         such module exists.
     */
    public static @Nullable <T extends IAnalysisModule> T getAnalysisModuleOfClass(ITmfTrace trace,
            Class<T> moduleClass, String id) {
        Iterable<T> modules = getAnalysisModulesOfClass(trace, moduleClass);
        for (T module : modules) {
            if (id.equals(module.getId())) {
                return module;
            }
        }
        return null;
    }

    /**
     * Return the analysis modules that are of a given class. Module will be
     * casted to the requested class.
     *
     * @param trace
     *            The trace for which you want the modules
     * @param moduleClass
     *            Returned modules must extend this class
     * @return List of modules of class moduleClass
     */
    public static @NonNull <T> Iterable<T> getAnalysisModulesOfClass(ITmfTrace trace, Class<T> moduleClass) {
        Iterable<IAnalysisModule> analysisModules = trace.getAnalysisModules();
        Set<T> modules = new HashSet<>();
        for  (IAnalysisModule module : analysisModules) {
            if (moduleClass.isAssignableFrom(module.getClass())) {
                modules.add(moduleClass.cast(module));
            }
        }
        return modules;
    }

    /**
     * Return the list of event aspects exposed by the given trace that match a
     * given class.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The returned aspects must be of this class (or a subtype)
     * @return The event aspects that match the given class
     */
    public static @NonNull <T extends ITmfEventAspect> Iterable<T> getEventAspectsOfClass(
            ITmfTrace trace, Class<T> aspectClass) {
        Iterable<ITmfEventAspect> aspects = trace.getEventAspects();
        List<T> ret = new LinkedList<>();
        for (ITmfEventAspect aspect : aspects) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                ret.add(aspectClass.cast(aspect));
            }
        }
        return ret;
    }
}
