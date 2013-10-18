/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * State system tests using a full history back-end and the LTTng kernel state
 * input.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemFullHistoryTest extends StateSystemTest {

    private static File stateFile;
    private static File stateFileBenchmark;

    /**
     * Initialize the test cases (build the history file once for all tests).
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(testTrace.exists());
        try {
            stateFile = File.createTempFile("test", ".ht");
            stateFileBenchmark = File.createTempFile("test", ".ht.benchmark");

            input = new LttngKernelStateProvider(testTrace.getTrace());
            ssq = TmfStateSystemFactory.newFullHistory(stateFile, input, true);
        } catch (IOException e) {
            fail();
        } catch (TmfTraceException e) {
            fail();
        }
    }

    /**
     * Clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        stateFile.delete();
        stateFileBenchmark.delete();
    }

    // ------------------------------------------------------------------------
    // Tests specific to a full-history
    // ------------------------------------------------------------------------

    /**
     * Rebuild independently so we can benchmark it. Too bad JUnit doesn't allow
     * us to @Test the @BeforeClass...
     */
    @Test
    public void testBuild() {
        try {
            ITmfStateProvider input2 = new LttngKernelStateProvider(testTrace.getTrace());
            ITmfStateSystem ssb2 = TmfStateSystemFactory.newFullHistory(stateFileBenchmark, input2, true);

            assertEquals(startTime, ssb2.getStartTime());
            assertEquals(endTime, ssb2.getCurrentEndTime());

        } catch (TmfTraceException e) {
            fail();
        }
    }

    /**
     * Test re-opening the existing file.
     */
    @Test
    public void testOpenExistingStateFile() {
        try {
            /* 'newStateFile' should have already been created */
            ITmfStateSystem ssb2 = TmfStateSystemFactory.newFullHistory(stateFile, null, true);

            assertNotNull(ssb2);
            assertEquals(startTime, ssb2.getStartTime());
            assertEquals(endTime, ssb2.getCurrentEndTime());

         } catch (TmfTraceException e) {
             fail();
         }
    }

}
