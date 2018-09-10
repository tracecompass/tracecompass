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
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Iterables;

/**
 * This class represents a simple filter expression
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpression implements Predicate<Map<String, String>> {

    private final String fField;
    private final BiPredicate<String, String> fOperator;
    private final @Nullable String fValue;

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
    public FilterSimpleExpression(String field, BiPredicate<String, String> operator, @Nullable String value) {
        fField = field;
        fOperator = operator;
        fValue = value;
    }

    @Override
    public boolean test(Map<String, String> data) {
        String value = fValue;
        return Iterables.any(data.entrySet(), entry -> (fField.equals("*") ||  //$NON-NLS-1$
                Objects.requireNonNull(entry.getKey()).equals(fField) ||
                Objects.requireNonNull(entry.getKey()).equals("> " + fField)) &&  //$NON-NLS-1$
                (value == null || fOperator.test(entry.getValue(), value)));
    }

}
