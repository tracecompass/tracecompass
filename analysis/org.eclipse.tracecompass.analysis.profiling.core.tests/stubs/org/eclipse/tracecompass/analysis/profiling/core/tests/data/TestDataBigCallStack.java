/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.analysis.profiling.core.tests.stubs.CallStackProviderStub;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Describe the callstack test data for a big stub trace, randomly generated. It
 * is mostly to test the algorithms that require accesses to the state system,
 * the state system here being big enough to have multiple nodes and levels.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TestDataBigCallStack extends CallStackTestData {

    private static final String CALLSTACK_FILE = "testfiles/traces/callstackBig.xml";
    private boolean fDataInitialized = false;
    private long fEnd = 0;
    private long fStart = Long.MAX_VALUE;

    @Override
    public @NonNull ITmfTrace getTrace() {
        // A good moment to initialize the expected data by reading the trace
        ITmfTrace trace = super.getTrace();
        if (!fDataInitialized) {
            setCallStackData(getExpectedData(trace));
            fDataInitialized = true;
        }
        return trace;
    }

    @Override
    protected @NonNull String getTraceFile() {
        return CALLSTACK_FILE;
    }

    @Override
    protected long getStartTime() {
        return fStart;
    }

    @Override
    protected long getEndTime() {
        return fEnd;
    }

    private List<ExpectedCallStackElement> getExpectedData(ITmfTrace trace) {
        ExpectedDataBuildingRequest request = new ExpectedDataBuildingRequest();
        trace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Request was interrupted");
        }
        return request.getExpectedData();
    }

    private static class OpenedFunction {
        private final long fFctStart;
        private final String fFctName;
        private final List<ExpectedFunction> fChildren = new ArrayList<>();

        OpenedFunction(long fctStart, String fctName) {
            fFctStart = fctStart;
            fFctName = fctName;
        }

        ExpectedFunction closeFunction(long end) {
            return new ExpectedFunction((int) fFctStart, (int) end, fFctName, fChildren);
        }
    }

    private static class OpenedElement {
        private final int fPid;
        private final int fTid;
        private final List<ExpectedFunction> fCalls = new ArrayList<>();

        OpenedElement(int pid, int tid) {
            fPid = pid;
            fTid = tid;
        }
    }

    private class ExpectedDataBuildingRequest extends TmfEventRequest {

        Map<Long, Stack<OpenedFunction>> fOpenedFct = new HashMap<>();
        Map<Long, OpenedElement> fOpenedElements = new HashMap<>();

        public ExpectedDataBuildingRequest() {
            super(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        }

        public List<ExpectedCallStackElement> getExpectedData() {
            List<ExpectedCallStackElement> list = new ArrayList<>();
            for (OpenedElement element : fOpenedElements.values()) {
                list.add(new ExpectedCallStackElement(element.fPid, element.fTid, element.fCalls));
            }
            return list;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            long ts = event.getTimestamp().toNanos();
            fStart = Math.min(ts, fStart);
            fEnd = Math.max(ts, fEnd);
            long tid = CallStackProviderStub.getThreadIdFromEvent(event);
            if (tid == CallStackStateProvider.UNKNOWN_PID) {
                return;
            }
            Stack<OpenedFunction> stack = fOpenedFct.get(tid);
            if (stack == null) {
                stack = new Stack<>();
                fOpenedFct.put(tid, stack);
            }
            String name = event.getName();
            if (name.equals(CallStackProviderStub.ENTRY)) {
                // Add the opened function to the stack
                ITmfEventField field = event.getContent().getField(CallStackProviderStub.FIELD_NAME);
                if (field == null) {
                    return;
                }
                stack.push(new OpenedFunction(ts, field.getValue().toString()));
            } else if (name.equals(CallStackProviderStub.EXIT)) {
                if (stack.isEmpty()) {
                    return;
                }
                OpenedFunction toClose = stack.pop();
                ExpectedFunction closedFunction = toClose.closeFunction(ts);
                if (stack.isEmpty()) {
                    // Add the expected function to the base element
                    // See if we have an opened element for this tid
                    OpenedElement openedElement = fOpenedElements.get(tid);
                    if (openedElement == null) {
                        openedElement = createElementFromEvent(event, tid);
                    }
                    openedElement.fCalls.add(closedFunction);
                } else {
                    // Add the closed function to the parent
                    OpenedFunction parent = stack.peek();
                    parent.fChildren.add(closedFunction);
                }

            }
        }

        private OpenedElement createElementFromEvent(ITmfEvent event, long tid) {
            int pid = CallStackProviderStub.getProcessIdFromEvent(event);
            if (pid == CallStackStateProvider.UNKNOWN_PID) {
                throw new NullPointerException("There should be a pid field");
            }
            OpenedElement openedElement = new OpenedElement(pid, (int) tid);
            fOpenedElements.put(tid, openedElement);
            return openedElement;
        }

    }

}
