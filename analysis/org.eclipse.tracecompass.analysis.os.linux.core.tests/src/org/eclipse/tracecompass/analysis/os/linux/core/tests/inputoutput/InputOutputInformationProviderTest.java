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
package org.eclipse.tracecompass.analysis.os.linux.core.tests.inputoutput;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestFactory;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Disk;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.InputOutputInformationProvider;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link InputOutputInformationProvider} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class InputOutputInformationProviderTest extends AbstractTestInputOutput {

    private final LinuxTestCase fTestCase;
    private final int fDiskCount;

    /**
     * Constructor
     *
     * @param testName
     *            A name for the test, to display in the header
     * @param test
     *            A test case parameter for this test
     * @param diskCount
     *            The number of disks
     */
    public InputOutputInformationProviderTest(String testName, LinuxTestCase test, int diskCount) {
        super();
        fTestCase = test;
        fDiskCount = diskCount;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        super.deleteTrace();
    }

    @Override
    protected @NonNull InputOutputAnalysisModule setUp(String fileName) {
        InputOutputAnalysisModule module = super.setUp(fileName);
        TmfTestHelper.executeAnalysis(module);
        return module;
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { IoTestFactory.SIMPLE_REQUESTS.getTraceFileName(), IoTestFactory.SIMPLE_REQUESTS, 1 },
                { IoTestFactory.SIMPLE_NO_STATEDUMP.getTraceFileName(), IoTestFactory.SIMPLE_NO_STATEDUMP, 1 },
                { IoTestFactory.TWO_DEVICES.getTraceFileName(), IoTestFactory.TWO_DEVICES, 3 }
        });
    }

    /**
     * Test the
     * {@link InputOutputInformationProvider#getDisks(InputOutputAnalysisModule)}
     * method
     */
    @Test
    public void testGetDisks() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());
        Collection<Disk> disks = InputOutputInformationProvider.getDisks(module);
        assertEquals(fDiskCount, disks.size());
    }

}
