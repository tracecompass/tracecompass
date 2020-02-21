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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.ISimpleDatatypeDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.ctf.core.trace.Metadata;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * The class <code>MetadataTest</code> contains tests for the class
 * <code>{@link Metadata}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class MetadataTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static final String mdStart = "typealias integer { size = 8; align = 8; signed = false; } := uint8_t;\n" +
            "    typealias integer { size = 16; align = 8; signed = false; } := uint16_t;\n" +
            "    typealias integer { size = 32; align = 8; signed = false; } := uint32_t;\n" +
            "    typealias integer { size = 64; align = 8; signed = false; } := uint64_t;\n" +
            "    typealias integer { size = 64; align = 8; signed = false; } := unsigned long;\n" +
            "    typealias integer { size = 5; align = 1; signed = false; } := uint5_t;\n" +
            "    typealias integer { size = 27; align = 1; signed = false; } := uint27_t;\n" +
            "" +
            "    trace {\n" +
            "        major = 1;\n" +
            "        minor = 8;\n" +
            "        uuid = \"8b1258ba-effb-554b-b779-fbd676746000\";\n" +
            "        byte_order = le;\n" +
            "        packet.header := struct {\n" +
            "            uint32_t magic;\n" +
            "            uint8_t  uuid[16];\n" +
            "            uint32_t stream_id;\n" +
            "        };\n" +
            "    };\n" +
            "" +
            "    env {\n" +
            "        hostname = \"computer\";\n" +
            "        domain = \"kernel\";\n" +
            "        sysname = \"BeOS\";\n" +
            "        kernel_release = \"95\";\n" +
            "        kernel_version = \"BeWare 95\";\n" +
            "        tracer_name = \"BeOS Tracer\";\n" +
            "        tracer_major = 2;\n" +
            "        tracer_minor = 3;\n" +
            "        tracer_patchlevel = 0;\n" +
            "    };\n" +
            "    clock {\n" +
            "        name = monotonic;\n" +
            "        uuid = \"4d737a79-e3f1-4f4d-a649-42015266baf5\";\n" +
            "        description = \"Monotonic Clock\";\n" +
            "        freq = 1000000000; /* Frequency, in Hz */\n" +
            "        /* clock value offset from Epoch is: offset * (1/freq) */\n" +
            "        offset = 1383600210829415521;\n" +
            "    };\n" +

            "    typealias integer {\n" +
            "size = 27; align = 1; signed = false;\n" +
            "        map = clock.monotonic.value;\n" +
            "    } := uint27_clock_monotonic_t;\n" +
            "    \n" +
            "    typealias integer {\n" +
            "        size = 32; align = 8; signed = false;\n" +
            "        map = clock.monotonic.value;\n" +
            "    } := uint32_clock_monotonic_t;\n" +
            "    \n" +
            "    typealias integer {\n" +
            "        size = 64; align = 8; signed = false;\n" +
            "        map = clock.monotonic.value;\n" +
            "    } := uint64_clock_monotonic_t;\n" +
            "    \n" +
            "    struct packet_context {\n" +
            "        uint64_clock_monotonic_t timestamp_begin;\n" +
            "        uint64_clock_monotonic_t timestamp_end;\n" +
            "        uint64_t content_size;\n" +
            "        uint64_t packet_size;\n" +
            "        unsigned long events_discarded;\n" +
            "        uint32_t cpu_id;\n" +
            "    };\n" +
            "    \n" +
            "    struct event_header_compact {\n" +
            "        enum : uint5_t { compact = 0 ... 30, extended = 31 } id;\n" +
            "        variant <id> {\n" +
            "            struct {\n" +
            "                uint27_clock_monotonic_t timestamp;\n" +
            "            } compact;\n" +
            "            struct {\n" +
            "                uint32_t id;\n" +
            "                uint64_clock_monotonic_t timestamp;\n" +
            "            } extended;\n" +
            "        } v;\n" +
            "    } align(8);\n" +
            "    \n" +
            "    struct event_header_large {\n" +
            "        enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;\n" +
            "        variant <id> {\n" +
            "            struct {\n" +
            "                uint32_clock_monotonic_t timestamp;\n" +
            "            } compact;\n" +
            "            struct {\n" +
            "                uint32_t id;\n" +
            "                uint64_clock_monotonic_t timestamp;\n" +
            "            } extended;\n" +
            "        } v;\n" +
            "    } align(8);\n" +
            "    \n" +
            "    stream {\n" +
            "        id = 0;\n" +
            "        event.header := struct event_header_compact;\n" +
            "        packet.context := struct packet_context;\n" +
            "    };\n" +
            "    \n" +
            "    event {\n" +
            "        name = sched_switch;\n" +
            "        id = 0;\n" +
            "        stream_id = 0;\n" +
            "        fields := struct {\n" +
            "            integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _prev_comm[16];\n" +
            "            integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _prev_tid;\n" +
            "            integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _prev_prio;\n" +
            "            integer { size = 64; align = 8; signed = 1; encoding = none; base = 10; } _prev_state;\n" +
            "            integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _next_comm[16];\n" +
            "            integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _next_tid;\n" +
            "            integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _next_prio;\n" +
            "        };\n" +
            "    };";

    private static final String mdSecond = "    event {\n" +
            "        name = bozo_the_clown;\n" +
            "        id = 1;\n" +
            "        stream_id = 0;\n" +
            "        fields := struct {\n" +
            "            integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } clown_nose;\n" +
            "        };\n" +
            "    };";

    private static final String ENDIAN_CHANGE_L_B =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16; byte_order = be; signed = true; base = dec; } := INT;"
            + "trace { byte_order = le; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";

    private static final String ENDIAN_CHANGE_L_N =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16; signed = true; base = dec; } := INT;"
            + "trace { byte_order = le; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";

    private static final String ENDIAN_CHANGE_L_L =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16; byte_order = le; signed = true; base = dec; } := INT;"
            + "trace { byte_order = le; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";


    private static final String ENDIAN_CHANGE_B_L =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16;  byte_order = le; signed = true; base = dec; } := INT;"
            + "trace { byte_order = be; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";

    private static final String ENDIAN_CHANGE_B_N =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16; signed = true; base = dec; } := INT;"
            + "trace { byte_order = be; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";

    private static final String ENDIAN_CHANGE_B_B =
            "/* ctf 1.8 */"
            + "typealias integer { size = 32; align = 16; byte_order = be; signed = true; base = dec; } := INT;"
            + "trace { byte_order = be; };"
            + "event { "
            + " name = \"bob\"; "
            + " fields := struct field { INT data ; };"
            + "};";


    private Metadata fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @Before
    public void setUp() throws CTFException {
        fixture = new Metadata(CtfTestTraceUtils.getTrace(testTrace));
    }

    /**
     * Run the Metadata(CTFTrace) constructor test.
     */
    @Test
    public void testMetadata() {
        assertNotNull(fixture);
    }

    @Test
    public void testTextMD() throws CTFException {
        testSingleFragment();
    }

    protected CTFTrace testSingleFragment() throws CTFException {
        fixture = new Metadata();
        CTFTrace trace = fixture.getTrace();
        for (ICTFStream s : trace.getStreams()) {
            fail("This should be empty, has" + s.toString());
        }
        fixture.parseText(mdStart);
        int count = 0;
        for (ICTFStream s : trace.getStreams()) {
            count++;
            assertNotNull(s);
        }
        assertEquals(1, count);
        assertEquals(1, trace.getEventDeclarations(0L).size());
        return trace;
    }

    @Test
    public void testStreamTextMD() throws CTFException {
        CTFTrace trace = testSingleFragment();
        fixture.parseTextFragment(mdSecond);
        final List<IEventDeclaration> eventDeclarations = new ArrayList<>(trace.getEventDeclarations(0L));
        assertEquals(2, eventDeclarations.size());
        assertEquals("bozo_the_clown", eventDeclarations.get(1).getName());
    }

    /**
     * Run the ByteOrder getDetectedByteOrder() method test.
     *
     * @throws CTFException
     */
    @Test
    public void testGetDetectedByteOrder() throws CTFException {
        setUp();
        ByteOrder result = fixture.getDetectedByteOrder();
        assertNull(result);
    }

    /**
     * Test toString
     *
     * @throws CTFException
     */
    @Test
    public void testToString() throws CTFException {
        setUp();
        String result = fixture.toString();
        assertNotNull(result);
    }

    /**
     * Test a changing endian event field
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testEndian() throws CTFException {
        testEndianess(ENDIAN_CHANGE_L_B, ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN);
        testEndianess(ENDIAN_CHANGE_L_N, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
        testEndianess(ENDIAN_CHANGE_L_L, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
        testEndianess(ENDIAN_CHANGE_B_L, ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN);
        testEndianess(ENDIAN_CHANGE_B_N, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN);
        testEndianess(ENDIAN_CHANGE_B_B, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN);
    }

    private void testEndianess(String tsdl, ByteOrder traceEndian, ByteOrder fieldEndian) throws CTFException {
        fixture = new Metadata();
        CTFTrace trace = fixture.getTrace();
        fixture.parseText(tsdl);
        assertEquals(traceEndian, trace.getByteOrder());
        final Iterable<IEventDeclaration> eventDeclarations = trace.getEventDeclarations(0L);
        assertNotNull(eventDeclarations);
        IEventDeclaration event = Iterables.getFirst(eventDeclarations, null);
        assertNotNull(event);
        assertNotNull(event.getFields());
        final @Nullable IDeclaration field = event.getFields().getField("data");
        assertNotNull(field);
        if (field instanceof ISimpleDatatypeDeclaration) {
            ISimpleDatatypeDeclaration declaration = (ISimpleDatatypeDeclaration) field;
            assertEquals(fieldEndian, declaration.getByteOrder());
        } else {
            fail("data is not the right type");
        }
    }

    /**
     * Run the void parse() method test.
     *
     * @throws CTFException
     */
    @Test
    public void testParse() throws CTFException {
        setUp();
        assertEquals(new UUID(0xd18e637435a1cd42L, 0x8e70a9cffa712793L), CtfTestTraceUtils.getTrace(testTrace).getUUID());
        assertEquals(1332166405241713920.0, CtfTestTraceUtils.getTrace(testTrace).getClock().getClockOffset(), 200.0);
        assertEquals(8, CtfTestTraceUtils.getTrace(testTrace).getEnvironment().size());
    }
}
