/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.stubs;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Dummy test ctf trace
 */
public class CtfTmfTraceStub extends CtfTmfTrace {

    /**
     * Simulate trace opening, to be called by tests who need an actively opened
     * trace
     */
    public void openTrace() {
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, this, null));
        selectTrace();
    }

    /**
     * Simulate selecting the trace
     */
    public void selectTrace() {
        TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, this));
    }

}
