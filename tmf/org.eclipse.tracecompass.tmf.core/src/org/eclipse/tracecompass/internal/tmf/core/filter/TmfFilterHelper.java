/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.IFilterStrings;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAspectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Helper class to convert to/from {@link ITmfFilter} and filter regexes
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public final class TmfFilterHelper {

    private static final String SPACE = " "; //$NON-NLS-1$
    private static final String QUOTE = "\""; //$NON-NLS-1$

    private TmfFilterHelper() {
        // nothing to do
    }

    /**
     * Build an event filter from the regex string in parameter
     *
     * @param regexes
     *            The filter regex
     * @param trace
     *            The trace this filter applies to
     * @return An event filter
     */
    public static @Nullable ITmfFilter buildFilterFromRegex(Collection<String> regexes, ITmfTrace trace) {
        FilterCu compile = FilterCu.compile(IFilterStrings.mergeFilters(regexes));
        if (compile == null) {
            Activator.logInfo("buildFilterFromRegex: Invalid regex"); //$NON-NLS-1$
            return null;
        }
        return compile.getEventFilter(trace);
    }

    /**
     * Get the regex that corresponds to this filter. The regex should be in the
     * filter language described in the
     * {@link org.eclipse.tracecompass.tmf.filter.parser} plugin. And as it may
     * be used to filter anything, so it may not be the direct string
     * representing of the original filter. For instance, a ITmfFilter specific
     * for events will do a smart conversion, so that the parameters of the
     * filter do not match only events, but are generic enough to match any data
     * source.
     *
     * The default implementation of this method returns a regex that will
     * return true at all time.
     *
     * @param filter
     *            The filter object to convert to regex
     *
     * @return The regex String, using the
     *         {@link org.eclipse.tracecompass.tmf.filter.parser} syntax
     * @since 4.1
     */
    public static String getRegexFromFilter(ITmfFilter filter) {
        if (filter instanceof TmfFilterRootNode) {
            return getRegexFromRootNode((TmfFilterRootNode) filter);
        }
        if (filter instanceof TmfFilterAndNode) {
            return getRegexFromAndNode((TmfFilterAndNode) filter);
        }
        if (filter instanceof TmfFilterOrNode) {
            return getRegexFromOrNode((TmfFilterOrNode) filter);
        }
        if (filter instanceof TmfFilterCompareNode) {
            return getRegexFromCompareNode((TmfFilterCompareNode) filter);
        }
        if (filter instanceof TmfFilterContainsNode) {
            return getRegexFromContainsNode((TmfFilterContainsNode) filter);
        }
        if (filter instanceof TmfFilterEqualsNode) {
            return getRegexFromEqualsNode((TmfFilterEqualsNode) filter);
        }
        if (filter instanceof TmfFilterMatchesNode) {
            return getRegexFromMatchesNode((TmfFilterMatchesNode) filter);
        }
        if (filter instanceof TmfFilterNode) {
            return joinChildrenRegex((TmfFilterNode) filter, IFilterStrings.AND);
        }
        // Return an empty regex by default
        return ""; //$NON-NLS-1$
    }

    private static String getRegexFromMatchesNode(TmfFilterMatchesNode filter) {
        // Special case if the filter value is a wildcard, maps to present regex
        if (filter.getRegex().equals(".*")) { //$NON-NLS-1$
            String regex = quote(getFilterAspectName(filter)) + SPACE + IFilterStrings.PRESENT;
            return filter.isNot() ? not(regex) : regex;
        }
        String regex = quote(getFilterAspectName(filter)) + SPACE + IFilterStrings.MATCHES + SPACE + quote(filter.getRegex());
        return filter.isNot() ? not(regex) : regex;
    }

    private static String getRegexFromEqualsNode(TmfFilterEqualsNode filter) {
        return quote(getFilterAspectName(filter)) + SPACE + (filter.isNot() ? IFilterStrings.NOT_EQUAL : IFilterStrings.EQUAL) + SPACE + quote(filter.getValue());
    }

    private static String getRegexFromContainsNode(TmfFilterContainsNode filter) {
        String regex = quote(getFilterAspectName(filter)) + SPACE + IFilterStrings.CONTAINS + SPACE + quote(filter.getValue());
        return filter.isNot() ? not(regex) : regex;
    }

    private static String getRegexFromCompareNode(TmfFilterCompareNode filter) {
        String regex = quote(getFilterAspectName(filter)) + SPACE + getCompareSign(filter.getResult()) + SPACE + quote(filter.getValue());
        return filter.isNot() ? not(regex) : regex;
    }

    private static String not(String string) {
        return "!(" + string + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String quote(@Nullable String string) {
        if (string == null) {
            throw new NullPointerException("unexpected null string in the filter"); //$NON-NLS-1$
        }
        return QUOTE + string + QUOTE;
    }

    private static String getCompareSign(int result) {
        if (result < 0) {
            return IFilterStrings.LT;
        }
        if (result == 0) {
            return IFilterStrings.EQUAL;
        }
        return IFilterStrings.GT;
    }

    private static String getFilterAspectName(TmfFilterAspectNode filter) {
        ITmfEventAspect<?> aspect = filter.getEventAspect();
        // The event filter is on the content, just use a wildcard for field
        if (aspect == TmfBaseAspects.getContentsAspect()) {
            return IFilterStrings.WILDCARD;
        }
        String name = aspect.getName();
        if (aspect instanceof TmfEventFieldAspect) {
            TmfEventFieldAspect eventAspect = (TmfEventFieldAspect) aspect;
            String fieldPath = eventAspect.getFieldPath();
            if (fieldPath != null) {
                name = fieldPath;
            } else {
                // Could be an empty field aspect, so if the name if the same as
                // the content aspect, return a wildcard
                if (eventAspect.getName().equals(TmfBaseAspects.getContentsAspect().getName())) {
                    return IFilterStrings.WILDCARD;
                }
            }
        }
        // Keep only the last element of the path, after the last slash
        int slashPos = name.lastIndexOf("/"); //$NON-NLS-1$
        return slashPos < 0 ? name : name.substring(slashPos);
    }

    private static String getRegexFromOrNode(TmfFilterOrNode filter) {
        String regex = joinChildrenRegex(filter, IFilterStrings.OR);
        return filter.isNot() ? not(regex) : regex;
    }

    private static String getRegexFromAndNode(TmfFilterAndNode filter) {
        String regex = joinChildrenRegex(filter, IFilterStrings.AND);
        return filter.isNot() ? not(regex) : regex;
    }

    private static String getRegexFromRootNode(TmfFilterRootNode filter) {
        return joinChildrenRegex(filter, IFilterStrings.AND);
    }

    private static String joinChildrenRegex(TmfFilterTreeNode filter, String joinString) {
        List<String> regexes = new ArrayList<>();
        for (ITmfFilter childFilter : filter.getChildren()) {
            String regex = getRegexFromFilter(childFilter);
            if (!regex.isEmpty()) {
                regexes.add(regex);
            }
        }
        return Objects.requireNonNull(StringUtils.join(regexes, SPACE + joinString + SPACE));
    }

}
