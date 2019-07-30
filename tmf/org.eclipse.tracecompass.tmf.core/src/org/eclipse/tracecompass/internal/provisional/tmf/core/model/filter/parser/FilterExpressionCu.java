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
import java.util.List;
import java.util.Objects;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser;

import com.google.common.collect.ImmutableList;

/**
 * This implements a filter expression compilation unit
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterExpressionCu implements IFilterCu {

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
    public static @Nullable FilterExpressionCu compile(CommonTree treeNode) {
        int childCount = treeNode.getChildCount();

        // We can only have an odd number of parameter at this point
        if (childCount % 2 != 1) {
            return null;
        }

        List<Object> elements = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 0) {
                CommonTree nodeTree = (CommonTree) treeNode.getChild(i);
                int subChildCount = nodeTree.getChildCount();

                if (nodeTree.getType() == FilterParserParser.ROOT1) {
                    boolean negate = nodeTree.getChild(0).getText().equals(IFilterStrings.NOT);
                    // The logical part is the penultimate child
                    CommonTree logical = Objects.requireNonNull((CommonTree) nodeTree.getChild(subChildCount - 2));
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
                elements.add(Objects.requireNonNull(op));
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
        List<Object> queue = getElementsQueue();
        return new FilterExpression(queue);
    }

    /**
     * get the queue of operations this expression validation will do
     *
     * @return The queue of operation
     */
    protected final List<Object> getElementsQueue() {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        int count = fElements.size();

        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                Object element = fElements.get(i);
                if (element instanceof FilterSimpleExpressionCu) {

                    FilterSimpleExpression node = ((FilterSimpleExpressionCu) element).generate();

                    builder.add(node);
                } else if (element instanceof FilterExpressionCu) {

                    FilterExpression node = ((FilterExpressionCu) element).generate();

                    builder.add(node);

                } else {
                    throw new IllegalStateException("Unknown element while getting the filter element queue"); //$NON-NLS-1$
                }
            } else {
                Object op = fElements.get(i);
                if (!(op instanceof String)) {
                    throw new IllegalStateException("Element at position " + i + " should be a String"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                builder.add(op);
            }
        }
        return builder.build();
    }

    @Override
    public ITmfFilterTreeNode getEventFilter(ITmfTrace trace) {
        int count = fElements.size();
        ITmfFilterTreeNode prevNode = null;
        String operator = IFilterStrings.OR;

        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                Object element = fElements.get(i);
                if (element instanceof IFilterCu) {

                    ITmfFilterTreeNode node = ((IFilterCu) element).getEventFilter(trace);
                    prevNode = mergeEventFilters(prevNode, operator, node);
                } else {
                    throw new IllegalStateException("Unknown element while getting the filter element queue"); //$NON-NLS-1$
                }
            } else {
                Object op = fElements.get(i);
                if (!(op instanceof String)) {
                    throw new IllegalStateException("Element at position " + i + " should be a String"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                operator = (String) op;
            }
        }
        if (prevNode == null) {
            throw new NullPointerException("The filter should not be null at this point"); //$NON-NLS-1$
        }
        if (getNot()) {
            setFilterNot(prevNode);
        }
        return prevNode;
    }

    private static void setFilterNot(ITmfFilterTreeNode node) {
        // TODO: Ideally all those classes would have a common parent class, but
        // that would be API breaking
        if (node instanceof TmfFilterOrNode) {
            ((TmfFilterOrNode) node).setNot(true);
        }
        if (node instanceof TmfFilterAndNode) {
            ((TmfFilterAndNode) node).setNot(true);
        }
        if (node instanceof TmfFilterContainsNode) {
            ((TmfFilterContainsNode) node).setNot(true);
        }
        if (node instanceof TmfFilterEqualsNode) {
            ((TmfFilterEqualsNode) node).setNot(true);
        }
        if (node instanceof TmfFilterCompareNode) {
            ((TmfFilterCompareNode) node).setNot(true);
        }
        if (node instanceof TmfFilterMatchesNode) {
            ((TmfFilterMatchesNode) node).setNot(true);
        }
    }

    private ITmfFilterTreeNode mergeEventFilters(@Nullable ITmfFilterTreeNode prevNode, String operator, ITmfFilterTreeNode node) {
        if (prevNode == null) {
            return node;
        }
        TmfFilterTreeNode parentNode;
        if (operator == IFilterStrings.OR) {
            TmfFilterOrNode orNode = new TmfFilterOrNode(null);
            orNode.setNot(getNot());
            parentNode = orNode;
        } else {
            TmfFilterAndNode andNode = new TmfFilterAndNode(null);
            andNode.setNot(getNot());
            parentNode = andNode;
        }
        parentNode.addChild(prevNode);
        parentNode.addChild(node);

        return parentNode;
    }

    /**
     * Get whether this Cu expression is a negation
     *
     * @return <code>true</code> if the expression is a negation
     */
    protected boolean getNot() {
        return false;
    }

}
