/*******************************************************************************
* Copyright (c) 2018, 2019 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License 2.0 which
* accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterSimpleExpressionCu.ConditionOperator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * This class represents a simple filter expression
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpression implements Predicate<Multimap<String, Object>> {

    private final String fField;
    private final BiPredicate<Object, Object> fOperator;
    private final @Nullable String fOriginalValue;
    private final @Nullable Object fValue;

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
    public FilterSimpleExpression(String field, ConditionOperator operator, @Nullable String value) {
        fField = field;
        fOperator = operator;
        fOriginalValue = value;
        fValue = ConditionOperator.prepareValue(operator, value);
    }

    @Override
    public boolean test(Multimap<String, Object> data) {
        Object value = fValue;
        return Iterables.any(data.entries(), entry -> (fField.equals("*") ||  //$NON-NLS-1$
                Objects.requireNonNull(entry.getKey()).equals(fField) ||
                Objects.requireNonNull(entry.getKey()).equals("> " + fField)) &&  //$NON-NLS-1$
                (value == null || fOperator.test(entry.getValue(), value)));
    }

    @Override
    public String toString() {
        return fField + fOperator + fOriginalValue;
    }

}
