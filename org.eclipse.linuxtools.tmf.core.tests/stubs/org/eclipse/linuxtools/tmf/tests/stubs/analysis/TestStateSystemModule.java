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

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Test State System module
 *
 * @author Geneviève Bastien
 */
public class TestStateSystemModule extends TmfStateSystemAnalysisModule {

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new TestStateSystemProvider(getTrace());
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.INMEM;
    }

}
