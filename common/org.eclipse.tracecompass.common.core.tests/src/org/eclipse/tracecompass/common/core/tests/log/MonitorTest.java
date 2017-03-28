/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassMonitorManager;
import org.eclipse.tracecompass.internal.common.core.log.ITraceCompassMonitor;
import org.junit.Test;

/**
 * Test monitoring, this connects to the local server to do it.
 *
 * @author Matthew Khouzam
 */
public class MonitorTest {

    private static final String SUM = "sum";
    private static final String COUNT = "count";
    private static final String MEAN = "mean";
    private static final String MIN = "min";
    private static final String MAX = "max";

    /**
     * Test climbing array
     *
     * @throws MalformedObjectNameException
     *             bad name
     */
    @Test
    public void testClimbing() throws MalformedObjectNameException {
        long[] inputVector = { 1, 2, 3, 4, 5 };
        String name = "testClimbing";
        ITraceCompassMonitor fixture = populateVector(inputVector, name);
        assertNotNull(fixture);
        assertEquals(MEAN, 3, fixture.getMeanTime(), 0.00001);
        assertEquals(COUNT, inputVector.length, fixture.getCount());
        assertEquals(SUM, 15, fixture.getTotalTime());
        assertEquals(MIN, 1, fixture.getMinTime());
        assertEquals(MAX, 5, fixture.getMaxTime());
    }

    /**
     * Test descending array
     *
     * @throws MalformedObjectNameException
     *             bad name
     */
    @Test
    public void testDescending() throws MalformedObjectNameException {
        long[] inputVector = { 5, 4, 3, 2, 1 };
        String name = "testDescending";
        ITraceCompassMonitor fixture = populateVector(inputVector, name);
        assertNotNull(fixture);
        assertEquals(MEAN, 3, fixture.getMeanTime(), 0.00001);
        assertEquals(COUNT, inputVector.length, fixture.getCount());
        assertEquals(SUM, 15, fixture.getTotalTime());
        assertEquals(MIN, 1, fixture.getMinTime());
        assertEquals(MAX, 5, fixture.getMaxTime());
    }

    /**
     * Test same array
     *
     * @throws MalformedObjectNameException
     *             bad name
     */
    @Test
    public void testSame() throws MalformedObjectNameException {
        long[] inputVector = { 3, 3, 3, 3, 3 };
        String name = "testSame";
        ITraceCompassMonitor fixture = populateVector(inputVector, name);
        assertNotNull(fixture);
        assertEquals(MEAN, 3, fixture.getMeanTime(), 0.00001);
        assertEquals(COUNT, inputVector.length, fixture.getCount());
        assertEquals(SUM, 15, fixture.getTotalTime());
        assertEquals(MIN, 3, fixture.getMinTime());
        assertEquals(MAX, 3, fixture.getMaxTime());
    }

    private static ITraceCompassMonitor populateVector(long[] inputVector, String name) throws MalformedObjectNameException {
        assertNotNull(name);
        for (int i = 0; i < inputVector.length; i++) {
            TraceCompassMonitorManager.getInstance().update(name, inputVector[i]);
        }
        ITraceCompassMonitor fixture = getBean(name);
        assertNotNull(fixture);
        assertEquals("Contains name", name, fixture.getObservedElementName());
        return fixture;
    }

    private static ITraceCompassMonitor getBean(String label) throws MalformedObjectNameException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName("org.eclipse.tracecompass.common.core.log:type=TraceCompassMonitoring,name=" + label);
        @Nullable ITraceCompassMonitor fixture = JMX.newMXBeanProxy(mbs, mbeanName, ITraceCompassMonitor.class, true);
        assertNotNull(fixture);
        return fixture;
    }
}
