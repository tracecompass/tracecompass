/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class LttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * The file name of the History Tree
     */
    @NonNull
    public static final String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /** The ID of this analysis module */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.analysis"; //$NON-NLS-1$

    /**
     * Constructor adding the views to the analysis
     */
    public LttngKernelAnalysisModule() {
        super();
    }

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngKernelTrace)) {
            throw new IllegalArgumentException("LttngKernelStateSystemModule: trace should be of type LttngKernelTrace"); //$NON-NLS-1$
        }
        super.setTrace(trace);
    }

    @Override
    protected LttngKernelTrace getTrace() {
        /* Cast should be safe because we check the type at the setTrace() */
        return (LttngKernelTrace) super.getTrace();
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new LttngKernelStateProvider(getTrace());
    }

    @Override
    @NonNull
    protected String getSsFileName() {
        return HISTORY_TREE_FILE_NAME;
    }

    @Override
    protected String getFullHelpText() {
        return Messages.LttngKernelAnalysisModule_Help;
    }

}
