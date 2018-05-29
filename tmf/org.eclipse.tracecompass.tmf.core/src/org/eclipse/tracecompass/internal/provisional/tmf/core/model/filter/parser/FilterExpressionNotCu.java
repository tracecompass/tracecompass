/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.List;
import java.util.Queue;

import org.antlr.runtime.tree.CommonTree;

/**
 * This implements a filter expression compilation unit negation
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpressionNotCu extends FilterExpressionCu {

    /**
     * Constructor
     *
     * @param elements
     *            The list of elements representing the logical expression
     */
    public FilterExpressionNotCu(List<Object> elements) {
        super(elements);
    }

    public static FilterExpressionNotCu compile(CommonTree treeNode) {
        FilterExpressionCu cu = FilterExpressionCu.compile(treeNode);
        return new FilterExpressionNotCu(cu.getElement());
    }
    @Override
    public FilterExpression generate() {
        Queue<Object> queue = getElementsQueue();
        return new FilterExpressionNot(queue);
    }
}
