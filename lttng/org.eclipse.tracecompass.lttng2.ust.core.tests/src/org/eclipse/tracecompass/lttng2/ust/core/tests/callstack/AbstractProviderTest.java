/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.callstack.LttngUstCallStackProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Base class for the UST callstack state provider tests.
 *
 * @author Alexandre Montplaisir
 */
public abstract class AbstractProviderTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final @NonNull CtfTestTrace otherUstTrace = CtfTestTrace.HELLO_LOST;

    private CtfTmfTrace fTrace = null;
    private ITmfStateSystem fSS = null;
    private TestLttngCallStackModule fModule;


    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * @return The test trace to use for this test
     */
    protected abstract @NonNull CtfTestTrace getTestTrace();

    /**
     * @return The ID of the process the desired thread belongs to
     */
    protected abstract int getProcessId();

    /**
     * @return The name of the executable process in that particular trace
     */
    protected abstract String getThreadName();

    /**
     * Get the list of timestamps to query in that trace.
     *
     * @param index
     *            Which of the test timestamps?
     * @return That particular timestamp
     */
    protected abstract long getTestTimestamp(int index);

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Perform pre-class initialization.
     */
    @Before
    public void setUp() {
        CtfTestTrace testTrace = getTestTrace();

        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
        fTrace = trace;
        fModule = new TestLttngCallStackModule();
        try {
            assertTrue(fModule.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail();
        }
        fModule.schedule();
        assertTrue(fModule.waitForCompletion());

        fSS = fModule.getStateSystem();
        assertNotNull(fSS);
    }

    /**
     * Perform post-class clean-up.
     */
    @After
    public void tearDown() {
        fModule.dispose();
        if (fTrace != null) {
            fTrace.dispose();
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(fTrace));
            deleteDirectory(suppDir);
        }
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test the handling of generic UST traces who do not contain the required
     * information.
     */
    @Test
    public void testOtherUstTrace() {
        /* Initialize the trace and analysis module */
        File suppDir;
        CtfTmfTrace ustTrace = CtfTmfTestTraceUtils.getTrace(otherUstTrace);
        TestLttngCallStackModule module = null;
        try {
            module = new TestLttngCallStackModule();
            try {
                assertTrue(module.setTrace(ustTrace));
            } catch (TmfAnalysisException e) {
                fail();
            }
            module.schedule();
            assertTrue(module.waitForCompletion());

            /* Make sure the generated state system exists, but is empty */
            ITmfStateSystem ss = module.getStateSystem();
            assertNotNull(ss);
            assertTrue(ss.getStartTime() >= ustTrace.getStartTime().toNanos());
            assertEquals(0, ss.getNbAttributes());
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
        suppDir = new File(TmfTraceManager.getSupplementaryFileDir(ustTrace));

        ustTrace.dispose();
        deleteDirectory(suppDir);
        assertFalse(suppDir.exists());
    }

    /**
     * Test that the callstack state system is there and contains data.
     */
    @Test
    public void testConstruction() {
        assertNotNull(fSS);
        assertTrue(fSS.getNbAttributes() > 0);
    }

    /**
     * Test the callstack at the beginning of the state system.
     */
    @Test
    public void testCallStackBegin() {
        long start = fSS.getStartTime();
        String[] cs = getCallStack(fSS, getProcessId(), getThreadName(), start);
        assertEquals(1, cs.length);

        assertEquals("40472b", cs[0]);
    }

    /**
     * Test the callstack somewhere in the trace.
     */
    @Test
    public void testCallStack1() {
        String[] cs = getCallStack(fSS, getProcessId(), getThreadName(), getTestTimestamp(0));
        assertEquals(2, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("403d60", cs[1]);
    }

    /**
     * Test the callstack somewhere in the trace.
     */
    @Test
    public void testCallStack2() {
        String[] cs = getCallStack(fSS, getProcessId(), getThreadName(), getTestTimestamp(1));
        assertEquals(3, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("403b14", cs[1]);
        assertEquals("401b23", cs[2]);
    }

    /**
     * Test the callstack somewhere in the trace.
     */
    @Test
    public void testCallStack3() {
        String[] cs = getCallStack(fSS, getProcessId(), getThreadName(), getTestTimestamp(2));
        assertEquals(4, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("4045c8", cs[1]);
        assertEquals("403760", cs[2]);
        assertEquals("401aac", cs[3]);
    }

    /**
     * Test the callstack at the end of the trace/state system.
     */
    @Test
    public void testCallStackEnd() {
        long end = fSS.getCurrentEndTime();
        String[] cs = getCallStack(fSS, getProcessId(), getThreadName(), end);
        assertEquals(3, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("4045c8", cs[1]);
        assertEquals("403760", cs[2]);
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    /** Empty and delete a directory */
    private static void deleteDirectory(File dir) {
        /* Assuming the dir only contains file or empty directories */
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    /** Get the callstack for the given timestamp, for this particular trace */
    private static String[] getCallStack(ITmfStateSystem ss, int pid, String threadName, long timestamp) {
        try {
            int stackAttribute = ss.getQuarkAbsolute("Processes", Integer.toString(pid), threadName, "CallStack");
            List<ITmfStateInterval> state = ss.queryFullState(timestamp);
            int depth = state.get(stackAttribute).getStateValue().unboxInt();

            int stackTop = ss.getQuarkRelative(stackAttribute, String.valueOf(depth));
            ITmfStateValue expectedValue = state.get(stackTop).getStateValue();
            ITmfStateInterval interval = StateSystemUtils.querySingleStackTop(ss, timestamp, stackAttribute);
            assertNotNull(interval);
            assertEquals(expectedValue, interval.getStateValue());

            String[] ret = new String[depth];
            for (int i = 0; i < depth; i++) {
                int quark = ss.getQuarkRelative(stackAttribute, String.valueOf(i + 1));
                ret[i] = state.get(quark).getStateValue().unboxStr();
            }
            return ret;

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        }
        fail();
        return null;
    }

    private class TestLttngCallStackModule extends TmfStateSystemAnalysisModule {

        @Override
        protected ITmfStateProvider createStateProvider() {
            return new LttngUstCallStackProvider(checkNotNull(getTrace()));
        }
    }
}
