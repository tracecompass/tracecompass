/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.tests.inputoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Disk;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.IoOperationType;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestCase.DiskInfo;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestCase.SectorCountInfo;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestFactory;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link Disk} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class InputOutputDiskTest extends AbstractTestInputOutput {

    private final IoTestCase fTestCase;

    /**
     * Constructor
     *
     * @param testName
     *            A name for the test, to display in the header
     * @param test
     *            A test case parameter for this test
     */
    public InputOutputDiskTest(String testName, IoTestCase test) {
        super();
        fTestCase = test;

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
                { IoTestFactory.SIMPLE_REQUESTS.getTraceFileName(), IoTestFactory.SIMPLE_REQUESTS },
                { IoTestFactory.SIMPLE_NO_STATEDUMP.getTraceFileName(), IoTestFactory.SIMPLE_NO_STATEDUMP },
                { IoTestFactory.TWO_DEVICES.getTraceFileName(), IoTestFactory.TWO_DEVICES }
        });
    }

    private static Disk getDisk(@NonNull InputOutputAnalysisModule module, int deviceId) {
        return InputOutputInformationProvider.getDisks(module).stream().filter(d -> deviceId == d.getDeviceId()).findFirst().get();
    }

    /**
     * Test the {@link Disk#getSectorsAt(long, IoOperationType)} method
     * method
     */
    @Test
    public void testSectorsAt() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());

        for (Integer deviceId : fTestCase.getSectorCount().keySet()) {
            Disk disk = getDisk(module, deviceId);
            assertNotNull(disk);
            for (SectorCountInfo info : fTestCase.getSectorCount().get(deviceId)) {
                double sectorsAt = disk.getSectorsAt(info.getTimestamp(), info.getType());
                assertEquals("Sectors at " + info.getTimestamp() + " for type " + info.getType(), info.getNbSectors(), sectorsAt, 1.0);
            }
        }
    }

    /**
     * Test the {@link Disk#getDeviceIdString()} and
     * {@link Disk#getDiskName()} methods
     */
    @Test
    public void testDeviceStrings() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());

        for (Entry<Integer, DiskInfo> deviceInfo : fTestCase.getDiskInfo().entrySet()) {
            Integer deviceId = deviceInfo.getKey();
            DiskInfo diskInfo = deviceInfo.getValue();
            Disk disk = getDisk(module, deviceId);
            assertNotNull(disk);
            assertEquals("Device ID string for " + deviceId, diskInfo.getDeviceString(), disk.getDeviceIdString());
            assertEquals("Disk name string for " + deviceId, diskInfo.getDeviceName(), disk.getDiskName());
            assertEquals("Disk activity for " + deviceId, diskInfo.hasActivity(), disk.hasActivity());
        }
    }
}
