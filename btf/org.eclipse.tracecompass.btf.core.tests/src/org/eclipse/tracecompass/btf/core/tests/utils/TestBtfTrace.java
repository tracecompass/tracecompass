/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.tests.utils;

import org.eclipse.tracecompass.btf.core.trace.BtfTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * Helpers for testing the btf trace. There is a main() top run the code without
 * eclipse (for educational purposes) and an event printer
 *
 * @author Matthew Khouzam
 */
public class TestBtfTrace {

    /**
     * Test
     *
     * @param args
     *            nothing
     * @throws TmfTraceException
     *             exception
     */
    public static void main(String[] args) throws TmfTraceException {
        BtfTrace trace = new BtfTrace();
        trace.initTrace(null, BtfTestTrace.BTF_TEST.getFullPath(), null);
        System.out.println(trace.toString());

        ITmfContext ctx = trace.seekEvent(0);
        ITmfContext ctx1 = trace.seekEvent(10);
        ITmfEvent event = trace.getNext(ctx);
        ITmfEvent compare = null;
        while (event != null) {
            if (event.getRank() == 10) {
                compare = event;
            }
            printEvent(event);
            event = trace.getNext(ctx);
        }
        ITmfEvent other = trace.getNext(ctx1);
        printEvent(other);
        printEvent(compare);

        trace.dispose();
    }

    private static void printEvent(ITmfEvent event) {
        if (event == null) {
            System.out.println("null");
        } else {
            System.out.println(event.getRank() + " " + event.getTimestamp().getValue() + " " + event.getName() + " " + event.getContent().toString());
        }
    }

}
