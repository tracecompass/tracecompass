/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.datastore.core.encoding;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Random;

import org.eclipse.tracecompass.datastore.core.encoding.HTVarInt;
import org.junit.Test;

/**
 * Test for HTVarInt
 *
 * @author David Piché
 *
 */
public class HTVarIntTest {

    private static final int LOOP_COUNT = 65535;

    /**
     * Test write long read long unsigned
     *
     */
    @Test
    public void ReadWriteLongTestUnsigned() {
        ByteBuffer bb = ByteBuffer.allocate(128);
        Random r= new Random();
        r.setSeed(0);
        for (int i = 1; i < Long.BYTES; i++) {
            for (int l = 0; l < LOOP_COUNT; l++) {
                long value = Math.abs(r.nextLong()) >> (i * Byte.SIZE);
                HTVarInt.writeLong(bb, value);
                bb.position(0);
                assertEquals(l + " " + Long.toHexString(value), value, HTVarInt.readLong(bb));
                bb.position(0);
            }
        }
    }

    /**
     * Test write long read long
     *
     */
   @Test
   public void ReadWriteLongTest() {
       ByteBuffer bb = ByteBuffer.allocate(128);
       Random r= new Random();
       r.setSeed(0);
       for (int i = 1; i < Long.BYTES; i++) {
           for (int l = 0; l < LOOP_COUNT; l++) {
               long value = r.nextLong() >> (i * Byte.SIZE);
               HTVarInt.writeLong(bb, value);
               bb.position(0);
               assertEquals(l + " " + Long.toHexString(value), value, HTVarInt.readLong(bb));
               bb.position(0);
           }
       }
   }

}
