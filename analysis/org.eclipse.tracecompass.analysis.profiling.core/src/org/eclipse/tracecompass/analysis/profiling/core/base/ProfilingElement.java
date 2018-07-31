/*******************************************************************************
 * Copyright (c) 2017, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.base;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A basic callstack element implementing the methods of the interface.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public class ProfilingElement implements IProfilingElement {

    /**
     * The default key to use for symbol resolution if none is available
     */
    public static final int DEFAULT_SYMBOL_KEY = -1;

    private final String fName;
    private final IProfilingGroupDescriptor fDescriptor;
    private final @Nullable IProfilingGroupDescriptor fNextDescriptor;
    private final Collection<IProfilingElement> fChildren = new ArrayList<>();
    private final @Nullable IProfilingElement fParent;
    private @Nullable IProfilingElement fSymbolKeyElement = null;

    /**
     * Constructor
     *
     * @param name
     *            The name of this element
     * @param descriptor
     *            The corresponding group descriptor
     */
    public ProfilingElement(String name, IProfilingGroupDescriptor descriptor) {
        this(name, descriptor, null, null);
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of this element
     * @param descriptor
     *            The corresponding group descriptor
     * @param nextGroup
     *            The next group descriptor
     * @param parent
     *            The parent element
     */
    public ProfilingElement(String name, IProfilingGroupDescriptor descriptor, @Nullable IProfilingGroupDescriptor nextGroup, @Nullable IProfilingElement parent) {
        fName = name;
        fDescriptor = descriptor;
        fParent = parent;
        fNextDescriptor = nextGroup;
        if (parent instanceof ProfilingElement) {
            fSymbolKeyElement = ((ProfilingElement) parent).fSymbolKeyElement;
        }
    }

    @Override
    public Collection<IProfilingElement> getChildren() {
        return fChildren;
    }

    @Override
    public void addChild(IProfilingElement node) {
        fChildren.add(node);
    }

    @Override
    public IProfilingGroupDescriptor getGroup() {
        return fDescriptor;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public boolean isLeaf() {
        return fNextDescriptor == null;
    }

    @Override
    public @Nullable IProfilingGroupDescriptor getNextGroup() {
        return fNextDescriptor;
    }

    @Override
    public void setSymbolKeyElement(IProfilingElement element) {
        fSymbolKeyElement = element;
    }

    @Override
    public boolean isSymbolKeyElement() {
        return fSymbolKeyElement == this;
    }

    @Override
    public final int getSymbolKeyAt(long startTime) {
        int processId = DEFAULT_SYMBOL_KEY;
        if (isSymbolKeyElement()) {
            return retrieveSymbolKeyAt(startTime);
        }
        IProfilingElement symbolKeyElement = fSymbolKeyElement;
        // if there is no symbol key element, return the default value
        if (symbolKeyElement == null) {
            return processId;
        }
        return symbolKeyElement.getSymbolKeyAt(startTime);
    }

    /**
     * Retrieve the symbol key for this element. This method is called by
     * {@link #getSymbolKeyAt(long)} when the current element is the symbol key. So
     * this method should assume the current is the symbol key provider and use its
     * own values to retrieve what the key to resolve symbols should be at the time
     * of the query.
     *
     * @param time
     *            The time at which to resolve the symbol
     * @return The symbol key at the requested time
     */
    protected int retrieveSymbolKeyAt(long time) {
        return DEFAULT_SYMBOL_KEY;
    }

    @Override
    public @Nullable IProfilingElement getParentElement() {
        return fParent;
    }

    @Override
    public String toString() {
        return "Element: " + getName() + '[' + fDescriptor + ']'; //$NON-NLS-1$
    }

}
