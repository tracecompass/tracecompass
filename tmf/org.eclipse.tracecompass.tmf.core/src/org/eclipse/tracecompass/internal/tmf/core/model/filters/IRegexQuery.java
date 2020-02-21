/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.model.filters;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Multimap;

/**
 * Interface for query filter with a regex predicate
 *
 * @author Jean-Christian
 *
 */
public interface IRegexQuery {
    /**
     * Get the regexes use to filter the queried data. It is a multimap of filter
     * strings by property. The data provider will use the filter strings to
     * determine whether the property should be activated or not.
     *
     * @return The multimap of regexes by property.
     */
    public Multimap<@NonNull Integer, @NonNull String> getRegexes();
}
