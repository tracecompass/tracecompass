/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

import com.google.common.collect.Iterables;

/**
 * Multiple aspect aggregator
 *
 * @param <T>
 *            return type
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 *
 * @since 3.1
 */
public class MultiAspect<T> implements ITmfEventAspect<T> {

    private static final Logger LOGGER = TraceCompassLog.getLogger(MultiAspect.class);

    private final String fName;
    private final Iterable<ITmfEventAspect<?>> fAspects;

    /**
     * Factory method for building a multi aspect.
     *
     * @param aspects
     *            Aspects sharing the same type as the "aspectClass" parameter
     * @param aspectClass
     *            Class of the aspects to aggregate
     * @return a MultiAspect or another ITmfEventAspect
     */
    public static @Nullable ITmfEventAspect<?> create(Iterable<ITmfEventAspect<?>> aspects, Class<?> aspectClass) {
        int size = Iterables.size(aspects);
        if (size == 0 || size == 1) {
            // Nothing to aggregate if there is no aspects or one unique aspect
            return Iterables.getFirst(aspects, null);
        }

        Set<String> names = new HashSet<>();
        for (ITmfEventAspect<?> aspect : aspects) {
            // Ensure all aspects belong to the same class as the "aspectClass" parameter
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                names.add(aspect.getName());
            } else {
                throw new IllegalArgumentException("Aspects must belong to the same class as the \"aspectClass\" parameter."); //$NON-NLS-1$
            }
        }

        // Ensure all aspects of the same type share the same name
        if (names.size() != 1) {
            StringJoiner sj = new StringJoiner(", "); //$NON-NLS-1$
            names.forEach(sj::add);
            TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "Aspects do not have the same name: ", sj.toString()); //$NON-NLS-1$ );
        }

        return new MultiAspect<>(Iterables.get(names, 0), aspects);
    }

    private MultiAspect(String name, Iterable<ITmfEventAspect<?>> aspects) {
        fName = name;
        fAspects = aspects;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public String getHelpText() {
        return Iterables.get(fAspects, 0).getHelpText();
    }

    @Override
    public @Nullable T resolve(ITmfEvent event) {
        for (ITmfEventAspect<?> aspect : fAspects) {
            Object resolve = aspect.resolve(event);
            if (resolve != null) {
                return (T) resolve;
            }
        }
        return null;
    }

}