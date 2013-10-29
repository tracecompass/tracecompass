/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.tests.trace.callstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the UST callstack state provider, using the trace of a program
 * instrumented with lttng-ust-cyg-profile-fast.so tracepoints. These do not
 * contain the function addresses in the func_exit events.
 *
 * @author Alexandre Montplaisir
 */
public class LttngUstCallStackProviderFastTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.CYG_PROFILE_FAST;

    private static final String PROCNAME = "glxgears-29822";

    private static LttngUstTrace fixture = null;

    // ------------------------------------------------------------------------
    // Class  methods
    // ------------------------------------------------------------------------

    /**
     * Perform pre-class initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @BeforeClass
    public static void setUp() throws TmfTraceException {
        assumeTrue(testTrace.exists());

        /* We init the trace ourselves (we need the specific LttngUstTrace type) */
        fixture = new LttngUstTrace();
        IStatus valid = fixture.validate(null, testTrace.getPath());
        assertTrue(valid.isOK());
        fixture.initTrace((IResource) null, testTrace.getPath(), CtfTmfEvent.class);
        TestUtils.openTrace(fixture);
    }

    /**
     * Perform post-class clean-up.
     */
    @AfterClass
    public static void tearDown() {
        if (fixture != null) {
            fixture.dispose();
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(fixture));
            TestUtils.deleteDirectory(suppDir);
        }
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test that the callstack state system is there and contains data.
     */
    @Test
    public void testConstruction() {
        ITmfStateSystem ss = fixture.getStateSystems().get(TestUtils.SSID);
        assertNotNull(ss);
        assertTrue(ss.getNbAttributes() > 0);
    }

    /**
     * Test the callstack at the beginning of the state system.
     */
    @Test
    public void testCallStackBegin() {
        long start = fixture.getStateSystems().get(TestUtils.SSID).getStartTime();
        String[] cs = TestUtils.getCallStack(fixture, PROCNAME, start);
        assertEquals(1, cs.length);

        assertEquals("40472b", cs[0]);
    }

    /**
     * Test the callstack somewhere in the trace.
     */
    @Test
    public void testCallStack1() {
        String[] cs = TestUtils.getCallStack(fixture, PROCNAME, 1379361250310000000L);
        assertEquals(2, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("403d60", cs[1]);
    }

    /**
     * Test the callstack somewhere in the trace.
     */
    @Test
    public void testCallStack2() {
        String[] cs = TestUtils.getCallStack(fixture, PROCNAME, 1379361250498400000L);
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
        String[] cs = TestUtils.getCallStack(fixture, PROCNAME, 1379361250499759000L);
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
        long end = fixture.getStateSystems().get(TestUtils.SSID).getCurrentEndTime();
        String[] cs = TestUtils.getCallStack(fixture, PROCNAME, end);
        assertEquals(3, cs.length);

        assertEquals("40472b", cs[0]);
        assertEquals("4045c8", cs[1]);
        assertEquals("403760", cs[2]);
    }
}
