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
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * An entry in the Control Flow view
 *
 * @author Geneviève Bastien
 */
public class CriticalPathEntry extends TimeGraphEntry {

    private final IGraphWorker fWorker;

    /**
     * Constructor
     *
     * @param taskname
     *            Name of the task
     * @param trace
     *            The trace on which we are working
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     * @param worker
     *            The worker object of this entry
     */
    public CriticalPathEntry(String taskname, ITmfTrace trace, long startTime, long endTime, IGraphWorker worker) {
        super(taskname, startTime, endTime);
        fWorker = worker;
    }

    /**
     * Get the worker object associated with the entry
     *
     * @return The worker object
     */
    public IGraphWorker getWorker() {
        return fWorker;
    }

}
