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

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Compilation unit for a simple filter expression negation
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpressionNotCu extends FilterSimpleExpressionCu {

    /**
     * Constructor
     *
     * @param field
     *            The field to look for
     * @param op
     *            The operator to use for the test
     * @param value
     *            The value to to test
     */
    public FilterSimpleExpressionNotCu(String field, String op, @Nullable String value) {
        super(field, op, value);
    }

    public static @Nullable FilterSimpleExpressionNotCu compile(CommonTree tree) {
        FilterSimpleExpressionCu cu = FilterSimpleExpressionCu.compile(tree);
        if (cu == null) {
            return null;
        }
        return new FilterSimpleExpressionNotCu(cu.getField(), cu.getOperator(), cu.getValue());
    }

    @Override
    public FilterSimpleExpression generate() {
        return new FilterSimpleExpressionNot(getField(), getConditionOperator(getOperator()), getValue());
    }

    @Override
    protected boolean getNot() {
        return true;
    }

}
