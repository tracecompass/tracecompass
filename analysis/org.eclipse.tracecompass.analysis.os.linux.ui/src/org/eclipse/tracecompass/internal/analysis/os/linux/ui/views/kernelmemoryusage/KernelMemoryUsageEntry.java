/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageTreeModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;

/**
 * This class represents an entry in the tree viewer of the kernel memory usage
 * View.
 *
 * @author mahdi zolnouri
 */
public class KernelMemoryUsageEntry extends TmfGenericTreeEntry<KernelMemoryUsageTreeModel> {

    /**
     * Constructor
     *
     * @param model
     *            The data provider model
     */
    public KernelMemoryUsageEntry(@NonNull KernelMemoryUsageTreeModel model) {
        super(model);
    }
}
