/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Small program to regenerate the values used in "TestValues.java" from the
 * current LTTng-kernel state provider.
 *
 * It will write its output the a file called 'TestValues<something>.java' in
 * your temporary files directory.
 *
 * @author Alexandre Montplaisir
 */
public class GenerateTestValues {

    private static @NonNull CtfTestTrace testTrace = CtfTestTrace.TRACE2;
    private static final long targetTimestamp = 18670067372290L + 1331649577946812237L;
    private static final String INDENT = "    ";

    /**
     * Test wrapper to run main properly
     *
     * @throws Exception
     *             we're messing with exception
     */
    @Test
    public void test() throws Exception {
        main(null);
    }

    /**
     * Run the program
     *
     * @param args
     *            Command-line arguments, unused.
     * @throws Exception
     *             I'm messing with Exception. Come at me bro!
     */
    public static void main(String[] args) throws Exception {
        /* Prepare the files */
        File logFile = File.createTempFile("TestValues", ".java");
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile), true);) {

            /* Build and query the state system */
            final CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
            TmfStateSystemAnalysisModule module = new KernelAnalysisModule() {
                @Override
                protected String getSsFileName() {
                    return "test-values";
                }
            };

            if (!module.setTrace(trace)) {
                throw new IllegalStateException();
            }
            module.setId("test-values");
            module.schedule();
            module.waitForCompletion();
            ITmfStateSystem ssq = module.getStateSystem();
            if (ssq == null) {
                throw new IllegalStateException();
            }

            List<ITmfStateInterval> fullState = ssq.queryFullState(targetTimestamp);

            /* Start printing the java file's contents */
            writer.println("interface TestValues {");
            writer.println();
            writer.println(INDENT + "int size = " + fullState.size() + ";");
            writer.println();

            /* Print the array contents */
            writer.println(INDENT + "long[] startTimes = {");
            for (ITmfStateInterval interval : fullState) {
                writer.println(INDENT + INDENT + String.valueOf(interval.getStartTime()) + "L,");
            }
            writer.println(INDENT + "};");
            writer.println();

            writer.println(INDENT + "long[] endTimes = {");
            for (ITmfStateInterval interval : fullState) {
                writer.println(INDENT + INDENT + String.valueOf(interval.getEndTime()) + "L,");
            }
            writer.println(INDENT + "};");
            writer.println();

            writer.println(INDENT + "ITmfStateValue[] values = {");
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
                    writer.println("TmfStateValue.newValueLong(" + val.unboxLong() + "),");
                    break;
                case DOUBLE:
                    writer.println("TmfStateValue.newValueDouble(" + val.unboxDouble() + "),");
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

            module.dispose();
            trace.dispose();
        }
        System.exit(0);
    }

}
