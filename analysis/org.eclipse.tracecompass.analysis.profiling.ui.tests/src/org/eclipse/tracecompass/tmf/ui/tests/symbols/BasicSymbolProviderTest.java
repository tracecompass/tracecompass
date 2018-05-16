/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.symbols;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.TmfResolvedSymbol;
import org.eclipse.tracecompass.analysis.profiling.core.tests.symbols.MappingFileTest;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.symbols.BasicSymbolProvider;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.symbols.BasicSymbolProviderFactory;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link BasicSymbolProvider} class
 *
 * @author Geneviève Bastien
 */
public class BasicSymbolProviderTest {

    private @Nullable ITmfTrace fTrace = null;

    /**
     * Create a trace
     */
    @Before
    public void setupTest() {
        fTrace = TmfTestTrace.A_TEST_10K.getTrace();
    }

    /**
     * Delete the trace at the end of the test
     */
    @After
    public void cleanUp() {
        ITmfTrace trace = fTrace;
        if (trace != null) {
            trace.dispose();
        }
    }

    private BasicSymbolProvider getSymbolProvider() {
        // Setup the symbol provider with the trace
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        BasicSymbolProviderFactory factory = new BasicSymbolProviderFactory();
        ISymbolProvider symbolProvider = factory.createProvider(trace);
        assertTrue(symbolProvider instanceof BasicSymbolProvider);
        BasicSymbolProvider bsp = (BasicSymbolProvider) symbolProvider;
        return bsp;
    }

    /**
     * Test symbol resolution by the {@link BasicSymbolProvider}
     */
    @Test
    public void testBasicProvider() {
        BasicSymbolProvider symbolProvider = getSymbolProvider();
        symbolProvider.setMappingFiles(MappingFileTest.getMappingFiles());

        // Test a symbol without pid, that is in one mapping only
        TmfResolvedSymbol symbol = symbolProvider.getSymbol(Long.parseUnsignedLong("601050", 16));
        assertNotNull(symbol);
        assertEquals("__dso_handle", symbol.getSymbolName());

        // No pid, should return the symbol closer to the value in all files
        // FIXME: If no pid specified, should we ignore mappings with pid?
        symbol = symbolProvider.getSymbol(Long.parseUnsignedLong("400752", 16));
        assertNotNull(symbol);
        assertEquals("A little bit after nm_ouput's frame_dummy", symbol.getSymbolName());

        // Different pid, should ignore the symbol from 123 mapping file
        symbol = symbolProvider.getSymbol(1, 0L, Long.parseUnsignedLong("400752", 16));
        assertNotNull(symbol);
        assertEquals("frame_dummy", symbol.getSymbolName());

        // A symbol for process 123 that hits the mark
        symbol = symbolProvider.getSymbol(123, 0L, Long.parseUnsignedLong("4005d0", 16));
        assertNotNull(symbol);
        assertEquals("same address as nm_output", symbol.getSymbolName());

        // A symbol for process 123 that is closer to a symbol from the global nm, shoud
        // resolve to the process's value
        symbol = symbolProvider.getSymbol(123, 0L, Long.parseUnsignedLong("400735", 16));
        assertNotNull(symbol);
        assertEquals("A little bit before nm_ouput's frame_dummy", symbol.getSymbolName());

        // process 123, outside the address space of the global mapping file
        symbol = symbolProvider.getSymbol(123, 0L, Long.parseUnsignedLong("ffeeddccbbaa0090", 16));
        assertNotNull(symbol);
        assertEquals("One huge symbol from nm mapping", symbol.getSymbolName());

        // process 1, outside the address space of the global mapping file
        symbol = symbolProvider.getSymbol(1, 0L, Long.parseUnsignedLong("ffeeddccbbaa0090", 16));
        assertNull(symbol);
    }

}
