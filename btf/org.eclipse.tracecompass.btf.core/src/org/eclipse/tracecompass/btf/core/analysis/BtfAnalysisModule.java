/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.tracecompass.btf.core.trace.BtfTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Analysis module for the BTF base analysis
 *
 * @author Alexandre Montplaisir
 */
public class BtfAnalysisModule extends TmfStateSystemAnalysisModule {

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof BtfTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    /**
     * @since 2.1
     */
    @Override
    public BtfTrace getTrace() {
        return (BtfTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new BtfStateProvider(checkNotNull(getTrace()));
    }
}
