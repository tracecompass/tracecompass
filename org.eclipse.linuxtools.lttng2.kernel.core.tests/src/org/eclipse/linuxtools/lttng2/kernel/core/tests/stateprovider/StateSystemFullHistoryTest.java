/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import java.io.File;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;
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
        try {
            stateFile = File.createTempFile("test", ".ht"); //$NON-NLS-1$ //$NON-NLS-2$
            stateFileBenchmark = File.createTempFile("test", ".ht.benchmark"); //$NON-NLS-1$ //$NON-NLS-2$

            input = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
            ssq = StateSystemManager.loadStateHistory(stateFile, input, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the temp files after we're done
     */
    @AfterClass
    public static void cleanup() {
        boolean ret1, ret2;
        ret1 = stateFile.delete();
        ret2 = stateFileBenchmark.delete();
        if ( !(ret1 && ret2) ) {
            System.err.println("Error cleaning up during unit testing, " + //$NON-NLS-1$
                    "you might have leftovers state history files in /tmp"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Tests specific to a full-history
    // ------------------------------------------------------------------------

    /**
     * Rebuild independently so we can benchmark it. Too bad JUnit doesn't allow
     * us to @Test the @BeforeClass...
     *
     * @throws TmfTraceException
     *             Fails the test
     */
    @Test
    public void testBuild() throws TmfTraceException {
        IStateChangeInput input2;
        ITmfStateSystem ssb2;

        input2 = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
        ssb2 = StateSystemManager.loadStateHistory(stateFileBenchmark, input2, true);

        assertEquals(CtfTestFiles.startTime, ssb2.getStartTime());
        assertEquals(CtfTestFiles.endTime, ssb2.getCurrentEndTime());
    }

    /**
     * Test re-opening the existing file.
     *
     * @throws TmfTraceException
     *             Fails the test
     */
    @Test
    public void testOpenExistingStateFile() throws TmfTraceException {
        ITmfStateSystem ssb2;

        /* 'newStateFile' should have already been created */
        ssb2 = StateSystemManager.loadStateHistory(stateFile, null, true);

        assertNotNull(ssb2);
        assertEquals(CtfTestFiles.startTime, ssb2.getStartTime());
        assertEquals(CtfTestFiles.endTime, ssb2.getCurrentEndTime());
    }

}
