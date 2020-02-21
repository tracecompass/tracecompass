/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFResponse;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputReaderTest</code> contains tests for the class
 * <code>{@link CTFStreamInputReader}</code>.
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputReaderTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CTFStreamInputReader fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @Before
    public void setUp() throws CTFException {
        fixture = getStreamInputReader();
        fixture.setName(1);
        fixture.setCurrentEvent(new EventDefinition(new EventDeclaration(),
                fixture.getCPU(), 0, null, null, null,
                new StructDefinition(
                        new StructDeclaration(0),
                        null,
                        "packet",
                        new Definition[] { new StringDefinition(StringDeclaration.getStringDeclaration(Encoding.UTF8), null, "field", "test") }),
                null, fixture.getCurrentPacketReader().getCurrentPacket()));
    }

    private static CTFStreamInputReader getStreamInputReader() throws CTFException {
        CTFTrace trace = CtfTestTraceUtils.getTrace(testTrace);
        ICTFStream s = trace.getStream((long) 0);
        Set<CTFStreamInput> streamInput = s.getStreamInputs();
        CTFStreamInputReader retVal = null;
        for (CTFStreamInput si : streamInput) {
            /*
             * For the tests, we'll use the stream input corresponding to the
             * CPU 0
             */
            if (si.getFilename().endsWith("0_0")) {
                retVal = new CTFStreamInputReader(si);
                break;
            }
        }
        return retVal;
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with a valid
     * trace.
     */
    @Test
    public void testStreamInputReader_valid() {
        assertNotNull(fixture);
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with an invalid
     * trace.
     *
     * @throws CTFException
     * @throws IOException
     */
    @Test(expected = CTFException.class)
    public void testStreamInputReader_invalid() throws CTFException, IOException {
        CTFStreamInput streamInput = new CTFStreamInput(new CTFStream(new CTFTrace("")), new File(""));
        try (CTFStreamInputReader result = new CTFStreamInputReader(streamInput)) {
            assertNotNull(result);
        }
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    public void testGetCPU() {
        int result = fixture.getCPU();
        assertEquals(0, result);
    }

    /**
     * Run the EventDefinition getCurrentEvent() method test.
     */
    @Test
    public void testGetCurrentEvent() {
        assertNotNull(fixture.getCurrentEvent());
    }

    /**
     * Run the StructDefinition getCurrentPacketContext() method test.
     */
    @Test
    public void testGetCurrentPacketContext() {
        IEventDefinition currentEvent = fixture.getCurrentEvent();
        assertNotNull(currentEvent);
        ICompositeDefinition result = currentEvent.getPacketContext();
        assertNotNull(result);
    }

    /**
     * Run the int getName() method test.
     */
    @Test
    public void testGetName() {
        int result = fixture.getName();
        assertEquals(1, result);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGoToLastEvent1() throws CTFException {
        final long endTimestamp = goToEnd();
        final long endTime = 4287422460315L;
        assertEquals(endTime, endTimestamp);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testGoToLastEvent2() throws CTFException {
        long timestamp = -1;
        while (fixture.readNextEvent().equals(CTFResponse.OK)) {
            IEventDefinition currentEvent = fixture.getCurrentEvent();
            assertNotNull(currentEvent);
            timestamp = currentEvent.getTimestamp();
        }
        long endTimestamp = goToEnd();
        assertEquals(0, timestamp - endTimestamp);
    }

    private long goToEnd() throws CTFException {
        fixture.goToLastEvent();
        IEventDefinition currentEvent = fixture.getCurrentEvent();
        assertNotNull(currentEvent);
        return currentEvent.getTimestamp();
    }

    /**
     * Run the boolean readNextEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testReadNextEvent() throws CTFException {
        assertEquals(CTFResponse.OK, fixture.readNextEvent());
    }

    /**
     * Run the void seek(long) method test. Seek by direct timestamp
     *
     * @throws CTFException
     *             error
     */
    @Test
    public void testSeek_timestamp() throws CTFException {
        long timestamp = 1L;
        fixture.seek(timestamp);
    }

    /**
     * Run the seek test. Seek by passing an EventDefinition to which we've
     * given the timestamp we want.
     *
     * @throws CTFException
     *             error
     * @throws IOException
     *             file not there
     */
    @Test
    public void testSeek_eventDefinition() throws CTFException, IOException {
        try (CTFStreamInputReader streamInputReader = getStreamInputReader()) {
            EventDefinition eventDefinition = new EventDefinition(
                    new EventDeclaration(), streamInputReader.getCPU(), 1L, null, null, null, null, null, streamInputReader.getCurrentPacketReader().getCurrentPacket());
            fixture.setCurrentEvent(eventDefinition);
        }
    }
}
