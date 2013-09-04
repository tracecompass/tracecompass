/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputReaderTest</code> contains tests for the class
 * <code>{@link StreamInputReader}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class StreamInputReaderTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private StreamInputReader fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = getStreamInputReader();
        fixture.setName(1);
        fixture.setCurrentEvent(new EventDefinition(new EventDeclaration(),
                getStreamInputReader()));
    }

    private static StreamInputReader getStreamInputReader() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        CTFTrace trace = testTrace.getTrace();
        Stream s = trace.getStream((long) 0);
        Set<StreamInput> streamInput = s.getStreamInputs();
        StreamInputReader retVal = null;
        for (StreamInput si : streamInput) {
            /*
             * For the tests, we'll use the stream input corresponding to the
             * CPU 0
             */
            if (si.getFilename().endsWith("0_0")) {
                retVal = new StreamInputReader(si);
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
     * @throws CTFReaderException
     */
    @Test(expected = CTFReaderException.class)
    public void testStreamInputReader_invalid() throws CTFReaderException {
        StreamInput streamInput = new StreamInput(
                new Stream(new CTFTrace("")), (FileChannel) null, new File(""));

        StreamInputReader result = new StreamInputReader(streamInput);
        assertNotNull(result);
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
        EventDefinition result = fixture.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the StructDefinition getCurrentPacketContext() method test.
     */
    @Test
    public void testGetCurrentPacketContext() {
        StructDefinition result = fixture.getCurrentPacketContext();
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
     */
    @Test
    public void testGoToLastEvent1() {
        final long endTimestamp = goToEnd();
        final long endTime = 4287422460315L;
        assertEquals(endTime , endTimestamp  );
    }

    /**
     * Run the void goToLastEvent() method test.
     */
    @Test
    public void testGoToLastEvent2() {
        long timestamp = -1;
        while(fixture.readNextEvent()) {
            timestamp = fixture.getCurrentEvent().getTimestamp();
        }
        long endTimestamp = goToEnd();
        assertEquals(0 , timestamp- endTimestamp );
    }

    private long goToEnd() {
        fixture.goToLastEvent();
        return fixture.getCurrentEvent().getTimestamp();
    }

    /**
     * Run the boolean readNextEvent() method test.
     */
    @Test
    public void testReadNextEvent() {
        boolean result = fixture.readNextEvent();
        assertTrue(result);
    }

    /**
     * Run the void seek(long) method test. Seek by direct timestamp
     */
    @Test
    public void testSeek_timestamp() {
        long timestamp = 1L;
        fixture.seek(timestamp);
    }

    /**
     * Run the seek test. Seek by passing an EventDefinition to which we've
     * given the timestamp we want.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testSeek_eventDefinition() throws CTFReaderException {
        EventDefinition eventDefinition = new EventDefinition(
                new EventDeclaration(), getStreamInputReader());
        eventDefinition.setTimestamp(1L);
        fixture.setCurrentEvent(eventDefinition);
    }
}