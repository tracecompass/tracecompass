/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend.historytree;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the reading/writing logic in {@link HTInterval}, including unknown
 * object types.
 *
 * @author Bernd Hufmann
 */
@RunWith(Parameterized.class)
@NonNullByDefault({})
public class HTIntervalObjectReadWriteTest {
    private Object fTestObject;

    /**
     * Parameter generator.
     *
     * Generates different objects to be serialized.
     *
     * @return The test parameters
     */
    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        List<Object[]> list = new ArrayList<>();
        Object array[] = new Object[] { String.valueOf("Hello") };
        list.add(array);
        array = new Object[] { new TestObject("Test", 0) };
        list.add(array);
        return list;
    }

    /**
     * Test constructor
     *
     * @param obj
     *            The object to put in state system.
     */
    public HTIntervalObjectReadWriteTest(Object obj) {
        fTestObject = obj;
    }

    /**
     * Test method.
     *
     * @throws IOException
     *             Fails the test
     */
    @Test
    public void testHtInterval() throws IOException {
        HTInterval interval = new HTInterval(0, 10, 1, fTestObject);
        writeAndReadInterval(interval);
    }

    private static void writeAndReadInterval(HTInterval interval) throws IOException {
        int sizeOnDisk = interval.getSizeOnDisk();

        /* Write the interval to a file */
        File tempFile = File.createTempFile("test-interval", ".interval");
        try (FileOutputStream fos = new FileOutputStream(tempFile, false);
                FileChannel fc = fos.getChannel();) {

            ByteBuffer bb = ByteBuffer.allocate(sizeOnDisk);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.clear();

            interval.writeInterval(bb, 1);
            bb.flip();
            int written = fc.write(bb);
            assertEquals(sizeOnDisk, written);
        }

        /* Read the interval from the file */
        HTInterval readInterval;
        try (FileInputStream fis = new FileInputStream(tempFile);
                FileChannel fc = fis.getChannel();) {

            ByteBuffer bb = ByteBuffer.allocate(sizeOnDisk);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.clear();

            int read = fc.read(bb);
            assertEquals(sizeOnDisk, read);
            bb.flip();
            readInterval = HTInterval.readFrom(bb, 1);
        }

        assertEquals(interval.toString(), readInterval.toString());
    }

    private static class TestObject {
        private final String fName;
        private final int fId;
        private TestObject(String name, int id) {
            fName = name;
            fId = id;
        }
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(fName).append(fId);
            return b.toString();
        }
    }
}
