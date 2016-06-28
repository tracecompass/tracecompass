/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryAspect;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoLoadedBinaryFile;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstEvent;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Tests for the {@link UstDebugInfoAnalysisModule}
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoAnalysisModuleTest {

    private static final @NonNull CtfTestTrace REAL_TEST_TRACE = CtfTestTrace.DEBUG_INFO4;
    private static final @NonNull CtfTestTrace SYNTH_EXEC_TRACE = CtfTestTrace.DEBUG_INFO_SYNTH_EXEC;
    private static final @NonNull CtfTestTrace SYNTH_TWO_PROCESSES_TRACE = CtfTestTrace.DEBUG_INFO_SYNTH_TWO_PROCESSES;
    private static final @NonNull CtfTestTrace SYNTH_BUILDID_DEBUGLINK_TRACE = CtfTestTrace.DEBUG_INFO_SYNTH_BUILDID_DEBUGLINK;
    private static final @NonNull CtfTestTrace INVALID_TRACE = CtfTestTrace.CYG_PROFILE;

    private LttngUstTrace fTrace;
    private UstDebugInfoAnalysisModule fModule;

    /**
     * Test setup
     */
    @Before
    public void setup() {
        fModule = new UstDebugInfoAnalysisModule();
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }

        fModule.dispose();
        fModule = null;
    }

    private @NonNull LttngUstTrace setupTrace(@NonNull CtfTestTrace testTrace) {
        LttngUstTrace trace = new LttngUstTrace();
        try {
            trace.initTrace(null, CtfTmfTestTraceUtils.getTrace(testTrace).getPath(), CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        fTrace = trace;
        return trace;
    }

    /**
     * Test for {@link UstDebugInfoAnalysisModule#getAnalysisRequirements()}
     */
    @Test
    public void testGetAnalysisRequirements() {
        Iterable<TmfAbstractAnalysisRequirement> requirements = fModule.getAnalysisRequirements();
        assertNotNull(requirements);
        assertTrue(Iterables.isEmpty(requirements));
    }

    /**
     * Test that the analysis can execute on a valid trace.
     */
    @Test
    public void testCanExecute() {
        LttngUstTrace trace = setupTrace(REAL_TEST_TRACE);
        assertTrue(fModule.canExecute(trace));
    }

    /**
     * Test that the analysis correctly refuses to execute on an invalid trace
     * (LTTng-UST < 2.8 in this case).
     */
    @Test
    public void testCannotExcecute() {
        LttngUstTrace invalidTrace = setupTrace(INVALID_TRACE);
        assertFalse(fModule.canExecute(invalidTrace));
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
        setupTrace(REAL_TEST_TRACE);
        executeModule();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
    }

    /**
     * Test that the binary callsite aspect resolves correctly for some
     * user-defined tracepoints in the trace.
     *
     * These should be available even without the binaries with debug symbols
     * being present on the system.
     */
    @Test
    public void testBinaryCallsites() {
        LttngUstTrace trace = setupTrace(REAL_TEST_TRACE);

        /*
         * Fake a "trace opened" signal, so that the relevant analyses are
         * started.
         */
        TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, trace, null);
        TmfSignalManager.dispatchSignal(signal);

        /* Send a request to get the 3 events we are interested in */
        List<@NonNull LttngUstEvent> events = new ArrayList<>();
        TmfEventRequest request = new TmfEventRequest(LttngUstEvent.class, 31, 1, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                events.add((LttngUstEvent) event);
            }
        };
        trace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        /* Tests that the aspects are resolved correctly */
        final UstDebugInfoBinaryAspect aspect = UstDebugInfoBinaryAspect.INSTANCE;

        String actual = checkNotNull(aspect.resolve(events.get(0))).toString();
        String expected = "/home/simark/src/babeltrace/tests/debug-info-data/libhello_so+0x14d4";
        assertEquals(expected, actual);
    }

    /**
     * Test the analysis with a test trace doing an "exec" system call.
     */
    @Test
    public void testExec() {
        UstDebugInfoLoadedBinaryFile matchingFile, expected;

        int vpid = 1337;

        setupTrace(SYNTH_EXEC_TRACE);
        executeModule();

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo", null, null, false);
        matchingFile = fModule.getMatchingFile(4000000, vpid, 0x400100);
        assertEquals(expected, matchingFile);

        expected = null;
        matchingFile = fModule.getMatchingFile(8000000, vpid, 0x400100);
        assertEquals(expected, matchingFile);

        expected = new UstDebugInfoLoadedBinaryFile(0x500000, "/tmp/bar", null, null, false);
        matchingFile = fModule.getMatchingFile(9000000, vpid, 0x500100);
        assertEquals(expected, matchingFile);
    }

    /**
     * Test the analysis with a test trace with two processes doing a statedump
     * simultaneously.
     */
    @Test
    public void testTwoProcesses() {
        UstDebugInfoLoadedBinaryFile matchingFile, expected;
        int vpid1 = 1337;
        int vpid2 = 2001;

        setupTrace(SYNTH_TWO_PROCESSES_TRACE);
        executeModule();

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "/tmp/debuglink1", false);
        matchingFile = fModule.getMatchingFile(11000000, vpid1, 0x400100);
        assertEquals(expected, matchingFile);

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/bar", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "/tmp/debuglink2", false);
        matchingFile = fModule.getMatchingFile(12000000, vpid2, 0x400100);
        assertEquals(expected, matchingFile);
    }


    /**
     * Test the analysis with a trace with debug_link information.
     */
    @Test
    public void testBuildIDDebugLink() {
        UstDebugInfoLoadedBinaryFile matchingFile, expected;

        setupTrace(SYNTH_BUILDID_DEBUGLINK_TRACE);
        executeModule();

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_nn", null, null, false);
        matchingFile = fModule.getMatchingFile(17000000, 1337, 0x400100);
        assertEquals(expected, matchingFile);

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_yn", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", null, false);
        matchingFile = fModule.getMatchingFile(18000000, 1338, 0x400100);
        assertEquals(expected, matchingFile);

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_ny", null, "/tmp/debug_link1", false);
        matchingFile = fModule.getMatchingFile(19000000, 1339, 0x400100);
        assertEquals(expected, matchingFile);

        expected = new UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_yy", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "/tmp/debug_link2", false);
        matchingFile = fModule.getMatchingFile(20000000, 1340, 0x400100);
        assertEquals(expected, matchingFile);
    }

    /**
     * Test the {@link UstDebugInfoAnalysisModule#getAllBinaries} method.
     */
    @Test
    public void testGetAllBinaries() {
        setupTrace(REAL_TEST_TRACE);
        executeModule();

        List<UstDebugInfoBinaryFile> actualBinaries = Lists.newArrayList(fModule.getAllBinaries());
        List<UstDebugInfoBinaryFile> expectedBinaries = Lists.newArrayList(
                new UstDebugInfoBinaryFile("/home/simark/src/babeltrace/tests/debug-info-data/libhello_so",
                                           "cdd98cdd87f7fe64c13b6daad553987eafd40cbb", null, true),
                new UstDebugInfoBinaryFile("/home/simark/src/babeltrace/tests/debug-info-data/test",
                                           "0683255d2cf219c33cc0efd6039db09ccc4416d7", null, false),
                new UstDebugInfoBinaryFile("[linux-vdso.so.1]", null, null, false),
                new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-dl.so.0.0.0",
                                           "39c035014cc02008d6884fcb1be4e020cc820366", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/libdl-2.23.so",
                                           "db3f9be9f4ebe9e2a21e4ae0b4ef7165d40fdfef", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/libc-2.23.so",
                                           "946025a5cad7b5f2dfbaebc6ebd1fcc004349b48", null, true),
                new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust.so.0.0.0",
                                           "405b0b15daa73eccb88076247ba30356c00d3b92", null, true),
                new UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-tracepoint.so.0.0.0",
                                           "62c028aad38adb5e0910c527d522e8c86a0a3344", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/librt-2.23.so",
                                           "aba676bda7fb6adb71e100159915504e1a0c17e6", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/liburcu-bp.so.4.0.0",
                                           "b9dfadea234107f8453bc636fc160047e0c01b7a", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/liburcu-cds.so.4.0.0",
                                           "420527f6dacc762378d9fa7def54d91c80a6c87e", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/libpthread-2.23.so",
                                           "d91ed99c8425b7ce5da5bb750662a91038e02a78", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/ld-2.23.so",
                                           "524eff0527e923e4adc4be9db1ef7475607b92e8", null, true),
                new UstDebugInfoBinaryFile("/usr/lib/liburcu-common.so.4.0.0",
                                           "f279a6d46a2b846e15e7abd99cfe9fbe8d7f8295", null, true));

        Comparator<UstDebugInfoBinaryFile> comparator = Comparator.comparing(UstDebugInfoBinaryFile::getFilePath);
        actualBinaries.sort(comparator);
        expectedBinaries.sort(comparator);

        /* Highlights failures more easily */
        for (int i = 0; i < expectedBinaries.size(); i++) {
            assertEquals(expectedBinaries.get(i), actualBinaries.get(i));
        }

        assertEquals(actualBinaries, expectedBinaries);
    }

}
