/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.stubs.module;

import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Graph builder module stub
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class GraphBuilderModuleStub extends TmfGraphBuilderModule {

    /** The analysis id */
    public static final String ANALYSIS_ID = "org.eclipse.linuxtools.tmf.analysis.graph.tests.stub";

    /* Make it public so unit tests can use the graph provider */
    @Override
    public GraphProviderStub getGraphProvider() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new NullPointerException();
        }
        return new GraphProviderStub(trace);
    }

}
