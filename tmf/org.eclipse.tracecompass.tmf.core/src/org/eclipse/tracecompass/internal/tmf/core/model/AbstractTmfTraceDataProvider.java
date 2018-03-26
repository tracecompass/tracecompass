/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.IFilterStrings;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.TimeGraphStateQueryFilter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Every concrete data provider which relies on a trace is highly recommended to
 * extend this class. Instead of duplicating the trace as a property in every
 * data provider, this class is intended to limit code duplication.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public abstract class AbstractTmfTraceDataProvider {

    /** The trace that will be used by data providers */
    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param trace
     *            A trace that will be used to perform analysis
     */
    public AbstractTmfTraceDataProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * Gets the trace that is encapsulated by this provider
     *
     * @return An {@link ITmfTrace} instance
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Compute the predicate for every property regexes
     *
     * @param queryFilter
     *            The query filter holding the regexes
     * @return A map of time event filters predicate by property
     */
    protected Map<Integer, Predicate<@NonNull Map<@NonNull String, @NonNull String>>> computeRegexPredicate(TimeGraphStateQueryFilter queryFilter) {
        Multimap<@NonNull Integer, @NonNull String> regexes = queryFilter.getRegexes();
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Map<@NonNull String, @NonNull String>>> predicates = new HashMap<>();
        for (Map.Entry<Integer, Collection<String>> entry : regexes.asMap().entrySet()) {
            String regex = Joiner.on(IFilterStrings.AND).skipNulls().join(Iterables.filter(Objects.requireNonNull(entry.getValue()), s -> !s.isEmpty())); //$NON-NLS-1$
            FilterCu cu = FilterCu.compile(regex);
            Predicate<@NonNull Map<@NonNull String, @NonNull String>> predicate = cu != null ? cu.generate() : null;
                if (predicate != null) {
                    predicates.put(entry.getKey(), predicate);
                }
        }
        return predicates;
    }
}
