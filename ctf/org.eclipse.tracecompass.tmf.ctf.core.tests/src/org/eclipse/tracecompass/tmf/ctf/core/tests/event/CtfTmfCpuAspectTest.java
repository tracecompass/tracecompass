/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.event.aspect.CtfCpuAspect;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the CPU resolution aspect on a ctf trace.
 *
 * @author Matthew Khoumzam
 */
public class CtfTmfCpuAspectTest {

    private static final @NonNull String ROOT = "root";

    private List<@NonNull IEventDefinition> fEvents = new ArrayList<>();

    private CtfTmfTrace fTrace;

    /**
     * Create 3 event definitions and a trace, the event definitions should
     * stress the CPU aspect.
     *
     * @throws CTFException
     *             Won't happen, if the buffer isn't big enough throw an
     *             exception
     */
    @Before
    public void setUp() throws CTFException {

        fTrace = new CtfTmfTrace();
        int capacity = 2048;
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity);

        StructDeclaration sDec = new StructDeclaration(1l);
        IntegerDeclaration intDec = IntegerDeclaration.INT_8_DECL;

        sDec.addField("cpu_id", intDec);
        sDec.addField("CpuId", intDec);
        /* Set cpu_id = 2 */
        bb.put((byte) 2);
        /* Set CpuId = 3 */
        bb.put((byte) 3);
        StructDefinition def = sDec.createDefinition(null, ROOT, new BitBuffer(bb));
        EventDeclaration dec = new EventDeclaration();
        dec.setName("hi");
        /*
         * CPU in the stream == undefined cpu, has a cpu_id field set to 2,
         * resolve to 2
         */
        fEvents.add(new EventDefinition(dec, IEventDefinition.UNKNOWN_CPU, 0, null, null, null, null, def, null));
        /*
         * CPU in the stream == 3, has a cpu_id field set to 2, stream take
         * priority, resolve to 3
         */
        fEvents.add(new EventDefinition(dec, 3, 0, null, null, null, null, def, null));
        /*
         * CPU in the stream == undefined cpu, has a context with cpu_id,
         * unsupported, resolves to null
         */
        fEvents.add(new EventDefinition(dec, IEventDefinition.UNKNOWN_CPU, 0, null, null, null, def, null, null));
        /* CPU in the stream == undefined cpu, has a no fields */
        fEvents.add(new EventDefinition(dec, IEventDefinition.UNKNOWN_CPU, 0, null, null, null, null, null, null));
        /* CPU in the stream == 6, has a no fields */
        fEvents.add(new EventDefinition(dec, 6, 0, null, null, null, null, null, null));
    }

    /**
     * Cleanup
     */
    @After
    public void dispose() {
        fTrace.dispose();
    }

    /**
     * Test the cpu aspect
     */
    @Test
    public void test() {
        CtfCpuAspect fixture = new CtfCpuAspect();
        CtfTmfTrace trace = fTrace;
        assertNotNull(trace);
        CtfTmfEventFactory fabrica = CtfTmfEventFactory.instance();

        /*
         * Evaluate field, no stream
         */
        CtfTmfEvent e = fabrica.createEvent(trace, fEvents.get(0), "");
        assertEquals(Integer.valueOf(2), fixture.resolve(e));

        /*
         * Evaluate stream and field
         */
        e = fabrica.createEvent(trace, fEvents.get(1), "");
        assertEquals(Integer.valueOf(3), fixture.resolve(e));

        /*
         * Evaluate context
         */
        e = fabrica.createEvent(trace, fEvents.get(2), "");
        assertNull(fixture.resolve(e));

        /*
         * Evaluate an empty event
         */
        e = fabrica.createEvent(trace, fEvents.get(3), "");
        assertNull(fixture.resolve(e));

        /*
         * Evaluate stream and no field, default LTTng behaviour
         */
        e = fabrica.createEvent(trace, fEvents.get(4), "");
        assertEquals(Integer.valueOf(6), fixture.resolve(e));

        /*
         * Evaluate non-ctf event
         */
        assertNull(fixture.resolve(new TmfEvent(trace, 0, TmfTimestamp.BIG_BANG, null, null)));
    }
}
