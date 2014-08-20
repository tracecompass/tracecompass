/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.event;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
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
        streamContextDec.addField("pod", pidDec);
        streamContextDec.addField("ctx", pidDec);
        StructDeclaration fDec = new StructDeclaration(8);
        EventDeclaration eventDeclaration = new EventDeclaration();

        fDec.addField("pid", pidDec);
        fDec.addField("ctx", ctxDec);
        fDec.addField("pod", pidDec);

        List<String> sFieldNames = Arrays.asList("pid", "ctx");
        List<String> eFieldNames = Arrays.asList("pod", "ctx");
        List<String> fieldNames = Arrays.asList("pid", "ctx", "pod");

        Definition[] sDefs = { pid, ctx };
        Definition[] eDefs = { pod, ctx };
        Definition[] fDefs = { pid, ctx, pod };

        StructDeclaration pContextDec = new StructDeclaration(8);

        StructDefinition sContext = new StructDefinition(streamContextDec, null, LexicalScope.STREAM_PACKET_CONTEXT.toString(), sFieldNames, sDefs);
        StructDefinition eContext = new StructDefinition(eventContextDec, null, LexicalScope.STREAM_EVENT_CONTEXT.toString(), eFieldNames, eDefs);
        StructDefinition pContext = new StructDefinition(pContextDec, null, LexicalScope.FIELDS.toString(), Collections.EMPTY_LIST, new Definition[0]);
        StructDefinition fields = new StructDefinition(fDec, null, LexicalScope.FIELDS.toString(), fieldNames, fDefs);

        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, null, null, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, null, pContext, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, null, pContext, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, eContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, eContext, null, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, eContext, pContext, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, null, eContext, pContext, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, null, null, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, null, null, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, null, pContext, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, null, pContext, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, eContext, null, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, eContext, null, fields));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, eContext, pContext, null));
        fixture.add(new EventDefinition(eventDeclaration, null, 100, sContext, eContext, pContext, fields));
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
        StructDefinition context = ed.getContext();
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
        assertTrue(title, ed.toString().startsWith("Event type: null\nTimestamp: 100"));
    }

}
