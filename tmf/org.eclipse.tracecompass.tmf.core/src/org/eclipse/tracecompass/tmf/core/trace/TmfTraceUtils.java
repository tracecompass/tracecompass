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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Utility methods for ITmfTrace's.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public final class TmfTraceUtils {

    private static final int MAX_NB_BINARY_BYTES = 2048;

    private TmfTraceUtils() {
    }

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
    public static <T> Iterable<@NonNull T> getAnalysisModulesOfClass(ITmfTrace trace, Class<T> moduleClass) {
        Iterable<IAnalysisModule> analysisModules = trace.getAnalysisModules();
        Set<@NonNull T> modules = new HashSet<>();
        for (IAnalysisModule module : analysisModules) {
            if (moduleClass.isAssignableFrom(module.getClass())) {
                modules.add(checkNotNull(moduleClass.cast(module)));
            }
        }
        return modules;
    }

    /**
     * Return the first result of the first aspect that resolves as non null for
     * the event received in parameter. If the returned value is not null, it
     * can be safely cast to the aspect's class proper return type.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The class of the aspect(s) to resolve
     * @param event
     *            The event for which to get the aspect
     * @return The first result of the
     *         {@link ITmfEventAspect#resolve(ITmfEvent)} that returns non null
     *         for the event or {@code null} otherwise
     */
    public static @Nullable <T extends ITmfEventAspect> Object resolveEventAspectOfClassForEvent(
            ITmfTrace trace, Class<T> aspectClass, ITmfEvent event) {
        Iterable<ITmfEventAspect> aspects = trace.getEventAspects();
        for (ITmfEventAspect aspect : aspects) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                Object obj = aspect.resolve(event);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Return the first result of the first aspect that resolves as non null for
     * the event received in parameter. The result is cast to an Integer if
     * possible, otherwise null is returned.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The class of the aspect(s) to resolve
     * @param event
     *            The event for which to get the aspect
     * @return Integer of the first result of the
     *         {@link ITmfEventAspect#resolve(ITmfEvent)} that returns non null
     *         for the event or {@code null} otherwise
     * @since 2.0
     */
    public static @Nullable <T extends ITmfEventAspect> Integer resolveIntEventAspectOfClassForEvent(
            ITmfTrace trace, Class<T> aspectClass, ITmfEvent event) {
        Object result = resolveEventAspectOfClassForEvent(trace, aspectClass, event);
        if (result instanceof Integer) {
            return (Integer) result;
        }
        return null;
    }

    /**
     * Checks for text file.
     *
     * Note that it checks for binary value 0 in the first MAX_NB_BINARY_BYTES
     * bytes to determine if the file is text.
     *
     * @param file
     *            the file to check. Caller has to make sure that file exists.
     * @return true if it is binary else false
     * @throws IOException
     *             if IOException occurs
     * @since 2.0
     */
    public static boolean isText(File file) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            int count = 0;
            int val = bufferedInputStream.read();
            while ((count < MAX_NB_BINARY_BYTES) && (val >= 0)) {
                if (val == 0) {
                    return false;
                }
                count++;
                val = bufferedInputStream.read();
            }
        }
        return true;
    }
}
