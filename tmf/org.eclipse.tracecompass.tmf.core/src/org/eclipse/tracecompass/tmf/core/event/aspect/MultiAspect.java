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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
     * Factory method for building a {@link MultiAspect}.
     *
     * @param aspects
     *            Aspects sharing the same type as the {@code aspectClass}
     *            parameter
     * @param aspectClass
     *            {@link Class} of the aspects to aggregate
     * @return a {@link MultiAspect} or another {@link ITmfEventAspect}
     */
    public static @Nullable ITmfEventAspect<?> create(Iterable<ITmfEventAspect<?>> aspects, Class<?> aspectClass) {
        int size = Iterables.size(aspects);
        if (size == 0 || size == 1) {
            // Nothing to aggregate if there is no aspects or one unique aspect
            return Iterables.getFirst(aspects, null);
        }

        Set<String> names = new HashSet<>();
        for (ITmfEventAspect<?> aspect : aspects) {
            // Ensure all aspects belong to the same class as the "aspectClass"
            // parameter
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

    /**
     * Factory method for building a {@link MultiAspect} out of {@code existing}
     * ones. Cannot return {@code null} so it can be used by
     * {@link Map#computeIfPresent} or similar lambdas.
     *
     * @param existing
     *            The existing ({@link MultiAspect}, or not) aspect
     * @param toAdd
     *            The (non-{@link MultiAspect}) aspect to add to the
     *            {@code existing} one
     *
     * @return a {@link MultiAspect} or another {@link ITmfEventAspect};
     *         {@code existing} if unable to add
     * @since 8.1
     */
    public static ITmfEventAspect<?> createFrom(ITmfEventAspect<?> existing, ITmfEventAspect<?> toAdd) {
        if (toAdd instanceof MultiAspect) {
            // TODO: investigate support for potentially adding a multi.
            throw new IllegalArgumentException(String.format("\"%s\" as the to-add parameter cannot be a MultiAspect.", toAdd.getName())); //$NON-NLS-1$
        }
        List<ITmfEventAspect<?>> aspects = new ArrayList<>();
        ITmfEventAspect<?> subclassOwner;
        if (existing instanceof MultiAspect) { // >2 twins case, including toAdd
            MultiAspect<?> multi = (MultiAspect<?>) existing;
            for (ITmfEventAspect<?> each : multi.fAspects) {
                aspects.add(each);
            }
            subclassOwner = aspects.get(0); // same class for all
        } else {
            aspects.add(existing);
            subclassOwner = existing;
        }
        aspects.add(toAdd);
        Class<?> aspectClass = assignableFor(subclassOwner, toAdd);
        ITmfEventAspect<?> createdFrom = MultiAspect.create(aspects, aspectClass);
        if (createdFrom == null) {
            createdFrom = existing;
        }
        return createdFrom;
    }

    private static Class<?> assignableFor(ITmfEventAspect<?> subclassOwner, ITmfEventAspect<?> toAdd) {
        Class<?> candidate = subclassOwner.getClass();
        Class<?> superclass = candidate.getSuperclass();
        while (superclass != null && !candidate.isAssignableFrom(toAdd.getClass())) {
            candidate = superclass;
            superclass = candidate.getSuperclass();
        }
        return candidate;
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

    @SuppressWarnings("unchecked")
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
