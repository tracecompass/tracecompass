/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.headless;

import java.io.File;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;

/**
 * Simple example of how to use the state system using a CTF kernel trace.
 *
 * @author Mathieu Denis
 */
public class BasicStateSystemExample {

    /**
     * Run the program
     * @param args Arguments on the command-line
     */
    public static void main(String[] args) {
        /* Read a trace and build the state system */
        try {
            File newStateFile = new File("/tmp/helloworldctf.ht");
            ITmfStateProvider input = new LttngKernelStateProvider(CtfTmfTestTraces.getTestTrace(1));
            ITmfStateSystem ss = TmfStateSystemFactory.newFullHistory(newStateFile, input, true);

            requestExample(ss);
        } catch (TmfTraceException e) {
            e.printStackTrace();
        }
    }

    /**
     * From a state system tree previously built with a CTF kernel trace, print
     * to the console the interval of each state and the ID of the current
     * thread running on each CPU.
     *
     * @param ssb
     *            the State System Builder through which make request
     */
    private static void requestExample(final ITmfStateSystem ssb) {
        try {
            /* Request the current thread executing on each CPU */
            List<Integer> currentThreadByCPUS;
            List<ITmfStateInterval> stateIntervals;
            StringBuilder output = new StringBuilder();

            currentThreadByCPUS = ssb.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD);

            for (Integer currentThread : currentThreadByCPUS) {
                stateIntervals = ssb.queryHistoryRange(currentThread.intValue(), ssb.getStartTime(),
                        ssb.getCurrentEndTime());

                /* Output formatting */
                output.append("Value of attribute : ");
                output.append(ssb.getFullAttributePath(currentThread.intValue()));
                output.append("\n------------------------------------------------\n");
                for (ITmfStateInterval stateInterval : stateIntervals) {
                    /* Print the interval */
                    output.append('[');
                    output.append(String.valueOf(stateInterval.getStartTime()));
                    output.append(", ");
                    output.append(String.valueOf(stateInterval.getEndTime()));
                    output.append(']');
                    /* Print the attribute value */
                    output.append(" = ");
                    output.append(stateInterval.getStateValue().unboxInt());
                    output.append('\n');
                }
            }
            System.out.println(output.toString());
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }
    }
}
