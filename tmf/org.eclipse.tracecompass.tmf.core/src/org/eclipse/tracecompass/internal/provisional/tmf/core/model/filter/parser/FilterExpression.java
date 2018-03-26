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
 * This class implement a filter expression that could be tested against an
 * input
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpression implements Predicate<Map<String, String>> {

    private FilterSimpleExpression fLeftOperand;
    private FilterSimpleExpression fRightOperand;
    private String fLogicalOperator;

    /**
     * Constructor
     *
     * @param leftOperand
     *            The left operand
     * @param logicalOp
     *            The logical operation
     * @param rightOperand
     *            The right operand
     */
    public FilterExpression(FilterSimpleExpression leftOperand, String logicalOp, FilterSimpleExpression rightOperand) {
        fLeftOperand = leftOperand;
        fRightOperand = rightOperand;
        fLogicalOperator = logicalOp;
    }

    @Override
    public boolean  test(Map<String, String> data) {
        boolean result = fLeftOperand.test(data);

        if (fLogicalOperator != null && !fLogicalOperator.isEmpty()) {
            if (fLogicalOperator.equals(IFilterStrings.OR)) {
                result = result || fRightOperand.test(data);
            } else if (fLogicalOperator.equals(IFilterStrings.AND) && fRightOperand != null) {
                result = result && fRightOperand.test(data);
            }
        }

        return result;
    }
}
