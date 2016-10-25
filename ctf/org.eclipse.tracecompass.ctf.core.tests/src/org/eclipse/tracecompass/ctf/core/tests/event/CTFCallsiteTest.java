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

package org.eclipse.tracecompass.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.ctf.core.event.CTFCallsite;
import org.junit.Test;

/**
 * The class <code>CTFCallsiteTest</code> contains tests for the class
 * <code>{@link CTFCallsite}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */

public class CTFCallsiteTest {

    private static CTFCallsite GenerateCS(long ip) {
        return new CTFCallsite("event name", "func name", ip, "file.java", 1);
    }

    /**
     * Test the constructor
     */
    @Test
    public void constructorTest() {
        CTFCallsite cs = GenerateCS(0x01);
        assertNotNull(cs);
    }

    /**
     * Test the getters
     */
    @Test
    public void getterTest() {
        CTFCallsite cs = GenerateCS(0x01);
        assertEquals("instruction pointer", 1, cs.getIp());
        assertEquals("event name", "event name", cs.getEventName());
        assertEquals("file name", "file.java", cs.getFileName());
        assertEquals("function name", "func name", cs.getFunctionName());
        assertEquals("line number", 1, cs.getLineNumber());
    }

    /**
     * Test the hash code
     */
    @Test
    public void hashCodeTest() {
        CTFCallsite cs = GenerateCS(0x01);
        Map<CTFCallsite, Object> test = new HashMap<>();
        test.put(cs, new Object());
        assertTrue(test.containsKey(cs));
        assertTrue(test.containsKey(GenerateCS(0x01)));
        assertFalse(test.containsKey(GenerateCS(0x02)));
        assertFalse(test.containsKey(new CTFCallsite("event nam", "func name", 1, "file.java", 1)));
        assertFalse(test.containsKey(new CTFCallsite("event name", "func nam", 1, "file.java", 1)));
        assertFalse(test.containsKey(new CTFCallsite("event name", "func name", 1, "file.jav", 1)));
        assertFalse(test.containsKey(new CTFCallsite("event name", "func name", 1, "file.java", 2)));
    }

    /**
     * Test equality
     */
    @Test
    public void equalsTest() {
        CTFCallsite cs = GenerateCS(0x01);
        CTFCallsite cs1 = GenerateCS(0x01);
        assertEquals(cs, cs);
        assertEquals(cs, cs1);
        assertEquals(cs, new CTFCallsite("event name", "func name", 1, "file.java", 1));
        assertFalse(cs.equals(null));
        assertFalse(cs.equals(-1));
        assertFalse(cs.equals(new CTFCallsite("event nam", "func name", 1, "file.java", 1)));
        assertFalse(cs.equals(new CTFCallsite("event name", "func nam", 1, "file.java", 1)));
        assertFalse(cs.equals(new CTFCallsite("event name", "func name", 2, "file.java", 1)));
        assertFalse(cs.equals(new CTFCallsite("event name", "func name", 1, "file.jav", 1)));
        assertFalse(cs.equals(new CTFCallsite("event name", "func name", 1, "file.java", 2)));
    }

    /**
     * Test the comparator (it should sort using the IP)
     */
    @Test
    public void comparatorTest() {
        CTFCallsite cs[] = new CTFCallsite[5];
        long vals[] = { 1L, 0L, -2L, 2L, -1L };
        for (int i = 0; i < 5; i++) {
            cs[i] = GenerateCS(vals[i]);
        }

        Arrays.sort(cs, (o1, o2) -> Long.compareUnsigned(o1.getIp(), o2.getIp()));

        assertEquals(0L, cs[0].getIp());
        assertEquals(1L, cs[1].getIp());
        assertEquals(2L, cs[2].getIp());
        assertEquals(-2L, cs[3].getIp());
        assertEquals(-1L, cs[4].getIp());
    }

    /**
     * Tests the output of a callsite toString function
     */
    @Test
    public void toStringTest() {
        CTFCallsite cs = GenerateCS(0x01);
        assertEquals("file.java/func name:1", cs.toString());
    }
}
