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

import java.util.function.Predicate;

import com.google.common.collect.Multimap;

/**
 * Item filter runtime object
 *
 * @author Jean-Christian Kouame
 *
 */
public class Filter implements Predicate<Multimap<String, Object>> {

    private Iterable<FilterExpression> fExpressions;

    /**
     * Constructor
     *
     * @param expressions
     *            The list of filter expression to test
     */
    public Filter(Iterable<FilterExpression> expressions) {
        fExpressions = expressions;
    }

    @Override
    public boolean test(Multimap<String, Object> data) {
        for (FilterExpression expression : fExpressions) {
            if (!expression.test(data)) {
                return false;
            }
        }
        return true;
    }

}
