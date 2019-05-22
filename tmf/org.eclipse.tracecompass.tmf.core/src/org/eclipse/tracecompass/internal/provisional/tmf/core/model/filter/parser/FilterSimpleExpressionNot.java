/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterSimpleExpressionCu.ConditionOperator;

import com.google.common.collect.Multimap;

/**
 * This class represents a simple filter expression negation
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpressionNot extends FilterSimpleExpression {

    /**
     * Constructor
     *
     * @param field
     *            The field to look for
     * @param operator
     *            The operator to use for the test
     * @param value
     *            The value to test
     */
    public FilterSimpleExpressionNot(String field, ConditionOperator operator, @Nullable String value) {
        super(field, operator, value);
    }

    @Override
    public boolean test(Multimap<String, Object> data) {
        return !super.test(data);
    }
}
