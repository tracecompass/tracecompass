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
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.headless;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;

/**
 * Small program to regenerate the values used in "TestValues.java" from the
 * current LTTng-kernel state provider.
 *
 * It will write its output the a file called 'test-values*.log' in your
 * temporary files directory.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class GenerateTestValues {

    private static final int TRACE_INDEX = 1;
    private static final long targetTimestamp = 18670067372290L + 1331649577946812237L;

    /**
     * Run the program
     *
     * @param args
     *            Command-line arguments, unused.
     * @throws Exception
     *             I'm messing with Exception. Come at me bro!
     */
    public static void main(String[] args) throws Exception {
        if (!CtfTmfTestTraces.tracesExist()) {
            System.err.println("Trace files not present.");
            return;
        }

        /* Prepare the files */
        File stateFile = File.createTempFile("test-values", ".ht");
        stateFile.deleteOnExit();
        File logFile = File.createTempFile("test-values", ".log");
        PrintWriter writer = new PrintWriter(new FileWriter(logFile), true);

        /* Build and query the state system */
        IStateChangeInput input = new CtfKernelStateInput(CtfTmfTestTraces.getTestTrace(TRACE_INDEX));
        ITmfStateSystem ssq = StateSystemManager.loadStateHistory(stateFile, input, true);
        List<ITmfStateInterval> fullState = ssq.queryFullState(targetTimestamp);

        /* Print the interval contents (with some convenience formatting) */
        writer.println("Start times:");
        for (ITmfStateInterval interval : fullState) {
            writer.println(String.valueOf(interval.getStartTime()) + "L,");
        }
        writer.println();

        writer.println("End times:");
        for (ITmfStateInterval interval : fullState) {
            writer.println(String.valueOf(interval.getEndTime())+ "L,");
        }
        writer.println();

        writer.println("State values:");
        for (ITmfStateInterval interval : fullState) {
            ITmfStateValue val = interval.getStateValue();
            switch (val.getType()) {
            case NULL:
                writer.println("TmfStateValue.nullValue(),");
                break;

            case INTEGER:
                writer.println("TmfStateValue.newValueInt(" + val.unboxInt() + "),");
                break;

            case STRING:
                writer.println("TmfStateValue.newValueString(\"" + val.unboxStr() + "\"),");
                break;

            default:
                writer.println(val.toString());
                break;
            }
        }

        writer.close();
        System.exit(0);
    }

}
