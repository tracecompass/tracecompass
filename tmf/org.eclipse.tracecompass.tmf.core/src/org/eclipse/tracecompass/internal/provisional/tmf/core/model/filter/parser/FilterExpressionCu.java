/*******************************************************************************
* Copyright (c) 2018 Ericsson
*
* All rights reserved. This program and the accompanying materials are
* made available under the terms of the Eclipse Public License v1.0 which
* accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser;

/**
 * This implements a filter expression compilation unit
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpressionCu {

    private List<Object> fElements;

    /**
     * Constructor
     *
     * @param elements
     *            The list of elements representing the logical expression
     */
    public FilterExpressionCu(List<Object> elements) {
        fElements = elements;
    }

    /**
     * Get the list of elements that define this logical expression
     *
     * @return the list of elements
     */
    public List<Object> getElement() {
        return fElements;
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

        // We can only have an odd number of parameter at this point
        if (childCount % 2 != 1) {
            return null;
        }

        List<Object> elements = new ArrayList<>();
        for (int i = 0; i<childCount; i++) {
            if (i % 2 == 0) {
                CommonTree nodeTree = (CommonTree) treeNode.getChild(i);
                int subChildCount = nodeTree.getChildCount();

                if (nodeTree.getType() == FilterParserParser.ROOT1) {
                    boolean negate = nodeTree.getChild(0).getText().equals(IFilterStrings.NOT);
                    // The logical part is the penultimate child
                    CommonTree logical = (CommonTree) nodeTree.getChild(subChildCount - 2);
                    FilterExpressionCu cu = negate ? FilterExpressionNotCu.compile(logical) : FilterExpressionCu.compile(logical);

                    if (cu == null) {
                        return null;
                    }

                    elements.add(cu);

                } else if (nodeTree.getType() == FilterParserParser.ROOT2) {
                    FilterSimpleExpressionCu node = FilterSimpleExpressionCu.compile(nodeTree);
                    if (node == null) {
                        return null;
                    }

                    elements.add(node);
                }
            } else {
                CommonTree opTree = (CommonTree) treeNode.getChild(i);
                String op = opTree.getText();
                elements.add(op);
            }
        }

        return new FilterExpressionCu(elements);
    }

    /**
     * Generate a filter expression runtime object
     *
     * @return a filter expression
     */
    public FilterExpression generate() {
        Queue<Object> queue = getElementsQueue();
        return new FilterExpression(queue);
    }

    /**
     * get the queue of operations this expression validation will do
     *
     * @return The queue of operation
     */
    protected final Queue<Object> getElementsQueue() {
        Queue<Object> queue = new LinkedList<>();
        int count = fElements.size();

        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                Object element = fElements.get(i);
                if (element instanceof FilterSimpleExpressionCu) {

                    FilterSimpleExpression node = ((FilterSimpleExpressionCu) element).generate();
                    if (node == null) {
                        return null;
                    }

                    queue.offer(node);
                } else if (element instanceof FilterExpressionCu) {

                    FilterExpression node = ((FilterExpressionCu) element).generate();
                    if (node == null) {
                        return null;
                    }

                    queue.offer(node);

                } else {
                    return null;
                }
            } else {
                Object op = fElements.get(i);
                if (!(op instanceof String)) {
                    return null;
                }
                queue.offer(op);
            }
        }
        return queue;
    }
}
