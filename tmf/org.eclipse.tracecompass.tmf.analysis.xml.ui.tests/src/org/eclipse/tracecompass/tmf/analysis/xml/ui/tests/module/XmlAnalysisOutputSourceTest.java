/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.ui.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.Test;

/**
 * Test that XML-defined outputs are added to the analysis modules they are for
 *
 * @author Geneviève Bastien
 */
public class XmlAnalysisOutputSourceTest {

    private static final String BUILTIN_MODULE = "test.builtin.sp";
    private static final String BUILTIN_OUTPUT= "Test output of XML builtin module";

    /**
     * Test the
     * {@link TmfXmlAnalysisOutputSource#moduleCreated(IAnalysisModule)} method
     */
    @Test
    public void testBuiltinOutput() {

        TmfTrace trace = new TmfXmlTraceStubNs();
        try {
            trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));

            IAnalysisModule module = trace.getAnalysisModule(BUILTIN_MODULE);
            assertNotNull(module);

            Iterator<IAnalysisOutput> iterator = module.getOutputs().iterator();
            assertTrue(iterator.hasNext());
            IAnalysisOutput output = iterator.next();
            assertEquals(BUILTIN_OUTPUT, output.getName());

        } finally {
            trace.dispose();
        }

    }

}