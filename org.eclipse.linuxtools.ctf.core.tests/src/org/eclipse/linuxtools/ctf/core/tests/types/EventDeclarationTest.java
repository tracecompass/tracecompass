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

package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTraces;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EventDeclarationTest</code> contains tests for the class
 * <code>{@link EventDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class EventDeclarationTest {

    private static final int TRACE_INDEX = 0;

    private EventDeclaration fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(CtfTestTraces.tracesExist());
        fixture = new EventDeclaration();
        fixture.setContext(new StructDeclaration(1L));
        fixture.setId(1L);
        fixture.setFields(new StructDeclaration(1L));
        fixture.setStream(new Stream(CtfTestTraces.getTestTrace(TRACE_INDEX)));
        fixture.setName("");
    }

    /**
     * Run the EventDeclaration() constructor test.
     */
    @Test
    public void testEventDeclaration() {
        EventDeclaration result = new EventDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the boolean contextIsSet() method test.
     */
    @Test
    public void testContextIsSet() {
        boolean result = fixture.contextIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean contextIsSet() method test.
     */
    @Test
    public void testContextIsSet_null() {
        fixture.setContext((StructDeclaration) null);

        boolean result = fixture.contextIsSet();
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testEquals() throws CTFReaderException {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));
        obj.setStream(new Stream(CtfTestTraces.getTestTrace(TRACE_INDEX)));
        obj.setName("");

        assertTrue(fixture.equals(fixture));
        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_null() {
        Object obj = null;

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_emptyObject() {
        Object obj = new Object();

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_other1() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(fixture.getContext());

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_other2() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setFields(new StructDeclaration(1L));

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_other3() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals_other4() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));
        obj.setName("");

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean fieldsIsSet() method test.
     */
    @Test
    public void testFieldsIsSet() {
        boolean result = fixture.fieldsIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean fieldsIsSet() method test.
     */
    @Test
    public void testFieldsIsSet_null() {
        fixture.setFields((StructDeclaration) null);

        boolean result = fixture.fieldsIsSet();
        assertFalse(result);
    }

    /**
     * Run the StructDeclaration getFields() method test.
     */
    @Test
    public void testGetFields() {
        StructDeclaration result = fixture.getFields();
        assertNotNull(result);
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
     * Run the String getName() method test.
     */
    @Test
    public void testGetName() {
        String result = fixture.getName();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    public void testGetStream() {
        Stream result = fixture.getStream();
        assertNotNull(result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode() {
        int result = fixture.hashCode();
        assertTrue(0 != result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    public void testHashCode_null() {
        fixture.setStream((Stream) null);
        fixture.setName((String) null);

        int result = fixture.hashCode();
        assertTrue(0 != result);
    }

    /**
     * Run the boolean idIsSet() method test.
     */
    @Test
    public void testIdIsSet() {
        boolean result = fixture.idIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean nameIsSet() method test.
     */
    @Test
    public void testNameIsSet() {
        boolean result = fixture.nameIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean nameIsSet() method test.
     */
    @Test
    public void testNameIsSet_null() {
        fixture.setName((String) null);

        boolean result = fixture.nameIsSet();
        assertFalse(result);
    }

    /**
     * Run the boolean streamIsSet() method test.
     */
    @Test
    public void testStreamIsSet() {
        boolean result = fixture.streamIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean streamIsSet() method test.
     */
    @Test
    public void testStreamIsSet_null() {
        fixture.setStream((Stream) null);

        boolean result = fixture.streamIsSet();
        assertEquals(false, result);
    }

    /**
     * Test for the EventDefinition class
     *
     * @throws CTFReaderException
     */
    @Test
    public void testEventDefinition() throws CTFReaderException {
        CTFTrace trace = CtfTestTraces.getTestTrace(TRACE_INDEX);
        CTFTraceReader tr = new CTFTraceReader(trace);
        tr.advance();
        EventDefinition ed = new EventDefinition(null, null);
        ed = tr.getCurrentEventDef();
        assertNotNull(ed);
        assertNotNull(ed.getPath());
        assertNotNull(ed.getDeclaration());
        assertNotNull(ed.getFields());
        assertNull(ed.getContext());
        assertNotNull(ed.getPacketContext());
        assertNotNull(ed.getCPU());
        assertNotNull(ed.getPacketContext());
        assertNotNull(ed.getStreamInputReader());
        assertNull(ed.lookupDefinition("context"));
        assertNotNull(ed.lookupDefinition("fields"));
        assertNull(ed.lookupDefinition("other"));
        assertNotNull(ed.toString());
        ed.setContext( ed.getFields());
        assertNotNull(ed.toString());
    }

    EventDeclaration e1;
    EventDeclaration e2;


    @Test
    public void testEquals1(){
        e1 = new EventDeclaration();
        assertFalse(e1.equals(null));
    }

    @Test
    public void testEquals2(){
        e1 = EventDeclaration.getLostEventDeclaration();
        assertFalse(e1.equals(new Long(23L)));
    }

    @Test
    public void testEquals3(){
        e1 = EventDeclaration.getLostEventDeclaration();
        assertEquals(e1,e1);
    }

    @Test
    public void testEquals4(){
        e1 = EventDeclaration.getLostEventDeclaration();
        e2 = EventDeclaration.getLostEventDeclaration();
        assertEquals(e1,e2);
    }

    @Test
    public void testEquals5(){
        e1 = EventDeclaration.getLostEventDeclaration();
        e2 = new EventDeclaration();
        assertFalse(e1.equals(e2));
    }
}
