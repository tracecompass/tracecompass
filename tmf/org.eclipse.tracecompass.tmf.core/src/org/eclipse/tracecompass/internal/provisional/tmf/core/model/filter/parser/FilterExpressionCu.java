/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * This implements a filter expression compilation unit
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpressionCu {

    private FilterSimpleExpressionCu fLeftExpr;
    private String fLogicalOp;
    private FilterSimpleExpressionCu fRightExpr;

    /**
     * Constructor
     *
     * @param leftExpr
     *            The left filter expression
     * @param logicalOp
     *            The logical operator
     * @param rightExpr
     *            The right filter expression
     */
    public FilterExpressionCu(FilterSimpleExpressionCu leftExpr, String logicalOp, FilterSimpleExpressionCu rightExpr) {
        fLeftExpr = leftExpr;
        fLogicalOp = logicalOp;
        fRightExpr = rightExpr;
    }

    /**
     * Compile a filter expression from a tree
     *
     * @param treeNode
     *            The root node used to build this compilation unit
     * @return a filter expression compilation unit
     */
    public static FilterExpressionCu compile(CommonTree treeNode) {
        int childCount = treeNode.getChildCount();
        CommonTree leftTree = childCount > 0 ? (CommonTree) treeNode.getChild(0) : null;
        if (leftTree == null) {
            Activator.logError("At least one expression is needed", new IllegalArgumentException("Invalid time event filter")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        FilterSimpleExpressionCu left = FilterSimpleExpressionCu.compile(leftTree);
        if (left == null) {
            return null;
        }

        CommonTree opTree = childCount > 1 ? (CommonTree) treeNode.getChild(1) : null;
        String op = opTree != null ? opTree.getText() : null;

        CommonTree rightTree = childCount > 2 ? (CommonTree) treeNode.getChild(2) : null;
        FilterSimpleExpressionCu right = rightTree != null ? FilterSimpleExpressionCu.compile(rightTree) : null;

        return new FilterExpressionCu(left, op, right);
    }

    /**
     * Generate a filter expression runtime object
     *
     * @return a filter expression
     */
    public FilterExpression generate() {
        FilterSimpleExpression leftOperand = fLeftExpr.generate();
        FilterSimpleExpression rightOperand = fLogicalOp != null && fRightExpr != null ? fRightExpr.generate() : null;
        return new FilterExpression(leftOperand, fLogicalOp, rightOperand);
    }
}
