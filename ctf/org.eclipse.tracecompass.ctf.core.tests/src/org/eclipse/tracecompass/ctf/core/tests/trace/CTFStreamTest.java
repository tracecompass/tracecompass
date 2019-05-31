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

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamTest</code> contains tests for the class
 * <code>{@link CTFStream}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CTFStream fixture;

    private CTFStreamInput fInput;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @Before
    public void setUp() throws CTFException {
        fixture = new CTFStream(CtfTestTraceUtils.getTrace(testTrace));
        fixture.setEventContext(new StructDeclaration(1L));
        fixture.setPacketContext(new StructDeclaration(1L));
        fixture.setEventHeader(new StructDeclaration(1L));
        fixture.setId(1L);
        fInput = new CTFStreamInput(new CTFStream(CtfTestTraceUtils.getTrace(testTrace)), createFile());
        fixture.addInput(fInput);
    }

    private static @NonNull File createFile() throws CTFException {
        File path = new File(CtfTestTraceUtils.getTrace(testTrace).getPath());
        final File[] listFiles = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains("hann")) {
                    return true;
                }
                return false;
            }
        });
        assertNotNull(listFiles);
        final File returnFile = listFiles[0];
        assertNotNull(returnFile);
        return returnFile;
    }

    /**
     * Run the Stream(CTFTrace) constructor test.
     *
     * @throws CTFException
     */
    @Test
    public void testStream() throws CTFException {
        CTFTrace trace = CtfTestTraceUtils.getTrace(testTrace);
        CTFStream result = new CTFStream(trace);
        assertNotNull(result);
    }

    /**
     * Run the void addEvent(EventDeclaration) method test with the basic event.
     *
     * @throws ParseException
     */
    @Test
    public void testAddEvent_base() throws ParseException {
        EventDeclaration event = new EventDeclaration();
        fixture.addEvent(event);
    }


    /**
     * Run the void addEvent(EventDeclaration) method test with the basic huge id'ed event.
     *
     * @throws ParseException
     */
    @Test
    public void testAddHugeIDEvent_base() throws ParseException {
        EventDeclaration event = new EventDeclaration();
        event.setId(123456789);
        fixture.addEvent(event);
    }

    /**
     * Run the boolean eventContextIsSet() method test.
     */
    @Test
    public void testEventContextIsSet() {
        assertTrue(fixture.isEventContextSet());
    }

    /**
     * Run the boolean eventContextIsSet() method test.
     */
    @Test
    public void testToString() {
        assertNotNull(fixture.toString());
    }

    /**
     * Run the boolean eventHeaderIsSet() method test.
     */
    @Test
    public void testEventHeaderIsSet() {
        assertTrue(fixture.isEventHeaderSet());
    }

    /**
     * Run the StructDeclaration getEventContextDecl() method test.
     */
    @Test
    public void testGetEventContextDecl() {
        assertNotNull(fixture.getEventContextDecl());
    }

    /**
     * Run the StructDeclaration getEventHeaderDecl() method test.
     */
    @Test
    public void testGetEventHeaderDecl() {
        IDeclaration eventHeaderDecl = fixture.getEventHeaderDeclaration();
        assertNotNull(eventHeaderDecl);
    }

    /**
     * Run the HashMap<Long, EventDeclaration> getEvents() method test.
     */
    @Test
    public void testGetEvents() {
        assertNotNull(fixture.getEventDeclarations());
    }

    /**
     * Run the Long getId() method test.
     */
    @Test
    public void testGetId() {
        Long result = fixture.getId();
        assertNotNull(result);
    }

    /**
     * Run the StructDeclaration getPacketContextDecl() method test.
     */
    @Test
    public void testGetPacketContextDecl() {
        StructDeclaration result = fixture.getPacketContextDecl();
        assertNotNull(result);
    }

    /**
     * Run the Set<StreamInput> getStreamInputs() method test.
     */
    @Test
    public void testGetStreamInputs() {
        Set<CTFStreamInput> result = fixture.getStreamInputs();
        assertNotNull(result);
    }

    /**
     * Run the CTFTrace getTrace() method test.
     */
    @Test
    public void testGetTrace() {
        CTFTrace result = fixture.getTrace();
        assertNotNull(result);
    }

    /**
     * Run the boolean idIsSet() method test.
     */
    @Test
    public void testIdIsSet() {
        boolean result = fixture.isIdSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetContextIsSet() method test.
     */
    @Test
    public void testPacketContextIsSet() {
        boolean result = fixture.isPacketContextSet();
        assertTrue(result);
    }

    /**
     * Run the void setEventContext(StructDeclaration) method test.
     */
    @Test
    public void testSetEventContext() {
        StructDeclaration eventContext = new StructDeclaration(1L);
        fixture.setEventContext(eventContext);
    }

    /**
     * Run the void setEventHeader(StructDeclaration) method test.
     */
    @Test
    public void testSetEventHeader() {
        StructDeclaration eventHeader = new StructDeclaration(1L);
        fixture.setEventHeader(eventHeader);
    }

    /**
     * Run the void setId(long) method test.
     */
    @Test
    public void testSetId() {
        long id = 1L;
        fixture.setId(id);
    }

    /**
     * Run the void setPacketContext(StructDeclaration) method test.
     */
    @Test
    public void testSetPacketContext() {
        StructDeclaration packetContext = new StructDeclaration(1L);
        fixture.setPacketContext(packetContext);
    }
}
