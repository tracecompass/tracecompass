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

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;

/**
 * Small program to regenerate the values used in "TestValues.java" from the
 * current LTTng-kernel state provider.
 *
 * It will write its output the a file called 'TestValues<something>.java' in your
 * temporary files directory.
 *
 * @author Alexandre Montplaisir
 */
public class GenerateTestValues {

    private static CtfTmfTestTrace testTrace = CtfTmfTestTrace.TRACE2;
    private static final long targetTimestamp = 18670067372290L + 1331649577946812237L;
    private static final String INDENT = "    ";

    /**
     * Run the program
     *
     * @param args
     *            Command-line arguments, unused.
     * @throws Exception
     *             I'm messing with Exception. Come at me bro!
     */
    public static void main(String[] args) throws Exception {
        if (!testTrace.exists()) {
            System.err.println("Trace files not present.");
            return;
        }

        /* Prepare the files */
        File stateFile = File.createTempFile("test-values", ".ht");
        stateFile.deleteOnExit();
        File logFile = File.createTempFile("TestValues", ".java");
        PrintWriter writer = new PrintWriter(new FileWriter(logFile), true);

        /* Build and query the state system */
        ITmfStateProvider input = new LttngKernelStateProvider(testTrace.getTrace());
        ITmfStateSystem ssq = TmfStateSystemFactory.newFullHistory(stateFile, input, true);
        List<ITmfStateInterval> fullState = ssq.queryFullState(targetTimestamp);

        /* Start printing the java file's contents */
        writer.println("interface TestValues {");
        writer.println();
        writer.println(INDENT + "static final int size = " + fullState.size() +";");
        writer.println();

        /* Print the array contents */
        writer.println(INDENT + "static final long[] startTimes = {");
        for (ITmfStateInterval interval : fullState) {
            writer.println(INDENT + INDENT + String.valueOf(interval.getStartTime()) + "L,");
        }
        writer.println(INDENT + "};");
        writer.println();

        writer.println(INDENT + "static final long[] endTimes = {");
        for (ITmfStateInterval interval : fullState) {
            writer.println(INDENT + INDENT + String.valueOf(interval.getEndTime())+ "L,");
        }
        writer.println(INDENT + "};");
        writer.println();

        writer.println(INDENT + "static final ITmfStateValue[] values = {");
        for (ITmfStateInterval interval : fullState) {
            ITmfStateValue val = interval.getStateValue();
            writer.print(INDENT + INDENT);

            switch (val.getType()) {
            case NULL:
                writer.println("TmfStateValue.nullValue(),");
                break;
            case INTEGER:
                writer.println("TmfStateValue.newValueInt(" + val.unboxInt() + "),");
                break;
            case LONG:
                writer.println("TmfStateValue.newValueLong(" + val.unboxLong() +"),");
                break;
            case STRING:
                writer.println("TmfStateValue.newValueString(\"" + val.unboxStr() + "\"),");
                break;
            default:
                writer.println(val.toString());
                break;
            }
        }
        writer.println(INDENT + "};");

        writer.println("}");
        writer.println();

        writer.close();
        System.exit(0);
    }

}
