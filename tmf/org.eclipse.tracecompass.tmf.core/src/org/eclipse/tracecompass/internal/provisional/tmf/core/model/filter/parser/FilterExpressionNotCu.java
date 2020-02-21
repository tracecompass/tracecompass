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

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.Nullable;

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

    public static @Nullable FilterExpressionNotCu compile(CommonTree treeNode) {
        FilterExpressionCu cu = FilterExpressionCu.compile(treeNode);
        if (cu == null) {
            return null;
        }
        return new FilterExpressionNotCu(cu.getElement());
    }

    @Override
    public FilterExpression generate() {
        List<Object> queue = getElementsQueue();
        return new FilterExpressionNot(queue);
    }

    @Override
    protected boolean getNot() {
        return true;
    }

}
