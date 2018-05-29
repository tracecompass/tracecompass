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
import java.util.Queue;
import java.util.function.Predicate;

/**
 * This class implement a filter expression that could be tested against an
 * input
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpression implements Predicate<Map<String, String>> {

    private Queue<Object> fElements;

    /**
     * Constructor
     *
     * @param elements
     *            The list of element representing this experession
     *
     */
    public FilterExpression(Queue<Object> elements) {
        fElements = elements;
    }

    @Override
    public boolean test(Map<String, String> data) {

        if (fElements == null || fElements.isEmpty()) {
            return false;
        }

        int index = 0;
        boolean result = false;
        String operator = IFilterStrings.OR;
        while (!fElements.isEmpty()) {
            Object element = fElements.poll();

            if (index % 2 == 0) {
                if (element instanceof FilterSimpleExpression) {
                    FilterSimpleExpression expression = (FilterSimpleExpression) element;
                    result = handleOperator(result, operator, expression.test(data));
                } else if (element instanceof FilterExpression) {
                    FilterExpression expression = (FilterExpression) element;
                    result = handleOperator(result, operator, expression.test(data));
                } else {
                    return false;
                }
            } else {
                if (!(element instanceof String)) {
                    return false;
                }
                operator = (String) element;
            }
            index++;
        }
        return result;
    }

    private static boolean handleOperator(boolean left, String operator, boolean right) {
        return operator.equals(IFilterStrings.OR) ? (left || right) : (left && right);
    }
}
