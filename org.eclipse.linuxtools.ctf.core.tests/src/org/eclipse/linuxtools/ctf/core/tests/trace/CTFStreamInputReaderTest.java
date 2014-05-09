/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFResponse;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFStream;
import org.eclipse.linuxtools.ctf.core.trace.CTFStreamInput;
import org.eclipse.linuxtools.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * The class <code>StreamInputReaderTest</code> contains tests for the class
 * <code>{@link CTFStreamInputReader}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputReaderTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CTFStreamInputReader fixture;

    private static ImmutableList<String> wrap(String s) {
        return ImmutableList.<String> builder().add(s).build();
    }

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
                getStreamInputReader(), 0, null, null,
                new StructDefinition(
                        new StructDeclaration(0),
                        null,
                        "packet",
                        wrap( "field" ),
                        new Definition[] { new StringDefinition(new StringDeclaration(), null, "field", "test") }),
                null)
                );
    }

    private static CTFStreamInputReader getStreamInputReader() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        CTFTrace trace = testTrace.getTrace();
        CTFStream s = trace.getStream((long) 0);
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
     * @throws CTFReaderException
     * @throws IOException
     */
    @Test(expected = CTFReaderException.class)
    public void testStreamInputReader_invalid() throws CTFReaderException, IOException {
        try (CTFStreamInput streamInput = new CTFStreamInput(new CTFStream(new CTFTrace("")), new File(""));
                CTFStreamInputReader result = new CTFStreamInputReader(streamInput)) {
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
        EventDefinition result = fixture.getCurrentEvent();
        assertNotNull(result);
    }

    /**
     * Run the StructDefinition getCurrentPacketContext() method test.
     */
    @Test
    public void testGetCurrentPacketContext() {
        StructDefinition result = fixture.getCurrentEvent().getPacketContext();
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
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGoToLastEvent1() throws CTFReaderException {
        final long endTimestamp = goToEnd();
        final long endTime = 4287422460315L;
        assertEquals(endTime, endTimestamp);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testGoToLastEvent2() throws CTFReaderException {
        long timestamp = -1;
        while (fixture.readNextEvent().equals(CTFResponse.OK)) {
            timestamp = fixture.getCurrentEvent().getTimestamp();
        }
        long endTimestamp = goToEnd();
        assertEquals(0, timestamp - endTimestamp);
    }

    private long goToEnd() throws CTFReaderException {
        fixture.goToLastEvent();
        return fixture.getCurrentEvent().getTimestamp();
    }

    /**
     * Run the boolean readNextEvent() method test.
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testReadNextEvent() throws CTFReaderException {
        assertEquals(CTFResponse.OK, fixture.readNextEvent());
    }

    /**
     * Run the void seek(long) method test. Seek by direct timestamp
     *
     * @throws CTFReaderException
     *             error
     */
    @Test
    public void testSeek_timestamp() throws CTFReaderException {
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
                new EventDeclaration(), getStreamInputReader(), 1L, null, null, null, null);
        fixture.setCurrentEvent(eventDefinition);
    }
}
