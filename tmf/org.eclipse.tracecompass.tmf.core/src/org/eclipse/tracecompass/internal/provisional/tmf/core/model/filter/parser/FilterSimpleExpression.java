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
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.google.common.collect.Iterables;

/**
 * This class represents a simple filter expression
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpression implements Predicate<Map<String, String>> {

    private String fField;
    private BiPredicate<String, String> fOperator;
    private String fValue;

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
    public FilterSimpleExpression(String field, BiPredicate<String, String> operator, String value) {
        fField = field;
        fOperator = operator;
        fValue = value;
    }

    @Override
    public boolean test(Map<String, String> data) {
        return Iterables.any(data.entrySet(), entry -> (fField.equals("*") || entry.getKey().equals(fField) || entry.getKey().equals("> " + fField)) && fOperator.test(entry.getValue(), fValue)); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
