/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.analysis.debuginfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Tests for the {@link UstDebugInfoAnalysisModule}
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoAnalysisModuleTest {

    // TODO Change to real traces
    private static final @NonNull CtfTestTrace TEST_TRACE = CtfTestTrace.DEBUG_INFO;
    private static final @NonNull CtfTestTrace INVALID_TRACE = CtfTestTrace.CYG_PROFILE;

    private LttngUstTrace fTrace;
    private UstDebugInfoAnalysisModule fModule;

    /**
     * Test setup
     */
    @Before
    public void setup() {
        fModule = new UstDebugInfoAnalysisModule();
        fTrace = new LttngUstTrace();
        try {
            fTrace.initTrace(null, CtfTmfTestTraceUtils.getTrace(TEST_TRACE).getPath(), CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fModule.dispose();
        fTrace = null;
        fModule = null;
    }

    /**
     * Test for {@link UstDebugInfoAnalysisModule#getAnalysisRequirements()}
     */
    @Test
    public void testGetAnalysisRequirements() {
        Iterable<TmfAnalysisRequirement> requirements = fModule.getAnalysisRequirements();
        assertNotNull(requirements);
        assertTrue(Iterables.isEmpty(requirements));
    }

    /**
     * Test that the analysis can execute on a valid trace.
     */
    @Test
    public void testCanExecute() {
        assertNotNull(fTrace);
        assertTrue(fModule.canExecute(fTrace));
    }

    /**
     * Test that the analysis correctly refuses to execute on an invalid trace
     * (LTTng-UST < 2.8 in this case).
     *
     * @throws TmfTraceException
     *             Should not happen
     */
    @Test
    public void testCannotExcecute() throws TmfTraceException {
        LttngUstTrace invalidTrace = new LttngUstTrace();
        invalidTrace.initTrace(null, CtfTmfTestTraceUtils.getTrace(INVALID_TRACE).getPath(), CtfTmfEvent.class);
        assertFalse(fModule.canExecute(invalidTrace));

        invalidTrace.dispose();
    }

    private void executeModule() {
        assertNotNull(fTrace);
        try {
            fModule.setTrace(fTrace);
        } catch (TmfAnalysisException e) {
            fail();
        }
        fModule.schedule();
        fModule.waitForCompletion();
    }

    /**
     * Test that basic execution of the module works well.
     */
    @Test
    public void testExecution() {
        executeModule();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
    }

    /**
     * Test the {@link UstDebugInfoAnalysisModule#getAllBinaries} method.
     */
    @Test
    public void testGetAllBinaries() {
        executeModule();
        Collection<UstDebugInfoBinaryFile> actualBinaries = fModule.getAllBinaries();
        Collection<UstDebugInfoBinaryFile> expectedBinaries = ImmutableList.<UstDebugInfoBinaryFile> builder()
                .add(new UstDebugInfoBinaryFile("/home/alexandre/src/lttng/examples/test_app_debuginfo/libhello.so", "77e5df951d3c960de71aa816dace97d0769c6357"))
                .add(new UstDebugInfoBinaryFile("/home/alexandre/src/lttng/examples/test_app_debuginfo/main.out", "a6048c2a073213db0815a08cccd8d7bc17999a12"))
                .add(new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/ld-2.21.so", "903bb7a6deefd966dceec4566c70444c727ed294"))
                .add(new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libc-2.21.so", "8acd43cf74a9756cd727b8516b08679ee071a92d"))
                .add(new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libdl-2.21.so", "f974b99c0c327670ef882ba13912e995a12c6402"))
                .add(new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/libpthread-2.21.so", "a37a144bcbee86a9e02dff5021a111ede6a1f212"))
                .add(new UstDebugInfoBinaryFile("/lib/x86_64-linux-gnu/librt-2.21.so", "cb26cb6169cbaa9da2e38a70bcf7d57a8047ccf3"))
                .add(new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liburcu-bp.so.3.0.0", "b89e6022fa66afaea454ac9825bd2d1d8aabcc2f"))
                .add(new UstDebugInfoBinaryFile("/usr/lib/x86_64-linux-gnu/liburcu-cds.so.3.0.0", "20adc09068db509a8ee159e5a5cab1642c45aaad"))
                .add(new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-dl.so.0.0.0", "6b2b70024b39672b34268974ec84d65dcd91bd91"))
                .add(new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-tracepoint.so.0.0.0", "46ef60f7a1b42cbb97dae28ae2231a7e84f0bc05"))
                .add(new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust.so.0.0.0", "48e2b74b240d06d86b5730d666fa0af8337fbe99"))
                .build();

        assertTrue(actualBinaries.containsAll(expectedBinaries));
    }

}
