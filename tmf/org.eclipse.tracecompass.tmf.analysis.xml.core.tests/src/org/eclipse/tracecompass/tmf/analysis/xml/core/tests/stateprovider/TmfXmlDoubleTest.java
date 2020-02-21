/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.Test;

/**
 * Test Doubles in xml state system
 *
 * @author Matthew Khouzam
 *
 */
public class TmfXmlDoubleTest {

    private static final @NonNull String testTrace3 = "test_traces/testTrace3.xml";

    /**
     * Test the state system on a double
     *
     * @throws TmfAnalysisException
     *             if it happens, we fail
     * @throws StateSystemDisposedException
     *             if it happens, we fail
     *
     */
    @Test
    public void testConditionsValidation() throws TmfAnalysisException, StateSystemDisposedException {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(testTrace3);
        DataDrivenAnalysisModule module = XmlUtilsTest.initializeModule(TmfXmlTestFiles.DOUBLES_FILE);
        module.setTrace(trace);

        module.schedule();
        module.waitForCompletion();

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);
        List<ITmfStateInterval> val = ss.queryFullState(2);
        assertEquals(3.141592, val.get(0).getStateValue().unboxDouble(), Double.MIN_VALUE);
        val = ss.queryFullState(4);
        assertEquals(2.71828, val.get(0).getStateValue().unboxDouble(), Double.MIN_VALUE);
        val = ss.queryFullState(6);
        assertEquals(1.41421, val.get(0).getStateValue().unboxDouble(), Double.MIN_VALUE);
        trace.dispose();
        module.dispose();
    }
}
