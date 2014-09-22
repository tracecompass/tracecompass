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

package org.eclipse.linuxtools.btf.core.analysis;

import org.eclipse.linuxtools.btf.core.trace.BtfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Analysis module for the BTF base analysis
 *
 * @author Alexandre Montplaisir
 */
public class BtfAnalysisModule extends TmfStateSystemAnalysisModule {

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof BtfTrace)) {
            throw new IllegalArgumentException("BtfAnalysisModule: trace should be of type BtfTrace"); //$NON-NLS-1$
        }
        super.setTrace(trace);
    }

    @Override
    protected BtfTrace getTrace() {
        return (BtfTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new BtfStateProvider(getTrace());
    }
}
