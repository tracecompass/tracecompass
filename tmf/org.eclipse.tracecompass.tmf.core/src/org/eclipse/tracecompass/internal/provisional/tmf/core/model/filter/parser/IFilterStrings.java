/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.Collection;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * List of string used in the filter syntax
 *
 * @author Jean-Christian Kouame
 *
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface IFilterStrings {

    // OPERATORS
    static final String EQUAL = "==";
    static final String NOT_EQUAL = "!=";
    static final String MATCHES = "matches";
    static final String CONTAINS = "contains";
    static final String PRESENT = "present";
    static final String NOT = "!";
    static final String GT = ">";
    static final String LT = "<";

    static final String WILDCARD = "*";
    static final String OR = "||";
    static final String AND = "&&";
    static final String CLOSE_PARENTHESIS = ")";

    /**
     * Merge filter strings into one filter, joined with {@link #AND}.
     *
     * @param filters
     *            The individual regexes to join
     * @return A single string corresponding to the AND'ed filters
     */
    static String mergeFilters(Collection<String> filters) {
        return Objects.requireNonNull(
                Joiner.on(IFilterStrings.AND).skipNulls().join(Iterables.filter(filters, s -> !s.isEmpty())));
    }

}
