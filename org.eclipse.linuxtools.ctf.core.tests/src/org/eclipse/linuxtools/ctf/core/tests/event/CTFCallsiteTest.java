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

package org.eclipse.linuxtools.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.junit.Test;

/**
 * The class <code>CTFCallsiteTest</code> contains tests for the class
 * <code>{@link CTFCallsite}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */

public class CTFCallsiteTest {

    private static CTFCallsite GenerateCS(long ip){
        return new CTFCallsite("event name", "func name", ip, "file.java", 1);
    }

    /**
     * Test the constructor
     */
    @Test
    public void constructorTest(){
        CTFCallsite cs = GenerateCS(0x01);
        assertNotNull(cs);
    }

    /**
     * Test the comparator (it should sort using the IP)
     */
    @Test
    public void comparatorTest(){
        CTFCallsite cs[] = new CTFCallsite[5];
        long vals[] = {1L, 0L, -2L, 2L, -1L};
        for(int i = 0 ; i < 5 ; i++ ){
            cs[i] = GenerateCS(vals[i]);
        }

        assertEquals(1, cs[0].compareTo(cs[1]));
        assertEquals(-1, cs[1].compareTo(cs[0]));
        assertEquals(0, cs[0].compareTo(cs[0]));
        assertEquals(-1, cs[0].compareTo(cs[2]));
        assertEquals(1, cs[2].compareTo(cs[0]));

        Arrays.sort(cs);

        assertEquals( 0L, cs[0].getIp());
        assertEquals( 1L, cs[1].getIp());
        assertEquals( 2L, cs[2].getIp());
        assertEquals( -2L , cs[3].getIp());
        assertEquals( -1L, cs[4].getIp());
    }

    /**
     * Tests the output of a callsite toString function
     */
    @Test
    public void toStringTest(){
        CTFCallsite cs = GenerateCS(0x01);
        assertEquals("file.java/func name:1", cs.toString());
    }
}
