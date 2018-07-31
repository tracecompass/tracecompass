/*******************************************************************************
 * Copyright (c) 2017, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.base;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface that classes representing a single element in the callstack
 * hierarchy must implement. Typically, a {@link IProfilingElement} will be
 * associated with a {@link IProfilingGroupDescriptor}. It will have children
 * that will correspond to the next group in the hierarchy.
 *
 * The actual data of the various available analyses containing those elements
 * will be available only at the leaf elements.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public interface IProfilingElement {

    /**
     * Get the name of this element
     *
     * @return The name of the element
     */
    String getName();

    /**
     * Get the elements at the next level of the callstack hierarchy from this
     * element
     *
     * @return The list of children elements in the hierarchy
     */
    Collection<IProfilingElement> getChildren();

    /**
     * Add a child element to this one
     *
     * @param node
     */
    void addChild(IProfilingElement node);

    /**
     * Get the corresponding group descriptor
     *
     * @return The group descriptor of this element
     */
    IProfilingGroupDescriptor getGroup();

    /**
     * Get the next group descriptor
     *
     * @return The next group descriptor, or <code>null</code> if this is a leaf
     *         element
     */
    @Nullable IProfilingGroupDescriptor getNextGroup();

    /**
     * Get the key for symbol resolution at a given time
     *
     * @param time
     *            The time at which to get the symbol key
     * @return The symbol key at time
     */
    int getSymbolKeyAt(long time);

    /**
     * Set the symbol key element to use for this hierarchy
     *
     * @param element
     *            The symbol key element
     */
    void setSymbolKeyElement(IProfilingElement element);

    /**
     * Return whether this element is the symbol key element
     *
     * @return Whether the element is the symbol key
     */
    boolean isSymbolKeyElement();

    /**
     * Get the parent element, or <code>null</code> if this element corresponds to
     * the first group of the hierarchy
     *
     * @return The parent element
     */
    @Nullable IProfilingElement getParentElement();

    /**
     * Get whether this element is a leaf element in the callstack hierarchy. Leaf
     * elements are expected to contain the proper analysis data.
     *
     * @return Whether this element is a leaf, ie contains analysis data or not
     */
    boolean isLeaf();
}
