/*******************************************************************************
 * Copyright (c) 2014 Ericsson
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

package org.eclipse.tracecompass.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the event definition
 *
 * @author Matthew Khouzam
 *
 */
public class CTFEventDefinitionTest {
    List<EventDefinition> fixture;

    /**
     * Making a power set of configurations to test the event definition
     */
    @Before
    public void init() {
        fixture = new ArrayList<>();
        IntegerDeclaration pidDec = IntegerDeclaration.createDeclaration(5, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 8);
        IntegerDeclaration ctxDec = IntegerDeclaration.createDeclaration(16, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 8);
        IntegerDefinition pid = new IntegerDefinition(pidDec, null, "pid", 3);
        IntegerDefinition pod = new IntegerDefinition(pidDec, null, "pod", 3);
        IntegerDefinition ctx = new IntegerDefinition(pidDec, null, "ctx", 3);

        StructDeclaration streamContextDec = new StructDeclaration(8);
        streamContextDec.addField("pid", pidDec);
        streamContextDec.addField("ctx", ctxDec);
        StructDeclaration eventContextDec = new StructDeclaration(8);
        eventContextDec.addField("pod", pidDec);
        eventContextDec.addField("ctx", pidDec);
        StructDeclaration fDec = new StructDeclaration(8);
        EventDeclaration eventDeclaration = new EventDeclaration();

        fDec.addField("pid", pidDec);
        fDec.addField("ctx", ctxDec);
        fDec.addField("pod", pidDec);

        Definition[] sDefs = { pid, ctx };
        Definition[] eDefs = { pod, ctx };
        Definition[] fDefs = { pid, ctx, pod };

        StructDeclaration pHeaderDec = new StructDeclaration(8);

        StructDefinition sContext = new StructDefinition(streamContextDec, null, ILexicalScope.STREAM_PACKET_CONTEXT.getPath(), sDefs);
        StructDefinition eContext = new StructDefinition(eventContextDec, null, ILexicalScope.STREAM_EVENT_CONTEXT.getPath(), eDefs);
        StructDefinition pHeader  = new StructDefinition(pHeaderDec, null, ILexicalScope.FIELDS.getPath(), new Definition[0]);
        StructDefinition fields = new StructDefinition(fDec, null, ILexicalScope.FIELDS.getPath(), fDefs);

        int cpu = IEventDefinition.UNKNOWN_CPU;

        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, pHeader, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, null, pHeader, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, pHeader, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, null, eContext, pHeader, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, pHeader, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, null, pHeader, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, null, fields, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, pHeader, null, null));
        fixture.add(new EventDefinition(eventDeclaration, cpu, 100, null, sContext, eContext, pHeader, fields, null));
    }

    /**
     * Test all the events
     */
    @Test
    public void testEvents() {
        int i = 0;
        for (EventDefinition ed : fixture) {
            test(i, ed);
            i++;
        }
    }

    private static void test(int rank, EventDefinition ed) {
        String title = "event #" + rank;
        assertEquals(title, 100L, ed.getTimestamp());
        ICompositeDefinition context = ed.getContext();
        if (rank >= 4) {
            assertNotNull(title, context);
            if (rank >= 12) {
                assertEquals(title, 3, context.getFieldNames().size());
            } else {
                assertEquals(title, 2, context.getFieldNames().size());
            }

        } else {
            assertNull(title, context);
        }
        if (((rank / 4) % 2) == 1) {
            assertNotNull(title, ed.getEventContext());
        }else{
            assertNull(title, ed.getEventContext());
        }
        if (rank % 2 == 1) {
            assertNotNull(title, ed.getFields());
            assertEquals(title, 3, ed.getFields().getFieldNames().size());
        } else {
            assertNull(title, ed.getFields());
        }
        assertTrue(title, ed.toString().startsWith("Event type: null" + System.getProperty("line.separator") + "Timestamp: 100"));
    }

}
