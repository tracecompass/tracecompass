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
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.common.collect.ImmutableList;

/**
 * Describe the callstack test data for a small human generated trace, which
 * covers most use cases. It is easy to debug code changes using this trace.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TestDataSmallCallStack extends CallStackTestData {

    private static final String CALLSTACK_FILE = "testfiles/traces/callstack.xml";

    private static final List<ExpectedCallStackElement> CALLSTACK_RAW_DATA = new ArrayList<>();
    private static final long START = 1L;
    private static final long END = 25L;

    static {
        // Prepare pid 1, tid 2
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(1, 2, ImmutableList.of(
                new ExpectedFunction(1, 10, "op1", ImmutableList.of(
                        new ExpectedFunction(3, 7, "op2", ImmutableList.of(
                                new ExpectedFunction(4, 5, "op3", Collections.emptyList()))))),
                new ExpectedFunction(12, 20, "op4", Collections.emptyList()),
                new ExpectedFunction(22, 26, "op4", ImmutableList.of(
                        new ExpectedFunction(24, 26, "op2", ImmutableList.of(
                                new ExpectedFunction(25, 26, "op2", Collections.emptyList())))))
                )));
        // Prepare pid 1, tid 3
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(1, 3, ImmutableList.of(
                new ExpectedFunction(3, 20, "op2", ImmutableList.of(
                        new ExpectedFunction(5, 6, "op3", Collections.emptyList()),
                        new ExpectedFunction(7, 13, "op2", Collections.emptyList()))))));
        // Prepare pid 5, tid 6
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(5, 6, ImmutableList.of(
                new ExpectedFunction(1, 20, "op1", ImmutableList.of(
                        new ExpectedFunction(2, 7, "op3", ImmutableList.of(
                                new ExpectedFunction(4, 6, "op1", Collections.emptyList()))),
                        new ExpectedFunction(8, 11, "op2", ImmutableList.of(
                                new ExpectedFunction(9, 10, "op3", Collections.emptyList()))),
                        new ExpectedFunction(12, 20, "op4", Collections.emptyList()))))));
        // Prepare pid 5, tid 7
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(5, 7, ImmutableList.of(
                new ExpectedFunction(1, 20, "op5", ImmutableList.of(
                        new ExpectedFunction(2, 6, "op2", Collections.emptyList()),
                        new ExpectedFunction(9, 13, "op2", ImmutableList.of(
                                new ExpectedFunction(10, 11, "op3", Collections.emptyList()))),
                        new ExpectedFunction(15, 19, "op2", Collections.emptyList()))))));
    }

    /**
     * Constructor
     */
    public TestDataSmallCallStack() {
        super();
        setCallStackData(CALLSTACK_RAW_DATA);
    }

    @Override
    protected String getTraceFile() {
        return CALLSTACK_FILE;
    }

    @Override
    protected long getStartTime() {
        return START;
    }

    @Override
    protected long getEndTime() {
        return END;
    }

}
