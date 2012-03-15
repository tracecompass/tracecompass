package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.channels.FileChannel;

import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EventDeclarationTest</code> contains tests for the class
 * <code>{@link EventDeclaration}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class EventDeclarationTest {

    private EventDeclaration fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(EventDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new EventDeclaration();
        fixture.setContext(new StructDeclaration(1L));
        fixture.setId(1L);
        fixture.setFields(new StructDeclaration(1L));
        fixture.setStream(new Stream(TestParams.createTrace()));
        fixture.setName(""); //$NON-NLS-1$
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
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
     * Run the EventDefinition createDefinition(StreamInputReader) method test.
     */
    @Test
    public void testCreateDefinition() {
        StreamInputReader streamInputReader = new StreamInputReader(
                new StreamInput(new Stream(TestParams.createTrace()),
                        (FileChannel) null, TestParams.getEmptyFile()));

        EventDefinition result = fixture.createDefinition(streamInputReader);
        assertNotNull(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    public void testEquals() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));
        obj.setStream(new Stream(TestParams.createTrace()));
        obj.setName(""); //$NON-NLS-1$

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
        obj.setName(""); //$NON-NLS-1$

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
     */
    @Test
    public void testEventDefinition() {
        CTFTrace trace = TestParams.createTrace();
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
        assertNull(ed.lookupDefinition("context")); //$NON-NLS-1$
        assertNotNull(ed.lookupDefinition("fields")); //$NON-NLS-1$
        assertNull(ed.lookupDefinition("other")); //$NON-NLS-1$
        assertNotNull(ed.toString());
        ed.context = ed.getFields();
        assertNotNull(ed.toString());

    }
}
