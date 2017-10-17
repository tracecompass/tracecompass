/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;

/**
 * This class represents an entry in the tree viewer of the UST memory usage
 * View.
 *
 * @author Loic Prieur-Drevon
 */
public class MemoryUsageEntry extends TmfGenericTreeEntry<MemoryUsageTreeModel> {

    /**
     * Constructor
     *
     * @param model
     *            The data provider model
     */
    public MemoryUsageEntry(MemoryUsageTreeModel model) {
        super(model);
    }

}
