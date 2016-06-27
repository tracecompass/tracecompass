/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend.historytree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Test the reading/writing logic in {@link HTInterval}, particularly regarding
 * the string length limitations.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(Parameterized.class)
@NonNullByDefault({})
public class HTIntervalStringReadWriteTest {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private int fNbChars;
    private int fCharLength;
    private char fChar;

    /**
     * Parameter generator.
     *
     * Generates a combination of all possible string lenghts and all possible
     * character lengths.
     *
     * @return The test parameters
     */
    @Parameters(name = "nb of chars: {0}, char length: {1}")
    public static Iterable<Object[]> getTestParams() {
        Set<List<Integer>> set = Sets.cartesianProduct(ImmutableList.of(
                ImmutableSet.of(
                        0,
                        10,
                        Byte.MAX_VALUE - 1,
                        (int) Byte.MAX_VALUE,
                        Byte.MAX_VALUE + 1,

                        Short.MAX_VALUE / 2 - 1,
                        Short.MAX_VALUE / 2,
                        Short.MAX_VALUE / 2 + 1,

                        Short.MAX_VALUE / 3 - 1,
                        Short.MAX_VALUE / 3,
                        Short.MAX_VALUE / 3 + 1,

                        Short.MAX_VALUE - 1,
                        (int) Short.MAX_VALUE,
                        Short.MAX_VALUE + 1),

                ImmutableSet.of(1, 2, 3)
                ));

        return set.stream()
                .map(List::toArray)
                .collect(Collectors.toList());
    }

    /**
     * Test constructor, take the generated parameters as parameters.
     *
     * @param nbChars
     *            The number of characters in the test string.
     * @param charLength
     *            The length (in bytes) of the UTF-8-encoded form of the
     *            character being used.
     */
    public HTIntervalStringReadWriteTest(Integer nbChars, Integer charLength) {
        fNbChars = nbChars.intValue();
        fCharLength = charLength.intValue();
        switch (charLength) {
        case 1:
            fChar = 'a';
            break;
        case 2:
            fChar = 'é';
            break;
        case 3:
            fChar = '长'; // "chang" means long / length in Chinese!
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Test method
     *
     * @throws IOException
     *             Fails the test
     */
    @Test
    public void testStringWithChars() throws IOException {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, fNbChars).forEach(i -> sb.append(fChar));
        String str = sb.toString();
        assertEquals(fNbChars, str.length());
        assertEquals(fNbChars * fCharLength, str.getBytes(CHARSET).length);

        TmfStateValue value = TmfStateValue.newValueString(str);
        if (fNbChars * fCharLength > Short.MAX_VALUE) {
            /* For sizes that are known to be too long, expect an exception */
            try {
                new HTInterval(0, 10, 1, value);
            } catch (IllegalArgumentException e) {
                return;
            }
            fail();
        }
        HTInterval interval = new HTInterval(0, 10, 1, value);
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

            interval.writeInterval(bb);
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
            readInterval = HTInterval.readFrom(bb);
        }

        assertEquals(interval, readInterval);
    }
}
