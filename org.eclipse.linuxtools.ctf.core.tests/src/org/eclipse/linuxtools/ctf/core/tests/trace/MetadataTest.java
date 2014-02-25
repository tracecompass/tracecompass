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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.nio.ByteOrder;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Metadata;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.junit.Before;
import org.junit.Test;

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

    private Metadata fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        fixture = new Metadata(testTrace.getTrace());
    }

    /**
     * Run the Metadata(CTFTrace) constructor test.
     */
    @Test
    public void testMetadata() {
        assertNotNull(fixture);
    }

    @Test
    public void testTextMD() throws CTFReaderException {
        testSingleFragment();
    }

    protected CTFTrace testSingleFragment() throws CTFReaderException {
        fixture = new Metadata();
        CTFTrace trace = fixture.getTrace();
        for (Stream s : trace.getStreams()) {
            fail("This should be empty, has" + s.toString());
        }
        fixture.parseText(mdStart);
        int count = 0;
        for (Stream s : trace.getStreams()) {
            count++;
            assertNotNull(s);
        }
        assertEquals(1, count);
        assertEquals(1, trace.getEvents(0L).size());
        return trace;
    }

    @Test
    public void testStreamTextMD() throws CTFReaderException {
        CTFTrace trace = testSingleFragment();
        fixture.parseTextFragment(mdSecond);
        assertEquals(2, trace.getEvents(0L).size());
        assertEquals("bozo_the_clown", trace.getEvents(0L).get(1L).getName());
    }

    /**
     * Run the ByteOrder getDetectedByteOrder() method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testGetDetectedByteOrder() throws CTFReaderException {
        setUp();
        ByteOrder result = fixture.getDetectedByteOrder();
        assertNull(result);
    }

    /**
     * Test toString
     *
     * @throws CTFReaderException
     */
    @Test
    public void testToString() throws CTFReaderException {
        setUp();
        String result = fixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the void parse() method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testParse() throws CTFReaderException {
        setUp();
        assertEquals(new UUID(0xd18e637435a1cd42L, 0x8e70a9cffa712793L), testTrace.getTrace().getUUID());
        assertEquals(1332166405241713920.0, testTrace.getTrace().getClock().getClockOffset(), 200.0);
        assertEquals(8, testTrace.getTrace().getEnvironment().size());
    }
}
