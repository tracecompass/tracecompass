/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.pcap.core.tests.stubs;

import org.eclipse.tracecompass.internal.tmf.pcap.core.trace.PcapTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;

/**
 * Dummy test pcap trace
 */
public class PcapTmfTraceStub extends PcapTrace {

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
