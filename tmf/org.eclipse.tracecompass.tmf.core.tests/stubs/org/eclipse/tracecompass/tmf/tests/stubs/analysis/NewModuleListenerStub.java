/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.ITmfNewAnalysisModuleListener;

/**
 * A test listener for newly created modules
 *
 * @author Geneviève Bastien
 */
public class NewModuleListenerStub implements ITmfNewAnalysisModuleListener {

    private static int fNewModuleCount = 0;

    @Override
    public void moduleCreated(IAnalysisModule module) {
        fNewModuleCount++;
    }

    /**
     * Get the count of newly created modules
     *
     * @return the number of modules that were created
     */
    public static int getModuleCount() {
        return fNewModuleCount;
    }

}
