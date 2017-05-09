/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * A time graph for the critical path to be used by the view itself to identify
 * the base worker from which to get the critical path.
 *
 * @author Geneviève Bastien
 */
public class CriticalPathBaseEntry extends TimeGraphEntry {

    private final IGraphWorker fWorker;
    private final CriticalPathModule fModule;

    /**
     * Constructor
     *
     * @param worker
     *            The worker associated with this entry
     * @param module
     *            The critical path module associated with this entry
     */
    public CriticalPathBaseEntry(IGraphWorker worker, CriticalPathModule module) {
        super("Base entry", Long.MIN_VALUE, Long.MAX_VALUE); //$NON-NLS-1$
        fWorker = worker;
        fModule = module;
    }

    /**
     * Get the worker associated with this entry
     *
     * @return The worker
     */
    public IGraphWorker getWorker() {
        return fWorker;
    }

    /**
     * Get the critical path module associated with this entry
     *
     * @return The critical path module
     */
    public CriticalPathModule getModule() {
        return fModule;
    }

}
