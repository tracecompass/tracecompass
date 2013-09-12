/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.tests.trace.callstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * Common methods for LTTng-UST callstack trace tests.
 *
 * @author Alexandre Montplaisir
 */
final class TestUtils {

    private TestUtils() {}

    /** ID of the generated state systems */
    static final String SSID = CallStackStateProvider.ID;

    /** Empty and delete a directory */
    static void deleteDirectory(File dir) {
        /* Assuming the dir only contains file or empty directories */
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    /** Simulate a trace being opened (which triggers building the state system) */
    static void openTrace(LttngUstTrace trace) {
        trace.indexTrace(true);
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(trace, trace, null));
        trace.getStateSystems().get(SSID).waitUntilBuilt();
    }

    /** Get the callstack for the given timestamp, for this particular trace */
    static String[] getCallStack(LttngUstTrace trace, String processName, long timestamp) {
        try {
            ITmfStateSystem ss = trace.getStateSystems().get(SSID);
            int stackAttribute = ss.getQuarkAbsolute("Threads", processName, "CallStack");
            List<ITmfStateInterval> state = ss.queryFullState(timestamp);
            int depth = state.get(stackAttribute).getStateValue().unboxInt();

            int stackTop = ss.getQuarkRelative(stackAttribute, String.valueOf(depth));
            ITmfStateValue top = state.get(stackTop).getStateValue();
            assertEquals(top, ss.querySingleStackTop(timestamp, stackAttribute).getStateValue());

            String[] ret = new String[depth];
            for (int i = 0; i < depth; i++) {
                int quark = ss.getQuarkRelative(stackAttribute, String.valueOf(i + 1));
                ret[i] = state.get(quark).getStateValue().unboxStr();
            }
            return ret;

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        fail();
        return null;
    }

}
