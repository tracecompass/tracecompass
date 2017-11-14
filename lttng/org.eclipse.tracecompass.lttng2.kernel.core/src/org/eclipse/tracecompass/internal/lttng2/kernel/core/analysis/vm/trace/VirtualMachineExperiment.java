/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Patrick Tasse - Fix experiment name
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace;

import java.util.Collections;
import java.util.Set;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * Experiment class containing traces from physical machine and the virtual
 * guests running on them.
 *
 * @author Mohamad Gebai
 *
 *         TODO The virtual machine analysis and experiment have moved to the
 *         Trace Compass incubator. Remove this class and all other related
 *         classes and packages that only this uses.
 */
public class VirtualMachineExperiment extends TmfExperiment {

    /**
     * Default constructor. Needed by the extension point.
     */
    public VirtualMachineExperiment() {
        this("", Collections.EMPTY_SET); //$NON-NLS-1$
    }

    /**
     * Constructor with traces and id
     *
     * @param id
     *            The ID of this experiment
     * @param traces
     *            The set of traces that are part of this experiment
     */
    public VirtualMachineExperiment(String id, Set<ITmfTrace> traces) {
        super(CtfTmfEvent.class, id, traces.toArray(new ITmfTrace[traces.size()]), TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
    }

}
