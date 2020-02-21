/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.XmlUtilsTest;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.Test;

/**
 * Test the XML model for state providers
 *
 * @author Geneviève Bastien
 */
public class StateProviderModelTest {

    private static final @NonNull String testTrace1 = "test_traces/testTrace1.xml";

    /**
     * Test an increment of one, for an event name attribute
     */
    @Test
    public void testEventName() {
        ITmfTrace trace = XmlUtilsTest.initializeTrace(testTrace1);
        DataDrivenAnalysisModule module = XmlUtilsTest.initializeModule(TmfXmlTestFiles.ATTRIBUTE_FILE);
        try {

            module.setTrace(trace);

            module.schedule();
            module.waitForCompletion();

            ITmfStateSystem ss = module.getStateSystem();
            assertNotNull(ss);

            List<Integer> quarks = ss.getQuarks("*");
            assertEquals(2, quarks.size());

            for (Integer quark : quarks) {
                String name = ss.getAttributeName(quark);
                switch (name) {
                case "test":
                {
                    final int[] expectedStarts = { 1, 5, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.newValueInt(1), TmfStateValue.newValueInt(2) };
                    XmlUtilsTest.verifyStateIntervals("test", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                case "test1":
                {
                    final int[] expectedStarts = { 1, 3, 7, 7 };
                    ITmfStateValue[] expectedValues = { TmfStateValue.nullValue(), TmfStateValue.newValueInt(1), TmfStateValue.newValueInt(2) };
                    XmlUtilsTest.verifyStateIntervals("test1", ss, quark, expectedStarts, expectedValues);
                }
                    break;
                default:
                    fail("Wrong attribute name " + name);
                    break;
                }
            }

        } catch (TmfAnalysisException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        } finally {
            module.dispose();
            trace.dispose();
        }

    }

}
