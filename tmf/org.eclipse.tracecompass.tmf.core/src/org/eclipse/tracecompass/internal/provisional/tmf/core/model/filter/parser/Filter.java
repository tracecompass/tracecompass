/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Item filter runtime object
 *
 * @author Jean-Christian Kouame
 *
 */
public class Filter implements Predicate<Map<String, String>> {

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
    public boolean test(Map<String, String> data) {
        for (FilterExpression expression : fExpressions) {
            if (!expression.test(data)) {
                return false;
            }
        }
        return true;
    }

}
