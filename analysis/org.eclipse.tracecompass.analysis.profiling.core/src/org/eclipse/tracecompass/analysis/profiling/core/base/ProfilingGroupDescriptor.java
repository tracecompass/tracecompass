/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.base;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A basic group descriptor implementation.
 *
 * @author Geneviève Bastien
 */
public class ProfilingGroupDescriptor implements IProfilingGroupDescriptor {

    private final String fName;
    private final @Nullable IProfilingGroupDescriptor fNextGroup;
    private final boolean fSymbolKeyGroup;

    /**
     * Constructor
     *
     * @param name
     *            The name of this group
     * @param nextGroup
     *            The next group of the hierarchy, ie the child of the group
     *            being constructed or <code>null</code> if this group is the
     *            leaf
     * @param isSymbolKeyGroup
     *            Whether this level contains the symbol key
     */
    public ProfilingGroupDescriptor(String name, @Nullable IProfilingGroupDescriptor nextGroup, boolean isSymbolKeyGroup) {
        fName = name;
        fNextGroup = nextGroup;
        fSymbolKeyGroup = isSymbolKeyGroup;
    }

    @Override
    public @Nullable IProfilingGroupDescriptor getNextGroup() {
        return fNextGroup;
    }

    @Override
    public boolean isSymbolKeyGroup() {
        return fSymbolKeyGroup;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public String toString() {
        return "CallStack Descriptor: " + getName(); //$NON-NLS-1$
    }

}
