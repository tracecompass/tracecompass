/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Test State System module
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TestStateSystemModule extends TmfStateSystemAnalysisModule {

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new TestStateSystemProvider(checkNotNull(getTrace()));
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.INMEM;
    }

    /**
     * Get the name of the backend
     *
     * @return The name of the backend used
     */
    public String getBackendName() {
        return StateSystemBackendType.INMEM.name();
    }

}
