/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.symbols;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.Test;

/**
 * Test the {@link SymbolProviderManager} class
 *
 * @author Geneviève Bastien
 */
public class SymbolProviderManagerTest {

    /**
     * Test the {@link SymbolProviderManager#getSymbolProviders(org.eclipse.tracecompass.tmf.core.trace.ITmfTrace)} for an experiment
     */
    @Test
    public void testGetterForExperiment() {
        ITmfTrace trace = null;
        TmfExperiment experiment = null;
        try {
            trace = TmfTestTrace.A_TEST_10K.getTrace();
            ITmfTrace[] traces = new ITmfTrace[] { trace };
            experiment = new TmfExperiment(ITmfEvent.class, "test-exp", traces,
                    TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);

            // Get the symbol providers for the trace
            Collection<ISymbolProvider> traceSymbolProviders = SymbolProviderManager.getInstance().getSymbolProviders(trace);
            assertTrue(!traceSymbolProviders.isEmpty());

            // Get the symbol providers for the experiment
            Collection<ISymbolProvider> expSymbolProviders = SymbolProviderManager.getInstance().getSymbolProviders(experiment);
            assertTrue(!expSymbolProviders.isEmpty());

            // Assert that the trace of the symbol provider is the same
            for (ISymbolProvider sp : expSymbolProviders) {
                assertTrue(sp.getTrace() == trace);
            }

            for (ISymbolProvider symbolProvider : traceSymbolProviders) {
                assertTrue(expSymbolProviders.contains(symbolProvider));
            }
        } finally {
            if (trace != null) {
                trace.dispose();
            }
            if (experiment != null) {
                experiment.dispose();
            }
        }
    }
}
