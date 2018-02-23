/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a function call in a certain level in the call stack.
 * It's used to build an aggregation segment tree (aggregated by depth and
 * callers). Per example,the two calls to the function A() in the call graph
 * below will be combined into one node in the generated tree:
 *
 * <pre>
 *   (Depth=0)      main              main
 *               ↓↑  ↓↑   ↓↑    =>   ↓↑   ↓↑
 *   (Depth=1)  A()  B()  A()       A()   B()
 * </pre>
 *
 * @author Sonia Farrah
 *
 */
public class AggregatedCalledFunction implements Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Object fSymbol;
    private final int fDepth;
    private final int fMaxDepth;
    private final Map<Object, AggregatedCalledFunction> fChildren = new HashMap<>();
    private final @Nullable AggregatedCalledFunction fParent;
    private final AggregatedCalledFunctionStatistics fStatistics;
    private long fDuration;
    private long fSelfTime;
    private final int fProcessId;

    /**
     * Constructor, parent is not null
     *
     * @param calledFunction
     *            called function
     * @param parent
     *            the parent entry
     *
     */
    public AggregatedCalledFunction(AbstractCalledFunction calledFunction, AggregatedCalledFunction parent) {
        fSymbol = calledFunction.getSymbol();
        fDuration = calledFunction.getLength();
        fSelfTime = calledFunction.getLength();
        fDepth = calledFunction.getDepth();
        fProcessId = calledFunction.getProcessId();
        fMaxDepth = parent.getMaxDepth();
        fParent = parent;
        fStatistics = new AggregatedCalledFunctionStatistics();
    }

    /**
     * Root constructor, parent is null
     *
     * @param calledFunction
     *            the called function
     * @param maxDepth
     *            the maximum depth
     */
    public AggregatedCalledFunction(AbstractCalledFunction calledFunction, int maxDepth) {
        fSymbol = calledFunction.getSymbol();
        fDuration = calledFunction.getLength();
        fSelfTime = calledFunction.getLength();
        fDepth = calledFunction.getDepth();
        fProcessId = calledFunction.getProcessId();
        fMaxDepth = maxDepth;
        fParent = null;
        fStatistics = new AggregatedCalledFunctionStatistics();
    }

    /**
     * copy constructor, used by the clone method
     *
     * @param toCopy Object to copy
     */
    private AggregatedCalledFunction(AggregatedCalledFunction toCopy) {
        fSymbol = toCopy.fSymbol;
        for (Entry<Object, AggregatedCalledFunction> entry : toCopy.fChildren.entrySet()) {
            fChildren.put(entry.getKey(), entry.getValue().clone());
        }
        fParent = toCopy.fParent;
        fMaxDepth = toCopy.fMaxDepth;
        fDepth = toCopy.fDepth;
        fStatistics = new AggregatedCalledFunctionStatistics();
        fStatistics.merge(toCopy.fStatistics);
        fProcessId = toCopy.fProcessId;
        fDuration = toCopy.fDuration;
        fSelfTime = toCopy.fSelfTime;
    }

    /**
     * The function's symbol (address or name)
     *
     * @return The function's symbol
     */
    public Object getSymbol() {
        return fSymbol;
    }

    /**
     * The callees of the function
     *
     * @return The function's callees
     */
    public synchronized Collection<AggregatedCalledFunction> getChildren() {
        return fChildren.values();
    }

    /**
     * The function's caller
     *
     * @return The caller of a function
     */
    public @Nullable AggregatedCalledFunction getParent() {
        return fParent;
    }

    /**
     * Add a new callee into the Callees list. If the function exists in the
     * callees list, the new callee's duration will be added to its duration and
     * it'll combine their callees.
     *
     * @param child
     *            The callee to add to this function
     * @param aggregatedChild
     *            The aggregated data of the callee
     */
    public synchronized void addChild(AbstractCalledFunction child, AggregatedCalledFunction aggregatedChild) {
        // Update the child's statistics with itself
        fSelfTime -= aggregatedChild.getDuration();
        aggregatedChild.getFunctionStatistics().update(child);
        AggregatedCalledFunction node = fChildren.get(aggregatedChild.getSymbol());
        if (node == null) {
            fChildren.put(aggregatedChild.getSymbol(), aggregatedChild);
        } else {
            merge(node, aggregatedChild);
        }
    }

    @Override
    public @NonNull AggregatedCalledFunction clone() {
        // We use a constructor instead of super.clone, otherwise some fields cannot be
        // final
        return new AggregatedCalledFunction(this);
    }

    /**
     * Modify the function's duration
     *
     * @param duration
     *            The amount to increment the duration by
     */
    private void addToDuration(long duration) {
        fDuration += duration;
    }

    /**
     * Merge the callees of two functions.
     *
     * @param firstNode
     *            The first parent secondNode The second parent
     */
    private static void mergeChildren(AggregatedCalledFunction firstNode, AggregatedCalledFunction secondNode) {
        for (Map.Entry<Object, AggregatedCalledFunction> functionEntry : secondNode.fChildren.entrySet()) {
            Object childSymbol = Objects.requireNonNull(functionEntry.getKey());
            AggregatedCalledFunction secondNodeChild = Objects.requireNonNull(functionEntry.getValue());
            AggregatedCalledFunction aggregatedCalledFunction = firstNode.fChildren.get(childSymbol);
            if (aggregatedCalledFunction == null) {
                firstNode.fChildren.put(secondNodeChild.getSymbol(), secondNodeChild);
            } else {
                // combine children
                merge(aggregatedCalledFunction, secondNodeChild);
            }
        }
    }

    /**
     * Merge two functions, add durations, self times, increment the calls,
     * update statistics and merge children.
     *
     * @param destination
     *            the node to merge to
     * @param source
     *            the node to merge
     */
    private static void merge(AggregatedCalledFunction destination, AggregatedCalledFunction source) {
        destination.addToDuration(source.getDuration());
        destination.addToSelfTime(source.getSelfTime());
        destination.getFunctionStatistics().merge(source.getFunctionStatistics());
        // merge the children callees.
        mergeChildren(destination, source);
    }

    /**
     * The function's duration
     *
     * @return The duration of the function
     */
    public long getDuration() {
        return fDuration;
    }

    /**
     * The function's depth
     *
     * @return The depth of the function
     */
    public int getDepth() {
        return fDepth;
    }

    /**
     * The depth of the aggregated tree
     *
     * @return The depth of the aggregated tree
     */
    public int getMaxDepth() {
        return fMaxDepth;
    }

    /**
     * The number of calls of a function
     *
     * @return The number of calls of a function
     */
    public long getNbCalls() {
        return fStatistics.getDurationStatistics().getNbElements();
    }

    /**
     * The self time of an aggregated function
     *
     * @return The self time
     */
    public long getSelfTime() {
        return fSelfTime;
    }

    /**
     * Add to the self time of an aggregated function
     *
     * @param selfTime
     *            The amount of self time to add
     */
    public void addToSelfTime(long selfTime) {
        fSelfTime += selfTime;
    }

    /**
     * The process ID of the trace application.
     * @return The process Id
     */
    public int getProcessId() {
        return fProcessId;
    }

    /**
     * Returns whether the function has callees.
     *
     * @return Boolean
     */
    public boolean hasChildren() {
        return !fChildren.isEmpty();
    }

    /**
     * The function's statistics
     *
     * @return The function's statistics
     */
    public AggregatedCalledFunctionStatistics getFunctionStatistics() {
        return fStatistics;
    }

    @Override
    public String toString() {
        return "Aggregate Function: " + getSymbol() + ", Duration: " + getDuration() + ", Self Time: " + fSelfTime + " on " + getNbCalls() + " calls"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
}
