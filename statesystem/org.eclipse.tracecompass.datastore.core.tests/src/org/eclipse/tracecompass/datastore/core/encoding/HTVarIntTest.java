/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.datastore.core.encoding;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.PrimitiveIterator.OfLong;
import java.util.Random;

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
        OfLong randomStream = r.longs(0L, Long.MAX_VALUE).iterator();
        for (int i = 1; i < Long.BYTES; i++) {
            for (int l = 0; l < LOOP_COUNT; l++) {
                long value = randomStream.nextLong() >> (i * Byte.SIZE);
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
